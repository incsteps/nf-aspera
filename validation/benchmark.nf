// Read a file from NCBI remote host

params.test = 'http'
params.resource = '/refseq/release/bacteria/bacteria.1029.genomic.gbff.gz'

println "Downloading $params.resource using $params.test method"

Map download(String url){
    start = System.currentTimeMillis()
    bytes = file(url).bytes.length
    end = System.currentTimeMillis()
    return [start:start, bytes: bytes, end: end]
}

result = switch( params.test ){
   case 'ftp' -> download("ftp://ftp.ncbi.nlm.nih.gov/$params.resource")
   case 'http' -> download("http://ftp.ncbi.nlm.nih.gov/$params.resource")
   case 'aspera' -> download("aspera://ncbi//$params.resource")
   default-> throw new IllegalArgumentException("not valid")
}

println String.format("""
%s tooks %04.02f seconds to read %d bytes
""", params.test, (result.end-result.start)/1000 as float, result.bytes)

