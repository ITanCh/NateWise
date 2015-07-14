package com.nata.wise.strategy.DFS;

import java.awt.Menu;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.nata.wise.cmdtool.GetAdb;
import com.nata.wise.event.BasicAction;
import com.nata.wise.event.EventEdge;
import com.nata.wise.event.MenuAction;
import com.nata.wise.state.PkgAct;
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
	private File reappearFile;

	private File outFile;

	private int nodeCount = 0;
	ExecutorService pool = Executors.newCachedThreadPool();

	private boolean flag = true;

	public DFSTree(String s, String pn, String act, String out) {

		serial = s;
		pkgName = pn;
		actName = act;

		outFile = new File(out);
		outFile = new File(outFile, s);
		outFile.mkdirs();

		imageFile = new File(outFile, "image");
		modelFile = new File(outFile, "model");
		reappearFile = new File(outFile, "reappear");

		imageFile.mkdirs();
		modelFile.mkdirs();
		reappearFile.mkdirs();
		if (!outFile.exists() || !imageFile.exists() || !modelFile.exists()
				|| !reappearFile.exists()) {
			System.err.println("error: cannot create outfile in device: " + s);
			flag = false;
		}

		// GetAdb.sentKey(serial, GetAdb.KEYCODE_POWER);
		// GetAdb.sentKey(serial, GetAdb.KEYCODE_UNLOCK);
		startApp();
		wait(4000);

		rootState = StateFactory.createState(s);
		String actString = rootState.getPkgAct().getActName();
		String pkgString = rootState.getPkgAct().getPkgName();
		if (actString.contains("Unknow") || actString.contains("Error")) {
			System.err.println("Cannot create app in :" + serial);
			flag = false;
			return;
		}
		if (!(pkgString.equals(pkgName))) {
			System.err.println("Cannot start app in :" + serial);
			flag = false;
			return;
		}
		currentNode = rootState;
		addNode(rootState);
		System.out.println(rootState.toString());
	}

	private int classifyNode(State node) {
		PkgAct lPkg = node.getPkgAct();
		String actString = lPkg.getActName();
		String pkgString = lPkg.getPkgName();
		// out of this app
		if (actString.equals("Error") || actString.equals("Unknow")) {
			node.setKind(State.ERROR);
		}else if (!pkgString.equals(pkgName)) {
			node.setKind(State.OUT);
		} else {
			// nothing change
			if (currentNode != null && currentNode.equals(node)) {
				node.setKind(State.SAME);
			}else if (nodes.contains(node)) {
				node.setKind(State.OLD);
			}
		}
		return node.getKind();
	}

	private void addNode(State node) {

		// get screen shot
		node.setIndex(nodeCount);
		final File shotFile = new File(imageFile, nodeCount + ".png");
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
				GetAdb.sentKey(serial, GetAdb.KEYCODE_BACK);
				wait(1000);
			}

			tempState = StateFactory.createState(serial);

			if (currentNode.equals(tempState))
				return true;

			if (tempState != null && nodesStack.contains(tempState)) {
				// this node is ancestor of current node
				while (!nodesStack.isEmpty()
						&& !nodesStack.peek().equals(tempState)) {
					nodesStack.pop();
					edgesStack.pop();
				}
				System.out.println("one step back");
			} else {
				restartApp();
			}

			while (!edgesStack.isEmpty()) {
				EventEdge pe = edgesStack.pop();
				pe.play(serial);
				// System.out.println("replay: " + pe.toString());
			}

			tempState = StateFactory.createState(serial);
			if (currentNode.equals(tempState))
				return true;
			else {
				System.err.println("go to a wrong state!");
				return goBack();
			}
		}
		return true;
	}

	private void startApp() {
		GetAdb.startActivity(serial, pkgName + "/" + actName);
	}

	private void restartApp() {
		startApp();
		PkgAct rootPa = rootState.getPkgAct();
		int count = 0;
		while (true) {
			wait(1000);
			PkgAct pa = GetAdb.getCurrentPkgAct(serial);
			if (rootPa.equals(pa))
				break;
			count++;
			if (count > 10) {
				System.err.println("Error: cannot start app!");
				return;
			}
		}
	}

	public static void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			System.err.println("Cannot sleep...");
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
		final File treeFile = new File(modelFile, "treemodel.json");
		final File nodesFile = new File(modelFile, "nodes.xml");
		final File actnetFile = new File(modelFile, "actnet.jsonp");
		final File countFile = new File(modelFile, "count.xml");
		try {
			treeFile.createNewFile();
			nodesFile.createNewFile();
			actnetFile.createNewFile();
			countFile.createNewFile();
		} catch (IOException e) {
			System.err.println("cannot create tree model file!");
			e.printStackTrace();
		}

		pool.execute(new Thread() {
			@Override
			public void run() {
				TreeWeb.saveTreeModel(treeFile, rootState);
			}
		});

		pool.execute(new Thread() {
			@Override
			public void run() {
				TreeWeb.saveNodes(nodesFile, nodes);
			}
		});

		pool.execute(new Thread() {
			@Override
			public void run() {
				TreeWeb.saveActnet(actnetFile, nodes);
			}
		});

		pool.execute(new Thread() {
			@Override
			public void run() {
				TreeWeb.saveCase(reappearFile, nodes, serial, pkgName + "/"
						+ actName);
			}
		});

		pool.execute(new Thread() {
			@Override
			public void run() {
				TreeWeb.saveActLaunch(reappearFile, nodes, serial, pkgName
						+ "/" + actName);
			}
		});

		pool.execute(new Thread() {
			@Override
			public void run() {
				TreeWeb.moveWeb(outFile);
			}
		});

		pool.execute(new Thread() {
			@Override
			public void run() {
				TreeWeb.countNode(countFile, nodes);
			}
		});

	}

	/**
	 * start DFS Trace
	 */
	public void startTrace() {

		if (!flag)
			return;

		// 直到回到根节点，切节点没有可以再出发的事件
		while (flag
				&& !(currentNode.getFromEdge() == null && !currentNode
						.isNotOver())) {

			// if (nodes.size() > 20)
			// break;

			// 执行
			BasicAction tempAction = perfromAction();
			if (tempAction == null) {
				flag = goBack();
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
				flag = goBack();
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

		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("pool finish error!");
			e.printStackTrace();
		}
		System.out.println("Device: " + serial + " DFS test End!");
	}

	public static void main(String[] args) {

		// String pkg = "com.cvicse.zhnt";
		// String act = "com.cvicse.zhnt.LoadingActivity";

		String pkg = "com.cvicse.smarthome";
		String act = "com.cvicse.smarthome.guide.SplashActivity";

		String serial = "0093e1a0ce9a2fd0";

		GetAdb.setAdbFile("/Users/Tianchi/Tool/sdk/platform-tools/adb");
		DFSTree tree = new DFSTree(serial, pkg, act,
				"/Users/Tianchi/AppTest/out");
		tree.startTrace();
	}

}
