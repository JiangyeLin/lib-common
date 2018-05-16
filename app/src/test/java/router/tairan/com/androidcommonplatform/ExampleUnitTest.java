package router.tairan.com.androidcommonplatform;

import android.net.Uri;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        Uri uri = Uri.parse("http://www.baidu.com/1/regist");
        System.out.println(uri.getPath());
    }
}