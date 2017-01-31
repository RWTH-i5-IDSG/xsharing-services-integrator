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


import xjc.schema.ixsi.AttributeClassType;
import xjc.schema.ixsi.BookingTargetAvailabilityType;
import xjc.schema.ixsi.BookingTargetChangeAvailabilityType;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.BookingTargetType;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.11.2014
 */
public interface BookingTargetRepository {
    List<BookingTargetIDType> getIds(int partnerId);
    List<BookingTargetIDType> getActiveIds(int partnerId);

    void upsertBookingTargetList(int partnerId, List<BookingTargetType> bookingTargetList);
    void insertBookingTargetAvailabilityList(List<BookingTargetAvailabilityType> bookingTargetList);
    void updateBookingTargetChangeAvailabilityList(List<BookingTargetChangeAvailabilityType> bookingTargetList);

    /**
     * Cron job that runs at fixed intervals to clean up inavailabilities
     */
    void cleanUpInavailability();

    AttributeClassType getAttributeClassType(String bookingTargetId, String providerId);
}
