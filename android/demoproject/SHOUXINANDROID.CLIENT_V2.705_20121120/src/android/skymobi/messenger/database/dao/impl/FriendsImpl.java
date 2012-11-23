
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Friend;
import android.skymobi.messenger.database.dao.FriendsDAO;
import android.skymobi.messenger.provider.SocialMessenger.AccountsColumns;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.provider.SocialMessenger.FriendsColumns;
import android.skymobi.messenger.utils.SettingsPreferences;

import java.util.ArrayList;

/**
 * @ClassName: FriendsImpl
 * @Description: 好友数据库操作实现
 * @author Anson.Yang
 * @date 2012-3-4 下午7:51:27
 */
public class FriendsImpl extends ContactsImpl implements FriendsDAO {

    public FriendsImpl(Context context) {
        super(context);
    }

    @Override
    public long addFriend(Friend friend) {
        friend.setDisplayname(friend.getNickName());
        friend.setUserType(ContactsColumns.USER_TYPE_STRANGER);
        friend.setSynced(ContactsColumns.SYNC_YES);
        // 如果已经存在相同的contact,则只需要进行修改
        // long contactId =
        // getContactIdByAccount(friend.getAccounts().get(0).getSkyId());
        // if (contactId > 0) { // 设置contactId
        // friend.setId(contactId);
        // updateContactWithAccounts(friend);
        // } else {
        long contactId = addContact(friend);
        // }
        friend.setContactId(contactId);
        ContentValues values = new ContentValues();
        values.put(FriendsColumns.SKYID, SettingsPreferences.getSKYID());
        values.put(FriendsColumns.RECOMMEND_REASON, friend.getRecommendReason());
        values.put(FriendsColumns.NICKNAME, friend.getNickName());
        values.put(FriendsColumns.CONTACT_TYPE, friend.getContactType());
        values.put(FriendsColumns.DETAIL_REASON, friend.getDetailReason());
        values.put(FriendsColumns.TALK_REASON, friend.getTalkReason());
        values.put(FriendsColumns.CONTACT_ID, contactId);
        return insert(FriendsColumns.TABLE_NAME, null, values);
    }

    @Override
    public void addFriends(ArrayList<Friend> friends) {
        beginTransaction();
        deleteAllFriends();
        for (Friend friend : friends) {
            long id = addFriend(friend);
            friend.setId(id);
        }
        endTransaction(true);
    }

    @Override
    public ArrayList<Friend> getFriends() {
        ArrayList<Friend> friends = new ArrayList<Friend>();
        StringBuilder friendSQL = new StringBuilder();
        friendSQL
                .append("select ")
                .append("f.").append(FriendsColumns._ID).append(" as id,")
                .append("f.").append(FriendsColumns.CONTACT_ID).append(" as contactId,")
                .append("f.").append(FriendsColumns.RECOMMEND_REASON)
                .append(" as recommendReason,")
                .append("f.").append(FriendsColumns.DETAIL_REASON).append(" as detailReason,")
                .append("c.").append(ContactsColumns.SIGNATURE).append(" as signature,")
                .append("f.").append(FriendsColumns.NICKNAME).append(" as nickname,")
                .append("c.").append(ContactsColumns.PHOTO_ID).append(" as photoId,")
                .append("c.").append(ContactsColumns.DISPLAY_NAME).append(" as displayname,")
                .append("c.").append(ContactsColumns.SEX).append(" as sex")
                .append(" from ")
                .append(FriendsColumns.TABLE_NAME + " f ," + ContactsColumns.TABLE_NAME + " c")
                .append(" where f.contact_id=c._id and f.skyid= " + SettingsPreferences.getSKYID())
                .append(" and c." + ContactsColumns.BLACK_LIST + "<>"
                        + ContactsColumns.BLACK_LIST_YES)
                .append(" and (f." + FriendsColumns.CONTACT_TYPE + "="
                        + FriendsColumns.CONTACT_TYPE_YUNYING + " or f."
                        + FriendsColumns.CONTACT_TYPE
                        + "="
                        + FriendsColumns.CONTACT_TYPE_TUIJIAN + ")");
        friends = queryWithSort(Friend.class, friendSQL.toString(), null);
        return friends;
    }

