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
package de.rwth.idsg.mb.monitor;

import de.rwth.idsg.mb.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 11.05.2015
 */
@Slf4j
@Singleton
public class FileProvider {

    @Getter private String gitProperties;
    @Getter private String ixsiSchema;

    @PostConstruct
    public void init() {
        try {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("git.properties")) {
                gitProperties = internalRead(in, new LineBreakFileBuilder());
            }

            try (InputStream in = getClass().getClassLoader().getResourceAsStream(Constants.Ixsi.XML_SCHEMA_FILE)) {
                ixsiSchema = internalRead(in, new FileBuilder());
            }
        } catch (IOException e) {
            log.error("Exception occurred", e);
        }
    }

    private String internalRead(InputStream in, FileBuilder builder) throws IOException {
        try (InputStreamReader ist = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(ist)) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.add(line);
            }
        }
        return builder.print();
    }

    // Just a wrapper with default append since StringBuilder cannot be extended
    private class FileBuilder {
        StringBuilder stringBuilder = new StringBuilder();

        public FileBuilder add(String line) {
            stringBuilder.append(line);
            return this;
        }

        public String print() {
            return stringBuilder.toString();
        }
    }

    // Append string and then a line break
    private class LineBreakFileBuilder extends FileBuilder {

        @Override
        public LineBreakFileBuilder add(String line) {
            stringBuilder.append(line).append(System.getProperty("line.separator"));
            return this;
        }
    }
}
