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
package de.rwth.idsg.mb.adapter.ixsi.client.impl;

import de.rwth.idsg.ixsi.jaxb.PushMessageGroup;
import de.rwth.idsg.ixsi.jaxb.ResponseMessageGroup;
import de.rwth.idsg.ixsi.jaxb.StaticDataResponseGroup;
import de.rwth.idsg.ixsi.jaxb.SubscriptionResponseGroup;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredResponseChoice;
import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.client.api.IncomingIxsiDispatcher;
import de.rwth.idsg.mb.adapter.ixsi.context.CommunicationContext;
import de.rwth.idsg.mb.adapter.ixsi.context.InContext;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionAdminProcessor;
import de.rwth.idsg.mb.adapter.ixsi.store.InOutContextStore;
import de.rwth.idsg.mb.adapter.ixsi.store.ProcessorStore;
import de.rwth.idsg.mb.adapter.ixsi.store.UserInOutContextStore;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.HeartBeatResponseType;
import xjc.schema.ixsi.IxsiMessageType;
import xjc.schema.ixsi.QueryResponseType;
import xjc.schema.ixsi.SubscriptionResponseType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.09.2014
 */
@Slf4j
@Stateless
public class IncomingIxsiDispatcherImpl implements IncomingIxsiDispatcher {

    @EJB private ProcessorStore processorStore;
    @EJB private SubscriptionAdminProcessor heartBeatResponseProcessor;

    @EJB private InOutContextStore inOutContextStore;
    @EJB private UserInOutContextStore userInOutContextStore;

    // -------------------------------------------------------------------------
    // 1. Level
    // -------------------------------------------------------------------------

    @Override
    public void handle(CommunicationContext context) {
        log.trace("Entered handle...");

        IxsiMessageType incoming = context.getIncomingIxsi();

        if (incoming.isSetResponse()) {
            handleResponse(context);

        } else if (incoming.isSetSubscriptionResponse()) {
            handleSubscriptionResponse(context);

        } else if (incoming.isSetSubscriptionMessage()) {
            handlePush(context);

        } else {
            throw new IxsiProcessingException("Incoming Ixsi is unknown");
        }
    }

    // -------------------------------------------------------------------------
    // 2. Level
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void handleResponse(CommunicationContext context) {
        List<QueryResponseType> responseList = context.getIncomingIxsi().getResponse();
        log.trace("QueryResponseType size: {}", responseList.size());

        for (QueryResponseType response : responseList) {
            if (response.isSetStaticDataResponseGroup()) {
                StaticDataResponseGroup s = response.getStaticDataResponseGroup();

                InOutContext inOutContext = inOutContextStore.get(response.getTransaction());
                inOutContext.setAfterResponse(s, context.getPartnerContext());
                processorStore.find(s).processIfEnabled(inOutContext);

            } else if (response.isSetUserTriggeredResponseGroup()) {
                UserTriggeredResponseChoice u = response.getUserTriggeredResponseGroup();

                // TODO: process these
                // response.getSessionID();
                // response.getSessionTimeout();

                UserInOutContext inOutContext = userInOutContextStore.get(response.getTransaction());
                inOutContext.setAfterResponse(u, context.getPartnerContext());
                try {
                    processorStore.find(u).processIfEnabled(inOutContext);
                } catch (Exception e) {
                    boolean success = inOutContext.getAsyncResponse().resume(e);
                    if (success) {
                        log.error("Error occurred. This has been conveyed to the user", e);
                    } else {
                        log.error("Error occurred. This could NOT be conveyed to the user", e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSubscriptionResponse(CommunicationContext context) {
        SubscriptionResponseType response = context.getIncomingIxsi().getSubscriptionResponse();

        // TODO: Do something with the CalcTime/duration

        if (response.isSetSubscriptionResponseGroup()) {
            SubscriptionResponseGroup s = response.getSubscriptionResponseGroup();

            InOutContext inOutContext = inOutContextStore.get(response.getTransaction());
            inOutContext.setAfterResponse(s, context.getPartnerContext());
            processorStore.find(s).processIfEnabled(inOutContext);

        } else if (response.isSetResponseMessageGroup()) {
            ResponseMessageGroup r = response.getResponseMessageGroup();

            InOutContext inOutContext = inOutContextStore.get(response.getTransaction());
            inOutContext.setAfterResponse(r, context.getPartnerContext());
            processorStore.find(r).processIfEnabled(inOutContext);

        } else if (response.isSetHeartBeat()) {
            HeartBeatResponseType r = response.getHeartBeat();

            InOutContext inOutContext = inOutContextStore.get(response.getTransaction());
            inOutContext.setAfterResponse(r, context.getPartnerContext());
            heartBeatResponseProcessor.processIfEnabled(inOutContext);
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePush(CommunicationContext context) {
        PushMessageGroup p = context.getIncomingIxsi().getSubscriptionMessage().getPushMessageGroup();
        processorStore.find(p).processIfEnabled(new InContext(context.getPartnerContext(), p));
    }
}
