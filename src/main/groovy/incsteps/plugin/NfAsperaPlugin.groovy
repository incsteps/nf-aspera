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

package incsteps.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import incsteps.plugin.aspera.nio.AsperaFileSystemProvider
import incsteps.plugin.aspera.transferd.Transferd
import nextflow.Global
import nextflow.file.FileHelper
import nextflow.plugin.BasePlugin
import org.pf4j.PluginWrapper

/**
 * The plugin entry point
 */
@CompileStatic
@Slf4j
class NfAsperaPlugin extends BasePlugin {

    NfAsperaPlugin(PluginWrapper wrapper) {
        super(wrapper)
        showBanner()
    }

    static void showBanner() {
        log.info(" ___                                             _        _ ")
        log.info("|_ _|_ __   ___ _ __ ___ _ __ ___   ___ _ __  | |_ __ _| |")
        log.info(" | || '_ \\ / __| '__/ _ \\ '_ ` _ \\ / _ \\ '_ \\ | __/ _` | |")
        log.info(" | || | | | (__| | |  __/ | | | | |  __/ | | || | || (_| | |")
        log.info("|___|_| |_|\\___|_|  \\___|_| |_| |_|\\___|_| |_||_| \\__|\\__,_|_|")
        log.info("")
        log.info("   ____  _                 ")
        log.info("  / ___|| |_ ___ _ __  ___ ")
        log.info("  \\___ \\| __/ _ \\ '_ \\/ __|")
        log.info("   ___) | ||  __/ |_) \\__ \\")
        log.info("  |____/ \\__\\___| .__/|___/")
        log.info("                |_|        ")
        log.info("")
        log.info("  :: http://incsteps.com :: ")
        log.info("")
    }

    @Override
    void start() {
        super.start()
        FileHelper.getOrInstallProvider(AsperaFileSystemProvider)
        Transferd.instance.launchDaemon()
        Global.session.addShutdownHook {
            Transferd.instance.killDaemon()
        }
    }

    @Override
    void stop() {
        super.stop()
        Transferd.instance.killDaemon()
    }
}
