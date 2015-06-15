package com.nata.wise;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class DeviceModel {
	private String serialNumber = ""; // * serial number for identifying a
										// device
	private String manufacturer = ""; // producer
	private String model = ""; // model
	private String api = "";
	private String buildVersion = "";
	private String cpuAbi = "";

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	public String getCpuAbi() {
		return cpuAbi;
	}

	public void setCpuAbi(String cpuAbi) {
		this.cpuAbi = cpuAbi;
	}
	
	public void makeMeXml(File f) throws FileNotFoundException {
		XMLEncoder encoder;

		encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(
				f)));
		encoder.writeObject(this);
		encoder.close();

	}

}
