package org.jax.peas.userinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PredictionAnnotator implements Function {

	private String _python;
	private String _path;
	private String _featurefile;
	private String _predictionfile;
	
	public PredictionAnnotator(String python, String path, String featurefile, String predictionfile){
		_python = python.trim();
		_path = path.trim();
		_featurefile = featurefile.trim();
		_predictionfile = predictionfile.trim();
		
	}
	
	public String getOutputFile(){
		return _featurefile+".annotated.txt";
	}
	
	@Override
	public String run() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(_python);
		sb.append(" ");
		sb.append(_path);
		sb.append("PEASPredictionAnnotator.py");
		sb.append(" ");
		sb.append(_featurefile);
		sb.append(" ");
		sb.append(_predictionfile);

		String[] cmd = new String[] {"/bin/bash", "--login", "-c", sb.toString()};
		
		final ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		
		
		BufferedReader stdin = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		StringBuilder rvsb = new StringBuilder();
		String s = null;
		while((s = stdin.readLine()) != null){
			rvsb.append(s);
		}
		
		rvsb.append("\n");
		rvsb.append("Predictions can be located at the following location:\n"+getOutputFile()+"\n");

		return rvsb.toString();
	}

}
