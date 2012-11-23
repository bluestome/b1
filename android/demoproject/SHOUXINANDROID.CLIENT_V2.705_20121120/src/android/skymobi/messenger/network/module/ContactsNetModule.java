
package android.skymobi.messenger.network.module;

import android.graphics.Bitmap;
import android.skymobi.app.net.event.ISXListener;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Friend;
import android.skymobi.messenger.cache.APPCache;
import android.skymobi.messenger.comparator.ComparatorFactory;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.exception.MessengerException;
import android.skymobi.messenger.exception.NoNeedSendRequestException;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.provider.SocialMessenger.FriendsColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.Observer;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.FileUtils;
import android.skymobi.messenger.utils.ImageUtils;
import android.skymobi.messenger.utils.ListUtil;
import android.skymobi.messenger.utils.PropertiesUtils;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.StringUtil;
import android.text.TextUtils;
import android.util.Log;

import com.skymobi.android.sx.codec.beans.clientbean.NetAddFriendResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetBlackResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetCalcFriendsResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetContacts;
import com.skymobi.android.sx.codec.beans.clientbean.NetContactsPhone;
import com.skymobi.android.sx.codec.beans.clientbean.NetContactsResultInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetDownloadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetBlakListResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetContactsList2Response;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetContactsListResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetContactsStatus2Response;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetFriendstResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetUserInfoByUserNameResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetOperateContactsResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetSpecifiedContactsStatusResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetSyncContactsResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfoResponse;
import com.skymobi.android.sx.codec.beans.common.BlackList;
import com.skymobi.android.sx.codec.beans.common.ContactsStatus;
import com.skymobi.android.sx.codec.beans.common.ContactsStatusItem;
import com.skymobi.android.sx.codec.beans.common.FriendsList;
import com.skymobi.android.sx.codec.beans.common.SimpleStatus;
import com.skymobi.android.sx.codec.beans.common.SimpleStatusItem;
import com.skymobi.android.sx.codec.beans.common.SimpleUserInfo;
import com.skymobi.android.sx.codec.beans.common.SimpleUserInfoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: MessageNetModule
 * @author Sean.Xie
 * @date 2012-3-2 上午11:17:34
 */
public class ContactsNetModule extends BaseNetModule {

    private static String TAG = ContactsNetModule.class.getSimpleName();

    /**
     * @param netClient.getBiz()
     */
    public ContactsNetModule(ISXListener netClient) {
        super(netClient);
    }

    /**
     * 获取推荐好友
     * 
     * @param start 起始数
     * @param pageSize 每页的数量
     * @param updateTime
     * @return null 表示网络错误,空列表表示无更新
     */
    public ArrayList<Friend> getFriends(int start, int pageSize, long updateTime) {
        ArrayList<Friend> friends = null;
        if (start < 0 || pageSize < 0) {
            return friends;
        }
        if (!MainApp.isLoggedIn()) {
            return friends;
        }
        NetGetFriendstResponse resp = netClient.getBiz()
                .getFriendsList(start, pageSize, updateTime);
        if (resp.isSuccess()) {
            friends = new ArrayList<Friend>();
            if (resp.isUpdate()) {
                ArrayList<FriendsList> list = resp.getFiendList();
                for (FriendsList item : list) {
                    Friend friend = new Friend();
                    friend.setContactType(item.getContactType());
                    friend.setDetailReason(item.getDetailReason());
                    friend.setRecommendReason(item.getRecommendReason());
                    friend.setNickName(item.getNickname());
                    friend.setTalkReason(item.getTalkReason());
                    friend.setSignature(item.getUsignature());
                    friend.setPhotoId(item.getImageHead());
                    Account account = new Account();
                    account.setNickName(item.getNickname());
                    account.setSkyId(item.getSkyId());
                    friend.addAccount(account);
                    friends.add(friend);
                }
                DaoFactory.getInstance(MainApp.i()).getUsersDAO()
                        .saveFriendLastUpdateTime(resp.getUpdateTime());
            }
        }
        return friends;
    }

    /**
     * 获取联系人详情
     * 
     * @param skyid
     * @return
     */
    public Contact getContactBySkyID(int skyid) {
        Contact contact = null;
        // 获取skyid时，如果发现没有登录或者skyid值非法，那么直接返回null
        if (!MainApp.isLoggedIn() || skyid <= 0) {
            return contact;
        }
        NetUserInfoResponse resp = netClient.getBiz().getBuddyUserInfo(skyid);
        Log.i(TAG, "get Contact by skyid");
        if (resp.isSuccess()) {
            NetUserInfo userInfo = resp.getUserInfo();
            if (userInfo == null) {
                return null;
            }
            int status = resp.getStatus();
            contact = getContact(userInfo, status);
        } else {
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
        }
        return contact;
    }

    /**
     * @param contact
     * @param userInfo
     * @param status
     */
    public Contact getContact(NetUserInfo userInfo, int onlineStatus) {
        Contact contact = new Contact();
        contact.setBirthday(DateUtil.getLongTime(userInfo.getUbirthday()));
        String province = userInfo.getUprovince();
        String city = userInfo.getUcity();
        if (province == null) {
            province = "";
        }
        if (city == null) {
            city = "";
        }
        String hometown = province;
        // 直辖市省份和城市名一样，如“北京”，只显示直辖市名
        if (!city.equals(hometown)) {
            hometown = hometown.concat(" ").concat(city);
        }
        contact.setHometown(hometown.trim());
        contact.setNote(userInfo.getUdesc());
        contact.setOrganization(userInfo.getUcorporation());
        contact.setPhone(userInfo.getUmobile());
        String photoId = userInfo.getUuidPortrait();
        if (ImageUtils.isImageUrl(photoId)) {
            contact.setPhotoId(photoId);
        }
        contact.setSchool(userInfo.getUschoolgraduated());
        try {
            contact.setSex(Integer.valueOf(userInfo.getUsex()));
        } catch (NumberFormatException e) {
            // 处理userInfo.getSex() 为null的情况,默认设置为男生
            contact.setSex(1);
        }

        contact.setSignature(userInfo.getUsignature());
        contact.setSynced(ContactsColumns.SYNC_YES);
        contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
        ArrayList<Account> accounts = new ArrayList<Account>();
        Account account = new Account();
        account.setSkyAccount(userInfo.getUserName());
        String nickName = StringUtil.isBlank(userInfo.getPersonnickname()) ? userInfo
                .getNickname() : userInfo.getPersonnickname();
        account.setNickName(nickName);
        account.setSkyId(userInfo.getSkyId());
        String phone = userInfo.getUmobile();
        if (!TextUtils.isEmpty(phone) && !phone.contains("*")) {
            account.setPhone(phone);
        }
        account.setOnline(onlineStatus == 0 ? false : true);
        accounts.add(account);
        contact.setNickName(nickName);
        contact.setAccounts(accounts);

        return contact;
    }

