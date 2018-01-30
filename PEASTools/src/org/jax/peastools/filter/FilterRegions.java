package org.jax.peastools.filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import org.jax.peastools.util.Location;
import org.jax.peastools.util.Util;

public class FilterRegions {
	
	public static void main(String[] args){
		FilterRegions fb = new FilterRegions();
		try {
			fb.filter(args[0], args[1], args[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void filter(String peakfile, String filter, String out) throws IOException{
		Util u = new Util();
		BEDPeak[] allpeaks = readPeaks(peakfile).values().toArray(new BEDPeak[0]);
		Location[] blacklistpeaks = u.readLocationsWithIds(filter);
		
		TreeMap<String, Location[]> allsorted = u.getChrStartSorted(allpeaks);
		TreeMap<String, Location[]> blsorted = u.getChrStartSorted(blacklistpeaks);
		
		String[] chrs = allsorted.keySet().toArray(new String[0]);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
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
			Location[] blpeaks = blsorted.get(curchr);
			
			int bli = 0;
			int pi = 0;
			while(bli < blpeaks.length && pi < peaks.length){
				Location curpeak = peaks[pi];
				Location curbl = blpeaks[bli];

				if(curpeak.getStart() > curbl.getEnd()){
					bli++;
				}
				else{
					if(!u.isIn(curpeak, curbl.getStart(), curbl.getEnd())){
						BEDPeak pf = (BEDPeak) curpeak;
						bw.write(pf.toString()+"\n");
					}
					pi++;
				}
			}
			
			//write the rest
			while(pi < peaks.length){
				BEDPeak pf = (BEDPeak) peaks[pi];
				bw.write(pf.toString()+"\n");
				pi++;
			}
			
		}
		
		bw.flush();
		bw.close();
		
	}
	
	public TreeMap<String,BEDPeak> readPeaks(String file) throws IOException{
		TreeMap<String,BEDPeak> rv = new TreeMap<String,BEDPeak>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			String chr = split[0];
			int start = Integer.parseInt(split[1]);
			int end = Integer.parseInt(split[2]);
			String id = split[3];

			String[] rest = new String[split.length-4];
			for(int i = 4; i < split.length; i++){
				rest[i-4] = split[i];
			}
			
			BEDPeak pf = new BEDPeak(id, chr, start, end, rest);
			
			rv.put(id,pf);
		}
		br.close();
		return rv;
	}
	
}
