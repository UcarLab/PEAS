import pandas as pd
import numpy as np
from sklearn import preprocessing
from sklearn.metrics import accuracy_score, roc_curve, auc, precision_recall_curve,average_precision_score, confusion_matrix
from matplotlib import pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

def getDatasets(fileinformation):
    """Reads dataset information corresponding to
        dataset file location and dataset labels.
        
        Parameters
        ----------
        fileinformation : str
        Path of the file containing the dataset
        metadata.
        
        Returns
        -------
        datalabels : list
        A list of labels corresponding to the datasets.
        
        datafiles : list
        A list of file locations corresponding to the
        datasets.
        """
    filedata = pd.read_csv(fileinformation, sep="\t", header=None)
    datalabels = list(filedata.iloc[:,0].values)
    datafiles = list(filedata.iloc[:,1].values)
    return datalabels, datafiles

def getModelParameters(parameterstring):
    """Creates a mapping of model parameters
        to pass to the model.
        
        Parameters
        ----------
        parameterstring : str
        A string encoding the parameters. Example:
        parameter1=value1, parameter2=value2, ...
        
        Returns
        -------
        rv : dict
        A dictionary containing the parameters
        and values.
        """
    
    def getFormattedValue(strval):
        if '\'' in strval:
            return strval.replace('\'', '')
        elif '"' in strval:
            return strval.replace('"', '')
        elif '.' in strval:
            return float(strval)
        elif strval == 'True':
            return True
        elif strval == 'False':
            return False
        else:
            return int(strval)
    
    ((25,),)
    def parseTuple(strval):
        idx = strval.find("(")+1
        values = []
        i = idx
        while i < len(strval):
            if strval[i] == '(':
                nested, lnested = parseTuple(strval[i:])
                print(i)
                i += lnested
                idx = i+1
                print(i)
                values.append(nested)
            elif strval[i] == ')':
                newval = strval[idx:i].strip()
                if newval != '':
                    values.append(getFormattedValue(newval))
                return tuple(values), i
            elif strval[i] == ',':
                newval = strval[idx:i].strip()
                if newval != '':
                    values.append(getFormattedValue(newval))
                idx = i+1
            i += 1
    
    rv = dict()
    if parameterstring is None:
        return rv
    params = parameterstring.strip().split("=")
    nextkey = params[0]
    for pi in range(1,len(params)):
        cur = params[pi]
        if '(' in cur:
            if cur.count("(") != cur.count(")"):
                raise InvalidParameters("Unequal number of paranthesis.")
            value, _ = parseTuple(cur)
            rv[nextkey] = value
            nextkey = cur[cur.rfind(',')].strip()
        else:
            commasplit = cur.split(",")
            value = commasplit[0].strip()
            rv[nextkey] = getFormattedValue(value)
            nextkey = commasplit[1].strip()
    
    return rv


def getFeatureColumnData(featurefile):
    """Reads the file containing information
        regarding which columns are to be used
        as features.
        
        Parameters
        ----------
        featurefile : str
        Path of the file containing the feature
        column metadata.
        
        Returns
        -------
        features : list
        A list column indices corresponding to the
        features used.
        """
    featurecoldata = pd.read_csv(featurefile, sep="\t", header=None).values
    features = []
    for i in range(0, len(featurecoldata)):
        features.extend(range(featurecoldata[i,0], featurecoldata[i,1]))
    return features

def getClassConversions(classconversionfile):
    """Reads the file containing class label
        transformations used to determine which
        features to use as well as to convert
        string class labels to integers.
        
        Parameters
        ----------
        classconversionfile : str
        Path of the file containing the class
        conversion metadata.
        
        Returns
        -------
        classconversion : array-like, shape (n_classes, 3)
        Returns  class conversions where the first column
        corresponds to the column containing the class label
        information, the second contains which class labels
        to transform and the third column contains the
        integer representation to transform the class label.
        """
    return pd.read_csv(classconversionfile, sep="\t", header=None).values


def getFormattedDirectory(directory):
    """Handles the trailing slash for directory
        inputs.
        
        Parameters
        ----------
        directory : str
        The directory.
        
        Returns
        -------
        rv : str
        the directory with a backslash
        """
    outdir = directory
    if not(outdir.endswith("/")):
        outdir = outdir+"/"
    return outdir

def getLabelEncoder(labelencoderfile):
    """Gets informations for converting features
        to integer values.
        
        Parameters
        ----------
        labelencoderfile : str
        File containg the label encoder information.
        
        Returns
        -------
        rv : list of lists
        Returns a list of lists containing the feature
        column for the conversions (first item) and the
        string labels to convert to integers (remaining).
        """
    labelencoders = []
    ledata = pd.read_csv(labelencoderfile, sep="\t", header=None)
    for i in range(0, len(ledata)):
        cur = ledata.iloc[i,1:].values
        lei = cur.astype(str)
        labelencoders.append(list(lei[lei != 'nan']))
        labelencoders[i].insert(0, ledata.iloc[i,0])
    return labelencoders


