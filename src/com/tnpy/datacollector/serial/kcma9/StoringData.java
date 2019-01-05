package com.tnpy.datacollector.serial.kcma9;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * 将数据存入数据库
 * @author 2018122006
 *
 */
public class StoringData extends Thread {

    float[] data;
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
		int no = Integer.parseInt(con.getProperty("instrument.startNo")) + i + 1;
		String sql = "insert into tb_equipmentparamrecord (equipmentid,value) values(" + no + "," + data[i]
			+ ")";
		System.out.println(sql);
//		    stmt.execute(sql);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
