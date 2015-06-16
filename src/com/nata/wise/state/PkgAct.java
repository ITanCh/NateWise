package com.nata.wise.state;

public class PkgAct {
	String pkg, act;

	public PkgAct(String p, String a) {
		pkg = p;
		act = a;
	}

	public String getPkgName() {
		return pkg;
	}

	public String getActName() {
		return act;
	}

	public String getShortActName() {
		String[] names = act.split("\\.");
		return names[names.length - 1];
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PkgAct other = (PkgAct) obj;
		if (act == null) {
			if (other.act != null)
				return false;
		} else if (!act.equals(other.act))
			return false;
		if (pkg == null) {
			if (other.pkg != null)
				return false;
		} else if (!pkg.equals(other.pkg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return pkg + "/" + act;
	}

}