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

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jooq.SQLDialect;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.util.List;

/**
 * Holds the global configuration parameters for the whole application.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.07.2014
 */
@Slf4j
@Getter
@Singleton
@Startup
public class AppConfiguration {

    private String applicationName;
    private RegioIT regioIT;
    private Ixsi ixsi;
    private Resource resource;
    private Mail mail;
    private DB db;

    @PostConstruct
    private void init() {

        DateTimeZone.setDefault(DateTimeZone.UTC);
        log.info("Date/time zone of the application is set to UTC. Current date/time: {}", new DateTime());

        PropertiesFileLoader prop = getPropertiesFile();

        applicationName = prop.getString("application.name");

        ixsi = Ixsi.builder()
                   .systemId(prop.getString("ixsi.systemid"))
                   .build();

        resource = Resource.builder()
                           .authHeader(prop.getString("resource.auth.header"))
                           .authToken(prop.getString("resource.auth.token"))
                           .build();

        regioIT = RegioIT.builder()
                         .pushEnabled(prop.getBoolean("regioit.push.enabled"))
                         .uraBaseUrl(prop.getString("regioit.ura.base.url"))
                         .uraBasePort(prop.getInt("regioit.ura.base.url.port"))
                         .restBaseUrl(prop.getString("regioit.rest.base.url"))
                         .build();

        db = DB.builder()
               .jndiName(prop.getString("db.jndi.name"))
               .sqlLogging(prop.getBoolean("db.sql.logging"))
               .build();

        mail = Mail.builder()
                   .enabled(prop.getBoolean("mail.enabled"))
                   .host(prop.getOptionalString("mail.host"))
                   .username(prop.getOptionalString("mail.username"))
                   .password(prop.getOptionalString("mail.password"))
                   .protocol(prop.getOptionalString("mail.protocol"))
                   .port(prop.getOptionalInt("mail.port"))
                   .from(prop.getOptionalString("mail.from"))
                   .recipients(prop.getStringList("mail.recipients"))
                   .build();
    }

    private PropertiesFileLoader getPropertiesFile() {
        PropertiesFileLoader pfl;

        try {
            // First, try internal and look at the classpath/bundle
            pfl = new PropertiesFileLoader("main.properties");

        } catch (IOException e) {
            // OK, not included, try external file
            String configDirProperty = "jboss.server.config.dir";
            String fileName = "mb-adapter.properties";

            log.warn(e.getMessage());
            log.info("Loading '{}' from '{}'", fileName, configDirProperty);

            pfl = new PropertiesFileLoader(configDirProperty, fileName);
        }

        return pfl;
    }

    @Builder
    @Getter
    public static class Ixsi {
        private final String systemId;
    }

    @Builder
    @Getter
    public static class Resource {
        private final String authHeader;
        private final String authToken;
    }

    @Builder
    @Getter
    public static class RegioIT {
        private boolean pushEnabled;
        private String uraBaseUrl;
        private int uraBasePort;
        private String restBaseUrl;
    }

    @Builder
    @Getter
    public static class DB {
        private final String jndiName;
        private final boolean sqlLogging;
        private final SQLDialect sqlDialect = SQLDialect.POSTGRES;
    }

    @Builder
    @Getter
    public static class Mail {
        private final boolean enabled;
        private final String host, username, password, protocol;
        private final Integer port;
        private final String from;
        private final List<String> recipients;
    }

}
