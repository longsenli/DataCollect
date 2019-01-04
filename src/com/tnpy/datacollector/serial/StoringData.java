package com.tnpy.datacollector.serial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class StoringData extends Thread {

    // 需要保存的数据
    float[] data;
    // 配置文件
    Properties con;

    public StoringData(float[] lastData, Properties config) {
	data = lastData;
	con = config;
    }

    @Override
    public void run() {
	try {
	    Class.forName(con.getProperty("mysql.driver-class-name"));
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
	try (Connection conn = DriverManager.getConnection(con.getProperty("mysql.jdbc-url"),
		con.getProperty("mysql.username"), con.getProperty("mysql.password"));
		Statement stmt = conn.createStatement();) {
	    for (int i = 0; i < data.length; i++) {
		    // 温度表总编号
		    int no = Integer.parseInt(con.getProperty("instrument.startNo")) + i + 1;
		    String sql = "insert into tb_equipmentparamrecord (equipmentid,value) values("+no+","+data[i]+")";
		    System.out.println(sql);
//		    stmt.execute(sql);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
