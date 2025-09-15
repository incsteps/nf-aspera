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

package incsteps.plugin.aspera.transferd

import groovy.json.JsonOutput
import groovy.transform.CompileDynamic
import incsteps.plugin.aspera.ClientConfig

@CompileDynamic
class TransferBuilder {

    static String downloadFiles(ClientConfig client, List<String> sources, String destination) {
        def ret = [:]
        ret.session_initiation = buildInitiation(client)
        ret.security = buildSecurity(client)
        ret.file_system = buildFileSystem(client, destination)
        ret.transport = buildTransport(client)
        ret.assets = buildAssets(client, destination, sources)

        ret.direction = 'recv'
        ret.remote_host = client.remote_host

        def str = JsonOutput.prettyPrint(JsonOutput.toJson(ret))
        str
    }

    static String streamForDownload(ClientConfig client, String source) {
        downloadFiles(client, [source], null)
    }

    static private Map<String,Object> buildInitiation(ClientConfig client){
        def ret = [:]
        ret.ssh = buildSsh(client)
        ret
    }

    static private Map<String,Object> buildSsh(ClientConfig client){
        def ret = [:]
        ret.ssh_port = client.ssh_port as long
        if(client.remote_user)ret.remote_user = client.remote_user
        if(client.remote_password)ret.remote_password = client.remote_password
        if(client.ssh_private_key)ret.ssh_private_key = client.ssh_private_key
        if(client.ssh_private_key_path)ret.ssh_private_key_path = client.ssh_private_key_path
        if(client.ssh_private_key_passphrase)ret.ssh_private_key_passphrase = client.ssh_private_key_passphrase
        if(client.ssh_fingerprint)ret.ssh_fingerprint = client.ssh_fingerprint
        ret
    }

    static private Map<String,Object> buildSecurity(ClientConfig client){
        def ret = [:]
        if(client.cipher)ret.cipher = client.cipher
        ret
    }

    static private Map<String,Object> buildFileSystem(ClientConfig client, String destination){
        def ret = [:]
        if( destination ) {
            ret.create_dir = true
            ret.overwrite = "diff"
        }else {
            ret.overwrite = "always"
        }
        ret
    }

    static private Map<String,Object> buildTransport(ClientConfig client){
        def ret = [:]
        ret
    }

    static private Map<String,Object> buildAssets(ClientConfig client, String destination, List<String>sources){
        def ret = [:]
        if( destination )ret.destination_root = destination
        ret.paths = []
        sources.each{ s->
            def path = [:]
            path.source = s
            ret.paths << path
        }
        ret
    }
}
