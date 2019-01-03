package com.tnpy.datacollector.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * ���ڷ����࣬�ṩ�򿪡��رմ��ڣ���ȡ�����ʹ������ݵȷ��񣨲��õ������ģʽ��
 */
public class SerialTool {

    private static SerialTool serialTool = null;

    static {
	// �ڸ��౻ClassLoader����ʱ�ͳ�ʼ��һ��SerialTool����
	if (serialTool == null) {
	    serialTool = new SerialTool();
	}
    }

    // ˽�л�SerialTool��Ĺ��췽��������������������SerialTool����
    private SerialTool() {
    }

    /**
     * ��ȡ�ṩ�����SerialTool����
     * 
     * @return serialTool
     */
    public static SerialTool getSerialTool() {
	if (serialTool == null) {
	    serialTool = new SerialTool();
	}
	return serialTool;
    }

    /**
     * �������п��ö˿�
     * 
     * @return ���ö˿������б�
     */
    public static final ArrayList<String> findPort() {

	// ��õ�ǰ���п��ô���
	Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();

	ArrayList<String> portNameList = new ArrayList<>();

	// �����ô�������ӵ�List�����ظ�List
	while (portList.hasMoreElements()) {
	    String portName = portList.nextElement().getName();
	    portNameList.add(portName);
	}
	return portNameList;
    }

    /**
     * �򿪴���
     * 
     * @param portName �˿�����
     * @param baudrate ������
     * @return ���ڶ���
     * @throws SerialPortException ���ô��ڲ���ʧ��
     * @throws NotASerialPort      �˿�ָ���豸���Ǵ�������
     * @throws NoSuchPort          û�иö˿ڶ�Ӧ�Ĵ����豸
     * @throws PortInUse           �˿��ѱ�ռ��
     */
    public static final SerialPort openPort(String portName, int baudrate) throws SerialPortException {

	try {

	    // ͨ���˿���ʶ��˿�
	    CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

	    // �򿪶˿ڣ������˿����ֺ�һ��timeout���򿪲����ĳ�ʱʱ�䣩
	    CommPort commPort = portIdentifier.open(portName, 2000);

	    // �ж��ǲ��Ǵ���
	    if (commPort instanceof SerialPort) {

		SerialPort serialPort = (SerialPort) commPort;

		try {
		    // ����һ�´��ڵĲ����ʵȲ���
		    serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
			    SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
		    throw new SerialPortException("���ô��ڲ���ʧ�ܣ��򿪴��ڲ���δ��ɣ�");
		}

		return serialPort;
	    } else {
		throw new SerialPortException("���Ǵ��ڣ�");
	    }
	} catch (NoSuchPortException e1) {
	    throw new SerialPortException(e1.getMessage());
	} catch (PortInUseException e2) {
	    throw new SerialPortException(e2.getMessage());
	}
    }

    /**
     * �رմ���
     * 
     * @param serialport ���رյĴ��ڶ���
     */
    public static void closePort(SerialPort serialPort) {
	if (serialPort != null) {
	    serialPort.close();
	    serialPort = null;
	}
    }

    /**
     * �����ڷ�������
     * 
     * @param serialPort ���ڶ���
     * @param order      ����������
     */
    public static void sendToPort(SerialPort serialPort, byte[] order) throws SerialPortException {

	OutputStream out = null;
	try {
	    out = serialPort.getOutputStream();
	    out.write(order);
	    out.flush();
	} catch (IOException e) {
	    throw new SerialPortException("�򴮿ڷ�������ʧ��");
	} finally {
	    try {
		if (out != null) {
		    out.close();
		    out = null;
		}
	    } catch (IOException e) {
		throw new SerialPortException("�رմ��ڶ�������������");
	    }
	}
    }

    /**
     * �Ӵ��ڶ�ȡ����
     * 
     * @param serialPort ��ǰ�ѽ������ӵ�SerialPort����
     * @return ��ȡ��������
     */
    public static byte[] readFromPort(SerialPort serialPort) throws SerialPortException {

	InputStream in = null;
	byte[] bytes = null;

	try {
	    in = serialPort.getInputStream();
	    int bufflenth = in.available(); // ��ȡbuffer������ݳ���
	    while (bufflenth != 0) {
		bytes = new byte[bufflenth]; // ��ʼ��byte����Ϊbuffer�����ݵĳ���
		in.read(bytes);
		bufflenth = in.available();
	    }
	} catch (IOException e) {
	    throw new SerialPortException("�Ӵ��ڶ�ȡ����ʱ����");
	} finally {
	    try {
		if (in != null) {
		    in.close();
		    in = null;
		}
	    } catch (IOException e) {
		throw new SerialPortException("�رմ��ڶ�������������");
	    }
	}
	return bytes;
    }

    /**
     * ��Ӽ�����
     * 
     * @param port     ���ڶ���
     * @param listener ���ڼ�����
     * @throws TooManyListeners ������������
     */
    public static void addListener(SerialPort port, SerialPortEventListener listener) throws SerialPortException {

	try {
	    // ��������Ӽ�����
	    port.addEventListener(listener);
	    // ���õ������ݵ���ʱ���Ѽ��������߳�
	    port.notifyOnDataAvailable(true);
	    // ���õ�ͨ���ж�ʱ�����ж��߳�
	    port.notifyOnBreakInterrupt(true);
	} catch (TooManyListenersException e) {
	    throw new SerialPortException(e.getMessage());
	}
    }
}