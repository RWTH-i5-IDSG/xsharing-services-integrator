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

import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.05.2015
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class InOutContextStore extends AbstractItemStore<InOutContext> {

    @PostConstruct
    public void init() {
        log.debug("Ready");
    }
}
