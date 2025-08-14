/*
 * Copyright 2021, Seqera Labs
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

package com.incsteps.nfaspera

import groovy.transform.CompileStatic
import nextflow.Session
import nextflow.trace.TraceObserver
import nextflow.trace.TraceObserverFactory
/**
 * Implements the validation observer factory
 *
 * @author Jorge Aguilera <jorge@incsteps.com>
 */
@CompileStatic
class TransferdFactory implements TraceObserverFactory {

    @Override
    Collection<TraceObserver> create(Session session) {

        PluginConfig config = initConfig(session)

        final result = new ArrayList()
        result.add( new TransferdProcess(trasnferdPath: config.transferdPath) )
        return result
    }

    PluginConfig initConfig(Session session){
        new PluginConfig( (session.config?.aspera ?: Collections.emptyMap()) as Map)
    }
}
