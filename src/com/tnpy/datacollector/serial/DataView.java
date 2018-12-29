package com.tnpy.datacollector.serial;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.JOptionPane;

import com.tnpy.datacollector.Bytes2HexStr;
import com.tnpy.datacollector.CRC16;
import com.tnpy.datacollector.ExceptionWriter;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * ���������ʾ��
 */
public class DataView extends Frame {

    private static final long serialVersionUID = 1L;

    Client client = null;

    private List<String> commList = null; // ������ö˿ں�
    private SerialPort serialPort = null; // ���洮�ڶ���

    private Font font = new Font("΢���ź�", Font.BOLD, 25);

    // 485�Ǳ������
    private int num = 30;
    private Label[] arTem = new Label[num];

    private Choice commChoice = new Choice(); // ����ѡ��������
    private Choice bpsChoice = new Choice(); // ������ѡ��

    private Button openSerialButton = new Button("�򿪴���");

    Image offScreen = null; // �ػ�ʱ�Ļ���

    /**
     * ��Ĺ��췽��
     * 
     * @param client
     */
    public DataView(Client client) {
	this.client = client;
	commList = SerialTool.findPort(); // �����ʼ��ʱ��ɨ��һ����Ч����
    }

    /**
     * ���˵�������ʾ�� ���Label����ť��������������¼�������
     */
    public void dataFrame() {
	this.setBounds(client.LOC_X, client.LOC_Y, client.WIDTH, client.HEIGHT);
	this.setTitle("�������ݲɼ�");
	this.setBackground(Color.white);
	this.setLayout(null);

	this.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent arg0) {
		if (serialPort != null) {
		    // �����˳�ʱ�رմ����ͷ���Դ
		    SerialTool.closePort(serialPort);
		}
		System.exit(0);
	    }
	});

	for (int i = 0; i < num; i++) {
	    int x = 140 + (i % 3) * 380;
	    int y = 60 + 70 * Math.round(i / 3);
	    arTem[i] = new Label("��������", Label.CENTER);
	    arTem[i].setBounds(x, y, 225, 40);
	    arTem[i].setBackground(Color.black);
	    arTem[i].setFont(font);
	    arTem[i].setForeground(Color.white);
	    add(arTem[i]);
	}

	// ��Ӵ���ѡ��ѡ��
	commChoice.setBounds(160, 770, 200, 200);
	// ����Ƿ��п��ô��ڣ��������ѡ����
	if (commList == null || commList.size() < 1) {
	    JOptionPane.showMessageDialog(null, "û����������Ч���ڣ�", "����", JOptionPane.INFORMATION_MESSAGE);
	} else {
	    for (String s : commList) {
		commChoice.add(s);
	    }
	}
	add(commChoice);

	// ��Ӳ�����ѡ��
	bpsChoice.setBounds(526, 770, 200, 200);
	bpsChoice.add("1200");
	bpsChoice.add("2400");
	bpsChoice.add("4800");
	bpsChoice.add("9600");
	bpsChoice.add("14400");
	bpsChoice.add("19200");
	bpsChoice.add("115200");
	add(bpsChoice);

	// ��Ӵ򿪴��ڰ�ť
	openSerialButton.setBounds(900, 760, 225, 40);
	openSerialButton.setBackground(Color.lightGray);
	openSerialButton.setFont(new Font("΢���ź�", Font.BOLD, 20));
	openSerialButton.setForeground(Color.darkGray);
	add(openSerialButton);
	// ��Ӵ򿪴��ڰ�ť���¼�����
	openSerialButton.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {

		// ��ȡ��������
		String commName = commChoice.getSelectedItem();
		// ��ȡ������
		String bpsStr = bpsChoice.getSelectedItem();

		// ��鴮�������Ƿ��ȡ��ȷ
		if (commName == null || commName.equals("")) {
		    JOptionPane.showMessageDialog(null, "û����������Ч���ڣ�", "����", JOptionPane.INFORMATION_MESSAGE);
		} else {
		    // ��鲨�����Ƿ��ȡ��ȷ
		    if (bpsStr == null || bpsStr.equals("")) {
			JOptionPane.showMessageDialog(null, "�����ʻ�ȡ����", "����", JOptionPane.INFORMATION_MESSAGE);
		    } else {
			// �������������ʾ���ȡ��ȷʱ
			int bps = Integer.parseInt(bpsStr);
			try {

			    // ��ȡָ���˿����������ʵĴ��ڶ���
			    serialPort = SerialTool.openPort(commName, bps);
			    // �ڸô��ڶ�������Ӽ�����
			    SerialTool.addListener(serialPort, new SerialListener());
			    // �����ɹ�������ʾ
			    JOptionPane.showMessageDialog(null, "�����ɹ����Ժ���ʾ������ݣ�", "��ʾ",
				    JOptionPane.INFORMATION_MESSAGE);

			    // ������ʱ�������ݵ��߳�.��Ҧ�����Ǳ����޹�˾ KCM-91WRS�¶ȱ�����
			    new Thread() {
				public void run() {
				    while (true) {
					try {
					    // ������������
					    String[] address = { "01", "02", "03", "04", "05", "06", "07", "08", "09",
						    "0A", "0B", "0C", "0D", "0E", "0F", "10", "11", "12", "13", "14",
						    "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E" };
					    for (String one : address) {
						String halfOrder = one + "0310010001";
						String crc = CRC16.getCRC(Bytes2HexStr.toBytes(halfOrder));
						byte[] order = Bytes2HexStr.toBytes(halfOrder + crc);
						SerialTool.sendToPort(serialPort, order);
						sleep(1000);
					    }
					} catch (Exception e) {
					    e.printStackTrace();
					}
				    }
				}
			    }.start();
			} catch (Exception e1) {
			    // ��������ʱʹ��һ��Dialog��ʾ����Ĵ�����Ϣ
			    JOptionPane.showMessageDialog(null, e1, "����", JOptionPane.INFORMATION_MESSAGE);
			}
		    }
		}
	    }
	});

	this.setResizable(false);

	new Thread(new RepaintThread()).start(); // �����ػ��߳�
    }

    /**
     * �������������Ԫ��
     */
    public void paint(Graphics g) {
	Color c = g.getColor();
	g.setColor(Color.black);
	g.setFont(new Font("΢���ź�", Font.BOLD, 25));

	for (int i = 0; i < num; i++) {
	    int x = 45 + (i % 3) * 380;
	    int y = 80 + 70 * Math.round(i / 3);
	    g.drawString("�¶�" + (i + 1) + ":", x, y);
	}

	g.setColor(Color.gray);
	g.setFont(new Font("΢���ź�", Font.BOLD, 20));
	g.drawString(" ����ѡ�� ", 45, 780);

	g.setColor(Color.gray);
	g.setFont(new Font("΢���ź�", Font.BOLD, 20));
	g.drawString(" �����ʣ� ", 425, 780);

    }

    /**
     * ˫���巽ʽ�ػ������Ԫ�����
     */
    public void update(Graphics g) {
	if (offScreen == null)
	    offScreen = this.createImage(Client.WIDTH, Client.HEIGHT);
	Graphics gOffScreen = offScreen.getGraphics();
	Color c = gOffScreen.getColor();
	gOffScreen.setColor(Color.white);
	gOffScreen.fillRect(0, 0, Client.WIDTH, Client.HEIGHT); // �ػ���������
	this.paint(gOffScreen); // �ػ�����Ԫ��
	gOffScreen.setColor(c);
	g.drawImage(offScreen, 0, 0, null); // ���»��õĻ�����������ԭ������
    }

    /*
     * �ػ��̣߳�ÿ��30�����ػ�һ�Σ�
     */
    private class RepaintThread implements Runnable {
	public void run() {
	    while (true) {
		// �����ػ�����
		repaint();

		// ɨ����ô���
		commList = SerialTool.findPort();
		if (commList != null && commList.size() > 0) {

		    // �����ɨ�赽�Ŀ��ô���
		    for (String s : commList) {

			// �ô������Ƿ��Ѵ��ڣ���ʼĬ��Ϊ�����ڣ���commList����ڵ���commChoice�ﲻ���ڣ�������ӣ�
			boolean commExist = false;

			for (int i = 0; i < commChoice.getItemCount(); i++) {
			    if (s.equals(commChoice.getItem(i))) {
				// ��ǰɨ�赽�Ĵ������Ѿ��ڳ�ʼɨ��ʱ����
				commExist = true;
				break;
			    }
			}

			if (commExist) {
			    // ��ǰɨ�赽�Ĵ������Ѿ��ڳ�ʼɨ��ʱ���ڣ�ֱ�ӽ�����һ��ѭ��
			    continue;
			} else {
			    // ��������������´����������ô��������б�
			    commChoice.add(s);
			}
		    }

		    // �Ƴ��Ѿ������õĴ���
		    for (int i = 0; i < commChoice.getItemCount(); i++) {

			// �ô����Ƿ���ʧЧ����ʼĬ��Ϊ�Ѿ�ʧЧ����commChoice����ڵ���commList�ﲻ���ڣ����Ѿ�ʧЧ��
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
		    // ���ɨ�赽��commListΪ�գ����Ƴ��������д���
		    commChoice.removeAll();
		}

		try {
		    Thread.sleep(30);
		} catch (InterruptedException e) {
		    String err = ExceptionWriter.getErrorInfoFromException(e);
		    JOptionPane.showMessageDialog(null, err, "����", JOptionPane.INFORMATION_MESSAGE);
		    System.exit(0);
		}
	    }
	}
    }

    /**
     * ���ڲ�����ʽ����һ�����ڼ�����
     */
    private class SerialListener implements SerialPortEventListener {

	/**
	 * �����ص��Ĵ����¼�
	 */
	public void serialEvent(SerialPortEvent serialPortEvent) {

	    switch (serialPortEvent.getEventType()) {

	    case SerialPortEvent.BI: // 10 ͨѶ�ж�
		JOptionPane.showMessageDialog(null, "�봮���豸ͨѶ�ж�", "����", JOptionPane.INFORMATION_MESSAGE);
		break;

	    case SerialPortEvent.OE: // 7 ��λ�����������

	    case SerialPortEvent.FE: // 9 ֡����

	    case SerialPortEvent.PE: // 8 ��żУ�����

	    case SerialPortEvent.CD: // 6 �ز����

	    case SerialPortEvent.CTS: // 3 �������������

	    case SerialPortEvent.DSR: // 4 ����������׼������

	    case SerialPortEvent.RI: // 5 ����ָʾ

	    case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 ��������������
		break;

	    case SerialPortEvent.DATA_AVAILABLE: // 1 ���ڴ��ڿ�������

		byte[] data = null;

		try {
		    if (serialPort == null) {
			JOptionPane.showMessageDialog(null, "���ڶ���Ϊ�գ�����ʧ�ܣ�", "����", JOptionPane.INFORMATION_MESSAGE);
		    } else {
			data = SerialTool.readFromPort(serialPort); // ��ȡ���ݣ������ֽ�����

			// �Զ����������
			if (data == null || data.length < 1) { // ��������Ƿ��ȡ��ȷ
			    JOptionPane.showMessageDialog(null, "��ȡ���ݹ�����δ��ȡ����Ч���ݣ�", "����",
				    JOptionPane.INFORMATION_MESSAGE);
			    System.exit(0);
			} else {
			    try {
				// ��������,�����¶ȱ��Э��
				int address = Bytes2HexStr.getInt2(data, 0);
				float temp = (float) Bytes2HexStr.getInt4(data, 6)/(float)10;
				// ���½���Labelֵ
				arTem[address].setText(temp + " ��");
			    } catch (ArrayIndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(null, "���ݽ������̳������½�������ʧ�ܣ�", "����",
					JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			    }
			}
		    }
		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null, e, "����", JOptionPane.INFORMATION_MESSAGE);
		    System.exit(0); // ������ȡ����ʱ��ʾ������Ϣ���˳�ϵͳ
		}
		break;
	    }
	}
    }
}