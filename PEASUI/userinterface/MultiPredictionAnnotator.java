package org.jax.peas.userinterface;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MultiPredictionAnnotator implements Function {

	private PredictionAnnotator[] _annotators;
	private String[] _annotationfiles;
	
	public MultiPredictionAnnotator(PredictionAnnotator[] annotators){
		_annotators = annotators;
		_annotationfiles = new String[_annotators.length];
		for(int i = 0; i < _annotators.length; i++){
			_annotationfiles[i] = _annotators[i].getOutputFile();
		}
	}
	
	@Override
	public String run() throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < _annotators.length; i++){
			sb.append(_annotators[i].run());
			_annotationfiles[i] = _annotators[i].getOutputFile();
		}
		
		return sb.toString();
	}
	
	public String getPredictionFile(String outdir, String suffix){
		if(_annotationfiles.length > 1){
			List<ListFile> l = new LinkedList<ListFile>();
			for(int i = 0; i < _annotationfiles.length; i++){
				String f = _annotationfiles[i];
				l.add(new ListFile(FeatureFileUtility.getFileName(f), f));
			}
			FeatureFileUtility.writeFeatureFile(l, "all_predictions", suffix, outdir);
		}
		else if(_annotationfiles.length == 1){
			return _annotationfiles[0];
		}
		return "";
	}

}
