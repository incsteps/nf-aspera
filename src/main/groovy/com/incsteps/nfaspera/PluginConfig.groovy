package com.incsteps.nfaspera

import groovy.transform.PackageScope


/**
 * This class allows model an specific configuration, extracting values from a map and converting
 *
 *
 * We anotate this class as @PackageScope to restrict the access of their methods only to class in the
 * same package
 *
 * @author : jorge <jorge.aguilera@seqera.io>
 *
 */
@PackageScope
class PluginConfig {

    final private String transferdPath = "/home/jorge/personales/aspera-demo/ibm-aspera-transfer-sdk-linux-amd64-1.1.5/sbin/transferd"

    final private Map<String, AsperaClient> clients

    PluginConfig(Map map){
        def config = map ?: Collections.emptyMap()

        clients = collectClients(config)
    }

    AsperaClient getClient(String id){
        if( clients.containsKey(id))
            return clients.get(id)
        clients.entrySet().first()?.value
    }

    String getTransferdPath(){
        this.transferdPath
    }

    protected static Map<String, AsperaClient> collectClients(Map config){
        def clients = (config.clients ?: Collections.emptyMap()) as Map
        return clients.entrySet().collectEntries { entry ->
            [(entry.key) : AsperaClient.fromMap(entry.value as Map)]
        }
    }

    static class AsperaClient{
        int ssh_port
        String remote_user
        String remote_password
        String remote_host
        String ssh_private_key_path
        String ssh_private_key_passphrase
        String cipher
        static AsperaClient fromMap(Map config){
            return new AsperaClient(
                    ssh_port: config.containsKey("ssh_port") && "$config.ssh_port".isNumber() ? config.ssh_port as int : 22,
                    remote_user: config?.remote_user,
                    remote_host: config?.remote_host,
                    remote_password: config?.remote_password,
                    ssh_private_key_path:config?.ssh_private_key_path,
                    ssh_private_key_passphrase:config?.ssh_private_key_passphrase,
                    cipher: config?.cipher,
            )
        }
    }
}
