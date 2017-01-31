/*
 * Copyright (C) 2014-2017 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rwth.idsg.mb.controller.repository;

import de.ivu.realtime.modules.ura.data.response.StopPoint;
import de.rwth.idsg.mb.Constants;
import de.rwth.idsg.mb.pg.CustomDSL;
import de.rwth.idsg.mb.pg.TsRange;
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.enums.Status;
import jooq.db.public_.routines.Geography3;
import jooq.db.public_.routines.Geometry2;
import jooq.db.public_.routines.StDwithin3;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record2;
import org.jooq.Record9;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.util.postgres.PostgresDataType;
import org.postgresql.geometric.PGpoint;
import xjc.schema.ixsi.AttributeClassType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static jooq.db.ixsi.tables.Attribute.ATTRIBUTE;
import static jooq.db.ixsi.tables.BookingTarget.BOOKING_TARGET;
import static jooq.db.ixsi.tables.BookingTargetStatusInavailability.BOOKING_TARGET_STATUS_INAVAILABILITY;
import static jooq.db.ixsi.tables.BookingTargetStatusPlace.BOOKING_TARGET_STATUS_PLACE;
import static jooq.db.ixsi.tables.Place.PLACE;
import static jooq.db.ixsi.tables.PlaceAttribute.PLACE_ATTRIBUTE;
import static jooq.db.ixsi.tables.PlaceName.PLACE_NAME;
import static jooq.db.ixsi.tables.PlacegroupPlace.PLACEGROUP_PLACE;
import static jooq.db.ixsi.tables.Provider.PROVIDER;
import static org.jooq.impl.DSL.count;

/**
 * Created by Wolfgang Kluth on 03/02/15.
 */

@Slf4j
@Stateless
public class StopInfoRepositoryImpl implements StopInfoRepository {

    @Inject private DSLContext ctx;

    private static final String PROVIDER_FIELD = "provider";
    private static final String STOP_POINT_TYPE_FIELD = "stop_point_type";
    private static final String TOTAL_VEHICLES = "total_vehicles";
    private static final String TOTAL_SPACES = "total_spaces";
    private static final String AVAILABLE_VEHICLES = "available_vehicles";
    private static final String AVAILABLE_SPACES = "available_spaces";
    private static final String NAME = "name";
    private static final String GPS_POSITION = "gps_position";
    private static final String PLACE_ID = "place_id";
    private static final String COUNT = "count";

