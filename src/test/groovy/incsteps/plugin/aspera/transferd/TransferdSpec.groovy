package incsteps.plugin.aspera.transferd

import spock.lang.Specification

class TransferdSpec extends Specification{

    def 'should start a transferd instance' () {

        when:
        Transferd.instance.launchDaemon()

        then:
        Transferd.instance.pidProcess()

        when:
        Transferd.instance.killDaemon()

        then:
        Transferd.instance.pidProcess() == -1
    }

}
