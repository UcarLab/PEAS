import sys
import pandas as pd
import numpy as np
import PEASUtil
import argparse
import os

wd = os.getcwd()

parser = argparse.ArgumentParser(description='Identifies promoters based on distance to TSS.')
parser.add_argument('featurefile', type=str, help='Feature file for identifying promoters.')

parser.add_argument('-c', dest='column', type=int, default=24, help='Index column containing the distance to TSS.')
parser.add_argument('-u', dest='upstreamtss', type=int, default=2000, help='The upstream distance threshold from the TSS. Default: 2000')
parser.add_argument('-d', dest='downstreamtss', type=int, default=2000, help='The downstream distance threshold from the TSS. Default: 2000')
parser.add_argument('-t', dest='dest', type=str, help='The promoter prediction file destination.')

args = parser.parse_args()

inputfile = pd.read_csv(args.featurefile, sep="\t", header=None).values
dtssidx = args.column
downthresh = abs(int(args.downstreamtss))
upthresh = abs(int(args.upstreamtss))

if args.dest is not None:
    dest = args.dest
else:
    dest = args.featurefile+".promoterprediction.txt"

def getPromoterPredictions(dataset, dtssidx, upthresh, downthresh):
    rv = []
    for i in range(0, len(dataset)):
        chr = dataset[i,0]
        start = dataset[i,1]
        end = dataset[i,2]
        try:
            dtss = int(dataset[i,dtssidx])
            if dtss < 0:
                rv.append([chr, start, end, int(abs(dtss) <= upthresh)])
            elif dtss > 0:
                rv.append([chr, start, end, int(dtss <= downthresh)])
            else:
                rv.append([chr, start, end, 1])
        except:
            pass
    return np.array(rv)

predictions = getPromoterPredictions(inputfile, dtssidx, downthresh, upthresh)

pd.DataFrame(predictions, columns=["chr", "start", "end", "promoter prediction"]).to_csv(dest, sep="\t", index=None)
print("Finished annotating promoters.")