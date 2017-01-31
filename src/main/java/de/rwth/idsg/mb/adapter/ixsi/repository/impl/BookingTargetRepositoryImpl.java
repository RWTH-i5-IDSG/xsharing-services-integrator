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

import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.BookingTargetAttributeWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.BookingTargetNameWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.BookingTargetWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.subscription.complete.BookingTargetStatusAddressWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.subscription.complete.BookingTargetStatusInavailabilityWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.subscription.complete.BookingTargetStatusPlaceWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.subscription.complete.BookingTargetStatusWorker;
import de.rwth.idsg.mb.pg.CustomDSL;
import de.rwth.idsg.mb.pg.TsRange;
import de.rwth.idsg.mb.pg.range.EmptyTsRange;
import de.rwth.idsg.mb.utils.BasicUtils;
import de.rwth.idsg.mb.utils.ItemIdComparator;
import jooq.db.ixsi.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.postgresql.geometric.PGpoint;
import xjc.schema.ixsi.AddressType;
import xjc.schema.ixsi.AttributeClassType;
import xjc.schema.ixsi.BookingTargetAvailabilityType;
import xjc.schema.ixsi.BookingTargetChangeAvailabilityType;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.BookingTargetType;
import xjc.schema.ixsi.ClassType;
import xjc.schema.ixsi.TextType;
import xjc.schema.ixsi.TimePeriodType;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.isSetAddress;
import static de.rwth.idsg.mb.utils.IxsiConverterUtils.isSetCoord;
import static jooq.db.ixsi.tables.BookingTarget.BOOKING_TARGET;
import static jooq.db.ixsi.tables.BookingTargetStatus.BOOKING_TARGET_STATUS;
import static jooq.db.ixsi.tables.BookingTargetStatusAddress.BOOKING_TARGET_STATUS_ADDRESS;
import static jooq.db.ixsi.tables.BookingTargetStatusInavailability.BOOKING_TARGET_STATUS_INAVAILABILITY;
import static jooq.db.ixsi.tables.BookingTargetStatusPlace.BOOKING_TARGET_STATUS_PLACE;
import static jooq.db.ixsi.tables.Provider.PROVIDER;

/**
 * Created by max on 10/12/14.
 */
