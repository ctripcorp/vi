package com.ctrip.framework.vi.instrument;

import com.ctrip.framework.vi.NotFoundException;
import com.ctrip.framework.vi.asm.ClassReader;
import com.ctrip.framework.vi.asm.ClassWriter;
import com.ctrip.framework.vi.asm.util.CheckClassAdapter;
import com.ctrip.framework.vi.code.ProfileClassVisitor;
import com.ctrip.framework.vi.code.SourceCodeHelper;
import com.ctrip.framework.vi.code.debug.*;
import com.ctrip.framework.vi.metrics.MetricsCollector;
import com.ctrip.framework.vi.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jiang.j on 2017/4/27.
 */
public class AgentTool {
    static Class<?> vmProviderClass=null;
    static Logger logger = LoggerFactory.getLogger(AgentTool.class);
    private static Map<String,byte[]> needMetricsClasses = new ConcurrentHashMap<>();
    private static Map<String,byte[]> needDebugClasses = new ConcurrentHashMap<>();
    private static Map<String,byte[]> needRecoverClasses = new ConcurrentHashMap<>();
    private static Map<String,byte[]> modifiedClasses = new ConcurrentHashMap<>();
    private static Map<String,DebugInfo> debugClassInfos = new HashMap<>();


    private static Boolean isLoaded = false;
    private static final int MAXCLASSCOUNT = 10;

    public static Map<String,byte[]> getModifiedClasses(){
        return modifiedClasses;
    }

    public static boolean agentIsLoaded(){
        return isLoaded;
    }

    public static synchronized  void startUp(){
        if(!isLoaded){
            try {
                loadAgent();
                Instrumentation inst = instrumentation();
                inst.addTransformer(new CodeTransformer(), true);
                isLoaded = true;
            }catch (Throwable e){
                logger.warn("start agentTool failed",e);
            }
        }
    }
    private static void loadAgent() throws Exception{

        final String virtualMachineClassName = "com.sun.tools.attach.VirtualMachine";
        final String hotspotVMName = "sun.tools.attach.HotSpotVirtualMachine";
        Object VM;
        Method loadAgentMethod;
        Method detachMethod;
        Class<?> vmClass = Tools.loadJDKToolClass(virtualMachineClassName);
        Class<?> hotspotVMClass = Tools.loadJDKToolClass(hotspotVMName);
        String pid = Tools.currentPID();
        if (vmProviderClass != null) {
            Object vmProvider = vmProviderClass.newInstance();
            VM = vmProviderClass.getMethod("attachVirtualMachine", String.class).invoke(vmProvider, pid);
            loadAgentMethod = VM.getClass().getMethod("loadAgent", String.class);
            detachMethod = VM.getClass().getMethod("detach");
        } else {
            Method attacheMethod = vmClass.getMethod("attach", String.class);
            VM = attacheMethod.invoke(null, pid);
            vmProviderClass = vmClass.getMethod("provider").invoke(VM).getClass();
            loadAgentMethod = hotspotVMClass.getMethod("loadAgent", String.class);
            detachMethod = vmClass.getMethod("detach");
        }
        CodeSource src = AgentMain.class.getProtectionDomain().getCodeSource();

        String jarPath = Paths.get(src.getLocation().toURI()).toString();
        loadAgentMethod.invoke(VM, jarPath);
        detachMethod.invoke(VM);
    }

    public static Instrumentation instrumentation() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {

        ClassLoader mainAppLoader = ClassLoader.getSystemClassLoader();
        final Class<?> javaAgentClass = mainAppLoader.loadClass(AgentMain.class.getCanonicalName());
        final Method method = javaAgentClass.getDeclaredMethod("instrumentation",
                new Class[0]);
        return (Instrumentation) method.invoke(null, new Object[0]);
    }

