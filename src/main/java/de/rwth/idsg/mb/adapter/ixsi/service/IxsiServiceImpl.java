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

import de.rwth.idsg.ixsi.jaxb.RequestMessageGroup;
import de.rwth.idsg.ixsi.jaxb.StaticDataRequestGroup;
import de.rwth.idsg.ixsi.jaxb.SubscriptionRequestGroup;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredRequestChoice;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredResponseChoice;
import de.rwth.idsg.mb.AppConfiguration;
import de.rwth.idsg.mb.adapter.ixsi.client.api.Producer;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.store.InOutContextStore;
import org.joda.time.DateTime;
import xjc.schema.ixsi.AuthType;
import xjc.schema.ixsi.HeartBeatRequestType;
import xjc.schema.ixsi.IxsiMessageType;
import xjc.schema.ixsi.Language;
import xjc.schema.ixsi.QueryRequestType;
import xjc.schema.ixsi.SubscriptionRequestType;
import xjc.schema.ixsi.TransactionType;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.ejb.ConcurrencyManagementType.BEAN;

/**
 * FOR INTERNAL USE ONLY
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
@Singleton
@ConcurrencyManagement(BEAN)
public class IxsiServiceImpl implements IxsiService {

    @EJB private AppConfiguration appConfiguration;
    @EJB private Producer producer;
    @EJB private InOutContextStore inOutContextStore;

    // Important: Every time the application starts, ids start with 1.
    // But, since TransactionType also contains a timestamp, that's okay (or?)
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    private String systemId;

    @PostConstruct
    public void init() {
        systemId = appConfiguration.getIxsi().getSystemId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendStaticRequest(PartnerContext pctx, StaticDataRequestGroup staticRequest, Language lan) {
        QueryRequestType query = new QueryRequestType()
                .withStaticDataRequestGroup(staticRequest)
                .withLanguage(lan);

        setTransactionAndSystemId(query);
        IxsiMessageType ixsi = new IxsiMessageType().withRequest(query);
        producer.send(pctx, ixsi);

        inOutContextStore.add(query.getTransaction(), new InOutContext(pctx, staticRequest));
    }

    @Override
    public UserInOutContext sendUserRequest(PartnerContext pctx, UserTriggeredRequestChoice userRequest,
                                            AuthType auth, Language lan) {
        QueryRequestType query = new QueryRequestType()
                .withUserTriggeredRequestChoice(userRequest)
                .withAuth(auth)
                .withLanguage(lan);

        setTransactionAndSystemId(query);
        IxsiMessageType ixsi = new IxsiMessageType().withRequest(query);
        producer.send(pctx, ixsi);

        // Prepare the return type
        UserInOutContext<UserTriggeredRequestChoice, UserTriggeredResponseChoice> pc = new UserInOutContext<>(pctx, userRequest);
        pc.setUserInfo(auth.getUserInfo());
        pc.setTransaction(query.getTransaction());
        return pc;
    }

    @Override
    public void getHeartbeat(PartnerContext pctx) {
        HeartBeatRequestType h = new HeartBeatRequestType();

        SubscriptionRequestType sub = new SubscriptionRequestType()
                .withHeartBeat(h);

        setTransactionAndSystemId(sub);
        IxsiMessageType ixsi = new IxsiMessageType().withSubscriptionRequest(sub);
        producer.send(pctx, ixsi);

        inOutContextStore.add(sub.getTransaction(), new InOutContext<>(pctx, h));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendSubscriptionRequest(PartnerContext pctx, SubscriptionRequestGroup subscriptionRequest) {
        SubscriptionRequestType sub = new SubscriptionRequestType()
                .withSubscriptionRequestGroup(subscriptionRequest);

        setTransactionAndSystemId(sub);
        IxsiMessageType ixsi = new IxsiMessageType().withSubscriptionRequest(sub);
        producer.send(pctx, ixsi);

        inOutContextStore.add(sub.getTransaction(), new InOutContext(pctx, subscriptionRequest));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendSubscriptionCompleteRequest(PartnerContext pctx, RequestMessageGroup request) {
        SubscriptionRequestType sub = new SubscriptionRequestType()
                .withRequestMessageGroup(request);

        setTransactionAndSystemId(sub);
        IxsiMessageType ixsi = new IxsiMessageType().withSubscriptionRequest(sub);
        producer.send(pctx, ixsi);

        inOutContextStore.add(sub.getTransaction(), new InOutContext(pctx, request));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void setTransactionAndSystemId(QueryRequestType req) {
        req.withTransaction(createTransactionType())
           .withSystemID(systemId);
    }

    private void setTransactionAndSystemId(SubscriptionRequestType req) {
        req.withTransaction(createTransactionType())
           .withSystemID(systemId);
    }

    private TransactionType createTransactionType() {
        return new TransactionType().withTimeStamp(new DateTime())
                                    .withMessageID(atomicInteger.incrementAndGet());
    }
}
