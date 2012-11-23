
package android.skymobi.messenger.adapter;

import android.skymobi.common.log.SLog;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.comparator.ComparatorFactory;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.ImageUtils;
import android.skymobi.messenger.utils.PinYinUtil;
import android.skymobi.messenger.utils.SearchUtil;
import android.skymobi.messenger.utils.SearchUtil.PinyinResult;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName: ContactListCache
 * @deprecated： 定义联系人缓存，并且建立skyid/phone/accout和Contact的映射关系
 * @author Sean.Xie
 * @date 2012-4-23 下午3:51:34
 */
public class ContactListCache {

    // 联系人列表缓存
    private final ArrayList<ContactsListItem> items = new ArrayList<ContactsListItem>();
    // 联系人搜索列表缓存
    private final ArrayList<ContactsListItem> searchItems = new ArrayList<ContactsListItem>();

    private static ContactListCache instance;
    // 建立电话号码和Contact的映射
    private final HashMap<String, Contact> phoneMap = new HashMap<String, Contact>();
    // 建立skyid和Contact的映射
    private final HashMap<Integer, Contact> skyidMap = new HashMap<Integer, Contact>();
    // 建立accountid和Contact的映射
    private final HashMap<Long, Contact> accountMap = new HashMap<Long, Contact>();

    private final HashMap<String, String> phoneMapForDisplay = new HashMap<String, String>();
    // 建立skyid和Contact的映射
    private final HashMap<Integer, String> skyidMapForDisplay = new HashMap<Integer, String>();
    private String preSearchText;
    private long mSearchStartTime;
    // 记录联系人总数
    private long mTotalContactCount = 0;

    // 区分联系人列表的在线和全部类别,根据列表设置显示的数据
    private boolean selectOnlineType = false;
    private boolean mPreOnlineType = false;
    private ArrayList<ContactsListItem> onlineItems = new ArrayList<ContactsListItem>();

    private ContactListCache() {
    }

    public ArrayList<ContactsListItem> getListItems() {
        return items;
    }

    /**
     * @return
     */
    public static ContactListCache getInstance() {
        if (instance == null) {
            instance = new ContactListCache();
        }
        return instance;
    }

    public void clearListItems() {
        items.clear();
    }

    /**
     * 重置列表项
     * 
     * @param contact
     */
    public void resetContactItem(Contact contact) {
        for (ContactsListItem item : items) {
            if (item.getId() == contact.getId()) {
                String signature = contact.getSignature();
                String phone = null;
                for (Account account : contact.getAccounts()) {
                    if (!TextUtils.isEmpty(account.getPhone())) {
                        phone = account.getPhone();
                        break;
                    }
                }
                // 2012-11-07 如果签名为空，则优先显示手机号码，如果手机号码也不存在，则签名显示空
                if (!TextUtils.isEmpty(signature)) {
                    item.setSignature(signature);
                } else if (!TextUtils.isEmpty(phone)) {
                    item.setSignature(phone);
                } else {
                    item.setSignature("");
                }
                if (!TextUtils.isEmpty(contact.getPhotoId())) {
                    item.setPhotoId(contact.getPhotoId());
                }
                setContactMap(item);
                setContactMapForDisplay(item);
                return;
            }
        }
    }

