package com.nata.wise.event;

public abstract class BasicAction {

	public BasicAction() {
		
	}
	
	abstract public void perform(String serial);
	//abstract public boolean equal(SmallAction sa);
	abstract public String toCommand(String serial);
	
}
