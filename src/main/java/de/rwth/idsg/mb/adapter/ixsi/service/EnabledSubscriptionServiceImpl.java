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

import de.rwth.idsg.mb.adapter.ixsi.BeanProducer;
import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.EmptyListDiscard;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.UserInfoType;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * Wrapper around the actual business logic service to only trigger actions for enabled IXSI features
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 11.02.2016
 */
@Stateless
public class EnabledSubscriptionServiceImpl implements EnabledSubscriptionService {

    @Inject private BeanProducer beanProducer;

    private SubscriptionService subscriptionService;

    @PostConstruct
    private void init() {
        subscriptionService = beanProducer.getSubscriptionService();
    }

    @Override
    public void requestHeartbeat(PartnerContext pctx) {
        if (isEnabled(pctx, IxsiFeature.SA_HeartBeat)) {
            subscriptionService.requestHeartbeat(pctx);
        }
    }

    // -------------------------------------------------------------------------
    // Availability
    // -------------------------------------------------------------------------

    @EmptyListDiscard
    @Override
    public void availabilitySub(PartnerContext pctx, List<BookingTargetIDType> bookingTargetIDList,
                                Integer subscriptionDurationInMinutes) {
        if (isEnabled(pctx, IxsiFeature.S_Availability)) {
            subscriptionService.availabilitySub(pctx, bookingTargetIDList, subscriptionDurationInMinutes);
        }
    }

    @EmptyListDiscard
    @Override
    public void availabilityUnSub(PartnerContext pctx, List<BookingTargetIDType> bookingTargetIDList) {
        if (isEnabled(pctx, IxsiFeature.S_Availability)) {
            subscriptionService.availabilityUnSub(pctx, bookingTargetIDList);
        }
    }

    @Override
    public void availabilitySubStatus(PartnerContext pctx) {
        if (isEnabled(pctx, IxsiFeature.S_Availability)) {
            subscriptionService.availabilitySubStatus(pctx);
        }
    }

    // -------------------------------------------------------------------------
    // PlaceAvailability
    // -------------------------------------------------------------------------

    @EmptyListDiscard
    @Override
    public void placeAvailabilitySub(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList) {
        if (isEnabled(pctx, IxsiFeature.S_PlaceAvailability)) {
            subscriptionService.placeAvailabilitySub(pctx, placeIDList);
        }
    }

    @EmptyListDiscard
    @Override
    public void placeAvailabilityUnSub(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList) {
        if (isEnabled(pctx, IxsiFeature.S_PlaceAvailability)) {
            subscriptionService.placeAvailabilityUnSub(pctx, placeIDList);
        }
    }

    @Override
    public void placeAvailabilitySubStatus(PartnerContext pctx) {
        if (isEnabled(pctx, IxsiFeature.S_PlaceAvailability)) {
            subscriptionService.placeAvailabilitySubStatus(pctx);
        }
    }

    // -------------------------------------------------------------------------
    // BookingAlert
    // -------------------------------------------------------------------------

    @EmptyListDiscard
    @Override
    public void bookingAlertSub(PartnerContext pctx, List<String> bookingIDList) {
        if (isEnabled(pctx, IxsiFeature.S_BookingAlert)) {
            subscriptionService.bookingAlertSub(pctx, bookingIDList);
        }
    }

    @EmptyListDiscard
    @Override
    public void bookingAlertUnSub(PartnerContext pctx, List<String> bookingIDList) {
        if (isEnabled(pctx, IxsiFeature.S_BookingAlert)) {
            subscriptionService.bookingAlertUnSub(pctx, bookingIDList);
        }
    }

    @Override
    public void bookingAlertSubStatus(PartnerContext pctx) {
        if (isEnabled(pctx, IxsiFeature.S_BookingAlert)) {
            subscriptionService.bookingAlertSubStatus(pctx);
        }
    }

    // -------------------------------------------------------------------------
    // Consumption
    // -------------------------------------------------------------------------

    @EmptyListDiscard
    @Override
    public void consumptionSub(PartnerContext pctx, List<String> bookingIDList) {
        if (isEnabled(pctx, IxsiFeature.S_Consumption)) {
            subscriptionService.consumptionSub(pctx, bookingIDList);
        }
    }

    @EmptyListDiscard
    @Override
    public void consumptionUnSub(PartnerContext pctx, List<String> bookingIDList) {
        if (isEnabled(pctx, IxsiFeature.S_Consumption)) {
            subscriptionService.consumptionUnSub(pctx, bookingIDList);
        }
    }

    @Override
    public void consumptionSubStatus(PartnerContext pctx) {
        if (isEnabled(pctx, IxsiFeature.S_Consumption)) {
            subscriptionService.consumptionSubStatus(pctx);
        }
    }

    // -------------------------------------------------------------------------
    // External Booking
    // -------------------------------------------------------------------------

    @EmptyListDiscard
    @Override
    public void externalBookingSub(PartnerContext pctx, List<UserInfoType> userList) {
        if (isEnabled(pctx, IxsiFeature.S_ExternalBooking)) {
            subscriptionService.externalBookingSub(pctx, userList);
        }
    }

    @EmptyListDiscard
    @Override
    public void externalBookingUnSub(PartnerContext pctx, List<UserInfoType> userList) {
        if (isEnabled(pctx, IxsiFeature.S_ExternalBooking)) {
            subscriptionService.externalBookingUnSub(pctx, userList);
        }
    }

    @Override
    public void externalBookingSubStatus(PartnerContext pctx) {
        if (isEnabled(pctx, IxsiFeature.S_ExternalBooking)) {
            subscriptionService.externalBookingSubStatus(pctx);
        }
    }

    // -------------------------------------------------------------------------
    // Complete
    // -------------------------------------------------------------------------

    @Override
    public void getCompleteAvailability(PartnerContext pctx, Integer maxTargetsInOneMessage) {
        if (isEnabled(pctx, IxsiFeature.S_Availability)) {
            subscriptionService.getCompleteAvailability(pctx, maxTargetsInOneMessage);
        }
    }

    @Override
    public void getCompletePlaceAvailability(PartnerContext pctx, Integer maxPlacesInOneMessage) {
        if (isEnabled(pctx, IxsiFeature.S_PlaceAvailability)) {
            subscriptionService.getCompletePlaceAvailability(pctx, maxPlacesInOneMessage);
        }
    }

    @Override
    public void getCompleteBookingAlert(PartnerContext pctx, Integer maxResultsInOneMessage) {
        if (isEnabled(pctx, IxsiFeature.S_BookingAlert)) {
            subscriptionService.getCompleteBookingAlert(pctx, maxResultsInOneMessage);
        }
    }

    @Override
    public void getCompleteConsumption(PartnerContext pctx, Integer maxResultsInOneMessage) {
        if (isEnabled(pctx, IxsiFeature.S_Consumption)) {
            subscriptionService.getCompleteConsumption(pctx, maxResultsInOneMessage);
        }
    }

    @Override
    public void getCompleteExternalBooking(PartnerContext pctx, Integer maxResultsInOneMessage) {
        if (isEnabled(pctx, IxsiFeature.S_ExternalBooking)) {
            subscriptionService.getCompleteExternalBooking(pctx, maxResultsInOneMessage);
        }
    }

}
