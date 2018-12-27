package com.tnpy.datacollector;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 负责将传入的Exception中的错误信息提取出来并转换成字符串；
 */
public class ExceptionWriter {

    public static String getErrorInfoFromException(Exception e) { 
            
            StringWriter sw = null;
            PrintWriter pw = null;
            
            try {  
                sw = new StringWriter();  
                pw = new PrintWriter(sw);  
                e.printStackTrace(pw);  
                return "\r\n" + sw.toString() + "\r\n";  
                
            } catch (Exception e2) {  
                return "出错啦！未获取到错误信息，请检查后重试!";  
            } finally {
                try {
                    if (pw != null) {
                        pw.close();
                    }
                    if (sw != null) {
                        sw.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
}
