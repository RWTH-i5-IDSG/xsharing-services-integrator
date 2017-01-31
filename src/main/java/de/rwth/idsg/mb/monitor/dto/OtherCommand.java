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
package de.rwth.idsg.mb.monitor.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.06.2015
 */
public enum OtherCommand {

    REFRESH_PARTNERS("Refresh Partners"),
    REFRESH_PROVIDER_LOOKUP("Refresh ProviderLookupTable"),
    TOGGLE_PUSH("Enable/disable RegioITPushService");

    private final String value;

    OtherCommand(String v) {
        value = v;
    }

    public static OtherCommand fromValue(String v) {
        for (OtherCommand c: OtherCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static List<String> getValues() {
        List<String> list = new ArrayList<>(OtherCommand.values().length);
        for (OtherCommand c: OtherCommand.values()) {
            list.add(c.value);
        }
        return list;
    }
}
