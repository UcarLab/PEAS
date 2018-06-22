package org.jax.peas.userinterface.inputvalidation;

import java.io.File;

public class ExtractFeaturesValidator implements InputValidator{

	private String _path;
	private String _bamfile;
	private String _outdir;
	private String _fastafile;
	private String _reference;
	private String _peakfilter;
	private String _motifs;
	private String _conservation;
	private String _ctcf;
	
	private boolean _haserror;
	private String _errormessages;
	
	public ExtractFeaturesValidator(String path, String bamfile, String outdir, String fasta, String reference, String filter, String motifs, String conservation, String ctcf){
		StringBuilder errormessages = new StringBuilder();
		_haserror = false;
		
		
		_path = path.trim();
		if(!(new File(_path)).exists()){
			_haserror = true;
			errormessages.append("Incorrect file path set for PEAS.  Please go to the configure menu to specify the file path for PEAS.\n");
		}

		
		_bamfile = bamfile.trim();
		if(_bamfile.equals("") || !(new File(_bamfile)).exists()){
			_haserror = true;
			errormessages.append("BAM file does not exist.\n");
		}
		
		_outdir = outdir.trim();
		if(!(new File(_outdir)).isDirectory()){
			_haserror = true;
			errormessages.append("Output directory does not exist.\n");
		}
		if(!_outdir.endsWith("/")){
			_outdir = _outdir+"/";
		}
		
		_fastafile = fasta.trim();
		if(_fastafile.equals("") || !(new File(_fastafile)).exists()){
			_haserror = true;
			errormessages.append("FASTA file does not exist.\n");
		}
		
		_peakfilter = filter.trim();
		if(_peakfilter.equals("") || !(new File(_peakfilter)).exists()){
			_haserror = true;
			errormessages.append("Peak filter file does not exist.\n");
		}
		
		_reference = reference.trim();
		if(_reference.equals("")){
			_haserror = true;
			errormessages.append("HOMER reference genome is not specified.\n");
		}
		
		_motifs = motifs.trim();
		if(_motifs.equals("") || !(new File(_motifs)).exists()){
			_haserror = true;
			errormessages.append("Known motifs file does not exist.\n");
		}
		
		_conservation = conservation.trim();
		if(_conservation.equals("") || !(new File(_conservation)).exists()){
			_haserror = true;
			errormessages.append("Conservation file does not exist.\n");
		}
		
		_ctcf = ctcf.trim();
		if(_ctcf.equals("") || !(new File(_ctcf)).exists()){
			_haserror = true;
			errormessages.append("CTCF motifs file does not exist.\n");
		}
		
		_errormessages = errormessages.toString();
	}
	
	public String getFeatureFile(){
		String bamprefix = _bamfile.substring(_bamfile.lastIndexOf("/")+1, _bamfile.lastIndexOf("."));
		return _outdir+"peak_features/"+bamprefix+"_features.txt";
	}
	
	@Override
	public boolean hasError() {
		return _haserror;
	}

	@Override
	public String getErrorMessage() {
		return _errormessages;
	}

	public String getCommand(){
		int lastidx = _bamfile.lastIndexOf("/")+1;
		String bamdir = _bamfile.substring(0, lastidx);
		String prefix = _bamfile.substring(lastidx, _bamfile.lastIndexOf("."));
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\""+_path);
		sb.append("PEASFeatureExtraction.sh"+"\"");
		sb.append(" ");
		sb.append("\""+bamdir+"\"");
		sb.append(" ");
		sb.append("\""+prefix+"\"");
		sb.append(" ");
		sb.append("\""+_outdir+"\"");
		sb.append(" ");
		sb.append("\""+_fastafile+"\"");
		sb.append(" ");
		sb.append("\""+_reference+"\"");
		sb.append(" ");
		sb.append("\""+_peakfilter+"\"");
		sb.append(" ");
		sb.append("\""+_motifs+"\"");
		sb.append(" ");
		sb.append("\""+_conservation+"\"");
		sb.append(" ");
		sb.append("\""+_ctcf+"\"");
		sb.append(" ");
		sb.append("\""+_path+"\"");

		return sb.toString();
	}
	
	@Override
	public String[] getCommandArray() {
		return new String[] {"/bin/bash", "--login", "-c", getCommand()};
	}


}
