
package android.skymobi.messenger.comparator;

import android.skymobi.common.log.SLog;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @ClassName: CloudComparator
 * @author Sean.Xie
 * @date 2012-3-27 下午5:59:16
 */
public class ComparatorFactory {

    /**
     * 手信联系人比较器
     */
    private static Comparator<Contact> shouxinComparator = new Comparator<Contact>() {
        @Override
        public int compare(Contact lhs, Contact rhs) {
            return lhs.getId() == rhs.getId() ? 0 : 1;
        }
    };
    /**
     * 云端联系人比较器
     */
    private static Comparator<Contact> cloudComparator = new Comparator<Contact>() {
        @Override
        public int compare(Contact lhs, Contact rhs) {
            return lhs.getCloudId() == rhs.getCloudId() ? 0 : 1;
        }
    };
    /**
     * 本地联系人比较器
     */
    private static Comparator<Contact> localComparator = new Comparator<Contact>() {
        @Override
        public int compare(Contact lhs, Contact rhs) {
            return lhs.getLocalContactId() == rhs.getLocalContactId() ? 0 : 1;
        }
    };
    /**
     * 账号比较器
     */
    private static Comparator<Account> accountComparator = new Comparator<Account>() {
        @Override
        public int compare(Account lhs, Account rhs) {
            if (!TextUtils.isEmpty(lhs.getPhone()) && lhs.getPhone().equals(rhs.getPhone())) {
                return 0;
            } else if ((lhs.getSkyId() != 0) && (lhs.getSkyId() == rhs.getSkyId())) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    /**
     * 手信联系人比较器
     */
    public static Comparator<Contact> getShouxinComparator() {
        return shouxinComparator;
    }

    /**
     * 云端联系人比较器
     */
    public static Comparator<Contact> getCloudComparator() {
        return cloudComparator;
    }

    /**
     * 本地联系人比较器
     */
    public static Comparator<Contact> getLocalComparator() {
        return localComparator;
    }

    /**
     * 账号比较器
     */
    public static Comparator<Account> getAccountComparator() {
        return accountComparator;
    }

    // 联系人列表按照传入的比较器进行排序并且去重,isDeleteMulti表示是否去重和去空
    public static void SortContactList(ArrayList<Contact> list, Comparator<Contact> comparator,
            boolean isDeleteMulti) {
        Collections.sort(list, comparator);
        SLog.d("Sort", "去重前：list.size() = " + list.size());

        // 去掉帐号是空的联系人和去掉重复联系人
        if (isDeleteMulti) {
            for (int i = 0; i < list.size() - 1; i++) {
                // 去掉
                if (list.get(i).getAccounts().isEmpty()) {
                    list.remove(i);
                    i--;
                    continue;
                }
                if (comparator.compare(list.get(i), list.get(i + 1)) == 0) {
                    list.remove(i + 1);
                    i--;
                }
            }
        }
        SLog.d("Sort", "去重后：list.size() = " + list.size());
    }

    // account 排序比较器CM1 按照电话1，电话2来排序
    private static Comparator<Account> accountCM1 = new Comparator<Account>() {
        @Override
        public int compare(Account lhs, Account rhs) {
            String lhsPhone = lhs.getPhone();
            String rhsPhone = rhs.getPhone();
            if (TextUtils.isEmpty(lhsPhone) && TextUtils.isEmpty(rhsPhone)) {
                return 0;
            }
            if (TextUtils.isEmpty(lhsPhone)) {
                return -1;
            }
            if (TextUtils.isEmpty(rhsPhone)) {
                return 1;
            }
            return (lhsPhone.compareTo(rhsPhone));
        }

    };

    // 帐号列表accounts进行排序，电话1，电话2，帐号来排序
    public static void SortAccountListCM1(ArrayList<Account> list) {
        Collections.sort(list, accountCM1);
    }

    // account 排序比较器CM2，按照电话1，电话2，帐号1，帐号2来排序
    private static Comparator<Account> accountCM2 = new Comparator<Account>() {
        @Override
        public int compare(Account lhs, Account rhs) {

            String lhsPhone = lhs.getPhone();
            String rhsPhone = rhs.getPhone();

            if (TextUtils.isEmpty(lhsPhone) && TextUtils.isEmpty(rhsPhone)) {

                int lhsSkyId = lhs.getSkyId();
                int rhsSkyId = rhs.getSkyId();

                return (lhsSkyId - rhsSkyId);
            }
            if (TextUtils.isEmpty(lhsPhone)) {
                return -1;
            }
            if (TextUtils.isEmpty(rhsPhone)) {
                return 1;
            }
            return (lhsPhone.compareTo(rhsPhone));
        }

    };

    // 帐号列表accounts进行排序，电话1，电话2，帐号来排序
    public static void SortAccountListCM2(ArrayList<Account> list) {
        Collections.sort(list, accountCM2);
    }

    // 定义本地联系人列表和手信联系人列表之间的数据比较器CM1
    private static Comparator<Contact> CM1 = new Comparator<Contact>() {

        @Override
        public int compare(Contact lhs, Contact rhs) {
            String lhsDisplayname = lhs.getDisplayname();
            String rhsDisplayname = rhs.getDisplayname();
            if (TextUtils.isEmpty(lhsDisplayname) && TextUtils.isEmpty(rhsDisplayname)) {
                return 0;
            }
            if (TextUtils.isEmpty(lhsDisplayname)) {
                return -1;
            }
            if (TextUtils.isEmpty(rhsDisplayname)) {
                return 1;
            }

            int result = lhsDisplayname.compareTo(rhsDisplayname);
            if (result > 0) {
                return 1;
            } else if (result < 0) {
                return -1;
            } else {
                String lhsToPhoneStr = lhs.toPhoneStr();
                String rhsToPhoneStr = rhs.toPhoneStr();
                if (TextUtils.isEmpty(lhsToPhoneStr)
                        && TextUtils.isEmpty(rhsToPhoneStr)) {
                    return 0;
                }
                if (TextUtils.isEmpty(lhsToPhoneStr)) {
                    return -1;
                }
                if (TextUtils.isEmpty(rhsToPhoneStr)) {
                    return 1;
                }

                return lhsToPhoneStr.compareTo(rhsToPhoneStr);
            }
        }
    };

    public static Comparator<Contact> getCM1Comparator() {
        return CM1;
    }

    // 定义云端联系人和手信联系人列表之间的数据比较器CM2
    static class ComparatorCM2 implements Comparator<Contact> {
        private final boolean isContain;

        /**
         * <p>
         * Title:
         * </p>
         * <p>
         * Description:
         * </p>
         * 
         * @param isContain
         */
        public ComparatorCM2(boolean isContain) {
            super();
            this.isContain = isContain;
        }

        @Override
        public int compare(Contact lhs, Contact rhs) {

            String lhsDisplayname = lhs.getDisplayname();
            String rhsDisplayname = rhs.getDisplayname();
            if (TextUtils.isEmpty(lhsDisplayname) && TextUtils.isEmpty(rhsDisplayname)) {
                return 0;
            }
            if (TextUtils.isEmpty(lhsDisplayname)) {
                return -1;
            }
            if (TextUtils.isEmpty(rhsDisplayname)) {
                return 1;
            }

            int result = lhsDisplayname.compareTo(rhsDisplayname);
            if (result > 0) {
                return 1;
            } else if (result < 0) {
                return -1;
            } else {
                String lhsToAllStr = lhs.toAllStr();
                String rhsToAllStr = rhs.toAllStr();
                if (TextUtils.isEmpty(lhsToAllStr)
                        && TextUtils.isEmpty(rhsToAllStr)) {
                    return 0;
                }
                if (TextUtils.isEmpty(lhsToAllStr)) {
                    return -1;
                }
                if (TextUtils.isEmpty(rhsToAllStr)) {
                    return 1;
                }
                // 如果云端和手信是云端包含手信的关系，那么我们也认为是相等的
                if (isContain && lhsToAllStr.contains(rhsToAllStr)) {
                    return 0;
                }
                return lhsToAllStr.compareTo(rhsToAllStr);
            }
        }
    }

    public static ComparatorCM2 getCM2ComparatorForComp() {
        return new ComparatorFactory.ComparatorCM2(true);
    }

    public static ComparatorCM2 getCM2ComparatorForSort() {
        return new ComparatorFactory.ComparatorCM2(false);
    }

    public static int ComparaTwoList(ArrayList<Contact> left, ArrayList<Contact> right,
            Comparator<Contact> comparator, CompCallBack callback) {
        if (comparator == null || callback == null) {
            return 0;
        }
        int L = 0;
        int R = 0;
        int lenL = left.size();
        int lenR = right.size();
        int count = 0;
        while (L < lenL && R < lenR) {
            count++;
            Contact lhs = left.get(L);
            Contact rhs = right.get(R);
            int result = comparator.compare(left.get(L), right.get(R));
            if (result == 0) {
                callback.onCmpEqual(lhs, rhs);
                L++;
                R++;
            } else if (result < 0) {
                callback.onCmpLess(lhs, rhs);
                L++;
            } else {
                callback.onCmpMore(lhs, rhs);
                R++;
            }
        }
        // 处理列表最后剩余没参与比较的值
        while (L < lenL) {
            Contact lhs = left.get(L);
            callback.onCmpLTail(lhs);
            L++;
        }
        // 处理列表最后剩余没参与比较的值
        while (R < lenR) {
            Contact rhs = right.get(R);
            callback.onCmpRTail(rhs);
            R++;
        }

        return count;
    }

    private static Comparator<ContactsListItem> contactSearchComparator = new Comparator<ContactsListItem>() {
        @Override
        public int compare(ContactsListItem lhs, ContactsListItem rhs) {
            int[] lPt = lhs.getPositions();
            int[] rPt = rhs.getPositions();
            // if (lPt.toString().equals(rPt.toString())) {
            // return 0;
            // }
            for (int i = 0; i < (lPt.length <= rPt.length ? lPt.length : rPt.length); i++) {
                int leftValue = lPt[i];
                int rightValue = rPt[i];
                if (leftValue == rightValue) {
                    continue;
                }

                return leftValue > rightValue ? 1 : -1;
            }
            return 0;
        }
    };

    public static Comparator<ContactsListItem> getContactSearchComparator() {
        return contactSearchComparator;
    }
}
