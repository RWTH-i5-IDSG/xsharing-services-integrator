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
package de.rwth.idsg.mb.controller.service;

import de.ivu.realtime.modules.ura.client.UraClient;
import de.ivu.realtime.modules.ura.client.UraResponse;
import de.ivu.realtime.modules.ura.data.request.Ura2Request;
import de.ivu.realtime.modules.ura.data.request.UraRequestParseException;
import de.ivu.realtime.modules.ura.data.response.UraEntity;
import de.rwth.idsg.mb.controller.BeanProducer;
import de.rwth.idsg.mb.controller.parse.UraStringParser;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 08.06.2015
 */
@Slf4j
@Stateless
public class AseagService {

    @Inject private BeanProducer beanProducer;

    private UraClient uraClient;

    @PostConstruct
    public void init() {
        uraClient = beanProducer.getAseagURAClient();
    }

    @Nullable
    public UraEntity get(UraStringParser uraStringParser) throws UraRequestParseException {

        String str = uraStringParser.cleanUpAseagParams();

        // 1. Rebuild
        //
        Ura2Request request = new Ura2Request.Builder()
                .withQueryParameters(str)
                .build();

        // 2. Call the server
        //
        UraResponse res;
        try {
            res = uraClient.request(request)
                           .get();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception happened", e);
            return null;
        }

        // 3. Get the response
        //
        if (res instanceof UraResponse.Success) {
            return ((UraResponse.Success) res).getEntity();

        } else if (res instanceof UraResponse.Failure) {
            log.error("Failure while fetching Bus-Stations with URAClient", ((UraResponse.Failure) res).getReason());
            return null;

        } else {
            return null;
        }
    }
}
