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

import java.util.Collection;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
public interface WebSocketClientEndpointStore {

    /**
     * Returns the store size after operation
     */
    int add(WebSocketClientEndpoint endpoint);

    /**
     * Returns the store size after operation
     */
    int remove(WebSocketClientEndpoint endpoint);

    PartnerContext getPartnerContext(int partnerId);
    WebSocketClientEndpoint getNext(int partnerId);
    Collection<WebSocketClientEndpoint> getAll(int partnerId);
    List<WebSocketClientEndpoint> getAll();
    int getSize(int partnerId);
    void clear();
}
