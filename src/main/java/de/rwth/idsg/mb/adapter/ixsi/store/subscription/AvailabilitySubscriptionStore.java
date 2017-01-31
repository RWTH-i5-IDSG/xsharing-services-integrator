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

import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.BookingTargetIDType;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.02.2015
 */
@Slf4j
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class AvailabilitySubscriptionStore extends AbstractSubscriptionStore<BookingTargetIDType> {

    @PostConstruct
    public void init() {
        log.debug("Ready");
    }
}
