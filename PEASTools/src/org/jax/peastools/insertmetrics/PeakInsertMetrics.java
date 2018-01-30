package org.jax.peastools.insertmetrics;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jax.peastools.util.Location;
import org.jax.peastools.util.Util;


public class PeakInsertMetrics {

	public static void main(String[] args){
		PeakInsertMetrics ir = new PeakInsertMetrics();
		String chr = args[0];	//Chromosome being processed
		String bamfile = args[1];	//BAM of one chromosome
		String peakfile = args[2];	//ATAC-Seq Peak File
		String outfile = args[3];	//Output file
		int thresh = Integer.parseInt(args[4]);	//Maximum insert size
		try {
			ir.getInsertMetrics(chr, peakfile, bamfile, outfile, thresh);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param chr The chromosome to compare.
	 * @param peakfile	PEAK file of one chromosome.
	 * @param bamfile	Coordinate sorted BAM file of one chromosome.
	 * @param outfile Output file.
	 * @throws IOException 
	 */
	public void getInsertMetrics(String chr, String peakfile, String bamfile, String outfile, int threshold) throws IOException{
		Util util = new Util();
		
		Location[] peaks = util.getSortedLocationsWithIds(peakfile, chr);
		
		//LinkedList<Location>[] peaknucleosomes = getNucleosomeOverlap(peaks, getSortedNucleosomePositions(occfile).get(chr));

		SamReaderFactory factory = SamReaderFactory.makeDefault()
	              .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
	              .validationStringency(ValidationStringency.SILENT);
		final SamReader reader = factory.open(new File(bamfile));
		
		//BAMIndexer.createIndex(reader, new File("/Users/athib/Desktop/ATAC Prediction/PipelineScripts/CD4T.bai"));
		
		SAMRecordIterator it = reader.iterator();
		it = it.assertSorted(SAMFileHeader.SortOrder.coordinate);
		
		List<SAMRecord> incurrent = new LinkedList<SAMRecord>();
		List<SAMRecord> innext = new LinkedList<SAMRecord>();
		
		List<PeakInsert> rv = new LinkedList<PeakInsert>();
		int pi = 0;
		int npi = Math.min(1,peaks.length-1);
		while(it.hasNext() && pi < peaks.length){
			SAMRecord next = it.next();
			
			if(!includeRead(next, threshold)){
				continue;
			}

			
			//Get insert size start/end
			int istart;
			int iend;
			int isize = next.getInferredInsertSize()-1;
			
			int rstart = next.getAlignmentStart();
			istart = rstart;
			iend = rstart+isize;
			
			if(util.isIn(peaks[pi], istart, iend)){
				incurrent.add(next);
			}
			
			if(util.isIn(peaks[npi], istart, iend)){
				innext.add(next);
			}
			
			if(increment(peaks[pi], next.getStart())){
				//rv.add(new PeakInsert(peaks[pi],new InsertFeatures(peaks[pi],incurrent, 150, peaknucleosomes[pi])));
				rv.add(new PeakInsert(peaks[pi],new InsertFeatures(peaks[pi],incurrent, 150)));
				pi++;
				npi = Math.min(pi+1,peaks.length-1);
				incurrent = innext;
				innext = new LinkedList<SAMRecord>();
				
				//This should not be necessary.  All this is doing is checking one more time whether an insert overlaps multiple peaks
				if(pi < npi){
					for(Iterator<SAMRecord> it2 = incurrent.iterator(); it2.hasNext();){
						SAMRecord next2 = it2.next();
						int start2 = next2.getStart();
						int end2 = next2.getEnd();
						if(util.isIn(peaks[npi], start2, end2)){
							innext.add(next2);
							System.out.println("Multipeak read.");
						}
					}
				}
			}
		}
		while(pi < peaks.length){
			//rv.add(new PeakInsert(peaks[pi],new InsertFeatures(peaks[pi],incurrent, 150, peaknucleosomes[pi])));
			rv.add(new PeakInsert(peaks[pi],new InsertFeatures(peaks[pi],incurrent, 150)));
			pi++;
			npi = Math.min(pi+1,peaks.length-1);
			incurrent = innext;
			innext = new LinkedList<SAMRecord>();
			if(pi < npi){
				for(Iterator<SAMRecord> it2 = incurrent.iterator(); it2.hasNext();){
					SAMRecord next2 = it2.next();
					int start2 = next2.getStart();
					int end2 = next2.getEnd();
					if(util.isIn(peaks[npi], start2, end2)){
						innext.add(next2);
						System.out.println("Multipeak read.");
					}
				}
			}
		}
		it.close();
		writeFile(rv, outfile);
	}
	
	private void writeFile(List<PeakInsert> rv, String outfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		
		int l = rv.size();
		for(int i = 0; i < l; i++){
			PeakInsert val = rv.remove(0);
			Location l1 = val.PEAK;
			InsertFeatures if1 = val.INSERTFEATURES;
			
			bw.write(l1.getChr()+"\t"
					+l1.getStart()+"\t"
					+l1.getEnd()+"\t"
					+l1.getId()+"\t"
					
					+if1.getInsertCount()+"\t"
					+if1.getCutCount()+"\t"
					+if1.getMean()+"\t"
					+if1.getMedian()+"\t"
					+if1.getRatio()+"\t"
					+if1.getB50()+"\t"
					+if1.getB150()+"\t"
					+if1.getB300()+"\t"
					+if1.getB500()+"\t"
					+if1.getBOther()+"\t"
					+if1.getDenseCuts()+"\n");
		}
		
		bw.flush();
		bw.close();
	}
	
	
	private boolean includeRead(SAMRecord record, int thresh){
		if ((!record.getReadPairedFlag() ||
                record.getReadUnmappedFlag() ||
                record.getMateUnmappedFlag() ||
                record.isSecondaryOrSupplementary() ||
                record.getDuplicateReadFlag() ||
                record.getReadNegativeStrandFlag() ||
                record.getInferredInsertSize() == 0)) {
			return false;
		}
		
		if(record.getReferenceIndex() != record.getMateReferenceIndex()){
			return false;
		}
		
		if(record.getInferredInsertSize() > thresh){
			return false;
		}
		
		return true;
	}
	
	
	private boolean increment(Location l, int start){
		return l.getEnd()-1 < start;//0base
	}
	
	private class PeakInsert {
		
		public final Location PEAK;
		public final InsertFeatures INSERTFEATURES;
		
		public PeakInsert(Location p, InsertFeatures insertfeatures){
			PEAK = p;
			INSERTFEATURES = insertfeatures;
		}
	}
	
	
}
