include {ena_ascp} from 'plugin/nf-aspera'

workflow{
    Channel.ena_ascp([
        destination:'downloads/',
        sources:[
            'aspera://ena/pub/databases/ena/doc/FT_current.txt',
        ]
    ])
    | map { file -> file.bytes }
    | view
}