@Slf4j
@Stateless
public class BookingTargetRepositoryImpl implements BookingTargetRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<BookingTargetIDType> getIds(int partnerId) {
        return internalGet(partnerId, false);
    }

    @Override
    public List<BookingTargetIDType> getActiveIds(int partnerId) {
        return internalGet(partnerId, true);
    }

    @SuppressWarnings("unchecked")
    private List<BookingTargetIDType> internalGet(int partnerId, boolean isActive) {
        SelectQuery sq = ctx.selectQuery();
        sq.addFrom(BOOKING_TARGET);
        sq.addSelect(
                BOOKING_TARGET.BOOKING_TARGET_ID,
                BOOKING_TARGET.PROVIDER_ID
        );

        sq.addJoin(
                PROVIDER,
                PROVIDER.PROVIDER_ID.eq(BOOKING_TARGET.PROVIDER_ID),
                PROVIDER.PARTNER_ID.eq(partnerId)
        );

        if (isActive) {
            sq.addConditions(BOOKING_TARGET.STATUS.eq(Status.ACTIVE));
        }

        return sq.fetch()
                 .map(new RecordMapper<Record2<String, String>, BookingTargetIDType>() {
                     @Override
                     public BookingTargetIDType map(Record2<String, String> record) {
                         return new BookingTargetIDType()
                                 .withBookeeID(record.value1())
                                 .withProviderID(record.value2());
                     }
                 });
    }

    @Override
    @Transactional
    public void upsertBookingTargetList(int partnerId, final List<BookingTargetType> bookingTargetList) {
        log.debug("Size of the arrived booking target list: {}", bookingTargetList.size());

        List<BookingTargetIDType> dbList = getIds(partnerId);

        // -------------------------------------------------------------------------
        // Prepare environment
        // -------------------------------------------------------------------------

        BookingTargetWorker bookingTargetWorker = new BookingTargetWorker(ctx, dbList);
        bookingTargetWorker.prepare();

        BookingTargetAttributeWorker bookingTargetAttributeWorker = new BookingTargetAttributeWorker(ctx);
        bookingTargetAttributeWorker.prepare();

        BookingTargetNameWorker bookingTargetNameWorker = new BookingTargetNameWorker(ctx);
        bookingTargetNameWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (BookingTargetType bookingTarget : bookingTargetList) {
            bookingTargetWorker.bind(bookingTarget);
            BookingTargetIDType bookingTargetId = bookingTarget.getID();

            if (bookingTarget.isSetAttributeID()) {
                bookingTargetAttributeWorker.bindDelete(bookingTargetId);
                for (String attrId : bookingTarget.getAttributeID()) {
                    bookingTargetAttributeWorker.bindInsert(bookingTargetId, attrId);
                }
            }

            if (bookingTarget.isSetName()) {
                bookingTargetNameWorker.bindDelete(bookingTargetId);
                for (TextType name : bookingTarget.getName()) {
                    bookingTargetNameWorker.bindInsert(bookingTargetId, name);
                }
            }
        }

        // -------------------------------------------------------------------------
        // Execute
        // -------------------------------------------------------------------------

        bookingTargetWorker.execute();

        bookingTargetNameWorker.execute();
        bookingTargetAttributeWorker.execute();


        // -------------------------------------------------------------------------
        // Set others to inactive
        // -------------------------------------------------------------------------

        List<BookingTargetIDType> newList = new ArrayList<>(bookingTargetList.size());
        for (BookingTargetType at : bookingTargetList) {
            newList.add(at.getID());
        }

        ItemIdComparator<BookingTargetIDType> comparator = new ItemIdComparator<>();
        comparator.setDatabaseList(dbList);
        comparator.setNewList(newList);

        updateStatus(partnerId, comparator);
    }

    @Override
    @Transactional
    public void insertBookingTargetAvailabilityList(final List<BookingTargetAvailabilityType> bookingTargetList) {
        log.debug("Size of the arrived booking target availability list: {}", bookingTargetList.size());

        // -------------------------------------------------------------------------
        // Prepare statements
        // -------------------------------------------------------------------------

        BookingTargetStatusWorker statusWorker = new BookingTargetStatusWorker(ctx);
        statusWorker.prepare();

        BookingTargetStatusInavailabilityWorker inavailabilityWorker = new BookingTargetStatusInavailabilityWorker(ctx);
        inavailabilityWorker.prepare();

        BookingTargetStatusPlaceWorker placeWorker = new BookingTargetStatusPlaceWorker(ctx);
        placeWorker.prepare();

        BookingTargetStatusAddressWorker addressWorker = new BookingTargetStatusAddressWorker(ctx);
        addressWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (BookingTargetAvailabilityType ba : bookingTargetList) {
            BookingTargetIDType bookingTargetId = ba.getID();
            statusWorker.bind(ba);

            if (ba.isSetGeoPosition() && ba.getGeoPosition().isSetAddress()) {
                addressWorker.bind(bookingTargetId, ba.getGeoPosition().getAddress());
            }

            if (ba.isSetInavailability()) {
                inavailabilityWorker.bindDelete(bookingTargetId);
                for (TimePeriodType tp : ba.getInavailability()) {
                    inavailabilityWorker.bindInsert(bookingTargetId, tp);
                }
            }

            placeWorker.bindDelete(bookingTargetId);

            if (ba.isSetPlaceID()) {
                placeWorker.bindInsert(bookingTargetId, ba.getPlaceID());
            }
        }

        // -------------------------------------------------------------------------
        // Execute
        // -------------------------------------------------------------------------

        statusWorker.execute();
        inavailabilityWorker.execute();
        addressWorker.execute();
        placeWorker.execute();
    }

    @Override
    @Transactional
    public void updateBookingTargetChangeAvailabilityList(List<BookingTargetChangeAvailabilityType> bookingTargetList) {
        log.debug("Size of the arrived booking target availability changes list: {}", bookingTargetList.size());

        for (BookingTargetChangeAvailabilityType item : bookingTargetList) {
            refreshStatus(item);
            refreshStatusAddress(item);
            refreshStatusInavailability(item);
            refreshStatusPlace(item);
        }
    }

    /**
     * Cron job that runs at fixed intervals to clean up inavailabilities
     */
    @Schedule(minute = "*/15", hour = "*", persistent = false)
    @Override
    public void cleanUpInavailability() {

        // 1. Empty inavailabilities
        Condition c1 = BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY.eq(new EmptyTsRange());

        // -------------------------------------------------------------------------
        // 2. Duplicate inavailability entries
        // https://wiki.postgresql.org/wiki/Deleting_duplicates
        // -------------------------------------------------------------------------

        // CTID is a hidden postgres column
        // http://www.postgresql.org/docs/9.4/static/ddl-system-columns.html
        Field<Object> ctid = DSL.field("ctid");

        Field<Integer> rowNum = DSL.rowNumber()
                                   .over(DSL.partitionBy(BOOKING_TARGET_STATUS_INAVAILABILITY.PROVIDER_ID,
                                           BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID,
                                           BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY)
                                            .orderBy(ctid))
                                   .as("rowNum");

        Condition c2 = ctid.in(DSL.select(ctid)
                                  .from(DSL.select(ctid, rowNum)
                                           .from(BOOKING_TARGET_STATUS_INAVAILABILITY)
                                           .asTable("t"))
                                  .where(rowNum.greaterThan(1)));

        // 3. Inavailabilities that are in the past
        Condition c3 = CustomDSL.upper(BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY)
                                .lessThan(CustomDSL.utcTimestamp());

        // Run
        ctx.delete(BOOKING_TARGET_STATUS_INAVAILABILITY)
           .where(c1.or(c2).or(c3))
           .execute();
    }

    @Override
    public AttributeClassType getAttributeClassType(String bookingTargetId, String providerId) {

        String classTypeString =
                ctx.select(BOOKING_TARGET.CLASS)
                   .from(BOOKING_TARGET)
                   .where(BOOKING_TARGET.PROVIDER_ID.eq(providerId))
                   .and(BOOKING_TARGET.BOOKING_TARGET_ID.eq(bookingTargetId))
                   .fetchOne(BOOKING_TARGET.CLASS);

        if (classTypeString == null) {
            throw new IxsiProcessingException("ClassType is missing for BookingTarget with BookingTargetId: " + bookingTargetId);
        }

        ClassType classType = ClassType.fromValue(classTypeString);

        switch (classType) {
            case BIKE:
                return AttributeClassType.BIKE_SHARING;
            case MOTORCYCLE:
                throw new IxsiProcessingException("No AttributeClassType for ClassType: " + classType.value());
            case MEDIUM:
            case MICRO:
            case MINI:
            case SMALL:
            case LARGE:
            case TRANSPORTER:
            case VAN:
                return AttributeClassType.CAR_SHARING;
            default:
                throw new IxsiProcessingException("Can't find AttributeClassType match for ClassType: " + classTypeString + " with BookingTargetId: " + bookingTargetId);
        }
    }

    // -------------------------------------------------------------------------
    // Private helper methods
    // -------------------------------------------------------------------------

    private void updateStatus(int partnerId, ItemIdComparator<BookingTargetIDType> comparator) {
        List<BookingTargetIDType> toDelete = comparator.getForDelete();

        BatchBindStep batchBindStep = ctx.batch(
                ctx.update(BOOKING_TARGET)
                   .set(BOOKING_TARGET.STATUS, (Status) null)
                   .where(BOOKING_TARGET.BOOKING_TARGET_ID.equal(""))
                   .and(BOOKING_TARGET.PROVIDER_ID.equal(""))
        );

        for (BookingTargetIDType p : toDelete) {
            batchBindStep.bind(Status.INACTIVE, p.getBookeeID(), p.getProviderID());
        }

        batchBindStep.execute();
    }

    private void refreshStatus(BookingTargetChangeAvailabilityType item) {
        String boId = item.getID().getBookeeID();
        String providerId = item.getID().getProviderID();

        if (!isSetCoord(item)) {
            log.debug("Coordinates are not set. Assuming booking target is mobile -> Deleting coordinates");

            ctx.update(BOOKING_TARGET_STATUS)
               .set(BOOKING_TARGET_STATUS.GPS_POSITION, (PGpoint) null)
               .where(BOOKING_TARGET_STATUS.BOOKING_TARGET_ID.eq(boId))
               .and(BOOKING_TARGET_STATUS.PROVIDER_ID.eq(providerId))
               .execute();

            return;
        }

        // -------------------------------------------------------------------------
        // 1. Try an update
        // -------------------------------------------------------------------------

        PGpoint gps = BasicUtils.toPoint(item.getGeoPosition().getCoord());
        int rowCount = ctx.update(BOOKING_TARGET_STATUS)
                          .set(BOOKING_TARGET_STATUS.GPS_POSITION, gps)
                          .where(BOOKING_TARGET_STATUS.BOOKING_TARGET_ID.eq(boId))
                          .and(BOOKING_TARGET_STATUS.PROVIDER_ID.eq(providerId))
                          .execute();

        // -------------------------------------------------------------------------
        // 2. If update fails (=> row doesn't exist), do insert
        // -------------------------------------------------------------------------

        if (rowCount == 0) {
            log.debug("booking_target_status was not inserted into DB for "
                    + "booking_target_id={} and provider_id={} before. Doing an insert...", boId, providerId);

            ctx.insertInto(BOOKING_TARGET_STATUS,
                    BOOKING_TARGET_STATUS.BOOKING_TARGET_ID,
                    BOOKING_TARGET_STATUS.PROVIDER_ID,
                    BOOKING_TARGET_STATUS.GPS_POSITION)
               .values(boId, providerId, gps)
               .execute();
        }
    }

    private void refreshStatusAddress(BookingTargetChangeAvailabilityType item) {
        String boId = item.getID().getBookeeID();
        String providerId = item.getID().getProviderID();

        if (!isSetAddress(item)) {
            log.debug("Address is not set. Assuming booking target is mobile -> Deleting address");

            ctx.delete(BOOKING_TARGET_STATUS_ADDRESS)
               .where(BOOKING_TARGET_STATUS_ADDRESS.BOOKING_TARGET_ID.eq(boId))
               .and(BOOKING_TARGET_STATUS_ADDRESS.PROVIDER_ID.eq(providerId))
               .execute();

            return;
        }

        AddressType address = item.getGeoPosition().getAddress();

        // -------------------------------------------------------------------------
        // 1. Try an update
        // -------------------------------------------------------------------------

        log.debug("Updating booking_target_status_address");

        int rowCount = ctx.update(BOOKING_TARGET_STATUS_ADDRESS)
                          .set(BOOKING_TARGET_STATUS_ADDRESS.COUNTRY, address.getCountry())
                          .set(BOOKING_TARGET_STATUS_ADDRESS.CITY, address.getCity())
                          .set(BOOKING_TARGET_STATUS_ADDRESS.POSTAL_CODE, address.getPostalCode())
                          .set(BOOKING_TARGET_STATUS_ADDRESS.STREET_HOUSE_NR, address.getStreetHouseNr())
                          .where(BOOKING_TARGET_STATUS_ADDRESS.BOOKING_TARGET_ID.eq(boId))
                          .and(BOOKING_TARGET_STATUS_ADDRESS.PROVIDER_ID.eq(providerId))
                          .execute();

        // -------------------------------------------------------------------------
        // 2. If update fails (=> row doesn't exist), do insert
        // -------------------------------------------------------------------------

        if (rowCount == 0) {
            log.debug("booking_target_status_address was not inserted into DB for "
                    + "booking_target_id={} and provider_id={} before. Doing an insert...", boId, providerId);

            ctx.insertInto(BOOKING_TARGET_STATUS_ADDRESS,
                    BOOKING_TARGET_STATUS_ADDRESS.BOOKING_TARGET_ID,
                    BOOKING_TARGET_STATUS_ADDRESS.PROVIDER_ID,
                    BOOKING_TARGET_STATUS_ADDRESS.COUNTRY,
                    BOOKING_TARGET_STATUS_ADDRESS.CITY,
                    BOOKING_TARGET_STATUS_ADDRESS.POSTAL_CODE,
                    BOOKING_TARGET_STATUS_ADDRESS.STREET_HOUSE_NR)
               .values(boId, providerId,
                       address.getCountry(), address.getCity(),
                       address.getPostalCode(), address.getStreetHouseNr())
               .execute();
        }
    }

    private void refreshStatusInavailability(BookingTargetChangeAvailabilityType item) {
        String boId = item.getID().getBookeeID();
        String providerId = item.getID().getProviderID();

        if (BasicUtils.isSetAndValid(item.getAvailability())) {
            TsRange tp = BasicUtils.toTsRange(item.getAvailability());
            ctx.update(BOOKING_TARGET_STATUS_INAVAILABILITY)
               .set(BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY,
                       CustomDSL.difference(BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY, tp))
               .where(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.eq(boId))
               .and(BOOKING_TARGET_STATUS_INAVAILABILITY.PROVIDER_ID.eq(providerId))
               .and(CustomDSL.overlaps(BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY, tp))
               .and(DSL.not(CustomDSL.containedBy(tp, BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY))
                       .or(CustomDSL.lower(BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY).eq(CustomDSL.lower(tp)))
                       .or(CustomDSL.upper(BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY).eq(CustomDSL.upper(tp)))
               ).execute();

            // TODO: splitted ranges will be ignored! 1. recognize case 2. create two new inavailabilities and delete old one
        }

        if (BasicUtils.isSetAndValid(item.getInavailability())) {
            TsRange tp = BasicUtils.toTsRange(item.getInavailability());
            ctx.insertInto(BOOKING_TARGET_STATUS_INAVAILABILITY,
                    BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID,
                    BOOKING_TARGET_STATUS_INAVAILABILITY.PROVIDER_ID,
                    BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY)
               .values(boId, providerId, tp)
               .execute();
        }
    }

    private void refreshStatusPlace(BookingTargetChangeAvailabilityType item) {
        String boId = item.getID().getBookeeID();
        String providerId = item.getID().getProviderID();

        if (!item.isSetPlaceID()) {
            log.debug("Place is not set. Assuming booking target is mobile -> Deleting place");

            ctx.delete(BOOKING_TARGET_STATUS_PLACE)
               .where(BOOKING_TARGET_STATUS_PLACE.BOOKING_TARGET_ID.eq(boId))
               .and(BOOKING_TARGET_STATUS_PLACE.PROVIDER_ID.eq(providerId))
               .execute();

            return;
        }

        String placeId = item.getPlaceID();

        // -------------------------------------------------------------------------
        // 1. Try an update
        // -------------------------------------------------------------------------

        log.debug("Updating booking_target_status_place");

        int rowCount = ctx.update(BOOKING_TARGET_STATUS_PLACE)
                          .set(BOOKING_TARGET_STATUS_PLACE.PLACE_ID, placeId)
                          .where(BOOKING_TARGET_STATUS_PLACE.BOOKING_TARGET_ID.eq(boId))
                          .and(BOOKING_TARGET_STATUS_PLACE.PROVIDER_ID.eq(providerId))
                          .execute();

        // -------------------------------------------------------------------------
        // 2. If update fails (=> row doesn't exist), do insert
        // -------------------------------------------------------------------------

        if (rowCount == 0) {
            log.debug("booking_target_status_place was not inserted into DB for "
                    + "booking_target_id={} and provider_id={} before. Doing an insert...", boId, providerId);

            ctx.insertInto(BOOKING_TARGET_STATUS_PLACE,
                    BOOKING_TARGET_STATUS_PLACE.BOOKING_TARGET_ID,
                    BOOKING_TARGET_STATUS_PLACE.PROVIDER_ID,
                    BOOKING_TARGET_STATUS_PLACE.PLACE_ID)
               .values(boId, providerId, placeId)
               .execute();
        }
    }
}
