package org.jax.peas.userinterface.inputvalidation;

public class FeaturePrediction {
	
	private String _featurefile;
	private String _predictionfile;
	
	public FeaturePrediction(String ff, String pf){
		_featurefile = ff;
		_predictionfile = pf;
	}
	
	public String getFeatureFile(){
		return _featurefile;
	}
	
	public String getPredictionFile(){
		return _predictionfile;
	}
	
}