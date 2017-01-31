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
package de.rwth.idsg.mb.adapter.ixsi.repository.worker.subscription.complete;

import org.jooq.DSLContext;
import xjc.schema.ixsi.BookingTargetIDType;

import java.util.HashSet;
import java.util.Set;

import static jooq.db.ixsi.tables.BookingTargetStatusInavailability.BOOKING_TARGET_STATUS_INAVAILABILITY;

/**
 * In the super class, we delete only the entries that are subject to insert (to prevent collisions/conflicts). That
 * also meant, that we retain inavailabilities that are not part of the arrived CompleteAvailability. It does not break
 * things, but is confusing. This is why we go and delete all the entries related to the delivered provider ids.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 14.04.2016
 */
public class BookingTargetStatusInavailabilityReplaceWorker extends BookingTargetStatusInavailabilityWorker {

    private final Set<String> providersToDelete;

    public BookingTargetStatusInavailabilityReplaceWorker(DSLContext ctx) {
        super(ctx);
        providersToDelete = new HashSet<>();
    }

    @Override
    public void bindDelete(BookingTargetIDType parentId) {
        providersToDelete.add(parentId.getProviderID());
    }

    @Override
    public void execute() {
        getCtx().delete(BOOKING_TARGET_STATUS_INAVAILABILITY)
                .where(BOOKING_TARGET_STATUS_INAVAILABILITY.PROVIDER_ID.in(providersToDelete))
                .execute();

        getInsertBatch().execute();
    }
}
