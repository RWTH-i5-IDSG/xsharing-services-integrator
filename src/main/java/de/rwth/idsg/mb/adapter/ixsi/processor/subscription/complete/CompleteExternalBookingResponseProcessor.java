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
package de.rwth.idsg.mb.adapter.ixsi.processor.subscription.complete;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionResponseMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.CompleteExternalBookingRequestType;
import xjc.schema.ixsi.CompleteExternalBookingResponseType;

import javax.ejb.Stateless;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.02.2015
 */
@Slf4j
@Stateless
public class CompleteExternalBookingResponseProcessor
        implements SubscriptionResponseMessageProcessor<CompleteExternalBookingRequestType,
                                                        CompleteExternalBookingResponseType> {

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_ExternalBooking;
    }

    @Override
    public Class<CompleteExternalBookingResponseType> getProcessingClass() {
        return CompleteExternalBookingResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(InOutContext<CompleteExternalBookingRequestType, CompleteExternalBookingResponseType> context) {
        updateState(context.getPartnerContext());
        CompleteExternalBookingResponseType response = context.getIncoming();

        log.debug("CompleteExternalBookingAlert message arrived. messageBlockID={}, isLastPart={}",
                response.getMessageBlockID(), response.isSetLast());

        if (response.isSetExternalBooking()) {
            // TODO : Persist in DB?
            // Not really sure what to do with complete external bookings,
            // since the functionality is only there to notify the app for a single user
            // or to subscribe for consumption after a push
            log.warn("Received an unexpected message (This feature is not supported)");
        } else {
            log.debug("No external booking alert set.");
        }
    }

    /**
     * Notify that we finished SETUP for this subscription
     */
    private void updateState(PartnerContext ctx) {
        ctx.finished(getRelatedFeature());
    }
}
