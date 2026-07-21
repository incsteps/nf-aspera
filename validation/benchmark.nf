nextflow.enable.dsl=2

params.resource = 'refseq/release/bacteria/bacteria.1029.genomic.gbff.gz'

workflow {
    methods_ch = Channel.of(
        ['aspera', "aspera://ncbi/${params.resource}"],
        ['http',   "http://ftp.ncbi.nlm.nih.gov/${params.resource}"],
        ['ftp',    "ftp://ftp.ncbi.nlm.nih.gov/${params.resource}"],
    )

    files_ch = methods_ch.map { method, url ->
        def start = System.currentTimeMillis()
        def bytes = file(url).bytes.length
        def end = System.currentTimeMillis()
        """
        $method took ${(end-start)/1000} seconds to read $bytes bytes
        """
    } | view

}