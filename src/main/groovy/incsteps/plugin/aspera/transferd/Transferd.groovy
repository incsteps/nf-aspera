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

    static final String LINUX_AMD_PATH =
            "/ibm/linux-amd64-1.1.6"

    static final String MACOS_PATH =
            "/ibm/macos-1.1.6"

    static String getAsperaPath(){
        switch(OSDetector.operatingSystem){
            case OSDetector.OperatingSystem.LINUX:
                return LINUX_AMD_PATH
                break
            case OSDetector.OperatingSystem.MAC:
                return MACOS_PATH
                break
            //case OSDetector.OperatingSystem.WINDOWS: //TODO
            default:
                throw new RuntimeException("Operating System not supported")
        }
    }

    static String getTransferdBin(){
        "sbin/transferd"
    }

    static String getTransferdPath(){
        Path.of( Transferd.getResource("$asperaPath/$transferdBin").toURI()).toAbsolutePath().toString()
    }

    static void makeExecutableDir(String path){
        def bin = new File(path)
        if( bin.exists() && bin.isDirectory() ){
            bin.traverse {
                it.executable = true
            }
        }
    }

    static void makeExecutable(){
        makeExecutableDir( Path.of(Transferd.getResource("$asperaPath/bin").toURI()).toAbsolutePath().toString() )
        makeExecutableDir( Path.of(Transferd.getResource("$asperaPath/lib").toURI()).toAbsolutePath().toString() )
        makeExecutableDir( Path.of(Transferd.getResource("$asperaPath/sbin").toURI()).toAbsolutePath().toString() )
    }


    private Process transferd

    long pidProcess(){
        transferd ? transferd.pid() : -1
    }

    long launchDaemon(){

        makeExecutable()

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
