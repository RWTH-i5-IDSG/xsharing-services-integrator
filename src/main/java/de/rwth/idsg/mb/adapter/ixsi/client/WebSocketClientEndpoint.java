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

import de.rwth.idsg.mb.adapter.ixsi.context.CommunicationContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static de.rwth.idsg.mb.Constants.SHUTDOWN_PHRASE;
import static javax.websocket.CloseReason.CloseCodes.GOING_AWAY;
import static javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.10.2014
 */
@Slf4j
@ClientEndpoint
public class WebSocketClientEndpoint {

    private final UUID uuid = UUID.randomUUID();    // To use in equals/hashCode
    private final PartnerContext partnerContext;
    private final Lock sendLock = new ReentrantLock();
    private final AtomicBoolean adapterShuttingDown = new AtomicBoolean(false);
    private final String partnerName;

    private ScheduledFuture pingTask;
    private Session session;

    @Getter private long startTime;
    @Getter private String sessionId;

    public WebSocketClientEndpoint(PartnerContext partnerContext) {
        this.partnerContext = partnerContext;
        this.partnerName = partnerContext.getConfig().getPartnerName();
    }

    // -------------------------------------------------------------------------
    // WebSocket API methods
    // -------------------------------------------------------------------------

    @OnOpen
    public void onOpen(Session newSession) {
        this.session = newSession;
        sessionId = newSession.getId();
        log.info("[{} @ {}] Connection opened.", partnerName, sessionId);

        startTime = System.currentTimeMillis();
        partnerContext.connected(this);

        pingTask = partnerContext.getPingService().create(partnerContext, newSession);
    }

    @OnMessage
    public void onMessage(String incomingMessage) {
        log.info("[{} @ {}] Received: {}", partnerName, sessionId, incomingMessage);
        CommunicationContext context = new CommunicationContext(partnerContext, incomingMessage);

        // Catch all exceptions down the pipeline caused by processors and do not let the communication fail!
        // Otherwise, the connection will break
        try {
            partnerContext.getConsumer().consume(context);
        } catch (Exception e) {
            log.error(getErrorOccurredMsg(), e);
        }
    }

    @OnMessage
    public void onPongMessage(PongMessage pongMessage) {
        //log.info("[{} @ {}] Received pong message", partnerName, sessionId);
    }

    @OnError
    public void onError(Throwable t) {
        log.error(getErrorOccurredMsg() + " (@OnError was called)", t);

        // TODO: When to remove the connection? Does it disconnect automatically for every exception?
        //endpointStore.remove(this);
    }

    @OnClose
    public void onClose(Session closedSession, CloseReason closeReason) {
        log.info("[{} @ {}] Connection closed (Reason: {}). It was open for {} minutes",
                partnerName, sessionId, closeReason, getConnectionMinutes());
        this.session = closedSession;
        pingTask.cancel(true);

        partnerContext.disconnected(this, isNormalClosure(closeReason));
    }

    // -------------------------------------------------------------------------
    // Others
    // -------------------------------------------------------------------------

    private void overrideTimeout(Session session) {
        long oldTimeout = session.getMaxIdleTimeout();
        long newTimeout = TimeUnit.HOURS.toMillis(2);
        session.setMaxIdleTimeout(newTimeout);
        log.info("Session timeout was {} ms, set it to {} ms", oldTimeout, newTimeout);
    }

    public PartnerContext getPartnerContext() {
        return partnerContext;
    }

    public int getPartnerId() {
        return partnerContext.getConfig().getPartnerId();
    }

    public void sendMessage(String message) throws IOException {
        log.info("[{} @ {}] Sending: {}", partnerName, sessionId, message);
        sendLock.lock();
        try {
            session.getBasicRemote().sendText(message);
            log.debug("[{} @ {}] Sent", partnerName, sessionId);
        } finally {
            sendLock.unlock();
        }
    }

    public void shutDown() {
        try {
            adapterShuttingDown.getAndSet(true);
            session.close(new CloseReason(GOING_AWAY, SHUTDOWN_PHRASE));
        } catch (IOException e) {
            log.error(getErrorOccurredMsg(), e);
        }
    }

    public int getConnectionMinutes() {
        long duration =  System.currentTimeMillis() - startTime;
        return (int) TimeUnit.MILLISECONDS.toMinutes(duration);
    }

    private boolean isNormalClosure(CloseReason closeReason) {
        return adapterShuttingDown.get() || closeReason.getCloseCode() == NORMAL_CLOSURE;
    }

    /**
     * Because there are log.error(...) methods with either "msg + throwable",
     * or "msg + arg" parameters but not "msg + arg + throwable", which is what we need
     */
    private String getErrorOccurredMsg() {
        return "[" + partnerName + " @ " + sessionId + "] Error occurred";
    }

    // -------------------------------------------------------------------------
    // equals and hashCode, because WebSocketClientEndpointStoreImpl uses an
    // ArrayDeque of WebSocketClientEndpoint instances and for the operations
    // in ArrayDeque equals/hashCode are relevant.
    //
    // Here, we make an important decision to include only the uuid field
    // (which was added only for this purpose), but not the others to guarantee
    // uniqueness. We cannot use sessionId (even though it's good enough
    // to be used), because it is only set after the connection is opened,
    // and not after object init.
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebSocketClientEndpoint endpoint = (WebSocketClientEndpoint) o;
        return Objects.equals(uuid, endpoint.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