    @Override
    public ArrayList<Friend> getFriendsByIds(String ids) {
        ArrayList<Friend> friends = new ArrayList<Friend>();
        StringBuilder friendSQL = new StringBuilder();
        friendSQL.append("select ")
                .append("f.")
                .append(FriendsColumns._ID).append(" as id,")
                .append(FriendsColumns.CONTACT_ID).append(" as contactId,")
                .append(FriendsColumns.RECOMMEND_REASON).append(" as recommendReason,")
                .append(ContactsColumns.SIGNATURE).append(" as signature,")
                .append(ContactsColumns.SEX).append(" as sex,")
                .append(ContactsColumns.DISPLAY_NAME).append(" as nickname")
                .append(" from ")
                .append(FriendsColumns.TABLE_NAME).append(" as f,")
                .append(ContactsColumns.TABLE_NAME).append(" as c")
                .append(" where f.skyid=? and ")
                .append("f.")
                .append(FriendsColumns.CONTACT_ID).append("=").append("c.")
                .append(ContactsColumns._ID);
        if (ids != null) {
            friendSQL.append(" and f.").append(FriendsColumns._ID).append(" in (").append(ids)
                    .append(")");
        }
        friends = queryWithSort(Friend.class, friendSQL.toString(), new
                String[] {
                    "" + SettingsPreferences.getSKYID()
                });
        return friends;
    }

