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
package de.rwth.idsg.mb.controller.repository;

import de.rwth.idsg.mb.controller.dto.ProviderDTO;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Result;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static jooq.db.ixsi.tables.BookingCreate.BOOKING_CREATE;
import static jooq.db.ixsi.tables.Consumption.CONSUMPTION;
import static jooq.db.ixsi.tables.Provider.PROVIDER;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 30.06.2015
 */
@Stateless
public class IxsiProviderRepositoryImpl implements IxsiProviderRepository {

    @Inject private DSLContext ctx;

    /**
     * Key:   Provider Name
     * Value: Provider Id, Partner Id
     */
    @Override
    public Map<String, ProviderDTO> getProviders() {
        Result<Record3<String, String, Integer>> result = ctx.select(PROVIDER.NAME,
                                                                     PROVIDER.PROVIDER_ID,
                                                                     PROVIDER.PARTNER_ID)
                                                             .from(PROVIDER)
                                                             .fetch();

        HashMap<String, ProviderDTO> map = new HashMap<>();
        for (Record3<String, String, Integer> r : result) {
            map.put(r.value1(), new ProviderDTO(r.value2(), r.value3()));
        }
        return map;
    }

    @Override
    public String getProviderName(String bookingId) {
        return ctx.select(PROVIDER.NAME)
                  .from(PROVIDER)
                  .join(BOOKING_CREATE)
                  .on(BOOKING_CREATE.PROVIDER_ID.eq(PROVIDER.PROVIDER_ID))
                  .where(BOOKING_CREATE.BOOKING_ID.eq(bookingId))
                  .fetchOne()
                  .value1();
    }

    /**
     * Key:   Booking Id
     * Value: Provider Name
     */
    @Override
    public Map<String, String> getBookingProviderMap(Collection<String> bookingIdCollection) {
        Result<Record2<String, String>> result =
                ctx.selectDistinct(CONSUMPTION.BOOKING_ID, PROVIDER.NAME)
                   .from(PROVIDER)
                   .join(CONSUMPTION)
                   .on(CONSUMPTION.PARTNER_ID.eq(PROVIDER.PARTNER_ID))
                   .where(CONSUMPTION.BOOKING_ID.in(bookingIdCollection))
                   .fetch();

        HashMap<String, String> map = new HashMap<>();
        for (Record2<String, String> r : result) {
            map.put(r.value1(), r.value2());
        }
        return map;
    }
}
