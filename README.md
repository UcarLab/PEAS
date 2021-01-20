# PEAS (Predict Enhancers from ATAC-seq) #

## NEW: Singularity image file (.sif) ##
The latest release (v1.2) provides a singularity image file and definition. Documentation for using this image can be found in the /singularity/ folder. This image sets up the environment to successfully run PEAS (after configuring the reference genomes) while reducing the number of user inputs for making promoter & enhancer predictions.

*Note: Please have enough disk space for 5x the size of the input bamfile.*

## Requirements & Dependencies ##

1. Bash (can execute shell scripts)
2. Java version 1.8.0_171 or more recent (https://java.com/en/download/)
3. SAMTools (https://github.com/samtools/samtools/releases)
4. MACS2 (https://github.com/taoliu/MACS)
5. HOMER (http://homer.ucsd.edu/homer/)
7. Python (https://www.python.org/downloads/) with the following libraries:
 - numpy
 - pandas
 - sklearn
 - matplotlib

```sh
pip install numpy pandas scikit-learn matplotlib

conda install --upgrade numpy pandas scikit-learn matplotlib
```

Please ensure the following commands are available in terminal:
1. java -jar
2. samtools
3. macs2
4. findMotifsGenome.pl
5. annotatePeaks.pl

Note: python can be configured in the PEAS GUI.


## Running PEAS ##
To run PEAS, download and extract the latest PEAS zip file (https://github.com/UcarLab/PEAS/releases) and run the PEAS.jar file either by double clicking or by running it in the command line: java -jar PEAS.jar (Requires Java 1.8.0_171, https://java.com/en/download/), 

Please refer to the Manual (PEASManual.pdf) for installing dependencies and for further information on how to run feature extraction and prediction scripts.