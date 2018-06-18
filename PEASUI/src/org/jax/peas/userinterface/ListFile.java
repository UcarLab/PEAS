package org.jax.peas.userinterface;

public class ListFile {

		
	private String _name;
	private String _path;
	
	public ListFile(String name, String path){
		_name = name;
		_path = path;
	}
	
	public String getPath(){
		return _path;
	}
	
	public String toString(){
		return _name;
	}
	
	@Override
	public boolean equals(Object o){
		return o instanceof ListFile && _name.equals(o.toString());
	}

}
