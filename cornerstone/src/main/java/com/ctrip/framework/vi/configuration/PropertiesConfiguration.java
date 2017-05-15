package com.ctrip.framework.cornerstone.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jiang.j on 2016/11/18.
 */
public class PropertiesConfiguration implements Configuration {

    Logger logger = LoggerFactory.getLogger(getClass());
    ConcurrentMap<String,Object> properties = new ConcurrentHashMap<>();
    File propsFile = null;
    public PropertiesConfiguration(){

    }

    public PropertiesConfiguration(File file){
        if(file.isFile()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                propsFile = file;
                Properties pros = new Properties();
                pros.load(inputStream);
                loadProperties(pros);
            } catch (Throwable e) {
                logger.error("read configuraiont from file:"+file.getPath()+" failed",e);
            }
        }

    }


    public PropertiesConfiguration(URL url){

        try (InputStream inputStream = url.openStream()) {
            Properties pros = new Properties();
            pros.load(inputStream);
            loadProperties(pros);
        } catch (Throwable e) {
            logger.error("read configuraiont from url:"+url.getPath()+" failed",e);
        }

    }

    public void loadProperties(Properties properties){

        for(Map.Entry<Object,Object> entry:properties.entrySet()){
            properties.put(String.valueOf(entry.getKey()),entry.getValue());
        }
    }

    public void clearPropertyDirect(String key){
        properties.remove(key);
    }
    public void save(){
        if(this.propsFile != null){
           Properties props = new Properties();
            props.putAll(this.properties);
            try {
                props.store(new FileOutputStream(this.propsFile),"");
            } catch (IOException e) {
                logger.error("write configuration to file failed!",e);
            }

        }
    }

    @Override
    public Configuration subset(String prefix) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }


    @Override
    public void setProperty(String key, Object value) {
        if(value !=null) {
            properties.put(key, value);
        }else{
            properties.remove(key);
        }
    }

    @Override
    public void clearProperty(String key) {
        properties.remove(key);
    }

    @Override
    public void clear() {
        properties.clear();
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        Set<String> keys = new HashSet<>();
        for(String key:properties.keySet()){
            if(key.startsWith(prefix)) {
                keys.add(key);
            }
        }
        return keys.iterator();
    }

    @Override
    public Iterator<String> getKeys() {
        return properties.keySet().iterator();
    }

    @Override
    public Properties getProperties(String key) {
        return null;
    }

    @Override
    public boolean getBoolean(String key) {
        return  Boolean.parseBoolean(String.valueOf(properties.get(key)));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        if(containsKey(key)){
            return getBoolean(key);
        }else {
            return defaultValue;
        }
    }


    @Override
    public Boolean getBoolean(String key, Boolean defaultValue){
        return getBoolean(key,defaultValue.booleanValue());
    }
    @Override
    public byte getByte(String key) {
        return  containsKey(key)?Byte.parseByte(String.valueOf(properties.get(key))):0;
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        if(containsKey(key)){
            return getByte(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public Byte getByte(String key, Byte defaultValue) {
        return getByte(key,defaultValue.byteValue());
    }

    @Override
    public double getDouble(String key) {
        return  containsKey(key)?Double.parseDouble(String.valueOf(properties.get(key))):0;
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        if(containsKey(key)){
            return getDouble(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return getDouble(key,defaultValue.doubleValue());
    }

    @Override
    public float getFloat(String key) {
        return  containsKey(key)?Float.parseFloat(String.valueOf(properties.get(key))):0;
    }

    @Override
    public float getFloat(String key, float defaultValue) {

        if(containsKey(key)){
            return getFloat(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {

        if(containsKey(key)){
            return getFloat(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public int getInt(String key) {

        return  containsKey(key)?Integer.parseInt(String.valueOf(properties.get(key))):0;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        if(containsKey(key)){
            return getInt(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        if(containsKey(key)){
            return getInt(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key) {
        return  containsKey(key)?Long.parseLong(String.valueOf(properties.get(key))):0;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        if(containsKey(key)){
            return getLong(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        if(containsKey(key)){
            return getLong(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public short getShort(String key) {
        return  containsKey(key)?Short.parseShort(String.valueOf(properties.get(key))):0;
    }

    @Override
    public short getShort(String key, short defaultValue) {
        if(containsKey(key)){
            return getShort(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        if(containsKey(key)){
            return getShort(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return  containsKey(key)?new BigDecimal(String.valueOf(this.properties.get(key)).replace(",","")):null;
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {

        if(containsKey(key)){
            return getBigDecimal(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public BigInteger getBigInteger(String key) {

        String raw = String.valueOf(this.properties.get(key));
        return containsKey(key)?new BigInteger(raw.replace(",","")):null;
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue) {

        if(containsKey(key)){
            return getBigInteger(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public String getString(String key) {

        char[] startChar = new char[]{'$','{'};
        char endDelimiter = '}';
        int judegStartIndex = 0;
        final int maxLen = 50;
        String raw =  (String)properties.get(key);

        if(raw == null)
            return null;

        StringBuilder sb = new StringBuilder(raw.length());
        StringBuilder tmp = new StringBuilder();

        for(char c:raw.toCharArray()){

            int tmpLen = tmp.length();

            boolean isStartEnd = judegStartIndex>=startChar.length;

            if((!isStartEnd && c==startChar[judegStartIndex]) ||
                    (isStartEnd && tmpLen>0 && tmpLen<maxLen)){
                judegStartIndex++;
                if(isStartEnd && c==endDelimiter){
                    String tmpKey = tmp.substring(startChar.length);
                    String keyVal = System.getProperty(tmpKey);
                    if(keyVal != null){
                       sb.append(keyVal);
                    }else if (this.properties.containsKey(tmpKey)) {
                        sb.append(this.properties.get(tmpKey));
                    }else if(System.getenv(tmpKey)!=null){
                        sb.append(this.properties.get(tmpKey));
                    }

                    tmp.delete(0,tmp.length());
                    judegStartIndex = 0;
                }else {
                    tmp.append(c);
                }
            }else{
                sb.append(tmp.toString());
                tmp.delete(0,tmp.length());
                judegStartIndex =0;
                sb.append(c);
            }

        }
        return sb.toString();
    }

    @Override
    public String getString(String key, String defaultValue) {
        if(properties.containsKey(key)){
            return getString(key);
        }else {
            return defaultValue;
        }
    }

    @Override
    public String[] getStringArray(String key) {
        return new String[0];
    }

    @Override
    public List<Object> getList(String key) {
        return null;
    }

    @Override
    public List<Object> getList(String key, List<?> defaultValue) {
        return null;
    }
}
