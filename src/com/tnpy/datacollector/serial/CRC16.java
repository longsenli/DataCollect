package com.tnpy.datacollector.serial;

public class CRC16 {

    public static String getCRC(byte[] bytes) {
	int CRC = 0x0000ffff;
	int POLYNOMIAL = 0x0000a001;

	int i, j;
	for (i = 0; i < bytes.length; i++) {
	    CRC ^= ((int) bytes[i] & 0x000000ff);
	    for (j = 0; j < 8; j++) {
		if ((CRC & 0x00000001) != 0) {
		    CRC >>= 1;
		    CRC ^= POLYNOMIAL;
		} else {
		    CRC >>= 1;
		}
	    }
	}
	// 高低位互换
	CRC = ((CRC & 0x0000FF00) >> 8) | ((CRC & 0x000000FF) << 8);
	return Integer.toHexString(CRC);
    }
}
