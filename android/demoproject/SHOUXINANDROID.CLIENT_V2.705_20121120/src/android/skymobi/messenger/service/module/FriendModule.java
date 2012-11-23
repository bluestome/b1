
package android.skymobi.messenger.service.module;

import android.content.ContentValues;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Friend;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.database.dao.ContactsDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.database.dao.FriendsDAO;
import android.skymobi.messenger.database.dao.StrangerDAO;
import android.skymobi.messenger.network.NetWorkMgr;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ResultCode;
import android.text.format.DateUtils;
import android.util.Log;

import com.skymobi.android.sx.codec.beans.clientbean.NetGetUserInfoByUserNameResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;

import java.util.ArrayList;

/**
 * @ClassName: FriendModule
 * @Description: 推荐好友组件friend id
 * @author Anson.Yang
 * @date 2012-2-29 上午9:50:37
 */
public class FriendModule extends BaseModule {

    private static final String TAG = "FriendModule";
    private ContactsNetModule contactsNetModule = null;
    private FriendsDAO friendDao = null;
    private ContactsDAO contactsDAO = null;
    private StrangerDAO strangerDAO = null;

    public final static int IS_SHOUXIN = 1;

    public FriendModule(CoreService service) {
        super(service);
        contactsNetModule = netWorkMgr.getContactsNetModule();
        DaoFactory factory = DaoFactory.getInstance(MainApp.i().getApplicationContext());
        friendDao = factory.getFriendsDAO();
        contactsDAO = factory.getContactsDAO();
        strangerDAO = factory.getStrangerDAO();

    }

    /**
     * 检查skyid是否在陌生人表中存在
     * 
     * @param skyid
     * @return true: 存在 false: 不存在
     */
    public boolean checkStrangerExists(int skyid) {
        return strangerDAO.checkExistsSkyid(skyid);
    }

