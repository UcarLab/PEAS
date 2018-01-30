package org.jax.peastools.insertmetrics;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.util.Histogram;


public class ReadStatistics {
	
	private int _total, _notpaired, _unmapped, _mateunmapped, _secondary, _duplicate, _zeroinsert, _diffchrom, _flagremoved;
	private Histogram<Integer> _ish;
	public ReadStatistics(){
		_ish = new Histogram<Integer>();
	}
	
	public int getNotPairedCount(){
		return _notpaired;
	}
	
	public int getUnmappedCount(){
		return _unmapped;
	}
	
	public int getMateUnmappedCount(){
		return _mateunmapped;
	}
	
	public int getSecondaryCount(){
		return _secondary;
	}
	
	public int getDuplicateCount(){
		return _duplicate;
	}
	
	public int getZeroInsertCount(){
		return _zeroinsert;
	}

	public int getFlagRemovedCount(){
		return _flagremoved;
	}
	
	public void addRecord(SAMRecord record){
		_total++;
		int is = record.getInferredInsertSize();
		boolean removed = false;
		if(!record.getReadPairedFlag()){
			_notpaired++;
			removed = true;
		}
		
		if(record.getReadUnmappedFlag()){
			_unmapped++;
			removed = true;
		}
		
		if(record.getMateUnmappedFlag()){
			_mateunmapped++;
			removed = true;
		}
		
		if(record.isSecondaryOrSupplementary()){
			_secondary++;
			removed = true;
		}
		
		if(record.getDuplicateReadFlag()){
			_duplicate++;
			removed = true;
		}
		
		if(record.getReferenceIndex() != record.getMateReferenceIndex()){
			_diffchrom++;
			removed = true;
		}
		
		if(is == 0){
			_zeroinsert++;
			removed = true;
		}
		
		if(removed){
			_flagremoved++;
		}
		else{
			if(is > 0){
				_ish.increment(is);
			}
		}
	}
	
	//Using PicardTools method
	public int getInsertSizeWidthThreshold(int deviations){
		return (int) (_ish.getMedian() + (deviations * _ish.getMedianAbsoluteDeviation()));
	}
	
	
	public void trim(int t){
		 int before = (int) _ish.getCount();
		 System.out.println("Total Reads Before Trimming: "+before);
		 _ish.trimByWidth(t);
		 int after = (int) _ish.getCount();
		 System.out.println("Total Reads After Trimming: "+after);
		 
		 int removed = before-after;
		 System.out.println("Total Reads Removed By Trimmer:"+removed);

	}
	public void printStatistics(int thresh){
		 System.out.println("Total Reads: "+_total);
		 System.out.println("Not Paired: "+_notpaired);
		 System.out.println("Unmapped: "+_mateunmapped);
		 System.out.println("Mate Unmapped: "+_mateunmapped);
		 System.out.println("Secondary: "+_secondary);
		 System.out.println("Duplicate: "+_duplicate);
		 System.out.println("Zero Insert Size: "+_zeroinsert);
		 System.out.println("Different Chromosome/Reference: "+_diffchrom);
		 System.out.println("Removed Due to Above Flags: "+_flagremoved);
		 System.out.println("Insert Size Threshold:"+getInsertSizeWidthThreshold(thresh));
	}
}
