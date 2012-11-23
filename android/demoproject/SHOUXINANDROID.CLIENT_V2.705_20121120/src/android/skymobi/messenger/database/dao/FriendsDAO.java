
package android.skymobi.messenger.database.dao;

import android.skymobi.messenger.bean.Friend;

import java.util.ArrayList;

/**
 * @ClassName: FriendsDAO
 * @Description: 好友相关数据库操作
 * @author Anson.Yang
 * @date 2012-3-4 下午7:46:28
 */
public interface FriendsDAO extends ContactsDAO {
    /**
     * 添加好友
     * 
     * @param friend
     * @return
     */
    long addFriend(Friend friend);

    /**
     * 批量添加好友
     * 
     * @param frdList
     * @return
     */
    void addFriends(ArrayList<Friend> friends);

    /**
     * 获取全部推荐好友
     * 
     * @return
     */
    ArrayList<Friend> getFriends();

    /**
     * 根据skyid获取联系人详细信息
     * 
     * @param skyid
     * @return
     */
    public Friend getFriendById(long id);

    /**
     * 将推荐好友添加为联系人
     * 
     * @param friendId
     * @param contactId
     * @return
     */
    public int addContactFromFriend(long friendId, long contactId);

    /**
     * 将推荐好友加入黑名单
     * 
     * @param friendId
     */
    void addStrangerToBlackList(long contactId);

    /**
     * 删除好友以及对应的联系人(未添加为联系人)
     * 
     * @param friendId
     */
    void deleteFriendAndContactWithTransaction(long friendId);

    /**
     * 根据ids 查询好友
     * 
     * @param ids id用','隔开 ,例 1,2,3
     * @return
     */
    ArrayList<Friend> getFriendsByIds(String ids);

    /**
     * 根据contactId获取FriendId
     * 
     * @param contactId
     * @return
     */
    long getFriendIdByContactId(long contactId);

    /**
     * 判断account是否已经存在(返回的friendId > 0 已经存在)
     * 
     * @param skyid
     * @param nickname
     * @param phone
     * @return
     */
    long getFriendIdByAccount(int skyid, String nickname, String phone);

    /**
     * 添加推荐消息过来的好友
     * 
     * @param friendId
     * @return
     */
    int addContactFromFriendMsg(long friendId, long contactId);

    /**
     * 好友列表中,获取是否已经的contact
     */
    long getContactIdByAccount(int skyid);

    boolean deleteFriend(long id);

    /**
     * 根据联系人ID查找Friend是否存在
     * @param contactId
     */
    public boolean checkFriendExistByContactId(long contactId);
}
