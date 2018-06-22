import sys
import pandas as pd
import numpy as np
import PEASUtil
from sklearn.neural_network import MLPClassifier
from sklearn import preprocessing
from sklearn.externals import joblib
import argparse
import os

wd = os.getcwd()

#argument parsing
parser = argparse.ArgumentParser(description='Trains a multi-layer perceptron neural network model for ATAC-seq peak data.')
parser.add_argument('featurefiles', type=str, help='File listing the file paths of all features to train the model.')

parser.add_argument('-o', dest='out', type=str, help='The selected directory saving outputfiles.')
parser.add_argument('-n', dest='name', type=str, help='Name of the Model.')
parser.add_argument('-p', dest='paramstring', help='String containing the parameters for the model.', type=str)
parser.add_argument('-f', dest='features', help='Feature index file specifying which columns to include in the feature matrix.', type=str)
parser.add_argument('-c', dest='classes', help='File containing class label transformations into integer representations.', type=str)
parser.add_argument('-l', dest='labelencoder', help='File containing feature label transformations into integer representations.', type=str)
parser.add_argument('-r', dest='randomstate', help='Integer for setting the random number generator seed.', type=int, default=929)

args = parser.parse_args()

#Required Arguments
datasetlabels, datasetfiles = PEASUtil.getDatasets(args.featurefiles)

#Optional Arguments
featurefiledirectory = os.path.dirname(args.featurefiles)
featurefilename = os.path.splitext(os.path.basename(args.featurefiles))[0]

if args.name is not None:
    modelname = args.name
    modelnamefile = args.name.replace(" ", "_")
else:
    modelname = featurefilename
    modelnamefile = featurefilename.replace(" ", "_")

if args.out is not None:
    outdir = PEASUtil.getFormattedDirectory(args.out)
else:
    outdir = PEASUtil.getFormattedDirectory(featurefiledirectory)


parameters = PEASUtil.getModelParameters(args.paramstring)

if args.features is not None:
    featurecolumns = PEASUtil.getFeatureColumnData(args.features)
else:
    featurecolumns = PEASUtil.getFeatureColumnData(wd+"/features.txt")

if args.classes is not None:
    classconversion = PEASUtil.getClassConversions(args.classes)
else:
    classconversion = PEASUtil.getClassConversions(wd+"/classes.txt")

if args.labelencoder is not None:
    labelencoder = PEASUtil.getLabelEncoder(args.labelencoder)
else:
    labelencoder = PEASUtil.getLabelEncoder(wd+"/labelencoder.txt")

randomstate = args.randomstate
parameters['random_state'] = randomstate

#Model Training
imputer = preprocessing.Imputer(missing_values='NaN', strategy='mean', axis=0)
trainX = np.zeros((0,len(featurecolumns)))
trainy = np.zeros((0,))
print("Reading feature files")
for curfile in datasetfiles:
    curdata = pd.read_csv(curfile, sep="\t")
    trainXi, trainyi, _, _, = PEASUtil.getData(curdata, featurecolumns, labelencoder, classconversion)
    trainXi = preprocessing.StandardScaler().fit_transform(imputer.fit_transform(trainXi))
    trainX = np.concatenate((trainX, trainXi))
    trainy = np.concatenate((trainy, trainyi))
print("Training Model")
clf = MLPClassifier(**parameters)
print(clf)
clf.fit(trainX,trainy)

outfile = outdir+modelnamefile+'.pkl'
print("Writing model to: "+outfile)
joblib.dump(clf, outfile)
print("Complete.")


