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
import xjc.schema.ixsi.AddressType;

import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.PlaceAddress.PLACE_ADDRESS;

/**
 * Created by max on 17/12/14.
 */
@Slf4j
@RequiredArgsConstructor
public class PlaceAddressWorker implements ChildWorker<String, AddressType> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(PLACE_ADDRESS,
                        PLACE_ADDRESS.COUNTRY,
                        PLACE_ADDRESS.CITY,
                        PLACE_ADDRESS.STREET_HOUSE_NR,
                        PLACE_ADDRESS.POSTAL_CODE,
                        PLACE_ADDRESS.PLACE_ID)
                        .values("", null, null, null, null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, AddressType item) {
        insertBatch.bind(
                item.getCountry(),
                item.getCity(),
                item.getStreetHouseNr(),
                item.getPostalCode(),
                parentId
        );
    }

    @Override
    public void bind(String parentId, AddressType item) {
        bindDelete(parentId);
        bindInsert(parentId, item);
    }

    @Override
    public void execute() {
        if (itemIdList.isEmpty()) {
            log.debug("Nothing to write in table {}", PLACE_ADDRESS.getName());
        } else {
            ctx.delete(PLACE_ADDRESS)
               .where(PLACE_ADDRESS.PLACE_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }

}
