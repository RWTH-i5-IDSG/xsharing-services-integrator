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

import de.rwth.idsg.mb.adapter.ixsi.repository.ProviderRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.ProviderAttributeWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.ProviderWorker;
import de.rwth.idsg.mb.utils.ItemIdComparator;
import jooq.db.ixsi.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import xjc.schema.ixsi.ProviderType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.Provider.PROVIDER;

/**
 * Created by max on 08/12/14.
 */
@Slf4j
@Stateless
public class ProviderRepositoryImpl implements ProviderRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<String> getIds(int partnerId) {
        return ctx.select(PROVIDER.PROVIDER_ID)
                  .from(PROVIDER)
                  .where(PROVIDER.PARTNER_ID.eq(partnerId))
                  .fetch(PROVIDER.PROVIDER_ID);
    }

    @Override
    @Transactional
    public void upsertProviderList(int partnerId, final List<ProviderType> providers) {
        log.debug("Size of the arrived providers list: {}", providers.size());

        List<String> dbList = getIds(partnerId);

        // -------------------------------------------------------------------------
        // Prepare environment
        // -------------------------------------------------------------------------

        ProviderWorker providerWorker = new ProviderWorker(ctx, dbList, partnerId);
        providerWorker.prepare();

        ProviderAttributeWorker providerAttributeWorker = new ProviderAttributeWorker(ctx);
        providerAttributeWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (ProviderType p : providers) {
            providerWorker.bind(p);

            if (p.isSetAttributeID()) {
                String providerId = p.getID();
                providerAttributeWorker.bindDelete(providerId);

                for (String s : p.getAttributeID()) {
                    providerAttributeWorker.bindInsert(providerId, s);
                }
            }
        }

        // -------------------------------------------------------------------------
        // Execute
        // -------------------------------------------------------------------------

        providerWorker.execute();
        providerAttributeWorker.execute();

        // -------------------------------------------------------------------------
        // Set others to inactive
        // -------------------------------------------------------------------------

        List<String> stringList = new ArrayList<>(providers.size());
        for (ProviderType pt : providers) {
            stringList.add(pt.getID());
        }

        ItemIdComparator<String> comparator = new ItemIdComparator<>();
        comparator.setDatabaseList(dbList);
        comparator.setNewList(stringList);

        updateStatus(partnerId, comparator);
    }

    private void updateStatus(int partnerId, ItemIdComparator<String> comparator) {
        List<String> toDelete = comparator.getForDelete();

        ctx.update(PROVIDER)
           .set(PROVIDER.STATUS, Status.INACTIVE)
           .where(PROVIDER.PARTNER_ID.eq(partnerId))
           .and(PROVIDER.PROVIDER_ID.in(toDelete))
           .execute();
    }
}
