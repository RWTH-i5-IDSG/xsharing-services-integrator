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
package de.rwth.idsg.mb.adapter.ixsi.processor.subscription.response;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ExternalBookingSubscriptionStore;
import xjc.schema.ixsi.ExternalBookingSubscriptionRequestType;
import xjc.schema.ixsi.ExternalBookingSubscriptionResponseType;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.isTrue;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.02.2015
 */
@Stateless
public class ExternalBookingSubscriptionResponseProcessor
        implements SubscriptionResponseProcessor<ExternalBookingSubscriptionRequestType,
                                                 ExternalBookingSubscriptionResponseType> {

    @EJB private ExternalBookingSubscriptionStore subscriptionStore;
    @EJB private EnabledSubscriptionService subscriptionService;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_ExternalBooking;
    }

    @Override
    public Class<ExternalBookingSubscriptionResponseType> getProcessingClass() {
        return ExternalBookingSubscriptionResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(InOutContext<ExternalBookingSubscriptionRequestType,
                                     ExternalBookingSubscriptionResponseType> context) {
        if (context.getIncoming().isSetError()) {
            return;
        }

        int partnerId = context.getPartnerContext().getConfig().getPartnerId();
        ExternalBookingSubscriptionRequestType request = context.getOutgoing();

        if (isTrue(request.isUnsubscription())) {
            // invalidate subscriptions
            subscriptionStore.unsubscribe(partnerId, request.getUserInfo());
        } else {
            // apply subscription
            subscriptionStore.subscribe(partnerId, request.getUserInfo());
        }

        optionalGetComplete(context.getPartnerContext());
    }

    /**
     * Get complete when in SETUP phase
     */
    private void optionalGetComplete(PartnerContext ctx) {
        if (ctx.getState().isSetup()) {
            subscriptionService.getCompleteExternalBooking(ctx, null);
        }
    }
}