    private static void validateTransform(Class seleClass) throws IllegalOperationException {

        String className = seleClass.getName();
        if((className.startsWith("com.ctrip.framework.vi.instrument.")) ||(className.startsWith("com.ctrip.framework.vi.metrics."))
                ||(className.startsWith("com.ctrip.framework.vi.code.")) ||(className.startsWith("com.ctrip.framework.vi.asm.")) ){
            throw new IllegalOperationException("Cannot modify this class,because it be used in code transform!");
        }else if(seleClass.getClassLoader() == null){
            throw new IllegalOperationException("Cannot modify system class!");
        }
    }

    private static void reloadClass(Class seleClass) throws Exception {
        if (!isLoaded) {
            startUp();
        }
        Instrumentation inst = instrumentation();

        inst.retransformClasses(seleClass);
    }

    public synchronized static void addMethodMetricsClass(String className) throws Exception {
        Class seleClass = Class.forName(className.replace('/', '.'));

        validateTransform(seleClass);

        if(needDebugClasses.containsKey(className)){
            throw new IllegalOperationException("Cannot monitor this class, because it already be monitored by debugger!");
        }

        if(needMetricsClasses.size()>= MAXCLASSCOUNT){
            throw new IllegalOperationException("Has reached the maximum limit "+MAXCLASSCOUNT+", you cannot add!");
        }

        if(!needMetricsClasses.containsKey(className)) {
            if (needMetricsClasses.put(className, new byte[]{}) == null) {
                reloadClass(seleClass);
            }
        }

    }

    public synchronized static void addDebugClass(String className,DebugInfo debugInfo) throws Exception {
        Class seleClass = Class.forName(className.replace('/', '.'));

        validateTransform(seleClass);

        if(needMetricsClasses.containsKey(className)){
            throw new IllegalOperationException("Cannot monitor this class, because it already be monitored by metrics!");
        }

        if(needDebugClasses.size()>= MAXCLASSCOUNT){
            throw new IllegalOperationException("Has reached the maximum limit "+MAXCLASSCOUNT+", you cannot add!");
        }

        if(needDebugClasses.containsKey(className)){
            throw new IllegalOperationException("Cannot debug this class, because it already be debugged by others!");
        }

        debugClassInfos.put(className, debugInfo);
        if(needDebugClasses.put(className,new byte[]{}) == null) {
            reloadClass(seleClass);
        }


    }


    public static List<String> loadedClasses() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<String> rtn = new ArrayList<>();
        Instrumentation inst = instrumentation();
        Class[] classes = inst.getAllLoadedClasses();

