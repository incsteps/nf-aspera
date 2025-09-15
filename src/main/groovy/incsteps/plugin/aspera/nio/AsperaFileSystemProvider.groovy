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
import incsteps.plugin.aspera.ClientConfig
import incsteps.plugin.aspera.transferd.TransferdMonitor

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider

@CompileStatic
class AsperaFileSystemProvider extends FileSystemProvider{

    @Override
    String getScheme() {
        return "aspera"
    }

    private Map<URI, FileSystem> fileSystemMap = new LinkedHashMap<>(20)

    static private URI key(String s, String a) {
        new URI("$s://$a")
    }

    static private URI key(URI uri) {
        final base = uri.authority
        return key(uri.scheme.toLowerCase(), base.toLowerCase())
    }


    @Override
    FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        final scheme = uri.scheme.toLowerCase()
        if( scheme != this.getScheme() )
            throw new IllegalArgumentException("Not a valid ${getScheme().toUpperCase()} scheme: $scheme")

        final base = key(uri)
        if (fileSystemMap.containsKey(base))
            throw new IllegalStateException("File system `$base` already exists")

        return new AsperaFileSystem(this, uri.authority)
    }

    @Override
    FileSystem getFileSystem(URI uri) {
        getFileSystem(uri,false)
    }

    FileSystem getFileSystem(URI uri, boolean canCreate) {
        assert fileSystemMap != null

        final scheme = uri.scheme.toLowerCase()

        if( scheme != this.getScheme() )
            throw new IllegalArgumentException("Not a valid ${getScheme().toUpperCase()} scheme: $scheme")

        final key = key(uri)

        if( !canCreate ) {
            FileSystem result = fileSystemMap[key]
            if( result==null )
                throw new FileSystemNotFoundException("File system not found: $key")
            return result
        }

        synchronized (fileSystemMap) {
            FileSystem result = fileSystemMap[key]
            if( result==null ) {
                result = newFileSystem(uri,Collections.emptyMap())
                fileSystemMap[key] = result
            }
            return result
        }
    }

    @Override
    Path getPath(URI uri) {
        def path = uri.path
        return getFileSystem(uri,true).getPath(path)
    }

    @Override
    InputStream newInputStream(Path path, OpenOption... options) throws IOException{
        if(path.class != AsperaPath)
            throw new ProviderMismatchException()
        if (options.length > 0) {
            for (OpenOption opt: options) {
                // All OpenOption values except for APPEND and WRITE are allowed
                if (opt == StandardOpenOption.APPEND ||
                        opt == StandardOpenOption.WRITE)
                    throw new UnsupportedOperationException("'$opt' not allowed");
            }
        }
        def asperaPath = path as AsperaPath
        def client = ClientConfig.getClient(asperaPath.client)
        def source = path.toUriString()
        def input = new PipedInputStream()
        def output = new PipedOutputStream(input)
        TransferdMonitor.instance.downloadStream(client, source, output)
        input
    }

    @Override
    OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        throw new UnsupportedOperationException("Write not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException("Directory listing unsupported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Create directory not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Delete not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Copy not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Move not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    boolean isSameFile(Path path, Path path2) throws IOException {
        return path == path2
    }

    @Override
    boolean isHidden(Path path) throws IOException {
        return path.getFileName().startsWith('.')
    }

    @Override
    FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException("File store not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    void checkAccess(Path path, AccessMode... modes) throws IOException {

        for( AccessMode m : modes ) {
            if( m == AccessMode.WRITE )
                throw new AccessDeniedException("Write mode not supported")
            if( m == AccessMode.EXECUTE )
                throw new AccessDeniedException("Execute mode not supported")
        }
    }

    @Override
    def <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null
    }

    @Override
    def <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not a valid ${getScheme().toUpperCase()} file attribute type: $type")
    }

    @Override
    Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Read file attributes not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Set file attributes not supported by ${getScheme().toUpperCase()} file system provider")
    }

    @Override
    SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        if (path.class != AsperaPath)
            throw new ProviderMismatchException()

        if (options.size() > 0) {
            for (OpenOption opt: options) {
                // All OpenOption values except for APPEND and WRITE are allowed
                if (opt == StandardOpenOption.APPEND || opt == StandardOpenOption.WRITE)
                    throw new UnsupportedOperationException("'$opt' not allowed");
            }
        }

        def client = ClientConfig.getClient('ncbi')
        def source = "/refseq/README"
        def input = new PipedInputStream()
        def output = new PipedOutputStream(input)

        TransferdMonitor.instance.downloadStream(client, source, output)

        new SeekableByteChannel() {

            private long _position

            @Override
            int read(ByteBuffer buffer) throws IOException {
                def data=0
                int len=0
                while( buffer.hasRemaining() && (data=input.read())!=-1 ) {
                    buffer.put((byte)data)
                    len++
                }
                _position += len
                return len ?: -1
            }

            @Override
            int write(ByteBuffer src) throws IOException {
                throw new UnsupportedOperationException("Write operation not supported")
            }

            @Override
            long position() throws IOException {
                return _position
            }

            @Override
            SeekableByteChannel position(long newPosition) throws IOException {
                throw new UnsupportedOperationException("Position operation not supported")
            }

            @Override
            long size() throws IOException {
                // this value is going to be used as the buffer size
                // file related operation. See for example {@link Files#readAllBytes}
                return 8192
            }

            @Override
            SeekableByteChannel truncate(long unused) throws IOException {
                throw new UnsupportedOperationException("Truncate operation not supported")
            }

            @Override
            boolean isOpen() {
                return true
            }

            @Override
            void close() throws IOException {
                input.close()
            }
        }
    }
}
