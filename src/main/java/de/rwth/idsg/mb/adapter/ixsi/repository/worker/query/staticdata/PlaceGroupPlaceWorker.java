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

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ChildWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.ProbabilityPlaceIDType;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getProbabilityOrNull;
import static jooq.db.ixsi.tables.PlacegroupPlace.PLACEGROUP_PLACE;

/**
 * Created by max on 17/12/14.
 */
@Slf4j
@RequiredArgsConstructor
public class PlaceGroupPlaceWorker implements ChildWorker<String, ProbabilityPlaceIDType> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(PLACEGROUP_PLACE,
                        PLACEGROUP_PLACE.PLACEGROUP_ID,
                        PLACEGROUP_PLACE.PLACE_ID,
                        PLACEGROUP_PLACE.PROBABILITY)
                   .values("", null, null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, ProbabilityPlaceIDType item) {
        Integer probability = getProbabilityOrNull(item.getProbability());

        insertBatch.bind(
                parentId,
                item.getID(),
                probability
        );
    }

    /**
     * @deprecated  Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(String parentId, ProbabilityPlaceIDType item) {

    }

    @Override
    public void execute() {
        if (itemIdList.isEmpty()) {
            log.debug("Nothing to write in table {}", PLACEGROUP_PLACE.getName());
        } else {
            ctx.delete(PLACEGROUP_PLACE)
               .where(PLACEGROUP_PLACE.PLACEGROUP_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }

}
