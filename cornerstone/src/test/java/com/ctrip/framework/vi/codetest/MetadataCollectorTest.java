package com.ctrip.framework.vi.codetest;

import com.ctrip.framework.vi.code.debug.ClassField;
import com.ctrip.framework.vi.code.debug.ClassMetadata;
import com.ctrip.framework.vi.instrument.AgentTool;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/8/8.
 */
public class MetadataCollectorTest {

    @Test
    public void testGetFiled() throws IOException {

        ClassMetadata metadata = AgentTool.getClassMetadata(ClassField.class.getName());

        List<ClassField> fieldList = metadata.getFields();
        assertTrue(fieldList.size() > 0);

        Map<String,String> props = metadata.getPropMethodFields();
        System.out.print(props);


    }
}
