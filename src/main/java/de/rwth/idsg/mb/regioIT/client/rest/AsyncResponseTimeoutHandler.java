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
package de.rwth.idsg.mb.regioIT.client.rest;

import de.rwth.idsg.mb.adapter.ixsi.store.UserInOutContextStore;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.06.2015
 */
@Slf4j
@Stateless
public class AsyncResponseTimeoutHandler implements TimeoutHandler {

    @EJB private UserInOutContextStore userInOutContextStore;

    @Override
    public void handleTimeout(AsyncResponse asyncResponse) {
        boolean success = asyncResponse.resume(
                Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("The remote IXSI system did not respond within the time-out period.")
                        .build()
        );

        if (success) {
            log.debug("The asyncResponse timed out. Removing it from the store");
            userInOutContextStore.removeItem(asyncResponse);
        }
    }
}
