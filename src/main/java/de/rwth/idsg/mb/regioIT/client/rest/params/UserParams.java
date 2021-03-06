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
package de.rwth.idsg.mb.regioIT.client.rest.params;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.05.2015
 */
@ToString
@Getter
@Setter
public class UserParams {
    private String userId;
    private UserState state;

    private String password;    // optional
    private String pin;         // optional

    // -------------------------------------------------------------------------

    public enum UserState {
        OPERATIVE("operative"),
        INOPERATIVE("inoperative"),
        DELETED("deleted");

        private final String value;

        UserState(String v) {
            value = v;
        }

        @JsonValue // serialize
        public String value() {
            return value;
        }

        @JsonCreator // deserialize
        public static UserState fromValue(String v) {
            for (UserState c : UserState.values()) {
                if (c.value.equalsIgnoreCase(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }

    }
}
