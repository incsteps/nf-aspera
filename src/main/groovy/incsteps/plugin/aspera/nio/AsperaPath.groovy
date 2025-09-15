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

@CompileStatic
class AsperaPath implements Path{

    private final AsperaFileSystem fs
    private final String client
    private final Path path

    AsperaPath(AsperaFileSystem fs, String client, String path) {
        this(fs, client, Paths.get(path))
    }

    AsperaPath(AsperaFileSystem fs, String client, Path path) {
        this.fs = fs
        this.client = client
        this.path = path
    }

    String getClient(){
        client
    }

    private AsperaPath createPath(String path) {
        fs && path.startsWith('/') ? new AsperaPath(fs,client, path) : new AsperaPath(null, client, path)
    }

    @Override
    FileSystem getFileSystem() {
        return fs
    }

    @Override
    boolean isAbsolute() {
        return path.absolute
    }

    @Override
    Path getRoot() {
        return createPath("/")
    }

    @Override
    Path getFileName() {
        final result = path?.getFileName()?.toString()
        return result ? new AsperaPath(null, client, result) : null
    }

    @Override
    Path getParent() {
        String result = path.parent ? path.parent.toString() : null
        if( result ) {
            if( result != '/' ) result += '/'
            return createPath(result)
        }
        return null
    }

    @Override
    int getNameCount() {
        return path.toString() ? path.nameCount : 0
    }

    @Override
    Path getName(int index) {
        return new AsperaPath(null, client, path.getName(index).toString())
    }

    @Override
    Path subpath(int beginIndex, int endIndex) {
        return new AsperaPath(null, client, path.subpath(beginIndex, endIndex).toString())
    }

    @Override
    boolean startsWith(Path other) {
        return startsWith(other.toString())
    }

    @Override
    boolean endsWith(Path other) {
        return endsWith(other.toString())
    }

    @Override
    Path normalize() {
        return new AsperaPath(fs, client, path.normalize())
    }

    @Override
    Path resolve(Path other) {
        if( this.class != other.class )
            throw new ProviderMismatchException()

        def that = other as AsperaPath

        if( that.fs && this.fs != that.fs )
            return other

        else if( that.path ) {
            def newPath = this.path.resolve(that.path)
            return new AsperaPath(fs, client, newPath)
        }
        else {
            return this
        }
    }

    @Override
    Path relativize(Path other) {
        def otherPath = ((AsperaPath)other).path
        return createPath(path.relativize(otherPath).toString())
    }

    @Override
    URI toUri() {
        final String url = path.toString()
        return new URI(url)
    }

    @Override
    Path toAbsolutePath() {
        return this
    }

    @Override
    Path toRealPath(LinkOption... options) throws IOException {
        return this
    }

    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Register not supported by AsperaFileSystem")
    }

    @Override
    int compareTo(Path other) {
        return this.toUri().toString() <=> other.toUri().toString()
    }
}
