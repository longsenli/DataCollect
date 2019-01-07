package com.tnpy.datacollector.serial.kcma9;

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.tnpy.datacollector.Utilities;
import com.tnpy.datacollector.serial.SerialTool;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * 监测数据显示类
 */
public class Board extends Frame {

    static final long serialVersionUID = 1L;
    Monitor client;

    // 通信波特率
    static final int bps = 9600;
    // 串口列表
    List<String> commList = new ArrayList<String>(6);
    // 485仪表的数量
    int num = 147;
    int CDNum = 138;// 充电的仪表
    int SZGZNum = 9;// 树脂干燥的仪表
    // 界面显示小窗口
    Label[] arTem = new Label[147];
    // 串口对应的仪表数
    int[] numList = { 30, 27, 27, 27, 27, 9 };
    // 串口对象列表
    List<SerialPort> serialPortList = new ArrayList<SerialPort>(6);
    // 启动按钮
    Button openSerialButton = new Button("START");
    // 重画时的画布
    Image offScreen;

    /**
     * 类的构造方法
     */
    public Board(Monitor client) {
	this.client = client;
	commList.add("COM4");
	commList.add("COM5");
	commList.add("COM6");
	commList.add("COM7");
	commList.add("COM8");
	commList.add("COM9");
    }

    /**
     * 主菜单窗口显示； 添加Label、按钮、下拉条及相关事件监听；
     */
    @SuppressWarnings("deprecation")
    public void dataFrame() {
	this.setBounds(Monitor.LOC_X, Monitor.LOC_Y, Monitor.WIDTH, Monitor.HEIGHT);
	this.setTitle("实时数据监控");
	this.setBackground(Color.white);
	this.setLayout(null);

	this.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent arg0) {
		if (serialPortList != null) {
		    // 程序退出时关闭串口释放资源
		    for (SerialPort serialPort : serialPortList) {
			SerialTool.closePort(serialPort);
		    }
		}
		System.exit(0);
	    }
	});

	// 显示充电温度
	for (int i = 0; i < CDNum; i++) {
	    int x = 70 + (i % 12) * 125;
	    int y = 100 + 50 * Math.round(i / 12);
	    arTem[i] = new Label("", Label.CENTER);
	    arTem[i].setBounds(x, y, 100, 30);
	    arTem[i].setBackground(Color.black);
	    arTem[i].setFont(new Font("微软雅黑", Font.BOLD, 25));
	    arTem[i].setForeground(Color.white);
	    add(arTem[i]);
	}

	// 显示树脂干燥温度
	for (int i = 0; i < SZGZNum; i++) {
	    int x = 70 + (i % 12) * 125;
	    int y = 770 + 50 * Math.round(i / 12);
	    arTem[i + CDNum] = new Label("", Label.CENTER);
	    arTem[i + CDNum].setBounds(x, y, 100, 30);
	    arTem[i + CDNum].setBackground(Color.black);
	    arTem[i + CDNum].setFont(new Font("微软雅黑", Font.BOLD, 25));
	    arTem[i + CDNum].setForeground(Color.white);
	    add(arTem[i + CDNum]);
	}

	// 添加打开串口按钮
	openSerialButton.setBounds(700, 840, 225, 40);
	openSerialButton.setBackground(Color.lightGray);
	openSerialButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
	openSerialButton.setForeground(Color.darkGray);
	add(openSerialButton);
	// 添加打开串口按钮的事件监听
	openSerialButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		for (int i = 0; i < commList.size(); i++) {
		    try {
			// 打开串口
			SerialPort serialPort = SerialTool.openPort(commList.get(i), bps);
			serialPortList.add(serialPort);
			// 在串口对象上添加监听器
			SerialTool.addListener(serialPort, new SerialListener(serialPort, commList.get(i)));
			// 启动定时请求数据的线程.
			new Sender(serialPort, numList[i]).start();
		    } catch (Exception e1) {
			e1.printStackTrace();
		    }
		}
	    }
	});
	this.setResizable(false);
	new Thread(new RepaintThread()).start(); // 启动重画线程
    }

    /**
     * 画出主界面组件元素
     */
    public void paint(Graphics g) {
	g.setColor(Color.black);
	g.setFont(new Font("微软雅黑", Font.BOLD, 25));
	g.drawString("充电温度", 700, 65);
	g.drawString("树脂干燥温度", 700, 730);
    }

    /**
     * 双缓冲方式重画界面各元素组件
     */
    public void update(Graphics g) {
	if (offScreen == null)
	    offScreen = this.createImage(Monitor.WIDTH, Monitor.HEIGHT);
	Graphics gOffScreen = offScreen.getGraphics();
	Color c = gOffScreen.getColor();
	gOffScreen.setColor(Color.white);
	gOffScreen.fillRect(0, 0, Monitor.WIDTH, Monitor.HEIGHT); // 重画背景画布
	this.paint(gOffScreen); // 重画界面元素
	gOffScreen.setColor(c);
	g.drawImage(offScreen, 0, 0, null); // 将新画好的画布“贴”在原画布上
    }

    /*
     * 重画线程（每隔30毫秒重画一次）
     */
    private class RepaintThread implements Runnable {
	public void run() {
	    while (true) {
		// 调用重画方法
		repaint();
		try {
		    Thread.sleep(30);
		} catch (InterruptedException e) {
		    System.exit(0);
		}
	    }
	}
    }

    /**
     * 以内部类形式创建一个串口监听类
     */
    private class SerialListener implements SerialPortEventListener {
	private SerialPort serialPort;
	private String com;

	public SerialListener(SerialPort serialPort, String com) {
	    this.serialPort = serialPort;
	    this.com = com;
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
			String strData = Utilities.bytes2HexString(data);
			String crc1 = strData.substring(10);
			byte[] partData = new byte[5];
			System.arraycopy(data, 0, partData, 0, 5);
			String crc2 = Utilities.getCRC16(partData);
			if (!crc1.equalsIgnoreCase(crc2)) {
			    System.out.println("接收数据CRC检验错误");
			} else {
			    try {
				// 解析数据,依据温度表的协议
				int address = Utilities.oneByte2Int(data[0]);
				int functionCode = Utilities.oneByte2Int(data[1]);
				// code==3表示上传温度数据
				if (functionCode == 3) {
				    // 数据长度1byte或2byte
				    int dataLength = Utilities.oneByte2Int(data[2]);
				    float temp = 0;
				    if (dataLength == 1) {
					temp = (float) Utilities.oneByte2Int(data[4]) / (float) 10;
				    } else if (dataLength == 2) {
					temp = (float) Utilities.getShort2(data, 3) / (float) 10;
				    }
				    // 更新界面Label值(仪表的地址从1开始，label编号从0开始，此处要减1)
				    if (com.equalsIgnoreCase("com4")) {
					arTem[address - 1].setText(String.valueOf(temp));
				    }
				    if (com.equalsIgnoreCase("com5")) {
					arTem[address - 1 + 30].setText(String.valueOf(temp));
				    }
				    if (com.equalsIgnoreCase("com6")) {
					arTem[address - 1 + 57].setText(String.valueOf(temp));
				    }
				    if (com.equalsIgnoreCase("com7")) {
					arTem[address - 1 + 84].setText(String.valueOf(temp));
				    }
				    if (com.equalsIgnoreCase("com8")) {
					arTem[address - 1 + 111].setText(String.valueOf(temp));
				    }
				    if (com.equalsIgnoreCase("com9")) {
					arTem[address - 1 + 138].setText(String.valueOf(temp));
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
}