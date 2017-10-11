package com.ctrip.framework.cs.code.debug;

import com.ctrip.framework.cs.asm.Label;
import com.ctrip.framework.cs.asm.MethodVisitor;
import com.ctrip.framework.cs.asm.Opcodes;
import com.ctrip.framework.cs.asm.Type;
import com.ctrip.framework.cs.asm.commons.AdviceAdapter;

import java.util.Collection;
import java.util.List;

/**
 * Created by jiang.j on 2017/7/12.
 */
public class DebugMethodVisitor extends AdviceAdapter {
    private final String className;
    private final String methodUniqueName;
    private final int access;
    private final DebugInfo debugInfo;
    private final ClassMetadata classMetadata;
    private final String MAP_PUT_DES ="(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private final String DEBUGTOOL_CLASS = "com/ctrip/framework/cs/code/debug/DebugTool";

    DebugMethodVisitor(final String className,
                       final DebugInfo debugInfo,
                       final String methodName,
                       final String desc,
                       final int access,
                       final MethodVisitor mv,
                       final ClassMetadata classMetadata) {
        //super(access,desc, mv);
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodUniqueName = methodName + desc;
        this.access = access;
        this.classMetadata = classMetadata;
        this.debugInfo = debugInfo;
    }


    private int getVariableIndex(final List<LocalVariable> variableList, final int line,String name,String desc){

        for(LocalVariable variable:variableList){
            if(variable.getName().equals(name) && variable.getDesc().equals(desc)){
                return variable.getIndex();
            }
        }
        return -1;
    }

