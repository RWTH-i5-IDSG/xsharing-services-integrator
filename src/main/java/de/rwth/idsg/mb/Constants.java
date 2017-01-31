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
package de.rwth.idsg.mb;

import jooq.db.ixsi.enums.Language;

import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 31.01.2017
 */
public final class Constants {

    private Constants() { }

    public static final String MAJOR_CUSTOMER = "ASEAG";

    public static final String SHUTDOWN_PHRASE = "Service/application is shutting down";

    public static final class Ixsi {

        private Ixsi() { }

        public static final String XML_SCHEMA_FILE = "IXSI-with-enums.xsd";

        public static final Language DEFAULT_LANGUAGE = Language.DE;

        // Setting the max text size of IXSI messages to the sensible value of 8 MB
        public static final int MAX_TEXT_MSG_SIZE = 8_388_608;

        // Setting the max idle time to 15 minutes for WebSocket connections
        public static final long SESSION_IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(15);

        // WebSocket session ping-pong interval
        public static final long PING_INTERVAL_IN_MINUTES = 10L;
    }

}
