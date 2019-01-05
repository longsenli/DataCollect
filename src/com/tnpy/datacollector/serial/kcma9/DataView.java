package com.tnpy.datacollector.serial.kcma9;

import java.awt.Button;
import java.awt.Choice;
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
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.tnpy.datacollector.serial.Bytes2HexStr;
import com.tnpy.datacollector.serial.CRC16;
import com.tnpy.datacollector.serial.SerialTool;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * 监测数据显示类
 */
public class DataView extends Frame {

    private static final long serialVersionUID = 1L;
    Client client;

    private List<String> commList; // 保存可用端口号
    private SerialPort serialPort; // 保存串口对象

    // 配置文件
    Properties config;

    // 485仪表的数量
    private int num;
    private Label[] arTem;
    // 最新数据值
    private float[] lastData;
    // 计数器
    private int counter = 0;

    private Choice commChoice = new Choice(); // 串口选择（下拉框）
    private Choice bpsChoice = new Choice(); // 波特率选择

    private Button openSerialButton = new Button("START");

    Image offScreen; // 重画时的画布
    private Font font = new Font("微软雅黑", Font.BOLD, 25);

    /**
     * 类的构造方法
     * 
     * @param client
     */
    public DataView(Client client) {
	this.client = client;
	commList = SerialTool.findPort(); // 程序初始化时就扫描一次有效串口

	try (InputStream is = ClassLoader.getSystemResourceAsStream("com/tnpy/datacollector/config.properties");) {
	    config = new Properties();
	    config.load(is);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	num = Integer.parseInt(config.getProperty("instrument.num"));
	arTem = new Label[num];
	lastData = new float[num];
    }

    /**
     * 主菜单窗口显示； 添加Label、按钮、下拉条及相关事件监听；
     */
    public void dataFrame() {
	this.setBounds(client.LOC_X, client.LOC_Y, client.WIDTH, client.HEIGHT);
	this.setTitle("串口数据采集");
	this.setBackground(Color.white);
	this.setLayout(null);

	this.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent arg0) {
		if (serialPort != null) {
		    // 程序退出时关闭串口释放资源
		    SerialTool.closePort(serialPort);
		}
		System.exit(0);
	    }
	});

	for (int i = 0; i < num; i++) {
	    int x = 140 + (i % 3) * 380;
	    int y = 60 + 70 * Math.round(i / 3);
	    arTem[i] = new Label("", Label.CENTER);
	    arTem[i].setBounds(x, y, 225, 40);
	    arTem[i].setBackground(Color.black);
	    arTem[i].setFont(font);
	    arTem[i].setForeground(Color.white);
	    add(arTem[i]);
	}

	// 添加串口选择选项
	commChoice.setBounds(160, 770, 200, 200);
	// 检查是否有可用串口，有则加入选项中
	if (commList == null || commList.size() < 1) {
	    JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);
	} else {
	    for (String s : commList) {
		commChoice.add(s);
	    }
	}
	add(commChoice);

	// 添加波特率选项
	bpsChoice.setBounds(526, 770, 200, 200);
	bpsChoice.add("1200");
	bpsChoice.add("2400");
	bpsChoice.add("4800");
	bpsChoice.add("9600");
	bpsChoice.add("14400");
	bpsChoice.add("19200");
	bpsChoice.add("115200");
	add(bpsChoice);

	// 添加打开串口按钮
	openSerialButton.setBounds(900, 760, 225, 40);
	openSerialButton.setBackground(Color.lightGray);
	openSerialButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
	openSerialButton.setForeground(Color.darkGray);
	add(openSerialButton);
	// 添加打开串口按钮的事件监听
	openSerialButton.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {

		// 获取串口名称
		String commName = commChoice.getSelectedItem();
		// 获取波特率
		String bpsStr = bpsChoice.getSelectedItem();

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
		    JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);
		} else {
		    // 检查波特率是否获取正确
		    if (bpsStr == null || bpsStr.equals("")) {
			JOptionPane.showMessageDialog(null, "波特率获取错误！", "错误", JOptionPane.INFORMATION_MESSAGE);
		    } else {
			// 串口名、波特率均获取正确时
			int bps = Integer.parseInt(bpsStr);
			try {
			    // 获取指定端口名及波特率的串口对象
			    serialPort = SerialTool.openPort(commName, bps);
			    // 在该串口对象上添加监听器
			    SerialTool.addListener(serialPort, new SerialListener());
			    // 监听成功进行提示
			    JOptionPane.showMessageDialog(null, "监听成功，稍后将显示监测数据！", "提示",
				    JOptionPane.INFORMATION_MESSAGE);
			    // 启动定时请求数据的线程.余姚精创仪表有限公司 KCM-91WRS温度变送器
			    new Thread() {
				public void run() {
				    while (true) {
					try {
					    // 定期请求数据
					    String[] address = { "01", "02", "03", "04", "05", "06", "07", "08", "09",
						    "0A", "0B", "0C", "0D", "0E", "0F", "10", "11", "12", "13", "14",
						    "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
						    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A" };
					    for (int i = 0; i < num; i++) {
						// 读实时温度命令
						String halfOrder = address[i] + "0310010001";
						String crc = CRC16.getCRC(Bytes2HexStr.toBytes(halfOrder));
						byte[] order = Bytes2HexStr.toBytes(halfOrder + crc);
						SerialTool.sendToPort(serialPort, order);
						// 等待一个数据处理后，再请求下一个数据
						sleep(1000);
					    }
					} catch (Exception e) {
					    e.printStackTrace();
					}
				    }
				}
			    }.start();
			} catch (Exception e1) {
			    e1.printStackTrace();
			}
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
	Color c = g.getColor();
	g.setColor(Color.black);
	g.setFont(new Font("微软雅黑", Font.BOLD, 25));
	for (int i = 0; i < num; i++) {
	    int x = 45 + (i % 3) * 380;
	    int y = 80 + 70 * Math.round(i / 3);
	    g.drawString("温度" + (i + 1) + ":", x, y);
	}
	g.setColor(Color.gray);
	g.setFont(new Font("微软雅黑", Font.BOLD, 20));
	g.drawString(" 串口选择： ", 45, 780);
	g.drawString(" 波特率： ", 425, 780);
    }

    /**
     * 双缓冲方式重画界面各元素组件
     */
    public void update(Graphics g) {
	if (offScreen == null)
	    offScreen = this.createImage(Client.WIDTH, Client.HEIGHT);
	Graphics gOffScreen = offScreen.getGraphics();
	Color c = gOffScreen.getColor();
	gOffScreen.setColor(Color.white);
	gOffScreen.fillRect(0, 0, Client.WIDTH, Client.HEIGHT); // 重画背景画布
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

		// 扫描可用串口
		commList = SerialTool.findPort();
		if (commList != null && commList.size() > 0) {

		    // 添加新扫描到的可用串口
		    for (String s : commList) {
			// 该串口名是否已存在，初始默认为不存在（在commList里存在但在commChoice里不存在，则新添加）
			boolean commExist = false;

			for (int i = 0; i < commChoice.getItemCount(); i++) {
			    if (s.equals(commChoice.getItem(i))) {
				// 当前扫描到的串口名已经在初始扫描时存在
				commExist = true;
				break;
			    }
			}
			if (commExist) {
			    // 当前扫描到的串口名已经在初始扫描时存在，直接进入下一次循环
			    continue;
			} else {
			    // 若不存在则添加新串口名至可用串口下拉列表
			    commChoice.add(s);
			}
		    }
		    // 移除已经不可用的串口
		    for (int i = 0; i < commChoice.getItemCount(); i++) {
			// 该串口是否已失效，初始默认为已经失效（在commChoice里存在但在commList里不存在，则已经失效）
			boolean commNotExist = true;

			for (String s : commList) {
			    if (s.equals(commChoice.getItem(i))) {
				commNotExist = false;
				break;
			    }
			}
			if (commNotExist) {
			    commChoice.remove(i);
			} else {
			    continue;
			}
		    }
		} else {
		    // 如果扫描到的commList为空，则移除所有已有串口
		    commChoice.removeAll();
		}
		try {
		    Thread.sleep(30);
		} catch (InterruptedException e) {
		    JOptionPane.showMessageDialog(null, e.getMessage(), "错误", JOptionPane.INFORMATION_MESSAGE);
		    System.exit(0);
		}
	    }
	}
    }

    /**
     * 以内部类形式创建一个串口监听类
     */
    private class SerialListener implements SerialPortEventListener {

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
		    if (serialPort == null) {
			JOptionPane.showMessageDialog(null, "串口对象为空！监听失败！", "错误", JOptionPane.INFORMATION_MESSAGE);
		    } else {
			data = SerialTool.readFromPort(serialPort); // 读取数据，存入字节数组

			// 余姚精创仪表有限公司 KCM-91WRS温度变送器
			if (data == null || data.length != 7) { // 检查数据是否读取正确
			    System.out.println("读取数据过程中未获取到有效数据");
			} else {
			    // CRC校验
			    String strData = Bytes2HexStr.bytesToHexFun1(data);
			    String crc1 = strData.substring(10);
			    byte[] partData = new byte[5];
			    System.arraycopy(data, 0, partData, 0, 5);
			    String crc2 = CRC16.getCRC(partData);
			    if (!crc1.equalsIgnoreCase(crc2)) {
				System.out.println("接收数据CRC检验错误");
			    } else {
				try {
				    // 解析数据,依据温度表的协议
				    int address = Bytes2HexStr.getInt1(data, 0);
				    // 数据长度1byte或2byte
				    String dataLength = strData.substring(4, 6);
				    float temp = (float) 0;
				    if (dataLength.equals("01")) {
					temp = (float) Bytes2HexStr.getInt1(data, 4) / (float) 10;
				    } else if (dataLength.equals("02")) {
					temp = (float) Bytes2HexStr.getInt2(data, 3) / (float) 10;
				    }
				    // 更新界面Label值(仪表的地址从1开始，label编号从0开始，此处要减1)
				    arTem[address - 1].setText(String.valueOf(temp));

				    // 保存最新数据
				    lastData[address - 1] = temp;
				    counter++;
				    if (counter > 300) {// 大约1秒钟读一次数据，300次大约是5分钟
					counter = 0;
					new StoringData(lastData, config).start();
				    }
				} catch (ArrayIndexOutOfBoundsException e) {
				    System.out.println("数据解析过程出错，更新界面数据失败！");
				}
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