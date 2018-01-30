package org.jax.peastools.filter;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.io.IOException;


public class BAMFilter {
	
	public static void main(String[] args){
		BAMFilter bf = new BAMFilter();
		try {
			bf.filterBAM(args[0], Integer.parseInt(args[1]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void filterBAM(String bamfile, int fragthresh) throws IOException{
		
		SamReaderFactory factory = SamReaderFactory.makeDefault()
	              .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
	              .validationStringency(ValidationStringency.SILENT);
		final SamReader reader = factory.open(new File(bamfile));
		
		SAMFileWriterFactory wf = new SAMFileWriterFactory();
		
		SAMFileHeader h = reader.getFileHeader();
		SAMFileWriter w1 = wf.makeBAMWriter(h, false, new File(bamfile+"."+fragthresh+".bam"));
		SAMFileWriter w2 = wf.makeBAMWriter(h, false, new File(bamfile+".rest.bam"));
		
		SAMRecordIterator it = reader.iterator();
		
		int[] distr = new int[fragthresh+1];
		
		int lc = 0;
		int gc = 0;
		while(it.hasNext()){
			SAMRecord next = it.next();
			
			int isize = Math.abs(next.getInferredInsertSize());
			if(isize <= fragthresh){
				w1.addAlignment(next);
				distr[isize]++;
				lc++;
			}
			else{
				w2.addAlignment(next);
				gc++;
			}
		}
		System.out.println(lc);
		System.out.println(gc);
		
		System.out.println("Distribution:");
		for(int i = 0; i < distr.length; i++){
			System.out.println(distr[i]);
		}
		reader.close();
		w1.close();
		w2.close();
	}
	
}
