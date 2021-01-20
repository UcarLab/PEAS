#!/bin/bash 

ARGUMENTS=() 

while [[ "$#" -gt 0 ]]; do
    ARGUMENTS+=("$1"); shift ;
done

if [[ ${#ARGUMENTS[@]} -ne 4 ]]; then
    echo "Usage: PEAS-singularity.sh bamfile genome fastapath outdir"
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

echo "Running PEAS Feature Extraction"

/PEAS/singularity/PEASFeatureExtraction-singularity.sh ${BAMPATH} ${GENOME} ${FASTA} ${OUTDIR}

cp ${OUTDIR}/peak_features/${BAMFILENAME}_features.txt ${OUTDIR}/${BAMFILENAME}_features.txt

echo "Predicting Promoters"

/PEAS/singularity/PEASPrediction-singularity.sh ${OUTDIR}/${BAMFILENAME}_features.txt promoter ${OUTDIR}

echo "Predicting Enhancers"

/PEAS/singularity/PEASPrediction-singularity.sh ${OUTDIR}/promoter_${BAMFILENAME}_features.txt enhancer ${OUTDIR}

