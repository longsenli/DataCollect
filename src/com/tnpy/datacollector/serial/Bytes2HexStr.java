package com.tnpy.datacollector.serial;

public class Bytes2HexStr {

    private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
	    'f' };

    /*
     * byte数组转16进制字符串 .三个方法功能相同.
     */
    public static String bytesToHexFun1(byte[] bytes) {
	char[] buf = new char[bytes.length * 2];
	int a = 0;
	int index = 0;
	for (byte b : bytes) {
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

    public static String bytesToHexFun2(byte[] bytes) {
	char[] buf = new char[bytes.length * 2];
	int index = 0;
	for (byte b : bytes) {
	    buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
	    buf[index++] = HEX_CHAR[b & 0xf];
	}

	return new String(buf);
    }

    public static String bytesToHexFun3(byte[] bytes) {
	StringBuilder buf = new StringBuilder(bytes.length * 2);
	for (byte b : bytes) {
	    buf.append(String.format("%02x", new Integer(b & 0xff)));
	}

	return buf.toString();
    }

    /*
     * 16进制字符串转byte数组
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

    /*
     * byte数组中提取整数，分别从1、2、4个byte中取。
     */
    public static int getInt1(byte[] arr, int index) {
	return (0x00ff & arr[index]);
    }

    public static int getInt2(byte[] arr, int index) {
	return (0xff00 & (arr[index] << 8)) | (0x00ff & arr[index + 1]);
    }

    public static int getInt4(byte[] arr, int index) {
	return (0xff000000 & (arr[index + 0] << 24)) | (0x00ff0000 & (arr[index + 1] << 16))
		| (0x0000ff00 & (arr[index + 2] << 8)) | (0x000000ff & arr[index + 3]);
    }
}