    private boolean deleteAllFriends() {
        ArrayList<String> contactIds = query("select contact_id from " + FriendsColumns.TABLE_NAME
                + " where "
                + FriendsColumns.CONTACT_TYPE + "="
                + FriendsColumns.CONTACT_TYPE_TUIJIAN + " or " + FriendsColumns.CONTACT_TYPE + "="
                + FriendsColumns.CONTACT_TYPE_YUNYING, null);
        int rows = delete(FriendsColumns.TABLE_NAME, FriendsColumns.CONTACT_TYPE + "="
                + FriendsColumns.CONTACT_TYPE_TUIJIAN + " or " + FriendsColumns.CONTACT_TYPE + "="
                + FriendsColumns.CONTACT_TYPE_YUNYING, null);
        SLog.d(TAG, " contactIds" + contactIds);
        if (null != contactIds && contactIds.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String id : contactIds) {
                sb.append(id + ",");
            }
            String ids = sb.substring(0, sb.length() - 1);
            delete(ContactsColumns.TABLE_NAME, "_id in (" + ids + ")", null);
        }
        return rows > 0;
    }

    @Override
    public boolean deleteFriend(long id) {
        int rows = delete(FriendsColumns.TABLE_NAME, FriendsColumns._ID + "=" + id, null);
        return rows > 0;
    }

    @Override
    public Friend getFriendById(long id) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append(FriendsColumns._ID).append(" as id, ")
                .append(FriendsColumns.CONTACT_TYPE).append(" as contactType, ")
                .append(FriendsColumns.CONTACT_ID).append(" as contactId, ")
                .append(FriendsColumns.TALK_REASON).append(" as talkReason,")
                .append(FriendsColumns.DETAIL_REASON).append(" as detailReason")
                .append(" from ")
                .append(FriendsColumns.TABLE_NAME)
                .append(" where ")
                .append(FriendsColumns._ID).append(" =? ");
        Friend friend = queryForObject(Friend.class, sql.toString(), new String[] {
                String.valueOf(id)
        });
        if (null != friend) {
            Contact contact = getContactById(friend.getContactId());
            friend.setAccounts(contact.getAccounts());
            friend.setBirthday(contact.getBirthday());
            friend.setDisplayname(contact.getDisplayname());
            friend.setHometown(contact.getHometown());
            friend.setOrganization(contact.getOrganization());
            friend.setPhotoId(contact.getPhotoId());
            friend.setSchool(contact.getSchool());
            friend.setSex(contact.getSex());
            friend.setSignature(contact.getSignature());
            friend.setUserType(contact.getUserType());
        }
        return friend;
    }

    @Override
    public int addContactFromFriend(long friendId, long contactId) {
        int result = -1;
        if (contactId == 0 && friendId > 0) {
            Friend friend = getFriendById(friendId);
            if (null != friend)
                contactId = getFriendById(friendId).getContactId();
        }
        // 判断好友表ID是否大于0
        if (friendId > 0) {
            result = delete(FriendsColumns.TABLE_NAME, "_id=" + friendId, null);
        }
        if (result > 0) {
            ContentValues values = new ContentValues();
            values.put(ContactsColumns.USER_TYPE, ContactsColumns.USER_TYPE_SHOUXIN);
            result = updateContactWithValues(values, ContactsColumns._ID + "=?", new String[] {
                    String.valueOf(contactId)
            });
        }
        return result;
    }

    @Override
    public int addContactFromFriendMsg(long friendId, long contactId) {
        int result = -1;
        if (contactId == 0) {
            contactId = getFriendById(friendId).getContactId();
        }
        ContentValues values = new ContentValues();
        values.put(ContactsColumns.USER_TYPE, ContactsColumns.USER_TYPE_SHOUXIN);
        result = updateContactWithValues(values, ContactsColumns._ID + "=?", new String[] {
                String.valueOf(contactId)
        });

        return result;
    }

    @Override
    public void addStrangerToBlackList(long contactId) {
        ContentValues values = new ContentValues();
        values.put(ContactsColumns.BLACK_LIST, ContactsColumns.BLACK_LIST_YES);
        updateContactWithValues(values, ContactsColumns._ID + "=?", new String[] {
                String.valueOf(contactId)
            });
    }

    @Override
    public void deleteFriendAndContactWithTransaction(long friendId) {
        Friend friend = getFriendById(friendId);
        beginTransaction();
        if (null != friend && friend.getContactId() > 0) {
            deleteFriend(friendId);
            if (friend.getUserType() == ContactsColumns.USER_TYPE_STRANGER) {
                // 如果还未添加为联系人,则删除contact中对应的数据
                deleteContactForSync(friend.getContactId());
            }
        }
        endTransaction(true);
    }

    @Override
    public long getFriendIdByContactId(long contactId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append(FriendsColumns._ID).append(" as id")
                .append(" from ")
                .append(FriendsColumns.TABLE_NAME)
                .append(" where ")
                .append(FriendsColumns.CONTACT_ID).append(" =? ");
        Friend friend = queryForObject(Friend.class, sql.toString(), new String[] {
                "" + contactId
        });
        if (null != friend) {
            return friend.getId();
        }
        return 0;
    }

    @Override
    public long getFriendIdByAccount(int skyid, String nickname, String phone) {
        StringBuilder sql = new StringBuilder();
        sql.append("select a.")
                .append(FriendsColumns._ID).append(" as id")
                .append(" from ")
                .append(FriendsColumns.TABLE_NAME).append(" as a,")
                .append(AccountsColumns.TABLE_NAME).append(" as b,")
                .append(ContactsColumns.TABLE_NAME).append(" as c")
                .append(" where ")
                .append("a.").append(FriendsColumns.CONTACT_ID).append("=")
                .append("c.").append(ContactsColumns._ID)
                .append(" and c.").append(ContactsColumns._ID).append("=")
                .append("b.").append(AccountsColumns.CONTACT_ID);
        Friend friend = null;
        if (skyid > 0 && null != nickname) {
            sql.append(" and b.").append(AccountsColumns.SKYID).append(" =? ")
                    .append(" and b.").append(AccountsColumns.SKY_NICKNAME).append(" =? ");
            friend = queryForObject(Friend.class, sql.toString(), new String[] {
                    "" + skyid, nickname
            });
        } else if (null != nickname && null != phone) {
            sql.append(" and b.").append(AccountsColumns.PHONE).append(" =? ")
                    .append(" and b.").append(AccountsColumns.SKY_NICKNAME).append(" =? ");
            friend = queryForObject(Friend.class, sql.toString(), new String[] {
                    phone, nickname
            });
        }

        if (null != friend) {
            return friend.getId();
        }
        return 0;
    }

    @Override
    public long getContactIdByAccount(int skyid) {
        StringBuilder sql = new StringBuilder();
        sql.append("select a.")
                .append(FriendsColumns.CONTACT_ID).append(" as contactId")
                .append(" from ")
                .append(FriendsColumns.TABLE_NAME).append(" as a,")
                .append(AccountsColumns.TABLE_NAME).append(" as b,")
                .append(ContactsColumns.TABLE_NAME).append(" as c")
                .append(" where ")
                .append("a.").append(FriendsColumns.CONTACT_ID).append("=")
                .append("c.").append(ContactsColumns._ID)
                .append(" and c.").append(ContactsColumns._ID).append("=")
                .append("b.").append(AccountsColumns.CONTACT_ID);
        Friend friend = null;
        if (skyid > 0) {
            sql.append(" and b.").append(AccountsColumns.SKYID).append(" =? ");
            friend = queryForObject(Friend.class, sql.toString(), new String[] {
                    "" + skyid
            });
            if (null != friend) {
                return friend.getContactId();
            }
        }

        return 0;
    }

    @Override
    public boolean checkFriendExistByContactId(long contactId) {
        int count = queryCount(FriendsColumns.TABLE_NAME, FriendsColumns.CONTACT_ID + "="
                + contactId);
        return count > 0 ? true : false;
    }
}
