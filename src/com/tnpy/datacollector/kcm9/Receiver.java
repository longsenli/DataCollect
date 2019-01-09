package com.tnpy.datacollector.kcm9;

import java.awt.Label;
import java.util.Map;

import javax.swing.JOptionPane;

import com.tnpy.datacollector.SerialTool;
import com.tnpy.datacollector.Util;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class Receiver implements SerialPortEventListener {
    private SerialPort serialPort;
    private String com;
    Label[] arTem;
    Map<String, String> lastData;

    /**
     * Constructor
     * 
     * @param serialPort 串口对象
     * @param com        串口名称
     * @param arTem      主页面显示小窗口
     * @param lastData   最新采集数据集合
     */
    public Receiver(SerialPort serialPort, String com, Label[] arTem, Map<String, String> lastData) {
	this.serialPort = serialPort;
	this.com = com;
	this.arTem = arTem;
	this.lastData = lastData;
    }

    /**
     * 处理监控到的串口事件
     */
    public void serialEvent(SerialPortEvent serialPortEvent) {
	try {
	    // 等待数据全部到位。
	    Thread.sleep(100);
	} catch (InterruptedException e1) {
	    e1.printStackTrace();
	}
	switch (serialPortEvent.getEventType()) {
	case SerialPortEvent.BI: // 10 通讯中断
	    JOptionPane.showMessageDialog(null, "与串口设备通讯中断", "错误", JOptionPane.INFORMATION_MESSAGE);
	    break;
	case SerialPortEvent.OE: // 7 溢位（溢出）错误
	case SerialPortEvent.FE: // 9 帧错误
	case SerialPortEvent.PE: // 8 奇偶校验错误
	case SerialPortEvent.CD: // 6 载波检测
	case SerialPortEvent.CTS: // 3 清除待发送数据
	case SerialPortEvent.DSR: // 4 待发送数据准备好了
	case SerialPortEvent.RI: // 5 振铃指示
	case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
	    break;
	case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
	    byte[] data = null;
	    try {
		data = SerialTool.readFromPort(serialPort); // 读取数据，存入字节数组
		// 余姚精创仪表有限公司 KCM-91WRS温度变送器
		if (data == null || data.length != 7) { // 检查数据是否读取正确
		    System.out.println("读取数据过程中未获取到有效数据");
		} else {
		    // CRC校验
		    String strData = Util.bytes2HexString(data);
		    String crc1 = strData.substring(10);
		    byte[] partData = new byte[5];
		    System.arraycopy(data, 0, partData, 0, 5);
		    String crc2 = Util.getCRC16(partData);
		    if (!crc1.equalsIgnoreCase(crc2)) {
			System.out.println("接收数据CRC检验错误");
		    } else {
			try {
			    // 解析数据,依据温度表的协议
			    int address = Util.oneByte2Int(data[0]);
			    int functionCode = Util.oneByte2Int(data[1]);
			    // code==3表示上传温度数据
			    if (functionCode == 3) {
				// 数据长度1byte或2byte
				int dataLength = Util.oneByte2Int(data[2]);
				float temp = 0;
				if (dataLength == 1) {
				    temp = (float) Util.oneByte2Int(data[4]) / (float) 10;
				} else if (dataLength == 2) {
				    temp = (float) Util.getShort(data, 3) / (float) 10;
				}
				// 更新界面Label值
				int boardNo = 0;
				if (com.equalsIgnoreCase("com4")) {
				    boardNo = address;
				}
				if (com.equalsIgnoreCase("com5")) {
				    boardNo = address + 30;
				}
				if (com.equalsIgnoreCase("com6")) {
				    boardNo = address + 57;
				}
				if (com.equalsIgnoreCase("com7")) {
				    boardNo = address + 84;
				}
				if (com.equalsIgnoreCase("com8")) {
				    boardNo = address + 111;
				}
				if (com.equalsIgnoreCase("com9")) {
				    boardNo = address + 138;
				}
				// 仪表的地址从1开始，label编号从0开始，此处要减1
				arTem[boardNo - 1].setText(String.valueOf(temp));

				// 将新数据保存
				if (com.equalsIgnoreCase("com9")) {
				    lastData.put(String.valueOf(390000 + boardNo), String.valueOf(temp));
				} else {
				    lastData.put(String.valueOf(381000 + boardNo), String.valueOf(temp));
				}
			    }
			} catch (ArrayIndexOutOfBoundsException e) {
			    System.out.println("数据解析过程出错，更新界面数据失败！");
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}