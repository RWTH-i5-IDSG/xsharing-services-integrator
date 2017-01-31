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
package de.rwth.idsg.mb.adapter.ixsi.service;

import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.UserInfoType;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.11.2014
 */
public interface SubscriptionService {

    // -------------------------------------------------------------------------
    // Subscription
    // -------------------------------------------------------------------------

    void requestHeartbeat(PartnerContext pctx);

    void availabilitySub(PartnerContext pctx, List<BookingTargetIDType> bookingTargetIDList,
                         Integer subscriptionDurationInMinutes);

    void availabilityUnSub(PartnerContext pctx, List<BookingTargetIDType> bookingTargetIDList);

    void availabilitySubStatus(PartnerContext pctx);

    void placeAvailabilitySub(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList);

    void placeAvailabilityUnSub(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList);

    void placeAvailabilitySubStatus(PartnerContext pctx);

    void bookingAlertSub(PartnerContext pctx, List<String> bookingIDList);

    void bookingAlertUnSub(PartnerContext pctx, List<String> bookingIDList);

    void bookingAlertSubStatus(PartnerContext pctx);

    void consumptionSub(PartnerContext pctx, List<String> bookingIDList);

    void consumptionUnSub(PartnerContext pctx, List<String> bookingIDList);

    void consumptionSubStatus(PartnerContext pctx);

    void externalBookingSub(PartnerContext pctx, List<UserInfoType> userList);

    void externalBookingUnSub(PartnerContext pctx, List<UserInfoType> userList);

    void externalBookingSubStatus(PartnerContext pctx);

    // -------------------------------------------------------------------------
    // Complete
    // -------------------------------------------------------------------------

    void getCompleteAvailability(PartnerContext pctx, Integer maxTargetsInOneMessage);

    void getCompletePlaceAvailability(PartnerContext pctx, Integer maxPlacesInOneMessage);

    void getCompleteBookingAlert(PartnerContext pctx, Integer maxResultsInOneMessage);

    void getCompleteConsumption(PartnerContext pctx, Integer maxResultsInOneMessage);

    void getCompleteExternalBooking(PartnerContext pctx, Integer maxResultsInOneMessage);

}
