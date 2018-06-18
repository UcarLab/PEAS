package org.jax.peastools.annotate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.jax.peastools.util.Location;



public class AnnotatePeaks {
	
	public static void main(String[] args){
		AnnotatePeaks ap = new AnnotatePeaks();
		ap.annotatePeaks(args[0],args[1],args[2]);		
	}
	

	//PEAK File - 3 column tab delimited (Chr, Start, End)
	//ChromHMM File - 4 Column Tab delimited (Chr, Start, End, State) 4th column should be the state
	public void annotatePeaks(String peakfile, String annotationfile, String outfile){
		
		try {
			Location[] peaks = readLocations(peakfile);
			AnnotatedLocation[] annotated = readAnnotationLocations(annotationfile);
			String[] states = getStates(annotated);
			//System.out.println(states[0]);
			Map<String, Integer> stateindex = new TreeMap<String, Integer>();
			for(int i = 0; i < states.length; i++){
				stateindex.put(states[i], i);
			}
			int[][] freq = getAllFrequencies(peaks, annotated, stateindex);
			writeAnnotatedPeaks(peaks, freq, peakfile, outfile, states);
			//writeFrequencies(peaks, freq, outfile, states);
			
			//double[][] mat = getSegmentMatrix(peaks, annotated, stateindex, 200);
			//writeMatrix(mat, outfile+".segments.txt");
			
			//writeStates(stateindex, outfile+".states.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void writeAnnotatedPeaks(Location[] peaks, int[][] freq, String peakfile, String outfile, String[] states) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(peakfile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));

		bw.write(br.readLine());
		bw.write("\tBest Annotation");
		bw.write("\tBest Annotation Freqeuncy");
		bw.write("\tAnnotation Breakdown");
		bw.write("\tUnique Annotation Count");

		bw.write("\n");
		
		for(int i = 0; i < freq.length; i++){
			String line = br.readLine();
			String[] split = line.split("\t");
			try{
				int start = Integer.parseInt(split[1]);
				int end = Integer.parseInt(split[2]);
				bw.write(line);
				String[] finalstate = getFinalState(end-start, states, freq[i]);
				bw.write("\t"+finalstate[0]+"\t"+finalstate[1]+"\t"+finalstate[2]+"\t"+finalstate[3]);
				bw.write("\n");
			}
			catch(NumberFormatException e){
				e.printStackTrace();
			}
			catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}

		}
		
		bw.flush();
		bw.close();
		
		if(br.ready()){
			System.out.println("Original peak file has more peaks than what was read before");
		}
		
		br.close();
	}
	
//	private void writeFrequencies(Location[] peaks, int[][] freq, String outfile, String[] states) throws IOException{
//		System.out.println(outfile);
//		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
//		bw.write("Chr");
//		bw.write("\tStart");
//		bw.write("\tEnd");
//		bw.write("\tFinal State");
//		for(int i = 0; i < states.length; i++){
//			bw.write("\t"+states[i]);
//		}
//		bw.write("\n");
//		
//
//		for(int i = 0; i < freq.length; i++){
//			bw.write(peaks[i].getChr());
//			bw.write("\t"+peaks[i].getStart());
//			bw.write("\t"+peaks[i].getEnd());
//			bw.write("\t"+(peaks[i].getEnd()-peaks[i].getStart()+1));
//			String[] finalstate = getFinalState(states, freq[i]);
//			bw.write("\t"+finalstate[0]);
//			bw.write("\t"+finalstate[1]);
//			for(int j = 0; j < freq[i].length; j++){
//				bw.write("\t"+freq[i][j]);
//			}
//			bw.write("\n");
//		}
//		
//		bw.flush();
//		bw.close();
//	}
	
