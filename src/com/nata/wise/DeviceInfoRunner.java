package com.nata.wise;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.nata.wise.cmdtool.GetAdb;

public class DeviceInfoRunner {

	public static void main(String[] args) {
		String adbPath="/Users/Tianchi/Tool/sdk/platform-tools/adb";
		String outPath="/Users/Tianchi/out";
		DeviceInfoRunner.start(adbPath, outPath);
	}

	/**
	 * start get devices information connected to me
	 * save information in xml
	 * 
	 * @param sdkPath
	 */
	public static void start(String sdkPath,String outPath){
		File sdk=new File(sdkPath);
		File out=new File(outPath);
			
		out.mkdir();
		if(!out.exists())
			System.out.println("error: cannot create out file");
		
		
		if(!sdk.exists()){
			System.out.println("errer:sdk cannot find!");
			return;
		}
		
		GetAdb.setAdbFile(sdkPath);
		AndroidDebugBridge adb = GetAdb.initAdb();
		
		IDevice[] devices = adb.getDevices();
		ArrayList<DeviceModel> models = new ArrayList<DeviceModel>();
		for (IDevice device : devices) {
			DeviceModel model = new DeviceModel();
			model.setSerialNumber(device.getSerialNumber());

			try {
				model.setApi(device
						.getPropertyCacheOrSync(IDevice.PROP_BUILD_API_LEVEL));
				model.setBuildVersion(device
						.getPropertyCacheOrSync(IDevice.PROP_BUILD_VERSION));
				model.setCpuAbi(device
						.getPropertyCacheOrSync(IDevice.PROP_DEVICE_CPU_ABI));
				model.setManufacturer(device
						.getPropertyCacheOrSync(IDevice.PROP_DEVICE_MANUFACTURER));
				model.setModel(device
						.getPropertyCacheOrSync(IDevice.PROP_DEVICE_MODEL));
			} catch (TimeoutException | AdbCommandRejectedException
					| ShellCommandUnresponsiveException | IOException e) {
				System.out.println("error:get devices info");
				e.printStackTrace();
			}
			models.add(model);
		}
		
		System.out.println("Get ["+models.size()+"] devices");
		for(DeviceModel model:models){
			File modelFile=new File(out,model.getSerialNumber()+".xml");
			try {
				modelFile.createNewFile();
				model.makeMeXml(modelFile);
			} catch (IOException e) {
				System.out.println("error: cannot not create model file");
				e.printStackTrace();
			}
		}
		
		AndroidDebugBridge.terminate();
		System.out.println("Devices information get finished !");
	}
	
	
}
