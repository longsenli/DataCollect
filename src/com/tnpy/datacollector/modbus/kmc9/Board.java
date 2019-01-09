package com.tnpy.datacollector.modbus.kmc9;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tnpy.datacollector.SerialTool;

import gnu.io.SerialPort;

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
    // 保存最新温度数据
    Map<String, String> lastData = new ConcurrentHashMap<String, String>();

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
			// 保存串口对象到列表中
			serialPortList.add(serialPort);
			// 在串口对象上添加监听器
			SerialTool.addListener(serialPort, new Receiver(serialPort, commList.get(i), arTem, lastData));
			// 启动定时请求数据的线程.
			new Sender(serialPort, numList[i]).start();
		    } catch (Exception e1) {
			e1.printStackTrace();
		    }
		}
		// 启动定时存储数据线程
		new Storer(lastData).start();
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
}