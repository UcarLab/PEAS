package org.jax.peastools.merge;
import org.jax.peastools.util.Location;


public class PeakFeatures extends Location {

	private double _peakscore;
	private double _foldchange;
	private int _summitpileup;
	private double _summitposition;
	
	private int _insertcount;
	private double _insertmean;
	private double _insertmedian;
	private double _insertratio;
	
	private String _annotation;
	private int _distancetotss;
	private String _genetype;
	
	private double _cpg;
	private double _gc;
	private double _maxcons;
	private double _meancons;
	private double _motif;
	private double _dmotif;
	private int _cutcount;
	private double _b50;
	private double _b150;
	private double _b300;
	private double _b500;
	private double _bother;
	private double _densecuts;
	
	private int[] _motifs;
	private int _ctcf;

	public PeakFeatures(String id, String chr, int start, int end) {
		super(id, chr, start, end);
	}
	
	
	public void setPeakScore(double peakscore){
		_peakscore = peakscore;
	}
	
	public void setFoldChange(double foldchange){
		_foldchange = foldchange;
	}
	
	public void setSummitPileup(int pileup){
		_summitpileup = pileup;
	}
	
	public void setSummitPosition(double summitposition){
		_summitposition = summitposition;
	}
	
	public void setInsertCount(int insertcount){
		_insertcount = insertcount;
	}
	
	public void setInsertMean(double insertmean){
		_insertmean = insertmean;
	}
	
	public void setInsertMedian(double insertmedian){
		_insertmedian = insertmedian;
	}
	
	public void setInsertRatio(double insertratio){
		_insertratio = insertratio;
	}
	
	public void setAnnotation(String annotation){
		_annotation = annotation;
	}
	
	public void setDistanceToTss(int distancetotss){
		_distancetotss = distancetotss;
	}
	
	
	public void setGeneType(String genetype){
		_genetype = genetype;
	}
	
	public void setCPG(double cpg){
		_cpg = cpg;
	}
	
	public void setGC(double gc){
		_gc = gc;
	}
	
	public void setMaxCons(double maxcons){
		_maxcons = maxcons;
	}
	
	public void setMeanCons(double meancons){
		_meancons = meancons;
	}
	
	public void setMotifScore(double motif){
		_motif = motif;
	}
	
	public void setDenovoMotifScore(double dmotif) {
		_dmotif = dmotif;
	}
	
	
	
	public void setCutCount(int cutcount) {
		_cutcount = cutcount;
	}

	public void setB50(double b50) {
		_b50 = b50;
	}


	public void setB150(double b150) {
		_b150 = b150;
	}


	public void setB300(double b300) {
		_b300 = b300;
	}


	public void setB500(double b500) {
		_b500 = b500;
	}


	public void setBOther(double bother) {
		_bother = bother;
	}


	public void setDenseCuts(double densecuts) {
		_densecuts = densecuts;
	}
	
	public int[] getMotifs(){
		return _motifs;
	}
	
	public void setMotifs(int[] motifs){
		_motifs = motifs;
	}
	
	public void setCTCFMotifs(int ctcf){
		_ctcf = ctcf;
	}

	
	public String getFeaturesLine(){
		StringBuilder sb = new StringBuilder();
		int start = getStart();
		int end = getEnd();
		sb.append(getChr()+"\t");
		sb.append(start+"\t");
		sb.append(end+"\t");
		
		//MACS2 Features
		sb.append(_peakscore+"\t");
		sb.append((end-start+1)+"\t");
		sb.append(_foldchange+"\t");
		sb.append(_summitpileup+"\t");
		sb.append(_summitposition+"\t");
		
		//Insert Features
		sb.append(_insertcount+"\t");
		sb.append(_insertmean+"\t");
		sb.append(_insertmedian+"\t");
		sb.append(_insertratio+"\t");
		
		
		sb.append(_b50+"\t");
		sb.append(_b150+"\t");
		sb.append(_b300+"\t");
		sb.append(_b500+"\t");
		sb.append(_bother+"\t");
		sb.append(_cutcount+"\t");
		sb.append(_densecuts+"\t");
		
		//Conservation
		sb.append(_maxcons+"\t");
		sb.append(_meancons+"\t");
		
		
		//HOMER Features
		sb.append(_gc+"\t");
		sb.append(_cpg+"\t");
		sb.append(_annotation+"\t");
		sb.append(_distancetotss+"\t");
		sb.append(_genetype);
		return sb.toString();
	}
	
	public String getMergedMotifFeatures(){
		return _motif+"\t"+_dmotif+"\t"+_ctcf;
	}
	

	public static String getColumnLabels(){
		StringBuilder sb = new StringBuilder();
		sb.append("Chr\t");
		sb.append("Start\t");
		sb.append("End\t");
		
		//MACS2 Features
		sb.append("Peak Score\t");
		sb.append("Peak Length\t");
		sb.append("Fold Change\t");
		sb.append("Summit Pileup\t");
		sb.append("Summit Position\t");
		
		
		//Insert Features
		sb.append("Insert Count\t");
		sb.append("Insert Mean\t");
		sb.append("Insert Median\t");
		sb.append("Insert Ratio\t");
		sb.append("Inserts (0,50]\t");
		sb.append("Inserts (50, 150]\t");
		sb.append("Inserts (150,300]\t");
		sb.append("Inserts (300,500]\t");
		sb.append("Inserts (500,)\t");
		sb.append("Cut Count\t");
		sb.append("Significant Cuts\t");
		
		//Conservation
		sb.append("Max Conservation Score\t");
		sb.append("Mean Conservation Score\t");
		
		//HOMER Features
		sb.append("GC%\t");
		sb.append("CPG%\t");
		sb.append("Annotation\t");
		sb.append("Distance to TSS\t");
		sb.append("Gene Type");

		return sb.toString();
	}
	
	
	public static String getMergedMotifColumnLabels(){
		return "Known Motif%\tDenovo Motif%\tCTCF Motifs";
	}
	
	public String getMotifFeatures(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < _motifs.length; i++){
			sb.append("\t"+_motifs[i]);
		}
		return sb.toString();
	}

}
