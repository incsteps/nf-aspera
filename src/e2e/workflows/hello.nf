#!/usr/bin/env nextflow
include { ascp } from 'plugin/nf-aspera'

workflow  {

    Channel.ascp( client: 'demo', destination:'downloads/', source: 'aspera-test-dir-small/10MB.1') | view

}
