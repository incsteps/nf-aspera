#!/usr/bin/env nextflow
workflow  {

    def txt = file('aspera://ena/pub/databases/ena/doc/FT_current.txt').text

    Channel.value(txt)| view

}
