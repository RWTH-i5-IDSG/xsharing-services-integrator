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
package de.rwth.idsg.mb.adapter.ixsi.client.connect;

import de.rwth.idsg.mb.Constants;
import de.rwth.idsg.mb.adapter.ixsi.client.WebSocketClientEndpoint;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.repository.ServerSystemRepository;
import de.rwth.idsg.mb.adapter.ixsi.store.WebSocketClientEndpointStore;
import de.rwth.idsg.mb.notification.Mailer;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The central component for connection management.
 *
 * For M partners with N number of connections defined for each partner, we create M*N ConnectContext instances.
 * Each ConnectContext instance is responsible for establishing only one connection.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.10.2015
 */
@Slf4j
@ApplicationScoped
public class ConnectionManager {

    @Inject private WebSocketClientEndpointStore store;
    @Inject private ServerSystemRepository serverSystemRepository;
    @Inject private Mailer mailer;

    @Resource private ManagedScheduledExecutorService scheduler;

    private WebSocketContainer webSocketContainer;
    private static final AtomicInteger CONTEXT_ID = new AtomicInteger(0);

    // To keep track of pending connections
    private final ConcurrentHashMap<Long, ConnectContext> connectContextMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setDefaultMaxTextMessageBufferSize(Constants.Ixsi.MAX_TEXT_MSG_SIZE);
        webSocketContainer.setDefaultMaxSessionIdleTimeout(Constants.Ixsi.SESSION_IDLE_TIMEOUT);
    }

    public void connect(List<PartnerContext> contextList) {
        if (contextList.isEmpty()) {
            log.warn("Nothing to connect to. Please check the '{}' table", serverSystemRepository.getTableName());
            return;
        }

        for (PartnerContext s : contextList) {
            for (short i = 0; i < s.getConfig().getNumberOfConnections(); i++) {
                connect(s);
            }
        }
    }

    public void reconnect(PartnerContext s) {
        connect(s);
    }

    @PreDestroy
    public void destroy() {
        for (WebSocketClientEndpoint e : store.getAll()) {
            e.shutDown();
        }
    }

    public void destroy(int partnerId) {
        log.debug("Starting to close WebSocket connections to partnerId '{}'...", partnerId);
        for (WebSocketClientEndpoint e : store.getAll(partnerId)) {
            e.shutDown();
        }
    }

    @Asynchronous
    private void connect(PartnerContext context) {
        ConnectContext cc = new ConnectContext(this, context);
        connectContextMap.put(cc.getContextId(), cc);
        cc.start();
    }

    public void deleteContext(ConnectContext cc) {
        connectContextMap.remove(cc.getContextId());
    }

    public List<Long> getActiveConnectContextIds(int partnerId) {
        return connectContextMap.values()
                                .stream()
                                .filter(cc -> cc.getPartnerContext().getConfig().getPartnerId() == partnerId)
                                .map(ConnectContext::getContextId)
                                .collect(Collectors.toList());
    }

    /**
     * We use an iterator because we want to iterate the set and remove an object
     * from the iterated set, at the same time. Otherwise, ConcurrentModificationException
     */
    public void cancelReconnectJobs(int partnerId) {
        Iterator<Map.Entry<Long, ConnectContext>> it = connectContextMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Long, ConnectContext> entry = it.next();
            if (entry.getValue().getPartnerContext().getConfig().getPartnerId() == partnerId) {
                entry.getValue().cancelReconnectJob();
                it.remove();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public long getNextContextId() {
        return CONTEXT_ID.incrementAndGet();
    }

    public Mailer getMailer() {
        return this.mailer;
    }

    public ManagedScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    public WebSocketContainer getWebSocketContainer() {
        return this.webSocketContainer;
    }
}
