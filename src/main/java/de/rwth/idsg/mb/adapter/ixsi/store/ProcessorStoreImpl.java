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

import com.google.common.base.Preconditions;
import de.rwth.idsg.ixsi.jaxb.PushMessageGroup;
import de.rwth.idsg.ixsi.jaxb.ResponseMessageGroup;
import de.rwth.idsg.ixsi.jaxb.StaticDataResponseGroup;
import de.rwth.idsg.ixsi.jaxb.SubscriptionResponseGroup;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredResponseChoice;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.ClassAwareProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.EnabledProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.PushMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.StaticResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionResponseMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.UserResponseProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ejb.ConcurrencyManagementType.BEAN;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
@Slf4j
@Singleton
@ConcurrencyManagement(BEAN)
public class ProcessorStoreImpl implements ProcessorStore {

    @Inject private BeanManager beanManager;

    private final LookupMap<StaticDataResponseGroup, StaticResponseProcessor>
            queryStaticMap = new LookupMap<>("queryStaticMap");

    private final LookupMap<UserTriggeredResponseChoice, UserResponseProcessor>
            queryUserMap = new LookupMap<>("queryUserMap");

    private final LookupMap<SubscriptionResponseGroup, SubscriptionResponseProcessor>
            subscriptionResponseMap = new LookupMap<>("subscriptionResponseMap");

    private final LookupMap<ResponseMessageGroup, SubscriptionResponseMessageProcessor>
            subscriptionResponseMessageMap = new LookupMap<>("subscriptionResponseMessageMap");

    private final LookupMap<PushMessageGroup, PushMessageProcessor>
            pushMessageMap = new LookupMap<>("pushMessageMap");

    @PostConstruct
    public void init() {
        Set<ClassAwareProcessor> allProcessorBeans = getEnabledProcessorBeans();

        queryStaticMap.putAll(getForProcessingClass(allProcessorBeans, StaticDataResponseGroup.class));
        queryUserMap.putAll(getForProcessingClass(allProcessorBeans, UserTriggeredResponseChoice.class));
        subscriptionResponseMap.putAll(getForProcessingClass(allProcessorBeans, SubscriptionResponseGroup.class));
        subscriptionResponseMessageMap.putAll(getForProcessingClass(allProcessorBeans, ResponseMessageGroup.class));
        pushMessageMap.putAll(getForProcessingClass(allProcessorBeans, PushMessageGroup.class));

        log.trace("Ready");
    }

    @Override
    public StaticResponseProcessor find(StaticDataResponseGroup s) {
        return queryStaticMap.get(s.getClass());
    }

    @Override
    public UserResponseProcessor find(UserTriggeredResponseChoice s) {
        return queryUserMap.get(s.getClass());
    }

    @Override
    public PushMessageProcessor find(PushMessageGroup s) {
        return pushMessageMap.get(s.getClass());
    }

    @Override
    public SubscriptionResponseProcessor find(SubscriptionResponseGroup s) {
        return subscriptionResponseMap.get(s.getClass());
    }

    @Override
    public SubscriptionResponseMessageProcessor find(ResponseMessageGroup s) {
        return subscriptionResponseMessageMap.get(s.getClass());
    }

    /**
     * Some pure Java EE black magic right here. Through the BeanManager we can discover all bean instances of
     * the given class type. Since we have many, many processors for response/message groups it was becoming
     * maintenance hell to manually inject them into this class and add them to the corresponding lookup map.
     */
    private Set<ClassAwareProcessor> getEnabledProcessorBeans() {
        HashSet<ClassAwareProcessor> actual = new HashSet<>();
        for (Bean<?> bean : beanManager.getBeans(EnabledProcessor.class)) {
            CreationalContext ctx = beanManager.createCreationalContext(bean);
            EnabledProcessor instance = (EnabledProcessor) beanManager.getReference(bean, EnabledProcessor.class, ctx);
            actual.add((ClassAwareProcessor) instance);
        }
        return actual;
    }

    /**
     * Iterate through the ClassAwareProcessor set, and return only the processors that are able to process
     * objects that extend/implement the given base class.
     */
    @SuppressWarnings("unchecked")
    private <T, K extends ClassAwareProcessor> List<K> getForProcessingClass(Set<ClassAwareProcessor> input,
                                                                             Class<T> baseMessageClazz) {
        return input.stream()
                    .filter(p -> baseMessageClazz.isAssignableFrom(p.getProcessingClass()))
                    .map(p -> (K) p)
                    .collect(Collectors.toList());
    }

    /**
     * A simple wrapper around HashMap with minor modifications for our own use case. Since we have many, many
     * processors for response/message groups, it was a maintenance hell to manually set the class type of each
     * processor in the map. With the addition of ClassAwareProcessor, each processor can now provide this class
     * information.
     */
    @RequiredArgsConstructor
    private static class LookupMap<T, K extends ClassAwareProcessor> {
        private final String name;
        private final HashMap<Class<? extends T>, K> lookup = new HashMap<>();

        private K get(Class<? extends T> clazz) {
            return Preconditions.checkNotNull(lookup.get(clazz));
        }

        @SuppressWarnings("unchecked")
        private void putAll(List<K> items) {
            for (K item : items) {
                lookup.put(Preconditions.checkNotNull(item.getProcessingClass()), item);
            }
            log();
        }

        private void log() {
            log.info("{} (size:{}) => {}", name, lookup.size(), lookup);
        }
    }
}
