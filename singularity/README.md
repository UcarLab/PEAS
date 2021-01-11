# Building the singularity image from .def file #

Use the PEAS-singularity.def to build a new .sif file using the following command:

`singularity build PEAS-singularity.sif PEAS-singularity.def`

# Finalizing the singularity image for usage #

The SIF is not installed with hg19 for HOMER and must be converted to a sandbox first:

1. Convert the SIF file to a sandbox:
`singularity build --sandbox ~/PEAS-singularity-hg19/ PEAS-singularity.sif`
2. Enter the sandbox:
`singularity shell --writable ~/PEAS-singularity-hg19/`
3. Install hg19 for Homer:
`perl /HOMER/configureHomer.pl -install hg19`

Steps 2 and 3 can be used to install other genomes.

** Use this sandbox directory to run PEAS. **


## Running PEAS with singularity ##

To run PEAS via command line, use the following steps after entering the sandbox directory:

`singularity shell ~/PEAS-singularity-hg19/`

### Typical usage ###

**Step 1: Feature Extraction**

    /PEAS/PEASFeatureExtraction.sh <PathToBamFileDirectory> <BamFileName> <OutputDirectory> <PathToHg19.fa> hg19 /PEAS/extraction_files/hg19.filter.bed /PEAS/extraction_files/humantop_Nov2016_HOMER.motifs /PEAS/extraction_files/phastCons46wayPlacental.bed /PEAS/extraction_files/CTCF.motifs /PEAS/

Use the above command after replacing the following:

 - `<PathToBamFileDirectory>`The file path to the directory containing the bam file 
 - `<BamFileName>` The name of the bam file (everything before .bam)
 - `<OutputDirectory>` The output directory
 - `<PathToHg19.fa>` The file path to the hg19.fa reference genome

**Step 2: Promoter Prediction**
 
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

**Step 3: Enhancer Prediction**
 
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
