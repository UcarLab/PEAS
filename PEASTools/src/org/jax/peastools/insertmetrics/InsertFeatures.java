package org.jax.peastools.insertmetrics;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;
import org.jax.peastools.util.Location;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.util.Histogram;

public class InsertFeatures {
	
	private double _mean;
	private double _median;
	private double _ratio;
	private int _insertcount, _cutcount;
	private double _b50, _b150, _b300, _b500, _bother;
	private int _densecuts;	//Requires some statistics
	
	/** Get the features for insert size
	 * 
	 * @param bam BAM File containing the reads for the peaks.
	 * @param thresh The threshold for the insert ratio, ie inserts >= 150, vs inserts < 150
	 */
	public InsertFeatures(Location peak, List<SAMRecord> samrecords, int thresh){
		_insertcount = samrecords.size();
		try {
			setInserts(peak, samrecords);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Histogram<Integer> h = new Histogram<Integer>("Insert Size", "READs");
		
		for(Iterator<SAMRecord> it = samrecords.iterator(); it.hasNext();){
			SAMRecord next = it.next();
			int isize = next.getInferredInsertSize();
			h.increment(Math.abs(isize));
			
			if(isize <= 50){
				_b50++;
			}
			else if(isize <=150){
				_b150++;
			}
			else if(isize <= 300){
				_b300++;
			}
			else if(isize <= 500){
				_b500++;
			}
			else{
				_bother++;
			}
		}
		
		_b50 /= _insertcount;
		_b150 /= _insertcount;
		_b300 /= _insertcount;
		_b500 /= _insertcount;
		_bother /= _insertcount;
		
		//System.out.println(h.getMean());
		//System.out.println(h.getMedian());
		
		TreeMap<Integer, InsertCount> insertsizemedian = new TreeMap<Integer, InsertCount>();
		double runningmean = 0;
		int above = 0;	// >= Thresh
		int below = 0;	// < Thresh
		
		
		for(Iterator<SAMRecord> it = samrecords.iterator(); it.hasNext();){
			SAMRecord next = it.next();
			int isize = next.getInferredInsertSize();
			int insertsize = Math.abs(isize);
			runningmean += (double)insertsize/_insertcount;
			if(!insertsizemedian.containsKey(insertsize)){
				insertsizemedian.put(insertsize, new InsertCount());
			}
			insertsizemedian.get(insertsize).increment();
			if(insertsize >= thresh){
				above++;
			}
			else{
				below++;
			}
		}
		
		
		_mean = runningmean;
		_ratio = (double)above/(double)Math.max(below,1);
		_median = calculateMedian(insertsizemedian, _insertcount);
		
		//System.out.println(_mean);
		//System.out.println(_median);

	}
	
	private void setInserts(Location peak,List<SAMRecord> samrecords) throws Exception{
		int[][] shapefeatures = getShapeFeatures(peak, samrecords);
		int[] cuts = shapefeatures[1];
		findDenseCuts(cuts);
	}
	
	private void findDenseCuts(int[] cuts){
		//Use binomial distribution with probability p = 5/l using a sliding window of 5
		BinomialTest bt = new BinomialTest();
		
		int totalcuts = 0;
		for(int i = 0; i < cuts.length; i++){
			totalcuts += cuts[i];
		}
		_cutcount = totalcuts;
		
		boolean[] sigcuts = new boolean[cuts.length-4];
		boolean hassig = false;
		for(int i = 0; i < sigcuts.length; i++){
			double prob = (double)5/cuts.length;
			
			int windowcuts = 0;
			for(int j = 0; j < 5; j++){
				windowcuts += cuts[i+j];
			}
			
			sigcuts[i] = bt.binomialTest(totalcuts, windowcuts, prob, AlternativeHypothesis.GREATER_THAN, 0.0005);
			if(!hassig && sigcuts[i]){
				hassig = true;
			}
		}
		
		if(hassig){
			//smooth gaps in significant cuts within 5bp windows
			int[] sigcutsites = new int[cuts.length];
			for(int i = 0; i < sigcuts.length; i++){
				if(sigcuts[i]){
					for(int j = 0; j < 5; j++){
						sigcutsites[i+j] = 1;
					}
				}
			}
			
			
			int rv = 0;
			int state = 0;
			for(int i = 0; i < sigcutsites.length; i++){
				if(sigcutsites[i] != state){
					if(state == 0){	
						rv++;
					}
					state = sigcutsites[i];
				}
			}
			_densecuts = rv;
		}
		else{
			_densecuts = 0;
		}

	}
	
	
	private int[][] getShapeFeatures(Location peak, List<SAMRecord> samrecords){
		int s = peak.getStart();
		int e = peak.getEnd();
		int l = e-s;	//0base
		int[] inserts = new int[l];
		int[] cutsites = new int[l];
		

		for(Iterator<SAMRecord> it = samrecords.iterator(); it.hasNext();){
			SAMRecord record = it.next();
			int isize = record.getInferredInsertSize();
			int cutstart = record.getAlignmentStart();
			int cutend = cutstart+isize;
			if(record.getReadNegativeStrandFlag()){
				cutend = record.getAlignmentEnd();
				cutstart = cutend+isize;
			}
			cutstart -= s;
			cutend -= s;
			
			if(cutstart >= 0){
				cutsites[cutstart]++;
			}
			
			if(cutend < l){
				cutsites[cutend]++;
			}
			int ms = Math.max(0, cutstart);
			int me = Math.min(l, cutend+1);
			for(int i = ms; i < me; i++){
				inserts[i]++;
			}

		}
		return new int[][]{inserts, cutsites};
	}
	
	private double calculateMedian(TreeMap<Integer, InsertCount> map, int size){
		int median2index = (size/2);
		int median1index = median2index-(1-size%2);
		
		int count = 0;
		
		double median1 = -1;
		double median2 = -1;
		
		for(Iterator<Entry<Integer,InsertCount>> it = map.entrySet().iterator(); it.hasNext();){
			Entry<Integer,InsertCount> next = it.next();
			Integer key = next.getKey();
			InsertCount value = next.getValue();
			int nextcount = count+value.getCount();
			if(median1index >= count && median1index <= nextcount){
				median1 = key;
			}
			if(median2index >= count && median2index <= nextcount){
				median2 = key;
			}
			count = nextcount;
		}
		
		return (median1+median2)/2;
	}
	
	public double getMean(){
		return _mean;
	}
	
	public double getMedian(){
		return _median;
	}
	
	public double getRatio(){
		return _ratio;
	}
	
	public int getInsertCount(){
		return _insertcount;
	}
	
	public double getB50(){
		return _b50;
	}
	
	public double getB150(){
		return _b150;
	}
	
	public double getB300(){
		return _b300;
	}
	
	public double getB500(){
		return _b500;
	}
	
	public double getBOther(){
		return _bother;
	}
	
	public int getDenseCuts(){
		return _densecuts;
	}
	
	public int getCutCount(){
		return _cutcount;
	}
	
	private class InsertCount {
		
		private int _count = 0;
		
		public void increment(){
			_count++;
		}
		
		public int getCount(){
			return _count;
		}
	}
	
}