#!/bin/bash
args=()
((index=0))
for i in "$@"
do
    args[${index}]="$i"
    ((index++))
done

inDir="${args[0]}"
prefix="${args[1]}"
outDir="${args[2]}"
fasta="${args[3]}"
filterpeaks="${args[4]}"
homermotifs="${args[5]}"
conservation="${args[6]}"
ctcfmotifs="${args[7]}"
nfrsize="150"

cd "${outDir}"

mkdir peak_features
cd peak_features

jarpath="${args[8]}/"


##Sort BAM  #TODO add option to skip this step if already sorted
echo "--- Sorting bam file. ---"
samtools sort  -T PEASEXTRACT -o ${prefix}_sorted.bam "${inDir}/${prefix}.bam"
samtools index ${prefix}_sorted.bam

#
##Filter BAM
echo "--- Filter bam file for nucleosome free reads. ---"
java -jar "${jarpath}PEASTools.jar" bamfilter "${prefix}_sorted.bam" "${nfrsize}"

#Call Peaks
echo "--- Calling Peaks. ---"
macs2 callpeak -t ${prefix}_sorted.bam.${nfrsize}.bam -f BAMPE -n ${prefix} -g 'hs' --nomodel --outdir "${outDir}/peak_features"

#Filter error prone regions
echo "--- Filtering peaks. ---"
java -jar "${jarpath}PEASTools.jar" filter "${prefix}_peaks.narrowPeak" "${filterpeaks}" "${prefix}_peaks.filtered"


echo "--- Calling annotations & known motifs. ---"
#HOMER Annotations
annotatePeaks.pl "${prefix}_peaks.filtered" hg19 -m "${homermotifs}" -nmotifs > ${prefix}_peaks_annotated.bed

#call denovo motifs
echo "--- Calling denovo motifs. ---"
findMotifsGenome.pl "${prefix}_peaks.filtered" "${fasta}" "${outDir}/denovo"

mkdir "${outDir}/denovo/merge"
cp "${outDir}/denovo/homerResults/"*.motif "${outDir}/denovo/merge"
rm "${outDir}/denovo/merge/"*.similar*
rm "${outDir}/denovo/merge/"*RV.motif
cat "${outDir}/denovo/merge/"*.motif >> "${outDir}/denovo/merge/merged.motifs"
#call motifs with homer again using denovo motifs file homerMotifs.all.motifs
annotatePeaks.pl "${prefix}_peaks.filtered" hg19 -m "${outDir}/denovo/merge/merged.motifs" -nmotifs > ${prefix}_peaks_denovo.bed

echo "-- Calling CTCF motifs. --"
annotatePeaks.pl "${prefix}_peaks.filtered" hg19 -m "${ctcfmotifs}" -nmotifs > ${prefix}_peaks_ctcf.bed

##check this Get the insert size threshold to remove outlier inserts
echo "--- Getting insert size threshold. ---"
java -jar "${jarpath}PEASTools.jar" insertsizethresh "${prefix}_sorted.bam" "${outDir}/peak_features"
thresh=$(cat "thresh.txt")


#Get Insert features
echo "--- Getting insert features. ---"
for i in {1..22}
do
    chr=chr$i
    java -jar "${jarpath}PEASTools.jar" insertmetrics "$chr" "${chr}.bam" "${prefix}_peaks.filtered" "${prefix}_${chr}_insertmetrics.txt" "$thresh"
rm ${chr}.bam
    cat ${prefix}_${chr}_insertmetrics.txt >> ${prefix}_insertmetrics.txt
rm "${prefix}_${chr}_insertmetrics.txt"
done

echo "--- Getting conservation scores. ---"
#Get Conservation Scores
java -jar "${jarpath}PEASTools.jar" conservation "${prefix}_peaks.filtered" "${conservation}" "${prefix}_conservation.txt"

echo "--- Merging features. ---"
#Merge Features #TODO add new features
java -jar "${jarpath}PEASTools.jar" merge "${prefix}_peaks.filtered" "${prefix}_peaks.xls" "${prefix}_peaks_annotated.bed" "${prefix}_insertmetrics.txt" "${prefix}_conservation.txt" "${prefix}_peaks_denovo.bed" "${prefix}_peaks_ctcf.bed" "${prefix}_features_cellspecific.txt"

java -jar "${jarpath}PEASTools.jar" merge "${prefix}_peaks.filtered" "${prefix}_peaks.xls" "${prefix}_peaks_annotated.bed" "${prefix}_insertmetrics.txt" "${prefix}_conservation.txt" "${prefix}_peaks_denovo.bed" "${prefix}_peaks_ctcf.bed" "${prefix}_features_cellagnostic.txt" "MERGED"


