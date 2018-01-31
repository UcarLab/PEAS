import sys
import pandas as pd
import numpy as np
from sklearn import preprocessing
from sklearn.metrics import accuracy_score,roc_curve, auc, precision_recall_curve,average_precision_score, confusion_matrix
from sklearn.preprocessing import label_binarize
from matplotlib import pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
args = sys.argv


def getData(data, featurecols, classconversion, labelencoders, classlabels=True):
    #Replace String features with numeric values
    for i in range(0, len(labelencoders)):
        le = preprocessing.LabelEncoder()
        le.fit(labelencoders[i][1:])
        data = data.loc[~pd.isnull(data.iloc[:, labelencoders[i][0]]),:]
        data.iloc[:, labelencoders[i][0]] = le.transform(data.iloc[:, labelencoders[i][0]].values)

    #select the appropriate columns for the features
    features = data.iloc[:, featurecols].values
    features = features.astype(float)
    
    featurelabels = data.columns[featurecols].values
    if classlabels:
        #set classes
        classes = np.empty(len(features))
        classes[:] = -1
        for i in range(0, len(classconversion)):
            classcol = data.iloc[:, classconversion[i,0]].values
            classes[classcol == classconversion[i,1]] = classconversion[i,2]
        classes = classes.astype(int)
    
        ids = data.iloc[classes[:] > -1, 0:3]
    
        #Return Features Classes and Labels, Removing Unclassified (-1)
        #return [features[classes[:] > -1,:], classes[classes[:] > -1], featurelabels, ids, data.iloc[classes[:] > -1, classconversion[0,0]].values]
        return [features[classes[:] > -1,:], classes[classes[:] > -1], featurelabels, ids, data.iloc[classes[:] > -1, classconversion[0,0]].values, data.iloc[classes[:] > -1, (classconversion[0,0]+1)]]#test#
    
    return [features, [], featurelabels, data.iloc[:, 0:5], []]

def getThresh(tdata, thresh):
    peakscores = tdata['Peak Score']
    l = len(peakscores)
    idx = np.argsort(peakscores)
    speaks = peakscores[idx]
    checkidx = int(l*thresh)
    return speaks.values[checkidx]

def filterData(tdata, thresh):
    if(thresh <= 0):
        return tdata
    return tdata.iloc[np.argwhere(tdata['Peak Score'].values > getThresh(tdata, thresh))[:,0],:]
    

def plotMicroROC(yscore, true, predtrue, n_classes, datasets, outfile, title):
    fig = plt.figure()
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.0])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title(title)

    for i in range(len(datasets)):
        newtrue = label_binarize(true[i], classes=range(0,n_classes))
        acc = accuracy_score(true[i], predtrue[i])
        fpr, tpr, _ = roc_curve(newtrue.ravel(), np.nan_to_num(yscore[i].ravel()))
        roc_auc = auc(fpr, tpr)
        plt.plot(fpr, tpr, label=datasets[i]+' (area = %0.2f, acc = %0.2f)' % (roc_auc,acc),linewidth=2)
    
    plt.legend(loc="lower right")

    pdfplot = PdfPages(outfile);
    pdfplot.savefig(fig)
    pdfplot.close()
    
def plotMicroPRC(yscore, true, n_classes, datasets, outfile, title):
    fig = plt.figure()
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.0])
    plt.xlabel('Recall')
    plt.ylabel('Precision')
    plt.title(title)

    for i in range(len(datasets)):
        newtrue = label_binarize(true[i], classes=range(0,n_classes))
        precision, recall, _ =  precision_recall_curve(newtrue.ravel(), np.nan_to_num(yscore[i].ravel()))
        prc_auc = average_precision_score(newtrue, yscore[i],average="micro")
        plt.plot(recall, precision, label=datasets[i]+' (area = %0.2f)' % (prc_auc),linewidth=1)
    
    plt.legend(loc="lower right")

    pdfplot = PdfPages(outfile);
    pdfplot.savefig(fig)
    pdfplot.close()

def MultiMCCCov(x,y, N):
    S = np.shape(x)[0]
    val = 0
    for n in range(0,N):
        xmean = np.mean(x[:,n])
        ymean = np.mean(y[:,n])
        for s in range(0,S):
            val += (x[s,n]-xmean)*(y[s,n]-ymean)
    return float(1.0/N)*val
    
def multiclassMCC(y, pred, nclasses):
    ymat = np.zeros((len(y),nclasses))
    xmat = np.zeros((len(pred),nclasses))
    for i in range(0, len(y)):
        ymat[i,y[i]] = 1
        xmat[i,int(pred[i])] = 1
    return MultiMCCCov(xmat,ymat,nclasses)/np.sqrt(MultiMCCCov(xmat,xmat,nclasses)*MultiMCCCov(ymat,ymat,nclasses))##/np.sqrt(np.cov(xmat,xmat)*np.cov(ymat,ymat))

def plotConfusionMatrix(y, pred, title,labels,outfile, cmap=plt.cm.Blues):
    cm = confusion_matrix(y,  pred);
    ncm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
    accuracy = accuracy_score(y,  pred)    
    
    fig = plt.figure(figsize=(10, 10))
    plt.imshow(ncm, interpolation='nearest', cmap=cmap, vmin=0, vmax=1)
    plt.title(title+" Acc: "+str(accuracy)+")")
    plt.colorbar()
    for i in range(0,len(labels)):
        for j in range(0,len(labels)):
            plt.text(j,i,cm[i,j],va='center',ha='center')
    tick_marks = np.arange(len(labels))
    plt.xticks(tick_marks, labels, rotation=45)
    plt.yticks(tick_marks, labels)
    plt.tight_layout()
    plt.ylabel('True label')
    plt.xlabel('Predicted label')
    pdfplot = PdfPages(outfile);
    pdfplot.savefig(fig)
    pdfplot.close()