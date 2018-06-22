import sys
import pandas as pd
import numpy as np
import PEASUtil
from sklearn import preprocessing
from sklearn.externals import joblib
import argparse
import os

wd = os.getcwd()

parser = argparse.ArgumentParser(description='Loads a model and makes predictions on ATAC-Seq peaks.')
parser.add_argument('modelfile', type=str, help='The filepath for the saved model (*.pkl).')
parser.add_argument('featurefiles', type=str, help='File listing the file paths of all features to train the model.')

parser.add_argument('-o', dest='out', type=str, help='The selected directory saving outputfiles.')
parser.add_argument('-p', dest='prefix', type=str, help='Prefix for saving files.')
parser.add_argument('-f', dest='features', help='Feature index file specifying which columns to include in the feature matrix.', type=str)
parser.add_argument('-c', dest='classes', type=str,
                    help='File containing class label transformations into integer representations. Also used for filtering peaks based on classes.')
parser.add_argument('-l', dest='labelencoder', help='File containing feature label transformations into integer representations.', type=str)
parser.add_argument('-e', dest='evalmode', action='store_true',
                    help='Whether or not to compare predictions with provided class labels.')

args = parser.parse_args()

modelfile = args.modelfile
datasetlabels, datasetfiles = PEASUtil.getDatasets(args.featurefiles)

#Optional Arguments
featurefiledirectory = os.path.dirname(args.featurefiles)
featurefilename = os.path.splitext(os.path.basename(args.featurefiles))[0]

if args.prefix is not None:
    prefix = args.prefix
    prefixfile = args.prefix.replace(" ", "_")
else:
    prefix = featurefilename
    prefixfile = featurefilename.replace(" ", "_")

if args.out is not None:
    outdir = PEASUtil.getFormattedDirectory(args.out)
else:
    outdir = PEASUtil.getFormattedDirectory(featurefiledirectory)

if args.features is not None:
    featurecolumns = PEASUtil.getFeatureColumnData(args.features)
else:
    featurecolumns = PEASUtil.getFeatureColumnData(wd+"/features.txt")

evalmode = args.evalmode
if args.classes is not None:
    classconversion = PEASUtil.getClassConversions(args.classes)
elif evalmode:
    classconversion = PEASUtil.getClassConversions(wd+"/classes.txt")
else:
    classconversion = None

if args.labelencoder is not None:
    labelencoder = PEASUtil.getLabelEncoder(args.labelencoder)
else:
    labelencoder = PEASUtil.getLabelEncoder(wd+"/labelencoder.txt")


#loading model
print("Loading model.")
clf = joblib.load(modelfile)

#make predictions
print("Reading features and making predictions.")
allproba = []
ally = []
allpred = []
alldata = []
imputer = preprocessing.Imputer(missing_values='NaN', strategy='mean', axis=0)

for i in range(0,len(datasetfiles)):
    curdatalabel = datasetlabels[i]
    curfile = datasetfiles[i]
    curdata = pd.read_csv(curfile, sep="\t")
    testX, testy, _, data = PEASUtil.getData(curdata, featurecolumns, labelencoder, classconversion)
    testX = preprocessing.StandardScaler().fit_transform(imputer.fit_transform(testX))
    allproba.append(clf.predict_proba(testX))
    allpred.append(clf.predict(testX))
    ally.append(testy)
    alldata.append(data)
    PEASUtil.writePredictions(outdir+prefixfile+"_"+curdatalabel+"_predictions.txt", allpred[i], allproba[i], ally[i], alldata[i], evalmode)
    if evalmode:
        PEASUtil.plotConfusionMatrix(testy, allpred[-1], curdatalabel, np.unique(testy), outdir+prefixfile+"_"+curdatalabel+"_Confusion.pdf")

if evalmode:
    print("Plotting ROC/PRC AUC curves.")
    PEASUtil.plotROC(allproba, ally, allpred, datasetlabels, prefix, outdir+prefixfile+"_ROC.pdf")
    PEASUtil.plotPRC(allproba, ally, datasetlabels, prefix, outdir+prefixfile+"_PRC.pdf")

print("Complete.")
