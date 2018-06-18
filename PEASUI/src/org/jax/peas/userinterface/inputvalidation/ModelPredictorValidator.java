package org.jax.peas.userinterface.inputvalidation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.jax.peas.userinterface.FeatureFileUtility;
import org.jax.peas.userinterface.ListFile;

public class ModelPredictorValidator implements InputValidator{

	private String _python;
	private String _path;
	private String _featurefile;
	private String _outdir;
	private String _model;
	private String _featureindices;
	private String _labelencoder;
	private FeaturePrediction[] _featurepredictions;
	private String _classes;
	private boolean _evaluation;
	private boolean _haserror;
	private String _errormessage;

	
	public ModelPredictorValidator(String python, String path,String features, String outdir, String model, String featureindices, String labelencoder, String classes, boolean eval, String id){
		_python = python.trim();
		_path = path.trim();
		_haserror = false;
		
		_evaluation = eval;
		
		StringBuilder errormessages = new StringBuilder();
		
		_featurefile = features.trim();
		if(_featurefile.equals("") || !(new File(_featurefile)).exists()){
			_haserror = true;
			errormessages.append("Feature file does not exist.\n");
		}
		else{
			if(FeatureFileUtility.isMultiFeature(_featurefile)){
				try {
					ListFile[] files = FeatureFileUtility.reformatFeatureFile(_featurefile, id, ".reformatted.txt");
					String filepathprefix = _featurefile+".reformatted";
					_featurefile = _featurefile+".reformatted.txt";
					_featurepredictions = new FeaturePrediction[files.length];
					for(int i = 0; i < files.length; i++){
						String featurename = files[i].toString();
						_featurepredictions[i] = new FeaturePrediction(files[i].getPath(), filepathprefix+"_"+featurename+"_predictions.txt");
					}
				} catch (FileNotFoundException e) {
					_haserror = true;
					errormessages.append("Error reading multi-feature file.\n");
				}
			}
			else{
				String p  =_featurefile;
				int slashidx = p.lastIndexOf("/")+1;
				String ffoutdir = p.substring(0, slashidx);
				String ffname = p.substring(slashidx);
				List<ListFile> fflist = new LinkedList<ListFile>();
				fflist.add(new ListFile(id, _featurefile));
				_featurefile = FeatureFileUtility.writeFeatureFile(fflist, ffname+".list", "", ffoutdir);
				
				String prefix = _featurefile.substring(0,_featurefile.lastIndexOf("."));
				_featurepredictions = new FeaturePrediction[1];
				_featurepredictions[0] = new FeaturePrediction(p, prefix+"_"+id+"_predictions.txt");;
				
				if(!(new File(_featurefile)).exists()){
					_haserror = true;
					errormessages.append("Error creating feature file list.\n");
				}
			}
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
		
		_model = model.trim();
		if(_model.equals("") || !(new File(_model)).exists()){
			_haserror = true;
			errormessages.append("Model file does not exist.\n");
		}

		
		_featureindices = featureindices.trim();
		if(_featureindices.equals("") || !(new File(_featureindices)).exists()){
			_haserror = true;
			errormessages.append("Feature indices file does not exist.\n");
		}
		
		_labelencoder = labelencoder.trim();
		if(_labelencoder.equals("") || !(new File(_labelencoder)).exists()){
			_haserror = true;
			errormessages.append("Label encoder file does not exist.\n");
		}
		
		_classes = classes.trim();
		if(!_labelencoder.equals("") && !(new File(_labelencoder)).exists()){
			_haserror = true;
			errormessages.append("Class file does not exist.\n");
		}
		
		_errormessage = errormessages.toString();
	}
	
	public FeaturePrediction[] getFeaturePredictions(){
		return _featurepredictions;
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
		sb.append(_path);
		sb.append("PEASPredictor.py");
		sb.append(" -o ");
		sb.append(_outdir);
		sb.append(" -f ");
		sb.append("\""+_featureindices+"\"");
		sb.append(" -l ");
		sb.append("\""+_labelencoder+"\"");
		if(!_classes.equals("")){
			sb.append(" -c ");
			sb.append("\""+_classes+"\"");
		}
		if(_evaluation){
			sb.append(" -e ");
		}
		sb.append(" ");
		sb.append("\""+_model+"\"");
		sb.append(" ");
		sb.append("\""+_featurefile+"\"");
		
		return sb.toString();
	}

	@Override
	public String[] getCommandArray() {
		String[] cmd = new String[] {"/bin/bash", "--login", "-c", getCommand()};
		return cmd;
	}

	public String getOutputDirectory() {
		return _outdir;
	}
	

	
}