    @Override
    @SuppressWarnings("unchecked")
    public List<StopPoint> find(List<String> stationIds, List<String> providers, List<String> stopPointTypes,
                                Double latitude, Double longitude, Double radius,
                                TsRange timePeriod) {

        Select<Record2<String, Integer>> count1 = selectCountPlaceIndependent(timePeriod);
        Select<Record2<String, Integer>> count2 = selectCountPlaceDependent(timePeriod);
        Table<Record2<String, Integer>> placeAvail = count1.unionAll(count2).asTable("place_avail");

        SelectQuery ixsiSelect = selectIxsi(placeAvail);
        Table res = ixsiSelect.asTable("res");

        SelectQuery selectQuery = ctx.selectQuery();
        selectQuery.addFrom(res);

        // ------ Optional conditions ------ //

        // Provider filter
        if (BasicUtils.hasElements(providers)) {
            selectQuery.addConditions(res.field(PROVIDER_FIELD).in(providers));
        }

        // StopPointType filter
        if (BasicUtils.hasElements(stopPointTypes)) {
            selectQuery.addConditions(res.field(STOP_POINT_TYPE_FIELD).in(stopPointTypes));
        }

        // StopID filter
        if (BasicUtils.hasElements(stationIds)) {
            selectQuery.addConditions(res.field(PLACE_ID).in(stationIds));
        }

        // Circle filter
        if (radius != null && radius > 0) {
            selectQuery.addConditions(
                    DSL.condition(getDistanceWithinCondition(latitude, longitude, radius, res.field(GPS_POSITION)).asField())
            );
        }

        return selectQuery.fetch().map(new StopPointMapper());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Select<Record2<String, Integer>> selectCountPlaceIndependent(TsRange timePeriod) {
        return ctx.select(PLACE.PLACE_ID.as(PLACE_ID),
                          count(BOOKING_TARGET_STATUS_PLACE.PLACE_ID).as(COUNT))
                  .from(PLACE)
                  .join(PLACEGROUP_PLACE)
                        .on(PLACE.PLACE_ID.eq(PLACEGROUP_PLACE.PLACE_ID))
                  .leftOuterJoin(BOOKING_TARGET)
                        .on(PLACEGROUP_PLACE.PLACEGROUP_ID.eq(BOOKING_TARGET.EXCLUSIVE_TO_PLACEGROUP_ID))
                        .and(BOOKING_TARGET.STATUS.eq(Status.ACTIVE))
                  .leftOuterJoin(BOOKING_TARGET_STATUS_PLACE)
                        .on(BOOKING_TARGET.BOOKING_TARGET_ID.eq(BOOKING_TARGET_STATUS_PLACE.BOOKING_TARGET_ID))
                        .and(BOOKING_TARGET.PROVIDER_ID.eq(BOOKING_TARGET_STATUS_PLACE.PROVIDER_ID))
                        .and(PLACE.PLACE_ID.eq(BOOKING_TARGET_STATUS_PLACE.PLACE_ID))
                  .leftOuterJoin(BOOKING_TARGET_STATUS_INAVAILABILITY)
                        .on(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.eq(BOOKING_TARGET_STATUS_PLACE.BOOKING_TARGET_ID))
                        .and(timePeriodCondition(timePeriod))
                  .where(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.isNull())
                  .groupBy(PLACE.PLACE_ID);
    }

    private Select<Record2<String, Integer>> selectCountPlaceDependent(TsRange timePeriod) {
        return ctx.select(PLACE.PLACE_ID.as(PLACE_ID),
                          count(BOOKING_TARGET.EXCLUSIVE_TO_PLACE_ID).as(COUNT))
                  .from(PLACE)
                  .leftOuterJoin(BOOKING_TARGET)
                        .on(PLACE.PLACE_ID.eq(BOOKING_TARGET.EXCLUSIVE_TO_PLACE_ID))
                        .and(BOOKING_TARGET.STATUS.eq(Status.ACTIVE))
                  .leftOuterJoin(BOOKING_TARGET_STATUS_INAVAILABILITY)
                        .on(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.eq(BOOKING_TARGET.BOOKING_TARGET_ID))
                        .and(timePeriodCondition(timePeriod))
                  .where(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.isNull())
                        .and(BOOKING_TARGET.EXCLUSIVE_TO_PLACE_ID.isNotNull())
                  .groupBy(PLACE.PLACE_ID);
    }

    @SuppressWarnings("unchecked")
    private SelectQuery selectIxsi(Table countTable) {
        SelectQuery selectQuery = ctx.selectQuery();
        selectQuery.addFrom(PLACE);
        selectQuery.addJoin(PLACE_NAME, PLACE_NAME.PLACE_ID.eq(PLACE.PLACE_ID));
        selectQuery.addJoin(PLACE_ATTRIBUTE, PLACE_ATTRIBUTE.PLACE_ID.eq(PLACE.PLACE_ID));
        selectQuery.addJoin(ATTRIBUTE, ATTRIBUTE.ATTRIBUTE_ID.eq(PLACE_ATTRIBUTE.ATTRIBUTE_ID));
        selectQuery.addJoin(PROVIDER, PROVIDER.PROVIDER_ID.eq(PLACE.PROVIDER_ID));

        selectQuery.addJoin(countTable, JoinType.LEFT_OUTER_JOIN, PLACE.PLACE_ID.eq(countTable.field(PLACE_ID)));

        selectQuery.addSelect(
                ATTRIBUTE.CLASS.as(STOP_POINT_TYPE_FIELD),
                PROVIDER.NAME.as(PROVIDER_FIELD),
                PLACE.PLACE_ID.as(PLACE_ID),
                PLACE_NAME.VALUE.as(NAME),
                PLACE.GPS_POSITION.as(GPS_POSITION),
                PLACE.CAPACITY.as(TOTAL_SPACES),
                PLACE.CAPACITY.sub(PLACE.AVAILABLE_CAPACITY).cast(PostgresDataType.INT).as(TOTAL_VEHICLES), // not precise, because reservations are not excluded
                PLACE.AVAILABLE_CAPACITY.as(AVAILABLE_SPACES),
                countTable.field(COUNT).as(AVAILABLE_VEHICLES)
        );

        selectQuery.addConditions(
                PLACE_NAME.LANGUAGE.eq(Constants.Ixsi.DEFAULT_LANGUAGE),
                ATTRIBUTE.CLASS.in(AttributeClassType.BIKE_SHARING.value(), AttributeClassType.CAR_SHARING.value()),
                PLACE.STATUS.eq(Status.ACTIVE)
        );

        selectQuery.addOrderBy(PROVIDER.NAME, PLACE_NAME.VALUE);

        return selectQuery;
    }

    private StDwithin3 getDistanceWithinCondition(Double latitude, Double longitude, Double radius,
                                                  Field<PGpoint> stationLocation) {
        Geometry2 stationGeo = new Geometry2();
        stationGeo.set__1(stationLocation);

        PGpoint searchPosition = new PGpoint(longitude, latitude);

        Geometry2 searchGeo = new Geometry2();
        searchGeo.set__1(searchPosition);

        Geography3 searchGeography = new Geography3();
        searchGeography.set__1(searchGeo.asField());

        Geography3 stationGeography = new Geography3();
        stationGeography.set__1(stationGeo.asField());

        StDwithin3 searchWithin = new StDwithin3();
        searchWithin.set__1(stationGeography.asField());
        searchWithin.set__2(searchGeography.asField());
        searchWithin.set__3(radius);
        return searchWithin;
    }

    private static class StopPointMapper
            implements RecordMapper<Record9<String, String, String, String,
            PGpoint, Integer, Integer, Integer, Integer>, StopPoint> {

        @Override
        public StopPoint map(Record9<String, String, String, String,
                PGpoint, Integer, Integer, Integer, Integer> record) {
            // Extra careful with the order!
            return new StopPoint.Builder()
                    .withStopPointType(record.value1())
                    .withProvider(record.value2())
                    .withStopPointId(String.valueOf(record.value3()))
                    .withStopPointName(record.value4())
                    .withLatitude(record.value5().y)
                    .withLongitude(record.value5().x)
                    .withTotalSpaces(record.value6())
                    .withTotalVehicles(record.value7())
                    .withAvailSpaces(record.value8())
                    .withAvailVehicles(record.value9())
                    .build();
        }
    }

    private Condition timePeriodCondition(TsRange timePeriod) {
        if (timePeriod == null) {
            return CustomDSL.elementContainedBy(DSL.currentTimestamp(), BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY);
        } else {
            return CustomDSL.overlaps(BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY, timePeriod);
        }
    }

}
