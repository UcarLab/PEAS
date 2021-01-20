# Building the singularity image from .def file (if not using prebuilt .sif file)#

Use the PEAS-singularity.def to build a new .sif file using the following command:

`singularity build PEAS-singularity.sif PEAS-singularity.def`

# Finalizing the singularity image for usage #

The SIF is not installed with hg19 for HOMER and must be converted to a sandbox first:

1. Convert the SIF file to a sandbox:
`singularity build --sandbox ./PEAS-singularity-hg19/ PEAS-singularity.sif`
2. Enter the sandbox:
`singularity shell --writable ./PEAS-singularity-hg19/`
3. Install hg19 for Homer:
`perl /HOMER/configureHomer.pl -install hg19`

Steps 2 and 3 can be used to install other genomes. Supporting files for hg38 and mm10 are included. Other genomes will require a chromosome list, conservation score, and region filter files (see extraction_files directory).

** Use this sandbox directory to run PEAS. **

### Basic Usage ###

**Feature Extraction & Predicting**
To extract features and predict promoters and enhancers, use the singularity run command:

    singularity run ./PEAS-singularity-hg19/ <bamfile> <genome> <fastapath> <outdir>
    
Use the above command after replacing the following:

 - `<bamfile>`The file path to the bam file
 - `<genome>` The genome to use (i.e., hg19)
 - `<fastapath>` The filepath to the genome's reference sequence (.fa)
 - `<outdir>` The output directory
 
 **Feature Extraction Only**
 
To only extract features, enter the image directory using the singularity shell command:

    singularity shell ./PEAS-singularity-hg19/
    
Once in the directory run the shell script:

    /PEAS/singularity/PEASFeatureExtraction-singularity.sh <bamfile> <genome> <fastapath> <outdir>
    
after replacing the following:

 - `<bamfile>`The file path to the bam file
 - `<genome>` The genome to use (i.e., hg19)
 - `<fastapath>` The filepath to the genome's reference sequence (.fa)
 - `<outdir>` The output directory    
 
 **Promoter/Enhancer Prediction Only**

To predict promoters/enhancers from already generated features, enter the image directory using the singularity shell command:

    singularity shell ./PEAS-singularity-hg19/
    
Once in the directory run the shell script:

    /PEAS/singularity/PEASPrediction-singularity.sh <featurefile> <model> <outdir>
    
after replacing the following:

 - `<featurefile>` The file path to the feature file (file ending with features.txt)
 - `<model>` The model to use. This can either be promoter, enhancer, or a path to a .pkl file of a custom model trained using this pipeline
 - `<outdir>` The output directory
 
The class file can be changed using `--classes <classfile>`.  By default /PEAS/models/classes.txt is used. This file specifies 1) the column of the feature file that labels the classes of each peak, 2) The class label to substitute for and 3) the numeric value to substitute the values found in 2.  The integer values are 0 (negative class) 1 (positive class) -1 (ignored/removed).  This file is used for enhancer prediction after promoters are identified so as to remove promoters previously predicted and focus on enhancer vs. other prediction.
  


### Advanced Usage ###
This section covers the use of the shell scripts and python scripts used by the above wrapper scripts.  They may be useful for customizing the information used by these methods. For typical usage we strongly recommend using the commands in the previous section.

To run PEAS via command line, use the following steps after entering the sandbox directory:

`singularity shell ./PEAS-singularity-hg19/`

**Feature Extraction**

    /PEAS/PEASFeatureExtraction.sh <PathToBamFileDirectory> <BamFileName> <OutputDirectory> <PathToHg19.fa> hg19 /PEAS/extraction_files/hg19.filter.bed /PEAS/extraction_files/humantop_Nov2016_HOMER.motifs /PEAS/extraction_files/phastCons46wayPlacental.bed /PEAS/extraction_files/CTCF.motifs /PEAS/

Use the above command after replacing the following:

 - `<PathToBamFileDirectory>`The file path to the directory containing the bam file 
 - `<BamFileName>` The name of the bam file (everything before .bam)
 - `<OutputDirectory>` The output directory
 - `<PathToHg19.fa>` The file path to the hg19.fa reference genome

**Promoter Prediction**
 
Predicting promoters requires 2 steps:
 
*Promoter Prediction Step 1:* Create a file labeling the dataset and providing the full path to the feature extraction file.
 
The file should look similar to:
 
    <label>	<PathToFeatureExtraction>
 
Using a tab to separate the label and feature file path.
 
*Promoter Prediction Step 2:* Use the file created in step 1 to predict promoters using the promoter model.
 
    python /PEAS/PEASPredictor.py -o "<PathToOutputDirectory>" -p promoter -f /PEAS/models/features.txt -l /PEAS/models/labelencoder.txt -a "<PathToOutputDirectory>/promoter" /PEAS/models/promotermodel.pkl "<PathToFeatureFilelist>"
    
Use the above command after replacing the following:

 - `<PathToOutputDirectory>`The output directory to save the predictions
 - `<PathToFeatureFilelist>` The path to the feature file list created in *Promoter Prediction Step 1*

**Enhancer Prediction**
 
Similar to promoter prediction, predicting enhancers requires 2 steps:
 
*Enhancer Prediction Step 1:* Create a file labeling the dataset and providing the full path to the feature file annotated with promoters, which can be found in the output directory with `promoters.annotated.txt` suffix.
 
The file should look similar to:
 
    <label>	<PathToFeatureExtraction>
 
Using a tab to separate the label and feature file path.

*Enhancer Prediction Step 2:* Use the file created in step 1 to predict enhancers using the enhancer model.
 
    python /PEAS/PEASPredictor.py -o "<PathToOutputDirectory>" -p enhancer -f /PEAS/models/features.txt -l /PEAS/models/labelencoder.txt -c /PEAS/models/classes.txt /PEAS/models/enhancermodel.pkl "<PathToFeatureFilelist>"

Use the above command after replacing the following:

 - `<PathToOutputDirectory>`The output directory to save the predictions
 - `<PathToFeatureFilelist>` The path to the feature file list created in *Enhancer Prediction Step 1*
