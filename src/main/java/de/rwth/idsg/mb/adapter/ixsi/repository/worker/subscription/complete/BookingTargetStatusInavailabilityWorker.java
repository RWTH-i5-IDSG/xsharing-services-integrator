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
import de.rwth.idsg.mb.pg.range.IncExcTsRange;
import de.rwth.idsg.mb.utils.BasicUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.TimePeriodType;

import static jooq.db.ixsi.tables.BookingTargetStatusInavailability.BOOKING_TARGET_STATUS_INAVAILABILITY;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 17.02.2015
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class BookingTargetStatusInavailabilityWorker implements ChildWorker<BookingTargetIDType, TimePeriodType> {
    private final DSLContext ctx;
    private BatchBindStep deleteBatch;
    private BatchBindStep insertBatch;

    @Override
    public void prepare() {
        deleteBatch = ctx.batch(
                ctx.delete(BOOKING_TARGET_STATUS_INAVAILABILITY)
                   .where(BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID.equal(""))
                   .and(BOOKING_TARGET_STATUS_INAVAILABILITY.PROVIDER_ID.equal(""))
        );

        insertBatch = ctx.batch(
                ctx.insertInto(BOOKING_TARGET_STATUS_INAVAILABILITY,
                        BOOKING_TARGET_STATUS_INAVAILABILITY.BOOKING_TARGET_ID,
                        BOOKING_TARGET_STATUS_INAVAILABILITY.PROVIDER_ID,
                        BOOKING_TARGET_STATUS_INAVAILABILITY.INAVAILABILITY)
                   .values("", "", new IncExcTsRange())
        );
    }

    @Override
    public void bindDelete(BookingTargetIDType parentId) {
        deleteBatch.bind(
                parentId.getBookeeID(),
                parentId.getProviderID()
        );
    }

    @Override
    public void bindInsert(BookingTargetIDType parentId, TimePeriodType item) {
        insertBatch.bind(
                parentId.getBookeeID(),
                parentId.getProviderID(),
                BasicUtils.toTsRange(item)
        );
    }

    /**
     * @deprecated  Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(BookingTargetIDType parentId, TimePeriodType item) {
        // no-op
    }

    @Override
    public void execute() {
        deleteBatch.execute();
        insertBatch.execute();
    }
}