    /**
     * 获取推荐好友列表
     * 
     * @param count
     */
    public void getFriends(final long updateTime) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Friend> friends = getFriends(Constants.START, Constants.FRIEND_PAGESIZE,
                        updateTime);
                service.notifyObservers(CoreServiceMSG.MSG_FRIENDS_GET_LIST, friends);
            }
        });
    }

    /**
     * @param start
     * @param pagesize
     * @param updateTime
     * @return
     */
    protected ArrayList<Friend> getFriends(int start, int pageSize, long updateTime) {
        ArrayList<Friend> list = contactsNetModule.getFriends(start, pageSize, updateTime);
        ArrayList<Friend> friends = mergeWithContact(list);
        if (null == friends || friends.isEmpty()) {
            SLog.d(TAG, "network list is empty ");
            friends = friendDao.getFriends();
            SLog.d(TAG, "local list is empty " + friends);
        } else {
            friendDao.addFriends(friends);
            SLog.d(TAG, "list = " + friends);
        }
        ContactListCache.getInstance().recreatePhotosForSMS(
                friendDao.getContactInfoForPhoto());
        return friends;
    }

    /**
     * 异步获取找好友列表
     * 
     * @return
     */
    public void getFriends() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Friend> friends = friendDao.getFriends();
                if (null != friends)
                    service.notifyObservers(CoreServiceMSG.MSG_FRIENDS_GET_FROMDB_LIST, friends);
            }
        });
    }

    /**
     * 同步获取找好友列表
     */
    public ArrayList<Friend> getFriendsList() {
        return friendDao.getFriends();
    }

    /**
     * 获取推荐好友详细信息
     * 
     * @param skyid
     */
    public void getFriendInfo(final long contactId, final int skyId) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "send getFriendInfo by skyid:" + skyId);
                Contact contact = contactsNetModule.getContactBySkyID(skyId);
                if (contact != null) {
                    contact.setUserType(ContactsColumns.USER_TYPE_STRANGER);
                    contact.setDisplayname(contact.getAccounts().get(0).getNickName());
                    MainApp.i().putUserOnlineStatus(skyId,
                            contact.getAccounts().get(0).isOnline());
                    contact.setId(contactId);
                    if (contact.getId() > 0) {
                        contactsDAO.updateContactWithAccounts(contact);
                    }
                    ContactListCache.getInstance().recreateFriend(contact);
                    service.notifyObservers(CoreServiceMSG.MSG_FRIENDS_DETAIL_SUCCESS, contact);
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_FRIENDS_DETAIL_FAIL, skyId);
                    Log.i(TAG, "getFriendInfo error skyid:" + skyId);
                }
            }
        });
    }

    /**
     * 获取陌生人详情
     * 
     * @param skyID
     */
    public void getStrangerDetailInfo(final int skyID) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "send getStrangerDetailInfo by skyid:" + skyID);
                Contact contact = contactsNetModule.getContactBySkyID(skyID);
                if (null != contact) {
                    contact.setSkyid(skyID);
                    if (contact.getAccounts().size() > 0) {
                        Account ac = contact.getAccounts().get(0);
                        if (null != ac) {
                            String nickName = ac.getNickName();
                            String skyName = ac.getSkyAccount();
                            // 判断是否已经存在该陌生人
                            if (!strangerDAO.checkExistsSkyid(skyID)) {
                                strangerDAO.addStranger(contact, nickName, skyName);
                            }
                        }
                    }
                    service.notifyObservers(CoreServiceMSG.MSG_FRIENDS_DETAIL_SUCCESS, contact);
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_FRIENDS_DETAIL_FAIL, skyID);
                }
            }
        });
    }

    /**
     * 添加陌生人到黑名单
     * 
     * @param skyId
     * @return
     */
    public void addStrangerToBlackList(final Contact contact) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Account> accounts = contact.getAccounts();
                int skyId = 0;
                for (Account account : accounts) {
                    if (account.getSkyId() > 0) {
                        skyId = account.getSkyId();
                        break;
                    }
                }
                int result = netWorkMgr.getContactsNetModule().addToBlackList(
                        (int) contact.getCloudId(),
                        skyId);
                if (result == ContactsNetModule.NET_SUCCESS) {
                    contact.setBlackList(ContactsColumns.BLACK_LIST_YES);
                    contact.setUserType(ContactsColumns.USER_TYPE_LBS_STRANGER);
                    friendDao.addContact(contact);
                }
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_BLACKLIST, result);
            }
        });
    }

    /**
     * 找朋友中添加黑名单
     * 
     * @param friend
     */
    public void addFriendToBlackList(final Friend friend) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Account> accounts = friend.getAccounts();
                int skyId = 0;
                for (Account account : accounts) {
                    if (account.getSkyId() > 0) {
                        skyId = account.getSkyId();
                        break;
                    }
                }
                int result = netWorkMgr.getContactsNetModule().addToBlackList(
                        (int) friend.getCloudId(), skyId);
                if (result == ContactsNetModule.NET_SUCCESS) {
                    friendDao.addStrangerToBlackList(friend.getContactId());
                }
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_BLACKLIST, result);
            }
        });
    }

    /**
     * 根据ID获取好友信息
     * 
     * @param id
     * @return
     */
    public Friend getFriendById(long id) {
        return friendDao.getFriendById(id);
    }

    /**
     * 将好友添加为联系人
     * 
     * @param friendId
     * @return
     */
    public int addContactFromFriend(Friend friend) {
        return friendDao.addContactFromFriend(friend.getId(), friend.getContactId());
    }

    /**
     * 将好友添加为联系人
     * 
     * @param friendId
     * @return
     */
    public int addContactFromFriend(long friendId, long contactId) {
        return friendDao.addContactFromFriend(friendId, contactId);
    }

    /**
     * 根据ids 查询好友
     * 
     * @param ids id用','隔开 ,例 1,2,3
     * @return
     */
    public ArrayList<Friend> getFriendsByIds(String ids) {
        return friendDao.getFriendsByIds(ids);
    }

    /**
     * 根据contactId获取FriendId
     * 
     * @param contactId
     * @return
     */
    public long getFriendIdByContactId(long contactId) {
        return friendDao.getFriendIdByContactId(contactId);
    }

    /**
     * 添加好友
     * 
     * @param friend
     * @return
     */
    public long addFriend(Friend friend) {
        return friendDao.addFriend(friend);
    }

    /**
     * 判断account是否已经存在(返回的friendId > 0 已经存在)
     * 
     * @param skyid
     * @param nickname
     * @param phone
     * @return
     */
    public long getFriendIdByAccount(int skyid, String nickname, String phone) {
        return friendDao.getFriendIdByAccount(skyid, nickname, phone);
    }

    /**
     * 从推荐好友消息中添加好友
     * 
     * @param friendId
     * @param contactId
     * @return
     */
    public int addContactFromFriendMsg(long friendId, long contactId) {
        return friendDao.addContactFromFriendMsg(friendId, contactId);
    }

    /**
     * 删除推荐好友
     * 
     * @param id
     * @return
     */
    public void deteleFriendById(long id) {
        friendDao.deleteFriendAndContactWithTransaction(id);
    }

    /**
     * 添加推荐联系人为好友
     * 
     * @param friendId
     */
    public void addContactByFriend(final long friendId) {
        FriendsDAO friendsDAO = DaoFactory.getInstance(MainApp.i().getApplicationContext())
                .getFriendsDAO();
        Friend friend = friendsDAO.getFriendById(friendId);
        if (null != friend && null != friend.getAccounts()
                && friend.getAccounts().get(0).getSkyId() > 0) {
            // 如果该推荐联系人已经在本地,则提示好友添加失败
            boolean result = ContactListCache.getInstance().isInContactsList(
                    friend.getAccounts().get(0).getSkyId());
            if (result) { // 已经存在
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_IS_INCONTACT, null);
            } else {
                friend.setUserType(ContactsColumns.USER_TYPE_STRANGER);
                addFriendToCloud(friend, CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS);
            }
        }
    }

    /**
     * 将可能认识的人添加为好友
     * 
     * @param contact
     */
    public void addFriendToCloud(final Friend contact, final int MSG_CODE) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 将好友添加到云端
                // int contactType = contact.getContactType();
                Contact cloudContact = contactsNetModule.addFriends(contact);
                if (cloudContact != null) {
                    // 只有当添加的推荐联系人成功后，才会执行添加好友到本地联系人库。
                    // 区分是是否为推荐消息
                    int result = -1;
                    // if (contactType == FriendsColumns.CONTACT_TYPE_TUIJIAN
                    // || contactType == FriendsColumns.CONTACT_TYPE_YUNYING) {
                    result = service.getFriendModule()
                            .addContactFromFriend(contact.getId(), 0L);
                    // } else {
                    // result =
                    // service.getFriendModule().addContactFromFriendMsg(contact.getId(),
                    // 0);
                    // }

                    if (result > 0) {
                        // 操作Contacts表是需要指定ID
                        cloudContact.setId(contact.getContactId());
                        // 更新联系人信息

                        ContentValues values = new ContentValues();
                        values.put(ContactsColumns.CLOUD_ID, cloudContact.getCloudId());
                        values.put(ContactsColumns.SYNCED, cloudContact.getSynced());
                        contactsDAO.updateContactWithValues(values, ContactsColumns._ID + "=?",
                                new String[] {
                                String.valueOf(contact.getContactId())
                        });
                    } else {
                        contactsDAO.deleteContact(result, true);
                    }
                    service.notifyObservers(MSG_CODE, cloudContact);
                } else {
                    service.notifyObservers(MSG_CODE, null);
                }
            }
        });
    }

    /**
     * 将陌生人添加为好友
     * 
     * @param contact
     */
    public void addFriendToCloud(final Contact contact, final byte contactType) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 首先添加陌生人到本地联系人表中
                // contact.setLocalContactId(-1); localContactId 目前不需要设置
                SLog.d(TAG, "add friend,contactType: " + contactType);
                long result = contactsDAO.addContact(contact);
                if (result > 0) {
                    // 将联系人添加到云端
                    Contact cloudContact = contactsNetModule.addFriends(contact, contactType);
                    if (cloudContact != null) {
                        // 设置本地联系人ID
                        contact.setId(result);
                        // 更新联系人信息
                        ContentValues values = new ContentValues();
                        values.put(ContactsColumns.CLOUD_ID, cloudContact.getCloudId());
                        values.put(ContactsColumns.SYNCED, cloudContact.getSynced());
                        contactsDAO.updateContactWithValues(values, ContactsColumns._ID + "=?",
                                new String[] {
                                String.valueOf(result)
                        });
                        if (null != contact.getAccounts() && contact.getAccounts().size() > 0) {
                            Account ac = contact.getAccounts().get(0);
                            if (null != ac) {
                                boolean b = strangerDAO.delete(ac.getSkyId());
                                SLog.d(TAG, "\tzhang:删除陌生人数据结果:" + b);
                            }
                        }
                        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS,
                                cloudContact);
                    } else {
                        // 因为添加到云端失败,所以回滚数据，标记刚才添加的联系人为已删除
                        contactsDAO.deleteContact(result, true);
                        // 添加联系人失败
                        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS,
                                null);
                    }
                } else {
                    // 添加联系人失败
                    service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS,
                            null);
                }
            }
        });
    }

    /**
     * 将陌生人添加为好友
     * 
     * @param skyid
     */
    public void addFriendToCloud(final int skyid, final byte contactType) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Contact contact = NetWorkMgr.getInstance().getContactsNetModule()
                        .getContactBySkyID(skyid);
                // 网络错误会导致返回的对象为空
                if (null != contact) {
                    contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                    contact.setDisplayname(contact.getNickName());
                    addFriendToCloud(contact, contactType);
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS,
                            null);
                }
            }
        });
    }

    // 触发服务端重新计算推荐好友
    public void fireReCalsRecommendsFriends() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                long lastTime = CommonPreferences.getRecalcFindFriendsTime();
                if (System.currentTimeMillis() - lastTime < DateUtils.DAY_IN_MILLIS) {
                    return;
                }
                UserInfo userinfo = CommonPreferences.getUserInfo();
                String token = (userinfo != null) ? userinfo.token : null;
                boolean bSuccess = contactsNetModule.fireReCalsRecommendsFriends(token);
                if (bSuccess) {
                    CommonPreferences.setRecalcFindFriendsTime(System.currentTimeMillis());
                }
            }
        });
    }

    public void searchFriend(final String accountName) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                NetGetUserInfoByUserNameResponse response = null;
                if (!MainApp.isLoggedIn()) {
                    service.notifyObservers(CoreServiceMSG.MSG_SEARCH_FRIEND_NET_ERROR, null);
                    SLog.w(TAG, "精确查找用户时,当前用户未登录!");
                    return;
                } else {
                    response = contactsNetModule.searchFriend(accountName);
                }
                if (response == null) {
                    service.notifyObservers(CoreServiceMSG.MSG_SEARCH_FRIEND_NET_ERROR, null);
                    SLog.w(TAG, "精确查找用户时,响应为null!");
                    return;
                }
                Log.i(TAG, "response code" + response.getResultCode());
                if (response.isNetError()) {
                    Log.i(TAG, "find shouxin net error");
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    ResultCode.setCode(response.getResultCode());
                    service.notifyObservers(CoreServiceMSG.MSG_SEARCH_FRIEND_NET_ERROR, null);
                } else if (null != response && response.isSuccess()) {
                    Log.i(TAG,
                            "response finded" + response.isFinded() + ", userType: "
                                    + response.getUserType());
                    if (response.isFinded() && response.getUserType() == IS_SHOUXIN) {
                        Log.i(TAG, "finded shouxin user");
                        NetUserInfo userInfo = response.getUserInfo();
                        Contact contact = contactsNetModule.getContact(userInfo, 0);
                        service.notifyObservers(CoreServiceMSG.MSG_SEARCH_FRIEND_SUCCESS, contact);
                    } else {
                        Log.i(TAG, "not finded shouxin user or is not shouxin user");
                        if (response.getResultCode() == -1) {
                            response.setResult(Constants.NET_ERROR, response.getResultHint());
                        }
                        ResultCode.setCode(response.getResultCode());
                        service.notifyObservers(CoreServiceMSG.MSG_SEARCH_FRIEND_NOTFOUND, null);
                    }
                } else {
                    Log.i(TAG, "find shouxin user error");
                    if (response.getResultCode() == -1) {
                        response.setResult(Constants.NET_ERROR, response.getResultHint());
                    }
                    ResultCode.setCode(response.getResultCode());
                    service.notifyObservers(CoreServiceMSG.MSG_SEARCH_FRIEND_FAIL, null);
                }
            }
        });
    }

    /**
     * @param list
     */
    private ArrayList<Friend> mergeWithContact(ArrayList<Friend> list) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        ArrayList<Friend> friends = new ArrayList<Friend>();
        for (int i = 0; i < list.size(); i++) {
            Friend friend = list.get(i);
            if (null != friend && friend.getAccounts().size() == 1) {
                int skyid = friend.getAccounts().get(0).getSkyId();
                boolean result = ContactListCache.getInstance().isInContactsList(skyid);
                if (!result) {
                    friends.add(friend);
                }
            }
        }
        return friends;
    }
}
