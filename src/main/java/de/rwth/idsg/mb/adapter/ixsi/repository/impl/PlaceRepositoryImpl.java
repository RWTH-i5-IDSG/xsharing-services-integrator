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
package de.rwth.idsg.mb.adapter.ixsi.repository.impl;

import de.rwth.idsg.mb.adapter.ixsi.repository.PlaceRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceAddressWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceAttributeWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceDescriptionWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceNameWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceStopLinkWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceWorker;
import de.rwth.idsg.mb.utils.ItemIdComparator;
import jooq.db.ixsi.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import xjc.schema.ixsi.AddressType;
import xjc.schema.ixsi.PlaceAvailabilityType;
import xjc.schema.ixsi.PlaceType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.StopLinkType;
import xjc.schema.ixsi.TextType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.Place.PLACE;
import static jooq.db.ixsi.tables.Provider.PROVIDER;


/**
 * Created by max on 08/12/14.
 */
@Slf4j
@Stateless
public class PlaceRepositoryImpl implements PlaceRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<ProviderPlaceIDType> getIds(int partnerId) {
        return internalGet(partnerId, false);
    }

    @Override
    public List<ProviderPlaceIDType> getActiveIds(int partnerId) {
        return internalGet(partnerId, true);
    }

    @SuppressWarnings("unchecked")
    private List<ProviderPlaceIDType> internalGet(int partnerId, boolean isActive) {
        SelectQuery sq = ctx.selectQuery();
        sq.addFrom(PLACE);
        sq.addSelect(
                PLACE.PLACE_ID,
                PLACE.PROVIDER_ID
        );

        sq.addJoin(
                PROVIDER,
                PROVIDER.PROVIDER_ID.eq(PLACE.PROVIDER_ID),
                PROVIDER.PARTNER_ID.eq(partnerId)
        );

        if (isActive) {
            sq.addConditions(PLACE.STATUS.eq(Status.ACTIVE));
        }

        return sq.fetch()
                 .map(new RecordMapper<Record2<String, String>, ProviderPlaceIDType>() {
                     @Override
                     public ProviderPlaceIDType map(Record2<String, String> record) {
                         return new ProviderPlaceIDType()
                                 .withPlaceID(record.value1())
                                 .withProviderID(record.value2());
                     }
                 });

    }

    @Override
    @Transactional
    public void upsertPlaceList(int partnerId, final List<PlaceType> placeList) {
        log.debug("Size of the arrived place list: {}", placeList.size());

        List<ProviderPlaceIDType> dbList = getIds(partnerId);

        // -------------------------------------------------------------------------
        // Prepare statements
        // -------------------------------------------------------------------------

        PlaceWorker placeWorker = new PlaceWorker(ctx, dbList);
        placeWorker.prepare();

        PlaceAddressWorker placeAddressWorker = new PlaceAddressWorker(ctx);
        placeAddressWorker.prepare();

        PlaceAttributeWorker placeAttributeWorker = new PlaceAttributeWorker(ctx);
        placeAttributeWorker.prepare();

        PlaceDescriptionWorker placeDescriptionWorker = new PlaceDescriptionWorker(ctx);
        placeDescriptionWorker.prepare();

        PlaceNameWorker placeNameWorker = new PlaceNameWorker(ctx);
        placeNameWorker.prepare();

        PlaceStopLinkWorker placeStopLinkWorker = new PlaceStopLinkWorker(ctx);
        placeStopLinkWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (PlaceType place : placeList) {
            placeWorker.bind(place);

            String placeId = place.getID();

            if (place.isSetGeoPosition() && place.getGeoPosition().isSetAddress()) {
                AddressType add = place.getGeoPosition().getAddress();
                placeAddressWorker.bind(placeId, add);
            }

            if (place.isSetAttributeID()) {
                placeAttributeWorker.bindDelete(placeId);
                for (String att : place.getAttributeID()) {
                    placeAttributeWorker.bindInsert(placeId, att);
                }
            }

            if (place.isSetDescription()) {
                placeDescriptionWorker.bindDelete(placeId);
                for (TextType desc : place.getDescription()) {
                    placeDescriptionWorker.bindInsert(placeId, desc);
                }
            }

            if (place.isSetName()) {
                placeNameWorker.bindDelete(placeId);
                for (TextType name : place.getName()) {
                    placeNameWorker.bindInsert(placeId, name);
                }
            }

            if (place.isSetStopLink()) {
                placeStopLinkWorker.bindDelete(placeId);
                for (StopLinkType st : place.getStopLink()) {
                    placeStopLinkWorker.bindInsert(placeId, st);
                }
            }
        }

        // -------------------------------------------------------------------------
        // Execute - the order is important
        // -------------------------------------------------------------------------

        placeWorker.execute();
        placeNameWorker.execute();
        placeAddressWorker.execute();

        placeAttributeWorker.execute();
        placeDescriptionWorker.execute();
        placeStopLinkWorker.execute();

        // -------------------------------------------------------------------------
        // Set others to inactive
        // -------------------------------------------------------------------------

        List<ProviderPlaceIDType> newList = new ArrayList<>(placeList.size());
        for (PlaceType p : placeList) {
            newList.add(new ProviderPlaceIDType()
                    .withPlaceID(p.getID())
                    .withProviderID(p.getProviderID()));
        }

        ItemIdComparator<ProviderPlaceIDType> comparator = new ItemIdComparator<>();
        comparator.setDatabaseList(dbList);
        comparator.setNewList(newList);

        updateStatus(partnerId, comparator);
    }

    @Override
    @Transactional
    public void updatePlaceAvailabilityList(final List<PlaceAvailabilityType> placeAvailabilityTypes) {
        BatchBindStep updateBatch = ctx.batch(
                ctx.update(PLACE)
                   .set(PLACE.AVAILABLE_CAPACITY, (Integer) null)
                   .where(PLACE.PLACE_ID.equal(""))
                        .and(PLACE.PROVIDER_ID.equal(""))
        );

        for (PlaceAvailabilityType pa : placeAvailabilityTypes) {
            updateBatch.bind(
                    pa.getAvailability(),
                    pa.getID().getPlaceID(),
                    pa.getID().getProviderID()
            );
        }

        updateBatch.execute();
    }

    private void updateStatus(int partnerId, ItemIdComparator<ProviderPlaceIDType> comparator) {
        List<ProviderPlaceIDType> toDelete = comparator.getForDelete();

        BatchBindStep batchBindStep = ctx.batch(
                ctx.update(PLACE)
                   .set(PLACE.STATUS, (Status) null)
                   .where(PLACE.PLACE_ID.eq(""))
                   .and(PLACE.PROVIDER_ID.eq(""))
        );

        for (ProviderPlaceIDType p : toDelete) {
            batchBindStep.bind(Status.INACTIVE, p.getPlaceID(), p.getProviderID());
        }

        batchBindStep.execute();
    }
}
