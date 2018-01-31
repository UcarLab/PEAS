import sys
import numpy as np
import pandas as pd
from sklearn.neural_network import MLPClassifier
from sklearn import preprocessing
from sklearn.pipeline import Pipeline

import PEAS_util

args = sys.argv

#arg1: File containing list of files to process (two column label, filepath)
filedata = pd.read_csv(args[1], sep="\t", header=None)
flabels = filedata.iloc[:,0].values
print(flabels)
trainfiles = filedata.iloc[:,1].values

#arg2: File for feature columns (two column specifying ranges [start,end) )
featurecoldata = pd.read_csv(args[2], sep="\t", header=None).values
features = []
for i in range(0, len(featurecoldata)):
    features.extend(range(featurecoldata[i,0], featurecoldata[i,1]))

#arg3: File for classes (three column: column, value, conversion) 
#conversion must be 0, 1, or -1 where -1 means the data element is not used
classconversion = pd.read_csv(args[3], sep="\t", header=None).values #promoter vs enhancer/other

#arg4: Random State
rstate = int(args[4])

#arg5: output directory for the model
outdir = args[5]

#arg6: File for string feature conversions (Col1: Feature column, Subsequent columns, String values) 
labelencoders = []
ledata = pd.read_csv(args[6], sep="\t", header=None)
for i in range(0, len(ledata)):
    cur = ledata.iloc[i,1:].values
    lei = cur.astype(str)
    labelencoders.append(list(lei[lei != 'nan']))
    labelencoders[i].insert(0, ledata.iloc[i,0])
    

if not(outdir.endswith("/")):
    outdir = outdir+"/"


filedata = pd.read_csv(args[7], sep="\t", header=None)
flabels = filedata.iloc[:,0].values
print(flabels)
testfiles = filedata.iloc[:,1].values

testclassconversion = pd.read_csv(args[8], sep="\t", header=None).values

#TODO load saved model if one is available
imputer = preprocessing.Imputer(missing_values='NaN', strategy='mean', axis=0)
scaler = preprocessing.StandardScaler()

la =  MLPClassifier(random_state=rstate, verbose=True)#hidden_layer_sizes=(50, 25),activation='tanh',alpha=0.0001,beta_1=0.9, beta_2=0.9999, epsilon=0.00000001,random_state=rstate,learning_rate_init=0.001, verbose=True)
clf = Pipeline(steps=[('la', la)])

allproba = dict()
ally = dict()
allpred = dict()
alldata = dict()

trainX = np.zeros((0,1627))
trainy = np.zeros(0)

trainXtup = ()
trainytup = ()
for i in range(0, len(trainfiles)):
    trainXi, trainyi, _, _,_,freq = PEAS_util.getData(PEAS_util.filterData(pd.read_csv(trainfiles[i], sep="\t"), 0.0), features, classconversion, labelencoders)
    trainXi = preprocessing.StandardScaler().fit_transform(imputer.fit_transform(trainXi)) #test#
    idx = list(np.transpose(np.argwhere(freq >= 0.5))[0])
    trainXi = trainXi[idx,:]
    trainyi = trainyi[idx]
    trainXtup = trainXtup+(trainXi,)
    trainytup = trainytup+(trainyi[np.newaxis],)


trainy = np.concatenate((trainytup), axis=1)[0]
trainX = np.concatenate((trainXtup), axis=0)
trainXtup = False
trainytup = False


clf.fit(trainX, trainy)

numclasses = len(np.unique(trainy))
def writepredictions(allpred, allproba, ally, alldata, flabels):
    for i in range(0, len(flabels)):
        print(np.shape(alldata[i]))
        print(np.shape(allpred[i]))
        print(np.shape(ally[i]))
        print(np.shape(allproba[i]))
        pd.DataFrame(np.concatenate((alldata[i],np.transpose(allpred[i][np.newaxis]),np.transpose(ally[i][np.newaxis]), allproba[i]), axis=1)[:,:]).to_csv(outdir+flabels[i]+"_predictions.txt", sep="\t", index=None, header=None)

for i in range(0,len(testfiles)):
    testX, testy, _, ids,_,freq = PEAS_util.getData(PEAS_util.filterData(pd.read_csv(testfiles[i], sep="\t"), 0.0), features, testclassconversion, labelencoders)
    testX = preprocessing.StandardScaler().fit_transform(imputer.fit_transform(testX))#test# 
    testX  = testX[:,:]
    ctestX = testX
    allproba[i] = clf.predict_proba(ctestX)
    allpred[i] = clf.predict(ctestX)
    ally[i] = testy
    alldata[i] = ids.loc[:,:]
PEAS_util.plotMicroROC(allproba, ally, allpred, numclasses,flabels, outdir+"ROC.pdf", "ROC")
PEAS_util.plotMicroPRC(allproba, ally, numclasses,flabels, outdir+"PRC.pdf", "Precision Recall")
writepredictions(allpred, allproba, ally, alldata, flabels)


for i in range(0, len(allpred)):
    PEAS_util.plotConfusionMatrix( ally[i], allpred[i], flabels[i], range(0,numclasses),outdir+flabels[i]+"_confusion.pdf")
    