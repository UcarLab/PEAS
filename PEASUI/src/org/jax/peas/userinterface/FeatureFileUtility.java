package org.jax.peas.userinterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class FeatureFileUtility {
	
	public static boolean isMultiFeature(String file){
		boolean rv = false;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			if(br.ready()){
				String[] split = br.readLine().split("\t");
				if(split.length == 2 && new File(split[1].replace("\"", "")).exists()){
					rv = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rv;
	}
	
	public static LinkedList<ListFile> getFeatureFiles(String featurefile) throws IOException{
		LinkedList<ListFile> l = new LinkedList<ListFile>();
		BufferedReader br  = new BufferedReader(new FileReader(featurefile));
		while(br.ready()){
			String line = br.readLine();
			String[] split = line.split("\t");
			l.add(new ListFile(split[0], split[1]));
		}
		br.close();
		
		return l;
	}
	
	public static ListFile[] reformatFeatureFile(String featurefile, String dest) throws FileNotFoundException{
		if((new File(featurefile)).exists()){
			LinkedList<ListFile> rv = new LinkedList<ListFile>();
			BufferedReader br = null;
			BufferedWriter bw = null;
			try {
				br = new BufferedReader(new FileReader(featurefile));
				bw = new BufferedWriter(new FileWriter(dest));
				
				while(br.ready()){
					String line = br.readLine();
					String[] split = line.split("\t");
					String name = split[0].replace(" ", "_");
					String path = split[1];
					rv.add(new ListFile(name, path));
					bw.write(name+"\t"+path+"\n");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(bw != null){
					try {
						bw.flush();
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return rv.toArray(new ListFile[0]);
		}
		else{
			throw new FileNotFoundException();
		}
	}
	
	public static String writeFeatureFile(List<ListFile> listfiles, String name, String suffix, String outdir){
		
		TreeMap<String, ListFile> unique = new TreeMap<String, ListFile>();
		for(Iterator<ListFile> it = listfiles.iterator(); it.hasNext();){
			ListFile next = it.next();
			String path = next.getPath();
			if(!unique.containsKey(path)){
				unique.put(path, next);
			}
		}
		Collection<ListFile> ulistfiles = unique.values();
		
		if(!outdir.endsWith("/")){
			outdir = outdir+"/";
		}
		
		String origfeaturefile = outdir+name+"_features";
		String featurefile = origfeaturefile+".txt";
		
		//Wanted to avoid overwriting files, however this would just cause issues in
		//in writing too many files with how the GUI is currently set up.
		//int counter = 0;
		//while((new File(featurefile).exists())){
			//featurefile =  origfeaturefile+"_"+(++counter)+".txt";
		//}
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(featurefile));
			for(Iterator<ListFile> it = ulistfiles.iterator(); it.hasNext();){
				ListFile next = it.next();
				String fname = next.toString();
				String path = next.getPath();
				if(fname.equals(path)){
					fname = getFileName(path);
				}
				bw.write(fname+(suffix.equals("")? "" : "_"+suffix)+"\t"+path+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(bw != null){
					bw.flush();
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return featurefile;
	}
	
	public static String getFileName(String path){
		int lastdot = path.lastIndexOf(".");
		int lastslash = path.lastIndexOf("/")+1;
		String substring = path.substring(lastslash, lastdot);
		substring = substring.replace(" ","_");
		return substring;
	}
	
}
