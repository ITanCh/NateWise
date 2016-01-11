package com.nata.wise.cmdtool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.android.ddmlib.AndroidDebugBridge;
import com.nata.wise.state.PkgAct;
import com.nata.wise.strategy.DFS.DFSTree;

public class GetAdb {

	private static String adbPath = "adb";

	public static int KEYCODE_BACK = 4;
	public static int KEYCODE_POWER = 26;
	public static int KEYCODE_MENU = 82; // menu

	/**
	 * reset adb path
	 * 
	 * @param path
	 */
	public static void setAdbFile(String path) {
		adbPath = path;
	}

	/**
	 * Get an {@link com.android.ddmlib.AndroidDebugBridge} instance given an
	 * SDK path.
	 */
	public static AndroidDebugBridge initAdb() {
		AndroidDebugBridge.init(false);
		AndroidDebugBridge adb = AndroidDebugBridge
				.createBridge(adbPath, false);
		waitForAdb(adb);
		return adb;
	}

	private static void waitForAdb(AndroidDebugBridge adb) {
		for (int i = 1; i < 10; i++) {
			try {
				Thread.sleep(i * 100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (adb.isConnected()) {
				return;
			}
		}
		throw new RuntimeException("Unable to connect to adb.");
	}

	/*
	 * Convenience function to construct an 'adb' command, e.g. use 'adb' or
	 * 'adb -s NNN'
	 */
	public static ProcRunner getAdbRunner(String serial, String... command) {
		List<String> cmd = new ArrayList<String>();
		cmd.add(adbPath);
		if (serial != null) {
			cmd.add("-s");
			cmd.add(serial);
		}
		for (String s : command) {
			cmd.add(s);
		}
		return new ProcRunner(cmd);
	}

	/**
	 * start a activity
	 * 
	 * @param serial
	 * @param pkgact
	 */
	public static void startActivity(String serial, String pkgact) {
		ProcRunner procRunner = getAdbRunner(serial, "shell", "am", "start",
				"-S", "-n", pkgact);

		int retCode = 0;
		try {
			retCode = procRunner.run(3000);
		} catch (IOException e) {
			System.err.println("Failed to start activity!");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.err.println("start activity error!\n"
					+ procRunner.getOutputBlob());
			return;
		}
	}

	/**
	 * stop an application
	 * 
	 * @param serial
	 * @param pkg
	 */
	public static void stopApp(String serial, String pkg) {
		ProcRunner procRunner = getAdbRunner(serial, "shell", "am",
				"force-stop", pkg);

		int retCode = 0;
		try {
			retCode = procRunner.run(3000);
		} catch (IOException e) {
			System.err.println("Failed to stop app");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.err
					.println("stop app error!\n" + procRunner.getOutputBlob());
			return;
		}
	}

	/**
	 * take screenshot from the device
	 * 
	 * @param serial
	 * @param path
	 */
	public static void takeScreenShot(String serial, String path) {
		// String s2 = "'s/\\x0D\\x0A/\\x0A/g'";
		// String s3 = ">";
		// System.out.println(s2);
		//
		// ProcRunner procRunner = getAdbRunner(serial, "shell", "screencap",
		// "-p", "|", "perl", "-pe", s2, s3, path);

		String picPath = "/storage/emulated/legacy/screen.png";
		ProcRunner procRunner = getAdbRunner(serial, "shell", "screencap",
				"-p", picPath);
		int retCode = 0;
		try {
			retCode = procRunner.run(5000);
		} catch (IOException e) {
			System.err.println("Failed to take screenshot");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.err.println("take screenshot error!\n"
					+ procRunner.getOutputBlob());
			return;
		}

		procRunner = getAdbRunner(serial, "pull", picPath, path);
		retCode = 0;
		try {
			retCode = procRunner.run(5000);
		} catch (IOException e) {
			System.err.println("Failed to pull screenshot");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.err.println("pull screenshot error!\n"
					+ procRunner.getOutputBlob());
			return;
		}

		procRunner = getAdbRunner(serial, "shell", "rm", picPath);
		retCode = 0;
		try {
			retCode = procRunner.run(5000);
		} catch (IOException e) {
			System.out.println("Failed to rm screenshot");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.out.println("rm screenshot error!\n"
					+ procRunner.getOutputBlob());
			return;
		}

	}

	public static void sentKey(String serial, int num) {

		ProcRunner procRunner = getAdbRunner(serial, "shell", "input",
				"keyevent", num + "");

		int retCode = 0;
		try {
			retCode = procRunner.run(5000);
		} catch (IOException e) {
			System.err.println("Failed to back!");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.err.println("back error!\n" + procRunner.getOutputBlob());
			return;
		}
	}

	/**
	 * get current package name and activity name
	 * 
	 * @param serial
	 * @return
	 */
	public static PkgAct getCurrentPkgAct(String serial) {

		PkgAct pkgAct = null;
		int getCount = 0;
		while (pkgAct == null) {
			if (getCount++ > 10) {
				System.err.print("Cannot get Current PkgAct");
				break;
			}
			DFSTree.wait(500);

			int retCode = 0;
			ProcRunner procRunner = GetAdb.getAdbRunner(serial, "shell",
					"dumpsys window windows");
			try {
				retCode = procRunner.run(30000);
			} catch (IOException e) {
				System.err.println("Failed to get pkgact name");
				e.printStackTrace();
				return null;
			}
			if (retCode != 0) {
				System.err.println("Error: cannot get pkgact name\n"
						+ procRunner.getOutputBlob());
				return null;
			}

			String result = procRunner.getOutputBlob();
			String[] lines = result.split("\n");
			String targetLine = null;
			String currentLine = null;
			for (String line : lines) {
				if (line.contains("mFocusedApp"))
					targetLine = line;
				if (line.contains("mCurrentFocus"))
					currentLine = line;
			}
			if (targetLine == null || currentLine == null)
				continue;

			// System.out.println(result);

			String pkg = null;
			String act = null;
			if (currentLine.contains("Error")) {
				act = "Error";
			} else {
				String[] segments = currentLine.split("\\{| |\\}|/|\n");
				// for(String s:segments)System.out.println(s+"!");
				if (segments.length == 7) {
					pkg = segments[5];
					act = segments[6];
					if (act.startsWith("."))
						act = pkg + act;
				}
			}
			if (act == null || pkg == null) {
				String[] segments = targetLine.split("\\{| |\\}|/|\n");
				// for(String s:segments)System.out.println(s+"!");
				if (segments.length == 12) {
					pkg = segments[9];
					if (act == null) {
						act = segments[10];
						if (act.startsWith("."))
							act = pkg + act;
					}
				}
			}
			if (act != null && pkg != null)
				pkgAct = new PkgAct(pkg, act);
		}
		//System.out.println(pkgAct.toString());
		return pkgAct;
	}

	public static void main(String[] agrs) {
		String pkg = "com.cvicse.zhnt";
		String act = "com.cvicse.zhnt.LoadingActivity";
		String serial = "0093e1a0ce9a2fd0";
		String path = "/Users/Tianchi/AppTest/dump";
		GetAdb.setAdbFile("/Users/Tianchi/Tool/sdk/platform-tools/adb");
		// startActivity("0093e1a0ce9a2fd0", pkg + "/" + act);
		// stopApp(serial, pkg);
		// takeScreenShot(serial, path + "/shot.png");
		getCurrentPkgAct("0093e1a0ce9a2fd0");
	}

}
