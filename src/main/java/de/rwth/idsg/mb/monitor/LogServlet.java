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

import de.rwth.idsg.mb.AppConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 11.05.2015
 */
@Slf4j
@WebServlet(name = "LogServlet", value = "/monitor/log")
public class LogServlet extends HttpServlet {

    @EJB private AppConfiguration appConfiguration;

    private static File logFile;

    @PostConstruct
    private void postConstruct() {
        logFile = new File(
                System.getProperty("jboss.server.log.dir"),
                appConfiguration.getApplicationName() + ".log"
        );
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try (PrintWriter writer = response.getWriter()) {
            response.setContentType("text/plain");
            printLogFile(writer);
        }
    }

    private void printLogFile(PrintWriter writer) {
        try (InputStreamReader ist = new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(ist)) {
            String sCurrentLine;
            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                writer.println(sCurrentLine);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
