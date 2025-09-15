package incsteps.plugin.aspera.transferd

import incsteps.plugin.aspera.ClientConfig
import spock.lang.Specification

class TransferBuilderSpec extends Specification{

    void "a valid download spec"(){
        when:
        def transfer = TransferBuilder.downloadFiles(new ClientConfig(ssh_port: 1), ['test'], "destination")
        println transfer.toString()
        then:
        transfer.toString() == '''{
    "session_initiation": {
        "ssh": {
            "ssh_port": 1
        }
    },
    "security": {
        
    },
    "file_system": {
        "create_dir": true,
        "overwrite": "diff"
    },
    "transport": {
        
    },
    "assets": {
        "destination_root": "destination",
        "paths": [
            {
                "source": "test"
            }
        ]
    },
    "direction": "recv",
    "remote_host": null
}'''

    }

}
