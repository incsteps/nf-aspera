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

package incsteps.plugin.aspera.nio

import groovy.transform.CompileStatic

import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider

@CompileStatic
class AsperaFileSystem extends FileSystem{

    private final AsperaFileSystemProvider provider
    private final String base

    AsperaFileSystem(AsperaFileSystemProvider provider, String base){
        this.provider = provider
        this.base = base

    }

    @Override
    boolean equals(Object other) {
        if( this.class != other.class ) return false
        final that = other as AsperaFileSystem
        this.provider == that.provider && this.base == that.base
    }

    @Override
    int hashCode() {
        Objects.hash(provider,base)
    }

    @Override
    FileSystemProvider provider() {
        return provider
    }

    @Override
    void close() throws IOException {

    }

    @Override
    boolean isOpen() {
        return true
    }

    @Override
    boolean isReadOnly() {
        return true
    }

    @Override
    String getSeparator() {
        return '/'
    }

    @Override
    Iterable<Path> getRootDirectories() {
        return null
    }

    @Override
    Iterable<FileStore> getFileStores() {
        return null
    }

    @Override
    Set<String> supportedFileAttributeViews() {
        return null
    }

    @Override
    Path getPath(String first, String... more) {
        return new AsperaPath(this, base, first)
    }

    @Override
    PathMatcher getPathMatcher(String syntaxAndPattern) {
        return null
    }

    @Override
    UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException('User Principal Lookup Service not supported')
    }

    @Override
    WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException('Watch Service not supported')
    }

}
