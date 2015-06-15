package com.nata.wise.state;

class PkgAct {
	String pkg, act;

	public PkgAct(String p, String a) {
		pkg = p;
		act = a;
	}
	
	public String getPkgName(){
		return pkg;
	}
	
	public String getActName(){
		return act;
	}
	
	public String getShortActName(){
		String[] names=act.split("\\.");
		return names[names.length-1];
	}
}