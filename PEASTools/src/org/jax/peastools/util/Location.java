package org.jax.peastools.util;

public class Location {
	private String _id;
	protected String _chr;
	protected int _start;
	protected int _end;
	
	public Location(String id, String chr, int start, int end){
		_id = id;
		_chr = chr;
		_start = start;
		_end = end;
	}
	
	public String getId(){
		return _id;
	}
	
	public String getChr(){
		return _chr;
	}
	
	public int getStart(){
		return _start;
	}
	
	public int getEnd(){
		return _end;
	}
	
	public void setChr(String chr){
		_chr = chr;
	}
	
	public void setStart(int s){
		_start = s;
	}
	
	public void setEnd(int e){
		_end = e;
	}
}
