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
			System.out.println("Failed to detect device");
			e.printStackTrace();
			return;
		}
		if (retCode != 0) {
			System.out.println("No device or multiple devices connected. "
					+ "Use ANDROID_SERIAL environment variable "
					+ "if you have multiple devices");
			return;
		}
	}

	public String toString() {
		String string = "Click (" + x + ", " + y + ")";
		return string;
	}

	@Override
	public boolean equals(Object sa) {
		if (sa != null && sa instanceof ClickAction) {
			ClickAction lSmallAction = (ClickAction) sa;
			if (x == lSmallAction.x && y == lSmallAction.y)
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = x * 31 + y * 7 + x * y;
		return result;
	}

}
