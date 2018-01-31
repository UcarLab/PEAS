# PEAS (Predict Enhancers from ATAC-seq)

## Getting started
Please refer to the Manual (PEAS_Manual.pdf) for installing dependencies and information on how to run feature extraction and prediction scripts.


## Feature Extraction Files

* **PEASFeatureExtraction.sh** - Core shell script to extract features from aligned paired-end ATAC-seq data (.bam)
* **PEASTools.jar** - Required JAR file used by the shell script.  Source for this JAR file is available in the PEASTools directory.
* **hg19.filter.bed** - BED file for filtering out error prone regions in the hg19 reference genome.
* **humantop_Nov2016_HOMER.motifs** - List of known motif PWMs.
* **CTCF.motifs** - CTCF PWMs.
* **phastCons46wayPlacental.bed** - Conservation BED files.

## Prediction Files

* **PEASPredictor.py** - Core python file used for training a neural network model and making predictions.
* **PEAS_util.py** - Utility code used by the prediction python script.
* **trainfiles.txt** - Example file for listing the feature matrices to be used for model training.
* **testfiles.txt** - Example file for listing the feature matrices to be used for prediction.
* **classes_train.txt** - Example file for designating training class labels to integer labels.  1. Feature Column 2.Label in file 3. Integer Label to be Assigned
* **classes_test.txt** - Example file for designating testing class labels to integer labels.  1. Feature Column 2.Label in file 3. Integer Label to be Assigned
* **features.txt** - File selecting columns from the feature matrices. 1. Start column (inclusive) 2. End column (exclusive), multiple lines are allowed.
* **labelencoder.txt** - Specifies the non-numeric columns and values in the feature matrices that need to be converted to integers.