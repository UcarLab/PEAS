package org.jax.peastools.filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


public class AnnotationFiltering {

	public static void main(String[] args){
		AnnotationFiltering af = new AnnotationFiltering();
		if(args.length > 1){
			af.filterAnnotation(args[0], Double.parseDouble(args[1]));
		}
	} 
	
	public void filterAnnotation(String filelist, double thresh){
		String[] files = null;
		try {
			files = getFiles(filelist);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for(int i = 0; i < files.length; i++){
			try {
				filterAnnotation(files[i], files[i]+"_afthresh.txt", thresh);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void filterAnnotation(String in, String out, double thresh) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write(br.readLine()+"\n");
		int count = 0;
		while(br.ready()){
			String line = br.readLine();
			String[] split = line.split("\t");
			double af = Double.parseDouble(split[55]);
			if(af >= thresh){
				bw.write(line+"\n");
				count++;
			}
		}
		System.out.println(in+":"+count);
		bw.flush();
		bw.close();
		br.close();
	}
	
	
	private String[] getFiles(String file) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		LinkedList<String> rv = new LinkedList<String>();
		while(br.ready()){
			String l = br.readLine();
			if((new File(l)).exists()){
				rv.add(l);
			}
			else{
				System.out.println("Cannot find file:"+l);
			}
		}
		br.close();
		return rv.toArray(new String[0]);
	}
}
