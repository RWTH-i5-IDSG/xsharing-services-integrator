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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Encapsulates java.util.Properties and adds type specific convenience methods
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 01.10.2015
 */
@Slf4j
public class PropertiesFileLoader {

    private Properties prop;

    public PropertiesFileLoader(String name) throws IOException {
        loadFromClasspath(name);
    }

    /**
     * http://stackoverflow.com/a/27962543
     */
    public PropertiesFileLoader(String configDirProperty, String name) {
        String configDir = System.getProperty(configDirProperty);
        if (configDir == null) {
            throw new RuntimeException("System property '" + configDirProperty  + "' cannot be found");
        }

        String file = configDir + File.separator + name;
        loadFromSystem(file);
    }

    // -------------------------------------------------------------------------
    // Strict
    // -------------------------------------------------------------------------

    public String getString(String key) {
        String s = prop.getProperty(key);

        if (s == null) {
            throw new IllegalArgumentException("The property '" + key + "' is not found");
        }

        if (s.isEmpty()) {
            throw new IllegalArgumentException("The property '" + key + "' has no value set");
        }

        return trim(key, s);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    // -------------------------------------------------------------------------
    // Return null if not set
    // -------------------------------------------------------------------------

    public String getOptionalString(String key) {
        String s = prop.getProperty(key);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }
        return trim(key, s);
    }

    public List<String> getStringList(String key) {
        String s = prop.getProperty(key);
        if (Strings.isNullOrEmpty(s)) {
            return Collections.emptyList();
        }

        return Splitter.on(",")
                       .trimResults()
                       .omitEmptyStrings()
                       .splitToList(s);
    }

    public Boolean getOptionalBoolean(String key) {
        String s = getOptionalString(key);
        if (s == null) {
            // In this special case, to make findbugs happy, we don't return null.
            // Reason: http://findbugs.sourceforge.net/bugDescriptions.html#NP_BOOLEAN_RETURN_NULL
            return false;
        } else {
            return Boolean.parseBoolean(s);
        }
    }

    public Integer getOptionalInt(String key) {
        String s = getOptionalString(key);
        if (s == null) {
            return null;
        } else {
            return Integer.parseInt(s);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void loadFromSystem(String fileName) {
        try (FileInputStream inputStream = new FileInputStream(fileName)) {
            prop = new Properties();
            prop.load(inputStream);
            log.info("Loaded properties from {}", fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadFromClasspath(String fileName) throws IOException {
        URL url = this.getClass().getClassLoader().getResource(fileName);
        if (url == null) {
            throw new IOException("Property file '" + fileName + "' is not found in classpath");
        }

        try (InputStream is = url.openStream()) {
            prop = new Properties();
            prop.load(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String trim(String key, String value) {
        String trimmed = value.trim();
        if (!trimmed.equals(value)) {
            log.warn("The property '{}' has leading or trailing spaces which were removed!", key);
        }
        return trimmed;
    }
}