    @Override
    public void visitLineNumber(final int line,final Label start){

        if(line == debugInfo.getLineNum()){
            final String traceId = debugInfo.getTraceId();
            final Label theEnd = new Label();

            Condition[] conditions = debugInfo.getConditions();
            if(conditions == null || conditions.length ==0) {
                super.visitLdcInsn(traceId);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, DEBUGTOOL_CLASS, "needMonitor", "(Ljava/lang/String;)Z", false);
                super.visitJumpInsn(Opcodes.IFEQ, theEnd);
            }

            boolean illegalCondition = false;
            if(conditions != null){

                List<LocalVariable> variableList = this.classMetadata.getVariablesByLineNum(this.methodUniqueName,line);


                for(Condition c:conditions){
                    String name = c.getName();
                    String desc = c.getDesc();
                    int opCode = c.getOpcode();
                    int compCode = c.getCmpcode();
                    String value = c.getValue();


                    int varIndex = getVariableIndex(variableList,line,name,desc);
                    if(varIndex<0) {
                        illegalCondition = true;
                        break;
                    }

                    super.visitVarInsn(Type.getType(desc).getOpcode(Opcodes.ILOAD), varIndex);

                    String realDesc = handleFiled(c.getClassFields(),desc);

                    if(opCode >= -1 && compCode != Opcodes.IFNULL && compCode != Opcodes.IFNONNULL) {
                        handlerPrimiteValue(realDesc,value);
                    }

                    if(opCode >= 0) {
                        super.visitInsn(opCode);
                    }else{
                        switch (opCode){
                            case Condition.STRLEN:
                                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"java/lang/String","length","()I",false);
                                super.visitLdcInsn(new Integer(value));
                                break;
                            case Condition.STREQUAL:
                                super.visitLdcInsn(value);
                                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"java/lang/String","equals","(Ljava/lang/Object;)Z",false);

                        }
                    }

                    super.visitJumpInsn(compCode,theEnd);

                }

            }

            if(!illegalCondition) {
                int varMap = newLocal(Type.getObjectType("java/util/HashMap"));
                super.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
                super.visitInsn(Opcodes.DUP);
                super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
                super.visitVarInsn(Opcodes.ASTORE, varMap);

                addAllAccessibleLocalVariable(line, varMap);
                addAllClassStaticField(varMap);
                addAllClassFieldIfShould(varMap);
                super.visitLdcInsn(traceId);
                super.visitVarInsn(Opcodes.ALOAD, varMap);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, DEBUGTOOL_CLASS, "log", "(Ljava/lang/String;Ljava/util/Map;)V", false);
            }
            super.visitLabel(theEnd);
        }

    }

    private String handleFiled(ClassField[] classFields,String ownerDesc){

        if(classFields != null) {
            String owner = getTypeFromDesc(ownerDesc);
            String rtnDesc = ownerDesc;
            for (ClassField cf : classFields) {
                String desc = cf.getDesc();
                int access = cf.getAccess();
                if(access != Opcodes.ACC_PRIVATE) {
                    super.visitFieldInsn(Opcodes.GETFIELD, owner, cf.getName(), desc);
                }else{
                    super.visitLdcInsn(owner.replace('/','.'));
                    super.visitLdcInsn(cf.getName());
                    switch (desc){
                        case "C":
                        case "B":
                        case "S":
                        case "I":
                        case "Z":
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,DEBUGTOOL_CLASS , "getPrivateFieldInt", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)I", false);
                            break;
                        case "D":
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,DEBUGTOOL_CLASS , "getPrivateFieldDouble", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)D", false);
                            break;
                        case "F":
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,DEBUGTOOL_CLASS , "getPrivateFieldFloat", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)F", false);
                            break;
                        case "J":
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,DEBUGTOOL_CLASS , "getPrivateFieldLong", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)J", false);
                            break;
                        case "Ljava/lang/String;":
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,DEBUGTOOL_CLASS , "getPrivateFieldString", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
                            break;
                        default:
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,DEBUGTOOL_CLASS , "getPrivateFieldValue", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", false);
                            break;
                    }
                }
                owner = getTypeFromDesc(desc);
                rtnDesc = desc;
            }
            return rtnDesc;
        }else {
            return ownerDesc;
        }

    }

    private String getTypeFromDesc(String desc){
        if(desc.endsWith(";")){
            return desc.substring(1,desc.length()-1);
        }
       return desc;
    }

    private void addAllAccessibleLocalVariable(final int line,final int varMap) {
        final Collection<LocalVariable> variables = classMetadata
                .getVariablesByMethodId(methodUniqueName);
        for (final LocalVariable var : variables) {
            if (line >= var.getStart() && line < var.getEnd()) {
                addLocalVariable(var,varMap);
            }
        }
    }

    private void addAllClassStaticField(int varMap) {
        for (final ClassField field : classMetadata.getStaticFields()) {
            addClassStaticField(field,varMap);
        }
    }

    private void addClassStaticField(final ClassField field,final int varMap) {
        final String name = field.getName();
        final String desc = field.getDesc();

        super.visitVarInsn(ALOAD,varMap);
        super.visitLdcInsn("static_field." + name);
        super.visitFieldInsn(GETSTATIC, className, name, desc);
        handlePrimiteType(desc);
        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", MAP_PUT_DES, true);
        super.visitInsn(POP);
    }


    private void addAllClassFieldIfShould(final int varMap) {
        if ((access & Opcodes.ACC_STATIC) == 0) {
            for (final ClassField field : classMetadata.getFields()) {
                addClassField(field,varMap);
            }
        }
    }

    private void addClassField(final ClassField field,final int varMap) {
        final String name = field.getName();
        final String desc = field.getDesc();

        super.visitVarInsn(ALOAD,varMap);
        super.visitLdcInsn("field." + name);
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, className, name, desc);
        handlePrimiteType(desc);
        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put",MAP_PUT_DES, true);
        super.visitInsn(POP);
    }

    private void handlerPrimiteValue(String desc,String value){

        switch (desc) {
            case "D":
                super.visitLdcInsn(new Double(value));
                break;
            case "F":
                super.visitLdcInsn(new Float(value));
                break;
            case "B":
            case "S":
            case "I":
            case "Z":
                super.visitLdcInsn(new Integer(value));
                break;
            case "J":
                super.visitLdcInsn(new Long(value));
                break;
            case "C":
                super.visitLdcInsn(new Integer(value.charAt(0)));
                break;

        }
    }

    private void handlePrimiteType(String desc){
        switch (desc){
            case "C":
            case "D":
            case "F":
            case "I":
            case "J":
            case "Z":
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "("+desc+")Ljava/lang/String;", false);
                break;
            case "S":
            case "B":
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
                break;
        }
    }

    private void addLocalVariable(final LocalVariable variable,final int varMap) {
        final String desc = variable.getDesc();
        final String key = "var." + variable.getName();

        super.visitVarInsn(ALOAD,varMap);
        super.visitLdcInsn(key);
        super.visitVarInsn(Type.getType(desc).getOpcode(Opcodes.ILOAD), variable.getIndex());
        handlePrimiteType(desc);
        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", MAP_PUT_DES , true);
        super.visitInsn(POP);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals){
        super.visitMaxs(Math.max(maxStack,3),maxLocals);
    }

}
