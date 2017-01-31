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
package de.rwth.idsg.mb.adapter.ixsi.client.connect.backoff;

import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 15.10.2015
 */
public class SteppingBackoff extends AbstractBackoff {

    public SteppingBackoff() {
        super(TimeUnit.SECONDS.toMillis(5));
    }

    @Override
    public void calculateDelay() {
        int counter = getCounter();

        if (counter < 10) {
            setRetryDelay(TimeUnit.SECONDS.toMillis(15));

        } else if (counter < 20) {
            setRetryDelay(TimeUnit.SECONDS.toMillis(30));

        } else if (counter < 30) {
            setRetryDelay(TimeUnit.MINUTES.toMillis(1));

        } else if (counter < 40) {
            setRetryDelay(TimeUnit.MINUTES.toMillis(5));

        } else if (counter < 50) {
            setRetryDelay(TimeUnit.MINUTES.toMillis(10));

        } else {
            // This should not be accessed, because retryCount = 50 in AbstractBackoff;
            throw new RuntimeException("Too many retries");
        }
    }
}
