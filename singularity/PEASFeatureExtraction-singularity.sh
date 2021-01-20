#!/bin/bash

ARGUMENTS=() 

while [[ "$#" -gt 0 ]]; do
    ARGUMENTS+=("$1"); shift ;
done

if [[ ${#ARGUMENTS[@]} -ne 4 ]]; then
    echo "Usage: PEASFeatureExtraction-singularity.sh bamfile genome fastapath outdir"
    echo "bamfile: Path to the bam file."
    echo "genome: Genome used by HOMER and conservation scores (i.e., hg19, hg38, mm10)."
    echo "Note: For other genomes, please add conservation and filter files for the genome to /PEAS/extraction_files/ in the singularity directory."
    echo "fastapath: Path to the .fa file for the genome."
    echo "outdir: The output directory to save output files."
    exit 0
fi

BAMPATH=${ARGUMENTS[0]}
GENOME=${ARGUMENTS[1]}
FASTA=${ARGUMENTS[2]}
OUTDIR=${ARGUMENTS[3]}

BAMDIR="${BAMPATH%/*}"
BAMFILENAME="${BAMPATH##*/}"
BAMFILENAME="${BAMFILENAME%.*}"

/PEAS/PEASFeatureExtraction.sh ${BAMDIR} ${BAMFILENAME} ${OUTDIR} ${FASTA} ${GENOME} /PEAS/extraction_files/filter_${GENOME}.bed /PEAS/extraction_files/humantop_Nov2016_HOMER.motifs /PEAS/extraction_files/conservation_${GENOME}.bed /PEAS/extraction_files/CTCF.motifs /PEAS/ /PEAS/extraction_files/chromosomes_${GENOME}.txt

