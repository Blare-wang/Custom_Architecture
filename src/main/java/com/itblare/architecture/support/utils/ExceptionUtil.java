package com.itblare.architecture.support.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/19 14:51
 */
public class ExceptionUtil {

    public static String getMessage(Exception e) {
        String swStr;
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            swStr = sw.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return e.getMessage();
        }
        return swStr;
    }
}