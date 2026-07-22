/*
 * Copyright (C) 2026 Incremental Steps Software Solutions
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

import org.apache.commons.lang.SystemUtils

class OSDetector {
    enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX,
        UNKNOW
    }

    private static final String OS_ARCH = System.getProperty("os.arch")
    private static final String OS_NAME = System.getProperty("os.name")
    private static final String OS_VERSION = System.getProperty("os.version")

    static OperatingSystem getOperatingSystem(){
        if( getOSMatchesName("Linux") || getOSMatchesName("LINUX") ){
            return OperatingSystem.LINUX
        }
        return OperatingSystem.UNKNOW
    }

    private static boolean getOSMatches(String osNamePrefix, String osVersionPrefix) {
        return isOSMatch(OS_NAME, OS_VERSION, osNamePrefix, osVersionPrefix);
    }

    private static boolean getOSMatchesName(String osNamePrefix) {
        return isOSNameMatch(OS_NAME, osNamePrefix);
    }

    static boolean isOSMatch(String osName, String osVersion, String osNamePrefix, String osVersionPrefix) {
        if (osName != null && osVersion != null) {
            return osName.startsWith(osNamePrefix) && osVersion.startsWith(osVersionPrefix);
        } else {
            return false;
        }
    }

    static boolean isOSNameMatch(String osName, String osNamePrefix) {
        return osName == null ? false : osName.startsWith(osNamePrefix);
    }
}
