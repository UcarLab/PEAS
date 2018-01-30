package org.jax.peastools.conservation;


public class ConservationNoMemAnnotatedLocation extends ConservationAnnotatedLocation{

	private double _maxcons, _runningsum;
	
	public ConservationNoMemAnnotatedLocation(String id, String chr, int start, int end) {
		super(id, chr, start, end);
		_maxcons = 0;
	}
	
	
	public void addConservationScore(int l, double cons){
		_runningsum += l*cons;
		_maxcons = Math.max(_maxcons, cons);
	}

	
	public double getMaxConservation(){
		return _maxcons;
	}
	
	public double getMeanConservation(){
		return  _runningsum/(getEnd()-getStart());
	}

}
