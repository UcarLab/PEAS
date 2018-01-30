package org.jax.peastools.util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;


public class Util {

	public TreeMap<String, Location[]> getChrStartSorted(Location[] pe){
		TreeMap<String, Location[]> chrsorted = getChrSorted(pe);
		TreeMap<String, Location[]> rv = new TreeMap<String, Location[]>();
		while(!chrsorted.isEmpty()){
			Entry<String, Location[]> e = chrsorted.pollFirstEntry();
			rv.put(e.getKey(), getStartSorted(e.getValue()));
		}	
		return rv;
	}
	
	public TreeMap<String, Location[]> getChrEndSorted(Location[] pe){
		TreeMap<String, Location[]> chrsorted = getChrSorted(pe);
		TreeMap<String, Location[]> rv = new TreeMap<String, Location[]>();
		while(!chrsorted.isEmpty()){
			Entry<String, Location[]> e = chrsorted.pollFirstEntry();
			rv.put(e.getKey(), getEndSorted(e.getValue()));
		}	
		return rv;
	}
	
	public TreeMap<String, Location[]> getChrSorted(Location[] pe){
		TreeMap<String, LinkedList<Location>> tm = new TreeMap<String, LinkedList<Location>>();
		for(int i = 0; i < pe.length; i++){
			String chr = pe[i].getChr().toLowerCase();
			if(!tm.containsKey(chr)){
				tm.put(chr, new LinkedList<Location>());
			}
			tm.get(chr).add(pe[i]);
		}
		
		TreeMap<String, Location[]> rv = new TreeMap<String, Location[]>();
		while(!tm.isEmpty()){
			Entry<String, LinkedList<Location>> e = tm.pollFirstEntry();
			rv.put(e.getKey(), e.getValue().toArray(new Location[0]));
		}
		return rv;
	}
	
	private Location[] getStartSorted(Location[] pe){
		return getSorted(pe, new SortAdapter<Location>(){
			@Override
			public int getValue(Location e) {
				return e.getStart();
			}

			@Override
			public LinkedList<Location> getEntry(TreeMap<Integer, LinkedList<Location>> tm) {
				return tm.pollFirstEntry().getValue();
			}
		});
	}
	
	private Location[] getEndSorted(Location[] pe){
		return getSorted(pe, new SortAdapter<Location>(){
			@Override
			public int getValue(Location e) {
				return e.getEnd();
			}

			@Override
			public LinkedList<Location> getEntry(TreeMap<Integer, LinkedList<Location>> tm) {
				return tm.pollLastEntry().getValue();
			}
		});
	}
	
	
	private Location[] getSorted(Location[] pe, SortAdapter<Location> sa){
		TreeMap<Integer, LinkedList<Location>> tm = new TreeMap<Integer, LinkedList<Location>>();
		for(int i = 0; i < pe.length; i++){
			Location pei = pe[i];
			int start = sa.getValue(pei);
			if(!tm.containsKey(start)){
				tm.put(start, new LinkedList<Location>());
			}
			tm.get(start).add(pei);
		}
		Location[] rv = new Location[pe.length];
		int i = 0;
		while(!tm.isEmpty()){
			LinkedList<Location> l = sa.getEntry(tm);
			for(Iterator<Location> it = l.iterator(); it.hasNext();){
				rv[i++] = it.next();
			}
		}
		return rv;
	}
	
	public Location[] getUnion(LinkedList<Location>[] locations){
		LinkedList<Location> rv = new LinkedList<Location>();
		for(int i = 0; i < locations.length; i++){
			for(Iterator<Location> it = locations[i].iterator(); it.hasNext();){
				rv.add(it.next());
			}
		}
		return rv.toArray(new Location[0]);
	}
	
	public Location[] getUnion(Location[] ... locations){
		LinkedList<Location> rv = new LinkedList<Location>();
		for(int i = 0; i < locations.length; i++){
			for(int j = 0; j < locations[i].length; j++){
				rv.add(locations[i][j]);
			}
		}
		return rv.toArray(new Location[0]);
	}
	
