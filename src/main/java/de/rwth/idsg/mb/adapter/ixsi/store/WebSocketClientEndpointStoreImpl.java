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
package de.rwth.idsg.mb.adapter.ixsi.store;

import de.rwth.idsg.mb.adapter.ixsi.client.WebSocketClientEndpoint;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * We want to support multiple connections to a server system.
 *
 * For sending messages we need a mechanism to select one WebSocketClientEndpoint.
 * This is done in a round robin fashion. See getNext().
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 20.11.2014
 */
@Slf4j
@ApplicationScoped
public class WebSocketClientEndpointStoreImpl implements WebSocketClientEndpointStore {

    /**
     * Key   (Integer)                        = ID of the partner server system
     * Value (Deque<WebSocketClientEndpoint>) = WebSocket connections to the server system
     */
    private final ConcurrentHashMap<Integer, Deque<WebSocketClientEndpoint>> lookupTable = new ConcurrentHashMap<>();

    /**
     * Returns the store size after operation
     */
    @Override
    public synchronized int add(WebSocketClientEndpoint endpoint) {
        int partnerId = endpoint.getPartnerId();

        Deque<WebSocketClientEndpoint> endpointDeque = lookupTable.get(partnerId);
        if (endpointDeque == null) {
            final Deque<WebSocketClientEndpoint> emptyDeque = new ArrayDeque<>();
            endpointDeque = lookupTable.putIfAbsent(partnerId, emptyDeque);
            if (endpointDeque == null) {
                endpointDeque = emptyDeque;
            }
        }
        endpointDeque.addLast(endpoint); // Adding at the end

        int size = endpointDeque.size();
        log.debug("A new WebSocketClientEndpoint with id '{}' is stored for partner '{}'. Store size: {}",
                endpoint.getSessionId(), partnerId, size);

        return size;
    }

    /**
     * Returns the store size after operation
     */
    @Override
    public synchronized int remove(WebSocketClientEndpoint endpoint) {
        int partnerId = endpoint.getPartnerId();

        Deque<WebSocketClientEndpoint> endpointDeque = lookupTable.get(partnerId);
        if (endpointDeque != null) {
            endpointDeque.remove(endpoint);
            int size = endpointDeque.size();
            log.debug("The WebSocketClientEndpoint with id '{}' is removed for partner '{}'. Store size: {}",
                    endpoint.getSessionId(), partnerId, size);
            return size;
        }

        return 0;
    }

    @Override
    public PartnerContext getPartnerContext(int partnerId) {
        Deque<WebSocketClientEndpoint> endpointDeque = lookupTable.get(partnerId);
        if (endpointDeque == null || endpointDeque.isEmpty()) {
            throw new NoSuchElementException("There is no connection to the IXSI server");
        }

        WebSocketClientEndpoint endpoint = endpointDeque.peekFirst();
        if (endpoint == null) {
            throw new NoSuchElementException("There is no connection to the IXSI server");
        }

        return endpoint.getPartnerContext();
    }

    /**
     * endpointDeque.removeFirst() will throw NoSuchElementException, if the deque is empty.
     *
     * But the deque itself might be null, if there is no connection established and therefore,
     * the deque is not initialized, yet. In this case we throw the same exception,
     * since the ProducerImpl decides on the context based on the exception type.
     */
    @Override
    public synchronized WebSocketClientEndpoint getNext(int partnerId) {
        Deque<WebSocketClientEndpoint> endpointDeque = lookupTable.get(partnerId);
        if (endpointDeque == null) {
            throw new NoSuchElementException();
        }
        // Get the first item, and add at the end
        WebSocketClientEndpoint s = endpointDeque.removeFirst();
        endpointDeque.addLast(s);
        return s;

    }

    @Override
    public synchronized Collection<WebSocketClientEndpoint> getAll(int partnerId) {
        Deque<WebSocketClientEndpoint> endpointDeque = lookupTable.get(partnerId);
        if (endpointDeque == null) {
            return Collections.emptyList();
        } else {
            return endpointDeque;
        }
    }

    @Override
    public synchronized List<WebSocketClientEndpoint> getAll() {
        List<WebSocketClientEndpoint> list = new ArrayList<>();
        for (Map.Entry<Integer, Deque<WebSocketClientEndpoint>> entry : lookupTable.entrySet()) {
            for (WebSocketClientEndpoint endpoint : entry.getValue()) {
                list.add(endpoint);
            }
        }
        return list;
    }

    @Override
    public int getSize(int partnerId) {
        Deque<WebSocketClientEndpoint> endpointDeque = lookupTable.get(partnerId);
        if (endpointDeque == null) {
            return 0;
        } else {
            return endpointDeque.size();
        }
    }

    @Override
    public synchronized void clear() {
        lookupTable.clear();
        log.debug("Cleared the WebSocketClientEndpoint store");
    }

    @Override
    public String toString() {
        return lookupTable.toString();
    }

}