    /**
     * 增量获取云端联系人列表（获取服务器端变化的联系人（包括增加、删除、更新等））
     */

    public ArrayList<Contact> getIncContactsList() {
        ArrayList<Contact> list = new ArrayList<Contact>();
        ArrayList<Integer> notSuccessPage = new ArrayList<Integer>(); // 记录未能正常获取的页数
        int current = 1;
        TransationContacts transationContacts = getIncContactsList(current);
        int totalsize = transationContacts.totalSize;
        int totlePage = ((totalsize / Constants.PAGESIZE) + (totalsize % Constants.PAGESIZE > 0 ? 1
                : 0));
        SLog.e(TAG,
                "增量联系人totalsize = " + totalsize + " , totlePage = " + totlePage);
        current++;
        list.addAll(transationContacts.list);
        TransationContacts transation = null;
        while (current <= totlePage) {
            transation = getIncContactsList(current);
            if (transation.isSuccess) {
                list.addAll(transation.list);
                SLog.e(TAG,
                        "page success = " + current + ", list size = " + list.size());
            } else {
                notSuccessPage.add(current);
            }
            current++;
        }
        // 将错误的页数再次获取,再失败那就无语了
        for (int page : notSuccessPage) {
            transation = getIncContactsList(page);
            if (transation.isSuccess) {
                list.addAll(transation.list);
                SLog.e(TAG,
                        "重现获取 page = " + page + ", list size = " + list.size());
            } else {
                SLog.e(TAG, "重现获取仍然失败  那就没救了 page = " + page);
            }
        }
        if (totalsize != list.size()) {
            SLog.e(TAG, "云端获取联系人数目不匹配");
            throw new MessengerException("云端获取联系人数目不匹配");
        }
        APPCache.getInstance().setContactVersion(transationContacts.updateTime);
        return list;
    }

    /**
     * 全量获取云端联系人列表
     */

