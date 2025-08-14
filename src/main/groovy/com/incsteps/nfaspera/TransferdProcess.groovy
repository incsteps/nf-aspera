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
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.trace.TraceObserver

/**
 * Example workflow events observer
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class TransferdProcess implements TraceObserver {

    String trasnferdPath

    Process transferd

    @Override
    void onFlowCreate(Session session) {
        def pb = new ProcessBuilder(trasnferdPath)
        transferd = pb.start()
        log.info "Transferd is starting! 🚀"
    }

    @Override
    void onFlowComplete() {
        transferd.descendants().forEach { p->
            p.destroy()
        }
        transferd.destroy()
        log.info "Transferd complete! 👋"
    }
}