	public Location[] getNonOverlappingLocations(Location[] loc){
		TreeMap<String, Location[]> chrsorted = getChrStartSorted(loc);
		LinkedList<Location> rv = new LinkedList<Location>();
		
		while(!chrsorted.isEmpty()){
			Location[] sl = chrsorted.pollFirstEntry().getValue();
			
			int i = 0;
			while(i < sl.length-1){
				Location cl = sl[i];
				Location nl = sl[i+1];
				if(nl.getStart() < cl.getEnd()){	//0base
					int si = i;
					i++;
					while(i < sl.length-1){
						cl = sl[i];
						nl = sl[i+1];
						if(nl.getStart() >= cl.getEnd()){//0base
							int end = sl[si].getEnd();
							for(int j = si+1; j < i; j++){
								end = Math.max(sl[j].getEnd(), end);
							}
							rv.add(new Location(sl[si].getId(), sl[si].getChr(), sl[si].getStart(), end));
							break;
						}
						else{
							i++;
						}
					}
				}
				else{
					rv.add(cl);
				}
				i++;
			}
			if(i < sl.length){
				rv.add(sl[i]);
			}
		}
		
		return rv.toArray(new Location[0]);
	}
	
	public boolean isIn(Location l, int start, int end){
		//0base
		return l.getEnd()-1 >= start && end-1 >= l.getStart();
	}
	
	public Location[] readLocationsWithIds(String file) throws IOException{
		LinkedList<Location> rv = new LinkedList<Location>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		while(br.ready()){
			String[] split = br.readLine().split("\t");
			if(split.length >= 4){
				try{
					rv.add(new Location(split[3], split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2])));
				}
				catch(NumberFormatException e){
					
				}
			}
		}
		br.close();
		return rv.toArray(new Location[0]);
	}
	
	public Location[] getSortedLocationsWithIds(String peakfile, String chr) throws IOException{
		Location[] loci = readLocationsWithIds(peakfile);
		Util u = new Util();
		TreeMap<String, Location[]> rv = u.getChrStartSorted(loci);
		return rv.get(chr);
	}
	
	public int getOverlap(Location l1, Location l2){
		int rv = Math.min(l1.getEnd(), l2.getEnd())-Math.max(l1.getStart(), l2.getStart()); //0base
		return Math.max(0, rv);
	}
	
	public Location[] getOverlappingLoci(Location[] loci, Location l){
		int fi = findFirstIndex(loci, l);
		int s = l.getStart();
		int e = l.getEnd();
		if(fi != -1){
			LinkedList<Location> rv = new LinkedList<Location>();
			if(isIn(loci[fi], s,e)){
				rv.add(loci[fi]);
			}
			for(int i = fi+1; i < loci.length; i++){
				if(isIn(loci[i], s,e)){
					rv.add(loci[i]);
				}
			}
			return rv.toArray(new Location[0]);
		}
		else{
			return new Location[0];
		}
	}
	
	private int findFirstIndex(Location[] loci, Location l){
		int start = l.getStart();
		int end = l.getEnd();
		
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
		
		//Loop to make sure we have the location just before the start position goes beyond
		for(int i = s+1; i < loci.length; i++){
			if(loci[i].getStart() > start){
				s = i-1;
				break;
			}
		}
		
		//Choose position just before or just after depending on the overlap. 
		//If neither of them do return -1
		int n = s+1;
		if(s < loci.length && loci[s].getEnd() > start){ //0base
			return s;
		}
		else if(n < loci.length && loci[n].getStart() <= end && loci[n].getEnd() > start){ //0base
			return n;
		}
		else {
			return -1;
		}
	}
	
	private interface SortAdapter<T> {
		
		public int getValue(T e);
		
		public LinkedList<T> getEntry(TreeMap<Integer, LinkedList<T>> tm);
		
	}
}
