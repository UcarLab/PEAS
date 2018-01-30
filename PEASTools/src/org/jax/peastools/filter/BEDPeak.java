package org.jax.peastools.filter;

import org.jax.peastools.util.Location;

public class BEDPeak extends Location{
	
	private String[] _rest;
	
	public BEDPeak(String id, String chr, int start, int end, String[] rest) {
		super(id, chr, start, end);
		_rest = rest;
	}

	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getChr()+"\t"+getStart()+"\t"+getEnd()+"\t"+getId());
		
		for(int i = 0; i < _rest.length; i++){
			sb.append("\t"+_rest[i]);
		}
		
		return sb.toString();
	}
}
