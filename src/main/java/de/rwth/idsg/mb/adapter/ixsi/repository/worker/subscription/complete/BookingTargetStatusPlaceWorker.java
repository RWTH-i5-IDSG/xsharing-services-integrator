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

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ChildWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.BookingTargetIDType;

import static jooq.db.ixsi.tables.BookingTargetStatusPlace.BOOKING_TARGET_STATUS_PLACE;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 29.12.2014
 */
@Slf4j
@RequiredArgsConstructor
public class BookingTargetStatusPlaceWorker implements ChildWorker<BookingTargetIDType, String> {
    private final DSLContext ctx;
    private BatchBindStep deleteBatch;
    private BatchBindStep insertBatch;

    @Override
    public void prepare() {
        deleteBatch = ctx.batch(
                ctx.delete(BOOKING_TARGET_STATUS_PLACE)
                   .where(BOOKING_TARGET_STATUS_PLACE.PROVIDER_ID.equal(""))
        );

        insertBatch = ctx.batch(
                ctx.insertInto(BOOKING_TARGET_STATUS_PLACE,
                        BOOKING_TARGET_STATUS_PLACE.BOOKING_TARGET_ID,
                        BOOKING_TARGET_STATUS_PLACE.PROVIDER_ID,
                        BOOKING_TARGET_STATUS_PLACE.PLACE_ID)
                   .values("", "", null)
        );
    }

    @Override
    public void bindDelete(BookingTargetIDType parentId) {
        deleteBatch.bind(
                parentId.getProviderID()
        );
    }

    @Override
    public void bindInsert(BookingTargetIDType parentId, String placeId) {
        insertBatch.bind(
                parentId.getBookeeID(),
                parentId.getProviderID(),
                placeId
        );
    }

    @Override
    public void bind(BookingTargetIDType parentId, String placeId) {
        bindDelete(parentId);
        bindInsert(parentId, placeId);
    }

    @Override
    public void execute() {
        deleteBatch.execute();
        insertBatch.execute();
    }
}
