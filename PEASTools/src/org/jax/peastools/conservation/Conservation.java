package org.jax.peastools.conservation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.jax.peastools.util.Location;
import org.jax.peastools.util.Util;


public class Conservation {

	public static void main(String[] args){
		Conservation c = new Conservation();
		String peakfile = args[0];	//ATAC-Seq Peak File
		String conservationfile = args[1];
		String outfile = args[2];	//Output file
		try {
			c.getConservationScores(peakfile, conservationfile, outfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param peakfile	PEAK file of one chromosome.
	 * @param conservationpeakfile	PEAK file of the conservation scores.
	 * @param outfile Output file.
	 * @throws IOException 
	 */
	public void getConservationScores(String peakfile, String conservationpeakfile, String outfile) throws IOException{
		Util util = new Util();
		Location[] peaks = util.readLocationsWithIds(peakfile);
		LinkedList<ConservationNoMemAnnotatedLocation> cpeaks = new LinkedList<ConservationNoMemAnnotatedLocation>();
		List<ConservationAnnotatedLocation> rv = new LinkedList<ConservationAnnotatedLocation>();

		for(int i = 0; i < peaks.length; i++){
			ConservationNoMemAnnotatedLocation cl = new ConservationNoMemAnnotatedLocation(peaks[i].getId(), peaks[i].getChr(), peaks[i].getStart(), peaks[i].getEnd());
			cpeaks.add(cl);
			rv.add(cl);
		}
		
		TreeMap<String, Location[]> sortedpeaks = util.getChrStartSorted(cpeaks.toArray(new ConservationNoMemAnnotatedLocation[0]));
		
		//ConservationLocation[] conspeaks = readConservationLocations(conservationpeakfile);
		//TreeMap<String, Location[]>  sortedconspeaks = util.getChrStartSorted(conspeaks);
		
		
		
		BufferedReader br = new BufferedReader(new FileReader(conservationpeakfile));
		
		while(br.ready()){
			String line = br.readLine();
			String[] split = line.split("\t");
			
			if(split.length > 3){
				try{
					String chr = split[0];
					int start = Integer.parseInt(split[1]);
					int end = Integer.parseInt(split[2]);
					double cons = Double.parseDouble(split[3]);
					Location[] chrpeaks = sortedpeaks.get(chr);
					if(chrpeaks != null){
						Location consl = new Location(null, chr, start, end);
						Location[] ol = util.getOverlappingLoci(chrpeaks, consl);
						for(int i = 0; i < ol.length; i++){
							((ConservationNoMemAnnotatedLocation) ol[i]).addConservationScore(util.getOverlap(ol[i], consl),cons);
						}
					}
					
				}
				catch(NumberFormatException e){
				}
			}
		}
		
		br.close();
		
		writeFile(rv, outfile);
	}
	
	private void writeFile(List<ConservationAnnotatedLocation> l, String outfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		for(Iterator<ConservationAnnotatedLocation> it = l.iterator(); it.hasNext();){
			ConservationAnnotatedLocation next = it.next();
			bw.write(next.getChr()+"\t"+next.getStart()+"\t"+next.getEnd()+"\t"+next.getId()+"\t"+next.getMaxConservation()+"\t"+next.getMeanConservation()+"\n");
		}
		bw.flush();
		bw.close();
	}
	
	
	
}
