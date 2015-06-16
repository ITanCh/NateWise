package com.nata.wise.strategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.nata.wise.cmdtool.GetAdb;
import com.nata.wise.event.BasicAction;
import com.nata.wise.event.EventEdge;
import com.nata.wise.state.State;
import com.nata.wise.state.StateFactory;

public class DFSTree {

	private ArrayList<State> nodes = new ArrayList<>();;
	private State rootState;
	private State currentNode;
	private ArrayList<BasicAction> currentActions = new ArrayList<>();

	private String serial;

	private String pkgName;
	private String actName;
	private File imageFile;
	private File modelFile;

	private int nodeCount = 0;
	ExecutorService pool = Executors.newCachedThreadPool();

	public DFSTree(String s, String pn, String act, String out) {

		serial = s;
		pkgName = pn;
		actName = act;

		File outFile = new File(out);
		outFile = new File(outFile, s);
		imageFile = new File(outFile, "image");
		modelFile = new File(outFile, "model");
		imageFile.mkdirs();
		modelFile.mkdirs();
		if (!imageFile.exists() || !modelFile.exists()) {
			System.err.println("error: cannot create outfile in device: " + s);
		}

		startApp();
		wait(2000);

		rootState = StateFactory.createState(s);
		currentNode = rootState;
		addNode(rootState);

	}

	private int classifyNode(State node) {
		String lPkg = node.getPkgAct().getPkgName();
		int k = State.NORMAL;

		// out of this app
		if (!lPkg.equals(pkgName)) {
			node.setKind(State.OUT);
			k = State.OUT;
		} else {
			// nothing change
			if (currentNode != null && currentNode.equals(node)) {
				return State.SAME;
			}
			if (nodes.contains(node)) {
				node.setKind(State.OLD);
				k = State.OLD;
			}
		}
		return k;
	}

	private void addNode(State node) {
		node.setIndex(nodeCount);
		File shotFile = new File(imageFile, nodeCount + ".png");
		pool.execute(new Thread() {
			@Override
			public void run() {
				GetAdb.takeScreenShot(serial, shotFile.getAbsolutePath());
			}
		});

		if (nodeCount != 0) {
			new EventEdge(currentNode, node, currentActions);
			currentActions.clear();
		}

		nodeCount++;

		nodes.add(node);
	}

	private boolean goBack() {
		EventEdge ee = currentNode.getFromEdge();
		if (ee != null) {
			currentNode = ee.getFromState();

			while (!currentNode.isNotOver()
					&& currentNode.getFromEdge() != null) {
				currentNode = currentNode.getFromEdge().getFromState();
			}

			Stack<State> nodesStack = new Stack<>();
			Stack<EventEdge> edgesStack = new Stack<>();

			State tempState = currentNode;
			while (tempState.getFromEdge() != null) {
				edgesStack.push(tempState.getFromEdge());
				tempState = tempState.getFromEdge().getFromState();
				nodesStack.push(tempState);
			}

			// attempt to one step back
			if (edgesStack.size() > 2) {
				GetAdb.back(serial);
				wait(200);
			}

			tempState = StateFactory.createState(serial);
			if (currentNode.equals(tempState))
				return true;

			if (nodesStack.contains(tempState)) {
				// this node is ancestor of current node
				while (!nodesStack.isEmpty()
						&& !nodesStack.peek().equals(tempState)) {
					nodesStack.pop();
					edgesStack.pop();
				}
			} else {
				startApp();
				wait(2000);
			}

			while (!edgesStack.isEmpty()) {
				EventEdge pe = edgesStack.pop();
				pe.play(serial);
				System.out.println("replay: " + pe.toString());
			}

			tempState = StateFactory.createState(serial);
			if (currentNode.equals(tempState))
				return true;
			else {
				System.out.println("go to a wrong state!");
				goBack();
			}
		}
		return false;
	}

	private void startApp() {
		GetAdb.startActivity(serial, pkgName + "/" + actName);
	}

	private void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			System.out.println("Cannot sleep...");
			e.printStackTrace();
		}
	}

	public BasicAction perfromAction() {
		BasicAction ba = currentNode.getAction();
		if (ba != null) {
			ba.perform(serial);
			System.out.println(ba.toString());
		}
		return ba;
	}

	private void saveTree() {
		File treeFile = new File(modelFile, "treemodel.json");
		File nodesFile = new File(modelFile, "nodes.xml");
		try {
			treeFile.createNewFile();
			nodesFile.createNewFile();
		} catch (IOException e) {
			System.out.println("cannot create tree model file!");
			e.printStackTrace();
		}

		saveTreeModel(treeFile);
		saveNodes(nodesFile);
	}

	/**
	 * save the tree struct in the file
	 * 
	 * @param f
	 */
	private void saveTreeModel(File f) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f, false));
			// Ggson gson = new Ggson();
			// gson.toJson(rootNode, out);
			printTree(out, rootState, null);
			out.flush();
			out.close();
		} catch (IOException e) {
			System.out.println("Write tree error!");
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
	private void printTree(BufferedWriter out, State node, State parent)
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
	 * save node information in file
	 * 
	 * @param f
	 */
	public void saveNodes(File f) {
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
			System.out.println("Write nodes error!");
			e.printStackTrace();
		}
	}

	/**
	 * start DFS Trace
	 */
	public void startTrace() {

		// 直到回到根节点，切节点没有可以再出发的事件
		while (!(currentNode.getFromEdge() == null && !currentNode.isNotOver())) {
			// 执行
			BasicAction tempAction = perfromAction();
			if (tempAction == null) {
				goBack();
				continue;
			}

			wait(500);
			// 状态
			State tempNode = StateFactory.createState(serial);
			int kind = classifyNode(tempNode);

			switch (kind) {
			case State.ERROR:
			case State.OLD:
			case State.OUT:
				System.out.println("special node");
				currentActions.add(tempAction);
				addNode(tempNode);
				currentNode = tempNode;
				goBack();
				break;
			case State.SAME:
				System.out.println("same node");
				break;
			default:
				currentActions.add(tempAction);
				addNode(tempNode);
				currentNode = tempNode;
				System.out.println("new node");
				System.out.println(tempNode.toString());
				break;
			}
		}

		saveTree();
	}

	public static void main(String[] args) {

		String pkg = "com.cvicse.zhnt";
		String act = "com.cvicse.zhnt.LoadingActivity";
		String serial = "0093e1a0ce9a2fd0";

		GetAdb.setAdbFile("/Users/Tianchi/Tool/sdk/platform-tools/adb");
		DFSTree tree = new DFSTree(serial, pkg, act,
				"/Users/Tianchi/AppTest/out");
		tree.startTrace();
	}

}
