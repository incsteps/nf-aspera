package com.incsteps.nfaspera

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Channel
import nextflow.Session
import nextflow.plugin.extension.Factory
import nextflow.plugin.extension.PluginExtensionPoint
import nextflow.extension.CH

import java.nio.file.Path

@Slf4j
@CompileStatic
class AsperaExtension extends PluginExtensionPoint{

    private Session session

    private PluginConfig config

    @Override
    protected void init(Session session) {
        this.session = session
        initExtension()
    }

    private initExtension(){
        this.config = new PluginConfig( (session.config?.aspera ?: Collections.emptyMap()) as Map)
    }

    @Factory
    DataflowWriteChannel fromAspera(Map params=[:]) {
        final target = CH.create()
        session.addIgniter((action) -> downloadFile(target, params))
        return target
    }

    private void downloadFile(DataflowWriteChannel target, Map params){

        def transferd = new TransferdProtocol()
        def sources = params.sources as List<String>
        def clientConfig = config.getClient((params.client ?: 'default') as String)

        validate(params)

        params.put "destination", Path.of(session.workDir.toString(), params.destination.toString()).toAbsolutePath().toString()

        def transferred = transferd.downloadAssets(clientConfig, params.destination?.toString(), sources, new TransferListener() {
            @Override
            void onFileCompleted(File file) {
                target << file.absolutePath
            }
        })

        target << Channel.STOP
    }

    private void validate(Map params){
        if( !params.containsKey("destination")){
            params.put("destination", ".")
        }
        def file = new File(params.destination.toString())
        if( file.isAbsolute() ){
            throw new IllegalArgumentException("Destination can't be an absolute path")
        }
        if( file.exists() && file.isFile() ){
            throw new IllegalArgumentException("Destination needs to be a directory")
        }
    }

}
