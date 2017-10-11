package com.ctrip.framework.cs.code;

import com.ctrip.framework.cs.asm.MethodVisitor;
import com.ctrip.framework.cs.asm.Opcodes;
import com.ctrip.framework.cs.asm.ClassVisitor;

/**
 * Created by jiang.j on 2017/4/27.
 */
public class ProfileClassVisitor extends ClassVisitor {
    private String className;
    public ProfileClassVisitor(ClassVisitor cv,String className) {
        super(Opcodes.ASM5, cv);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        if (cv != null) {

            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if(!"<clinit>".equals(name)) {
                mv = new ProfileAdapter(api, mv, access, name, desc, this.className);
            }
            return mv;
        }
        return null;
    }
}
