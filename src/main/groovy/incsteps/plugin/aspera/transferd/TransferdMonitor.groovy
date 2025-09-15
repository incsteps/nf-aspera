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

import com.ibm.software.aspera.transferd.api.*
import groovy.util.logging.Slf4j
import incsteps.plugin.aspera.ClientConfig
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver

@Singleton
@Slf4j
class TransferdMonitor {

    final TransferServiceGrpc.TransferServiceStub asyncClient = TransferServiceGrpc.newStub(
            ManagedChannelBuilder.forAddress("localhost", 55002).usePlaintext().build())

    final TransferServiceGrpc.TransferServiceBlockingStub client = TransferServiceGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 55002).usePlaintext().build())

    void downloadFiles(ClientConfig clientConfig, List<String> sources, String destination, TransferListener listener=null){
        def transferSpec = TransferBuilder.downloadFiles(clientConfig, sources, destination)
        log.info("Downloading file $transferSpec")

        final transferResponse = client.startTransfer(TransferRequest.newBuilder()
                .setTransferType(TransferType.FILE_REGULAR)
                .setConfig(TransferConfig.newBuilder().build())
                .setTransferSpec(transferSpec)
                .build())

        Iterator<TransferResponse> monitorTransferResponse = client.monitorTransfers(
                RegistrationRequest.newBuilder()
                        .addFilters(RegistrationFilter.newBuilder()
                                .setOperator(RegistrationFilterOperator.OR)
                                .addTransferId(transferResponse.transferId)
                                .build())
                        .build())

        // monitor transfer until it finishes
        while (monitorTransferResponse.hasNext()) {
            TransferResponse info = monitorTransferResponse.next();

            if( listener && info.transferEvent == TransferEvent.FILE_STOP){
                def file = new File(info.fileInfo.path)
                listener.onFileCompleted(file)
            }

            if (listener && info.getStatus() == TransferStatus.FAILED ) {
                listener.failed(info.toString())
            }

            if (info.getStatus() == TransferStatus.FAILED ||
                    info.getStatus() == TransferStatus.COMPLETED) {
                log.info("Download finished $info.status")
                break
            }
        }
    }

    void downloadStream(ClientConfig clientConfig, String source, OutputStream outputStream){

        def transferSpec = TransferBuilder.streamForDownload(clientConfig, source)
        log.info("Downloading stream $transferSpec")

        final observer = new StreamObserver<StartTransferResponse>(){
            @Override
            void onNext(StartTransferResponse transferResponse) {
                try {
                    log.info("Transfer $transferResponse.transferId started")
                    readData(asyncClient, transferResponse.transferId, outputStream)
                } catch (InterruptedException e) {
                    log.error("failed to write data", e)
                }
            }

            @Override
            void onError(Throwable throwable) {
                println throwable
            }

            @Override
            void onCompleted() {
            }
        }

        asyncClient.startTransfer(
                TransferRequest.newBuilder()
                        .setTransferType(TransferType.FILE_TO_STREAM_DOWNLOAD)
                        .setConfig(TransferConfig.newBuilder().build())
                        .setTransferSpec(transferSpec)
                        .build(),
                observer)
    }

    static void readData(TransferServiceGrpc.TransferServiceStub pClient, String pTransferId, OutputStream outputStream) throws InterruptedException {
        pClient.readStream(ReadStreamRequest.newBuilder()
                .setTransferId(pTransferId)
                .build(),
                new StreamObserver<ReadStreamResponse>() {
                    @Override
                    void onNext(ReadStreamResponse value) {
                        if( value.hasChunk() ){
                            outputStream.write value.chunk.contents.toByteArray()
                        }
                    }

                    @Override
                    void onError(Throwable t) {
                        log.error("error while reading stream " + t.getMessage(),t)
                        outputStream.close()
                    }

                    @Override
                    void onCompleted() {
                        outputStream.close()
                    }
                })
    }
}
