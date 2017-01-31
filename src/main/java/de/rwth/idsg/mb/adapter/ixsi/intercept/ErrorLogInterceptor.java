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
package de.rwth.idsg.mb.adapter.ixsi.intercept;

import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.AbstractBaseResponseType;
import xjc.schema.ixsi.ErrorType;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.05.2015
 */
@Slf4j
@ErrorLog
@Interceptor
public class ErrorLogInterceptor {

    @AroundInvoke
    public Object logErrors(InvocationContext ctx) throws Exception {
        Object[] parameters = ctx.getParameters();
        InOutContext pc = (InOutContext) parameters[0];
        AbstractBaseResponseType ab = (AbstractBaseResponseType) pc.getIncoming();

        if (ab.isSetError()) {
            for (ErrorType e : ab.getError()) {
                log.error("Code:'{}', NonFatal?:'{}', SystemMessage:'{}', UserMessage:'{}'",
                        e.getCode().value(), e.isNonFatal(), e.getSystemMessage(), e.getUserMessage());
            }
        }

        return ctx.proceed();
    }
}
