
package android.skymobi.messenger.utils;

import java.util.ArrayList;

public class PinYinUtil {

    /**
     * 将中文字符转化为拼音,其他字符不变
     * 
     * @param inputString
     * @return
     */
    public static String getPingYin(char input) {
        return getToken(input).target;
    }
    /**
     * 将字符串中的中文转化为拼音,其他字符不变
     * 
     * @param inputString
     * @return
     */
    public static String getPingYin(String inputString) {
        ArrayList<Token> tokens = getTokens(inputString);
        if (tokens == null) {
            return inputString;
        }
        StringBuilder pinyin = new StringBuilder();
        for (Token token : tokens) {
            switch (token.type) {
                case Token.UNKNOWN:
                    pinyin.append(token.source);
                    break;
                case Token.PINYIN:
                case Token.LATIN:
                    pinyin.append(token.target);
            }
            pinyin.append(" ");
        }
        return pinyin.toString();
    }

    public static String getSortKey(String inputString) {
        ArrayList<Token> tokens = getTokens(inputString);
        if (tokens == null || tokens.size() == 0) {
            return inputString;
        }
        StringBuilder pinyin = new StringBuilder();
        for (Token token : tokens) {
            if (token.source.equalsIgnoreCase(token.target)) {
                for (int i = 0; i < token.source.length(); i++) {
                    pinyin.append(token.source.charAt(i));
                    pinyin.append(" ");
                    pinyin.append(token.target.charAt(i));
                    pinyin.append(" ");
                }
            } else {
                pinyin.append(token.source);
                pinyin.append(" ");
                pinyin.append(token.target);
                pinyin.append(" ");
            }
        }
        return pinyin.substring(0, pinyin.length() - 1);
    }

    private static ArrayList<Token> getTokens(String input) {
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >= 11) {
            return HanziToPinyin4.getInstance().get(input);
        } else {
            return HanziToPinyin.getInstance().get(input);
        }
    }
    
    private static Token getToken(char input) {
        int version = android.os.Build.VERSION.SDK_INT;
        if (version >= 11) {
            return HanziToPinyin4.getInstance().getToken(input);
        } else {
            return HanziToPinyin.getInstance().getToken(input);
        }
    }
    
    
}
