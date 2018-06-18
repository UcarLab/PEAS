package org.jax.peas.userinterface;

import java.io.IOException;

public class AnnotationInformer implements Function {

	private String _fp;
	
	public AnnotationInformer(String fp){
		_fp = fp;
	}
	
	
	@Override
	public String run() throws IOException {
		StringBuilder rvsb = new StringBuilder();
		rvsb.append("Annotated features can be located at the following location: \n");
			rvsb.append(_fp+"\n");

		return rvsb.toString();
	}

}
