package com.nata.wise.event;

import java.util.ArrayList;

import com.nata.wise.state.State;

public class EventEdge {
	private State fromState;
	private State toState;
	private ArrayList<BasicAction> actions;
	
	public EventEdge(State from, State to, ArrayList<BasicAction> as){
		fromState=from;
		toState=to;
		
		fromState.addOutEdges(this);
		toState.setFromEdge(this);
		
		actions=new ArrayList<BasicAction>(as);
	}

	public State getFromState() {
		return fromState;
	}

	public State getToState() {
		return toState;
	}

	public ArrayList<BasicAction> getActions() {
		return new ArrayList<>(actions);
	}
	
	public void play(String serial){
		for(BasicAction ba:actions){
			ba.perform(serial);
		}
	}

	/**
	 * get edge string
	 */
	@Override
	public String toString(){
		String s="";
		for(BasicAction a:actions){
			s+=a.toString()+"\n";
		}
		return s;
	}
}
