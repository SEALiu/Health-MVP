package cn.sealiu.health;

import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import cn.sealiu.health.setting.SettingFragment;
import cn.sealiu.health.util.Fun;

import static org.junit.Assert.assertEquals;

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
        System.out.print(Fun.encode("MD5", "123456"));
    }

    @Test
    public void test() {
        String dataCache = "";

        String[] datas = {
                "00",
                "FF0111160200000047E10000192CFF0D0A",
                "FF24080000000000000000000000FF0D0AFF2308",
                "0400000000000000000000FF0D0A",
                "FF0111162500000047E50000192DFF0D0A"
        };


        Pattern p24 = Pattern.compile("^FF24[\\dA-F]{24}FF0D0AFF23[\\dA-F]{2}");
        Pattern p = Pattern.compile("[\\dA-F]{22}FF0D0A$");

        for (String data : datas) {
            if (p24.matcher(data.toUpperCase()).find()) {
                dataCache = data;
            } else if (p.matcher(data.toUpperCase()).find() && !dataCache.equals("")) {
                data = dataCache + data;
                dataCache = "";
                System.out.println(data.substring(34, 68));
            }
        }
    }

    @Test
    public void testChineseLen() {
        System.out.print(SettingFragment.length("左肩") / 2);
    }

    @Test
    public void testDateBetween() {
        Date start = null;
        Date end = null;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            start = df.parse("2017-8-1");
            end = df.parse("2017-10-1");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (start != null && end != null) {
            for (String str : Fun.getDatesBetweenTwoDate(start, end)) {
                System.out.println(str);
            }
        }
    }

    @Test
    public void testGetWeekDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] leastWeek = new String[7];

        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            leastWeek[i] = df.format(today.getTime());
            today.add(Calendar.DATE, -1);
        }

        for (String str : leastWeek) {
            System.out.println(str);
        }
    }
}