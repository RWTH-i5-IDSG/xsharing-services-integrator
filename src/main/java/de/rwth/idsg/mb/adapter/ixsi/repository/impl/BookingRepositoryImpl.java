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
package de.rwth.idsg.mb.adapter.ixsi.repository.impl;

import com.google.common.base.Strings;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.BookingInfoDTO;
import de.rwth.idsg.mb.monitor.dto.BookingInfoParams;
import de.rwth.idsg.mb.pg.TsRange;
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.enums.BookingChangeType;
import jooq.db.ixsi.enums.EventOrigin;
import jooq.db.ixsi.tables.records.BookingChangeRecord;
import jooq.db.ixsi.tables.records.BookingCreateRecord;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.ExternalBookingType;
import xjc.schema.ixsi.OriginDestType;
import xjc.schema.ixsi.TimePeriodType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.mb.pg.CustomDSL.elementContainedBy;
import static de.rwth.idsg.mb.pg.CustomDSL.includes;
import static de.rwth.idsg.mb.pg.CustomDSL.overlaps;
import static jooq.db.ixsi.tables.BookingChange.BOOKING_CHANGE;
import static jooq.db.ixsi.tables.BookingCreate.BOOKING_CREATE;
import static jooq.db.ixsi.tables.Provider.PROVIDER;
import static jooq.db.ixsi.tables.PushEventExternalBooking.PUSH_EVENT_EXTERNAL_BOOKING;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.07.2015
 */
@Slf4j
@Stateless
public class BookingRepositoryImpl implements BookingRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<String> getActiveIds(int partnerId) {

//        // Get last events for bookings
//        Field<String> id = BOOKING_CHANGE.BOOKING_ID.as("id");
//        Field<Timestamp> changed = DSL.max(BOOKING_CHANGE.EVENT_TIMESTAMP).as("changed");
//        TableLike<Record2<String, Timestamp>> lastTable = DSL.select(id, changed)
//                                                             .from(BOOKING_CHANGE)
//                                                             .groupBy(BOOKING_CHANGE.BOOKING_ID)
//                                                             .asTable("last");
//
//        // Get cancelled bookings
//        Field<String> cancelledId = BOOKING_CHANGE.BOOKING_ID.as("cid");
//        TableLike<Record1<String>> cancelled = DSL.select(cancelledId)
//                                                  .from(BOOKING_CHANGE)
//                                                  .join(lastTable)
//                                                  .on(BOOKING_CHANGE.BOOKING_ID.eq(lastTable.field(id)))
//                                                  .and(BOOKING_CHANGE.EVENT_TIMESTAMP.eq(lastTable.field(changed)))
//                                                  .where(BOOKING_CHANGE.CHANGE_TYPE.eq(BookingChangeType.CANCEL))
//                                                  .asTable("cancelled");
//
//        // Return all not cancelled bookings
//        return DSL.using(config)
//                  .select(BOOKING_CREATE.BOOKING_ID)
//                  .from(BOOKING_CREATE)
//                  .join(PROVIDER)
//                    .on(PROVIDER.PROVIDER_ID.eq(BOOKING_CREATE.PROVIDER_ID))
//                  .leftOuterJoin(cancelled)
//                    .on(BOOKING_CREATE.BOOKING_ID.eq(cancelled.field(cancelledId)))
//                    .and(cancelled.field(cancelledId).isNull())
//                  .where(PROVIDER.PARTNER_ID.eq(partnerId))
//                  .fetch(BOOKING_CREATE.BOOKING_ID);

