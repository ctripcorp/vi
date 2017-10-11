package com.ctrip.framework.cs.component.defaultComponents;

import com.ctrip.framework.cs.annotation.ComponentStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by jiang.j on 2016/6/29.
 */
@ComponentStatus(id="vi.processinfo",name = "Process info",description = "当前系统进程信息")
public class ProcessInfo {

    public ProcessInfo(){

        try {
            Process proc =Runtime.getRuntime().exec("powershell  get-wmiobject win32_process|select ProcessId,Name,CreationDate,VirtualSize,WorkingSetSize,Handles,UserModeTime,KernelModeTime");
            proc.getOutputStream().close();
            try(InputStream in = proc.getInputStream();BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line = reader.readLine();
                while (line!=null) {
                    line = reader.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
