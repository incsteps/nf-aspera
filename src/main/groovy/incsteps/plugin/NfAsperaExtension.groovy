/*
 * Copyright (C) 2025 Incremental Steps Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package incsteps.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowWriteChannel
import incsteps.plugin.aspera.ClientConfig
import incsteps.plugin.aspera.transferd.TransferListener
import incsteps.plugin.aspera.transferd.TransferdMonitor
import nextflow.Channel
import nextflow.Session
import nextflow.extension.CH
import nextflow.plugin.extension.Factory
import nextflow.plugin.extension.PluginExtensionPoint

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Implements a custom function which can be imported by
 * Nextflow scripts.
 */
@CompileStatic
@Slf4j
class NfAsperaExtension extends PluginExtensionPoint {

    private Session session

    private PluginConfig config

    @Override
    protected void init(Session session) {
        this.session = session
        initExtension()
    }

    private initExtension(){
        this.config = new PluginConfig( (session.config.navigate('aspera') ?: Collections.emptyMap()) as Map)
    }

    @Factory
    DataflowWriteChannel ascp(Map params=[:]) {
        final target = CH.create()
        session.addIgniter((action) -> downloadFile(target, params))
        return target
    }

    @Factory
    DataflowWriteChannel ncbi_ascp(Map params=[:]) {
        final target = CH.create()
        params.client = 'ncbi'
        session.addIgniter((action) -> downloadFile(target, params))
        return target
    }

    private void downloadFile(DataflowWriteChannel target, Map params) {

        validate(params)

        def client = params.client as String
        def sources = params.sources as List<String>
        def destination = params.destination as String
        def config = ClientConfig.getClient(client)

        final AtomicBoolean failed = new AtomicBoolean(false)
        TransferdMonitor.instance.downloadFiles(config, sources, destination, new TransferListener() {
            @Override
            void onFileCompleted(File file) {
                target << file.absolutePath
            }

            @Override
            void failed(String reason) {
                log.error("Aspera transfer failed, ${reason}")
                failed.set(true)
            }
        })
        target <<  Channel.STOP
    }

    private void validate(Map params){
        if( !params.client ){
            throw new IllegalArgumentException("Aspera client is required")
        }
        if( !params.containsKey("destination")){
            params.put("destination", ".")
        }
        def file = new File(params.destination.toString())
        if( file.isAbsolute() ){
            throw new IllegalArgumentException("Destination can't be an absolute path")
        }
        params.put "destination", Path.of(session.baseDir.toString(), params.destination.toString()).toAbsolutePath().toString()
        if( file.exists() && file.isFile() ){
            throw new IllegalArgumentException("Destination needs to be a directory")
        }
        if( params.source && params.sources ){
            throw new IllegalArgumentException("Only source or sources allowed at the same time")
        }
        if( params.source ) {
            params.sources = [params.source as String]
        }
    }
}
