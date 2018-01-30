package org.jax.peastools.conservation;
import org.jax.peastools.util.Location;
import org.jax.peastools.util.Util;


public class ConservationAnnotatedLocation extends Location{

	private double _maxcons, _meancons;
	
	public ConservationAnnotatedLocation(String id, String chr, int start, int end) {
		super(id, chr, start, end);
		_maxcons = 0;
		_meancons = 0;
	}
	
	public ConservationAnnotatedLocation(String id, String chr, int start, int end, ConservationLocation[] a) {
		super(id, chr, start, end);
		
		double maxcons = 0;
		double weightedsum = 0;
		int l = a.length;
		Util u = new Util();
		if(l > 0){
			double consscore = a[0].getConservationScore();
			maxcons = Math.max(maxcons, consscore);
			weightedsum += consscore*u.getOverlap(new Location(id, chr, start, end), a[0]);
		}
		for(int i = 1; i < l; i++){
			double consscore = a[i].getConservationScore();
			maxcons = Math.max(maxcons, consscore);
			weightedsum += consscore*u.getOverlap(new Location(id, chr, start, end), a[i]);
		}
		
		_maxcons = maxcons;
		_meancons = weightedsum/(end-start+1);
		
	}
	

	
	public double getMaxConservation(){
		return _maxcons;
	}
	
	public double getMeanConservation(){
		return _meancons;
	}

}
