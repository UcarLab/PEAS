package org.jax.peas.userinterface;

import java.io.IOException;

public class AnnotationInformer implements Function {

	private String[] _fp;
	
	public AnnotationInformer(String[] fp){
		_fp = fp;
	}
	
	
	@Override
	public String run() throws IOException {
		StringBuilder rvsb = new StringBuilder();
		rvsb.append("Annotated features can be located at the following location: \n");
		for(int i = 0; i < _fp.length; i++){
			rvsb.append(_fp[i]+"\n");
		}

		return rvsb.toString();
	}

}
