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
package de.rwth.idsg.mb.regioIT.repository;

import de.rwth.idsg.mb.regioIT.AuthCredentials;
import de.rwth.idsg.mb.regioIT.client.rest.params.AuthResponse;
import jooq.db.regio_it.tables.records.AuthRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static jooq.db.regio_it.tables.Auth.AUTH;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 08.05.2015
 */
@Stateless
public class AuthRepositoryImpl implements AuthRepository {

    @EJB private AuthCredentials authCredentials;
    @Inject private DSLContext ctx;

    @Override
    public AuthRecord getAuthRecord() {
        return ctx.selectFrom(AUTH)
                  .fetchOne();
    }

    @Override
    public void saveResponse(AuthResponse response) {
        List<String> roleList = response.getRoles();

        String[] roleArray = new String[roleList.size()];   // init
        roleList.toArray(roleArray);    // fill the array

        ctx.update(AUTH)
           .set(AUTH.TOKEN, response.getToken())
           .set(AUTH.ROLES, roleArray)
           .set(AUTH.TOKEN_UPDATE_TIMESTAMP, DSL.currentTimestamp())
           .where(AUTH.USERNAME.equal(authCredentials.getUsername()))
           .execute();
    }
}
