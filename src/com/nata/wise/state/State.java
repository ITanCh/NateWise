package com.nata.wise.state;

import java.util.HashSet;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nata.wise.event.BasicAction;
import com.nata.wise.event.ClickAction;

public class State {
	private PkgAct mPkgAct;
	private HashSet<BasicAction> actions;
	private int kind;

	public State(PkgAct pa, NodeList nl, int k) {
		mPkgAct = pa;
		actions = getActions(nl);
		kind = k;
	}

	private HashSet<BasicAction> getActions(NodeList nl){
		HashSet<BasicAction> allActions=new HashSet<BasicAction>();
		for(int i=0;i<nl.getLength();i++){
			Node node=nl.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				
				//clickable
				String clickable=eElement.getAttribute("clickable");
				if(clickable.equals("true")){
					String bounds=eElement.getAttribute("bounds");
//					System.out.println(bounds);
					String[] segs=bounds.split("\\[|\\]|,");
					for(String s:segs)
						System.out.println(s);
					if(segs.length==6){
						int x=Integer.parseInt(segs[1])+1;
						int y=Integer.parseInt(segs[2])+1;
						allActions.add(new ClickAction(x, y));
					}
				}
				
			}
		}
		return allActions;
	}
	
	public String toString(){
		String all="";
		all+=mPkgAct.getPkgName()+":"+mPkgAct.getActName()+"\n";
		all+=(kind+"\n");
		Iterator<BasicAction> it=actions.iterator();
		while(it.hasNext()){
			BasicAction ba=it.next();
			all+=ba.toString()+"\n";
		}
		return all;
	}
}
