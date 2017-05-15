package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.util.IOUtils;
import com.ctrip.framework.cornerstone.util.TextUtils;
import org.junit.Test;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by jiang.j on 2016/5/3.
 */
public class TextUtilsTest {
    @Test
    public void testExtractTagText() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("testdata.xml");
        try {
            String xml = IOUtils.readAll(is);
            assertEquals("jiang.j", TextUtils.getTagInnerText("cas:user", xml));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFormatString(){
        String raw = "abc?name=%{hello}good%dfdf&dd=%{great}";

        Map<String,String> params = new HashMap<>();
        params.put("hello","framework");
        params.put("great","world");
        String expect = "abc?name=frameworkgood%dfdf&dd=world";
        assertEquals(expect,TextUtils.formatString(raw,params));
        raw = "http://ddfdf%4{df}dd?v1=%{version}&name=%{name}dd&{%}";
        expect = "http://ddfdf%4{df}dd?v1=&name=dd&{%}";
        assertEquals(expect,TextUtils.formatString(raw,params));
        params.put("version","1.8");
        params.put("name","java");
        expect = "http://ddfdf%4{df}dd?v1=1.8&name=javadd&{%}";
        assertEquals(expect,TextUtils.formatString(raw,params));
        raw = "http://ddfdf%4{df}dd?v1=%{version}&name=%{name}&dd=%{great}&{%}";
        expect = "http://ddfdf%4{df}dd?v1=1.8&name=java&dd=world&{%}";
        assertEquals(expect,TextUtils.formatString(raw,params));
    }

}
