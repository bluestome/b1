
package android.skymobi.messenger.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @ClassName: AccountParser
 * @Description: AccountParser
 * @author Michael.Pan
 * @date 2012-4-9 下午02:19:05
 */
public class AccountParser {
    public static ArrayList<Long> getAccountIDList(String accountIDs) {
        ArrayList<Long> list = new ArrayList<Long>();
        if (accountIDs == null || accountIDs.trim().equals(""))
            return list;
        StringTokenizer tokenizer = new StringTokenizer(accountIDs, Constants.separator);
        while (tokenizer.hasMoreTokens()) {
            list.add(Long.valueOf(tokenizer.nextToken()));
        }
        return list;
    }

    public static ArrayList<Long> getAddressIDList(String addressIDs) {
        ArrayList<Long> list = new ArrayList<Long>();
        if (addressIDs == null || addressIDs.trim().equals(""))
            return list;
        StringTokenizer tokenizer = new StringTokenizer(addressIDs, Constants.separator);
        while (tokenizer.hasMoreTokens()) {
            list.add(Long.valueOf(tokenizer.nextToken()));
        }
        return list;
    }
}