	private String[] getFinalState(int l, String[] states, int[] freq){
		String rv = "";
		int count = 0;
		int bfi = -1; 
		double bf = -1;
		for(int i = 0; i < freq.length; i++){
			if(freq[i] > 0){
				if(count > 0){
					rv += "|";
				}
				double curfreq = ((double)freq[i]/l);
				if(curfreq > bf){
					bf = curfreq;
					bfi = i;
				}
				rv += states[i]+"("+curfreq+")";
				count++;
			}
		}
		
		String beststate = "O";
		String beststatefreq = "1";
		if(bfi > -1){
			beststate = states[bfi];
			beststatefreq = Double.toString(bf);
		}
		
		return new String[]{beststate, beststatefreq, rv,Integer.toString(count)};
	}
	
//	private void writeStates(Map<String, Integer> states, String outfile) throws IOException{
//		System.out.println(outfile);
//		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
//		for(Iterator<Entry<String,Integer>> it = states.entrySet().iterator(); it.hasNext();){
//			Entry<String,Integer> next = it.next();
//			bw.write(next.getValue()+"\t"+next.getKey()+"\n");
//		}
//		bw.flush();
//		bw.close();
//	}
	
//	private void writeMatrix(double[][] mat, String outfile) throws IOException{
//		System.out.println(outfile);
//		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
//		for(int i = 0; i < mat.length; i++){
//			bw.write(Double.toString(mat[i][0]));
//			for(int j = 1; j < mat[i].length; j++){
//				bw.write("\t"+mat[i][j]);
//			}
//			bw.write("\n");
//		}
//		
//		bw.flush();
//		bw.close();
//	}
	
	private String[] getStates(AnnotatedLocation[] l){
		Set<String> s = new TreeSet<String>();
		for(int i = 0; i < l.length; i++){
			s.add(l[i].getState());
		}
		return s.toArray(new String[0]);
	}
	
	
	private int[][] getAllFrequencies(Location[] l, AnnotatedLocation[] chromhmm, Map<String, Integer> states) throws Exception{
		TreeMap<String, Location[]> sortedchromhmm = getChrSortedLocations(chromhmm);
		int[][] frequencies = new int[l.length][];
		//double meanfreq = 0;
		for(int i = 0; i < l.length; i++){
			frequencies[i] = getStateFrequencies(l[i], sortedchromhmm, states);
		}
		
		return frequencies;
	}
	
//	private double[][] getSegmentMatrix(Location[] l, AnnotatedLocation[] chromhmm, Map<String, Integer> states, int binsize) throws Exception{
//		TreeMap<String, Location[]> sortedchromhmm = getChrSortedLocations(chromhmm);
//		double[][] frequencies = new double[l.length][];
//		int maxlength = getMaxPeakLength(l);
//		int numbins = maxlength/binsize+2;
//		for(int i = 0; i < l.length; i++){
//			frequencies[i] = getStateSegments(l[i], sortedchromhmm, states, numbins, binsize);
//		}
//		
//		return frequencies;
//	}
	
	
//	private int getMaxPeakLength(Location[] l){
//		int max = 0;
//		for(int i = 0; i < l.length; i++){
//			max = Math.max(max, l[i].getEnd()-l[i].getStart()+1);
//		}
//		return max;
//	}
	
