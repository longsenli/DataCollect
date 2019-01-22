package com.tnpy.dataaccess.kcm9;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.tnpy.dataaccess.Configer;

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
			ResultSet rs = null;
			try (Connection conn = DriverManager.getConnection(Configer.getValue("mysql.jdbc-url"),
					Configer.getValue("mysql.username"), Configer.getValue("mysql.password"));
					Statement stmt = conn.createStatement();) {
				// 当前时间
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentTime = formatter.format(new Date());
				for (Entry<String, String> entry : lastData.entrySet()) {
					String id = UUID.randomUUID().toString().replace("-", "").toLowerCase();
					String equipmentID = entry.getKey();
					String value = entry.getValue();
					String paramID = null;
					String equipmentTypeID = null;
					String status = "2";
					// 38开头的ID是充电水槽温度 39的是树脂干燥窑温度
					if (equipmentID.substring(0, 2).equals("38")) {
						paramID = "50001";
						equipmentTypeID = "3";
					} else {
						paramID = "40001";
						equipmentTypeID = "4";
					}
					// 判断值是否在设置范围内
					rs = stmt.executeQuery("select max,min FROM ilpsdb.tb_parameterinfo where id='" + paramID + "'");
					if (rs.next()) {
						double max = rs.getDouble(1);
						double min = rs.getDouble(2);
						if (Double.parseDouble(value) > max) {
							status = "3";
						} else if (Double.parseDouble(value) < min) {
							status = "1";
						}
					}

					// insert into tb_equipmentparamrecord
					// (id,equipmentID,paramID,recordTime,value,recorder,equipmentTypeID,status)
					// values
					// (id为36位uuid()，设备ID,参数ID固定为40001，记录时间，温度值，记录者固定位自动采集，设备类型固定为4，状态低于最小值为1，正常为2，高于最大值为3)
					String sql = "insert into ilpsdb.tb_equipmentparamrecord (id,equipmentid,paramID,recordTime,value,recorder,equipmentTypeID,status) values('"
							+ id + "','" + equipmentID + "','" + paramID + "','" + currentTime + "','" + value
							+ "','仪表采集','" + equipmentTypeID + "','" + status + "')";
//					System.out.println("采集入库==========" + sql);
					stmt.execute(sql);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
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
