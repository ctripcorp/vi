package com.ctrip.framework.cs.jmx;

import javax.management.openmbean.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jiang.j on 2016/10/13.
 */
public class JMXTypeFactory {

    public static TabularType getTabularType(String name,String description,Class<?> type) throws OpenDataException {
        CompositeType compositeType = getCompositeType(type);
        Set<String> keys = compositeType.keySet();
        return new TabularType(name,description,compositeType,keys.toArray(new String[keys.size()]));
    }

    public static OpenType<?> getMapValueOpenType(Class<?> mapType) throws OpenDataException {

        if(Map.class.isAssignableFrom(mapType)){
            ParameterizedType parameterizedType = (ParameterizedType) mapType.getGenericSuperclass();
            Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
            return classMapToOpenType(valueType);
        }
        return null;
    }

    private static OpenType<?> classMapToOpenType(Class<?> fieldType) throws OpenDataException {

        String typeName = fieldType.getSimpleName().toLowerCase();
        OpenType<?> openType = SimpleType.STRING;
        if(fieldType.isPrimitive()||
                fieldType.getName().startsWith("java.math.") ||
                fieldType.getName().startsWith("java.lang.")){
            switch (typeName){
                case "int":
                case "integer":
                    openType = SimpleType.INTEGER;
                    break;
                case "bool":
                case "boolean":
                    openType = SimpleType.BOOLEAN;
                    break;
                case "byte":
                    openType = SimpleType.BYTE;
                    break;
                case "double":
                    openType = SimpleType.DOUBLE;
                    break;
                case "float":
                    openType = SimpleType.FLOAT;
                    break;
                case "short":
                    openType = SimpleType.SHORT;
                    break;
                case "bigdecimal":
                    openType = SimpleType.BIGDECIMAL;
                    break;
                case "biginteger":
                    openType = SimpleType.BIGINTEGER;
                    break;
                case "date":
                    openType = SimpleType.DATE;
                    break;
            }

        }else if(fieldType.isArray()){
            openType = new ArrayType<>(SimpleType.STRING,false);
        }
        return openType;
    }
    public static CompositeType getCompositeType(Class<?> type) throws OpenDataException {

        List<String> itemNames = new ArrayList<>();
        List<String> itemDescriptions = new ArrayList<>();
        List<OpenType<?>> itemTypes = new ArrayList<>();

        Field[] fields =type.getDeclaredFields();

        for(Field field :fields){
            itemNames.add(field.getName());
            Class<?> fieldType = field.getType();
            itemDescriptions.add(fieldType.getName());
            itemTypes.add(classMapToOpenType(fieldType));
        }
        return new CompositeType(type.getSimpleName(),type.getName(),itemNames.toArray(new String[itemNames.size()]),
                itemDescriptions.toArray(new String[itemDescriptions.size()]),itemTypes.toArray(new OpenType<?>[itemTypes.size()]));
    }
}
