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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xjc.schema.ixsi.TransactionType;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.05.2015
 */
public class AbstractItemStore<T> implements ItemStore<T> {

    // Want to get the logger of the extending class and not of this abstract one
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final Map<TransactionType, T> itemMap = new ConcurrentHashMap<>();

    @Override
    public void add(TransactionType transactionId, T item) {
        itemMap.put(transactionId, item);
        log.trace("Added transactionId={}, item={}", transactionId, item);
    }

    /**
     * We access the store after we get a IXSI response to correlate a request/response pair with an item,
     * and we hand it to call site so we don't need the object in the store anymore, hence remove.
     */
    @Override
    public T get(TransactionType transactionId) {
        T item = itemMap.remove(transactionId);
        log.trace("Removed transactionId={}, item={}", transactionId, item);

        if (item == null) {
            throw new NullPointerException("No item found for transactionId=" + transactionId);
        }
        return item;
    }

    @Override
    public int getSize() {
        return itemMap.size();
    }

    @Override
    public Map<TransactionType, T> getACopy() {
        return Collections.unmodifiableMap(itemMap);
    }
}
