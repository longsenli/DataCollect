package com.tnpy.datacollector.modbus.kcm9;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.tnpy.datacollector.Configer;

/**
 * 将数据存入数据库
 * 
 * @author 2018122006
 *
 */
public class Storer extends Thread {

    // 保存最新温度数据
    Map<String, String> lastData;

    public Storer(Map<String, String> lastData) {
	this.lastData = lastData;
    }

    @Override
    public void run() {
	while (true) {
	    try {
		Class.forName(Configer.getValue("mysql.driver-class-name"));
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    }
	    try (Connection conn = DriverManager.getConnection(Configer.getValue("mysql.jdbc-url"),
		    Configer.getValue("mysql.username"), Configer.getValue("mysql.password"));
		    Statement stmt = conn.createStatement();) {
		// 当前时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = formatter.format(new Date());
		for (Entry<String, String> entry : lastData.entrySet()) {
		    String id = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		    String sql = "insert into tnmesdb.tb_equipmentparamrecord (id,equipmentid,paramID,recordTime,value,equipmentTypeID) values('"
			    + id + "'," + entry.getKey() + ",1,'" + currentTime + "'," + entry.getValue() + ",3)";
		    stmt.execute(sql);
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    // 休息10分钟
	    try {
		sleep(600000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }
}
