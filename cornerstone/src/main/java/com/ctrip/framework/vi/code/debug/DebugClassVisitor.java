package com.ctrip.framework.vi.code.debug;

import com.ctrip.framework.vi.asm.ClassVisitor;
import com.ctrip.framework.vi.asm.MethodVisitor;
import com.ctrip.framework.vi.asm.Opcodes;

/**
 * Created by jiang.j on 2017/7/12.
 */
public class DebugClassVisitor extends ClassVisitor {
    private String className;
    private DebugInfo debugInfo;
    private final ClassMetadata classMetadata;
    public DebugClassVisitor(final ClassVisitor cv,final String className,final DebugInfo debugInfo,final ClassMetadata classMetadata) {
        super(Opcodes.ASM5, cv);
        this.className = className;
        this.debugInfo = debugInfo;
        this.classMetadata = classMetadata;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        if (cv != null) {

            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if(!"<clinit>".equals(name)) {
                mv = new DebugMethodVisitor(this.className,this.debugInfo,name,desc,access,mv,this.classMetadata);
            }
            return mv;
        }
        return null;
    }
}
