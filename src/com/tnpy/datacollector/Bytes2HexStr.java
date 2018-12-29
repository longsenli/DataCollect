package com.tnpy.datacollector;

import java.util.Arrays;

/**
 * byte[]��16�����ַ����໥ת��
 */
public class Bytes2HexStr {

    private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
	    'f' };

    /**
     * ����һ�� byte[] to hex string
     * 
     * @param bytes
     * @return
     */
    public static String bytesToHexFun1(byte[] bytes) {
	// һ��byteΪ8λ����������ʮ������λ��ʶ
	char[] buf = new char[bytes.length * 2];
	int a = 0;
	int index = 0;
	for (byte b : bytes) { // ʹ�ó���ȡ�����ת��
	    if (b < 0) {
		a = 256 + b;
	    } else {
		a = b;
	    }

	    buf[index++] = HEX_CHAR[a / 16];
	    buf[index++] = HEX_CHAR[a % 16];
	}

	return new String(buf);
    }

    /**
     * �������� byte[] to hex string
     * 
     * @param bytes
     * @return
     */
    public static String bytesToHexFun2(byte[] bytes) {
	char[] buf = new char[bytes.length * 2];
	int index = 0;
	for (byte b : bytes) { // ����λ�������ת�������Կ�������һ�ı���
	    buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
	    buf[index++] = HEX_CHAR[b & 0xf];
	}

	return new String(buf);
    }

    /**
     * �������� byte[] to hex string
     * 
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes) {
	StringBuilder buf = new StringBuilder(bytes.length * 2);
	for (byte b : bytes) { // ʹ��String��format��������ת��
	    buf.append(String.format("%02x", new Integer(b & 0xff)));
	}

	return buf.toString();
    }

    /**
     * ��16�����ַ���ת��Ϊbyte[]
     * 
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
	if (str == null || str.trim().equals("")) {
	    return new byte[0];
	}

	byte[] bytes = new byte[str.length() / 2];
	for (int i = 0; i < str.length() / 2; i++) {
	    String subStr = str.substring(i * 2, i * 2 + 2);
	    bytes[i] = (byte) Integer.parseInt(subStr, 16);
	}

	return bytes;
    }

    public static void main(String[] args) throws Exception {
	byte[] bytes = "����".getBytes("utf-8");
	System.out.println("�ֽ�����Ϊ��" + Arrays.toString(bytes));
	System.out.println("����һ��" + bytesToHexFun1(bytes));
	System.out.println("��������" + bytesToHexFun2(bytes));
	System.out.println("��������" + bytesToHexFun3(bytes));

	System.out.println("==================================");

	String str = "e6b58be8af95";
	System.out.println("ת������ֽ����飺" + Arrays.toString(toBytes(str)));
	System.out.println(new String(toBytes(str), "utf-8"));
    }
}