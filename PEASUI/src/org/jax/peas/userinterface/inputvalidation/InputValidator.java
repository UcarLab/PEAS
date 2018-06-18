package org.jax.peas.userinterface.inputvalidation;

public interface InputValidator {

	public boolean hasError();
	
	public String getErrorMessage();
	
	public String getCommand();
	
	public String[] getCommandArray();
	
}