        // Return all not cancelled bookings
        return ctx.select(BOOKING_CREATE.BOOKING_ID)
                  .from(BOOKING_CREATE)
                  .join(PROVIDER)
                  .on(PROVIDER.PROVIDER_ID.eq(BOOKING_CREATE.PROVIDER_ID))
                  .where(BOOKING_CREATE.BOOKING_ID.notIn(getCancelled()))
                  .and(PROVIDER.PARTNER_ID.eq(partnerId))
                  .fetch(BOOKING_CREATE.BOOKING_ID);
    }

    @Override
    public void create(EventOrigin origin, DateTime eventTimestamp,
                       String bookingId, BookingTargetIDType bookingTargetId, String userId,
                       TimePeriodType timePeriod, OriginDestType from, OriginDestType to) {

        String fromPlaceId = null;
        if (from != null) {
            fromPlaceId = from.getPlaceID();
        }

        String toPlaceId = null;
        if (to != null) {
            toPlaceId = to.getPlaceID();
        }

        ctx.insertInto(BOOKING_CREATE,
                   BOOKING_CREATE.EVENT_ORIGIN,
                   BOOKING_CREATE.EVENT_TIMESTAMP,
                   BOOKING_CREATE.PROVIDER_ID,
                   BOOKING_CREATE.BOOKING_TARGET_ID,
                   BOOKING_CREATE.USER_ID,
                   BOOKING_CREATE.BOOKING_ID,
                   BOOKING_CREATE.TIME_PERIOD,
                   BOOKING_CREATE.FROM_PLACE_ID,
                   BOOKING_CREATE.TO_PLACE_ID)
           .values(origin,
                   new Timestamp(eventTimestamp.getMillis()),
                   bookingTargetId.getProviderID(),
                   bookingTargetId.getBookeeID(),
                   userId,
                   bookingId,
                   BasicUtils.toTsRange(timePeriod),
                   fromPlaceId,
                   toPlaceId)
           .execute();
    }

    @Override
    public void update(EventOrigin origin, DateTime eventTimestamp, String bookingId, TimePeriodType newTimePeriod) {
        ctx.insertInto(BOOKING_CHANGE,
                   BOOKING_CHANGE.EVENT_TIMESTAMP,
                   BOOKING_CHANGE.EVENT_ORIGIN,
                   BOOKING_CHANGE.CHANGE_TYPE,
                   BOOKING_CHANGE.BOOKING_ID,
                   BOOKING_CHANGE.NEW_TIME_PERIOD)
           .values(new Timestamp(eventTimestamp.getMillis()),
                   origin,
                   BookingChangeType.UPDATE,
                   bookingId,
                   BasicUtils.toTsRange(newTimePeriod))
           .execute();
    }

    @Override
    public void cancel(EventOrigin origin, DateTime eventTimestamp, String bookingId) {
        ctx.insertInto(BOOKING_CHANGE,
                   BOOKING_CHANGE.EVENT_TIMESTAMP,
                   BOOKING_CHANGE.EVENT_ORIGIN,
                   BOOKING_CHANGE.CHANGE_TYPE,
                   BOOKING_CHANGE.BOOKING_ID)
           .values(new Timestamp(eventTimestamp.getMillis()),
                   origin,
                   BookingChangeType.CANCEL,
                   bookingId)
           .execute();
    }

    @Override
    public void insertPushEvent(int partnerId, ExternalBookingType externalBooking, boolean success) {
        ctx.insertInto(PUSH_EVENT_EXTERNAL_BOOKING,
                   PUSH_EVENT_EXTERNAL_BOOKING.PARTNER_ID,
                   PUSH_EVENT_EXTERNAL_BOOKING.BOOKING_ID,
                   PUSH_EVENT_EXTERNAL_BOOKING.EVENT_TIMESTAMP,
                   PUSH_EVENT_EXTERNAL_BOOKING.SUCCESS)
           .values(partnerId,
                   externalBooking.getBookingID(),
                   new Timestamp(DateTime.now().getMillis()),
                   success)
           .execute();
    }

    @Override
    public List<BookingInfoDTO> getInfo(BookingInfoParams param) {

        Field<String> bookingIdField = DSL.field("booking_id", String.class);
        Field<Timestamp> maxTsField = DSL.field("max_ts", Timestamp.class);
        String timePeriodField = "time_period";

        // -------------------------------------------------------------------------
        // CTEs
        // -------------------------------------------------------------------------

        CommonTableExpression<Record2<String, Timestamp>> updateEntryWithMaxTimestamp =
                DSL.name("update_entry_with_max_timestamp")
                   .as(DSL.select(
                               BOOKING_CHANGE.BOOKING_ID.as(bookingIdField),
                               DSL.max(BOOKING_CHANGE.EVENT_TIMESTAMP).as(maxTsField))
                          .from(BOOKING_CHANGE)
                          .where(BOOKING_CHANGE.CHANGE_TYPE.eq(BookingChangeType.UPDATE))
                          .groupBy(BOOKING_CHANGE.BOOKING_ID));

        CommonTableExpression<Record2<String, TsRange>> lastUpdate =
                DSL.name("last_update")
                   .as(DSL.select(
                               BOOKING_CHANGE.BOOKING_ID.as(bookingIdField),
                               BOOKING_CHANGE.NEW_TIME_PERIOD.as(timePeriodField))
                          .from(BOOKING_CHANGE)
                          .join(updateEntryWithMaxTimestamp)
                          .on(BOOKING_CHANGE.BOOKING_ID.eq(updateEntryWithMaxTimestamp.field(bookingIdField)))
                          .and(BOOKING_CHANGE.EVENT_TIMESTAMP.eq(updateEntryWithMaxTimestamp.field(maxTsField))));

        CommonTableExpression<Record4<String, String, String, TsRange>> createdAndUpdated =
                DSL.name("created_and_updated")
                   .as(DSL.select(
                               BOOKING_CREATE.BOOKING_ID,
                               BOOKING_CREATE.USER_ID,
                               BOOKING_CREATE.BOOKING_TARGET_ID,
                               lastUpdate.field(timePeriodField).coerce(TsRange.class))
                          .from(BOOKING_CREATE)
                          .join(lastUpdate)
                          .on(BOOKING_CREATE.BOOKING_ID.eq(lastUpdate.field(bookingIdField))));

        CommonTableExpression<Record4<String, String, String, TsRange>> createdNotChanged =
                DSL.name("created_not_changed")
                   .as(DSL.select(
                               BOOKING_CREATE.BOOKING_ID,
                               BOOKING_CREATE.USER_ID,
                               BOOKING_CREATE.BOOKING_TARGET_ID,
                               BOOKING_CREATE.TIME_PERIOD)
                          .from(BOOKING_CREATE)
                          .leftOuterJoin(BOOKING_CHANGE)
                          .on(BOOKING_CREATE.BOOKING_ID.eq(BOOKING_CHANGE.BOOKING_ID))
                          .where(BOOKING_CHANGE.BOOKING_ID.isNull()));

        CommonTableExpression<Record4<String, String, String, TsRange>> bookings =
                DSL.name("bookings")
                   .as(DSL.selectFrom(createdNotChanged)
                          .unionAll(DSL.selectFrom(createdAndUpdated)));

        // -------------------------------------------------------------------------
        // Conditions
        // -------------------------------------------------------------------------

        List<Condition> conditions = new ArrayList<>(4);

        if (!Strings.isNullOrEmpty(param.getBookingId())) {
            conditions.add(includes(bookings.field(0).coerce(String.class), param.getBookingId()));
        }

        if (!Strings.isNullOrEmpty(param.getUserId())) {
            conditions.add(includes(bookings.field(1).coerce(String.class), param.getUserId()));
        }

        if (!Strings.isNullOrEmpty(param.getVehicleId())) {
            conditions.add(includes(bookings.field(2).coerce(String.class), param.getVehicleId()));
        }

        if (param.getTimePeriod() == null) {
            Timestamp now = new Timestamp(DateTime.now().getMillis());
            conditions.add(elementContainedBy(now, bookings.field(3).coerce(TsRange.class)));
        } else {
            conditions.add(overlaps(bookings.field(3).coerce(TsRange.class), param.getTimePeriod()));
        }

        // -------------------------------------------------------------------------
        // Query
        // -------------------------------------------------------------------------

        return ctx.with(updateEntryWithMaxTimestamp)
                  .with(lastUpdate)
                  .with(createdAndUpdated)
                  .with(createdNotChanged)
                  .with(bookings)
                  .selectFrom(bookings)
                  .where(conditions)
                  .orderBy(bookings.field(2))
                  .fetch()
                  .map(r -> {
                      BookingInfoDTO dto = new BookingInfoDTO();
                      dto.setBookingId(r.value1());
                      dto.setUserId(r.value2());
                      dto.setVehicleId(r.value3());
                      dto.setTimePeriod(r.value4());
                      return dto;
                  });
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private SelectConditionStep<Record1<String>> getCancelled() {
        return DSL.selectDistinct(BOOKING_CHANGE.BOOKING_ID)
                  .from(BOOKING_CHANGE)
                  .where(BOOKING_CHANGE.CHANGE_TYPE.eq(BookingChangeType.CANCEL));
    }

    @Override
    public BookingCreateRecord getCreateRecord(String bookingId) {
        return ctx.selectFrom(BOOKING_CREATE)
                  .where(BOOKING_CREATE.BOOKING_ID.eq(bookingId))
                  .fetchOne();
    }

    @Override
    public List<BookingChangeRecord> getChangeRecords(String providerId, String bookingId) {
        return ctx.select(
                        BOOKING_CHANGE.EVENT_TIMESTAMP,
                        BOOKING_CHANGE.CHANGE_TYPE,
                        BOOKING_CHANGE.BOOKING_ID,
                        BOOKING_CHANGE.NEW_TIME_PERIOD,
                        BOOKING_CHANGE.EVENT_ORIGIN)
                  .from(BOOKING_CHANGE)
                  .join(BOOKING_CREATE)
                  .on(BOOKING_CHANGE.BOOKING_ID.eq(BOOKING_CREATE.BOOKING_ID))
                  .where(BOOKING_CHANGE.BOOKING_ID.eq(bookingId))
                  .and(BOOKING_CREATE.PROVIDER_ID.eq(providerId))
                  .fetch()
                  .map(i -> new BookingChangeRecord(i.value1(), i.value2(), i.value3(), i.value4(), i.value5()));
    }

}
