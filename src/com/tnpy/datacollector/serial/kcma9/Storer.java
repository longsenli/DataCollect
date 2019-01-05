package com.tnpy.datacollector.serial.kcma9;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * 将数据存入数据库
 * 
 * @author 2018122006
 *
 */
public class Storer extends Thread {

    float[] data;
    Properties con;

    public Storer(float[] lastData, Properties config) {
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
	    // 当前时间
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String currentTime = formatter.format(new Date());
	    for (int i = 0; i < data.length; i++) {
		int no = Integer.parseInt(con.getProperty("instrument.startNo")) + i + 1;
		String id = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		String sql = "insert into tnmesdb.tb_equipmentparamrecord (id,equipmentid,paramID,recordTime,value,equipmentTypeID) values('"
			+ id + "'," + no + ",1,'" + currentTime + "'," + data[i] + ",3)";
		stmt.execute(sql);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
