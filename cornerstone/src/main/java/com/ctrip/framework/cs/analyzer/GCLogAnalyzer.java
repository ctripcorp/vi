package com.ctrip.framework.cs.analyzer;

import com.ctrip.framework.cs.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

/**
 * Created by jiangjie on 3/27/17.
 */
public final class GCLogAnalyzer {


    private StringBuilder logJsonBuilder = new StringBuilder();
    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);
    Path filePath;
    private GCLogAnalyzer(Path filePath){

        this.filePath = filePath;
    }
    private GCLogAnalyzer(){

    }

    public static String parseToJson(Path path){

        GCLogAnalyzer analyzer = new GCLogAnalyzer(path);
        File f = path.toFile();
        try(InputStream is = new FileInputStream(f)){
           analyzer.parseFile(is);
        }catch (Throwable e){
            logger.warn("parse gclog "+path+" failed!",e);
        }

        return analyzer.logJsonBuilder.toString();
    }


    public static String parseToJson(InputStream inputStream){

        GCLogAnalyzer analyzer = new GCLogAnalyzer();
        analyzer.parseFile(inputStream);

        return analyzer.logJsonBuilder.toString();
    }

    void parseFile(InputStream is){
        try(InputStream stream = IOUtils.decompressStream(is);
            InputStreamReader reader = new InputStreamReader(stream)) {

            logJsonBuilder.append('[');
            while (true){
                if(this.parseLine(reader)== ReadResult.Over){
                    break;
                }
            }

            if(logJsonBuilder.length()>3) {
                logJsonBuilder.deleteCharAt(logJsonBuilder.length() - 1);
            }
            logJsonBuilder.append(']');


        }catch (Throwable e){

            logger.warn("parse gclog "+filePath+" failed!",e);

        }
    }


    private enum ReadResult{
        True,False,Over
    }
    ReadResult readToBuffer(Reader reader,char[] buffer) throws IOException {

        for(int i=0;i<buffer.length;i++){
            int code = reader.read();
            if(code == -1){
                return ReadResult.Over;
            }

            buffer[i] = (char) code;
            if(buffer[i] =='\n'){
                return ReadResult.False;
            }

            if(!Character.isDigit(buffer[i])){
                return skipLine(reader)? ReadResult.False: ReadResult.Over;

            }
        }
        return ReadResult.True;
    }

    boolean skipLine(Reader reader) throws IOException {

        int c;
        while (true){
            //skip char
            if((c=reader.read())==-1 || c=='\n'){
               break;
            }
        }
        return c!=-1;

    }

    String[] parseInfoData(String info){

        int sp = info.indexOf(",");

        String[] rtn = new String[4];
        String sizeData;
        if(sp>0){
            sizeData = info.substring(0, sp);
            info = info.substring(sp+1);
            info = info.substring(0,info.lastIndexOf(" "));
            rtn[0] = info;
        }else {
            sizeData = info;
            rtn[0] = "0";

        }

        StringBuilder builder = new StringBuilder();
        byte startIndex = 0;
        for (int i = 0; i < sizeData.length(); i++) {
            char c = sizeData.charAt(i);
            if(Character.isDigit(c)){
                builder.append(c);

            }else {

                if(builder.length()>0 && startIndex <3) {
                    rtn[++startIndex] = builder.toString();
                    builder.setLength(0);
                }


            }
        }
        if(startIndex<4) {
            String[] dst = new String[startIndex+1];

            System.arraycopy(rtn, 0, dst, 0, startIndex+1);
            return dst;
        }

        return rtn;
    }

    String lastFullDuration = null;
    String lastFullHeap = null;
    boolean parseInfo(Reader reader,StringBuilder lineBuffer) throws IOException {

        int c;
        StringBuilder buffer = new StringBuilder();
        boolean startParse =false;
        boolean beginSub = false;
        boolean mainLabel = false;
        String[] mainData=null;
        String[] subData=null;
        boolean subLabel = false;
        int lastChar=0;
        boolean mainParse = false;
        String[] CMSData = null;
        String[] permData = null;
        int subCount = 0;
        String subLabelStr="";
        String mainTmpStr = "";

        while ((c=reader.read())!=-1 && c!='\n'){
            //skip char
            if(!mainParse) {
                if (startParse) {
                    buffer.append((char) c);
                    if (!beginSub) {
                        if (!mainLabel && c == ' ') {
                            //gcInfo.label = buffer.substring(0,buffer.length()-(lastChar==':'?2:1));
                            mainLabel = true;
                            buffer.setLength(0);
                        } else if (c == '[') {
                            if(!mainLabel){
                                mainLabel = true;
                            }
                            if(buffer.length()>5 && buffer.charAt(buffer.length()-3)==',') {
                                mainTmpStr = buffer.substring(0, buffer.length() - 3);
                            }
                            buffer.setLength(0);
                            beginSub = true;
                            subLabel = false;
                            subCount++;

                        } else if (c == ']') {
                            String tmp = buffer.substring(0, buffer.length() - 1);
                            mainData = parseInfoData(mainTmpStr + tmp);
                            buffer.setLength(0);
                            mainParse = true;
                        }
                    } else {
                        if (!subLabel && c == ' ' && lastChar == ':') {
                            subLabel = true;
                            subLabelStr = buffer.substring(0,buffer.length()-(lastChar==':'?2:1));
                            buffer.setLength(0);

                        } else if (c == ']') {
                            String tmp = buffer.substring(0, buffer.length() - 1);
                            if(!subLabel){
                                int index = tmp.indexOf(',');
                                if(index>0){
                                    subLabel = true;
                                    subLabelStr = tmp.substring(0,index);
                                }

                            }
                            if("CMS".equalsIgnoreCase(subLabelStr)){
                                CMSData = parseInfoData(tmp);
                            }else if("CMS Perm ".equalsIgnoreCase(subLabelStr)){
                                permData = parseInfoData(tmp);
                            }else if(subData == null) {
                                subData = parseInfoData(tmp);
                            }

                            buffer.setLength(0);
                            beginSub = false;

                        }

                    }

                } else if (c == '[') {
                    startParse = true;
                }
            }else{
                buffer.append((char) c);

            }

            lastChar = c;
        }

        if((subData== null && CMSData == null) || mainData == null){
            return false;
        }

        String duration = mainData[0];
        final int arrayMaxLen = 3;
        if(mainData.length ==arrayMaxLen){
            lineBuffer.append("true,");
            if(subCount == 1){
                lastFullDuration =duration;
                lastFullHeap = mainData[1];
                return false;
            }
            if(lastFullDuration != null){
                duration = String.valueOf((Float.parseFloat(duration)+Float.parseFloat(lastFullDuration)));
            }
        }else if(permData != null){
            lineBuffer.append("true,");
        }else{
            lineBuffer.append("false,");
            lastFullDuration = null;
            lastFullHeap = null;
        }

        lineBuffer.append(duration).append(",");//duration

        if(subData != null) {
            if (subData.length == arrayMaxLen) {
                lineBuffer.append("0,");//minor before
                lineBuffer.append(subData[1]).append(",");//minor after
                lineBuffer.append(subData[2]).append(",");//young gen
            } else {
                lineBuffer.append(subData[1]).append(",");//minor before
                lineBuffer.append(subData[2]).append(",");//minor after
                lineBuffer.append(subData[3]).append(",");//young gen
            }
        }else{
            lineBuffer.append(CMSData[1]).append(",");//old before
            lineBuffer.append(CMSData[2]).append(",");//old after
            lineBuffer.append(CMSData[3]).append(",");//old gen
        }

        if(mainData.length == arrayMaxLen){
            lineBuffer.append(lastFullHeap).append(",");//heap before
            lineBuffer.append(mainData[1]).append(",");// heap after
            lineBuffer.append(mainData[2]);//heap

        }else {
            lineBuffer.append(mainData[1]).append(",");//heap before
            lineBuffer.append(mainData[2]).append(",");//heap after
            lineBuffer.append(mainData[3]);//heap
        }

        if(permData != null){
            lineBuffer.append(",").append(permData[1]).append(",");//perm before
            lineBuffer.append(permData[2]).append(",");//perm after
            lineBuffer.append(permData[3] );//perm gen
        }


        return true;

    }

    ReadResult readSep(Reader reader,char expect) throws IOException {

        int c = reader.read();
        switch (c){
            case -1:
                return ReadResult.Over;
            case '\n':
                return ReadResult.False;

        }

        if(c != expect){
            return skipLine(reader)? ReadResult.False: ReadResult.Over;
        }

        return ReadResult.True;
    }

    ReadResult parseLine(Reader reader) throws IOException {

        char[] year = new char[4];

        ReadResult result =readToBuffer(reader, year);
       if(result != ReadResult.True){
           return result;
       }

        result = readSep(reader, '-');
        if(result != ReadResult.True){
            return result;
        }

        int c;
        StringBuilder buffer = new StringBuilder();
        buffer.append("[\"");
        buffer.append(year);
        buffer.append('-');
        while ((c=reader.read()) != -1){
            if(c == '\n') {
                return ReadResult.False;
            }else if(c == ' ') {
                break;
            }
            buffer.append((char)c);

        }
        buffer.deleteCharAt(buffer.length()-1);
        buffer.append("\",");

        if(parseInfo(reader,buffer)){
            logJsonBuilder.append(buffer);
            logJsonBuilder.append("],");
        }

        return result;

    }

}
