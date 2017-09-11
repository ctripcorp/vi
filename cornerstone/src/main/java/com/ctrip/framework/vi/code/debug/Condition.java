package com.ctrip.framework.vi.code.debug;

import com.ctrip.framework.vi.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jiang.j on 2017/7/28.
 */
public class Condition {
    private final int opcode;
    private final int cmpcode;
    private final String fieldName;
    private final String desc;
    private final String value;
    private final ClassField[] classFields;
    public static final int STRLEN = -2;
    public static final int STREQUAL = -3;

    public Condition(String fieldName,String desc,int opcode,int cmpcode,String value,ClassField[] classFields){
        this.opcode = opcode;
        this.cmpcode = cmpcode;
        this.value = value;
        this.fieldName = fieldName;
        this.desc = desc;
        this.classFields = classFields;
    }

    public Condition(String fieldName,String desc,int opcode,int cmpcode){
        this(fieldName,desc,opcode,cmpcode,null,null);
    }

    public Condition(String fieldName,String desc,int opcode,int cmpcode,String value){
        this(fieldName,desc,opcode,cmpcode,value,null);
    }


    public String getName(){
        return this.fieldName;
    }
    public int getOpcode(){

        return this.opcode;
    }

    public int getCmpcode(){

        return this.cmpcode;
    }

    public String getDesc(){
        return this.desc;
    }

    public String getValue(){

        return this.value;
    }

    public ClassField[] getClassFields(){
        return this.classFields;
    }



    private static boolean checkHaveNullJudge(Condition previous,Condition current){

        ClassField[] fields = current.getClassFields();
        if(current.getCmpcode() == Opcodes.IFNULL || current.getCmpcode()==Opcodes.IFNONNULL){
            return true;
        }
        if(previous == null){
            return false;
        }

        if(previous.getCmpcode()!=Opcodes.IFNULL || !previous.getName().equals(current.getName())
                ||previous.getOpcode() != current.getOpcode()) {
            return false;
        }else if(fields!=null){
            ClassField[] preFields = previous.getClassFields();
            if(preFields == null || preFields.length != fields.length){
                return false;
            }else {

                for(int i=0;i<fields.length;i++){
                    ClassField item = fields[i];
                    ClassField preItem = preFields[i];
                    if(!item.getName().equals(preItem.getName())||item.getAccess()!=preItem.getAccess()||
                            item.getDesc()!=preItem.getDesc()){
                        return false;
                    }
                }

                return true;
            }

        }else{
            return true;
        }
    }
    public static Condition[] checkAndCorrect(Condition[] conditions) throws IllegalConditionException {

        List<Condition> rtn = new ArrayList<>();
        Condition previousCondition=null;
        for(Condition c:conditions){
            String desc = c.getDesc();
            ClassField[] fields = c.getClassFields();
            if(fields != null && fields.length>0){
                desc = fields[fields.length-1].getDesc();
            }

            boolean isPrimitive = desc.length()==1;
            boolean isStr = desc.equals("Ljava/lang/String;");
            boolean isObj = !isPrimitive && !isStr;
            if((isPrimitive && !checkPrimitiveCondition(c))
                    ||(isStr && !checkStrCondition(c))
                    ||(isObj && !checkObjCondition(c))) {
                throw new IllegalConditionException();
            }

            int currentOpcode = c.getOpcode();
            if(currentOpcode==Condition.STRLEN || currentOpcode == Condition.STREQUAL
                    || (fields!=null && fields.length>0)){
                if((isStr && previousCondition== null) || !checkHaveNullJudge(previousCondition,c)){
                    rtn.add(new Condition(c.getName(),c.getDesc(),-1,Opcodes.IFNULL));
                    if(fields!= null){
                        for (int i = 0; i < fields.length; i++) {
                            if(i!=fields.length-1 || c.getCmpcode()!=Opcodes.IFNONNULL) {
                                rtn.add(new Condition(c.getName(), c.getDesc(), -1, Opcodes.IFNULL, "", Arrays.copyOf(fields, i + 1)));
                            }

                        }

                    }

                }

            }

            rtn.add(c);
            previousCondition = c;

        }

        return rtn.toArray(new Condition[rtn.size()]);
    }

    private static boolean checkObjCondition(Condition c) {
        int op = c.getOpcode();
        int cmp = c.getCmpcode();
        switch (op){
            case -1:
                break;
            default:
                return false;
        }

        switch (cmp){
            case Opcodes.IFNONNULL:
            case Opcodes.IFNULL:
                break;
            default:
                return false;
        }


        return true;
    }

    private static boolean checkStrCondition(Condition c) {
        int op = c.getOpcode();
        int cmp = c.getCmpcode();
        switch (op){
            case -1:
            case Condition.STREQUAL:
            case Condition.STRLEN:
                break;
            default:
                return false;
        }
        switch (cmp){
            case Opcodes.IFNONNULL:
            case Opcodes.IFNULL:
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
                break;
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPEQ:
                Pattern pattern = Pattern.compile("[0-9]+");
                return pattern.matcher(c.getValue()).matches();
            default:
                return false;
        }


        return true;
    }

    private static boolean checkPrimitiveCondition(Condition c){
        int op = c.getOpcode();
        int cmp = c.getCmpcode();
        String desc = c.getDesc();
        switch (desc){
            case "I":
            case "S":
            case "B":
            case "C":
                switch (op){
                    case -1:
                        break;
                    default:
                        return false;
                }
                switch (cmp){
                    case Opcodes.IF_ICMPLE:
                    case Opcodes.IF_ICMPGE:
                    case Opcodes.IF_ICMPLT:
                    case Opcodes.IF_ICMPGT:
                    case Opcodes.IF_ICMPNE:
                    case Opcodes.IF_ICMPEQ:
                        Pattern pattern = Pattern.compile("[0-9]+");
                        if(!pattern.matcher(c.getValue()).matches()){
                            return false;
                        }
                        break;
                    default:
                        return false;
                }

                break;
            case "F":
                switch (op){
                    case Opcodes.FCMPL:
                    case Opcodes.FCMPG:
                        Pattern pattern = Pattern.compile("[0-9]+(\\.[0-9]+)?");
                        if(!pattern.matcher(c.getValue()).matches()){
                            return false;
                        }
                        break;
                    default:
                        return false;
                }
                break;
            case "D":
                switch (op){
                    case Opcodes.DCMPL:
                    case Opcodes.DCMPG:
                        Pattern pattern = Pattern.compile("[0-9]+(\\.[0-9]+)?");
                        if(!pattern.matcher(c.getValue()).matches()){
                            return false;
                        }
                        break;
                    default:
                        return false;
                }
                break;
            case "J":
                switch (op){
                    case Opcodes.LCMP:
                        Pattern pattern = Pattern.compile("[0-9]+");
                        if(!pattern.matcher(c.getValue()).matches()){
                            return false;
                        }
                        break;
                    default:
                        return false;
                }
                break;
        }

        switch (desc){
            case "F":
            case "D":
            case "J":
                switch (cmp){
                    case Opcodes.IFLE:
                    case Opcodes.IFLT:
                    case Opcodes.IFGE:
                    case Opcodes.IFGT:
                    case Opcodes.IFNE:
                    case Opcodes.IFEQ:
                        break;
                    default:
                        return false;
                }
                break;
        }


        return true;
    }

}
