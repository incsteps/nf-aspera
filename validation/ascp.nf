include { ascp; ncbi_ascp } from 'plugin/nf-aspera'

demo = 'aspera-test-dir-small/10MB.1'

files = [
    '/refseq/release/bacteria/bacteria.1.1.genomic.fna.gz',
    '/refseq/release/mitochondrion/mitochondrion.1.1.genomic.fna.gz',
]

workflow{
    Channel.ascp( client: 'demo', destination:'downloads/', source: demo) | view

    Channel.ncbi_ascp( destination:'downloads/', sources: files ) | view
}