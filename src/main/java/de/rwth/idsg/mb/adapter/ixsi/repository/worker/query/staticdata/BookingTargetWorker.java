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
package de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata;

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ParentWorker;
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.enums.Status;
import lombok.RequiredArgsConstructor;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.BookingTargetType;

import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getEngineTypeOrNull;
import static jooq.db.ixsi.tables.BookingTarget.BOOKING_TARGET;

/**
 * Created by swam on 17/12/14.
 */
@RequiredArgsConstructor
public class BookingTargetWorker implements ParentWorker<BookingTargetType> {
    private final DSLContext ctx;
    private final List<BookingTargetIDType> dbList;

    private BatchBindStep insertBatch;
    private BatchBindStep updateBatch;

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(BOOKING_TARGET,
                        BOOKING_TARGET.STATUS,
                        BOOKING_TARGET.BOOKING_TARGET_ID,
                        BOOKING_TARGET.PROVIDER_ID,
                        BOOKING_TARGET.GLOBAL_ID,
                        BOOKING_TARGET.CLASS,
                        BOOKING_TARGET.BOOKING_HORIZON_IN_SECONDS,
                        BOOKING_TARGET.BOOKING_GRID_IN_MINUTES,
                        BOOKING_TARGET.OPENING_TIME_IN_SECONDS,
                        BOOKING_TARGET.ENGINE,
                        BOOKING_TARGET.CO2_FACTOR,
                        BOOKING_TARGET.MAX_DISTANCE_IN_METERS,
                        BOOKING_TARGET.EXCLUSIVE_TO_FLOATING_AREA_ID,
                        BOOKING_TARGET.EXCLUSIVE_TO_PLACE_ID,
                        BOOKING_TARGET.EXCLUSIVE_TO_PLACEGROUP_ID)
                        .values(null, "", "", null, null, null, null, null, null, null, null, null, null, null)
        );

        updateBatch = ctx.batch(
                ctx.update(BOOKING_TARGET)
                .set(BOOKING_TARGET.STATUS, (Status) null)
                .set(BOOKING_TARGET.GLOBAL_ID, "")
                .set(BOOKING_TARGET.CLASS, "")
                .set(BOOKING_TARGET.BOOKING_HORIZON_IN_SECONDS, (Integer) null)
                .set(BOOKING_TARGET.BOOKING_GRID_IN_MINUTES, (Integer) null)
                .set(BOOKING_TARGET.OPENING_TIME_IN_SECONDS, (Integer) null)
                .set(BOOKING_TARGET.ENGINE, (String) null)
                .set(BOOKING_TARGET.CO2_FACTOR, (Integer) null)
                .set(BOOKING_TARGET.MAX_DISTANCE_IN_METERS, (Integer) null)
                .set(BOOKING_TARGET.EXCLUSIVE_TO_FLOATING_AREA_ID, "")
                .set(BOOKING_TARGET.EXCLUSIVE_TO_PLACE_ID, "")
                .set(BOOKING_TARGET.EXCLUSIVE_TO_PLACEGROUP_ID, "")
                .where(BOOKING_TARGET.BOOKING_TARGET_ID.equal(""))
                .and(BOOKING_TARGET.PROVIDER_ID.equal(""))
        );
    }

    @Override
    public void bind(BookingTargetType b) {
        BookingTargetIDType bookingTargetId = b.getID();

        String classType = b.getClazz().value();
        String engineType = getEngineTypeOrNull(b);

        if (dbList.contains(bookingTargetId)) {
            updateBatch.bind(
                    Status.ACTIVE,
                    b.getGlobalID(),
                    classType,
                    BasicUtils.toSeconds(b.getBookingHorizon()),
                    b.getBookingGrid(),
                    BasicUtils.toSeconds(b.getOpeningTime()),
                    engineType,
                    b.getCO2Factor(),
                    b.getMaxDistance(),
                    b.getAreaID(),
                    b.getPlaceID(),
                    b.getPlaceGroupID(),
                    bookingTargetId.getBookeeID(),
                    bookingTargetId.getProviderID()
            );
        } else {
            insertBatch.bind(
                    Status.ACTIVE,
                    bookingTargetId.getBookeeID(),
                    bookingTargetId.getProviderID(),
                    b.getGlobalID(),
                    classType,
                    BasicUtils.toSeconds(b.getBookingHorizon()),
                    b.getBookingGrid(),
                    BasicUtils.toSeconds(b.getOpeningTime()),
                    engineType,
                    b.getCO2Factor(),
                    b.getMaxDistance(),
                    b.getAreaID(),
                    b.getPlaceID(),
                    b.getPlaceGroupID()
            );
        }
    }

    @Override
    public void execute() {
        insertBatch.execute();
        updateBatch.execute();
    }

}
