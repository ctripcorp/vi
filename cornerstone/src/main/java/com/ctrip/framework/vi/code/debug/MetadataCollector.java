package com.ctrip.framework.vi.code.debug;

import com.ctrip.framework.vi.asm.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author keli.wang
 * Modified by jiang.j
 */

public class MetadataCollector  extends ClassVisitor{
    private final ClassMetadata classMetadata;
    private String className;

    public MetadataCollector(final ClassMetadata classMetadata) {
        super(Opcodes.ASM5);
        this.classMetadata = classMetadata;
    }


    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {

        this.className = name;
    }


    @Override
    public FieldVisitor visitField(final int access,
                                   final String name,
                                   final String desc,
                                   final String signature,
                                   final Object value) {
        classMetadata.addField(new ClassField(access, name, desc));
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(final int access,
                                     final String methodName,
                                     final String desc,
                                     final String signature,
                                     final String[] exceptions) {
        final MethodVisitor superMV = super.visitMethod(access,
                methodName,
                desc,
                signature,
                exceptions);

        final String methodUniqueName = methodName + desc;
        final String methodDesc = desc;
        classMetadata.addMethod(methodUniqueName);
        if(access != Opcodes.ACC_PRIVATE && desc.startsWith("()")){
            classMetadata.markPossibleProp(methodUniqueName);
        }
        return new MethodVisitor(Opcodes.ASM5, superMV) {
            private final Map<String, Integer> labelLineMapping = new HashMap<>();

            @Override
            public void visitLineNumber(final int line, final Label start) {
                labelLineMapping.put(start.toString(), line);
                classMetadata.addMethodLineNum(methodUniqueName,line);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name,
                                       String desc) {

                if(className.equals(owner) && methodDesc.equals("()"+desc)) {
                    //System.out.println(owner + "." + name);
                    //System.out.println(methodUniqueName);
                    classMetadata.addPropMethodField(methodUniqueName,name+"#"+desc);
                }
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {

                classMetadata.cleanPropMethodField(methodUniqueName);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name,
                                        String desc, boolean itf) {
                classMetadata.cleanPropMethodField(methodUniqueName);

            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
                                               Object... bsmArgs) {
                classMetadata.cleanPropMethodField(methodUniqueName);

            }

            @Override
            public void visitLdcInsn(Object cst) {
                classMetadata.cleanPropMethodField(methodUniqueName);
            }



            @Override
            public void visitIntInsn(int opcode, int operand) {
                classMetadata.cleanPropMethodField(methodUniqueName);

            }

            @Override
            public void visitLocalVariable(final String name,
                                           final String desc,
                                           final String signature,
                                           final Label start,
                                           final Label end,
                                           final int index) {
                if(!"this".equals(name)) {
                    classMetadata.cleanPropMethodField(methodUniqueName);
                }
                super.visitLocalVariable(name, desc, signature, start, end, index);
                classMetadata.addVariable(methodUniqueName,
                        new LocalVariable(
                                name, desc,
                                labelLine(start),
                                labelLine(end),
                                index));
            }

            private int labelLine(final Label label) {
                final String labelId = label.toString();
                if (labelLineMapping.containsKey(labelId)) {
                    return labelLineMapping.get(label.toString());
                }
                return Integer.MAX_VALUE;
            }
        };
    }

}
