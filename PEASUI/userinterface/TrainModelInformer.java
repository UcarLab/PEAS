package org.jax.peas.userinterface;

import java.io.IOException;

public class TrainModelInformer implements Function {

	private String _fp;
	
	public TrainModelInformer(String fp){
		_fp = fp;
	}
	
	
	@Override
	public String run() throws IOException {
		StringBuilder rvsb = new StringBuilder();
		rvsb.append("Model can be located at the following location: \n");
		rvsb.append(_fp+"\n");
		return rvsb.toString();
	}

}
