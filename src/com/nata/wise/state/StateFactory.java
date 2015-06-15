package com.nata.wise.state;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.NodeList;

import com.nata.wise.cmdtool.GetAdb;
import com.nata.wise.cmdtool.ProcRunner;

public class StateFactory {

	private static XMLParser mXmlParser = new XMLParser();

	public static State createState(String serial, File out) {

		if (!out.exists()) {
			System.out.println("error: out file not exist!");
			return null;
		}

		File xmlDumpFile = new File(out, "dump.xml");
		if (!xmlDumpFile.exists())
			try {
				xmlDumpFile.createNewFile();
			} catch (IOException e) {
				System.out.println("dump.xml cannot create!");
				e.printStackTrace();
			}

		DumpUI.dumpThisDevice(serial, xmlDumpFile);
		NodeList nodeList = mXmlParser.startParser(xmlDumpFile);

		PkgAct lPkgAct = getCurrentPkgAct(serial);
		State lState = new State(lPkgAct, nodeList, 1);

		return lState;
	}

	private static PkgAct getCurrentPkgAct(String serial) {
		int retCode = 0;
		ProcRunner procRunner = GetAdb.getAdbRunner(serial, "shell",
				"dumpsys window windows | grep -E 'mCurrentFocus'");
		try {
			retCode = procRunner.run(30000);
		} catch (IOException e) {
			System.out.println("Failed to detect device");
			e.printStackTrace();
			return null;
		}
		if (retCode != 0) {
			System.out.println("No device or multiple devices connected. "
					+ "Use ANDROID_SERIAL environment variable "
					+ "if you have multiple devices");
			return null;
		}

		String result = procRunner.getOutputBlob();
		String[] segments = result.split("\\{| |\\}|/");
		String pkg = null;
		String act = null;
		for (int i = 0; i < segments.length; i++) {
			if (segments[i].equals("u0")) {
				if (i + 2 < segments.length) {
					pkg = segments[i + 1];
					act = segments[i + 2];
				}
			}
		}

		if (pkg == null || act == null) {
			System.err.println("error:cannot get pkg and act name");
			return null;
		}
		PkgAct pkgAct = new PkgAct(pkg, act);
		return pkgAct;
	}

	public static void main(String[] args){
		String adb="/Users/Tianchi/Tool/sdk/platform-tools/adb";
		GetAdb.setAdbFile(adb);
		File outFile=new File("/Users/Tianchi/AppTest/dump");
		if(!outFile.exists())
			outFile.mkdirs();
		
		State mState=createState("0093e1a0ce9a2fd0", outFile);
		System.out.println(mState.toString());
	}
}
