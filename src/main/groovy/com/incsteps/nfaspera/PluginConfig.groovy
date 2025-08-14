package com.incsteps.nfaspera

import groovy.transform.PackageScope

import java.nio.file.Path

/**
 *
 * @author Jorge Aguilera <jorge@incsteps.com>
 */
@PackageScope
class PluginConfig {

    final private String transferdPath =
            "/ibm/linux-amd64-1.1.6/sbin/transferd"

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
        Path.of( this.class.getResource(transferdPath).toURI()).toAbsolutePath().toString()
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
                    ssh_private_key_path:new File(config ? config.ssh_private_key_path.toString():".").absolutePath.toString(),
                    ssh_private_key_passphrase:config?.ssh_private_key_passphrase,
                    cipher: config?.cipher,
            )
        }
    }
}
