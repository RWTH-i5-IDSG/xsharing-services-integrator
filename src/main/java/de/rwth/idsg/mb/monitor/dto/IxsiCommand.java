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

import java.util.Arrays;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.06.2015
 */
public enum IxsiCommand {

    CONNECT("Connect"),
    CANCEL_RECONNECT("Cancel Reconnect"),
    DESTROY("Destroy"),

    STATIC_GET("Get Static"),
    STATIC_UPDATE("Update Static"),

    SUB("Subscribe"),
    SUB_STATUS("Get Subscription Status"),
    SUB_COMPLETE("Complete Update of Subscriptions");

    private final String value;

    IxsiCommand(String v) {
        value = v;
    }

    public static IxsiCommand fromValue(String v) {
        for (IxsiCommand c: IxsiCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static List<String> getConnectionValues() {
        return Arrays.asList(
                CONNECT.value,
                CANCEL_RECONNECT.value,
                DESTROY.value
        );
    }

    public static List<String> getStaticDataValues() {
        return Arrays.asList(
                STATIC_GET.value,
                STATIC_UPDATE.value
        );
    }

    public static List<String> getSubscriptionValues() {
        return Arrays.asList(
                SUB.value,
                SUB_STATUS.value,
                SUB_COMPLETE.value
        );
    }
}
