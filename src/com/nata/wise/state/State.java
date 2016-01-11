package com.nata.wise.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nata.wise.event.BasicAction;
import com.nata.wise.event.ClickAction;
import com.nata.wise.event.EventEdge;
import com.nata.wise.event.MenuAction;

public class State {

	public static final int NORMAL = 1;
	public static final int OLD = 2;
	public static final int OUT = 3;
	public static final int ERROR = 4;
	public static final int SAME = 5;

	private PkgAct mPkgAct;
	private LinkedHashSet<BasicAction> actions;
	private Iterator<BasicAction> it;
	private int kind = 1;
	private int index = 0;

	private ArrayList<EventEdge> outEdges;
	private EventEdge fromEdge = null;

	public State(PkgAct pa, NodeList nl) {
		mPkgAct = pa;
		actions = getActions(nl);
		it = actions.iterator();

		outEdges = new ArrayList<>();
	}

	private LinkedHashSet<BasicAction> getActions(NodeList nl) {

		LinkedHashSet<BasicAction> allActions = new LinkedHashSet<BasicAction>();
		if (nl == null)
			return allActions;

		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;

				String className = eElement.getAttribute("class");
				if (className.equals("android.widget.EditText")) {

				} else {
					// clickable
					String clickable = eElement.getAttribute("clickable");
					if (clickable.equals("true")) {
						String bounds = eElement.getAttribute("bounds");
						String id = eElement.getAttribute("resource-id");

						// System.out.println(bounds);
						String[] segs = bounds.split("\\[|\\]|,");
//						 for(String s:segs)
//						 System.out.println(s);
						if (segs.length == 6) {
							int x = (Integer.parseInt(segs[1]) + 1);
							int y = (Integer.parseInt(segs[2]) + 1);
							allActions.add(new ClickAction(x, y));
						}
					}
				}

			}
		}
		allActions.add(new MenuAction());
		return allActions;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public ArrayList<EventEdge> getOutEdges() {
		return outEdges;
	}

	public void addOutEdges(EventEdge oe) {
		outEdges.add(oe);
	}

	public EventEdge getFromEdge() {
		return fromEdge;
	}

	public void setFromEdge(EventEdge fromEdge) {
		this.fromEdge = fromEdge;
	}

	public PkgAct getPkgAct() {
		return mPkgAct;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public BasicAction getAction() {
		if (it.hasNext())
			return it.next();
		return null;
	}

	public int getActionsSize() {
		return actions.size();
	}

	public boolean isNotOver() {
		return it.hasNext();
	}

	@Override
	public String toString() {
		String all = "";
		all += mPkgAct.getPkgName() + ":" + mPkgAct.getActName() + "\n";
		all += (kind + "\n");
		Iterator<BasicAction> it = actions.iterator();
		while (it.hasNext()) {
			BasicAction ba = it.next();
			all += ba.toString() + "\n";
		}
		return all;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (this == o)
			return true;

		if (!(o instanceof State))
			return false;

		State oState = (State) o;
		if (mPkgAct.equals(oState.mPkgAct)
				&& actions.size() == oState.actions.size()) {
			if(actions.size()==0)return true;
			
			int count = 0;
			Iterator<BasicAction> it = oState.actions.iterator();
			while (it.hasNext()) {
				if (!actions.contains(it.next()))
					count++;
			}
			float rate = ((float) count) / ((float) actions.size());
			if (rate < 0.5)
				return true;
			else {
				System.out.println(this.toString());
				System.out.println("=== vs ===");
				System.out.println(o.toString());
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 0;
		Iterator<BasicAction> it = actions.iterator();
		while (it.hasNext()) {
			BasicAction ba = it.next();
			result += ba.hashCode();
		}
		return result;
	}

	public void addAction(MenuAction menuAction) {
		// TODO Auto-generated method stub
		actions.add(menuAction);
		it=actions.iterator();
	}
}
