package org.jax.peas.userinterface;

import java.io.IOException;

public class PredictionInformer implements Function {

	private String[] _fp;
	
	public PredictionInformer(String[] fp){
		_fp = fp;
	}
	
	
	@Override
	public String run() throws IOException {
		StringBuilder rvsb = new StringBuilder();
		rvsb.append("Predictions can be located at the following locations: \n");
		for(int i = 0; i < _fp.length; i++){
			rvsb.append(_fp[i]+"\n");
		}

		return rvsb.toString();
	}

}
