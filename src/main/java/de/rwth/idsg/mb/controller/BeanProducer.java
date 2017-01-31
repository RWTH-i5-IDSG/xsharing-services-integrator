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
package de.rwth.idsg.mb.controller;

import de.ivu.realtime.modules.ura.client.UraClient;
import de.rwth.idsg.mb.AppConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 08.06.2015
 */
@ApplicationScoped
public class BeanProducer {

    @Resource private ManagedScheduledExecutorService scheduler;
    @Inject private AppConfiguration appConfiguration;

    private PooledUraConnection pooledUraConnection;
    private UraClient uraClient;

    @PostConstruct
    private void init() {
        AppConfiguration.RegioIT config = appConfiguration.getRegioIT();
        pooledUraConnection = new PooledUraConnection(config.getUraBaseUrl(), config.getUraBasePort());
        uraClient = new UraClient(pooledUraConnection, scheduler);
    }

    @PreDestroy
    private void preDestroy() {
        pooledUraConnection.close();
    }

    public UraClient getAseagURAClient() {
        return uraClient;
    }

}