    /**
     * 重新组织列表
     */
    public void recreateItems(ArrayList<Contact> contacts) {
        mTotalContactCount = contacts.size();
        ArrayList<ContactsListItem> children = new ArrayList<ContactsListItem>();
        // "#" 组
        ArrayList<ContactsListItem> sharpList = new ArrayList<ContactsListItem>();
        char lastLetter = 32;
        String groupName = "#";
        boolean hasSharpGroup = false;
        for (int i = 0; i < contacts.size(); i++) {
            String pinyin = contacts.get(i).getPinyin();
            if (pinyin != null && pinyin.length() > 0) {
                char current = pinyin.charAt(0);
                if (current >= 'a' && current <= 'z') {
                    if (lastLetter != current) {
                        lastLetter = current;
                        ContactsListItem group = new ContactsListItem();
                        group.setGroup(true);
                        groupName = String.valueOf(lastLetter).toUpperCase();
                        group.setGroupName(groupName);
                        children.add(group);
                    }
                } else {
                    groupName = "#";
                    if (!hasSharpGroup) {
                        hasSharpGroup = true;
                        ContactsListItem group = new ContactsListItem();
                        group.setGroup(true);
                        group.setGroupName(groupName);
                        sharpList.add(group);
                    }
                }
            } else {
                groupName = "#";
                if (!hasSharpGroup) {
                    hasSharpGroup = true;
                    ContactsListItem group = new ContactsListItem();
                    group.setGroup(true);
                    group.setGroupName(groupName);
                    sharpList.add(group);
                }
            }
            ContactsListItem item = new ContactsListItem();
            item.setId(contacts.get(i).getId());
            item.setPinyin(pinyin);
            item.setPhotoId(contacts.get(i).getPhotoId());
            item.setUserType(contacts.get(i).getUserType());
            item.setLocalContactId(contacts.get(i).getLocalContactId());
            item.setCloudId(contacts.get(i).getCloudId());
            String displayName = contacts.get(i).getDisplayname();
            String nickName = contacts.get(i).getNickName();
            String phone = contacts.get(i).getPhone();
            if (!TextUtils.isEmpty(displayName)) {
                item.setDisplayname(displayName);
            } else if (!TextUtils.isEmpty(nickName)) {
                item.setDisplayname(nickName);
            } else {
                item.setDisplayname(phone);
            }
            String signature = contacts.get(i).getSignature();
            if (!TextUtils.isEmpty(signature)) {
                item.setSignature(signature);
            } else if (!TextUtils.isEmpty(phone)) {
                item.setSignature(phone);
            } else {
                item.setSignature("");
            }
            item.setAccounts(contacts.get(i).getAccounts());
            item.setGroupName(groupName);
            if ("#".equals(item.getGroupName())) {
                sharpList.add(item);
            } else {
                children.add(item);
            }
        }
        for (int i = contacts.size() - 1; i >= 0; i--) {
            setContactMap(contacts.get(i));// 建立ContactMap
        }
        sortContact(children);
        children.addAll(sharpList);
        for (ContactsListItem item : children) {
            setContactMapForDisplay(item);
        }
        items.clear();
        items.addAll(children);
        CoreService service = CoreService.getInstance();
        if (service != null) {
            service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS, null);
        }
    }

    private void sortContact(ArrayList<ContactsListItem> contacts) {
        Comparator<ContactsListItem> comparator = new Comparator<ContactsListItem>() {
            @Override
            public int compare(ContactsListItem lhs, ContactsListItem rhs) {

                String lgroup = lhs.getGroupName();
                String rgroup = rhs.getGroupName();
                // 先按组排列顺序
                if (!lgroup.equals(rgroup)) {
                    return lgroup.compareTo(rgroup);
                }

                lhs.setPinyin(PinYinUtil.getPingYin(lhs.getDisplayname()));
                rhs.setPinyin(PinYinUtil.getPingYin(rhs.getDisplayname()));
                // 都没拼音相等
                if (TextUtils.isEmpty(lhs.getPinyin()) && TextUtils.isEmpty(rhs.getPinyin())) {
                    return 0;
                }
                // 左边没拼音左边小
                if (TextUtils.isEmpty(lhs.getPinyin())) {
                    return -1;
                }
                // 右边没拼音右边小
                if (TextUtils.isEmpty(rhs.getPinyin())) {
                    return 1;
                }

                // 拼音
                String[] lpinyin = lhs.getPinyin().split(" ");
                String[] rpinyin = rhs.getPinyin().split(" ");

                // 第一个字
                char lheader = lhs.getDisplayname().charAt(0);
                char rheader = rhs.getDisplayname().charAt(0);

                for (int i = 0; i < lpinyin.length && i < rpinyin.length; i++) {
                    String lpy = lpinyin[i];
                    String rpy = rpinyin[i];
                    // 如果拼音相同,比较第一个字
                    int result = lpy.compareTo(rpy);
                    if (result == 0) {
                        // 拼音相同,第一个字也相同,比较下一个拼音
                        if (lheader == rheader) {
                            continue;
                        } else {
                            return lheader - rheader;
                        }
                    } else {
                        return result;
                    }
                }
                // 比较完后,如果拼音相同第一个字也相同,看谁的字多,字数多的大
                int comp = lpinyin.length - rpinyin.length;
                if (comp == 0) {
                    String lhsStr = lhs.toAllStr();
                    String rhsStr = rhs.toAllStr();
                    if (lhsStr.equals(rhsStr)) {
                        return 0;
                    } else {
                        return lhsStr.compareTo(rhsStr);
                    }
                } else {
                    return comp;
                }
            }
        };
        Collections.sort(contacts, comparator);
        for (int i = 0; i < contacts.size() - 1;) {
            ContactsListItem first = contacts.get(i);
            ContactsListItem second = contacts.get(i + 1);
            if (comparator.compare(first, second) == 0) {
                if (first.toAllStr().equals(second.toAllStr())) {
                    contacts.remove(i + 1);
                    continue;
                }
            }
            i++;
        }
    }

    /**
     * 重新组织头像列表
     */
    public void recreatePhotosForSMS(ArrayList<Contact> contacts) {
        for (Contact c : contacts)
            setContactMap(c);
    }

    /**
     * 修改好友详情，需要更新一下HashMap
     */
    public void recreateFriend(Contact frd) {
        setContactMap(frd);
        setContactMapForDisplay(frd);
    }

    /**
     * 从联系人的cache中查询DisplayName
     * 
     * @param accountId
     * @param removeHeader
     * @param skyId
     * @return
     */
    public String getDisplayNameBySkyIdOrPhone(Threads thread,
            String phone, int skyId) {
        // 优先级accountId>skyId>phone
        try {
            if (skyId > 0) {
                String str = skyidMapForDisplay.get(skyId);
                if (str != null) {
                    JSONArray items = new JSONArray(str);
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            if (item.getInt(ContactsColumns.DELETED) != 1) {
                                return item.getString(ContactsColumns.DISPLAY_NAME);
                            }
                        }
                    }
                }

                if (thread != null && !TextUtils.isEmpty(thread.getDisplayName()))
                    return thread.getDisplayName();
            }

            if (!TextUtils.isEmpty(phone)) {
                String str = phoneMapForDisplay.get(phone);
                if (str != null) {
                    JSONArray items = new JSONArray(str);
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            if (item.getInt(ContactsColumns.DELETED) != 1) {
                                return TextUtils.isEmpty(item
                                        .getString(ContactsColumns.DISPLAY_NAME)) ? phone
                                        : item.getString(ContactsColumns.DISPLAY_NAME);
                            }
                        }
                    }
                }

                return phone;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 从联系人的cache中查询 photoId
     * 
     * @param accountId
     * @param phone
     * @param skyId
     * @return
     */
    public String getPhotoIdByAccountIdOrSkyIdOrPhone(long accountId, String phone, int skyId) {
        Contact item = null;
        if (accountId > 0) {
            item = accountMap.get(accountId);
        } else if (!TextUtils.isEmpty(phone)) {
            item = phoneMap.get(phone);
        } else if (skyId > 0) {
            item = skyidMap.get(skyId);
        }
        return (item == null) || !ImageUtils.isImageUrl(item.getPhotoId()) ? null : item
                .getPhotoId();
    }

    private void setContactMapForDisplay(Contact contact) {
        ArrayList<Account> accounts = contact.getAccounts();
        try {
            for (Account a : accounts) {
                if (!TextUtils.isEmpty(a.getPhone())) {
                    JSONArray jsonArray = new JSONArray();
                    String str = phoneMapForDisplay.get(a.getPhone());
                    if (str != null) {
                        jsonArray = new JSONArray(str);
                        boolean hasContact = false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);
                            if (item.getInt(ContactsColumns._ID) == contact.getId()) {
                                item.put(ContactsColumns.DISPLAY_NAME, contact.getDisplayname());
                                hasContact = true;
                            }
                        }
                        if (!hasContact) {
                            JSONObject item = new JSONObject();
                            item.put(ContactsColumns._ID, contact.getId());
                            item.put(ContactsColumns.DISPLAY_NAME, contact.getDisplayname());
                            item.put(ContactsColumns.DELETED, contact.getDeleted());
                            jsonArray.put(item);
                        }

                    } else {

                        JSONObject item = new JSONObject();
                        item.put(ContactsColumns._ID, contact.getId());
                        item.put(ContactsColumns.DISPLAY_NAME, contact.getDisplayname());
                        item.put(ContactsColumns.DELETED, contact.getDeleted());
                        jsonArray.put(item);

                    }
                    phoneMapForDisplay.put(a.getPhone(), jsonArray.toString());

                }
                if (a.getSkyId() > 0) {
                    JSONArray jsonArray = new JSONArray();
                    String str = skyidMapForDisplay.get(a.getSkyId());
                    if (str != null) {
                        jsonArray = new JSONArray(str);
                        boolean hasContact = false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);
                            if (item.getInt(ContactsColumns._ID) == contact.getId()) {
                                item.put(ContactsColumns.DISPLAY_NAME, contact.getDisplayname());
                                hasContact = true;
                            }
                        }
                        if (!hasContact) {
                            JSONObject item = new JSONObject();
                            item.put(ContactsColumns._ID, contact.getId());
                            item.put(ContactsColumns.DISPLAY_NAME, contact.getDisplayname());
                            item.put(ContactsColumns.DELETED, contact.getDeleted());
                            jsonArray.put(item);
                        }

                    } else {
                        jsonArray = new JSONArray();
                        JSONObject item = new JSONObject();
                        item.put(ContactsColumns._ID, contact.getId());
                        item.put(ContactsColumns.DISPLAY_NAME, contact.getDisplayname());
                        item.put(ContactsColumns.DELETED, contact.getDeleted());
                        jsonArray.put(item);

                    }
                    skyidMapForDisplay.put(a.getSkyId(), jsonArray.toString());
                }
                if (a.getId() > 0) {
                    accountMap.put(a.getId(), contact);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // 建立Map
    private void setContactMap(Contact contact) {
        ArrayList<Account> accounts = contact.getAccounts();
        for (Account a : accounts) {
            if (!TextUtils.isEmpty(a.getPhone())) {
                phoneMap.put(a.getPhone(), contact);
            }
            if (a.getSkyId() > 0) {
                skyidMap.put(a.getSkyId(), contact);
            }
            if (a.getId() > 0) {
                accountMap.put(a.getId(), contact);
            }

        }
    }

    public void removeContactMap(Contact contact) {
        try {
            if (phoneMap != null) {
                for (Account account : contact.getAccounts()) {
                    phoneMap.remove(account.getPhone());
                    String str = phoneMapForDisplay.get(account.getPhone());
                    if (str != null) {
                        JSONArray jsonArray = new JSONArray(str);

                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject item = jsonArray.getJSONObject(i);
                                if (item.getInt(ContactsColumns._ID) == contact.getId()) {
                                    jsonArray = ContactListCache.remove(i, jsonArray);
                                    break;
                                }
                            }
                        }
                        if (jsonArray.length() == 0) {
                            phoneMapForDisplay.remove(account.getPhone());
                        }
                    }
                }
            }
            if (skyidMap != null) {
                for (Account account : contact.getAccounts()) {
                    skyidMap.remove(account.getSkyId());

                    String str = skyidMapForDisplay.get(account.getSkyId());
                    if (str != null) {
                        JSONArray jsonArray = new JSONArray(str);

                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject item = jsonArray.getJSONObject(i);
                                if (item.getInt(ContactsColumns._ID) == contact.getId()) {
                                    jsonArray = ContactListCache.remove(i, jsonArray);
                                    break;
                                }
                            }
                        }
                        if (jsonArray.length() == 0) {
                            skyidMapForDisplay.remove(account.getSkyId());
                        }
                    }
                }
                if (accountMap != null) {
                    for (Account account : contact.getAccounts()) {
                        accountMap.remove(account.getId());
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 判断该skyid是否已经存在于联系人列表中
     * 
     * @param skyid
     * @return
     */
    public boolean isInContactsList(int skyid) {
        for (ContactsListItem item : items) {
            if (item.isSkyUser()) {
                ArrayList<Account> accounts = item.getAccounts();
                if (null != accounts && accounts.size() > 0) {
                    for (Account account : accounts) {
                        if (account.getSkyId() == skyid) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 拼装联系人项
     * 
     * @return
     */
    public synchronized ArrayList<ContactsListItem> getContactsListWithSearchText(String searchText) {
        // 搜索结果过期处理 30s
        long invalidTime = 30 * DateUtils.SECOND_IN_MILLIS;
        if ((System.currentTimeMillis() - mSearchStartTime) > invalidTime) {
            searchItems.clear();
        }

        if (TextUtils.isEmpty(searchText)) {
            // 初始化上次的搜索词
            preSearchText = null;
            if (selectOnlineType) {
                return onlineItems;
            } else {
                return getListItems();
            }
        }

        ArrayList<ContactsListItem> originalSearchItems = new ArrayList<ContactsListItem>();

        SLog.d("ContactListCache", "mPreOnlineType:" + mPreOnlineType + ",selectOnlineType:"
                + selectOnlineType);
        if (mPreOnlineType == selectOnlineType && null != preSearchText && searchItems.size() > 0) {
            // 两次是在同一个类别中搜索的时候,才能使用上一次的搜索结果
            String preString = searchText.length() >= preSearchText.length() ? searchText
                    .substring(
                            0, preSearchText.length()) : "";
            // 搜索在前一次的搜索结果上
            if (preString.equals(preSearchText)) {
                SLog.d("ContactListCache", "preString:" + preString);
                originalSearchItems = searchItems;
            } else {
                if (selectOnlineType) {
                    originalSearchItems = onlineItems;
                } else {
                    originalSearchItems = items;
                }
            }
        } else {
            if (selectOnlineType) {
                originalSearchItems = onlineItems;
            } else {
                originalSearchItems = items;
            }
        }

        mPreOnlineType = selectOnlineType;
        preSearchText = searchText;

        ArrayList<ContactsListItem> mNamesResult = new ArrayList<ContactsListAdapter.ContactsListItem>();
        ArrayList<ContactsListItem> header = new ArrayList<ContactsListAdapter.ContactsListItem>();
        ArrayList<ContactsListItem> fullChar = new ArrayList<ContactsListAdapter.ContactsListItem>();
        ArrayList<ContactsListItem> number = new ArrayList<ContactsListAdapter.ContactsListItem>();
        boolean isMacth = false;
        int size = originalSearchItems.size();

        int searchType = checkChineseLetter(searchText); // 搜索类型

        for (int i = 0; i < size && searchText.equals(preSearchText); i++) {
            String phone = null;
            ContactsListItem contact = originalSearchItems.get(i);
            if (contact.isGroup()) {
                continue;
            }
            ContactsListItem item = new ContactsListItem();
            String displayName = contact.getDisplayname();
            PinyinResult result = null;
            String sortKey = PinYinUtil.getSortKey(displayName);

            if (searchType == CHINESE) {
                result = SearchUtil.searchByZhongWen(searchText, sortKey);
                if (result.isMacth()) {
                    // 按中文查找
                    item.setHightlightType(result.getType());
                    item.setPositions(result.getPositions());
                    mNamesResult.add(item);
                    isMacth = true;
                }
            } else if (searchType == NUMBER) {
                // 按电话查找
                for (Account account : contact.getAccounts()) {
                    if ((result = SearchUtil.searchByNumber(searchText, account.getPhone()))
                            .isMacth()) {
                        item.setHightlightType(result.getType());
                        phone = account.getPhone();
                        item.setPositions(result.getPositions());
                        item.setSignature(account.getPhone());
                        number.add(item);
                        isMacth = true;
                        break;
                    }
                }
            } else {
                if ((result = SearchUtil.searchByHeaderChar(searchText, sortKey)).isMacth()) {
                    // 按首字母查找
                    item.setHightlightType(result.getType());
                    item.setPositions(result.getPositions());
                    header.add(item);
                    isMacth = true;
                } else if ((result = SearchUtil.searchByFullChar(searchText, sortKey)).isMacth()) {
                    // 按全拼查找
                    item.setHightlightType(result.getType());
                    item.setPositions(result.getPositions());
                    fullChar.add(item);
                    isMacth = true;
                }
            }
            if (isMacth) {
                item.setAccounts(contact.getAccounts());
                item.setPhotoId(contact.getPhotoId());
                item.setDisplayname(displayName);
                item.setId(contact.getId());
                // String pinyin = contact.getPinyin();
                String pinyin = PinYinUtil.getPingYin(contact.getDisplayname());
                if (pinyin != null && pinyin.length() > 0) {
                    item.setContactID(contact.getId());
                    item.setUserType(contact.getUserType());
                    item.setSortkey(contact.getSortkey());
                    if (!TextUtils.isEmpty(phone)) {
                        item.setPhone(phone);
                    } else {
                        item.setPhone(contact.getPhone());
                    }
                    item.setDisplayname(contact.getDisplayname());
                    if (!TextUtils.isEmpty(phone)) {
                        item.setSignature(phone);
                    } else if (!TextUtils.isEmpty(contact.getSignature())) {
                        item.setSignature(contact.getSignature());
                    }
                }
            }
        }
        if (!searchText.equals(preSearchText)) {
            return searchItems;
        }
        searchItems.clear();
        // 排序 中文>英文>数字>特殊字符
        Collections.sort(mNamesResult, ComparatorFactory.getContactSearchComparator());
        searchItems.addAll(mNamesResult);
        Collections.sort(header, ComparatorFactory.getContactSearchComparator());
        searchItems.addAll(header);
        Collections.sort(fullChar, ComparatorFactory.getContactSearchComparator());
        searchItems.addAll(fullChar);
        searchItems.addAll(number);
        Log.e(this.getClass().getSimpleName(), " > 目标数据:" + searchItems.size());
        Log.e(this.getClass().getSimpleName(), " > searchText :" + searchText);
        // 搜索的时间记录
        mSearchStartTime = System.currentTimeMillis();
        return searchItems;
    }

    public static final int MIX = 0; // 非中文混合
    public static final int NUMBER = 1; // 数字
    public static final int CHINESE = 2;// 中文

    /**
     * @param sortKey
     * @return
     */
    private int checkChineseLetter(String str) {
        if (str.matches("\\d{1,}")) {
            return NUMBER;
        }

        int length = str.toLowerCase().length();
        for (int i = 0; i < length; i++) {
            char letter = str.charAt(i);
            if (!((letter >= 'a' && letter <= 'z') || (letter >= '0' && letter <= '9'))) {
                return CHINESE;
            }
        }
        return MIX;
    }

    public void removeItemById(long id) {
        for (int i = 0; i < items.size(); i++) {
            long itemId = items.get(i).getId();
            if (itemId == id) {
                items.remove(i);
                if (i > 0) {
                    if (items.get(i - 1).isGroup()
                            && (i == items.size() || items.get(i).isGroup())) {
                        items.remove(i - 1);
                    }
                }
                break;
            }
        }
    }

    public long getTotalCount() {
        return mTotalContactCount;
    }

    /**
     * @param selectOnlineType the selectOnlineType to set
     */
    public void setSelectOnlineType(boolean selectOnlineType) {
        this.selectOnlineType = selectOnlineType;
    }

    /**
     * @return the selectOnlineType
     */
    public boolean isSelectOnlineType() {
        return selectOnlineType;
    }

    /**
     * @param onlineItems the onlineItems to set
     */
    public void setOnlineItems(ArrayList<ContactsListItem> onlineItems) {
        this.onlineItems = onlineItems;
    }

    /**
     * @return the onlineItems
     */
    public ArrayList<ContactsListItem> getOnlineItems() {
        return onlineItems;
    }

    public static void main(String[] args) {
        JSONArray itemsArray = new JSONArray();
        try {
            for (int i = 0; i < 5; i++) {
                JSONObject item = new JSONObject();
                item.put("No." + i, "value:" + i);
                itemsArray.put(item);
            }

            System.out.println(itemsArray.toString());

            JSONObject itemNew = itemsArray.toJSONObject(itemsArray);
            System.out.println(itemNew.toString());
            System.out.println(itemNew.remove("3"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }
}
