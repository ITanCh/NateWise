package com.nata.wise.strategy.DFS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Multiset.Entry;
import com.nata.wise.WiseRunner;
import com.nata.wise.event.EventEdge;
import com.nata.wise.state.State;

public class TreeWeb {

	private static String[] webNames = { "js/d3.js", "js/tooltip.css",
			"js/tooltip.js", "js/v3.js", "index.html", "actnet.html" };

	/**
	 * copy file to out put
	 * 
	 * @param resource
	 * @param output
	 */
	private static void copyStaticToOutput(String resource, File output) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = WiseRunner.class.getResourceAsStream("/" + resource);
			File outFile = new File(output, resource);
			File parentFile = outFile.getParentFile();
			parentFile.mkdirs();

			outFile.createNewFile();
			os = new FileOutputStream(outFile);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			throw new RuntimeException("Unable to copy static resource "
					+ resource + " to " + output + "/" + resource, e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	public static void moveWeb(File outFile) {
		for (String webName : webNames) {
			copyStaticToOutput(webName, outFile);
		}
	}

	/**
	 * save the tree struct in the file
	 * 
	 * @param f
	 */
	public static void saveTreeModel(File f, State rootState) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f, false));
			// Ggson gson = new Ggson();
			// gson.toJson(rootNode, out);
			printTree(out, rootState, null);
			out.flush();
			out.close();
		} catch (IOException e) {
			System.err.println("Write tree error!");
			e.printStackTrace();
		}

	}

	/**
	 * print tree
	 * 
	 * @param out
	 * @param node
	 * @param parent
	 * @throws IOException
	 */
	public static void printTree(BufferedWriter out, State node, State parent)
			throws IOException {
		if (node == null)
			return;
		out.write("{");
		out.newLine();
		out.write("\"name\": \"" + node.getIndex() + "\",");

		out.newLine();
		if (parent != null)
			out.write("\"parent\": \"" + parent.getIndex() + "\"");
		else {
			out.write("\"parent\": \"null\"");
		}

		ArrayList<EventEdge> edges = node.getOutEdges();
		int size = edges.size();
		if (size > 0) {
			out.write(",");
			out.newLine();
			out.write("\"children\": [");
			out.newLine();

			int i = 0;
			for (EventEdge edge : edges) {

				printTree(out, edge.getToState(), node);
				i++;
				if (i != size) {
					out.write(",");
					out.newLine();
				}
			}
			out.newLine();
			out.write("]");
		}
		out.newLine();
		out.write("}");
	}

	/**
	 * Save node information.
	 * 
	 * @param f
	 */
	public static void saveNodes(File f, ArrayList<State> nodes) {
		assert f != null;
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f, false));
			String t1 = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
			out.write(t1);
			out.newLine();
			out.write("<catalog>");
			out.newLine();

			for (State vn : nodes) {
				out.write("<node>");
				out.newLine();

				out.write("<title>" + vn.getPkgAct().getActName() + "</title>");
				out.newLine();
				out.write("<view>" + vn.getKind() + "</view>");
				out.newLine();
				out.write("<action>" + vn.getActionsSize() + "</action>");
				out.newLine();

				EventEdge tempEdge = vn.getFromEdge();
				if (tempEdge != null)
					out.write("<edge>" + vn.getFromEdge().toString()
							+ "</edge>");
				else
					out.write("<edge>null</edge>");
				out.newLine();

				out.write("</node>");
				out.newLine();
			}

			out.write("</catalog>");
			out.flush();
			out.close();
		} catch (IOException e) {
			System.err.println("Write nodes error!");
			e.printStackTrace();
		}
	}

	/**
	 * Save activity connections.
	 * 
	 * @param actnetFile
	 */
	public static void saveActnet(File f, ArrayList<State> nodes) {
		ArrayList<String> actList = new ArrayList<>();
		LinkedHashSet<Link> linkSet = new LinkedHashSet<>();
		for (State node : nodes) {
			String name1 = node.getPkgAct().getActName();
			if (!actList.contains(name1))
				actList.add(name1);

			int index1 = 0;
			for (String n : actList) {
				if (n.equals(name1))
					break;
				index1++;
			}

			for (EventEdge oe : node.getOutEdges()) {
				String name2 = oe.getToState().getPkgAct().getActName();
				if (name1.equals(name2))
					continue;

				if (!actList.contains(name2))
					actList.add(name2);

				int index2 = 0;
				for (String n : actList) {
					if (n.equals(name2))
						break;
					index2++;
				}
				linkSet.add(new Link(index1, index2));
			}
		}

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f, false));
			out.write("actnet = {");
			out.newLine();
			out.write("nodes: [");
			out.newLine();
			for (String name : actList) {
				out.write("{name: \"" + name + "\"},");
				out.newLine();
			}
			out.write(" ],");
			out.newLine();

			out.write("edges: [");
			out.newLine();
			Iterator<Link> it = linkSet.iterator();
			while (it.hasNext()) {
				out.write(it.next().toString() + ",");
				out.newLine();
			}
			out.write("]");
			out.newLine();
			out.write("};");

			out.flush();
			out.close();
		} catch (IOException e) {
			System.out.println("print actnet error!");
			e.printStackTrace();
		}

	}

	/**
	 * Save test cases. Every case consist of actions from root to leaf.
	 * 
	 * @param reappearFile
	 * @param nodes
	 * @param serial
	 * @param pkgact
	 */
	public static void saveCase(File reappearFile, ArrayList<State> nodes,
			String serial, String pkgact) {
		File caseFile = new File(reappearFile, "case");
		caseFile.mkdirs();
		if (!caseFile.exists())
			System.err.println("Cannot create case file!");

		String startString = "adb -s " + serial + " shell am start -S -n "
				+ pkgact;

		State preState = null;
		for (State node : nodes) {
			if (node.equals(preState))
				continue;
			preState = node;
			// find leaf nodes
			if (node.getOutEdges().size() <= 0) {
				File nodeFile = new File(caseFile, node.getIndex() + ".sh");
				try {
					nodeFile.createNewFile();
					BufferedWriter out = new BufferedWriter(new FileWriter(
							nodeFile, false));
					out.write("#! /bin/sh");
					out.newLine();
					out.write(startString);
					out.newLine();
					out.write("sleep 3");
					out.newLine();

					Stack<EventEdge> stack = new Stack<>();
					State lState = node;
					while (lState.getFromEdge() != null) {
						stack.push(lState.getFromEdge());
						lState = lState.getFromEdge().getFromState();
					}

					while (!stack.empty()) {
						out.write(stack.pop().toCommand(serial));
						out.newLine();
						out.write("sleep 1");
						out.newLine();
					}

					out.flush();
					out.close();
					Runtime.getRuntime().exec(
							"chmod u+x " + nodeFile.getAbsolutePath());

				} catch (IOException e) {
					System.err.println("Cannot create case.sh file!");
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Search shorter paths to start activities.
	 * 
	 * @param reappearFile
	 * @param nodes
	 * @param serial
	 * @param pkgact
	 */
	public static void saveActLaunch(File reappearFile, ArrayList<State> nodes,
			String serial, String pkgact) {

		File actFile = new File(reappearFile, "activity");
		actFile.mkdirs();
		if (!actFile.exists())
			System.err.println("Cannot create activity file!");

		String startString = "adb -s " + serial + " shell am start -S -n "
				+ pkgact;

		Map<String, State> map=new HashMap<String,State>();
		
		//search shorter path to start this activity
		for (State node : nodes) {
			int kind=node.getKind();
			if(kind==node.NORMAL||kind==node.OLD){
				String name=node.getPkgAct().getActName();
				if(map.containsKey(name)){
					State lState=map.get(name);
					if(pathLength(node)<pathLength(lState)){
						map.put(name, node);
					}
				}else{
					map.put(name, node);
				}
			}
		}
		
		for(String key:map.keySet()){
			State node=map.get(key);
			
			String name=key.replace('.', '_');
			File nodeFile = new File(actFile, name + ".sh");
			try {
				nodeFile.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(
						nodeFile, false));
				out.write("#! /bin/sh");
				out.newLine();
				out.write(startString);
				out.newLine();
				out.write("sleep 3");
				out.newLine();

				Stack<EventEdge> stack = new Stack<>();
				State lState = node;
				while (lState.getFromEdge() != null) {
					stack.push(lState.getFromEdge());
					lState = lState.getFromEdge().getFromState();
				}

				while (!stack.empty()) {
					out.write(stack.pop().toCommand(serial));
					out.newLine();
					out.write("sleep 1");
					out.newLine();
				}

				out.flush();
				out.close();
				Runtime.getRuntime().exec(
						"chmod u+x " + nodeFile.getAbsolutePath());

			} catch (IOException e) {
				System.err.println("Cannot create case.sh file!");
				e.printStackTrace();
			}
		}
		

	}

	private static int pathLength(State state){
		int len=0;
		
		while(state.getFromEdge()!=null)
		{
			len++;
			state=state.getFromEdge().getFromState();
		}
		return len;
	}
	
	public static void main(String[] args) {
		LinkedHashSet<Link> set = new LinkedHashSet<>();
		TreeWeb tWeb = new TreeWeb();

		Link l1 = new Link(1, 4);
		Link l2 = new Link(1, 4);
		Link l3 = new Link(1, 4);
		set.add(l1);
		set.add(l2);
		set.add(l3);

		System.out.println(set.size());

	}

}
