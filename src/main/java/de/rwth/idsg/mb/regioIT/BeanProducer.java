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
package de.rwth.idsg.mb.regioIT;

import de.rwth.idsg.mb.AppConfiguration;
import de.rwth.idsg.mb.regioIT.client.rest.AuthHeaderFilter;
import de.rwth.idsg.mb.regioIT.client.rest.api.AuthClient;
import de.rwth.idsg.mb.regioIT.client.rest.api.BookingClient;
import de.rwth.idsg.mb.regioIT.client.rest.api.ConsumptionClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Wolfgang Kluth <kluth@dbis.rwth-aachen.de>
 * @since 20.04.2015
 */
@ApplicationScoped
public class BeanProducer {

    @Resource private ManagedScheduledExecutorService scheduler;
    @Inject private AuthHeaderFilter authHeaderFilter;
    @Inject private AppConfiguration config;

    private ResteasyWebTarget produceWebTarget() {
        return new ResteasyClientBuilder()
                .connectionPoolSize(100)
                .maxPooledPerRoute(20)
                .asyncExecutor(scheduler)
                .build()
                .target(config.getRegioIT().getRestBaseUrl());
    }

    @Produces
    @ApplicationScoped
    private BookingClient externalBookingClient() {
        return produceWebTarget().register(authHeaderFilter).proxy(BookingClient.class);
    }

    @Produces
    @ApplicationScoped
    private ConsumptionClient consumptionClient() {
        return produceWebTarget().register(authHeaderFilter).proxy(ConsumptionClient.class);
    }

    @Produces
    @ApplicationScoped
    private AuthClient appAuthClient() {
        return produceWebTarget().proxy(AuthClient.class);
    }

}
