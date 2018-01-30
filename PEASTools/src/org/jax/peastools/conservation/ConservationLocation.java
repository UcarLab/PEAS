package org.jax.peastools.conservation;

import org.jax.peastools.util.Location;


public class ConservationLocation extends Location {

	private double _cons;
	public ConservationLocation(String id, String chr, int start, int end, double cons) {
		super(id, chr, start, end);
		_cons = cons;
	}

	public double getConservationScore(){
		return _cons;
	}
}
