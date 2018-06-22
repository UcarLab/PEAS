import sys
import pandas as pd
import numpy as np
import argparse
import os

wd = os.getcwd()

parser = argparse.ArgumentParser(description='Annotates a feature file using a prediction file.')
parser.add_argument('featurefile', type=str, help='Feature file.')
parser.add_argument('predictionfile', type=str, help='Prediction file.')

args = parser.parse_args()

features = pd.read_csv(args.featurefile, sep="\t")
headercolumns = list(features.columns)
features = features.values
predictions = pd.read_csv(args.predictionfile, sep="\t").values

predictmap = dict()
for i in range(0, len(predictions)):
    key = predictions[i,0]+":"+str(predictions[i,1])+"-"+str(predictions[i,2])
    predictmap[key] = predictions[i,3]

annvect = np.ones((len(features),1))*-1
for i in range(0, len(features)):
    key = features[i,0]+":"+str(features[i,1])+"-"+str(features[i,2])
    try:
        annvect[i,0] = predictmap[key]
    except:
        pass

headercolumns.append("Class Annotation")
afeatures = np.concatenate((features, annvect.astype(int)), axis=1)

pd.DataFrame(afeatures,columns=headercolumns).to_csv(args.featurefile+".annotated.txt", sep="\t", index=None)