include {ncbi_ascp} from 'plugin/nf-aspera'

workflow{
    Channel.ncbi_ascp([
        destination:'downloads/',
        sources:[
            '/refseq/release/bacteria/bacteria.1.1.genomic.fna.gz',
            '/refseq/release/bacteria/bacteria.100.1.genomic.fna.gz',
        ]
    ])
    | filter { "$it".endsWith("gz") }
    | map { file -> file.bytes }
    | view
}