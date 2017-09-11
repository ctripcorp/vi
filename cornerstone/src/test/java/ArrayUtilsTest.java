import com.ctrip.framework.vi.util.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/5/8.
 */
public class ArrayUtilsTest {

    @Test
    public void testMergeArray(){
        int[] a= {1,6,7,10,30};
        int[] b = {3,8,9,20};

        int[] c = ArrayUtils.mergeSortedArray(a, b);
        assertEquals(a.length + b.length, c.length);
        assertTrue(Arrays.equals(new int[]{1, 3, 6, 7, 8, 9, 10, 20, 30},c));

    }
    @Test
    public void testMergeNoSortedArray(){
        int[] a= {1,6,7,10,30};
        int[] b = {8,3,9,20};

        int[] c = ArrayUtils.mergeSortedArray(a, b);
        assertEquals(a.length + b.length, c.length);
        assertFalse(Arrays.equals(new int[]{1, 3, 6, 7, 8, 9, 10, 20, 30}, c));

    }

    @Test
    public void testSubtractSimpleArray(){
        int[] a= {1,6,7,10,30};
        int[] b = {6,10};
        int[] c = ArrayUtils.subtractSortedArray(a, b);
        assertEquals(a.length - b.length, c.length);
        assertTrue(Arrays.equals(new int[]{1,7,30}, c));

    }

    @Test
    public void testSubtractArray(){
        int[] a= new int[10000];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        int[] b = new int[100];
        for (int i = 0; i < b.length; i++) {
            b[i] = i+500;
        }

        int[] c = ArrayUtils.subtractSortedArray(a, b);

        int[] expect = new int[a.length-b.length];

        for (int i = 0; i < 500; i++) {
           expect[i] = i;
        }

        int leftLen = expect.length;
        for (int i = 500; i < leftLen; i++) {
            expect[i] = i+100;
        }

        assertEquals(a.length - b.length, c.length);
        assertTrue(Arrays.equals(expect,c));

    }
}
