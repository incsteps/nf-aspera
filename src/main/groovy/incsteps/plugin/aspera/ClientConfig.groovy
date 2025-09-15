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

package incsteps.plugin.aspera

class ClientConfig {

    int ssh_port
    String remote_user
    String remote_password
    String remote_host

    String ssh_private_key
    String ssh_private_key_path
    String ssh_private_key_passphrase
    String ssh_fingerprint

    String cipher

    static ClientConfig demoAspera(){
        return fromMap([
                remote_host: "demo.asperasoft.com",
                ssh_port: 33001,
                remote_user: "aspera",
                remote_password: "demoaspera",
        ])
    }

    static ClientConfig ncbi(){
        def privateKey = ClientConfig.getResourceAsStream("/ncbi/aspera_tokenauth_id_rsa").text
        return fromMap([
                remote_host : 'ftp.ncbi.nlm.nih.gov',
                ssh_port : 22,
                remote_user : "anonftp",
                ssh_private_key: privateKey,
                ssh_private_key_passphrase : "743128bf-3bf3-45b5-ab14-4602c67f2950",
                cipher : "none",
        ])
    }

    static ClientConfig fromMap(Map config){
        return new ClientConfig(
                ssh_port: config.containsKey("ssh_port") && "$config.ssh_port".isNumber() ? config.ssh_port as int : 22,
                remote_user: config?.remote_user,
                remote_host: config?.remote_host,
                remote_password: config?.remote_password,
                ssh_private_key: config?.ssh_private_key,
                ssh_private_key_path: config.containsKey('ssh_private_key_path') ? new File(config.ssh_private_key_path.toString()).absolutePath.toString() : null,
                ssh_private_key_passphrase:config?.ssh_private_key_passphrase,
                ssh_fingerprint: config?.ssh_fingerprint,
                cipher: config?.cipher,
        )
    }

    final static private Map<String, ClientConfig> clients = [
            'demo' : demoAspera(),
            'ncbi' : ncbi()
    ]

    static void register(String id, ClientConfig config){
        clients.put(id, config)
    }

    static ClientConfig getClient(String id){
        clients.get(id)
    }
}
