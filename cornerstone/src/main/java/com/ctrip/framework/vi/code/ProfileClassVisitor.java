package com.ctrip.framework.vi.code;

import com.ctrip.framework.vi.asm.ClassVisitor;
import com.ctrip.framework.vi.asm.MethodVisitor;
import com.ctrip.framework.vi.asm.Opcodes;

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
