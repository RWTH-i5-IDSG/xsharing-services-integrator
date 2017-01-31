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
package de.rwth.idsg.mb.adapter.ixsi;

import de.rwth.idsg.mb.adapter.ixsi.service.IxsiService;
import de.rwth.idsg.mb.adapter.ixsi.service.QueryService;
import de.rwth.idsg.mb.adapter.ixsi.service.QueryServiceImpl;
import de.rwth.idsg.mb.adapter.ixsi.service.SubscriptionService;
import de.rwth.idsg.mb.adapter.ixsi.service.SubscriptionServiceImpl;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.10.2014
 */
@ApplicationScoped
public class BeanProducer {

    @EJB private IxsiService ixsiService;

    private static QueryService queryService;
    private static SubscriptionService subscriptionService;

    @PostConstruct
    private void init() {
        queryService = new QueryServiceImpl(ixsiService);
        subscriptionService = new SubscriptionServiceImpl(ixsiService);
    }

    public QueryService getQueryService() {
        return queryService;
    }

    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

}
