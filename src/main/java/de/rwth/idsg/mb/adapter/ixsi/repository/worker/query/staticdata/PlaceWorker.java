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
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.postgresql.geometric.PGpoint;
import xjc.schema.ixsi.CoordType;
import xjc.schema.ixsi.PlaceType;
import xjc.schema.ixsi.ProviderPlaceIDType;

import java.util.List;

import static jooq.db.ixsi.tables.Place.PLACE;

/**
 * Created by max on 17/12/14.
 */
@RequiredArgsConstructor
@Slf4j
public class PlaceWorker implements ParentWorker<PlaceType> {
    private final DSLContext ctx;
    private final List<ProviderPlaceIDType> dbList;

    private BatchBindStep insertBatch;
    private BatchBindStep updateBatch;

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(PLACE,
                        PLACE.STATUS,
                        PLACE.PLACE_ID,
                        PLACE.PROVIDER_ID,
                        PLACE.GLOBAL_ID,
                        PLACE.CAPACITY,
                        PLACE.GPS_POSITION,
                        PLACE.ON_PREMISES_TIME_IN_SECONDS)
                   .values(null, "", "", null, null, null, null)
        );

        updateBatch = ctx.batch(
                ctx.update(PLACE)
                   .set(PLACE.STATUS, (Status) null)
                   .set(PLACE.GLOBAL_ID, "")
                   .set(PLACE.CAPACITY, (Integer) null)
                   .set(PLACE.GPS_POSITION, (PGpoint) null)
                   .set(PLACE.ON_PREMISES_TIME_IN_SECONDS, (Integer) null)
                   .where(PLACE.PLACE_ID.equal(""))
                   .and(PLACE.PROVIDER_ID.equal(""))
        );

    }

    @Override
    public void bind(PlaceType item) {
        String placeId = item.getID();
        String providerId = item.getProviderID();

        CoordType c = item.getGeoPosition().getCoord();
        PGpoint point = BasicUtils.toPoint(c);

        Integer capacity = item.getCapacity();

        ProviderPlaceIDType p = new ProviderPlaceIDType()
                .withPlaceID(placeId)
                .withProviderID(providerId);

        // according to IXSI schema, the field defaults to 0.
        Integer onPremises = BasicUtils.toSeconds(item.getOnPremisesTime());
        if (onPremises == null) {
            onPremises = 0;
        }

        if (dbList.contains(p)) {
            updateBatch.bind(
                    Status.ACTIVE,
                    item.getGlobalID(),
                    capacity,
                    point,
                    onPremises,
                    placeId,
                    providerId
            );
        } else {
            insertBatch.bind(
                    Status.ACTIVE,
                    placeId,
                    providerId,
                    item.getGlobalID(),
                    capacity,
                    point,
                    onPremises
            );
        }
    }

    @Override
    public void execute() {
        insertBatch.execute();
        updateBatch.execute();
    }
}
