package org.jax.peas.userinterface;

import java.io.IOException;

import org.jax.peas.userinterface.inputvalidation.FeaturePrediction;

public class PredictionInformer implements Function {

	private FeaturePrediction[] _fp;
	
	public PredictionInformer(FeaturePrediction[] fp){
		_fp = fp;
	}
	
	
	@Override
	public String run() throws IOException {
		StringBuilder rvsb = new StringBuilder();
		rvsb.append("Predictions can be located at the following locations: \n");
		for(int i = 0; i < _fp.length; i++){
			rvsb.append(_fp[i].getPredictionFile()+"\n");
		}

		return rvsb.toString();
	}

}
