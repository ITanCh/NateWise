package com.nata.wise;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.nata.wise.cmdtool.GetAdb;
import com.nata.wise.strategy.DFS.DFSTree;

public class WiseRunner {

	private static ExecutorService pool = Executors.newCachedThreadPool();
	public static String hopePkg="";
	/**
	 * start test
	 * 
	 * @param pkgName
	 * @param actName
	 * @param adbPath
	 * @param out
	 */
	public static void startWise(final String pkgName, final String actName,
			String adbPath, final String out) {
		hopePkg=pkgName;
		GetAdb.setAdbFile(adbPath);
		AndroidDebugBridge adb = GetAdb.initAdb();

		IDevice[] devices = adb.getDevices();
		for (IDevice device : devices) {
			final String serial = device.getSerialNumber();
			pool.execute(new Thread() {
				@Override
				public void run() {
					System.out.println("Start test in: "+serial);
					DFSTree tree = new DFSTree(serial, pkgName, actName, out);
					tree.startTrace();
				}
			});
		}

		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("pool finish error!");
			e.printStackTrace();
		}
		
		AndroidDebugBridge.terminate();
	}
	
	public static void main(String[] args) {

//		String pkg = "com.cvicse.smarthome";
//		String act = "com.cvicse.smarthome.guide.SplashActivity";
//		String pkg = "com.cvicse.zhnt";
//		String act = "com.cvicse.zhnt.LoadingActivity";
//		String adbPath="/Users/Tianchi/Tool/sdk/platform-tools/adb";
//		String outPath="/Users/Tianchi/AppTest/out";
		if(args.length<4)
		{
			System.err.println("enter pkgName actName adbPaht outPaht");
			return;
		}
		startWise(args[0], args[1], args[2], args[3]);
//		startWise(pkg, act, adbPath, outPath);
	}
}
