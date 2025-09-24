include { ascp; ncbi_ascp; ena_ascp } from 'plugin/nf-aspera'


workflow{

    Channel.ascp( client: 'demo', destination:'downloads/', source: 'aspera-test-dir-small/10MB.1') | view

    Channel.ncbi_ascp( destination:'downloads/', sources: [
            '/refseq/release/bacteria/bacteria.1.1.genomic.fna.gz',
            '/refseq/release/mitochondrion/mitochondrion.1.1.genomic.fna.gz',
    ] ) | view

    Channel.ena_ascp(  destination:'downloads/', source: 'vol1/fastq/ERR164/ERR164407/ERR164407.fastq.gz' ) | view
}