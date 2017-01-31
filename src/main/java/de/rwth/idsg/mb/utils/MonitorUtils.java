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
package de.rwth.idsg.mb.utils;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by igor on 05.08.2016.
 */
public final class MonitorUtils {

    private MonitorUtils() { }

    public static final int HOURS_OFFSET = 1;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String trim(String str) {
        if (str == null) {
            return "";
        } else {
            return str.trim();
        }
    }

    public static String getTimestampOrNow(String str, int hoursOffset) {
        if (Strings.isNullOrEmpty(str)) {
            return DATE_TIME_FORMATTER.print(DateTime.now().plusHours(hoursOffset));
        } else {
            return str;
        }
    }
}