def getData(data, featurecols, labelencoder, classconversion=None):
    """Converts PEAS feature files into a feature matrix.
        
        Parameters
        ----------
        data : array-like, shape (n_peaks, n_features)
        A numpy array containing feature data (columns)
        for ATAC-seq peaks (rows).
        
        featurecols : list
        A list of column indices to be extracted into
        the feature matrix.
        
        labelencoder : list
        Contains label information for specific feature
        columns that need to have their string values
        converted into integers.
        
        labelencoders :
        Description
        
        classconversion : array-like or None (default)
        Contains class conversion information for
        converting class labels at the column
        specified into integers starting from 0.
        
        Returns
        -------
        features : array-like, shape (n_peaks, n_features)
        The feature matrix.
        
        classes : list
        The true class labels.
        
        featurelabels : list
        List of feature labels.
        
        ids : array-like, shape(n_peaks, 3)
        The peak location information (chr, start, end).
        It is expected that the first three columns are
        reserved for peak location information.
        """
    for i in range(0, len(labelencoder)):
        le = preprocessing.LabelEncoder()
        le.fit(labelencoder[i][1:])
        data = data.loc[~pd.isnull(data.iloc[:, labelencoder[i][0]]),:]
        data.iloc[:, labelencoder[i][0]] = le.transform(data.iloc[:, labelencoder[i][0]].values)
    
    features = data.iloc[:, featurecols].values
    features = features.astype(float)
    
    featurelabels = data.columns[featurecols].values
    if classconversion is not None:
        classes = np.empty(len(features))
        classes[:] = -1
        for i in range(0, len(classconversion)):
            classcol = np.array(data.iloc[:, classconversion[i,0]].values)
            classes[classcol.astype(str) == str(classconversion[i,1])] = classconversion[i,2]
        classes = classes.astype(int)
        indices = classes[:] > -1
        ids = data.iloc[indices, 0:3]
        
        return features[indices,:], classes[indices], featurelabels, ids
    
    return features, None, featurelabels, data.iloc[:, 0:3]

def plotROC(yscore, true, predtrue, datasets, title, outfile):
    """Generates a receiver operating characteristic
        curve for the given prediction probabilities.
        
        Parameters
        ----------
        yscore : list of lists
        Probability scores.
        
        true : list of lists
        True labels.
        
        datasets : list
        Dataset labels of all datasets tested.
        
        title : str
        The title of the confusion matrix.
        
        outfile : str
        The destination of the .pdf file generated.
        """
    fig = plt.figure()
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.0])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title(title)
    
    for i in range(len(datasets)):
        acc = accuracy_score(true[i], predtrue[i])
        fpr, tpr, _ = roc_curve(true[i], yscore[i][:,1])
        roc_auc = auc(fpr, tpr)
        plt.plot(fpr, tpr, label=datasets[i]+' (area = %0.2f, acc = %0.2f)' % (roc_auc,acc),linewidth=2)
    
    plt.legend(loc="lower right")
    
    pdfplot = PdfPages(outfile);
    pdfplot.savefig(fig)
    pdfplot.close()

def plotPRC(yscore, true, datasets, title, outfile):
    """Generates a precision recall curve for the
        given prediction probabilities.
        
        Parameters
        ----------
        yscore : list of lists
        Probability scores.
        
        true : list of lists
        True labels.
        
        datasets : list
        Dataset labels of all datasets tested.
        
        title : str
        The title of the confusion matrix.
        
        outfile : str
        The destination of the .pdf file generated.
        """
    
    fig = plt.figure()
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.0])
    plt.xlabel('Recall')
    plt.ylabel('Precision')
    plt.title(title)
    
    for i in range(len(datasets)):
        precision, recall, _ =  precision_recall_curve(true[i], yscore[i][:,1])
        prc_auc = average_precision_score(true[i], yscore[i][:,1])
        plt.plot(recall, precision, label=datasets[i]+' (area = %0.2f)' % (prc_auc),linewidth=1)
    
    plt.legend(loc="lower right")
    
    pdfplot = PdfPages(outfile);
    pdfplot.savefig(fig)
    pdfplot.close()

def plotConfusionMatrix(y, pred, title, labels, outfile, cmap=plt.cm.Blues):
    """Generates a confusion matrix from the given predictions.
        
        Parameters
        ----------
        y : list of lists
        The true labels.
        
        pred : list of lists
        The predicted labels.
        
        title : str
        The title of the confusion matrix.
        
        labels : list
        List of class labels corresponding to the
        numeric class labels in ascending order.
        
        outfile : str
        The destination of the .pdf file generated.
        
        cmap : colormap
        Colormap for coloring the confusion matrix.
        """
    
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

def writePredictions(outfile, pred, proba, y, data, evalmode=False):
    """Writes a file containing class predictions.
        
        Parameters
        ----------
        outfile : str
        Path for writing the predictions.
        
        allpred : array-like, shape(npeaks,1)
        Predictions.
        
        allpred : array-like, shape(npeaks,1)
        Prediction probabilities.
        
        ally : list
        List containing true class labels.
        
        alldata : array-like, shape(npeaks,3)
        Contains chr, start, and end information for
        each peak.
        
        evalmode : bool
        Whether or not to include the true class label.
        """
    if evalmode:
        header = ['chr', 'start', 'end', 'prediction', 'true label']
        for i in range(np.shape(proba)[1]):
            header.append("probability:"+str(i))
        pd.DataFrame(np.concatenate((data.values[:,0:3],np.transpose(pred[np.newaxis]).astype(int),np.transpose(y[np.newaxis]), proba), axis=1)[:,:]).to_csv(outfile, sep="\t", index=None, header=header)
    else:
        header = ['chr', 'start', 'end', 'prediction']
        for i in range(np.shape(proba)[1]):
            header.append("probability:"+str(i))
        pd.DataFrame(np.concatenate((data.values[:,0:3],np.transpose(pred[np.newaxis]).astype(int), proba), axis=1)[:,:]).to_csv(outfile, sep="\t", index=None, header=header)
