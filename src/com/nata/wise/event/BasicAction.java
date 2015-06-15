package com.nata.wise.event;

public abstract class BasicAction {

	public BasicAction() {
		
	}
	
	abstract public void perform(String serial);
	//abstract public boolean equal(SmallAction sa);
	
	@Override  
	abstract public boolean equals(Object obj);
	
	@Override  
	abstract public int hashCode(); 
}
