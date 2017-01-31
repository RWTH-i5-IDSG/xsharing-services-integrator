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

import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import xjc.schema.ixsi.TransactionType;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import java.util.Map;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.05.2015
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class UserInOutContextStore extends AbstractItemStore<UserInOutContext> {

    @EJB private TimeoutHandler asyncResponseTimeoutHandler;

    @PostConstruct
    public void init() {
        log.debug("Ready");
    }

    @Override
    public void add(TransactionType transactionId, UserInOutContext item) {
        item.getAsyncResponse().setTimeoutHandler(asyncResponseTimeoutHandler);
        super.add(transactionId, item);
    }

    /**
     * When there is a problem with the IXSI server (never arriving response, timeouts etc.),
     * we might want to remove the now-dead object from the store.
     *
     * The approach of iterating over the whole map without a key is quite bad,
     * but since these objects have a short lifespan (removed after a response),
     * the map should never grow too much. Under heavy load this might slow
     * the application down, though.
     *
     * In this case, the better solution would be to extend the base class of T
     * in order to include the transactionId in this new object type (or use an interface).
     * That way, we always keep a reference to it in the item itself.
     *
     */
    public void removeItem(AsyncResponse asyncResponse) {
        TransactionType toRemove = null;

        for (Map.Entry<TransactionType, UserInOutContext> entry : itemMap.entrySet()) {
            if (entry.getValue().getAsyncResponse() == asyncResponse) {
                toRemove = entry.getKey();
                break;
            }
        }

        if (toRemove != null) {
            itemMap.remove(toRemove);
        }
    }
}
