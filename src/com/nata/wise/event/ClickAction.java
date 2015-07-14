package com.nata.wise.event;

import java.io.IOException;

import com.nata.wise.cmdtool.GetAdb;
import com.nata.wise.cmdtool.ProcRunner;

public class ClickAction extends BasicAction {

	int x, y;

	public ClickAction(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void perform(String serial) {
		int retCode = 0;
		ProcRunner procRunner = GetAdb.getAdbRunner(serial, "shell", "input",
				"tap", x + "", y + "");
		try {
			retCode = procRunner.run(30000);
		} catch (IOException e) {
			System.err.println("Failed to tap device");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.err.println("tap error: \n" + procRunner.getOutputBlob());
			return;
		}
	}

	public String toString() {
		String string = "Click (" + x + ", " + y + ")";
		return string;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object sa) {
		if (sa == null)
			return false;
		if (sa == this)
			return true;

		if (sa instanceof ClickAction) {
			ClickAction lSmallAction = (ClickAction) sa;
			if (x == lSmallAction.x && y == lSmallAction.y)
				return true;
		}
		return false;
	}

	@Override
	public String toCommand(String serial) {
		String command = "adb -s " + serial + " shell input tap " + x + " " + y;
		return command;
	}

}
