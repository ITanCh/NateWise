package com.nata.wise.state;

import java.io.File;
import java.io.IOException;

import com.nata.wise.cmdtool.GetAdb;
import com.nata.wise.cmdtool.ProcRunner;

public class DumpUI {
	
//	String adbPath=null;
//	String outPath=null;
//	File adbFile=null;
	
	public static void main(String[] args){
		String adb="/Users/Tianchi/Tool/sdk/platform-tools/adb";
		String out="/Users/Tianchi/AppTest/dump";
		File outFile=new File(out);
		outFile.mkdirs();
		File xmlDumpFile=new File(outFile,"dump.xml");
		GetAdb.setAdbFile(adb);
		DumpUI.dumpThisDevice("0093e1a0ce9a2fd0", xmlDumpFile);
	}
//	
//	public DumpUI(String adbP,String outP){
//		adbPath=adbP;
//		outPath=outP;
//		
//		adbFile=new File(adbP);
//		if(!adbFile.exists()){
//			System.out.println("error: cannot find adb");
//		}
//	}
//	
//	/**
//	 * start uidump.xml from all devices
//	 * 
//	 * @param adbPath
//	 * @param outPath
//	 */
//	public void startDump() {
//		File sdk = new File(adbPath);
//		File out = new File(outPath);
//
//		out.mkdirs();
//		if (!out.exists())
//			System.out.println("error: cannot create out file!");
//
//		if (!sdk.exists()) {
//			System.out.println("errer:sdk cannot find!");
//			return;
//		}
//
//		GetAdb.setAdbFile(adbPath);
//		AndroidDebugBridge adb = GetAdb.initAdb();
//
//		IDevice[] devices = adb.getDevices();
//		for (IDevice device : devices) {
//			String sn = device.getSerialNumber();
//			File dFile = new File(out, sn);
//			dFile.mkdirs();
//			File dumpFile=new File(dFile,"dump.xml");
//			try {
//				dumpFile.createNewFile();
//			} catch (IOException e) {
//				System.out.println("error: cannot create new dump file");
//				e.printStackTrace();
//			}
//			
//			if (!dumpFile.exists())
//				System.out.println("error: cannot create dump file");
//			
//			dumpThisDevice(sn,dumpFile);
//			//dumpADevice(device,dumpFile);
//		}
//		System.out.println("All dump finished..");
//		AndroidDebugBridge.terminate();
//		return;
//	}

	
	/**
	 * dump uidump.xml in a device
	 * 
	 * @param serial
	 * @param xmlDumpFile
	 */
	public static void dumpThisDevice(String serial,File xmlDumpFile) {

        int retCode = -1;
        String dumpPath="/storage/emulated/legacy/uidump.xml";

		ProcRunner procRunner = GetAdb.getAdbRunner(serial, "shell", "ls",
				"/system/bin/uiautomator");
		try {
			retCode = procRunner.run(30000);
		} catch (IOException e) {
			System.err.println("Failed to detect device");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.err.println("No device or multiple devices connected. "
					+ "Use ANDROID_SERIAL environment variable "
					+ "if you have multiple devices");
			return;
		}
		
		if (procRunner.getOutputBlob().indexOf("No such file or directory") != -1) {
			System.err.println("/system/bin/uiautomator not found on device");
			return;
		}
		
		procRunner = GetAdb.getAdbRunner(serial, "shell", "rm", dumpPath);
		try {
			retCode = procRunner.run(30000);
			if (retCode != 0) {
				throw new IOException(
						"Non-zero return code from \"rm\" xml dump command:\n"
								+ procRunner.getOutputBlob());
			}
		} catch (IOException e) {
			System.err.println("Failed to execute \"rm\" xml dump command.");
			e.printStackTrace();
			return;
		}

		procRunner = GetAdb.getAdbRunner(serial, "shell", "/system/bin/uiautomator",
				"dump", dumpPath);
		try {
			retCode = procRunner.run(30000);
			if (retCode != 0) {
				throw new IOException(
						"Non-zero return code from dump command:\n"
								+ procRunner.getOutputBlob());
			}
		} catch (IOException e) {
			System.err.println("Failed to execute dump command.");
			e.printStackTrace();
			return;
		}
		
		procRunner = GetAdb.getAdbRunner(serial, "pull", dumpPath,
				xmlDumpFile.getAbsolutePath());
		try {
			retCode = procRunner.run(30000);
			//System.out.println("dump uidump.xml finished..");
			if (retCode != 0) {
				throw new IOException(
						"Non-zero return code from pull command:\n"
								+ procRunner.getOutputBlob());
			}
		} catch (IOException e) {
			System.err.println("Failed to pull dump file.");
			e.printStackTrace();
			return;
		}
	}

}
