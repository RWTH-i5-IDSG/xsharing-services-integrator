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
package de.rwth.idsg.mb.adapter.ixsi.processor.subscription.push;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.InContext;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.PushMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingRepository;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ExternalBookingSubscriptionStore;
import de.rwth.idsg.mb.regioIT.service.RegioITPushService;
import jooq.db.ixsi.enums.EventOrigin;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import xjc.schema.ixsi.ExternalBookingPushMessageType;
import xjc.schema.ixsi.ExternalBookingType;
import xjc.schema.ixsi.UserInfoType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.02.2015
 */
@Slf4j
@Stateless
public class ExternalBookingPushProcessor
        implements PushMessageProcessor<ExternalBookingPushMessageType> {

    @Inject private EnabledSubscriptionService subscriptionService;
    @EJB private ExternalBookingSubscriptionStore subscriptionStore;
    @EJB private RegioITPushService regioITPushService;
    @EJB private BookingRepository bookingRepository;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_ExternalBooking;
    }

    @Override
    public Class<ExternalBookingPushMessageType> getProcessingClass() {
        return ExternalBookingPushMessageType.class;
    }

    @Override
    public void process(InContext<ExternalBookingPushMessageType> context) {
        ExternalBookingType actual = getSubscribed(context);
        if (actual == null) {
            return;
        }

        bookingRepository.create(
                EventOrigin.EXTERNAL,
                new DateTime(),
                actual.getBookingID(),
                actual.getBookingTargetID(),
                actual.getUserInfo().getUserID(),
                actual.getTimePeriod(),
                null,
                null
        );

        int partnerId = context.getPartnerContext().getConfig().getPartnerId();
        regioITPushService.pushExternalBooking(partnerId, actual);

        List<String> bookings = new ArrayList<>();
        bookings.add(actual.getBookingID());

        subscriptionService.bookingAlertSub(context.getPartnerContext(), bookings);
        subscriptionService.consumptionSub(context.getPartnerContext(), bookings);
    }

    /**
     * Look at what we subscribed, and what arrived with the message. Return only the ones that we
     * subscribed to be processed further, discard the others.
     */
    private ExternalBookingType getSubscribed(InContext<ExternalBookingPushMessageType> context) {
        int partnerId = context.getPartnerContext().getConfig().getPartnerId();
        Set<UserInfoType> subscriptions = subscriptionStore.getSubscriptions(partnerId);
        ExternalBookingType arrived = context.getIncoming().getExternalBooking();

        if (subscriptions.contains(arrived.getUserInfo())) {
            return arrived;
        } else {
            log.error("We did not subscribe to {}", arrived);
            return null;
        }
    }
}
