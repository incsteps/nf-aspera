// Read a file from NCBI remote host

long startTime = System.currentTimeMillis()
println "10MB.1 size in bytes = " + file('aspera://demo/aspera-test-dir-small/10MB.1').bytes.length
long endTime = System.currentTimeMillis()

println "Download stream tooks ${endTime-startTime} ms"