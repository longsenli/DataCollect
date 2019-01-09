package com.tnpy.datacollector.modbus.kcm9;

import com.tnpy.datacollector.SerialTool;
import com.tnpy.datacollector.Utilities;

import gnu.io.SerialPort;

public class Sender extends Thread {

    SerialPort serialPort;
    int num;

    public Sender(SerialPort serialPort, int num) {
	this.serialPort = serialPort;
	this.num = num;
    }

    @Override
    public void run() {
	while (true) {
	    try {
		// 向各个仪表发出读实时温度命令
		for (int m = 1; m <= num; m++) {
		    // 余姚精创仪表有限公司KCM-91WRS
		    String orderWithoutCrc = String.format("%02x", m).toUpperCase() + "0310010001";
		    String crc = Utilities.getCRC16(Utilities.hex2Bytes(orderWithoutCrc));
		    byte[] order = Utilities.hex2Bytes(orderWithoutCrc + crc);
		    SerialTool.sendToPort(serialPort, order);
		    // 等待一个数据处理后，再请求下一个数据
		    sleep(1000);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
