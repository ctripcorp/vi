package com.ctrip.framework.vi.asm;

import com.ctrip.framework.vi.Permission;
import com.ctrip.framework.vi.asm.commons.Method;
import com.ctrip.framework.vi.asm.util.CheckClassAdapter;
import com.ctrip.framework.vi.asm.util.TraceClassVisitor;
import com.ctrip.framework.vi.code.ProfileClassVisitor;
import com.ctrip.framework.vi.code.ProfilerManager;
import com.ctrip.framework.vi.fc.FCManager;
import com.ctrip.framework.vi.threading.ThreadingManager;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by jiang.j on 2017/4/14.
 */
public class MethodAdapterTest {



    public InputStream getClassStream(Class<?> x){
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(x.getName().replace('.','/')+".class");
        return is;

    }

    @Test
    public void testInsertCodeToMethod() throws IOException {
        ClassReader cr;

        Path zipPath =Paths.get(System.getProperty("java.io.tmpdir"),"MethodAdapterTest.zip");
        System.out.println(zipPath);
        File zipFile = zipPath.toFile();
        if (zipFile.exists()) {
            Files.delete(zipPath);
        }
        Class<?>[] testClasses = new Class<?>[]{ProfilerManager.class,FCManager.class, Permission.class, ThreadingManager.class};

        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
        for(Class<?> seleClass : testClasses) {
            try (InputStream input = getClassStream(seleClass)) {

                cr = new ClassReader(input);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                ProfileClassVisitor mr = new ProfileClassVisitor(new CheckClassAdapter(cv), seleClass.getName());

                cr.accept(mr, ClassReader.EXPAND_FRAMES);

                String fileName = seleClass.getSimpleName() + ".class";
                    out.putNextEntry(new ZipEntry(fileName));
                    out.write(cw.toByteArray());
                    out.closeEntry();

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("some test");

    }
}
