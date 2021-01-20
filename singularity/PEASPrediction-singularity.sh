#!/bin/bash

ARGUMENTS=()

CLASSES="None"

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --classes) CLASSES="$2"; shift 2 ;;
        --*) echo "Unknown option: $1"; exit 1 ;;
        *)  ARGUMENTS+=("$1"); shift ;;
    esac
done

if [[ ${#ARGUMENTS[@]} -ne 3 ]]; then
    echo "Usage: PEASPrediction-singularity.sh features model outdir"
    echo "features: The extracted features from PEAS."
    echo "model: The PEAS model to predict with. Provide 'promoter' 'enhancer' or a path to a custom model (.pkl file)"
    echo "outdir: The output directory to save the file."
    echo "Options: --classes.   Path to class file. This overrides default usage of /PEAS/models/classes.txt and is only used when model is not using 'promoter' or 'enhancer' ";
    exit 0
fi

FEATURES=${ARGUMENTS[0]}
MODEL=${ARGUMENTS[1]}
OUTDIR=${ARGUMENTS[2]}

if [ "$MODEL" == "promoter" ]; then
    MODELPARAMS="-a ${OUTDIR}/promoter /PEAS/models/promotermodel.pkl"
    PREFIX="promoter"
elif [ "$MODEL" == "enhancer" ]; then
    MODELPARAMS="-c /PEAS/models/classes.txt /PEAS/models/enhancermodel.pkl"
    PREFIX="enhancer"
else
    if [ "$CLASSES" == "None" ]; then
        MODELPARAMS="${MODEL}"
    else
        MODELPARAMS="-c ${CLASSES} ${MODEL}"
    fi
    
    PREFIX="${MODEL##*/}"
    PREFIX="${PREFIX%.*}"
fi

LABEL="${FEATURES##*/}"
LABEL="${LABEL%.*}"

TEMPFILE=${OUTDIR}/TMP_PEAS_${PREFIX}_FILELIST.txt

echo -e "${LABEL}\t${FEATURES}" > ${TEMPFILE}

python /PEAS/PEASPredictor.py -o "${OUTDIR}" -p ${PREFIX} -f /PEAS/models/features.txt -l /PEAS/models/labelencoder.txt ${MODELPARAMS} "${TEMPFILE}"

rm ${TEMPFILE}

echo "Predictions saved to ${OUTDIR}/${PREFIX}_${LABEL}_predictions.txt"
