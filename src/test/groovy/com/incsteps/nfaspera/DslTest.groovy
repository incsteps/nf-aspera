package com.incsteps.nfaspera

import java.nio.file.Files
import java.util.jar.Manifest

import nextflow.Channel
import nextflow.plugin.Plugins
import nextflow.plugin.TestPluginDescriptorFinder
import nextflow.plugin.TestPluginManager
import nextflow.plugin.extension.PluginExtensionProvider
import org.pf4j.PluginDescriptorFinder
import spock.lang.Shared
import spock.lang.Timeout
import test.Dsl2Spec

import java.nio.file.Path


/**
 *
 * @author : jorge <jorge@incsteps.com>
 */
@Timeout(60)
class DslTest extends Dsl2Spec{

    @Shared String pluginsMode

    def setup() {
        // reset previous instances
        PluginExtensionProvider.reset()
        // this need to be set *before* the plugin manager class is created
        pluginsMode = System.getProperty('pf4j.mode')
        System.setProperty('pf4j.mode', 'dev')
        // the plugin root should
        def root = Path.of('.').toAbsolutePath().normalize()
        def manager = new TestPluginManager(root){
            @Override
            protected PluginDescriptorFinder createPluginDescriptorFinder() {
                return new TestPluginDescriptorFinder(){

                    @Override
                    protected Manifest readManifestFromDirectory(Path pluginPath) {
                        if( !Files.isDirectory(pluginPath) )
                            return null

                        final manifestPath = pluginPath.resolve('build/tmp/jar/MANIFEST.MF')
                        if( !Files.exists(manifestPath) )
                            return null

                        final input = Files.newInputStream(manifestPath)
                        return new Manifest(input)
                    }
                }
            }
        }
        Plugins.init(root, 'dev', manager)
    }

    def cleanup() {
        Plugins.stop()
        PluginExtensionProvider.reset()
        pluginsMode ? System.setProperty('pf4j.mode',pluginsMode) : System.clearProperty('pf4j.mode')
    }

    def 'can use an imported function' () {
        given:
        def config = [
                aspera:[
                    clients:[
                        asperasoft: [
                            remote_host : 'demo.asperasoft.com',
                            ssh_port : 33001,
                            remote_user : "aspera",
                            remote_password : "demoaspera",
                        ]
                    ]
                ]
        ]
        when:
        def SCRIPT = '''
            include {withAspera } from 'plugin/nf-aspera'
            Channel.withAspera([
                client: 'asperasoft',
                destination:'build/',
                sources:['aspera-test-dir-large/100MB',]
            ])
            | view    
            sleep 20          
            '''
        and:
        new MockScriptRunner(config).setScript(SCRIPT).execute()
        then:
        new File("build/100MB")
    }

}
