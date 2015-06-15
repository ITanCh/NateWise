package com.nata.wise.cmdtool;

import java.util.ArrayList;
import java.util.List;

import com.android.ddmlib.AndroidDebugBridge;

public class GetAdb {

	private static String adbPath = "adb";

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
		AndroidDebugBridge adb = AndroidDebugBridge.createBridge(adbPath, false);
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
}
