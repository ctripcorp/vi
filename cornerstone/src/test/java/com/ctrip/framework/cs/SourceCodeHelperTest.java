package com.ctrip.framework.cs;

import com.ctrip.framework.cs.code.SourceCodeHelper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/6/20.
 */
public class SourceCodeHelperTest {

    @Test
    public void testGetSourceCode() throws IOException {
        SourceCodeHelper.SourceCode sourceCode =SourceCodeHelper.getJarSourceCode("rt.jar", "java.lang.Thread.java");
        assertNotNull(sourceCode);
        assertNotNull(sourceCode.size);
        assertNotNull(sourceCode.content);
        assertNotNull(sourceCode.file_name);
        assertTrue(sourceCode.content.length() > 100);
    }
}
