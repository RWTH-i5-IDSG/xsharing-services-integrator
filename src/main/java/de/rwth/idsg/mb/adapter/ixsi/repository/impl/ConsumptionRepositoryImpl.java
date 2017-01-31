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

import de.rwth.idsg.mb.adapter.ixsi.repository.ConsumptionRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ConsumptionDTO;
import de.rwth.idsg.mb.regioIT.client.rest.params.ConsumptionParams;
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.enums.BookingChangeType;
import jooq.db.ixsi.enums.Language;
import jooq.db.ixsi.tables.records.ConsumptionRecord;
import jooq.db.ixsi.tables.records.PushEventConsumptionRecord;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.jooq.BatchBindStep;
import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record7;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import xjc.schema.ixsi.ConsumptionType;
import xjc.schema.ixsi.TextType;
import xjc.schema.ixsi.TimePeriodType;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getPriceOrNull;
import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getValueOrNull;
import static de.rwth.idsg.mb.utils.BasicUtils.getTypeSafeBoolean;
import static de.rwth.idsg.mb.utils.BasicUtils.toBigDecimal;
import static de.rwth.idsg.mb.utils.BasicUtils.toSeconds;
import static jooq.db.ixsi.tables.BookingChange.BOOKING_CHANGE;
import static jooq.db.ixsi.tables.BookingCreate.BOOKING_CREATE;
import static jooq.db.ixsi.tables.Consumption.CONSUMPTION;
import static jooq.db.ixsi.tables.ConsumptionDescription.CONSUMPTION_DESCRIPTION;
import static jooq.db.ixsi.tables.Provider.PROVIDER;
import static jooq.db.ixsi.tables.PushEventConsumption.PUSH_EVENT_CONSUMPTION;


/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.01.2015
 */
@Slf4j
@Stateless
public class ConsumptionRepositoryImpl implements ConsumptionRepository {

    @Inject private DSLContext ctx;

    private static final Object LOCK = new Object();

    @Override
    public List<String> getBookingIdsWithNonFinalConsumption(int partnerId) {

        Field<String> changeBookingId = BOOKING_CHANGE.BOOKING_ID.as("booking_id");
        Field<String> consumptionBookingId = CONSUMPTION.BOOKING_ID.as("booking_id");
        Field<String> createBookingId = BOOKING_CREATE.BOOKING_ID.as("booking_id");

        // cancelled
        CommonTableExpression<Record1<String>> cancelled =
                DSL.name("cancelled")
                   .as(DSL.selectDistinct(changeBookingId)
                          .from(BOOKING_CHANGE)
                          .where(BOOKING_CHANGE.CHANGE_TYPE.eq(BookingChangeType.CANCEL))
                   );

        SelectJoinStep<Record1<String>> cancelledBookingIds =
                DSL.select(cancelled.field(changeBookingId))
                   .from(cancelled);

        // active = all - cancelled
        CommonTableExpression<Record1<String>> active =
                DSL.name("active")
                   .as(DSL.select(createBookingId)
                          .from(BOOKING_CREATE)
                          .join(PROVIDER)
                          .on(PROVIDER.PROVIDER_ID.eq(BOOKING_CREATE.PROVIDER_ID))
                          .where(BOOKING_CREATE.BOOKING_ID.notIn(cancelledBookingIds))
                          .and(PROVIDER.PARTNER_ID.eq(partnerId))
                   );

        // finalized = bookings with complete consumption
        CommonTableExpression<Record1<String>> finalized =
                DSL.name("finalized")
                   .as(DSL.selectDistinct(consumptionBookingId)
                          .from(CONSUMPTION)
                          .where(CONSUMPTION.PARTNER_ID.eq(partnerId))
                          .and(CONSUMPTION.FINAL.isTrue())
                   );

        SelectJoinStep<Record1<String>> finalizedBookingIds =
                DSL.select(finalized.field(consumptionBookingId))
                   .from(finalized);

        Field<String> activeBookingId = active.field(createBookingId);

        // active - finalized
        return ctx.with(cancelled)
                  .with(active)
                  .with(finalized)
                  .select(activeBookingId)
                  .from(active)
                  .where(activeBookingId.notIn(finalizedBookingIds))
                  .fetch(activeBookingId);
    }

    @Override
    @Transactional
    public List<ConsumptionDTO> insertConsumptionList(int partnerId, final List<ConsumptionType> consumptionList) {
        List<ConsumptionDTO> list = new ArrayList<>(consumptionList.size());
        for (ConsumptionType c : consumptionList) {
            Integer consumptionId = insertConsumption(partnerId, c);
            if (consumptionId == null) {
                continue;
            }
            if (c.isSetDescription()) {
                insertConsumptionDesc(consumptionId, c.getDescription());
            }
            list.add(new ConsumptionDTO(consumptionId, c));
        }
        return list;
    }

    @Override
    public void insertPushEvent(int partnerId, List<ConsumptionDTO> consumptionList, boolean success) {
        List<PushEventConsumptionRecord> tmp = new ArrayList<>(consumptionList.size());
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());

        for (ConsumptionDTO c : consumptionList) {
            tmp.add(ctx.newRecord(PUSH_EVENT_CONSUMPTION)
                       .setConsumptionId(c.getConsumptionId())
                       .setEventTimestamp(timestamp)
                       .setSuccess(success));
        }

