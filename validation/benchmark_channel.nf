// Read a file from NCBI remote host
include { ncbi_ascp } from 'plugin/nf-aspera'

params.resource = '/refseq/release/bacteria/bacteria.1029.genomic.gbff.gz'

println "Downloading $params.resource using ascp operator"

start = System.currentTimeMillis()

workflow{
    Channel.ncbi_ascp( destination:'downloads/', source: params.resource) | view
}

workflow.onComplete{
    end = System.currentTimeMillis()
    bytes = file("downloads/${params.resource.split('/').last()}").size()
    result = [start:start, bytes: bytes, end: end]

    println String.format("""
%s tooks %04.02f seconds to read %d bytes
""", "ascp", (result.end-result.start)/1000 as float, result.bytes)

}
