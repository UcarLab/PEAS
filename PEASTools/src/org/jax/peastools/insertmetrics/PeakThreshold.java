package org.jax.peastools.insertmetrics;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;


public class PeakThreshold {
	
	public static void main(String[] args){
		PeakThreshold pt = new PeakThreshold();
		String[] chrlist = null;
		if(args.length < 3) {
			chrlist = pt.getDefaultChrListHuman();
		}
		else {
			try {
				chrlist = pt.getChrList(args[2]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		pt.doIt(args[0], args[1], chrlist);
	}
	
	private String[] getDefaultChrListHuman() {
		String[] rv = new String[23];
		for(int i = 1; i < 23; i++) {
			rv[i-1] = "chr"+Integer.toString(i); 
		}
		return rv;
	}
	
	private String[] getChrList(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		LinkedList<String> rv = new LinkedList<String>();
		while(br.ready()) {
			rv.add(br.readLine());
		}
		br.close();
		return rv.toArray(new String[0]);
	}
	
	private void doIt(String in, String out, String[] chrlist){
		if(out.endsWith("/")){
			out = out.substring(0, out.length()-1);
		}
		try {
			writeFile(out, getThreshold(in, chrlist, out));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeFile(String outdir, int thresh) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outdir+"/thresh.txt"));
		bw.write(thresh+"\n");
		bw.flush();
		bw.close();
	}

	private int getThreshold(String bamfile, String[] chromosomes, String outdir){
		SamReaderFactory factory = SamReaderFactory.makeDefault()
	              .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
	              .validationStringency(ValidationStringency.SILENT);
		final SamReader reader = factory.open(new File(bamfile));
		
		TreeMap<String, SAMFileWriter> writers = new TreeMap<String, SAMFileWriter>();
		
		for(int i = 0; i < chromosomes.length; i++){
			String key = chromosomes[i];
			writers.put(key, new SAMFileWriterFactory().makeBAMWriter(reader.getFileHeader(), 
				true, new File(outdir+"/"+key+".bam")));
		}
		
		SAMRecordIterator it = reader.iterator();
		it = it.assertSorted(SAMFileHeader.SortOrder.coordinate);
		ReadStatistics stats = new ReadStatistics();
		
		while(it.hasNext()){
			SAMRecord next = it.next();
			stats.addRecord(next);
			SAMFileWriter w = writers.get(next.getReferenceName());
			if(w != null){
				w.addAlignment(next);
			}
		}
		
		while(!writers.isEmpty()){
			writers.pollFirstEntry().getValue().close();
		}
		
		int thresh = 20;
		int rv = stats.getInsertSizeWidthThreshold(thresh);
		
		stats.printStatistics(thresh);
		stats.trim(rv);
		it.close();
		
		return rv;
	}
}