        ctx.batchInsert(tmp)
           .execute();
    }

    @Override
    public List<ConsumptionRecord> getRecordsToPush() {
        Field<Integer> consumptionId = PUSH_EVENT_CONSUMPTION.CONSUMPTION_ID.as("id");
        Field<Timestamp> max = DSL.max(PUSH_EVENT_CONSUMPTION.EVENT_TIMESTAMP).as("latest_ts");

        CommonTableExpression<Record2<Integer, Timestamp>> latestCTE =
                DSL.name("latest")
                   .as(DSL.select(consumptionId, max)
                          .from(PUSH_EVENT_CONSUMPTION)
                          .where(PUSH_EVENT_CONSUMPTION.SUCCESS.isTrue())
                          .groupBy(PUSH_EVENT_CONSUMPTION.CONSUMPTION_ID)
                   );

        return ctx.with(latestCTE)
                  .selectFrom(CONSUMPTION)
                  .where(CONSUMPTION.CONSUMPTION_ID.notIn(DSL.select(latestCTE.field(consumptionId))
                                                             .from(latestCTE)))
                  .fetch();
    }

    @Override
    public List<ConsumptionParams> getRecords(String providerName, String bookingId) {
        return ctx.select(PROVIDER.NAME,
                          CONSUMPTION.BOOKING_ID,
                          CONSUMPTION.TIME_PERIOD_FROM,
                          CONSUMPTION.TIME_PERIOD_TO,
                          CONSUMPTION.VALUE,
                          CONSUMPTION.UNIT,
                          CONSUMPTION.FINAL)
                  .from(CONSUMPTION)
                  .join(PROVIDER)
                  .on(CONSUMPTION.PARTNER_ID.eq(PROVIDER.PARTNER_ID))
                  .where(PROVIDER.NAME.eq(providerName))
                  .and(CONSUMPTION.BOOKING_ID.eq(bookingId))
                  .fetch()
                  .map(toParams());
    }

    @Override
    public List<String> getFinalized(int partnerId) {
        return ctx.selectDistinct(CONSUMPTION.BOOKING_ID)
                  .from(CONSUMPTION)
                  .where(CONSUMPTION.PARTNER_ID.eq(partnerId))
                  .and(CONSUMPTION.FINAL.isTrue())
                  .fetch(CONSUMPTION.BOOKING_ID);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private RecordMapper<Record7<String, String, Timestamp, Timestamp, Float, String, Boolean>, ConsumptionParams> toParams() {
        return r -> ConsumptionParams.builder()
                                     .providerName(r.value1())
                                     .bookingNo(r.value2())
                                     .startDateTime(toSeconds(r.value3()))
                                     .endDateTime(toSeconds(r.value4()))
                                     .value(toBigDecimal(r.value5()))
                                     .unit(r.value6())
                                     .finalized(getTypeSafeBoolean(r.value7()))
                                     .build();
    }

    @Nullable
    private Integer insertConsumption(int partnerId, ConsumptionType c) {

        Integer price = getPriceOrNull(c);
        Float value = getValueOrNull(c);

        Timestamp from = null;
        Timestamp to = null;

        TimePeriodType t = c.getTimePeriod();
        if (t != null) {
            from = new Timestamp(t.getBegin().getMillis());
            to = new Timestamp(t.getEnd().getMillis());
        }

        // TODO: Dirty, dirty workaround with synchronized and LOCK.
        synchronized (LOCK) {
            // do we have "finalized" consumptions for this booking?
            Record1<Integer> im = ctx.selectOne()
                                     .from(CONSUMPTION)
                                     .where(CONSUMPTION.PARTNER_ID.eq(partnerId))
                                     .and(CONSUMPTION.BOOKING_ID.eq(c.getBookingID()))
                                     .and(CONSUMPTION.FINAL.isTrue())
                                     .fetchOne();

            if (im != null) {
                // We already have a complete consumption data set for this booking
                // Do not insert data for the second time.
                return null;
            }

            return ctx.insertInto(CONSUMPTION,
                                  CONSUMPTION.BOOKING_ID,
                                  CONSUMPTION.PARTNER_ID,
                                  CONSUMPTION.CLASS,
                                  CONSUMPTION.VALUE,
                                  CONSUMPTION.TIME_PERIOD_FROM,
                                  CONSUMPTION.TIME_PERIOD_TO,
                                  CONSUMPTION.UNIT,
                                  CONSUMPTION.PRICE_IN_EURO_CENTS,
                                  CONSUMPTION.FINAL)
                      .values(c.getBookingID(), partnerId, c.getType().value(),
                              value, from, to, c.getUnit(), price, c.isFinal())
                      .returning(CONSUMPTION.CONSUMPTION_ID)
                      .fetchOne()
                      .getConsumptionId();
        }
    }

    private void insertConsumptionDesc(int consumptionId, List<TextType> descriptionList) {
        BatchBindStep insertBatch = ctx.batch(
                ctx.insertInto(CONSUMPTION_DESCRIPTION,
                        CONSUMPTION_DESCRIPTION.CONSUMPTION_ID,
                        CONSUMPTION_DESCRIPTION.LANGUAGE,
                        CONSUMPTION_DESCRIPTION.VALUE)
                   .values(0, null, "")
        );

        for (TextType tt : descriptionList) {
            Language lng = BasicUtils.getDatabaseLangOrDefault(tt.getLanguage());
            insertBatch.bind(consumptionId, lng, tt.getText());
        }

        insertBatch.execute();
    }

}
