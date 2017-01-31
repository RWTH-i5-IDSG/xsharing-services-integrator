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

import java.util.List;
import java.util.Set;

/**
 * Created by max on 13/01/15.
 */
public interface SubscriptionStore<T> {
    Set<T> getSubscriptions(int partnerId);
    void subscribe(int partnerId, List<T> items);
    void unsubscribe(int partnerId, List<T> items);
    void unsubscribeAll(int partnerId);
    void replaceSubscriptions(int partnerId, List<T> items);
    int getSize();
}
