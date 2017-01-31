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

import de.rwth.idsg.mb.Constants;
import de.rwth.idsg.mb.controller.dto.VehicleDTO;
import de.rwth.idsg.mb.pg.CustomDSL;
import de.rwth.idsg.mb.pg.TsRange;
import jooq.db.ixsi.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static jooq.db.ixsi.tables.BookingTarget.BOOKING_TARGET;
import static jooq.db.ixsi.tables.BookingTargetName.BOOKING_TARGET_NAME;
import static jooq.db.ixsi.tables.BookingTargetStatusInavailability.BOOKING_TARGET_STATUS_INAVAILABILITY;
import static jooq.db.ixsi.tables.BookingTargetStatusPlace.BOOKING_TARGET_STATUS_PLACE;

/**
 * Created by Wolfgang Kluth on 24/02/15.
 */
@Slf4j
@Stateless
public class VehicleRepositoryImpl implements VehicleRepository {

    @Inject private DSLContext ctx;
    @EJB private StopInfoRepository stopInfoRepository;

    @Override
    @SuppressWarnings("unchecked")
    public List<VehicleDTO> find(String providerId, String placeId, TsRange timePeriod) {
        SelectQuery s1 = selectIxsiPlaceDependent(providerId, placeId, timePeriod);
        SelectQuery s2 = selectIxsiPlaceInDependent(providerId, placeId, timePeriod);

        Table selectUnion = s1.unionAll(s2).asTable("vehicles");
        SelectQuery selectQuery = ctx.selectQuery();
        selectQuery.addFrom(selectUnion);
        return selectQuery.fetch().map(new VehicleDTOMapper());
    }

    private SelectQuery selectIxsiPlaceDependent(String providerId, String placeId, TsRange timePeriod) {
        return selectIxsi(providerId, placeId, timePeriod, BOOKING_TARGET.EXCLUSIVE_TO_PLACE_ID);
    }

    @SuppressWarnings("unchecked")
    private SelectQuery selectIxsiPlaceInDependent(String providerId, String placeId, TsRange timePeriod) {
        SelectQuery sq = selectIxsi(providerId, placeId, timePeriod, BOOKING_TARGET_STATUS_PLACE.PLACE_ID);

        sq.addJoin(
                BOOKING_TARGET_STATUS_PLACE,
                BOOKING_TARGET_STATUS_PLACE.BOOKING_TARGET_ID.eq(BOOKING_TARGET.BOOKING_TARGET_ID),
                BOOKING_TARGET_STATUS_PLACE.PROVIDER_ID.eq(BOOKING_TARGET.PROVIDER_ID)
        );

        return sq;
    }

    @SuppressWarnings("unchecked")
    private SelectQuery selectIxsi(String providerId, String placeId, TsRange timePeriod, TableField placeField) {
        return ctx.select(BOOKING_TARGET.BOOKING_TARGET_ID,
                          BOOKING_TARGET_NAME.VALUE,
                          BOOKING_TARGET.CLASS)
                  .from(BOOKING_TARGET)
                  .join(BOOKING_TARGET_NAME)
                  .on(BOOKING_TARGET_NAME.BOOKING_TARGET_ID.eq(BOOKING_TARGET.BOOKING_TARGET_ID))
                  .and(BOOKING_TARGET_NAME.PROVIDER_ID.eq(BOOKING_TARGET.PROVIDER_ID))
                  .and(BOOKING_TARGET_NAME.LANGUAGE.eq(Constants.Ixsi.DEFAULT_LANGUAGE))
                  .leftOuterJoin(BOOKING_TARGET_STATUS_INAVAILABILITY)
                  .on(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.eq(BOOKING_TARGET.BOOKING_TARGET_ID))
                  .and(BOOKING_TARGET_STATUS_INAVAILABILITY.PROVIDER_ID.eq(BOOKING_TARGET.PROVIDER_ID))
                  .and(timePeriodCondition(timePeriod))
                  .where(placeField.eq(placeId))
                  .and(BOOKING_TARGET.PROVIDER_ID.eq(providerId))
                  .and(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.isNull())
                  .and(BOOKING_TARGET.STATUS.eq(Status.ACTIVE))
                  .getQuery();
    }

    private static class VehicleDTOMapper implements RecordMapper<Record3<String, String, String>, VehicleDTO> {
        @Override
        public VehicleDTO map(Record3<String, String, String> record) {
            // Extra careful with the order!
            return VehicleDTO.builder()
                             .vehicleId(record.value1())
                             .vehicleName(record.value2())
                             .vehicleType(record.value3())
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
