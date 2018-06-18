package org.jax.peas.userinterface.inputvalidation;

import java.io.File;

public class AnnotateValidator implements InputValidator{

	private String _path;
	private String _featurefile;
	private String _annotationfile;
	private String _outputfile;
	private boolean _haserror;
	private String _errormessages;
	
	public AnnotateValidator(String path, String featurefile, String annotationfile){
		_path = path.trim();
		
		StringBuilder errormessages = new StringBuilder();
		_haserror = false;
		
		_featurefile = featurefile.trim();
		if(_featurefile.equals("") || !(new File(_featurefile)).exists()){
			_haserror = true;
			errormessages.append("Feature file does not exist.\n");
		}
		
		_annotationfile = annotationfile.trim();
		if(_annotationfile.equals("") || !(new File(_featurefile)).exists()){
			_haserror = true;
			errormessages.append("Annotation file does not exist.\n");
		}
		
		_annotationfile = annotationfile;
		
		_outputfile = _featurefile+".annotated.txt";
		
		_errormessages = errormessages.toString();
	}
	
	public String getFeatureFile(){
		return _featurefile;
	}
	
	public String getAnnotationFile(){
		return _annotationfile;
	}
	
	public String getOutputFile(){
		return _outputfile;
	}
	
	@Override
	public boolean hasError() {
		return _haserror;
	}

	@Override
	public String getErrorMessage() {
		return _errormessages;
	}

	@Override
	public String getCommand() {
		return "java -jar \""+_path+"PEASTools.jar\" annotate \""+_featurefile+"\" \""+_annotationfile+"\" \""+_outputfile+"\"";
	}

	@Override
	public String[] getCommandArray() {
		String[] cmd = new String[] {"/bin/bash", "--login", "-c", getCommand()};
		return cmd;
	}

}
