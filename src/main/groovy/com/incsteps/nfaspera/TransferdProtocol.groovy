package com.incsteps.nfaspera

import groovy.json.JsonOutput
import com.ibm.software.aspera.transferd.api.*;
import groovy.util.logging.Slf4j
import io.grpc.ManagedChannelBuilder

import java.nio.file.Files
import java.nio.file.Path;

@Slf4j
class TransferdProtocol {


    List<File> downloadAssets(PluginConfig.AsperaClient config, String destination, List<String> sources, TransferListener listener=null){

        def spec =[
                session_initiation:[
                        ssh:[
                                ssh_port: config.ssh_port,
                                remote_user: config.remote_user,
                                remote_password: config.remote_password,
                                ssh_private_key_path:config.ssh_private_key_path,
                                ssh_private_key_passphrase:config.ssh_private_key_passphrase
                        ]
                ],

                security:[
                        cipher: config.cipher,
                ],

                direction:"recv",

                remote_host: config.remote_host,

                title:"strategic",

                "assets":[
                        destination_root: new File(destination).absolutePath + File.separator,
                        paths: sources.collect{[source: it]}
                ]
        ]

        Files.createDirectories(Path.of(spec.assets.destination_root))

        List<File> ret = []

        TransferServiceGrpc.TransferServiceBlockingStub client = TransferServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress("localhost", 55002).usePlaintext().build());


        String transferSpec = JsonOutput.prettyPrint(JsonOutput.toJson(spec))
        log.info(transferSpec)
        Transferd.StartTransferResponse transferResponse = client.startTransfer(Transferd.TransferRequest.newBuilder()
                .setTransferType(Transferd.TransferType.FILE_REGULAR)
                .setConfig(Transferd.TransferConfig.newBuilder().setLocalLog("/tmp/").build())
                .setTransferSpec(transferSpec)
                .build());
        String transferId = transferResponse.getTransferId();
        log.info(String.format("transfer started with id %s", transferId));

        Iterator<Transferd.TransferResponse> monitorTransferResponse = client.monitorTransfers(
                Transferd.RegistrationRequest.newBuilder()
                        .addFilters(Transferd.RegistrationFilter.newBuilder()
                                .setOperator(Transferd.RegistrationFilterOperator.OR)
                                .addTransferId(transferId)
                                .build())
                        .build());

        // monitor transfer until it finishes
        while (monitorTransferResponse.hasNext()) {
            Transferd.TransferResponse info = monitorTransferResponse.next();
            log.info("transfer info " + info);
            log.info("file info " + info.fileInfo);
            log.info("transfer event " + info.transferEvent);

            if (info.transferEvent == Transferd.TransferEvent.FILE_STOP) {
                log.info("finished " + info.status.toString());
                def file = new File( info.fileInfo.path)
                if( listener ){
                    listener.onFileCompleted(file)
                }
                ret << file
            }

            if( [   Transferd.TransferEvent.SESSION_STOP,
                    Transferd.TransferEvent.SESSION_ERROR,
                    Transferd.TransferEvent.SESSION_CANCELED,
                ].contains(info.transferEvent) ){
                break;
            }
        }

        return ret
    }
}