        for(Class c:classes){
            rtn.add(c.getName());
        }
        return rtn;
    }

    public static byte[] transformClass(byte[] classfileBuffer,String className) {


        if(needMetricsClasses.containsKey(className)) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ProfileClassVisitor mr = new ProfileClassVisitor(new CheckClassAdapter(cw), className);
            cr.accept(mr, ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
            needMetricsClasses.put(className, classfileBuffer);
            byte[] rtn = cw.toByteArray();
            modifiedClasses.put(className,rtn);
            return rtn;
        }else if(needDebugClasses.containsKey(className)){
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            //TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
            ClassMetadata metadata = new ClassMetadata();
            cr.accept(new MetadataCollector(metadata),ClassReader.SKIP_FRAMES);
            DebugClassVisitor mr = new DebugClassVisitor(new CheckClassAdapter(cw), className,debugClassInfos.get(className),metadata);
            cr.accept(mr, ClassReader.SKIP_FRAMES);
            needDebugClasses.put(className, classfileBuffer);
            byte[] rtn = cw.toByteArray();
            modifiedClasses.put(className, rtn);
            return rtn;

        }else if(needRecoverClasses.containsKey(className)){
            byte[] classBuffer = needRecoverClasses.remove(className);
            return classBuffer==null?classfileBuffer:classBuffer;

        }else{
            return classfileBuffer;
        }
    }

    public static ClassMetadata getClassMetadata(String className) throws IOException {

        String path = className.replace('.','/') + ".class";
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);

        ClassReader cr = null;
        if(url != null && "file".equals(url.getProtocol())){
            try(InputStream in = (InputStream) url.getContent()) {
                cr = new ClassReader(in);
            }
        }else {
            URL realUrl = new URL(SourceCodeHelper.getJarLocationByPath(url));
            try (ZipInputStream zip = new ZipInputStream(realUrl.openStream())) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {

                    if (entry.getName().equalsIgnoreCase(path)) {
                        cr = new ClassReader(zip);
                        break;
                    }
                }
            }
        }

        if(cr != null) {
            ClassMetadata metadata = new ClassMetadata();
            cr.accept(new MetadataCollector(metadata), ClassReader.SKIP_FRAMES);
            return metadata;
        }else {
            return null;
        }
    }

    public static List<LocalVariable> getLineAccessMetadata(String className, int lineNum) throws IOException {

        List<LocalVariable> rtn = null;

        ClassMetadata metadata = getClassMetadata(className);
        if(metadata!=null) {
            rtn = metadata.getVariablesByLineNum(null, lineNum);
        }

        return rtn;
    }

    public static List<ClassField> getClassFields(String className) throws IOException {
        List<ClassField> rtn = null;

        ClassMetadata metadata = getClassMetadata(className);
        if(metadata!=null) {
            rtn = metadata.getFields();
        }

        return rtn;

    }

    public static String[] getNeedMetricsClasses(){
        Set<String> strings = needMetricsClasses.keySet();
        return strings.toArray(new String[strings.size()]);
    }

    public static String[] getNeedDebugClasses(){
        Set<String> strings = needDebugClasses.keySet();
        return strings.toArray(new String[strings.size()]);
    }

    public static String getOriginClassASMCode(String className) throws IOException {

        String rtn = "";
        byte[] bytes = needMetricsClasses.get(className);
        if(bytes == null){
            bytes = needDebugClasses.get(className);
        }
        if(bytes != null){
           rtn = SourceCodeHelper.decompileClass(new ByteArrayInputStream(bytes));
        }
        return rtn;
    }

    public static String getModifiedClassASMCode(String className) throws IOException {
        String rtn = "";
        byte[] bytes = modifiedClasses.get(className);
        if(bytes!=null){
            rtn = SourceCodeHelper.decompileClass(new ByteArrayInputStream(bytes));
        }

        return rtn;
    }

    public static DebugInfo getDebugInfoByClassName(String className){
        return debugClassInfos.get(className);
    }


    public synchronized static void removeMetricsClass(String name) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, UnmodifiableClassException {
        Class seleClass = Class.forName(name.replace('/', '.'));
        needRecoverClasses.put(name, needMetricsClasses.remove(name));
        Instrumentation inst = instrumentation();
        inst.retransformClasses(seleClass);
        MetricsCollector.getCollector().removeClassMetrics(name);
    }

    public synchronized static Map<String, Object> removeDebugClassByTraceId(String traceId,boolean isForce) throws NotFoundException {
        String className = null;
        logger.debug("ready remove "+traceId);
        Map<String, Object> rtn = DebugTool.removeTraceInfo(traceId);

        for (Map.Entry<String, DebugInfo> entry : debugClassInfos.entrySet()) {
            if (traceId.equals(entry.getValue().getTraceId())) {
                className = entry.getKey();
                break;
            }
        }
        if (className == null) {
            throw  new NotFoundException("no debug class found by traceid "+traceId);
        }

        if(rtn != null || isForce) {
            try {

                Class seleClass = Class.forName(className.replace('/', '.'));
                debugClassInfos.remove(className);
                needRecoverClasses.put(className, needDebugClasses.remove(className));
                Instrumentation inst = instrumentation();
                inst.retransformClasses(seleClass);
            } catch (Throwable e) {
                logger.error("recover debug class failed!", e);
            }
        }
        return rtn;

    }

}
