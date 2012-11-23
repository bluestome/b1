
package android.skymobi.messenger.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchUtil {

    /**
     * 根据号码查找
     * 
     * @param searchText
     * @param phoneContent
     * @return
     */
    public static PinyinResult searchByNumber(String searchText, String phoneContent) {
        PinyinResult result = new PinyinResult();
        if (TextUtils.isEmpty(phoneContent)) {
            return result;
        }
        int index = phoneContent.indexOf(searchText);
        if (index != -1) {
            result.positions = new int[2];
            result.positions[0] = index;
            result.positions[1] = index + searchText.length();
            result.isMacth = true;
            result.type = PinyinResult.TYPE_NUMBER;
            return result;
        }

        return result;
    }

    /**
     * 根据全拼音查找
     * 
     * @param searchText
     * @param content
     * @return
     */
    public static PinyinResult searchByFullChar(String searchText, String sortKey) {
        PinyinResult result = new PinyinResult();
        if (TextUtils.isEmpty(sortKey)) {
            return result;
        }

        if (!isNotEmpty(sortKey)) {
            return result;
        }
        // 全字母匹配
        String[] pinyins = toHanyuPinyinFromSortKey(sortKey);
        if (pinyins == null)
            return result;
        result.positions = new int[pinyins.length];
        result.type = PinyinResult.TYPE_PINYIN;

        int currentPotion = 0;
        int countLetter = 0;
        for (int i = 0; i < pinyins.length && !TextUtils.isEmpty(searchText); i++) {
            String pinyin = pinyins[i];

            // 拼音 pattern
            StringBuilder pinyinPattern = new StringBuilder();
            if (pinyin.length() > searchText.length()) {
                countLetter = searchText.length();
                for (int j = 0; j < searchText.length(); j++) {
                    pinyinPattern.append(searchText.charAt(j));
                }
            } else {
                countLetter = pinyin.length();
                for (int j = 0; j < pinyin.length(); j++) {
                    pinyinPattern.append(searchText.charAt(j));
                }
            }
            pinyinPattern.append(".*");
            Pattern pattern = Pattern.compile(pinyinPattern.toString());
            Matcher matcher = pattern.matcher(pinyin);
            boolean fullCharMatch = matcher.matches();
            if (fullCharMatch) {
                result.positions[currentPotion++] = i + 1;
                result.isMacth = true;
                searchText = searchText.substring(countLetter);
            } else {
                result.isMacth = false;
            }
        }
        if (searchText.length() > 0) {
            result.isMacth = false;
        }
        return result;
    }

    public static PinyinResult searchByZhongWen(String searchText, String sortKey) {
        PinyinResult result = new PinyinResult();
        if (TextUtils.isEmpty(sortKey)) {
            return result;
        }

        if (!isNotEmpty(sortKey)) {
            return result;
        }
        String[] pinyins = toZhongWenFromSortKey(sortKey);
        if (pinyins == null)
            return result;
        result.positions = new int[pinyins.length];
        result.type = PinyinResult.TYPE_PINYIN;

        int currentPotion = 0;
        int countLetter = 0;
        for (int i = 0; i < pinyins.length && !TextUtils.isEmpty(searchText); i++) {
            String pinyin = pinyins[i];

            StringBuilder pinyinPattern = new StringBuilder();
            if (pinyin.length() > searchText.length()) {
                countLetter = searchText.length();
                for (int j = 0; j < searchText.length(); j++) {
                    pinyinPattern.append(searchText.charAt(j));
                }
            } else {
                countLetter = pinyin.length();
                for (int j = 0; j < pinyin.length(); j++) {
                    pinyinPattern.append(searchText.charAt(j));
                }
            }
            pinyinPattern.append(".*");
            Pattern pattern = Pattern.compile(pinyinPattern.toString());
            Matcher matcher = pattern.matcher(pinyin);
            boolean fullCharMatch = matcher.matches();
            if (fullCharMatch) {
                result.positions[currentPotion++] = i + 1;
                result.isMacth = true;
                searchText = searchText.substring(countLetter);
            } else {
                result.isMacth = false;
            }
        }
        if (searchText.length() > 0) {
            result.isMacth = false;
        }
        return result;
    }

    /**
     * @param content
     * @return
     */
    private static String[] toHanyuPinyinFromSortKey(String sortKey) {
        String[] sk = sortKey.split("\\s");
        String[] pinyin = new String[sk.length / 2];

        for (int i = 0; i < sk.length; i += 2) {
            pinyin[(i + 1) / 2] = sk[i + 1];
        }
        return pinyin;
    }

    private static String[] toZhongWenFromSortKey(String sortKey) {
        String[] sk = sortKey.split("\\s");
        String[] pinyin = new String[sk.length / 2];

        for (int i = 0; i < sk.length; i += 2) {
            pinyin[i / 2] = sk[i];
        }
        return pinyin;
    }

    /**
     * 根据首字母查找
     * 
     * @param searchText
     * @param content
     * @return
     */
    public static PinyinResult searchByHeaderChar(String searchText, String sortKey) {
        PinyinResult result = new PinyinResult();
        if (TextUtils.isEmpty(sortKey)) {
            return result;
        }

        if (!isNotEmpty(sortKey)) {
            return result;
        }

        // 首字母匹配
        String[] pinyins = toHanyuPinyinFromSortKey(sortKey);
        if (pinyins == null)
            return result;
        result.type = PinyinResult.TYPE_PINYIN;
        result.positions = new int[pinyins.length];

        int current = 0;
        for (int i = 0; i < pinyins.length; i++) {

            String pinyin = pinyins[i];
            if(TextUtils.isEmpty(pinyin)){
                continue;
            }
            char headerChar = pinyin.charAt(0);
            boolean headerMatch = false;
            if(headerChar == searchText.charAt(current)){
                headerMatch = true;
            }
            if (headerMatch) {
                // 首字母匹配成功
                result.positions[current] = i + 1;
                current++;
                if (current == searchText.length()) {
                    result.isMacth = true;
                    return result;
                }
            } else {
                result.isMacth = false;
            }
        }
        return result;
    }

    /**
     * 判断非空
     * 
     * @param value
     * @return
     */
    private static boolean isNotEmpty(String value) {
        if (value == null || value.trim().length() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 判断数字
     * 
     * @param value
     * @return
     */
    private static boolean matchNumber(String value) {
        if (isNotEmpty(value)) {
            if (value.matches("\\d{1,}")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回结果
     * 
     * @ClassName: PinyinResult
     * @author Sean.Xie
     * @date 2012-3-29 下午3:38:04
     */
    public static class PinyinResult {
        public static int TYPE_PINYIN = 1;
        public static int TYPE_NUMBER = 2;

        private boolean isMacth;
        private int[] positions;
        private int type;

        public boolean isMacth() {
            return isMacth;
        }

        public int[] getPositions() {
            return positions;
        }

        public int getType() {
            return type;
        }
    }
}
