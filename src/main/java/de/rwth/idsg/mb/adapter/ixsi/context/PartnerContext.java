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
package de.rwth.idsg.mb.adapter.ixsi.context;

import de.rwth.idsg.mb.adapter.ixsi.Engine;
import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.IxsiFeatureGroup;
import de.rwth.idsg.mb.adapter.ixsi.client.PingService;
import de.rwth.idsg.mb.adapter.ixsi.client.State;
import de.rwth.idsg.mb.adapter.ixsi.client.WebSocketClientEndpoint;
import de.rwth.idsg.mb.adapter.ixsi.client.api.Consumer;
import de.rwth.idsg.mb.adapter.ixsi.client.connect.ConnectionManager;
import de.rwth.idsg.mb.adapter.ixsi.store.WebSocketClientEndpointStore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Set;

/**
 * An instance is shared between all WebSocket connections of a partner.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 07.01.2016
 */
@Slf4j
public class PartnerContext {

    // Part of the builder
    //
    @Getter private final Config config;
    @Getter private final Consumer consumer;
    @Getter private final PingService pingService;
    @Getter private final Engine engine;
    private final ConnectionManager connectionManager;
    private final WebSocketClientEndpointStore endpointStore;

    // Others
    //
    @Getter private State state = State.OFFLINE;
    private static final Object LOCK = new Object();
    private final HashMap<IxsiFeature, SentReceivedPair> subscriptionFinishedMap = new HashMap<>(5);

    @Builder
    public PartnerContext(Config config, Consumer consumer, PingService pingService, Engine engine,
                          ConnectionManager connectionManager, WebSocketClientEndpointStore endpointStore) {
        this.config = config;
        this.consumer = consumer;
        this.pingService = pingService;
        this.engine = engine;
        this.connectionManager = connectionManager;
        this.endpointStore = endpointStore;
    }

    public void connected(WebSocketClientEndpoint endpoint) {
        synchronized (LOCK) {
            connectedInternal(endpoint);
        }
    }

    public void disconnected(WebSocketClientEndpoint endpoint, boolean isNormalClosure) {
        synchronized (LOCK) {
            disconnectedInternal(endpoint, isNormalClosure);
        }
    }

    public void sent(IxsiFeature ff) {
        if (canContinue(ff)) {
            synchronized (LOCK) {
                sentInternal(ff);
            }
        }
    }

    public void finished(IxsiFeature ff) {
        if (canContinue(ff)) {
            synchronized (LOCK) {
                finishedInternal(ff);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private void connectedInternal(WebSocketClientEndpoint endpoint) {
        int size = endpointStore.add(endpoint);

        // Act according to the current state
        switch (state) {
            case OFFLINE:
            case CONNECTING:
                // All connected ?
                if (size == config.getNumberOfConnections()) {
                    log.info("[partnerName: {}] All connected, switching the SETUP state", config.getPartnerName());
                    state = State.SETUP;
                    initMap();
                    engine.setup(this);
                } else {
                    state = State.CONNECTING;
                }
                break;

            case SETUP:
                // TODO:
                // This is an interesting (edge?) case. We lost a connection while trying to set up IXSI, and
                // just reconnected. What happens now? Find a solution!
                break;

            case ONLINE:
                // OK, only some of the connections were gone and we are recovering these.
                // No additional measures needed.
                break;
        }
    }

    private boolean canContinue(IxsiFeature ff) {
        boolean isSubFeature = ff.getGroup() == IxsiFeatureGroup.Subscription;
        boolean isSetup = state == State.SETUP;

        return isSetup && isSubFeature;
    }

    private void disconnectedInternal(WebSocketClientEndpoint endpoint, boolean isNormalClosure) {
        int size = endpointStore.remove(endpoint);

        // All disconnected
        if (size == 0) {
            log.warn("[partnerName: {}] There are no open connections left to IXSI server, switching to OFFLINE",
                    config.getPartnerName());
            state = State.OFFLINE;
            engine.clearStores(this);
            subscriptionFinishedMap.clear();
        }

        if (!isNormalClosure) {
            connectionManager.reconnect(this);
        }
    }

    private void sentInternal(IxsiFeature ff) {
        subscriptionFinishedMap.get(ff).setSentRequest(true);
    }

    private void finishedInternal(IxsiFeature ff) {
        subscriptionFinishedMap.get(ff).setReceivedResponse(true);

        if (checkMapWhetherFinished()) {
            log.info("[partnerName: {}] SETUP finished, switching to ONLINE", config.getPartnerName());
            state = State.ONLINE;
        }
    }

    private void initMap() {
        Set<IxsiFeature> featureSet = config.getFeatures();
        for (IxsiFeature fff : featureSet) {
            if (fff.getGroup() == IxsiFeatureGroup.Subscription) {
                subscriptionFinishedMap.put(fff, new SentReceivedPair());
            }
        }
    }
    
    private boolean checkMapWhetherFinished() {
        for (SentReceivedPair status : subscriptionFinishedMap.values()) {
            if (!status.sentRequest) {
                continue;
            }

            if (!status.isFinished()) {
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Helper classes
    // -------------------------------------------------------------------------

    @Builder
    @Getter
    public static class Config {
        private final int partnerId;
        private final String partnerName;
        private final String basePath;
        private final Integer numberOfConnections;
        private final Set<IxsiFeature> features;
    }

    @Setter
    private static class SentReceivedPair {
        private boolean sentRequest = false;
        private boolean receivedResponse = false;

        boolean isFinished() {
            return sentRequest && receivedResponse;
        }
    }
}
