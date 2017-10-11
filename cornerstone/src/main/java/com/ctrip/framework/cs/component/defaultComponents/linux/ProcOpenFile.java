package com.ctrip.framework.cs.component.defaultComponents.linux;

import com.ctrip.framework.cs.annotation.ComponentStatus;
import com.ctrip.framework.cs.annotation.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiang.j on 2017/1/5.
 */

@ComponentStatus(id="vi.linux.openfiles",name="open files info",description = "应用打开文件列表",list = true,auto = false)
public class ProcOpenFile {

    @FieldInfo(name = "file name",description = "文件名")
    public String name;
    @FieldInfo(name = "file size",description = "文件大小",type = FieldInfo.FieldType.Bytes)
    public long size;
    @FieldInfo(name = "file path",description = "文件路径")
    public String path;


    static transient Logger logger = LoggerFactory.getLogger(ProcOpenFile.class);

    public static List<ProcOpenFile> list() throws FileNotFoundException {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        String pid = (jvmName.substring(0, index));

        List<ProcOpenFile> rtn = new ArrayList<>();

        File folder = new File("/proc/" + pid + "/fd");
        if(folder.isDirectory()){
            File[] files = folder.listFiles();
            if(files == null){
                return rtn;
            }
            for(File fd:files){
                Path path = fd.toPath();
                if(Files.isSymbolicLink(path)){
                    try {
                        File real = Files.readSymbolicLink(path).toFile();
                        if(real.isFile()) {
                            ProcOpenFile of = new ProcOpenFile();
                            of.name = real.getName();
                            of.size = real.length();
                            of.path = real.getAbsolutePath();
                            rtn.add(of);
                        }
                    } catch (Throwable e) {
                        logger.warn("Read link:"+path.toString()+" failed!");
                    }
                }
            }
        }
        return rtn;
    }
}
