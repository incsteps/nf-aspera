// Read files from NCBI remote host
include { ncbi_ascp } from 'plugin/nf-aspera'

params.resources = [
    '/refseq/README',
    '/1000genomes/README.Aspera_Users',
    '/1000genomes/aspera_transfer_guide.pdf',
    '/bigwig/job',
    '/bioproject/summary.txt',
]

process showDetails{
    input:
    val url

    output:
    stdout

    script:
    "ls -lhat $url"
}


workflow {
    Channel.ncbi_ascp(destination:'downloads', sources:params.resources) | showDetails | view
}