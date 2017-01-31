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
package de.rwth.idsg.mb.adapter.ixsi.repository;

import de.rwth.idsg.mb.adapter.ixsi.repository.dto.BookingInfoDTO;
import de.rwth.idsg.mb.monitor.dto.BookingInfoParams;
import jooq.db.ixsi.enums.EventOrigin;
import jooq.db.ixsi.tables.records.BookingChangeRecord;
import jooq.db.ixsi.tables.records.BookingCreateRecord;
import org.joda.time.DateTime;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.ExternalBookingType;
import xjc.schema.ixsi.OriginDestType;
import xjc.schema.ixsi.TimePeriodType;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.07.2015
 */
public interface BookingRepository {

    List<String> getActiveIds(int partnerId);

    void create(EventOrigin origin, DateTime eventTimestamp,
                String bookingId, BookingTargetIDType bookingTargetId, String userId,
                TimePeriodType timePeriod, OriginDestType from, OriginDestType to);

    void update(EventOrigin origin, DateTime eventTimestamp, String bookingId, TimePeriodType newTimePeriod);

    void cancel(EventOrigin origin, DateTime eventTimestamp, String bookingId);

    void insertPushEvent(int partnerId, ExternalBookingType externalBooking, boolean success);

    List<BookingInfoDTO> getInfo(BookingInfoParams param);

    BookingCreateRecord getCreateRecord(String bookingId);

    List<BookingChangeRecord> getChangeRecords(String providerId, String bookingId);

}
