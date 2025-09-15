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

import groovy.transform.CompileStatic

import java.nio.file.Path

@CompileStatic
@Singleton
class Transferd {

    static final String TRANSFERD_PATH =
            "/ibm/linux-amd64-1.1.6/sbin/transferd"


    static String getTransferdPath(){
        Path.of( Transferd.getResource(TRANSFERD_PATH).toURI()).toAbsolutePath().toString()
    }

    private Process transferd

    long pidProcess(){
        transferd ? transferd.pid() : -1
    }

    long launchDaemon(){
        def pb = new ProcessBuilder(transferdPath)
        pb.redirectErrorStream(true)

        transferd = pb.start()
        // dont know why but we need to consume background daemon console to make grpc works fine
        final out = transferd.inputStream
        new Thread({
            byte[] buffer = new byte[4000];
            while (isAlive(transferd)) {
                int no = out.available();
                if (no > 0) {
                    out.read(buffer, 0, Math.min(no, buffer.length));
                }
                try {
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {
                }
            }
        }).start()
        transferd.pid()
    }

    void killDaemon(){
        transferd?.descendants()?.forEach { p->
            p.destroy()
        }
        transferd?.destroy()
        transferd = null
    }

    static boolean isAlive(Process p) {
        try {
            p?.exitValue();
            return false;
        }
        catch (IllegalThreadStateException e) {
            return true;
        }
    }

}
