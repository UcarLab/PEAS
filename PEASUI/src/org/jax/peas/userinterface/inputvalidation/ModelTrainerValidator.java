package org.jax.peas.userinterface.inputvalidation;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import org.jax.peas.userinterface.FeatureFileUtility;
import org.jax.peas.userinterface.ListFile;

public class ModelTrainerValidator implements InputValidator{

	private String _python;
	private String _path;
	private String _modelname;
	private String _featurefile;
	private String _outdir;
	private String _modelparameters;
	private String _featureindices;
	private String _classes;
	private String _labelencoder;
	private int _randomstate;
	private boolean _haserror;
	private String _errormessage;

	
	public ModelTrainerValidator(String python, String path, String modelname, ListFile[] featurefiles, String outdir, String modelparameters, String featureindices, String classes, String labelencoder, String randomstate){
		_python = python.trim();
		_path = path.trim();
		_haserror = false;
		StringBuilder errormessages = new StringBuilder();
		
		_modelname = modelname.trim();
		if(_modelname.equals("")){
			_haserror = true;
			errormessages.append("Missing model name.\n");
		}
		
		_outdir = outdir.trim();
		if(_outdir == null || !(new File(_outdir)).isDirectory()){
			_haserror = true;
			errormessages.append("Output directory does not exist.\n");
		}
		if(!_outdir.endsWith("/")){
			_outdir = _outdir+"/";
		}
		
		if(featurefiles == null || (featurefiles.length <= 0)){
			_haserror = true;
			errormessages.append("Missing feature files.\n");
		}
		else{
			LinkedList<String> keeporder = new LinkedList<String>();
			TreeSet<String> unique = new TreeSet<String>();
			for(int i = 0; i < featurefiles.length; i++){
				String file = featurefiles[i].getPath();
				if(FeatureFileUtility.isMultiFeature(file)){
					try {
						LinkedList<ListFile> l = FeatureFileUtility.getFeatureFiles(file);
						for(Iterator<ListFile> it = l.iterator(); it.hasNext();){
							String path2 = it.next().getPath().replace("\"", "");
							if(!(new File(path2).exists())){
								_haserror = true;
								errormessages.append(path2+" does not exist.\n");
							}
							else{
								if(!unique.contains(path2)){
									unique.add(path2);
									keeporder.add(path2);
								}
							}
						}
					} catch (IOException e) {
						_haserror = true;
						errormessages.append("Error reading: "+file+"\n");
					}
					
				}
				else if((new File(file).exists())){
					if(!unique.contains(file)){
						unique.add(file);
						keeporder.add(file);
					}
				}
				else{
					_haserror = true;
					errormessages.append(file+"does not exist.\n");
				}
			}
			if(!_haserror){
				LinkedList<ListFile> l = new LinkedList<ListFile>();
				for(Iterator<String> it = keeporder.iterator(); it.hasNext();){
					String next = it.next();
					l.add(new ListFile(FeatureFileUtility.getFileName(next), next));
				}
				_featurefile = FeatureFileUtility.writeFeatureFile(l, modelname, "", _outdir);
				if(!(new File(_featurefile)).exists()){
					_haserror = true;
					errormessages.append("Error writing file: "+_featurefile);
				}
			}
		}
		
		
		_modelparameters = modelparameters.trim();

		
		_featureindices = featureindices;
		if(_featureindices.equals("") || !(new File(_featureindices)).exists()){
			_haserror = true;
			errormessages.append("Feature indices file does not exist.\n");
		}
		
		_classes = classes;
		if(_classes.equals("") || !(new File(_classes)).exists()){
			_haserror = true;
			errormessages.append("Class file does not exist.\n");
		}
		
		_labelencoder = labelencoder;
		if(_labelencoder.equals("") || !(new File(_labelencoder)).exists()){
			_haserror = true;
			errormessages.append("Label encoder file does not exist.\n");
		}
		
		_randomstate = 929;
		try{
			_randomstate = Integer.parseInt(randomstate);
		}
		catch(NumberFormatException e1){
			_haserror = true;
			errormessages.append("Random state must be an integer value.\n");
		}
		
		_errormessage = errormessages.toString();
	}
	
	public String getModelFile(){
		return _outdir+_modelname+".pkl";
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
		sb.append("PEASTrainer.py"+"\"");
		sb.append(" -o ");
		sb.append(_outdir);
		sb.append(" -n ");
		sb.append(_modelname);
		if(!_modelparameters.equals("")){
			sb.append(" -p ");
			sb.append("\""+_modelparameters+"\"");
		}
		sb.append(" -f ");
		sb.append("\""+_featureindices+"\"");
		sb.append(" -c ");
		sb.append("\""+_classes+"\"");
		sb.append(" -l ");
		sb.append("\""+_labelencoder+"\"");
		sb.append(" -r ");
		sb.append(_randomstate);
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
