include {withAspera} from 'plugin/nf-aspera'


process DECOMPRESS_GZ {
    publishDir params.outdir, mode: 'copy'

    input:
    path gz_file

    output:
    path "${gz_file.baseName}"

    script:
    """
    gunzip -c ${gz_file} > ${gz_file.baseName}

    if [ ! -f "${gz_file.baseName}" ]; then
        echo "Error: ${gz_file}"
        exit 1
    fi

    echo "${gz_file.baseName}"
    """
}

process VALIDATE_FASTA {
    publishDir "${params.outdir}/validated", mode: 'copy'

    input:
    path fna_file

    output:
    path 'validation_report.txt'

    script:
    """
    #!/bin/bash

    echo "Validando archivo FASTA: ${fna_file}" > validation_report.txt
    echo "Fecha: \$(date)" >> validation_report.txt
    echo "=======================================" >> validation_report.txt

    # Contar número total de secuencias
    total_seqs=\$(grep -c "^>" ${fna_file})
    echo "Número total de secuencias: \$total_seqs" >> validation_report.txt

    # Validar formato FASTA básico
    if grep -q "^>" ${fna_file}; then
        echo "Formato FASTA válido: SÍ" >> validation_report.txt
        cp ${fna_file} validated_${fna_file}
    else
        echo "Formato FASTA válido: NO" >> validation_report.txt
        echo "Error: No se encontraron headers FASTA válidos" >> validation_report.txt
        exit 1
    fi

    echo "Validación completada exitosamente" >> validation_report.txt
    """
}


workflow{
    Channel.withAspera([
        client: 'ncbi',
        destination:'downloads/',
        sources:[
            '/refseq/release/bacteria/',
        ]
    ])
    | filter { "$it".endsWith("gz") }
    | DECOMPRESS_GZ
    | filter { "$it".endsWith("fna") }
    | VALIDATE_FASTA
    | map { file -> file.text }
    | view
}