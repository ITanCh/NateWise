package com.nata.wise.event;

import java.io.IOException;

import com.nata.wise.cmdtool.GetAdb;
import com.nata.wise.cmdtool.ProcRunner;

public class MenuAction extends BasicAction {

	@Override
	public void perform(String serial) {
		GetAdb.sentKey(serial, 82);
	}

	public String toString() {
		String string = "Click Menu";
		return string;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result ;
		return result;
	}

	@Override
	public boolean equals(Object sa) {
		if (sa == null)
			return false;
		if (sa == this)
			return true;

		if (sa instanceof MenuAction) {
				return true;
		}
		return false;
	}

	@Override
	public String toCommand(String serial) {
		String command = "adb -s " + serial + " shell input keyevent 82";
		return command;
	}

}
