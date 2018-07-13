package org.jax.peas.userinterface.inputvalidation;

import java.io.File;

public class TSSPromoterValidator implements InputValidator {

	private String _python;
	private String _path;
	private String _featurefile;
	private String _outdir;
	private String _tempdir;
	private int _tsscol;
	private int _up;
	private int _down;
	private String _dest;
	private String _adest;
	
	private boolean _haserror;
	private String _errormessage;

	
	public TSSPromoterValidator(String python, String path, String features, String outdir, String tsscol, String upstream, String downstream){
		_python = python.trim();
		_path = path.trim();
		_tempdir = _path+"/temp/";

		_haserror = false;
		StringBuilder errormessages = new StringBuilder();
		
		_tsscol = 24;
		_up = 2000;
		_down = 2000;
		
		_featurefile = features.trim();
		if(_featurefile.equals("") || !(new File(_featurefile)).exists()){
			_haserror = true;
			errormessages.append("Feature file does not exist.\n");
		}
		
		_outdir = outdir.trim();
		File fff = new File(_featurefile);
		if(_outdir.equals("") && fff.exists()){
			String p = fff.getAbsolutePath();
			_outdir = p.substring(0, p.lastIndexOf("/")+1);
		}
		if(!(new File(_outdir)).isDirectory()){
			_haserror = true;
			errormessages.append("Output directory does not exist.\n");
		}
		if(!_outdir.endsWith("/")){
			_outdir = _outdir+"/";
		}
		
		try{
			_tsscol = Integer.parseInt(tsscol);
		}
		catch(NumberFormatException e1){
			_haserror = true;
			errormessages.append("Tss column must be an integer value.\n");
		}
		
		try{
			_up = Integer.parseInt(upstream);
		}
		catch(NumberFormatException e1){
			_haserror = true;
			errormessages.append("Upstream distance must be an integer value.\n");
		}
		try{
			_down = Integer.parseInt(downstream);
		}
		catch(NumberFormatException e1){
			_haserror = true;
			errormessages.append("Downstream distance must be an integer value.\n");
		}

		String ffname = new File(_featurefile).getName();
		_dest = _tempdir+ffname+".promoterpredictions.txt";
		_adest = _outdir+ffname+".promoterannotated.txt";

		_errormessage = errormessages.toString();
	}
	
	public String getOutputDirectory(){
		return _outdir;
	}
	
	public String getPredictionFile(){
		return _dest;
	}
	
	public String getAnnotationFile(){
		return _adest;
	}
	
	@Override
	public boolean hasError() {
		return _haserror;
	}

	@Override
	public String getErrorMessage() {
		return _errormessage;
	}

	@Override
	public String getCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append(_python);
		sb.append(" ");
		sb.append("\""+_path);
		sb.append("PEASTssPromoter.py"+"\"");
		sb.append(" -c ");
		sb.append(_tsscol);
		sb.append(" -u ");
		sb.append(_up);
		sb.append(" -d ");
		sb.append(_down);
		sb.append(" -t ");
		sb.append("\""+_dest+"\"");
		sb.append(" -a ");
		sb.append("\""+_adest+"\"");
		sb.append(" ");
		sb.append("\""+_featurefile+"\"");
		return sb.toString();
	}

	@Override
	public String[] getCommandArray() {
		String[] cmd = new String[] {"/bin/bash", "--login", "-c", getCommand()};
		return cmd;
	}

}