	private int[] getStateFrequencies(Location l, TreeMap<String, Location[]> sortedchromhmm, Map<String, Integer> states) {
		String chr = l.getChr();
		int start = l.getStart();
		int end = l.getEnd()-1;	//0base
		Location[] chrloci = sortedchromhmm.get(chr);
		if(chrloci == null){
			chrloci = new AnnotatedLocation[0];
		}
		int ci = findFirstIndex(chrloci, l);

		int[] counts = new int[states.size()];
		
		while(ci < chrloci.length){
			AnnotatedLocation chl = (AnnotatedLocation) chrloci[ci];
			int cstart = chl.getStart();
			int cend = chl.getEnd()-1;	//0base
			ci++;
			if(cstart > end){
				break;
			}
			else if(cend < start){
				continue;
			}
			else{
				int count = Math.max(Math.min(cend, end)-Math.max(cstart, start), 1);
				counts[states.get(chl.getState())] += count;
			}
		}
		
		return counts;
	}
	
	
//	private double[] getStateSegments(Location l, TreeMap<String, Location[]> sortedchromhmm, Map<String, Integer> states, int numbins, int binsize) {
//		String chr = l.getChr();
//		int start = l.getStart();
//		int end = l.getEnd();
//		Location[] chrloci = sortedchromhmm.get(chr);
//		if(chrloci == null){
//			chrloci = new AnnotatedLocation[0];
//		}
//		int ci = findFirstIndex(chrloci, l);
//
//		//int numbins = (int) Math.floor(peaksize/binsize);
//		
//		LinkedList<Integer> sl = new LinkedList<Integer>();
//		while(ci < chrloci.length){
//			AnnotatedLocation chl = (AnnotatedLocation) chrloci[ci];
//			int cstart = chl.getStart();
//			int cend = chl.getEnd();
//			ci++;
//			if(cstart > end){
//				break;
//			}
//			else if(cend < start){
//				continue;
//			}
//			else{
//				int bp = Math.min(cend, end)-Math.max(cstart, start);
//				int binstouse = bp/binsize;
//				if(bp % binsize > 0){
//					binstouse++;
//				}
//				String cstate = chl.getState();
//				for(int i = 0; i < binstouse; i++){
//					sl.add(states.get(cstate));
//				}
//			}
//		}
//
//		
//		double[] rv = new double[numbins];
//		int usedbins = sl.size();
//		int half = (numbins-usedbins)/2;
//		
//		int index = 0;
//		while(index < half){
//			rv[index++] = Double.NaN;
//		}
//		for(Iterator<Integer> it = sl.iterator(); it.hasNext();){
//			try{
//				rv[index++] = it.next();
//			}
//			catch(ArrayIndexOutOfBoundsException e){
//				e.printStackTrace();
//			}
//		}
//		while(index < numbins){
//			rv[index++] = Double.NaN;
//		}
//		
//		return rv;
//	}
	
	
	//Returns -1 if there is no position that overlaps the region
	private int findFirstIndex(Location[] loci, Location l){
		int start = l.getStart();
		
		//Binary search to get an approximate index
		int s = 0;
		int e = loci.length-1;
		while((e-s) > 1){
			int mi = s+((e-s)/2);
			Location ml = loci[mi];
			int mstart = ml.getStart();
			if(mstart < start){
				s = mi;
			}
			else if(mstart > start){
				e = mi;
			}
			else{
				s = mi;
				e = mi;
				break;
			}
		}
		
		return s;
	}
	
	private Location[] readLocations(String file) throws IOException{
		LinkedList<Location> rv = new LinkedList<Location>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		int id = 0;
		br.readLine();
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			try{
			rv.add(new Location(Integer.toString(id++), split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2])));
			}
			catch(NumberFormatException e){
				e.printStackTrace();
			}
			catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}
		br.close();
		return rv.toArray(new Location[0]);
	}
	
	private AnnotatedLocation[] readAnnotationLocations(String file) throws IOException{
		LinkedList<AnnotatedLocation> rv = new LinkedList<AnnotatedLocation>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length > 3){
				rv.add(new AnnotatedLocation("", split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), split[3]));
			}
		}
		br.close();
		return rv.toArray(new AnnotatedLocation[0]);
	}
	
	private TreeMap<String, Location[]> getChrSortedLocations(Location[] loci) throws Exception{
		TreeMap<String, LinkedList<Location>> m = new TreeMap<String, LinkedList<Location>>();
		for(int i = 0; i < loci.length; i++){
			String key = loci[i].getChr();
			if(!m.containsKey(key)){
				m.put(key, new LinkedList<Location>());
			}
			m.get(key).add(loci[i]);
		}
		TreeMap<String, Location[]> rv = new TreeMap<String, Location[]>();
		while(!m.isEmpty()){
			Entry<String, LinkedList<Location>> e = m.pollFirstEntry();
			rv.put(e.getKey(), getStartSortedLocations(e.getValue().toArray(new AnnotatedLocation[0])));
		}
		return rv;
	}
	
	private Location[] getStartSortedLocations(Location[] loci) throws Exception{
		TreeMap<Integer, Location> m = new TreeMap<Integer, Location>();
		for(int i = 0; i < loci.length; i++){
			int key = loci[i].getStart();
			if(m.containsKey(key)){
				throw new Exception("Duplicate Start Position: "+loci[i].getChr()+":"+loci[i].getStart()); //Assumes that there is not a location that has the same start position
			}
			else{
				m.put(key, loci[i]);
			}
		}
		return m.values().toArray(new Location[0]);
	}

}
