package cn.sealiu.health;

import org.junit.Test;

import cn.sealiu.health.util.Fun;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void md5() {
        System.out.print(Fun.encode("MD5", "sme0rmm"));
    }
}