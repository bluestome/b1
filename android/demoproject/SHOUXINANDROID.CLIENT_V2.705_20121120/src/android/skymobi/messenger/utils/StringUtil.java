
package android.skymobi.messenger.utils;

import android.text.TextUtils;

/**
 * @ClassName: StringUtils
 * @author Sean.Xie
 * @date 2012-4-16 下午2:46:28
 */
public class StringUtil {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     */
    private StringUtil() {
    }

    public static String convertNull(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value;
    }

    /**
     * 判断输入的内容是否为手机号码
     * 
     * @param str 输入字符串
     * @return
     */
    public static boolean isPhoneNumber(String str) {
        if (null == str || str.equals("") || str.length() != 11) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否为空
     * 
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 去+86
     * 
     * @param phone
     * @return
     */
    public static String removeHeader(String phone) {
        if (phone != null) {
            // 去掉0086
            if (phone.startsWith("0086")) {
                phone = phone.replaceFirst("0086", "");
            }
            // 去掉+86
            if (phone.startsWith("+86")) {
                phone = phone.replaceFirst("\\+86", "");
            }
            // 去掉电话中间"-"
            phone = phone.replaceAll("-", "");
            // 去掉所有的空格，回车，Tab空格
            phone = phone.replaceAll("\\s+", "");

        }
        return phone;
    }

    /**
     * 去空格
     * 
     * @param phone
     * @return
     */
    public static String removeSpace(String phone) {
        if (phone != null) {
            phone = phone.replaceAll("-", "");
            phone = phone.replaceAll("\\s+", "");
        }
        return phone;
    }

    /**
     * 自动补零，默认是8位
     * 
     * @param code
     * @return
     */
    public static String autoFixZero(int code) {
        String pattern = "00000000";
        java.text.DecimalFormat df = new java.text.DecimalFormat(pattern);
        return df.format(code);
    }
}
