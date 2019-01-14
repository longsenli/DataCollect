package com.tnpy.dataaccess;

import java.io.InputStream;
import java.util.Properties;

public class Configer {

    private static Properties prop = null;

    private Configer() {
    }

    public static String getValue(String key) {
	if (prop == null) {
	    try (InputStream is = ClassLoader.getSystemResourceAsStream("com/tnpy/dataaccess/config.properties");) {
		prop = new Properties();
		prop.load(is);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return prop.getProperty(key);
    }
}
