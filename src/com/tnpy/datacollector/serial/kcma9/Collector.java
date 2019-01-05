package com.tnpy.datacollector.serial.kcma9;

import java.io.InputStream;
import java.util.Properties;

public class Collector {

    // 配置文件
    Properties config;

    public Collector() {
	try (InputStream is = ClassLoader.getSystemResourceAsStream("com/tnpy/datacollector/config.properties");) {
	    config = new Properties();
	    config.load(is);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	System.out.println("PID数据采集程序开始启动...");

    }
}
