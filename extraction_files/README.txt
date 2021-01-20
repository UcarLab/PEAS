Filter files for hg19, hg38, and mm10 were obtained from:
https://www.encodeproject.org/annotations/ENCSR636HFF/

Conservation files were obtained from:
conservation_hg19.bed: http://hgdownload.cse.ucsc.edu/goldenpath/hg19/database/phastCons46wayPlacental.txt.gz
conservation_hg38.bed: http://hgdownload.cse.ucsc.edu/goldenpath/hg38/database/phastCons30way.txt.gz
conservation_mm10.bed: http://hgdownload.cse.ucsc.edu/goldenpath/mm10/database/phastCons60wayPlacental.txt.gz

DataRange is used to obtain the maximum conservation observed for the ~1kb window.

To add a new genome add the following files to extraction_files directory in the singularity directory, replacing NEWGENOME with the name of the genome:
chromosomes_NEWGENOME.txt
filter_NEWGENOME.bed
conservation_NEWGENOME.bed
