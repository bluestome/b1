
package android.skymobi.messenger.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: RegexUtil
 * @Description: 正则表达式
 * @author Anson.Yang
 * @date 2012-3-27 下午3:19:43
 */
public class RegexUtil {
    public static ArrayList<String> getSMSContent(String content) {
        Pattern pattern = Pattern.compile("\\[([\\w\\d]+)\\]");
        Matcher matcher = pattern.matcher(content);
        ArrayList<String> list = new ArrayList<String>();
        while (matcher.find()) {
            String group = matcher.group(1);
            if (null != group) {
                list.add(group);
            }
        }
        return list;
    }

    public static String getRegexString(String str, String regex, int group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        String result = null;
        while (matcher.find()) {
            result = matcher.group(group);
        }

        return result;
    }
}
