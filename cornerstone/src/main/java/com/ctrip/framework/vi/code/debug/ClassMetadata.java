package com.ctrip.framework.vi.code.debug;

import com.ctrip.framework.vi.asm.Opcodes;

import java.util.*;

/**
 * @author keli.wang
 * Modified by jiang.j
 */
public class ClassMetadata {
    private final List<ClassField> fields = new ArrayList<>();
    private final List<ClassField> staticFields = new ArrayList<>();
    private final Map<String,List<LocalVariable>> variables = new HashMap<>();
    private final Map<String,MethodRange> methodRangeMap = new HashMap<>();
    private final Map<String,String> propMethodFields = new HashMap<>();

    public void addField(final ClassField field) {
        if (isStaticField(field)) {
            staticFields.add(field);
        } else {
            fields.add(field);
        }
    }

    public void addMethod(String methodId){
       if(!methodRangeMap.containsKey(methodId)) {
           methodRangeMap.put(methodId, new MethodRange());
       }
    }

    public void addMethodLineNum(String methodId,int lineNum){
        if(methodRangeMap.containsKey(methodId)) {
            methodRangeMap.get(methodId).update(lineNum);
        }

    }

    public void markPossibleProp(String methodId){
        propMethodFields.put(methodId,"");
    }

    public Map<String,String> getPropMethodFields(){
        return propMethodFields;
    }

    public void addPropMethodField(String methodId,String fieldId){

        if(propMethodFields.containsKey(methodId)){
            propMethodFields.put(methodId,fieldId);
        }
    }

    public void cleanPropMethodField(String methodId){
        propMethodFields.remove(methodId);
    }

    public void addVariable(final String methodId, final LocalVariable variable) {
        if(!variables.containsKey(methodId)){
            variables.put(methodId,new ArrayList<LocalVariable>());
        }
        variables.get(methodId).add(variable);
    }

    public List<ClassField> getFields() {
        return fields;
    }

    public List<ClassField> getStaticFields() {
        return staticFields;
    }

    public List<LocalVariable> getVariablesByMethodId(String methodId) {
        return variables.get(methodId);
    }

    public List<LocalVariable> getVariablesByLineNum(String methodName,int lineNum){
        List<LocalVariable> rtn = new ArrayList<>();

        if(methodName == null){
            Set<Map.Entry<String, MethodRange>> entries = methodRangeMap.entrySet();
           for(Map.Entry<String, MethodRange> entry:entries){
               if(entry.getValue().isInRange(lineNum)){
                   methodName = entry.getKey();
                   break;
               }
           }

        }

        if(methodName != null) {
            List<LocalVariable> localVariables = variables.get(methodName);
            for (LocalVariable variable : localVariables) {
                if (lineNum >= variable.getStart() && lineNum < variable.getEnd()) {
                    rtn.add(variable);
                }
            }
        }

        return rtn;
    }

    private boolean isStaticField(final ClassField field) {
        return (field.getAccess() & Opcodes.ACC_STATIC) != 0;
    }

}
