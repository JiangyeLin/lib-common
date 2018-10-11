package com.trc.android.common.h5.devtool;

import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * JiangyeLin on 2018/7/24
 * 为html添加缩进
 * <title>aaa</title> 这种情况不需要缩进换行
 *
 * <div>
 * xxxxxx
 * </div>
 * 这种为需要缩进换行的情况
 * <p>
 * <meta xxxxxxx>这种单标签，结束之后直接换行
 */
public class HtmlFormatterUtil {

    /**
     * 可以单独出现，不需要结束标签的那些标签
     */
    private static final String[] ENDTAGS = new String[]{
            "DOCTYPE", "area", "input", "img", "hr", "br", "link", "meta"
    };

    public static void formatter(String str, File file) {
        if (file.exists()) {
            file.delete();
        }
        int depth = 0;//缩进深度

        int left = 0;   //"<" 标签开始位置
        int right = 0;  //">" 标签结束位置
        int length = str.length();

        boolean isNeedBlank = true;

        StringBuilder result = new StringBuilder();
        StringBuilder tmpString = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if ('<' == c) {
                left = i;
            } else if ('>' == c) {
                right = i;
            }
            tmpString.append(c);
            if (right < left || right <= 0) {
                //未找到成对匹配的标签，进行下一次遍历
                continue;
            }

            if (str.charAt(left + 1) == '/') {
                // 类似于</br> 一个标签结束
                depth--;
                result.append(putBlank(depth, tmpString, isNeedBlank));
            } else if ('/' == str.charAt(right - 1)) {
                // 类似于<br/> 一个标签结束
                result.append(putBlank(depth, tmpString, isNeedBlank));
            } else if (isSingleEndTags(str.substring(left, right))) {
                // 类似与<input xxxx> 不需要/闭合的情况
                result.append(putBlank(depth, tmpString, isNeedBlank));
            } else {
                result.append(putBlank(depth, tmpString, isNeedBlank));
                depth++;
            }

            int nextLeft;
            isNeedBlank = true;
            for (nextLeft = right; nextLeft < length; nextLeft++) {
                /**
                 *查找right位置后，第一个左尖括号的位置，二者之间的内容即为代码中的文本内容
                 * <title>xxxxxxx</title>
                 */
                if (str.charAt(nextLeft) == '<') {
                    String tmp = str.substring(right + 1, nextLeft).trim();

                    isNeedBlank = TextUtils.isEmpty(tmp);
                    break;
                }
            }
            if (isNeedBlank) {
                result.append('\n');
            }

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file, true);
                fileOutputStream.write(result.toString().getBytes());
                fileOutputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //清空stringbuilder暂存
                result.delete(0, result.length());
                tmpString.delete(0, tmpString.length());
                right = 0;
            }
        }
    }

    private static boolean isSingleEndTags(String str) {
        for (String key : ENDTAGS) {
            if (str.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static String putBlank(int depth, StringBuilder str, boolean isNeedBlack) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!isNeedBlack) {
            return stringBuilder.append(str).toString();
        }
        while (depth > 0) {
            stringBuilder.append(" ");
            depth--;
        }
        return stringBuilder.append(str).toString();
    }
}
