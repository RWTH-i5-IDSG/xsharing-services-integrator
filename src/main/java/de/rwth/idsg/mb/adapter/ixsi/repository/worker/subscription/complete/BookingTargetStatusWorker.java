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

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ParentWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.postgresql.geometric.PGpoint;
import xjc.schema.ixsi.BookingTargetAvailabilityType;
import xjc.schema.ixsi.BookingTargetIDType;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getPointOrNull;
import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getSocOrNull;
import static jooq.db.ixsi.tables.BookingTargetStatus.BOOKING_TARGET_STATUS;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 17.02.2015
 */
@Slf4j
@RequiredArgsConstructor
public class BookingTargetStatusWorker implements ParentWorker<BookingTargetAvailabilityType> {
    private final DSLContext ctx;
    private BatchBindStep deleteBatch;
    private BatchBindStep insertBatch;

    @Override
    public void prepare() {
        deleteBatch = ctx.batch(
                ctx.delete(BOOKING_TARGET_STATUS)
                   .where(BOOKING_TARGET_STATUS.BOOKING_TARGET_ID.equal(""))
                   .and(BOOKING_TARGET_STATUS.PROVIDER_ID.equal(""))
        );

        insertBatch = ctx.batch(
                ctx.insertInto(BOOKING_TARGET_STATUS,
                        BOOKING_TARGET_STATUS.BOOKING_TARGET_ID,
                        BOOKING_TARGET_STATUS.PROVIDER_ID,
                        BOOKING_TARGET_STATUS.GPS_POSITION,
                        BOOKING_TARGET_STATUS.CURRENT_CHARGE,
                        BOOKING_TARGET_STATUS.CURRENT_DRIVING_RANGE_IN_METERS)
                   .values("", "", null, null, null)
        );
    }

    private void bindDelete(BookingTargetIDType id) {
        deleteBatch.bind(
                id.getBookeeID(),
                id.getProviderID()
        );
    }

    private void bindInsert(BookingTargetIDType id, PGpoint point, Integer soc, Integer range) {
        insertBatch.bind(
                id.getBookeeID(),
                id.getProviderID(),
                point,
                soc,
                range
        );
    }

    @Override
    public void bind(BookingTargetAvailabilityType item) {
        PGpoint point = getPointOrNull(item);
        Integer soc = getSocOrNull(item);
        Integer range = item.getCurrentDrivingRange();

        // if all three values are empty, we can skip this item.
        // otherwise we will have an empty row (only ids - which is useless) in table
        //
        if (point == null & soc == null & range == null) {
            return;
        }

        bindDelete(item.getID());
        bindInsert(item.getID(), point, soc, range);
    }

    @Override
    public void execute() {
        deleteBatch.execute();
        insertBatch.execute();
    }
}
