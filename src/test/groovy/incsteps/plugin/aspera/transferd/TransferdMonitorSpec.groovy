package incsteps.plugin.aspera.transferd

import incsteps.plugin.aspera.ClientConfig
import spock.lang.Specification

class TransferdMonitorSpec extends Specification{


    void setupSpec(){
        Transferd.instance.launchDaemon()
    }

    void cleanupSpec(){
        Transferd.instance.killDaemon()
    }


    void "should download an example from demoaspera"(){
        given:
        def obj = TransferdMonitor.instance

        def config = ClientConfig.demoAspera()

        if( new File("/tmp/10MB.1").exists() )
            new File("/tmp/10MB.1").delete()

        when:
        def ret = obj.downloadFiles(config, ["aspera-test-dir-small/10MB.1"], "/tmp" )

        then:

        new File("/tmp/10MB.1").exists()
    }

    void "should download multiple examples from demoaspera"(){
        given:
        def obj = TransferdMonitor.instance

        def config = ClientConfig.demoAspera()

        if( new File("/tmp/10MB.1").exists() )
            new File("/tmp/10MB.1").delete()
        if( new File("/tmp/10MB.2").exists() )
            new File("/tmp/10MB.2").delete()

        when:
        def ret = obj.downloadFiles(config, ["aspera-test-dir-small/10MB.1","aspera-test-dir-small/10MB.2"], "/tmp" )

        then:
        new File("/tmp/10MB.1").exists()
        new File("/tmp/10MB.2").exists()
    }

    void "should stream an example from ncbi"(){
        given:
        def obj = TransferdMonitor.instance

        def config = ClientConfig.ncbi()

        when:
        def input = new PipedInputStream()
        def output = new PipedOutputStream(input)

        obj.downloadStream(config, "/refseq/README" , output)
        def result = new String(input.readAllBytes())

        then:
        result.startsWith('#############################################################\nREADME for ftp://ncbi.nlm.nih.gov/refseq/')
    }
}
