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
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.BookingAlertSubscriptionStore;
import de.rwth.idsg.mb.regioIT.service.RegioITPushService;
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.enums.EventOrigin;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import xjc.schema.ixsi.BookingAlertPushMessageType;
import xjc.schema.ixsi.BookingChangeType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.isTrue;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
@Slf4j
@Stateless
public class BookingAlertPushProcessor
        implements PushMessageProcessor<BookingAlertPushMessageType> {

    @EJB private RegioITPushService regioITPushService;
    @EJB private BookingAlertSubscriptionStore subscriptionStore;
    @EJB private EnabledSubscriptionService subscriptionService;

    @Inject private BookingRepository bookingRepository;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_BookingAlert;
    }

    @Override
    public Class<BookingAlertPushMessageType> getProcessingClass() {
        return BookingAlertPushMessageType.class;
    }

    @Override
    public void process(InContext<BookingAlertPushMessageType> context) {
        if (!context.getIncoming().isSetBookingChange()) {
            return;
        }

        List<BookingChangeType> actual = getSubscribed(context);

        regioITPushService.postBookingAlert(actual);

        for (BookingChangeType bc : actual) {
            delegate(context, bc);
        }
    }

    /**
     * Look at what we subscribed, and what arrived with the message. Return only the ones that we
     * subscribed to be processed further, discard the others.
     */
    private List<BookingChangeType> getSubscribed(InContext<BookingAlertPushMessageType> context) {
        int partnerId = context.getPartnerContext().getConfig().getPartnerId();
        Set<String> subscriptions = subscriptionStore.getSubscriptions(partnerId);

        List<BookingChangeType> arrived = context.getIncoming().getBookingChange();
        List<BookingChangeType> actual = new ArrayList<>(arrived.size());

        for (BookingChangeType item : arrived) {
            if (subscriptions.contains(item.getBookingID())) {
                actual.add(item);
            } else {
                log.error("We did not subscribe to {}", item);
            }
        }
        return actual;
    }

    private void delegate(InContext<BookingAlertPushMessageType> context, BookingChangeType bc) {
        if (isTrue(bc.isCancelled())) {
            handleCancelled(context, bc);

        } else if (bc.isSetNewPeriod()) {
            handleNewPeriod(bc);
        }
    }

    private void handleCancelled(InContext<BookingAlertPushMessageType> context, BookingChangeType bc) {
        subscriptionService.bookingAlertUnSub(context.getPartnerContext(), Collections.singletonList(bc.getBookingID()));
        bookingRepository.cancel(EventOrigin.EXTERNAL, new DateTime(), bc.getBookingID());
    }

    private void handleNewPeriod(BookingChangeType bc) {
        if (BasicUtils.isSetAndValid(bc.getNewPeriod())) {
            bookingRepository.update(EventOrigin.EXTERNAL, new DateTime(), bc.getBookingID(), bc.getNewPeriod());
        } else {
            log.error("Received time period '{}' is invalid. Will not update the booking", bc.getNewPeriod());
        }
    }

}
