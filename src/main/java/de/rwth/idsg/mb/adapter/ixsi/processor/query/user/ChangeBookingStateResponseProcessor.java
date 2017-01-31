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
package de.rwth.idsg.mb.adapter.ixsi.processor.query.user;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.UserResponseProcessor;
import de.rwth.idsg.mb.regioIT.resource.BookingResource;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.ChangeBookingStateRequestType;
import xjc.schema.ixsi.ChangeBookingStateResponseType;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.11.2014
 */
@Slf4j
@Stateless
public class ChangeBookingStateResponseProcessor
        implements UserResponseProcessor<ChangeBookingStateRequestType, ChangeBookingStateResponseType> {

    @Inject private BookingResource bookingResource;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.QU_ChangeBookingState;
    }

    @Override
    public Class<ChangeBookingStateResponseType> getProcessingClass() {
        return ChangeBookingStateResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(UserInOutContext<ChangeBookingStateRequestType, ChangeBookingStateResponseType> context) {
        ChangeBookingStateResponseType response = context.getIncoming();

        if (response.isSetError()) {
            bookingResource.asyncErrorResponse(response.getError(), context.getAsyncResponse());
            return;
        }

        switch (context.getOutgoing().getBookingState()) {
            case OPEN:
                bookingResource.asyncBookingUnlockResponse(response.getAttributes(), context.getAsyncResponse());
            case SUSPENDED:
            case CLOSED:
            default:
                log.warn("Received an unexpected message (This feature is not supported)");
        }
    }
}
