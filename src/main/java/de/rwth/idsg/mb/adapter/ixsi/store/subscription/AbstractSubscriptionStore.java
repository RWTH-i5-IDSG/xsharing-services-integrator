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
package de.rwth.idsg.mb.adapter.ixsi.store.subscription;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.02.2015
 */
public abstract class AbstractSubscriptionStore<T> implements SubscriptionStore<T> {

    @Getter
    private final ConcurrentHashMap<Integer, Set<T>> lookupTable = new ConcurrentHashMap<>();

    @Override
    public Set<T> getSubscriptions(int partnerId) {
        Set<T> set = lookupTable.get(partnerId);
        if (set == null) {
            return Collections.emptySet();
        } else {
            return set;
        }
    }

    @Override
    public void subscribe(int partnerId, List<T> items) {
        Set<T> set = lookupTable.get(partnerId);
        if (set == null) {
            final Set<T> emptySet = new HashSet<>();
            set = lookupTable.putIfAbsent(partnerId, emptySet);
            if (set == null) {
                set = emptySet;
            }
        }
        set.addAll(items);
    }

    @Override
    public void unsubscribe(int partnerId, List<T> items) {
        Set<T> set = lookupTable.get(partnerId);
        if (set != null) {
            set.removeAll(items);
        }
    }

    @Override
    public void unsubscribeAll(int partnerId) {
        lookupTable.remove(partnerId);
    }

    @Override
    public void replaceSubscriptions(int partnerId, List<T> items) {
        Set<T> itemSet = new HashSet<>(items);

        if (lookupTable.containsKey(partnerId)) {
            lookupTable.replace(partnerId, itemSet);
        } else {
            lookupTable.put(partnerId, itemSet);
        }
    }

    @Override
    public int getSize() {
        return lookupTable.values()
                          .stream()
                          .mapToInt(Set::size)
                          .sum();
    }
}
