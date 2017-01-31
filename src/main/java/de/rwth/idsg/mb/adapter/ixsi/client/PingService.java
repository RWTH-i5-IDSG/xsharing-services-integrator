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
package de.rwth.idsg.mb.adapter.ixsi.client;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.rwth.idsg.mb.Constants.Ixsi.PING_INTERVAL_IN_MINUTES;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.05.2015
 */
@Slf4j
@Singleton
public class PingService {

    @Resource private ManagedScheduledExecutorService scheduler;
    @Inject private EnabledSubscriptionService subscriptionService;

    /**
     * Ping every X minutes
     */
    public ScheduledFuture create(PartnerContext pctx, Session session) {
        Runnable r;

        boolean sendHeartbeats = pctx.getConfig().getFeatures().contains(IxsiFeature.SA_HeartBeat);
        if (sendHeartbeats) {
            r = new PingHeartbeatRunner(pctx, session);
        } else {
            r = new PingRunner(pctx, session);
        }

        return scheduler.scheduleAtFixedRate(r, PING_INTERVAL_IN_MINUTES, PING_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);
    }

    private class PingRunner implements Runnable {
        private final PartnerContext pctx;
        private final Session session;

        private PingRunner(PartnerContext pctx, Session session) {
            this.pctx = pctx;
            this.session = session;
        }

        @Override
        public void run() {
            //log.info("[Session: {}] Sending ping message", session.getId());
            try {
                session.getBasicRemote().sendPing(ByteBuffer.wrap("ping-pong".getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                log.error("[Session: {}] Ping failed", session.getId());
            }
        }

        public PartnerContext getCtx() {
            return this.pctx;
        }
    }

    private class PingHeartbeatRunner extends PingRunner {

        private PingHeartbeatRunner(PartnerContext pctx, Session session) {
            super(pctx, session);
        }

        @Override
        public void run() {
            super.run();
            subscriptionService.requestHeartbeat(getCtx());
        }
    }
}
