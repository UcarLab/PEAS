package org.jax.peastools.merge;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import org.jax.peastools.util.Location;
import org.jax.peastools.util.Util;


public class Merge {
	
	private TreeMap<String,PeakFeatures> _peaks;
	private String _motifcollabels;

	public static void main(String[] args){
		try {
			Merge mf = new Merge(args[0]);
			mf.addSummitPileup(args[1]);
			mf.addAnnotatedFeatures(args[2]);
			mf.addInsertFeatures(args[3]);
			mf.addConservationFeatures(args[4]);
			mf.addDenovoMotifs(args[5]);
			mf.addCTCFMotifs(args[6]);
			mf.writePeaks(args[7], args.length > 8 && args[8].equals("MERGED"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void addDenovoMotifs(String denovomotifs) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(denovomotifs));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length > 20){
				String id = split[0];
				PeakFeatures pf = _peaks.get(id);
				if(pf != null){
					double ms = getMotifScore(split);
					pf.setDenovoMotifScore(ms);
				}
			}
		}
		br.close();		
	}
	
	private void addCTCFMotifs(String ctcfmotifs) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(ctcfmotifs));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			String id = split[0];
			PeakFeatures pf = _peaks.get(id);
			if(pf != null){
				try{
					pf.setCTCFMotifs(getCTCFCount(split));
				}
				catch(NumberFormatException e){ }
			}
		}
		br.close();		
	}
	

	public Merge(String peakfile) throws IOException{
		_peaks = readPeaks(peakfile);
	}
	
	public void writePeaks(String out, boolean merged) throws IOException{
		Util u = new Util();
		PeakFeatures[] allpeaks = _peaks.values().toArray(new PeakFeatures[0]);
		
		TreeMap<String, Location[]> allsorted = u.getChrStartSorted(allpeaks);
		
		String[] chrs = allsorted.keySet().toArray(new String[0]);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write(PeakFeatures.getColumnLabels());
		if(merged){
			bw.write("\t"+PeakFeatures.getMergedMotifColumnLabels()+"\n");
		}
		else{
			bw.write(_motifcollabels+"\n");
		}
		for(int i = 0; i < chrs.length; i++){
			String curchr = chrs[i];
			int chridx = 23;
			try{
				chridx = Integer.parseInt(curchr.replace("chr", ""));
			}
			catch(NumberFormatException e){
			}
			
			//Only chromosomes 1-22
			if(chridx < 1 || chridx >= 23){
				continue;
			}
			
			Location[] peaks = allsorted.get(curchr);
			
			int pi = 0;
			while(pi < peaks.length){
				PeakFeatures pf = (PeakFeatures) peaks[pi];
				bw.write(pf.getFeaturesLine());
				if(merged){
					bw.write("\t"+pf.getMergedMotifFeatures()+"\n");
				}
				else{
					bw.write(pf.getMotifFeatures()+"\n");
				}
				pi++;
			}
			
		}
		
		bw.flush();
		bw.close();
		
	}
	
	
	public void addInsertFeatures(String peakfile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(peakfile));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length >= 9){
				String id = split[3];
				PeakFeatures pf = _peaks.get(id);
				if(pf != null){
					pf.setInsertCount(Integer.parseInt(split[4]));
					pf.setCutCount(Integer.parseInt(split[5]));
					pf.setInsertMean(Double.parseDouble(split[6]));
					pf.setInsertRatio(Double.parseDouble(split[8]));
					pf.setB50(Double.parseDouble(split[9]));
					pf.setB150(Double.parseDouble(split[10]));
					pf.setB300(Double.parseDouble(split[11]));
					pf.setB500(Double.parseDouble(split[12]));
					pf.setBOther(Double.parseDouble(split[13]));
					pf.setDenseCuts(Double.parseDouble(split[14]));
				}
			}
		}
		br.close();
	}
	
	public void addConservationFeatures(String peakfile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(peakfile));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length >= 6){
				String id = split[3];
				PeakFeatures pf = _peaks.get(id);
				if(pf != null){
					double meancons = Double.parseDouble(split[5]);

					pf.setMeanCons(meancons);
				}
			}
		}
		br.close();
	}
	
	public void addSummitPileup(String summitfile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(summitfile));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length == 10){
				String id = split[9];
				PeakFeatures pf = _peaks.get(id);
				if(pf != null){
					int pileup = (int)Double.parseDouble(split[5]);
					pf.setSummitPileup(pileup);
				}
			}
		}
		br.close();
	}
	
	public void addAnnotatedFeatures(String apeakfile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(apeakfile));
		String header = br.readLine();
		_motifcollabels = getMotifHeader(header.split("\t"));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length > 20){
				String id = split[0];
				PeakFeatures pf = _peaks.get(id);
				if(pf != null){
					String annotation = parseAnnotation(split[7]);
					int dtss;
					try{
						dtss = Integer.parseInt(split[9]);
					}
					catch(NumberFormatException e){
						dtss = Integer.MAX_VALUE;
					}
					String genetype = split[18];
					double cpg = Double.parseDouble(split[19]);
					double gc = Double.parseDouble(split[20]);
					double ms = getMotifScore(split);
					int[] motifs = getMotifs(split);

					pf.setAnnotation(annotation);
					pf.setDistanceToTss(dtss);
					pf.setGeneType(genetype);
					pf.setCPG(cpg);
					pf.setGC(gc);
					pf.setMotifScore(ms);
					pf.setMotifs(motifs);
				}
			}
		}
		br.close();
	}
	
	private String parseAnnotation(String a){
		a.replace("' UTR", "_UTR");
		return a.split(" ")[0];
	}
	
	private String getMotifHeader(String[] ls){
		StringBuilder rv = new StringBuilder();
		int l = ls.length;
		for(int i = 21; i < l; i++){
			rv.append("\t"+ls[i]);
		}
		return rv.toString();
	}
	
	private int[] getMotifs(String[] ls){
		int l = ls.length;
		int nmotifs = l-21;
		int count = 0;
		int[] rv = new int[nmotifs];
		for(int i = 21; i < l; i++){
			rv[count++] = Integer.parseInt(ls[i]);
		}
		return rv;
	}
	
	private double getMotifScore(String[] ls){
		int l = ls.length;
		int nmotifs = l-21;
		int count = 0;
		for(int i = 21; i < l; i++){
			if(Integer.parseInt(ls[i]) > 0){
				count++;
			}
		}
		return (double)count/nmotifs;
	}
	
	private int getCTCFCount(String[] ls){
		int l = ls.length;
		int count = 0;
		for(int i = 21; i < l; i++){
				count += Integer.parseInt(ls[i]);
		}
		return count;
	}
	

	public TreeMap<String,PeakFeatures> readPeaks(String file) throws IOException{
		TreeMap<String,PeakFeatures> rv = new TreeMap<String,PeakFeatures>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length >= 10){
				String id = split[3];
				String chr = split[0];
				int start = Integer.parseInt(split[1]);
				int end = Integer.parseInt(split[2]);
				
				double peakscore = Double.parseDouble(split[7]);
				double foldchange = Double.parseDouble(split[6]);
				
				double pl = (end-start+1);
				double hpl = pl/2;
				double summitposition = (Integer.parseInt(split[9])-hpl)/hpl;
				
				PeakFeatures pf = new PeakFeatures(id, chr, start, end);
				pf.setPeakScore(peakscore);
				pf.setFoldChange(foldchange);
				pf.setSummitPosition(summitposition);
				
				rv.put(id,pf);
			}
		}
		br.close();
		return rv;
	}
}
