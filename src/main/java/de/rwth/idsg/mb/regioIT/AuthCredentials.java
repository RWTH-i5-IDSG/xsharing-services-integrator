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
package de.rwth.idsg.mb.regioIT;

import de.rwth.idsg.mb.regioIT.client.rest.api.AuthClient;
import de.rwth.idsg.mb.regioIT.client.rest.params.AuthParams;
import de.rwth.idsg.mb.regioIT.client.rest.params.AuthResponse;
import de.rwth.idsg.mb.regioIT.repository.AuthRepository;
import jooq.db.regio_it.tables.records.AuthRecord;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 08.05.2015
 */
@Slf4j
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class AuthCredentials {

    @Inject private AuthRepository authRepository;
    @Inject private AuthClient authClient;

    @Getter private String username;
    private String token;

    @PostConstruct
    private void init() {
        AuthRecord authRecord = authRepository.getAuthRecord();
        username = authRecord.getUsername();
        token = authRecord.getToken();
    }

    public synchronized String getToken() {
        if (token == null) {
            login();
        }
        return token;
    }

    /**
     * Save the password in DB and then POJO.
     * The order is quite important: If DB update fails, we should not update the POJO in memory.
     */
    private synchronized void update(AuthResponse response) {
        authRepository.saveResponse(response);
        token = response.getToken();
    }

    private void login() {
        log.debug("Initiating a new login");

        AuthRecord authRecord = authRepository.getAuthRecord();

        AuthParams params = AuthParams.builder()
                .username(authRecord.getUsername())
                .password(authRecord.getPassword())
                .tenant(authRecord.getTenant())
                .build();

        AuthResponse response = authClient.login(params);
        update(response);
    }

}
