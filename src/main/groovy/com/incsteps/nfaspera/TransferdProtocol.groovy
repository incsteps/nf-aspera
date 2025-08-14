package com.incsteps.nfaspera

import com.ibm.software.aspera.transferd.api.RegistrationFilter
import com.ibm.software.aspera.transferd.api.RegistrationFilterOperator
import com.ibm.software.aspera.transferd.api.RegistrationRequest
import com.ibm.software.aspera.transferd.api.TransferConfig
import com.ibm.software.aspera.transferd.api.TransferEvent
import com.ibm.software.aspera.transferd.api.TransferRequest
import com.ibm.software.aspera.transferd.api.TransferResponse
import com.ibm.software.aspera.transferd.api.TransferServiceGrpc
import com.ibm.software.aspera.transferd.api.TransferStatus
import com.ibm.software.aspera.transferd.api.TransferType

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.grpc.ManagedChannelBuilder

import java.nio.file.Files
import java.nio.file.Path

/**
 *
 * @author Jorge Aguilera <jorge@incsteps.com>
 */
@Slf4j
class TransferdProtocol {


    @CompileStatic
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

        Files.createDirectories(Path.of(spec.assets['destination_root'].toString()))

        List<File> ret = []

        def client = TransferServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress("localhost", 55002).usePlaintext().keepAliveWithoutCalls(true).build());

        String transferSpec = JsonOutput.prettyPrint(JsonOutput.toJson(spec))
        log.info(transferSpec)
        def tmp = new File("/tmp")
        tmp.mkdirs()
        def transferResponse = client.startTransfer(TransferRequest.newBuilder()
                .setTransferType(TransferType.FILE_REGULAR)
                .setConfig(TransferConfig.newBuilder().setLocalLog(tmp.absolutePath).build())
                .setTransferSpec(transferSpec)
                .build());

        String transferId = transferResponse.transferId
        log.info(String.format("transfer started with id %s", transferId));

        def monitorTransferResponse = client.monitorTransfers(
                RegistrationRequest.newBuilder()
                        .addFilters(RegistrationFilter.newBuilder()
                                .setOperator(RegistrationFilterOperator.OR)
                                .addTransferId(transferId).build())
                        .build()
        )

        // monitor transfer until it finishes
        while (monitorTransferResponse.hasNext()) {
            TransferResponse info = monitorTransferResponse.next()
            log.info("transfer info $info")

            if (info.transferEvent == TransferEvent.FILE_STOP) {
                log.info("finished " + info.status.toString());
                def file = new File( info.fileInfo.path)
                if( listener ){
                    listener.onFileCompleted(file)
                }
                ret << file
            }

            if ([
                    TransferStatus.FAILED,
                    TransferStatus.COMPLETED
            ].contains(info.status)) {
                break;
            }

            if( [   TransferEvent.SESSION_STOP,
                    TransferEvent.SESSION_ERROR,
                    TransferEvent.SESSION_CANCELED,
                ].contains(info.transferEvent) ){
                break;
            }
        }

        return ret
    }
}
