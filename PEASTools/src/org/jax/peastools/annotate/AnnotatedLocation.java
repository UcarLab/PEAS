package org.jax.peastools.annotate;

import org.jax.peastools.util.Location;


public class AnnotatedLocation extends Location {

	private String _state;

	public AnnotatedLocation(String id, String chr, int start, int end, String state) {
		super(id, chr, start, end);
		_state = state;
	}

	public String getState(){
		return _state;
	}
	
	public void setState(String state){
		_state = state;
	}
}
