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

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import lombok.RequiredArgsConstructor;
import org.joda.time.Period;
import xjc.schema.ixsi.AvailabilitySubscriptionRequestType;
import xjc.schema.ixsi.AvailabilitySubscriptionStatusRequestType;
import xjc.schema.ixsi.BookingAlertSubscriptionRequestType;
import xjc.schema.ixsi.BookingAlertSubscriptionStatusRequestType;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.CompleteAvailabilityRequestType;
import xjc.schema.ixsi.CompleteBookingAlertRequestType;
import xjc.schema.ixsi.CompleteConsumptionRequestType;
import xjc.schema.ixsi.CompleteExternalBookingRequestType;
import xjc.schema.ixsi.CompletePlaceAvailabilityRequestType;
import xjc.schema.ixsi.ConsumptionSubscriptionRequestType;
import xjc.schema.ixsi.ConsumptionSubscriptionStatusRequestType;
import xjc.schema.ixsi.ExternalBookingSubscriptionRequestType;
import xjc.schema.ixsi.ExternalBookingSubscriptionStatusRequestType;
import xjc.schema.ixsi.PlaceAvailabilitySubscriptionRequestType;
import xjc.schema.ixsi.PlaceAvailabilitySubscriptionStatusRequestType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.UserInfoType;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.11.2014
 */
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final IxsiService ixsiService;

    @Override
    public void requestHeartbeat(PartnerContext pctx) {
        ixsiService.getHeartbeat(pctx);
    }

    // -------------------------------------------------------------------------
    // Availability
    // -------------------------------------------------------------------------

    @Override
    public void availabilitySub(PartnerContext pctx, List<BookingTargetIDType> bookingTargetIDList,
                                Integer subscriptionDurationInMinutes) {
        AvailabilitySubscriptionRequestType a = new AvailabilitySubscriptionRequestType()
                .withBookingTargetID(bookingTargetIDList);

        if (subscriptionDurationInMinutes != null) {
            Period eventHorizon = Period.minutes(subscriptionDurationInMinutes);
            a.setEventHorizon(eventHorizon);
        }
        ixsiService.sendSubscriptionRequest(pctx, a);
        pctx.sent(IxsiFeature.S_Availability);
    }

    @Override
    public void availabilityUnSub(PartnerContext pctx, List<BookingTargetIDType> bookingTargetIDList) {
        AvailabilitySubscriptionRequestType a = new AvailabilitySubscriptionRequestType()
                .withBookingTargetID(bookingTargetIDList)
                .withUnsubscription(true);

        ixsiService.sendSubscriptionRequest(pctx, a);
    }

    @Override
    public void availabilitySubStatus(PartnerContext pctx) {
        AvailabilitySubscriptionStatusRequestType a = new AvailabilitySubscriptionStatusRequestType();
        ixsiService.sendSubscriptionRequest(pctx, a);
    }

    // -------------------------------------------------------------------------
    // PlaceAvailability
    // -------------------------------------------------------------------------

    @Override
    public void placeAvailabilitySub(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList) {
        PlaceAvailabilitySubscriptionRequestType p = new PlaceAvailabilitySubscriptionRequestType()
                .withPlaceID(placeIDList);

        ixsiService.sendSubscriptionRequest(pctx, p);
        pctx.sent(IxsiFeature.S_PlaceAvailability);
    }

    @Override
    public void placeAvailabilityUnSub(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList) {
        PlaceAvailabilitySubscriptionRequestType p = new PlaceAvailabilitySubscriptionRequestType()
                .withPlaceID(placeIDList)
                .withUnsubscription(true);

        ixsiService.sendSubscriptionRequest(pctx, p);
    }

    @Override
    public void placeAvailabilitySubStatus(PartnerContext pctx) {
        PlaceAvailabilitySubscriptionStatusRequestType p = new PlaceAvailabilitySubscriptionStatusRequestType();
        ixsiService.sendSubscriptionRequest(pctx, p);
    }

    // -------------------------------------------------------------------------
    // BookingAlert
    // -------------------------------------------------------------------------

    @Override
    public void bookingAlertSub(PartnerContext pctx, List<String> bookingIDList) {
        BookingAlertSubscriptionRequestType b = new BookingAlertSubscriptionRequestType()
                .withBookingID(bookingIDList);

        ixsiService.sendSubscriptionRequest(pctx, b);
        pctx.sent(IxsiFeature.S_BookingAlert);
    }

    @Override
    public void bookingAlertUnSub(PartnerContext pctx, List<String> bookingIDList) {
        BookingAlertSubscriptionRequestType b = new BookingAlertSubscriptionRequestType()
                .withBookingID(bookingIDList)
                .withUnsubscription(true);

        ixsiService.sendSubscriptionRequest(pctx, b);
    }

    @Override
    public void bookingAlertSubStatus(PartnerContext pctx) {
        BookingAlertSubscriptionStatusRequestType b = new BookingAlertSubscriptionStatusRequestType();
        ixsiService.sendSubscriptionRequest(pctx, b);
    }

    // -------------------------------------------------------------------------
    // Consumption
    // -------------------------------------------------------------------------

    @Override
    public void consumptionSub(PartnerContext pctx, List<String> bookingIDList) {
        ConsumptionSubscriptionRequestType c = new ConsumptionSubscriptionRequestType()
                .withBookingID(bookingIDList);

        ixsiService.sendSubscriptionRequest(pctx, c);
        pctx.sent(IxsiFeature.S_Consumption);
    }

    @Override
    public void consumptionUnSub(PartnerContext pctx, List<String> bookingIDList) {
        ConsumptionSubscriptionRequestType c = new ConsumptionSubscriptionRequestType()
                .withBookingID(bookingIDList)
                .withUnsubscription(true);

        ixsiService.sendSubscriptionRequest(pctx, c);
    }

    @Override
    public void consumptionSubStatus(PartnerContext pctx) {
        ConsumptionSubscriptionStatusRequestType c = new ConsumptionSubscriptionStatusRequestType();
        ixsiService.sendSubscriptionRequest(pctx, c);
    }

    // -------------------------------------------------------------------------
    // External Booking
    // -------------------------------------------------------------------------

    @Override
    public void externalBookingSub(PartnerContext pctx, List<UserInfoType> userList) {
        ExternalBookingSubscriptionRequestType e = new ExternalBookingSubscriptionRequestType()
                .withUserInfo(userList);

        ixsiService.sendSubscriptionRequest(pctx, e);
        pctx.sent(IxsiFeature.S_ExternalBooking);
    }

    @Override
    public void externalBookingUnSub(PartnerContext pctx, List<UserInfoType> userList) {
        ExternalBookingSubscriptionRequestType e = new ExternalBookingSubscriptionRequestType()
                .withUserInfo(userList)
                .withUnsubscription(true);

        ixsiService.sendSubscriptionRequest(pctx, e);
    }

    @Override
    public void externalBookingSubStatus(PartnerContext pctx) {
        ExternalBookingSubscriptionStatusRequestType e = new ExternalBookingSubscriptionStatusRequestType();
        ixsiService.sendSubscriptionRequest(pctx, e);
    }

    // -------------------------------------------------------------------------
    // Complete
    // -------------------------------------------------------------------------

    @Override
    public void getCompleteAvailability(PartnerContext pctx, Integer maxTargetsInOneMessage) {
        CompleteAvailabilityRequestType c = new CompleteAvailabilityRequestType()
                .withMaxTargets(maxTargetsInOneMessage);

        ixsiService.sendSubscriptionCompleteRequest(pctx, c);
    }

    @Override
    public void getCompletePlaceAvailability(PartnerContext pctx, Integer maxPlacesInOneMessage) {
        CompletePlaceAvailabilityRequestType c = new CompletePlaceAvailabilityRequestType()
                .withMaxPlaces(maxPlacesInOneMessage);

        ixsiService.sendSubscriptionCompleteRequest(pctx, c);
    }

    @Override
    public void getCompleteBookingAlert(PartnerContext pctx, Integer maxResultsInOneMessage) {
        CompleteBookingAlertRequestType c = new CompleteBookingAlertRequestType()
                .withMaxResults(maxResultsInOneMessage);

        ixsiService.sendSubscriptionCompleteRequest(pctx, c);
    }

    @Override
    public void getCompleteConsumption(PartnerContext pctx, Integer maxResultsInOneMessage) {
        CompleteConsumptionRequestType c = new CompleteConsumptionRequestType()
                .withMaxResults(maxResultsInOneMessage);

        ixsiService.sendSubscriptionCompleteRequest(pctx, c);
    }

    @Override
    public void getCompleteExternalBooking(PartnerContext pctx, Integer maxResultsInOneMessage) {
        CompleteExternalBookingRequestType c = new CompleteExternalBookingRequestType()
                .withMaxResults(maxResultsInOneMessage);

        ixsiService.sendSubscriptionCompleteRequest(pctx, c);
    }

}