    public ArrayList<Contact> getContactsList(Observer ob) {
        ArrayList<Contact> list = new ArrayList<Contact>();
        ArrayList<Integer> notSuccessPage = new ArrayList<Integer>(); // 记录未能正常获取的页数
        int current = 1;
        TransationContacts transationContacts = getContactsList(current);
        if (ob != null) {
            ob.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS,
                    Constants.SYNC_PROCESS_CONTACTS_BEGIN);
        }
        int totalsize = transationContacts.totalSize;
        int totlePage = ((totalsize / Constants.PAGESIZE) + (totalsize % Constants.PAGESIZE > 0 ? 1
                : 0));
        SLog.e(TAG,
                "全量联系人totalsize = " + totalsize + " , totlePage = " + totlePage);
        current++;
        list.addAll(transationContacts.list);
        TransationContacts transation = null;
        while (current <= totlePage) {
            transation = getContactsList(current);
            if (ob != null) {
                int process = Constants.SYNC_PROCESS_CONTACTS_BEGIN + 5 * current;
                if (process > Constants.SYNC_PROCESS_CONTACTS_SETP1)
                    process = Constants.SYNC_PROCESS_CONTACTS_SETP1;
                ob.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS, process);
            }
            if (transation.isSuccess) {
                list.addAll(transation.list);
                SLog.e(TAG,
                        "page success = " + current + ", list size = " + list.size());
            } else {
                notSuccessPage.add(current);
            }
            current++;
        }
        // 将错误的页数再次获取,再失败那就无语了
        for (int page : notSuccessPage) {
            transation = getContactsList(page);
            if (transation.isSuccess) {
                list.addAll(transation.list);
                SLog.e(TAG,
                        "重现获取 page = " + page + ", list size = " + list.size());
            } else {
                SLog.e(TAG, "重现获取仍然失败  那就没救了 page = " + page);
            }
        }
        if (totalsize != list.size()) {
            SLog.e(TAG, "云端获取联系人数目不匹配");
            throw new MessengerException("云端获取联系人数目不匹配");
        }
        APPCache.getInstance().setContactVersion(transationContacts.updateTime);
        return list;
    }

    /**
     * 增量获取联系人列表
     * 
     * @param page
     * @return
     */
    private TransationContacts getIncContactsList(int page) {
        TransationContacts transationContacts = new TransationContacts();
        ArrayList<Contact> list = new ArrayList<Contact>();
        transationContacts.list = list;
        if (!MainApp.isLoggedIn()) {
            throw new NoNeedSendRequestException();
        }
        NetSyncContactsResponse resp = netClient.getBiz().syncContacts(
                CommonPreferences.getContactsLastTimeUpdate(),
                page, Constants.PAGESIZE);

        if (resp.isSuccess()) {
            transationContacts.isSuccess = true;
            if (!resp.isHasUpdate()) {
                return transationContacts;
            }

            transationContacts.totalSize = resp.getTotalSize();
            transationContacts.updateTime = resp.getUpdateTime();
            ArrayList<NetContacts> contacts = resp.getContactsList();
            if (contacts != null) {
                for (NetContacts contact : contacts) {
                    Contact item = new Contact();
                    Integer cloudId = contact.getContactId();
                    String displayName = contact.getContactName();
                    if (TextUtils.isEmpty(displayName)) {
                        displayName = "";
                    }
                    Byte type = contact.getContactType();
                    String note = contact.getMemo();
                    Integer contactId = contact.getSequenceId();
                    item.setCloudId(cloudId);
                    item.setId(contactId);
                    item.setBlackList(contact.getGroup());
                    Byte action = contact.getAction();
                    if (action != null)
                        item.setAction(action);
                    item.setSynced(1);
                    item.setDisplayname(displayName);
                    item.setNote(note);
                    boolean hasMain = false;
                    ArrayList<NetContactsPhone> phones = contact.getPhoneList();
                    if (phones != null) {
                        for (NetContactsPhone phone : phones) {
                            int skyId = phone.getBuddyId();
                            String accountPhone = phone.getPhone();
                            accountPhone = StringUtil.removeHeader(accountPhone);
                            if (TextUtils.isEmpty(accountPhone) && skyId == 0) {
                                continue;
                            }
                            Account account = new Account();
                            account.setSkyId(skyId);
                            String nickName = phone.getNickname();
                            account.setNickName(nickName);
                            account.setPhone(accountPhone);
                            item.addAccount(account);
                            if (skyId != 0) {
                                item.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                                if (!hasMain) {
                                    account.setMain(1);
                                    if (TextUtils.isEmpty(displayName)) {
                                        item.setDisplayname(nickName);
                                    }
                                    hasMain = true;
                                }
                            }
                        }
                    }
                    list.add(item);
                }
            }
        } else {
            transationContacts.isSuccess = false;
            SLog.e(TAG, " error page = " + page);
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            SLog.e(TAG, "getContactsList : " + resp.getResultHint());
        }
        return transationContacts;
    }

    /**
     * 全量获取联系人列表
     * 
     * @param page
     * @return
     */
    private TransationContacts getContactsList(int page) {
        TransationContacts transationContacts = new TransationContacts();
        ArrayList<Contact> list = new ArrayList<Contact>();
        transationContacts.list = list;

        if (!MainApp.isLoggedIn()) {
            throw new NoNeedSendRequestException();
        }
        NetGetContactsListResponse resp = netClient.getBiz().getContactsList(
                CommonPreferences.getContactsLastTimeUpdate(),
                page, Constants.PAGESIZE);

        if (resp.isSuccess()) {
            transationContacts.isSuccess = true;
            if (!resp.isHasUpdate()) {
                return transationContacts;
            }

            transationContacts.totalSize = resp.getTotalSize();
            transationContacts.updateTime = resp.getUpdateTime();
            ArrayList<NetContacts> contacts = resp.getContactsList();
            if (contacts != null) {
                for (NetContacts contact : contacts) {
                    Contact item = new Contact();
                    Integer cloudId = contact.getContactId();
                    String displayName = contact.getContactName();
                    if (TextUtils.isEmpty(displayName)) {
                        displayName = "";
                    }
                    Byte type = contact.getContactType();
                    String note = contact.getMemo();
                    Integer contactId = contact.getSequenceId();
                    item.setCloudId(cloudId);
                    item.setId(contactId);
                    item.setBlackList(contact.getGroup());
                    Byte action = contact.getAction();
                    if (action != null)
                        item.setAction(action);
                    item.setSynced(1);
                    item.setDisplayname(displayName);
                    item.setNote(note);
                    boolean hasMain = false;
                    ArrayList<NetContactsPhone> phones = contact.getPhoneList();
                    if (phones != null) {
                        for (NetContactsPhone phone : phones) {
                            int skyId = phone.getBuddyId();
                            String accountPhone = phone.getPhone();
                            accountPhone = StringUtil.removeHeader(accountPhone);
                            if (TextUtils.isEmpty(accountPhone) && skyId == 0) {
                                continue;
                            }
                            Account account = new Account();
                            account.setSkyId(skyId);
                            String nickName = phone.getNickname();
                            account.setNickName(nickName);
                            account.setPhone(accountPhone);
                            item.addAccount(account);
                            if (skyId != 0) {
                                item.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                                if (!hasMain) {
                                    account.setMain(1);
                                    if (TextUtils.isEmpty(displayName)) {
                                        item.setDisplayname(nickName);
                                    }
                                    hasMain = true;
                                }
                            }
                        }
                    }
                    list.add(item);
                }
            }
        } else {
            transationContacts.isSuccess = false;
            SLog.e(TAG, " error page = " + page);
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            SLog.e(TAG, "getContactsList : " + resp.getResultHint());
        }
        return transationContacts;
    }

    /**
     * 修改联系人
     * 
     * @param contact
     * @return
     */
    public Contact updateContact(Contact contact) {
        try {
            contact.setAction(3);
            return uploadContact(contact);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 添加联系人
     * 
     * @param contact
     * @return
     */
    public Contact addContact(Contact contact) {
        try {
            contact.setAction(1);
            return uploadContact(contact);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将可能认识的人添加为好友
     */
    public Contact addFriends(Friend friend) {
        int buddySkyid = 0;
        // 从联系人中获取帐号信息中的SKYID
        if (null != friend.getAccounts() && friend.getAccounts().size() > 0) {
            // 推荐好友被修改后账号可能多个
            ArrayList<Account> accounts = friend.getAccounts();
            for (Account account : accounts) {
                if (account.getSkyId() > 0) {
                    buddySkyid = account.getSkyId();
                    break;
                }

            }
        }
        if (buddySkyid > 0) {
            // 区分与找朋友推荐的类型,
            // contactType基础上加FriendsColumns.CONTACT_TYPE_FROM_HELPER。
            // 1：运营账号 2：推荐联系人
            SLog.d(TAG, "FriendsColumns.CONTACT_TYPE_FROM_HELPER " + friend.getContactType());
            int contactType = friend.getContactType() >= FriendsColumns.CONTACT_TYPE_FROM_HELPER ? friend
                    .getContactType()
                    - FriendsColumns.CONTACT_TYPE_FROM_HELPER
                    : friend.getContactType();
            friend.setContactType(contactType);
            SLog.d(TAG, "FriendsColumns.CONTACT_TYPE_FROM_HELPER " + friend.getContactType());
            if (!MainApp.isLoggedIn()) {
                return null;
            }
            NetAddFriendResponse response = netClient.getBiz().addFriend(buddySkyid,
                    (byte) friend.getContactType());
            if (response.isSuccess()) {
                // 获取当前的最后更新时间
                long lastUpdateTime = response.getUpdateTime();
                if (lastUpdateTime != 0) {
                    // 如果最后修改时间不为0，则将当前的修改时间存入文件中，以供其他地方使用
                    CommonPreferences.saveContactsLastTimeUpdate(lastUpdateTime);
                }
                int coludId = response.getContactId();
                friend.setCloudId(coludId);
                friend.setLastUpdateTime(lastUpdateTime);

                Contact temp = getContactStatus(friend.getCloudId());
                if (temp != null) {
                    friend.setPhotoId(temp.getPhotoId());
                    ArrayList<Account> oldAccounts = friend.getAccounts();
                    ArrayList<Account> resultAccounts = temp.getAccounts();

                    for (Account account : oldAccounts) {
                        Account tmp = ListUtil.getObject(resultAccounts, account,
                                ComparatorFactory.getAccountComparator());
                        if (tmp != null) {
                            account.setContactId(friend.getContactId());
                            account.setNickName(tmp.getNickName());
                            account.setSkyAccount(tmp.getSkyAccount());
                            account.setSkyId(tmp.getSkyId());
                            account.setOnline(tmp.isOnline());
                            if (account.getSkyId() != 0) {
                                friend.setNickName(tmp.getNickName());
                                friend.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                                if (!TextUtils.isEmpty(friend.getDisplayname())) {
                                    friend.setDisplayname(tmp.getNickName());
                                }
                                MainApp.i().putUserOnlineStatus(account.getSkyId(),
                                        account.isOnline());
                            }
                        }
                    }
                }
                return friend;
            } else {
                if (response.getResultCode() == -1) {
                    response.setResult(Constants.NET_ERROR, response.getResultHint());
                }
                ResultCode.setCode(response.getResultCode());
                Log.e(TAG,
                        "添加可能认识的好友失败|" + response.getResultCode() + "|" + response.getResultHint());
                return null;
            }
        } else {
            Log.e(TAG, "获取好友SKYID失败");
            return null;
        }
    }

    /**
     * 将陌生人添加为好友
     */
    public Contact addFriends(Contact contact, byte contactType) {
        int buddySkyid = -1;
        // 从联系人中获取帐号信息中的SKYID
        if (null != contact.getAccounts() && contact.getAccounts().size() > 0) {
            Account account = contact.getAccounts().get(0);
            if (null != account) {
                buddySkyid = account.getSkyId();
            }
        }
        if (buddySkyid > -1) {
            if (!MainApp.isLoggedIn()) {
                return null;
            }
            NetAddFriendResponse response = netClient.getBiz().addFriend(buddySkyid,
                    contactType);
            if (response.isSuccess()) {
                long lastUpdateTime = response.getUpdateTime();
                if (lastUpdateTime != 0) {
                    // 如果最后修改时间不为0，则将当前的修改时间存入文件中，以供其他地方使用
                    CommonPreferences.saveContactsLastTimeUpdate(lastUpdateTime);
                }
                int coludId = response.getContactId();
                contact.setCloudId(coludId);
                contact.setLastUpdateTime(lastUpdateTime);
                contact.setSynced(ContactsColumns.SYNC_YES);
                Contact temp = getContactStatus(contact.getCloudId());
                if (temp != null) {
                    contact.setPhotoId(temp.getPhotoId());
                    ArrayList<Account> oldAccounts = contact.getAccounts();
                    ArrayList<Account> resultAccounts = temp.getAccounts();

                    for (Account account : oldAccounts) {
                        Account tmpAccount = ListUtil.getObject(resultAccounts, account,
                                ComparatorFactory.getAccountComparator());
                        if (tmpAccount != null) {
                            account.setContactId(contact.getId());
                            account.setNickName(tmpAccount.getNickName());
                            account.setSkyAccount(tmpAccount.getSkyAccount());
                            account.setSkyId(tmpAccount.getSkyId());
                            account.setOnline(tmpAccount.isOnline());
                            if (account.getSkyId() != 0) {
                                contact.setNickName(tmpAccount.getNickName());
                                if (!TextUtils.isEmpty(contact.getDisplayname())) {
                                    contact.setDisplayname(tmpAccount.getNickName());
                                }
                                contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                                MainApp.i().putUserOnlineStatus(account.getSkyId(),
                                        account.isOnline());
                            }
                        }
                    }
                }
                return contact;
            } else {
                if (response.getResultCode() == -1) {
                    response.setResult(Constants.NET_ERROR, response.getResultHint());
                }
                ResultCode.setCode(response.getResultCode());
                Log.e(TAG,
                        "添加可能认识的好友失败|" + response.getResultCode() + "|" + response.getResultHint());
                return null;
            }
        } else {
            Log.e(TAG, "获取好友SKYID失败");
            return null;
        }
    }

    /**
     * 删除联系人
     * 
     * @param contact
     * @return
     */
    public Contact deleteContact(Contact contact) {
        try {
            contact.setAction(2);
            return uploadContact(contact);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 上传单个联系人
     * 
     * @param contact
     * @return 修改后的联系人
     */
    public Contact uploadContact(Contact contact) {
        try {
            ArrayList<Contact> contactsList = new ArrayList<Contact>();
            contactsList.add(contact);
            TransationContacts tc = uploadContactsByPage(contactsList);
            if (tc.list.isEmpty()) {
                return null;
            }

            contact.setSynced(ContactsColumns.SYNC_YES);
            Contact result = tc.list.get(0);
            if (contact.getAction() != 2) {
                contact.setCloudId(result.getCloudId());
                Contact temp = getContactStatus(contact.getCloudId());
                if (temp != null) {
                    contact.setPhotoId(temp.getPhotoId());
                    ArrayList<Account> oldAccounts = contact.getAccounts();
                    ArrayList<Account> resultAccounts = temp.getAccounts();

                    ArrayList<Account> newAccounts = new ArrayList<Account>();
                    contact.setAccounts(newAccounts);
                    boolean isSkyUser = false;
                    boolean hasMain = false;
                    for (Account account : oldAccounts) {
                        Account tmpAccount = ListUtil.getObject(resultAccounts, account,
                                ComparatorFactory.getAccountComparator());
                        if (tmpAccount != null) {
                            if (!ListUtil.contains(newAccounts, account,
                                    ComparatorFactory.getAccountComparator())) {
                                newAccounts.add(account);
                            } else {
                                continue;
                            }
                            int skyId = tmpAccount.getSkyId();
                            account.setContactId(contact.getId());
                            account.setNickName(tmpAccount.getNickName());
                            account.setSkyAccount(tmpAccount.getSkyAccount());
                            account.setSkyId(skyId);
                            account.setOnline(tmpAccount.isOnline());
                            if (account.getSkyId() > 0) {
                                isSkyUser = true;
                                contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                                MainApp.i().putUserOnlineStatus(skyId,
                                        account.isOnline());
                                if (!hasMain) {
                                    hasMain = true;
                                    account.setMain(1);
                                    Contact cloudContact = getContactBySkyID(skyId);
                                    if (cloudContact != null) {
                                        contact.setBirthday(cloudContact.getBirthday());
                                        contact.setHometown(cloudContact.getHometown());
                                        contact.setOrganization(cloudContact.getOrganization());
                                        contact.setSchool(cloudContact.getSchool());
                                        contact.setSex(cloudContact.getSex());
                                        contact.setSignature(cloudContact.getSignature());
                                    }
                                }
                            }
                        } else {
                            if (!ListUtil.contains(newAccounts, account,
                                    ComparatorFactory.getAccountComparator())) {
                                newAccounts.add(account);
                            }
                        }
                    }
                    if (!isSkyUser) {
                        contact.setUserType(ContactsColumns.USER_TYPE_LOACL);
                    }
                } else {
                    contact.setUserType(ContactsColumns.USER_TYPE_LOACL);
                }
            }
            return contact;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 批量获取指定联系人状态
     * 
     * @param contactSkyids
     * @param contactPhones
     * @param cloudId
     * @return
     */
    public ArrayList<Contact> getSpecifiedContactsStatus(String contactSkyids,
            String contactPhones, int cloudId) {
        ArrayList<Contact> list = new ArrayList<Contact>();
        NetSpecifiedContactsStatusResponse resp = netClient.getBiz().getSpecifiedContactsStatus(
                contactSkyids, contactPhones,
                cloudId);
        if (resp.isSuccess()) {
            ArrayList<ContactsStatusItem> items = resp.getItems();
            if (items != null && items.size() > 0) {
                for (int i = 0; i < items.size(); i++) {
                    ContactsStatusItem item = items.get(i);
                    if (item != null) {
                        Contact contact = new Contact();
                        contact.setCloudId(item.getContactId());
                        boolean hasMain = false;
                        for (ContactsStatus contactStatus : item.getItems()) {
                            Account account = new Account();
                            account.setNickName(contactStatus.getNickname());
                            account.setPhone(contactStatus.getPhone());
                            account.setSkyAccount(contactStatus.getUserName());
                            account.setSkyId(contactStatus.getSkyId());
                            account.setOnline(contactStatus.getStatus() == 0 ? false : true);
                            MainApp.i().putUserOnlineStatus(contactStatus.getSkyId(),
                                    contactStatus.getStatus() == 0 ? false : true);
                            contact.addAccount(account);
                            // 新增签名 2012-09-20
                            contact.setSignature(contactStatus.getUsignature());
                            if (!hasMain) {
                                hasMain = true;
                                String photo = contactStatus.getImageHead();
                                if (ImageUtils.isImageUrl(photo))
                                    contact.setPhotoId(photo);
                            }
                        }
                        list.add(contact);
                    }
                }
            }
        } else {
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            Log.e(TAG, "获取指定联系人状态失败!  " + resp.getResultHint());
        }
        return list;
    }

    /**
     * 获取指定联系人状态
     * 
     * @param cloudId
     * @return
     */
    public Contact getContactStatus(long cloudId) {
        return getStatus(null, null, cloudId);
    }

    private Contact getStatus(String contactSkyids, String contactPhones, long cloudId) {
        Contact contact = null;
        NetSpecifiedContactsStatusResponse resp = netClient.getBiz().getSpecifiedContactsStatus(
                contactSkyids, contactPhones,
                (int) cloudId);
        if (resp.isSuccess()) {
            ArrayList<ContactsStatusItem> items = resp.getItems();
            if (items != null && items.size() > 0) {
                ContactsStatusItem item = items.get(0);
                if (item != null) {
                    contact = new Contact();
                    contact.setCloudId(item.getContactId());
                    boolean hasMain = false;
                    for (ContactsStatus contactStatus : item.getItems()) {
                        Account account = new Account();
                        account.setNickName(contactStatus.getNickname());
                        account.setPhone(contactStatus.getPhone());
                        account.setSkyAccount(contactStatus.getUserName());
                        account.setSkyId(contactStatus.getSkyId());
                        account.setOnline(contactStatus.getStatus() == 0 ? false : true);
                        contact.addAccount(account);
                        // 新增签名 2012-09-20
                        contact.setSignature(contactStatus.getUsignature());
                        if (!hasMain) {
                            hasMain = true;
                            String photo = contactStatus.getImageHead();
                            if (ImageUtils.isImageUrl(photo))
                                contact.setPhotoId(photo);
                        }
                    }
                }
            }
        } else {
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            Log.e(TAG, "获取指定联系人状态失败!  " + resp.getResultHint());
        }
        return contact;
    }

    /**
     * 批量上传联系人
     * 
     * @param contacts
     * @return
     */
    public synchronized ArrayList<Contact> uploadContacts(ArrayList<Contact> contacts) {
        ArrayList<Contact> result = new ArrayList<Contact>();
        int current = 0;
        // 开始上传联系人
        try {
            while (current < contacts.size()) {
                TransationContacts temp = uploadContactsByPage(contacts.subList(
                        current,
                        current = (current + Constants.PAGESIZE >= contacts.size() ? contacts
                                .size() : current + Constants.PAGESIZE)));
                result.addAll(temp.list);
            }
        } catch (NoNeedSendRequestException ne) {
            SLog.e(TAG, "未登录状态,取消继续上传");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 结束上传联系人
        return result;
    }

    /**
     * 获取全部联系人简单信息
     * 
     * @return
     */
    public ArrayList<Contact> getAllContactSimpleInfo() {
        ArrayList<Contact> statusList = new ArrayList<Contact>();
        int current = 1;
        try {
            TransationContacts statusTC = getContactsSimpleInfo(current);
            int totalsize = statusTC.totalSize;
            int totalPage = ((totalsize / Constants.PAGESIZE) + (totalsize % Constants.PAGESIZE > 0 ? 1
                    : 0));
            statusList.addAll(statusTC.list);
            current++;
            while (current <= totalPage) {
                statusList.addAll(getContactsSimpleInfo(current).list);
                current++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusList;
    }

    /**
     * 获取指定联系人简单信息
     * 
     * @param contactId 联系人ID
     * @return
     */
    public ArrayList<Contact> getContactSimpleInfoByContactId(int contactId) {
        ArrayList<Contact> statusList = new ArrayList<Contact>();
        int current = 1;
        try {
            TransationContacts statusTC = getContactsSimpleInfo(current, 1, contactId);
            statusList.addAll(statusTC.list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusList;
    }

    /*
     * 获取全部联系人状态
     * @return
     */
    public void getAllContactStatusList() {
        int current = 1;
        int totalsize = getContactsStatus(current);
        int totalPage = ((totalsize / Constants.PAGESIZE) + (totalsize % Constants.PAGESIZE > 0 ? 1
                : 0));
        current++;
        while (current <= totalPage) {
            getContactsStatus(current);
            current++;
        }
    }

    /**
     * 分页上传联系人
     * 
     * @param contacts
     * @return
     */
    public synchronized TransationContacts uploadContactsByPage(List<Contact> contacts) {
        TransationContacts transationContacts = new TransationContacts();
        ArrayList<Contact> resultList = new ArrayList<Contact>();
        ArrayList<NetContacts> contactsList = new ArrayList<NetContacts>();
        for (Contact contact : contacts) {
            if (contact.getAction() == 0) {
                continue;
            }
            NetContacts netContacts = new NetContacts();
            netContacts.setAction((byte) contact.getAction());
            netContacts.setContactId((int) contact.getCloudId());
            netContacts.setContactName(contact.getDisplayname().trim());
            netContacts.setContactType((byte) contact.getUserType());
            netContacts.setMemo(contact.getNote());
            netContacts.setSequenceId((int) contact.getId());
            ArrayList<NetContactsPhone> phoneList = new ArrayList<NetContactsPhone>();
            if (contact.getAction() != 2) {
                byte i = 0;
                for (Account account : contact.getAccounts()) {
                    int skyId = account.getSkyId();
                    String phone = account.getPhone();
                    phone = StringUtil.removeHeader(phone);
                    NetContactsPhone netContactsPhone = new NetContactsPhone();
                    netContactsPhone.setBuddyId(skyId);
                    netContactsPhone.setPhone(phone);
                    netContactsPhone.setIndex(i++);
                    if (!TextUtils.isEmpty(phone) || skyId > 0) {
                        phoneList.add(netContactsPhone);
                    }
                }
                netContacts.setPhoneList(phoneList);
            }
            if (!phoneList.isEmpty() || contact.getAction() == 2) {
                contactsList.add(netContacts);
            }
        }

        if (!MainApp.isLoggedIn()) {
            throw new NoNeedSendRequestException();
        }
        // 向服务器发起添加联系人请求
        NetOperateContactsResponse netOperateContactsResponse = netClient.getBiz()
                .operateContacts(contactsList);

        // 请求成功
        if (netOperateContactsResponse.isSuccess()) {
            ArrayList<NetContactsResultInfo> resultInfos = netOperateContactsResponse
                    .getResultInfo();
            for (NetContactsResultInfo resultInfo : resultInfos) {
                byte code = resultInfo.getCode();
                if (code == 1) {// 同步成功
                    // 将服务器类型转换为客户端联系人类型
                    int contactId = resultInfo.getSequenceId();
                    int cloudId = resultInfo.getContactId();
                    Contact contact = new Contact();
                    contact.setAction(resultInfo.getAction());
                    contact.setCloudId(cloudId);
                    contact.setId(contactId);
                    contact.setSynced(ContactsColumns.SYNC_YES);
                    resultList.add(contact);
                } else {
                    Log.e(TAG, "操作某一个联系人错误 ,错误码 : " + resultInfo.getCode());
                }
            }
            transationContacts.updateTime = netOperateContactsResponse.getUpdateTime();
            if (transationContacts.updateTime != 0) {
                CommonPreferences.saveContactsLastTimeUpdate(transationContacts.updateTime);
            }
        } else {
            Log.e(TAG,
                    "上传联系人失败  netOperateContactsResponse = "
                            + netOperateContactsResponse.getResultCode());
            if (netOperateContactsResponse.getResultCode() == -1) {
                netOperateContactsResponse.setResult(Constants.NET_ERROR,
                        netOperateContactsResponse.getResultHint());
            }
            ResultCode.setCode(netOperateContactsResponse.getResultCode());
            throw new MessengerException("操作联系人错误 ,错误码 : "
                    + netOperateContactsResponse.getResultHint()
                    + " code : " + netOperateContactsResponse.getResultCode());
        }
        transationContacts.list = resultList;
        return transationContacts;
    }

    /**
     * 获取联系人简单信息
     * 
     * @param page
     * @return
     */
    public TransationContacts getContactsSimpleInfo(int page) {
        TransationContacts transationContacts = new TransationContacts();
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        if (!MainApp.isLoggedIn()) {
            throw new NoNeedSendRequestException();
        }
        NetGetContactsList2Response contactsStauts = netClient.getBiz().getContactsList(page,
                Constants.PAGESIZE, 0, 0);
        if (contactsStauts.isSuccess()) {
            transationContacts.totalSize = contactsStauts.getTotalSize();
            ArrayList<SimpleUserInfoItem> statusList = contactsStauts.getContactsList();
            if (statusList != null) {
                for (SimpleUserInfoItem status : statusList) {
                    Contact contact = new Contact();
                    int cloudId = status.getContactId();
                    contact.setCloudId(cloudId);
                    ArrayList<SimpleUserInfo> items = status.getList();
                    if (items != null) {
                        boolean hasMain = false;
                        for (SimpleUserInfo contactStatus : items) {
                            Account account = new Account();
                            account.setNickName(contactStatus.getNickname());
                            account.setPhone(contactStatus.getPhone());
                            account.setSkyId(contactStatus.getSkyId());
                            account.setSkyAccount(contactStatus.getUserName());
                            contact.addAccount(account);
                            if (account.getSkyId() != 0 && !hasMain) {
                                hasMain = true;
                                account.setMain(1);
                                contact.setPhotoId(contactStatus.getImageHead());
                                contact.setSignature(contactStatus.getUsignature());
                                contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                            }
                        }
                    }
                    contacts.add(contact);
                }
            }
        } else {
            if (contactsStauts.getResultCode() == -1) {
                contactsStauts.setResult(Constants.NET_ERROR, contactsStauts.getResultHint());
            }
            ResultCode.setCode(contactsStauts.getResultCode());
            Log.e(TAG, "分页获取联系人简单信息失败! " + contactsStauts.getResultHint());
        }
        transationContacts.list = contacts;
        return transationContacts;
    }

    /**
     * 获取联系人简单信息
     * 
     * @param page
     * @return
     */
    public TransationContacts getContactsSimpleInfo(int page, int fetch, int contactId) {
        TransationContacts transationContacts = new TransationContacts();
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        if (!MainApp.isLoggedIn()) {
            throw new NoNeedSendRequestException();
        }
        NetGetContactsList2Response contactsStauts = netClient.getBiz().getContactsList(page,
                Constants.PAGESIZE, fetch, contactId);
        if (contactsStauts.isSuccess()) {
            transationContacts.totalSize = contactsStauts.getTotalSize();
            ArrayList<SimpleUserInfoItem> statusList = contactsStauts.getContactsList();
            if (statusList != null && statusList.size() > 0) {
                for (SimpleUserInfoItem status : statusList) {
                    Contact contact = new Contact();
                    int cloudId = status.getContactId();
                    contact.setCloudId(cloudId);
                    ArrayList<SimpleUserInfo> items = status.getList();
                    if (items != null) {
                        boolean hasMain = false;
                        for (SimpleUserInfo contactStatus : items) {
                            Account account = new Account();
                            account.setNickName(contactStatus.getNickname());
                            account.setPhone(contactStatus.getPhone());
                            account.setSkyId(contactStatus.getSkyId());
                            account.setSkyAccount(contactStatus.getUserName());
                            contact.addAccount(account);
                            if (account.getSkyId() != 0 && !hasMain) {
                                hasMain = true;
                                account.setMain(1);
                                contact.setPhotoId(contactStatus.getImageHead());
                                contact.setSignature(contactStatus.getUsignature());
                                contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                            }
                        }
                    }
                    contacts.add(contact);
                }
            }
        } else {
            if (contactsStauts.getResultCode() == -1) {
                contactsStauts.setResult(Constants.NET_ERROR, contactsStauts.getResultHint());
            }
            ResultCode.setCode(contactsStauts.getResultCode());
            Log.e(TAG, "分页获取联系人简单信息失败! " + contactsStauts.getResultHint());
        }
        transationContacts.list = contacts;
        return transationContacts;
    }

    /**
     * 获取联系人状态
     * 
     * @param page
     * @return
     */
    public int getContactsStatus(int page) {
        int totalPage = 0;
        if (!MainApp.isLoggedIn()) {
            return totalPage;
        }
        NetGetContactsStatus2Response contactsStauts = netClient.getBiz().getContactsStatusList(
                page,
                Constants.PAGESIZE, 0, 0);
        if (contactsStauts.isSuccess()) {
            totalPage = contactsStauts.getTotalSize();
            ArrayList<SimpleStatusItem> statusList = contactsStauts.getContactsStatusList();
            if (statusList != null) {
                for (SimpleStatusItem status : statusList) {
                    ArrayList<SimpleStatus> items = status.getList();
                    if (items != null) {
                        for (SimpleStatus contactStatus : items) {
                            MainApp.i().putUserOnlineStatus(
                                    contactStatus.getSkyId(),
                                    contactStatus.getStatus() == 0 ? false : true);
                        }
                    }
                }
            }
        } else {
            if (contactsStauts.getResultCode() == -1) {
                contactsStauts.setResult(Constants.NET_ERROR, contactsStauts.getResultHint());
            }
            ResultCode.setCode(contactsStauts.getResultCode());
            Log.e(TAG, "分页获取联系人状态失败! " + contactsStauts.getResultHint() + " code : "
                    + contactsStauts.getResultCode());
        }
        return totalPage;
    }

    /**
     * 获取指定联系人状态
     * 
     * @param contactId 联系人ID
     * @return
     */
    public int getContactsStatusByContactId(int contactId) {
        int totalPage = 0;
        if (!MainApp.isLoggedIn()) {
            return totalPage;
        }
        NetGetContactsStatus2Response contactsStauts = netClient.getBiz().getContactsStatusList(
                1,
                Constants.PAGESIZE, 1, contactId);
        if (contactsStauts.isSuccess()) {
            totalPage = contactsStauts.getTotalSize();
            ArrayList<SimpleStatusItem> statusList = contactsStauts.getContactsStatusList();
            if (statusList != null) {
                for (SimpleStatusItem status : statusList) {
                    ArrayList<SimpleStatus> items = status.getList();
                    if (items != null) {
                        for (SimpleStatus contactStatus : items) {
                            MainApp.i().putUserOnlineStatus(
                                    contactStatus.getSkyId(),
                                    contactStatus.getStatus() == 0 ? false : true);
                        }
                    }
                }
            }
        } else {
            if (contactsStauts.getResultCode() == -1) {
                contactsStauts.setResult(Constants.NET_ERROR, contactsStauts.getResultHint());
            }
            ResultCode.setCode(contactsStauts.getResultCode());
            Log.e(TAG, "分页获取联系人状态失败! " + contactsStauts.getResultHint() + " code : "
                    + contactsStauts.getResultCode());
        }
        return totalPage;
    }

    /**
     * 添加到黑名单
     * <ul>
     * <li>1、 contactId情况：用户将联系人加为黑名单</li>
     * <li>2、 destSkyid情况：用户将陌生人加为黑名单</li>
     * <li>3、如果contactId和destSkyid同时传入，优先考虑contactId的情况</li>
     * <li>4、判断contactId是否为登录者的联系人（好友），如果是则允许操作，不是则提示客户端</li>
     * <li>5、 加黑名单后发送下线通知给该黑名单用户（如果该用户在线的话）</li>
     * </ul>
     * 
     * @param contact
     * @param skyId
     * @return
     */
    public int addToBlackList(int cloudId, int skyId) {
        int result = NET_ERR;
        if (!MainApp.isLoggedIn()) {
            return result;
        }
        NetBlackResponse resp = netClient.getBiz().addBlackList(cloudId, skyId);
        if (!resp.isSuccess()) {
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            if (resp.isNetError())
                result = NET_ERR;
            else if (resp.isFailed())
                result = NET_FAILED;
            Log.e(TAG, resp.getResultHint());
        } else {
            long lastUpdateTime = resp.getUpdateTime();
            if (lastUpdateTime != 0)
                CommonPreferences.saveContactsLastTimeUpdate(lastUpdateTime);
            result = NET_SUCCESS;
        }
        return result;
    }

    /**
     * 解除黑名单
     * 
     * @param contact
     * @param skyId
     * @return
     */
    public int removeFromBlackList(int cloudId, int skyId) {
        int result = NET_ERR;

        if (!MainApp.isLoggedIn()) {
            return result;
        }
        NetBlackResponse resp = netClient.getBiz().delBlackList(cloudId, skyId);
        if (!resp.isSuccess()) {
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            if (resp.isNetError())
                result = NET_ERR;
            else if (resp.isFailed())
                result = NET_FAILED;
            Log.i(TAG, "解除黑名单失败");
        } else {
            long lastUpdateTime = resp.getUpdateTime();
            if (lastUpdateTime != 0)
                CommonPreferences.saveContactsLastTimeUpdate(lastUpdateTime);
            result = NET_SUCCESS;
        }
        return result;
    }

    /**
     * 获取头像并保存
     * 
     * @param fileName
     * @return
     */
    public String getHeaderImage(final String fileName) {
        String filePath = null;
        String fileUrl = PropertiesUtils.getInstance().getFileURL();
        NetDownloadResponse resp = netClient.getBiz().downloadImage(fileUrl, fileName, 72,
                0);
        if (resp.isSuccess()) {
            filePath = Constants.HEAD_PATH + fileName;
            byte[] body = resp.getBody();
            Bitmap bmp = FileUtils.Bytes2Bitmap(body);
            if (FileUtils.SaveBitmap2File(bmp, filePath)) {
                return filePath;
            } else {
                return null;
            }
        } else {
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
        }
        return filePath;
    }

    /**
     * 获取陌生人黑名单
     * 
     * @return
     */
    public ArrayList<Contact> getStrangerBlacklistContactList() {
        ArrayList<Contact> statusList = new ArrayList<Contact>();
        int current = 1;
        TransationContacts statusTC = getStrangerBlacklistContactList(current);
        int totalsize = statusTC.totalSize;
        int totalPage = ((totalsize / Constants.PAGESIZE) + (totalsize % Constants.PAGESIZE > 0 ? 1
                : 0));
        statusList.addAll(statusTC.list);
        current++;
        while (current <= totalPage) {
            statusList.addAll(getStrangerBlacklistContactList(current).list);
            current++;
        }
        return statusList;
    }

    /**
     * 获取陌生人黑名单
     * 
     * @param start
     * @return
     */
    public TransationContacts getStrangerBlacklistContactList(int start) {
        TransationContacts transationContacts = new TransationContacts();
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        if (!MainApp.isLoggedIn()) {
            throw new NoNeedSendRequestException();
        }
        NetGetBlakListResponse resp = netClient.getBiz().getBlackList(start, Constants.PAGESIZE, 1);
        if (resp.isSuccess()) {
            ArrayList<BlackList> blackList = resp.getBlackList();
            for (BlackList black : blackList) {
                Contact contact = new Contact();
                contact.setBlackList(ContactsColumns.BLACK_LIST_YES);
                contact.setUserType(ContactsColumns.USER_TYPE_STRANGER);
                contact.setSynced(ContactsColumns.SYNC_YES);
                contact.setDisplayname(TextUtils.isEmpty(black.getContactName()) ? black
                        .getNickname() : black.getContactName());
                contact.setCloudId(black.getContactId());
                contact.setSignature(black.getUsignature());
                Account account = new Account();
                account.setSkyId(black.getSkyId());
                account.setNickName(black.getNickname());
                account.setPhone(black.getPhone());
                contact.addAccount(account);
                contacts.add(contact);
            }
            transationContacts.totalSize = resp.getTotalSize();
        } else {
            if (resp.getResultCode() == -1) {
                resp.setResult(Constants.NET_ERROR, resp.getResultHint());
            }
            ResultCode.setCode(resp.getResultCode());
            Log.e(TAG, "getStrangerContactList Error:" + resp.getResultHint());
        }
        transationContacts.list = contacts;
        return transationContacts;
    }

    /**
     * 分页获取时,用作转换
     * 
     * @ClassName: TransationContacts
     * @author Sean.Xie
     * @date 2012-4-13 上午11:39:21
     */
    class TransationContacts {
        ArrayList<Contact> list;
        int totalSize;
        long updateTime;
        boolean isSuccess;
    }

    /**
     * 触发服务端重新计算推荐好友
     * 
     * @param token 登录用户的TOKEN
     * @return boolean 调用是否成功
     */
    public boolean fireReCalsRecommendsFriends(String token) {
        if (null == token || token.equals("")) {
            return false;
        }

        if (!MainApp.isLoggedIn()) {
            return false;
        }
        NetCalcFriendsResponse response = netClient.getBiz().calcFriends(token);
        if (null != response && response.isSuccess()) {
            Log.i(TAG, "触发服务器端计算推荐好友 successful");
            return true;
        }
        if (response.getResultCode() == -1) {
            response.setResult(Constants.NET_ERROR, response.getResultHint());
        }
        ResultCode.setCode(response.getResultCode());
        Log.i(TAG, "触发服务器端计算推荐好友 fail");
        return false;
    }

    public NetGetUserInfoByUserNameResponse searchFriend(String accountName) {
        return netClient.getBiz().getUserInfoByUserName(accountName);
    }

}
