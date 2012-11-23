
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.comparator.ComparatorFactory;
import android.skymobi.messenger.database.dao.ContactsDAO;
import android.skymobi.messenger.database.observer.ContactsObserver;
import android.skymobi.messenger.exception.MessengerException;
import android.skymobi.messenger.provider.SocialMessenger.AccountsColumns;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.utils.ListUtil;
import android.skymobi.messenger.utils.PinYinUtil;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.skymobi.android.sx.codec.beans.clientbean.NetVCardNotify;
import com.skymobi.android.sx.codec.beans.common.VCardContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @ClassName: ContactsImpl
 * @Description: 联系人数据库操作
 * @author Sean.Xie
 * @date 2012-2-20 下午1:53:08
 */
public class ContactsImpl extends BaseImpl implements ContactsDAO {

    public ContactsImpl(Context context) {
        super(context);
    }

    @Override
    public ArrayList<Contact> getContactsBlackList(boolean withAccounts) {
        return getContactInfoForBlackList(withAccounts);
    }

    @Override
    public ArrayList<Contact> getContactsListForSync() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append("c.").append(ContactsColumns._ID).append(" as id, ")
                .append("c.").append(ContactsColumns.DISPLAY_NAME).append(" as displayname,")
                .append("c.").append(ContactsColumns.CLOUD_ID).append(" as cloudId,")
                .append("c.").append(ContactsColumns.SYNCED).append(" as synced,")
                .append("c.").append(ContactsColumns.LOCAL_CONTACT_ID)
                .append(" as localContactId,")
                .append("c.").append(ContactsColumns.USER_TYPE).append(" as userType,")
                .append("c.").append(ContactsColumns.DELETED).append(" as deleted,")
                .append("a.").append(AccountsColumns.SKY_NICKNAME).append(" as nickName,")
                .append("a.").append(AccountsColumns.PHONE).append(" as phone,")
                .append("a.").append(AccountsColumns.SKY_NAME).append(" as username,")
                .append("a.").append(AccountsColumns.SKYID).append(" as skyid,")
                .append("a.").append(AccountsColumns._ID).append(" as accountid,")
                .append("a.").append(AccountsColumns.CONTACT_ID).append(" as contactId,")
                .append("a.").append(AccountsColumns.IS_MAIN).append(" as ismain")
                .append(" from ")
                .append(ContactsColumns.TABLE_NAME).append(" c,")
                .append(AccountsColumns.TABLE_NAME).append(" a ")
                .append(" where ")
                .append(" c._id = a.contact_id ")
                .append(" and c.").append(ContactsColumns.SKYID).append("=")
                .append(SettingsPreferences.getSKYID())
                .append(" and c.").append(ContactsColumns.USER_TYPE).append("<>")
                .append(ContactsColumns.USER_TYPE_STRANGER);

        Cursor cursor = getSQLiteDatabase().rawQuery(sql.toString(), null);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String displayName = cursor.getString(1);
                long cloudId = cursor.getLong(2);
                int synced = cursor.getInt(3);
                long localContactId = cursor.getLong(4);
                int userType = cursor.getInt(5);
                int deleted = cursor.getInt(6);
                String nickName = cursor.getString(7);
                String phone = cursor.getString(8);
                phone = StringUtil.removeHeader(phone);
                String username = cursor.getString(9);
                int skyid = cursor.getInt(10);
                long accountId = cursor.getLong(11);
                long contactId = cursor.getLong(12);
                int ismain = cursor.getInt(13);
                Contact contact = new Contact();
                contact.setId(id);
                contact = ListUtil.getObject(contacts, contact,
                        ComparatorFactory.getShouxinComparator());
                if (contact != null) {
                    Account account = new Account();
                    account.setId(accountId);
                    account.setContactId(contactId);
                    account.setPhone(phone);
                    account.setMain(ismain);
                    account.setNickName(nickName);
                    account.setSkyAccount(username);
                    account.setSkyId(skyid);
                    contact.addAccount(account);
                } else {
                    contact = new Contact();
                    contact.setId(id);
                    contact.setDisplayname(displayName);
                    contact.setCloudId(cloudId);
                    contact.setDeleted(deleted);
                    contact.setLocalContactId(localContactId);
                    contact.setUserType(userType);
                    Account account = new Account();
                    account.setId(accountId);
                    account.setContactId(contactId);
                    account.setPhone(phone);
                    contact.setSynced(synced);
                    account.setMain(ismain);
                    account.setNickName(nickName);
                    account.setSkyAccount(username);
                    account.setSkyId(skyid);
                    contact.addAccount(account);
                    contacts.add(contact);
                }
            }
            cursor.close();
        }
        return contacts;
    }

    @Override
    public HashMap<String, Contact> getSimContactList() {
        HashMap<String, Contact> map = new HashMap<String, Contact>();
        if (1 == 1)
            return map;
        Uri uri = Uri.parse("content://icc/adn");
        String[] projection = new String[] {
                "name", "phone"
        };
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        if (count == 0) {
            uri = Uri.parse("content://sim/adn");
            cursor = resolver.query(uri, projection, null, null, null);
        }
        while (cursor != null && cursor.moveToNext()) {
            String name = cursor.getString(0);
            String phone = cursor.getString(1);
            Contact contact = map.get(name);
            Account account = new Account();
            account.setPhone(phone);
            if (contact == null) {
                contact = new Contact();
                contact.setDisplayname(name);
                contact.addAccount(account);
                map.put(name, contact);
            } else {
                contact.addAccount(account);
            }
        }
        return map;
    }

    @Override
    public Contact getContactById(long id) {
        Contact contact = null;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select ").append(ContactsColumns._ID).append(" as id, ")
                    .append(ContactsColumns.DISPLAY_NAME).append(" as displayname,")
                    .append(ContactsColumns.SIGNATURE).append(" as signature,")
                    .append(ContactsColumns.BIRTHDAY).append(" as birthday,")
                    .append(ContactsColumns.CLOUD_ID).append(" as cloudId,")
                    .append(ContactsColumns.HOMETOWN).append(" as hometown,")
                    .append(ContactsColumns.LOCAL_CONTACT_ID).append(" as localContactId,")
                    .append(ContactsColumns.NOTE).append(" as note,")
                    .append(ContactsColumns.ORGANIZATION).append(" as organization,")
                    .append(ContactsColumns.PHOTO_ID).append(" as photoId,")
                    .append(ContactsColumns.SCHOOL).append(" as school,")
                    .append(ContactsColumns.SEX).append(" as sex,")
                    .append(ContactsColumns.SIGNATURE).append(" as signature,")
                    .append(ContactsColumns.PINYIN).append(" as pinyin,")
                    .append(ContactsColumns.USER_TYPE).append(" as userType,")
                    .append(ContactsColumns.BLACK_LIST).append(" as blackList,")
                    .append(ContactsColumns.DELETED).append(" as deleted")
                    .append(" from ")
                    .append(ContactsColumns.TABLE_NAME)
                    .append(" where ")
                    .append(ContactsColumns._ID).append("=?");

            contact = queryForObject(Contact.class, sql.toString(), new String[] {
                    id + ""
            });
            if (null != contact) {
                contact.setAccounts(getAccountByContactId(id));
            }
            // 从account表中查询出来的人,不一定全部都是手信好友,不能直接通过skyid来判断
            // if (contact != null) {
            // ArrayList<Account> accounts = getAccountByContactId(id);
            // contact.setAccounts(getAccountByContactId(id));
            // for (Account a : accounts) {
            // if (a.getSkyId() > 0) {
            // contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
            // break;
            // }
            // }
            // }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MessengerException(e);
        }
        return contact;
    }

    @Override
    public ArrayList<Contact> getContactByAccountIds(String ids) {
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select ")
                    .append("c.").append(ContactsColumns._ID).append(" as id, ")
                    .append("c.").append(ContactsColumns.DISPLAY_NAME).append(" as displayname,")
                    .append("c.").append(ContactsColumns.CLOUD_ID).append(" as cloudId,")
                    .append("c.").append(ContactsColumns.BLACK_LIST).append(" as blackList,")
                    .append("c.").append(ContactsColumns.LOCAL_CONTACT_ID)
                    .append(" as localContactId,")
                    .append("c.").append(ContactsColumns.PHOTO_ID).append(" as photoId,")
                    .append("c.").append(ContactsColumns.USER_TYPE).append(" as userType")
                    .append(" from ")
                    .append(ContactsColumns.TABLE_NAME).append(" c,")
                    .append(AccountsColumns.TABLE_NAME).append(" a ")
                    .append(" where c._id=a.")
                    .append(AccountsColumns.CONTACT_ID)
                    .append(" and a._id in (").append(ids).append(")")
                    .append(" order by c.pinyin ");
            contacts = queryWithSort(Contact.class, sql.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MessengerException(e);
        }
        return contacts;
    }

    /**
     * 添加联系人
     * 
     * @param contact
     * @return
     */
    @Override
    public long addContact(Contact contact) {
        ContentValues values = new ContentValues();
        String displayName = contact.getDisplayname();
        values.put(ContactsColumns.BIRTHDAY, contact.getBirthday());
        values.put(ContactsColumns.BLACK_LIST, contact.getBlackList());
        if (!TextUtils.isEmpty(displayName)) {
            values.put(ContactsColumns.DISPLAY_NAME, displayName);
            values.put(ContactsColumns.PINYIN, PinYinUtil.getPingYin(displayName));
            values.put(ContactsColumns.SORTKEY, PinYinUtil.getSortKey(displayName));
        }
        values.put(ContactsColumns.HOMETOWN, contact.getHometown());
        values.put(ContactsColumns.USER_TYPE, contact.getUserType());
        values.put(ContactsColumns.CLOUD_ID, contact.getCloudId());
        values.put(ContactsColumns.NOTE, contact.getNote());
        values.put(ContactsColumns.ORGANIZATION, contact.getOrganization());
        values.put(ContactsColumns.SCHOOL, contact.getSchool());
        values.put(ContactsColumns.BLACK_LIST, contact.getBlackList());
        values.put(ContactsColumns.SEX, contact.getSex());
        values.put(ContactsColumns.SKYID, SettingsPreferences.getSKYID());
        values.put(ContactsColumns.SIGNATURE, contact.getSignature());
        values.put(ContactsColumns.SYNCED, contact.getSynced());
        values.put(ContactsColumns.LAST_UPDATE_TIME, System.currentTimeMillis());
        values.put(ContactsColumns.PHOTO_ID, contact.getPhotoId());
        long localContactId = 0;
        if (SettingsPreferences.BIND_LOCAL.equals(SettingsPreferences.getBindStatus())) {
            if (contact.getLocalContactId() <= 0) {
                // 添加到本地
                if (contact.getUserType() != ContactsColumns.USER_TYPE_STRANGER) {
                    try {
                        localContactId = addLocalContact(contact);
                        contact.setLocalContactId(localContactId);
                        updateContact(contact);
                    } catch (Exception e) {
                        Log.e(TAG, "写本地联系人失败");
                    }
                }
            } else {
                localContactId = contact.getLocalContactId();
            }
        }
        values.put(ContactsColumns.LOCAL_CONTACT_ID, localContactId);
        long id = insert(ContactsColumns.TABLE_NAME, null, values);
        ArrayList<Account> accounts = contact.getAccounts();
        for (Account account : accounts) {
            account.setContactId(id);
            addAccount(account);
        }
        contact.setId(id);
        return id;
    }

    private long addLocalContact(Contact contact) {
        ContactsObserver.shouxinUpdateTime = System.currentTimeMillis();
        boolean hasPhone = false;
        if (contact.getLocalContactId() > 0) {
            return contact.getLocalContactId();
        }
        for (Account aPhone : contact.getAccounts()) {
            if (!TextUtils.isEmpty(aPhone.getPhone())) {
                hasPhone = true;
                break;
            }
        }
        if (!hasPhone)
            return 0;
        // 向contact 中添加联系人姓名,获得一个Id(rawContactId)作为后面添加信息的依据。
        ContentValues values = new ContentValues();
        values.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, contact
                .getDisplayname());
        Uri rawContactUri = resolver.insert(RawContacts.CONTENT_URI, values);

        long rawContactId = ContentUris.parseId(rawContactUri);
        // 添加联系人姓名
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE,
                StructuredName.CONTENT_ITEM_TYPE);
        values.put(StructuredName.GIVEN_NAME, contact.getDisplayname());
        resolver.insert(ContactsContract.Data.CONTENT_URI, values);

        Uri phoneUri = null;
        // 添加电话号码
        for (Account aPhone : contact.getAccounts()) {
            if (TextUtils.isEmpty(aPhone.getPhone())) {
                continue;
            }
            phoneUri = Uri.withAppendedPath(rawContactUri,
                    ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
            values.clear();
            values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            values.put(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY,
                    1);
            values.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, aPhone.getPhone());
            resolver.insert(phoneUri, values);
        }
        ContactsObserver.shouxinUpdateTime = 0;
        return rawContactId;
    }

    @Override
    public int deleteContact(ArrayList<Contact> contacts) {
        int size = contacts.size();
        StringBuilder idsb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            idsb.append(contacts.get(i).getId() + ",");
        }
        try {
            deleteLocalContact(contacts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sql = "update " + ContactsColumns.TABLE_NAME + " set " + ContactsColumns.DELETED
                + "=" + ContactsColumns.DELETED_YES + " where "
                + ContactsColumns._ID + " in (" + idsb.substring(0, idsb.length() - 1) + ")";
        executeSQL(sql);
        return 0;
    }

    @Override
    public Contact deleteContact(long contactId, boolean isDeleteLocalContact) {
        Contact contact = getContactById(contactId);
        if (contact != null) {
            if (deleteContact(contact, isDeleteLocalContact)) {
                return contact;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public void deleteContactForSync(long id) {
        String sql = "delete from " + ContactsColumns.TABLE_NAME + " where "
                + ContactsColumns._ID + "=" + id;
        executeSQL(sql);
    }

    @Override
    public boolean deleteContact(Contact contact, boolean isDeleteLocalContact) {
        String sql = "update " + ContactsColumns.TABLE_NAME + " set " + ContactsColumns.DELETED
                + "=" + ContactsColumns.DELETED_YES + " where "
                + ContactsColumns._ID + "=" + contact.getId();
        ArrayList<String> sqls = new ArrayList<String>();
        sqls.add(sql);
        boolean flag = executeWithTransaction(sqls);
        if (flag && isDeleteLocalContact) {
            contact.setDeleted(ContactsColumns.DELETED_YES);
            try {
                deleteLocalContact(contact);
            } catch (Exception e) {
                Log.e(TAG, "删除本地联系人失败");
            }
        }
        return flag;
    }

    /**
     * 刪除本地聯繫人
     * 
     * @param contact
     */
    protected void deleteLocalContact(Contact contact) {
        ContactsObserver.shouxinUpdateTime = System.currentTimeMillis();
        if (!SettingsPreferences.BIND_LOCAL.equals(SettingsPreferences.getBindStatus())
                || contact.getLocalContactId() < 1) {
            return;
        }
        resolver.delete(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI,
                contact.getLocalContactId()), null, null);
        ContactsObserver.shouxinUpdateTime = 0;
    }

    protected void deleteLocalContact(ArrayList<Contact> contacts) {
        ContactsObserver.shouxinUpdateTime = System.currentTimeMillis();
        if (!SettingsPreferences.BIND_LOCAL.equals(SettingsPreferences.getBindStatus())) {
            return;
        }
        int size = contacts.size();
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (int i = 0; i < size; i++) {
            Contact contact = contacts.get(i);
            long localId = contact.getLocalContactId();
            operations.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(
                    ContactsContract.RawContacts.CONTENT_URI, localId)).build());
        }
        try {
            resolver.applyBatch(ContactsContract.RawContacts.CONTENT_URI.getAuthority(), operations);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ContactsObserver.shouxinUpdateTime = 0;
    }

    /**
     * 修改联系人
     * 
     * @param contact
     * @return
     */
    protected int updateContact(Contact contact) {
        if (contact == null)
            return -1;
        ContentValues values = new ContentValues();
        values.put(ContactsColumns.BIRTHDAY, contact.getBirthday());
        if (!TextUtils.isEmpty(contact.getDisplayname())) {
            values.put(ContactsColumns.DISPLAY_NAME, contact.getDisplayname());
            values.put(ContactsColumns.PINYIN, PinYinUtil.getPingYin(contact.getDisplayname()));
            values.put(ContactsColumns.SORTKEY, PinYinUtil.getSortKey(contact.getDisplayname()));
        }
        values.put(ContactsColumns.HOMETOWN, contact.getHometown());
        values.put(ContactsColumns.USER_TYPE, contact.getUserType());
        values.put(ContactsColumns.LOCAL_CONTACT_ID, contact.getLocalContactId());
        values.put(ContactsColumns.CLOUD_ID, contact.getCloudId());
        values.put(ContactsColumns.NOTE, contact.getNote());
        values.put(ContactsColumns.ORGANIZATION, contact.getOrganization());
        values.put(ContactsColumns.SCHOOL, contact.getSchool());
        values.put(ContactsColumns.SEX, contact.getSex());
        values.put(ContactsColumns.SIGNATURE, contact.getSignature());

        values.put(ContactsColumns.SYNCED, contact.getSynced());
        values.put(ContactsColumns.BLACK_LIST, contact.getBlackList());
        values.put(ContactsColumns.PHOTO_ID, contact.getPhotoId());
        values.put(ContactsColumns.LAST_UPDATE_TIME, System.currentTimeMillis());
        String whereClause = " _id=?";
        int result = update(ContactsColumns.TABLE_NAME, values, whereClause, new String[] {
                String.valueOf(contact.getId())
        });
        return result;
    }

    @Override
    public int updateContactWithValues(long id, ContentValues values, String... whereClause) {
        String where = " _id=" + id;
        if (whereClause != null && whereClause.length > 0 && !TextUtils.isEmpty(whereClause[0])) {
            where = whereClause[0];
        }
        values.put(ContactsColumns.LAST_UPDATE_TIME, System.currentTimeMillis());
        String displayName = values.getAsString(ContactsColumns.DISPLAY_NAME);
        if (displayName != null) {
            values.put(ContactsColumns.PINYIN, PinYinUtil.getPingYin(displayName));
            values.put(ContactsColumns.SORTKEY, PinYinUtil.getSortKey(displayName));
        }
        int result = update(ContactsColumns.TABLE_NAME, values, where, null);
        return result;
    }

    /**
     * 根据指定条件更新数据，该方法用于规范SQL写法，不在where中出现条件=值的情况， 例如：select * from table where
     * col=1 修改为 select * from table where col=? 具体的值应该出现在args[]中
     * 
     * @param values
     * @param whereClause
     * @param args
     * @return
     */
    @Override
    public int updateContactWithValues(ContentValues values, String whereClause, String[] args) {
        values.put(ContactsColumns.LAST_UPDATE_TIME, System.currentTimeMillis());
        String displayName = values.getAsString(ContactsColumns.DISPLAY_NAME);
        if (displayName != null) {
            values.put(ContactsColumns.PINYIN, PinYinUtil.getPingYin(displayName));
            values.put(ContactsColumns.SORTKEY, PinYinUtil.getSortKey(displayName));
        }
        int result = update(ContactsColumns.TABLE_NAME, values, whereClause, args);
        return result;
    }

    @Override
    public int updateContactWithAccounts(Contact contact) {
        int result = updateContact(contact);
        deleteAccountsByContactId(contact.getId());
        ArrayList<Account> newAccounts = contact.getAccounts();
        for (Account account : newAccounts) {
            account.setContactId(contact.getId());
            addAccount(account);
        }
        long localContactId = contact.getLocalContactId();
        try {
            updateLocalContact(contact);
        } catch (Exception e) {
            Log.e(TAG, "修改本地联系人失败");
        }
        // localContactId发生了变化，需要更新到数据库中
        if (localContactId != contact.getLocalContactId()) {
            ContentValues values = new ContentValues();
            values.put(ContactsColumns.LOCAL_CONTACT_ID, contact.getLocalContactId());
            String whereClause = " _id=?";
            update(ContactsColumns.TABLE_NAME, values, whereClause, new String[] {
                    String.valueOf(contact.getId())
            });
        }
        return result;
    }

    @Override
    public void batchOperateContact(HashSet<Contact> contacts) {
        beginTransaction();
        for (Contact contact : contacts) {
            switch (contact.getActionShouXin()) {
                case Contact.ACTION_ADD:
                case Contact.ACTION_CLOUD_AND_LOCAL:
                    addContact(contact);
                    break;
                case Contact.ACTION_LOACL_ADD:
                    addContact(contact);
                    break;
                case Contact.ACTION_LOACL_DELETE:
                    deleteContact(contact.getId(), false);
                    break;
                case Contact.ACTION_SHOUXIN_LINK_CLOUD:
                case Contact.ACTION_SHOUXIN_LINK_LOCAL:
                    ContentValues values = new ContentValues();
                    values.put(ContactsColumns.SYNCED, ContactsColumns.SYNC_YES);
                    if (contact.getCloudId() > 0) {
                        values.put(ContactsColumns.CLOUD_ID, contact.getCloudId());
                    }
                    if (contact.getLocalContactId() > 0) {
                        values.put(ContactsColumns.LOCAL_CONTACT_ID, contact.getLocalContactId());
                    }
                    updateContactWithValues(values, ContactsColumns._ID + "=?", new String[] {
                            String.valueOf(contact.getId())
                    });
                    break;
            }
        }
        endTransaction(true);
    }

    private void updateLocalContact(Contact contact) {
        if (!SettingsPreferences.BIND_LOCAL.equals(SettingsPreferences.getBindStatus())) {
            return;
        }
        ContactsObserver.shouxinUpdateTime = System.currentTimeMillis();
        boolean hasPhone = false;
        for (Account aPhone : contact.getAccounts()) {
            if (!TextUtils.isEmpty(aPhone.getPhone())) {
                hasPhone = true;
                break;
            }
        }
        if (!hasPhone) {
            deleteLocalContact(contact);
            contact.setLocalContactId(0);// 删除本地联系人后，要撤销与手信联系人与本地联系人的关系，否则再次同步会被认为本地已经删除，从而手信中联系人也被删除
            ContentValues values = new ContentValues();
            values.put(ContactsColumns.LOCAL_CONTACT_ID, contact.getLocalContactId());
            updateContactWithValues(values, ContactsColumns._ID + "=?", new String[] {
                    String.valueOf(contact.getId())
            });
            return;
        }

        Uri url = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI,
                contact.getLocalContactId());
        Cursor cursor = resolver.query(url, new String[] {
                "_id"
        }, null, null, null);
        // 没有对应的本地联系人, 添加联系人
        if (!(cursor != null && cursor.getCount() > 0)) {
            ContentValues values = new ContentValues();
            values.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, contact
                    .getDisplayname());
            Uri rawContactUri = resolver.insert(RawContacts.CONTENT_URI, values);

            long rawContactId = ContentUris.parseId(rawContactUri);
            contact.setLocalContactId(rawContactId);
        } else {

            // 删除姓名
            StringBuilder whereName = new StringBuilder();
            whereName.append(ContactsContract.Data.MIMETYPE + "='"
                    + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .append("' and " + ContactsContract.Data.RAW_CONTACT_ID + "="
                            + contact.getLocalContactId());
            resolver.delete(ContactsContract.Data.CONTENT_URI, whereName.toString(), null);
            // 删除电话号码
            StringBuilder wherePhone = new StringBuilder();
            wherePhone.append(ContactsContract.Data.MIMETYPE + "='"
                    + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .append("' and " + ContactsContract.Data.RAW_CONTACT_ID + "="
                            + contact.getLocalContactId());
            resolver.delete(ContactsContract.Data.CONTENT_URI, wherePhone.toString(), null);
        }
        // 增加姓名
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, contact.getLocalContactId());
        values.put(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                contact.getDisplayname());
        resolver.insert(ContactsContract.Data.CONTENT_URI, values);

        // 增加电话
        ArrayList<Account> accounts = contact.getAccounts();
        for (Account account : accounts) {
            if (TextUtils.isEmpty(account.getPhone())) {
                continue;
            }
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, contact.getLocalContactId());
            values.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, account.getPhone());
            resolver.insert(ContactsContract.Data.CONTENT_URI, values);
        }
        ContactsObserver.shouxinUpdateTime = 0;
    }

    /**
     * 添加账号
     * 
     * @param account
     * @return
     */
    private long addAccount(Account account) {
        ContentValues accountValues = createAccountValue(account);
        long id = insert(AccountsColumns.TABLE_NAME, null, accountValues);
        account.setId(id);
        return id;
    }

    @Override
    public long updateAccount(Account account) {
        String whereClause = " _id=?";
        ContentValues accountValues = createAccountValue(account);
        return update(AccountsColumns.TABLE_NAME, accountValues, whereClause, new String[] {
                String.valueOf(account.getId())
        });
    }

    /**
     * 修改Account
     * 
     * @param account
     * @return
     */
    protected ContentValues createAccountValue(Account account) {
        ContentValues accountValues = new ContentValues();
        if (account.getContactId() != 0)
            accountValues.put(AccountsColumns.CONTACT_ID, account.getContactId());
        accountValues.put(AccountsColumns.IS_MAIN, account.isMain() ? 1 : 0);
        String phone = account.getPhone();
        if (!TextUtils.isEmpty(phone)) {
            phone = StringUtil.removeHeader(phone);
            accountValues.put(AccountsColumns.PHONE, phone);
        }
        accountValues.put(AccountsColumns.SKY_NAME, account.getSkyAccount());
        accountValues.put(AccountsColumns.SKYID, account.getSkyId());
        accountValues.put(AccountsColumns.SKY_NICKNAME, account.getNickName());
        return accountValues;
    }

    @Override
    public long updateAccountByPhone(Account account) {
        String whereClause = AccountsColumns.PHONE + "='" + account.getPhone() + "'";
        ContentValues accountValues = createAccountValue(account);
        return update(AccountsColumns.TABLE_NAME, accountValues, whereClause, null);
    }

    @Override
    public long updateAccountBySkyIdAndContactId(Account account) {
        String whereClause = AccountsColumns.CONTACT_ID + "=" + account.getContactId() + " and "
                + AccountsColumns.SKYID + "=" + account.getSkyId();
        ContentValues accountValues = createAccountValue(account);
        return update(AccountsColumns.TABLE_NAME, accountValues, whereClause, null);
    }

    @Override
    public long updateAccountByContactId(Account account) {
        String whereClause = AccountsColumns.CONTACT_ID + "=" + account.getContactId();
        ContentValues accountValues = createAccountValue(account);
        return update(AccountsColumns.TABLE_NAME, accountValues, whereClause, null);
    }

    /**
     * 删除账号
     * 
     * @param account
     * @return
     */
    private int deleteAccountsByContactId(long contactId) {
        return delete(AccountsColumns.TABLE_NAME, AccountsColumns.CONTACT_ID + "=" + contactId,
                null);
    }

    @Override
    public ArrayList<Account> getAccountByContactId(long contactId) {
        StringBuilder sqlForAccounts = new StringBuilder();
        sqlForAccounts.append("select ")
                .append(AccountsColumns.CONTACT_ID).append(" as contactId, ")
                .append(AccountsColumns.IS_MAIN).append(" as main, ")
                .append(AccountsColumns.PHONE).append(" as phone, ")
                .append(AccountsColumns.SKY_NAME).append(" as skyAccount, ")
                .append(AccountsColumns.SKYID).append(" as skyId, ")
                .append(AccountsColumns.SKY_NICKNAME).append(" as nickname, ")
                .append(AccountsColumns._ID).append(" as id")
                .append(" from ").append(AccountsColumns.TABLE_NAME)
                .append(" where ")
                .append(AccountsColumns.CONTACT_ID).append("=?");
        return queryWithSort(Account.class, sqlForAccounts.toString(),
                new String[] {
                    contactId + ""
                });
    }

    /**
     * 根据电话号码查账号ID
     * 
     * @param phone
     * @return
     */
    private long queryAccountIdByPhone(String phone) {
        StringBuilder sqlForContactId = new StringBuilder();
        phone = StringUtil.removeHeader(phone);
        sqlForContactId.append("select ")
                .append(AccountsColumns._ID).append(" as id,")
                .append(AccountsColumns.PHONE).append(" as phone,")
                .append(AccountsColumns.SKYID).append(" as skyId")
                .append(" from ").append(AccountsColumns.TABLE_NAME)
                .append(" where ")
                // .append(AccountsColumns.PHONE).append("=?");
                .append(AccountsColumns.PHONE).append(" like '%").append(phone).append("'");

        ArrayList<Account> accounts = queryWithSort(Account.class, sqlForContactId.toString());

        for (Account account : accounts) {
            if (PhoneNumberUtils.compare(account.getPhone(), phone))
                return account.getId();
        }
        return -1;
    }

    /**
     * 根据电话号码查账号ID
     * 
     * @param phone
     * @return
     */
    private ArrayList<Long> queryAccountIdByPhone2(String phone) {
        ArrayList<Long> ids = new ArrayList<Long>();
        StringBuilder sqlForContactId = new StringBuilder();
        phone = StringUtil.removeHeader(phone);
        sqlForContactId.append("select ")
                .append(AccountsColumns._ID).append(" as id,")
                .append(AccountsColumns.PHONE).append(" as phone,")
                .append(AccountsColumns.SKYID).append(" as skyId")
                .append(" from ").append(AccountsColumns.TABLE_NAME)
                .append(" where ")
                // .append(AccountsColumns.PHONE).append("=?");
                .append(AccountsColumns.PHONE).append(" like '%").append(phone).append("'");

        ArrayList<Account> accounts = queryWithSort(Account.class, sqlForContactId.toString());

        for (Account account : accounts) {
            if (PhoneNumberUtils.compare(account.getPhone(), phone))
                ids.add(account.getId());
        }
        return ids;
    }

    /**
     * @param phone
     * @return
     */
    private Account queryAccountByPhone(String phone) {
        StringBuilder sqlForContactId = new StringBuilder();
        phone = StringUtil.removeHeader(phone);
        sqlForContactId.append("select ")
                .append(AccountsColumns._ID).append(" as id,")
                .append(AccountsColumns.CONTACT_ID).append(" as contactId, ")
                .append(AccountsColumns.IS_MAIN).append(" as main, ")
                .append(AccountsColumns.PHONE).append(" as phone, ")
                .append(AccountsColumns.SKY_NAME).append(" as skyAccount, ")
                .append(AccountsColumns.SKY_NICKNAME).append(" as nickname, ")
                .append(AccountsColumns.SKYID).append(" as skyId ")
                .append(" from ").append(AccountsColumns.TABLE_NAME)
                .append(" where ")
                // .append(AccountsColumns.PHONE).append("=?");
                .append(AccountsColumns.PHONE).append(" like '%").append(phone).append("'");

        ArrayList<Account> accounts = queryWithSort(Account.class, sqlForContactId.toString());

        for (Account account : accounts) {
            if (PhoneNumberUtils.compare(account.getPhone(), phone))
                return account;
        }
        return null;
    }

    /**
     * @param skyid
     * @return
     */
    private Account queryAccountBySkyid(int skyid) {
        StringBuilder sqlForContactId = new StringBuilder();
        sqlForContactId.append("select ")
                .append(AccountsColumns._ID).append(" as id,")
                .append(AccountsColumns.CONTACT_ID).append(" as contactId, ")
                .append(AccountsColumns.IS_MAIN).append(" as main, ")
                .append(AccountsColumns.PHONE).append(" as phone, ")
                .append(AccountsColumns.SKY_NAME).append(" as skyAccount, ")
                .append(AccountsColumns.SKY_NICKNAME).append(" as nickname, ")
                .append(AccountsColumns.SKYID).append(" as skyId ")
                .append(" from ").append(AccountsColumns.TABLE_NAME)
                .append(" where ")
                .append(AccountsColumns.SKYID).append("=?");
        Account account = queryForObject(Account.class, sqlForContactId.toString(), new String[] {
                String.valueOf(skyid)
        });

        return account;
    }

    private long queryAccountIdBySkyid(int skyid, String nickname) {
        StringBuilder sqlForContactId = new StringBuilder();
        sqlForContactId.append("select ")
                .append(AccountsColumns._ID).append(" as id,")
                .append(AccountsColumns.CONTACT_ID).append(" as contactId, ")
                .append(AccountsColumns.IS_MAIN).append(" as main, ")
                .append(AccountsColumns.PHONE).append(" as phone, ")
                .append(AccountsColumns.SKY_NAME).append(" as skyAccount, ")
                .append(AccountsColumns.SKY_NICKNAME).append(" as nickname, ")
                .append(AccountsColumns.SKYID).append(" as skyId ")
                .append(" from ").append(AccountsColumns.TABLE_NAME)
                .append(" where ")
                .append(AccountsColumns.SKYID).append("=?");
        Account account = queryForObject(Account.class, sqlForContactId.toString(), new String[] {
                String.valueOf(skyid)
        });

        if (account == null) {
            return -1;
        }
        // 昵称改变后需要更新account里面的nickname
        if (nickname != null && !nickname.equals(account.getNickName())) {
            account.setNickName(nickname);
            updateAccount(account);
        }
        return account.getId();
    }

    @Override
    public String getAccoutIdByPhoneOrSkyid(String phone, int skyid, String skyAccount,
            String nickname) {
        if (null != phone) {
            long accountId = queryAccountIdByPhone(phone);
            if (accountId > 0) {
                return String.valueOf(accountId);
            } else { // insert
                Account account = new Account();
                account.setPhone(phone);
                account.setNickName(nickname);
                return String.valueOf(addAccount(account));
            }
        } else {
            long accountId = queryAccountIdBySkyid(skyid, nickname);
            if (accountId > 0) {
                return String.valueOf(accountId);
            } else {
                Account account = new Account();
                account.setSkyId(skyid);
                account.setSkyAccount(skyAccount != null ? skyAccount : nickname);
                account.setNickName(nickname);
                return String.valueOf(addAccount(account));
            }
        }
    }

    @Override
    public Account getAccountByAccountID(long accountId) {
        StringBuilder sqlForContactId = new StringBuilder();
        sqlForContactId.append("select ")
                .append(AccountsColumns._ID).append(" as id,")
                .append(AccountsColumns.CONTACT_ID).append(" as contactId, ")
                .append(AccountsColumns.IS_MAIN).append(" as main, ")
                .append(AccountsColumns.PHONE).append(" as phone, ")
                .append(AccountsColumns.SKY_NAME).append(" as skyAccount, ")
                .append(AccountsColumns.SKY_NICKNAME).append(" as nickname, ")
                .append(AccountsColumns.SKYID).append(" as skyId ")
                .append(" from ").append(AccountsColumns.TABLE_NAME)
                .append(" where ")
                .append(AccountsColumns._ID).append("=?");
        return queryForObject(Account.class, sqlForContactId.toString(), new String[] {
                String.valueOf(accountId)
        });
    }

    @Override
    public Account getAccoutByAddress(Address address) {
        if (null == address)
            throw new RuntimeException("参数不能为空");

        if (address.getSkyId() > 0) {
            Account account = queryAccountBySkyid(address.getSkyId());
            return account;
        } else if (null != address.getPhone()) {
            Account account = queryAccountByPhone(address.getPhone());
            return account;
        }
        return null;
    }

    @Override
    public ArrayList<Contact> getContactsLocalList() {
        HashMap<Long, Contact> contactMap = new HashMap<Long, Contact>();
        HashMap<Long, Boolean> contactIdMap = new HashMap<Long, Boolean>();
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        StringBuilder sb = new StringBuilder();
        // contacts 表中的联系人
        Cursor cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[] {
                    ContactsContract.Contacts._ID
                }, null/*
                        * 这两个字段在不同rom中定义不同故 不以此为条件
                        * ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1 and "
                        * + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1"
                        */, null, null);

        while (cursor.moveToNext()) {
            long contactId = cursor.getLong(0);
            sb.append(contactId + ",");
            contactIdMap.put(contactId, false);
        }
        cursor.close();
        String ids = "";
        if (sb.length() > 0) {
            ids = sb.substring(0, sb.length() - 1);
        }
        // raw_contacts 表中的联系人
        cursor = resolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[] {
                        ContactsContract.RawContacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.RawContacts.CONTACT_ID
                }, ContactsContract.RawContacts.CONTACT_ID + " in (" + ids + ") and deleted=0",
                null, null);

        sb = sb.delete(0, sb.length());
        while (cursor.moveToNext()) {
            Long id = cursor.getLong(0);
            Long contactId = cursor.getLong(2);
            Boolean need = contactIdMap.get(contactId);
            if (need != null && need) {
                continue;
            }
            contactIdMap.put(contactId, true);
            String displayName = cursor.getString(1);
            Contact contact = contactMap.get(id);
            if (contact == null) {
                contact = new Contact();
                contact.setLocalContactId(id);
                contact.setDisplayname(displayName);
                contactMap.put(id, contact);
            }
            sb.append(id + ",");
        }
        cursor.close();
        if (sb.length() > 0) {
            ids = sb.substring(0, sb.length() - 1);
        } else {
            ids = "";
        }
        // Data
        Cursor cursorLocalContacts = null;
        try {
            String minetypeSql = "select _id from mimetypes where mimetype='"
                    + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";
            cursorLocalContacts = resolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[] {
                            ContactsContract.Data.RAW_CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                    }, ContactsContract.Data.RAW_CONTACT_ID + " in (" + ids
                            + ") and mimetype_id=(" + minetypeSql + ")", null,
                    null);
        } catch (Exception e) {
            // 小米手机专用
            cursorLocalContacts = resolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[] {
                            ContactsContract.Data.RAW_CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                    }, ContactsContract.Data.RAW_CONTACT_ID + " in (" + ids
                            + ") and mimetype='"
                            + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'", null,
                    null);
        }

        if (cursorLocalContacts == null) {
            return contacts;
        }
        while (cursorLocalContacts.moveToNext()) {
            Long localContactId = cursorLocalContacts.getLong(0);
            Contact localContact = contactMap.get(localContactId);
            if (localContact == null) {
                continue;
            }
            String phone = cursorLocalContacts.getString(1);
            phone = StringUtil.removeHeader(phone);

            // 添加号码
            if (!TextUtils.isEmpty(phone)) {
                if (TextUtils.isEmpty(localContact.getDisplayname())) {
                    localContact.setDisplayname(phone);
                }

                Account account = new Account();
                account.setPhone(phone);
                localContact.addAccount(account);
            }
        }
        if (cursorLocalContacts != null)
            cursorLocalContacts.close();
        for (Map.Entry<Long, Contact> entry : contactMap.entrySet()) {
            Contact contact = entry.getValue();
            if (contact.getAccounts().isEmpty()) {
                continue;
            }
            contacts.add(contact);
        }
        return contacts;
    }

    @Override
    public Map<String, Object> getCardByContactId(long contactID) {
        Contact contact = getContactById(contactID);
        if (contact == null)
            return null;
        Map<String, Object> mapCard = new HashMap<String, Object>();
        mapCard.put(NetVCardNotify.CONTACT_NAME, contact.getDisplayname());
        ArrayList<VCardContent> list = new ArrayList<VCardContent>();
        ArrayList<Account> accounts = contact.getAccounts();
        // 首先设置为空
        int max = android.skymobi.messenger.utils.Constants.MAX_CARD_LIST;
        for (int i = 0; i < max; i++) {
            list.add(new VCardContent());
        }
        // 如果有电话号码则设置电话号码，没有则是用户sky帐号,将它设置到最后一个
        for (int i = 0; i < accounts.size(); i++) {
            // 如果有电话号码
            if (accounts.get(i).getPhone() != null) {
                list.get(i).setPhone(accounts.get(i).getPhone());
            } else if (accounts.get(i).getSkyId() > 1) {
                list.get(max - 1).setHeadicon("");
                list.get(max - 1).setNickname(accounts.get(i).getSkyAccount());
                list.get(max - 1).setSkyid(String.valueOf(accounts.get(i).getSkyId()));
                list.get(max - 1).setUsertype(String.valueOf(contact.getUserType()));
            }
        }
        mapCard.put(NetVCardNotify.CONTACT_DETAIL_LIST, list);
        return mapCard;
    }

    @Override
    public void addStangerBlacklistContacts(ArrayList<Contact> blackContacts) {
        beginTransaction();
        delete(ContactsColumns.TABLE_NAME, ContactsColumns.USER_TYPE + "=? and "
                + ContactsColumns.BLACK_LIST + "=?", new String[] {
                ContactsColumns.USER_TYPE_STRANGER + "", ContactsColumns.BLACK_LIST_YES + ""
        });
        for (Contact contact : blackContacts) {
            addContact(contact);
        }
        endTransaction(true);
    }

    /**
     * 查询黑名单列表相关信息
     * 
     * @param isBlackList
     * @return
     */
    private ArrayList<Contact> getContactInfoForBlackList(boolean withAccounts) {
        StringBuilder sql = new StringBuilder();
        sql.append(
                "select id,cloudId,localContactId,displayname,nickName,signature,pinyin,userType,phone,photoId from (")
                .append("select ")
                .append("c.").append(ContactsColumns._ID).append(" as id, ")
                .append("c.").append(ContactsColumns.DISPLAY_NAME).append(" as displayname,")
                .append("c.").append(ContactsColumns.CLOUD_ID).append(" as cloudId,")
                .append("c.").append(ContactsColumns.LOCAL_CONTACT_ID)
                .append(" as localContactId,")
                .append("a.").append(AccountsColumns.SKY_NICKNAME).append(" as nickName,")
                .append("c.").append(ContactsColumns.SIGNATURE).append(" as signature,")
                .append("c.").append(ContactsColumns.PINYIN).append(" as pinyin,")
                .append("c.").append(ContactsColumns.USER_TYPE).append(" as userType,")
                .append("c.").append(ContactsColumns.LAST_UPDATE_TIME)
                .append(" as lastUpdateTime,")
                .append("a.").append(AccountsColumns.PHONE).append(" as phone,")
                .append("c.").append(ContactsColumns.PHOTO_ID).append(" as photoId")
                .append(" from ")
                .append(ContactsColumns.TABLE_NAME).append(" c,")
                .append(AccountsColumns.TABLE_NAME).append(" a ")
                .append("where ")
                .append(" c." + ContactsColumns.SKYID + "=" + SettingsPreferences.getSKYID())
                .append(" and c." + ContactsColumns.BLACK_LIST + "="
                        + ContactsColumns.BLACK_LIST_YES)
                .append(" and c._id = a.contact_id and a."+AccountsColumns.SKYID+">0 ")
                .append(" order by c.pinyin ,a._id desc")
                .append(") ")
                .append(" group by id order by pinyin,displayname desc ,lastUpdateTime desc ");
        ArrayList<Contact> contacts = queryWithSort(Contact.class, sql.toString());
        // 查询联系人账号信息
        if (withAccounts) {
            for (Contact contact : contacts) {
                contact.setAccounts(getAccountByContactId(contact.getId()));
            }
        }
        return contacts;
    }

    /**
     * 查询列表相关信息
     * 
     * @return
     */
    @Override
    public ArrayList<Contact> getContactInfoForList() {
        StringBuilder sql = new StringBuilder();
        sql.append(
                "select id,displayname,nickName,signature,pinyin,userType,phone,photoId,username,skyid,ismain,accountId,contactId,cloudId,localContactId from (")
                .append("select ")
                .append("c.").append(ContactsColumns._ID).append(" as id, ")
                .append("c.").append(ContactsColumns.DISPLAY_NAME).append(" as displayname,")
                .append("c.").append(ContactsColumns.LOCAL_CONTACT_ID)
                .append(" as localContactId,")
                .append("c.").append(ContactsColumns.PHOTO_ID).append(" as photoId,")
                .append("c.").append(ContactsColumns.CLOUD_ID).append(" as cloudId,")
                .append("a.").append(AccountsColumns.SKY_NICKNAME).append(" as nickName,")
                .append("c.").append(ContactsColumns.SIGNATURE).append(" as signature,")
                .append("c.").append(ContactsColumns.PINYIN).append(" as pinyin,")
                .append("c.").append(ContactsColumns.USER_TYPE).append(" as userType,")
                .append("c.").append(ContactsColumns.LAST_UPDATE_TIME)
                .append(" as lastUpdateTime,")
                .append("a.").append(AccountsColumns.PHONE).append(" as phone,")
                .append("a.").append(AccountsColumns.SKY_NAME).append(" as username,")
                .append("a.").append(AccountsColumns.SKYID).append(" as skyid,")
                .append("a.").append(AccountsColumns.IS_MAIN).append(" as ismain,")
                .append("a.").append(AccountsColumns._ID).append(" as accountId,")
                .append("a.").append(AccountsColumns.CONTACT_ID).append(" as contactId")
                .append(" from ")
                .append(ContactsColumns.TABLE_NAME).append(" c,")
                .append(AccountsColumns.TABLE_NAME).append(" a ")
                .append("where ")
                .append(" c." + ContactsColumns.SKYID + "=" + SettingsPreferences.getSKYID())
                .append(" and c." + ContactsColumns.DELETED + "=0")
                .append(" and c." + ContactsColumns.USER_TYPE + "<>"
                        + ContactsColumns.USER_TYPE_STRANGER);
        sql.append(" and c." + ContactsColumns.BLACK_LIST + "=" + ContactsColumns.BLACK_LIST_NO)
                .append(" and c._id = a.contact_id ")
                .append(" order by c.pinyin ,a._id desc")
                .append(") ")
                .append(" order by pinyin,displayname desc ,lastUpdateTime desc ");
        Cursor cursor = getSQLiteDatabase().rawQuery(sql.toString(), null);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String displayName = cursor.getString(1);
                String nickName = cursor.getString(2);
                String signature = cursor.getString(3);
                String pinyin = cursor.getString(4);
                int userType = cursor.getInt(5);
                String phone = cursor.getString(6);
                String photoId = cursor.getString(7);
                String username = cursor.getString(8);
                int skyid = cursor.getInt(9);
                int ismain = cursor.getInt(10);
                long accountId = cursor.getLong(11);
                long contactId = cursor.getLong(12);
                long cloudId = cursor.getLong(13);
                long localContactId = cursor.getLong(14);
                Contact contact = new Contact();
                contact.setId(id);
                contact = ListUtil.getObject(contacts, contact,
                        ComparatorFactory.getShouxinComparator());
                if (contact != null) {
                    if (!TextUtils.isEmpty(phone)) {
                        contact.setPhone(phone);
                    }
                    if (TextUtils.isEmpty(contact.getDisplayname()) && !TextUtils.isEmpty(nickName)) {
                        contact.setNickName(nickName);
                    }
                    Account account = new Account();
                    account.setPhone(phone);
                    account.setMain(ismain);
                    account.setNickName(nickName);
                    account.setSkyAccount(username);
                    account.setSkyId(skyid);
                    account.setId(accountId);
                    account.setContactId(contactId);
                    contact.addAccount(account);
                    if (skyid > 0) {
                        contact.setUserType(1);
                    }
                } else {
                    contact = new Contact();
                    contact.setId(id);
                    contact.setDisplayname(displayName);
                    contact.setSignature(signature);
                    contact.setCloudId(cloudId);
                    contact.setPinyin(pinyin);
                    contact.setPhone(phone);
                    contact.setPhotoId(photoId);
                    contact.setLocalContactId(localContactId);
                    Account account = new Account();
                    account.setId(accountId);
                    account.setContactId(contactId);
                    account.setPhone(phone);
                    account.setMain(ismain);
                    account.setNickName(nickName);
                    account.setSkyAccount(username);
                    account.setSkyId(skyid);
                    if (skyid > 0) {
                        contact.setUserType(1);
                    }
                    contact.addAccount(account);
                    if (TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(nickName)) {
                        contact.setDisplayname(nickName);
                    }
                    contacts.add(contact);
                }
            }
            cursor.close();
        }
        return contacts;
    }

    @Override
    public HashMap<Long, Boolean> checkLocalContactByIds(String ids) {
        HashMap<Long, Boolean> map = new HashMap<Long, Boolean>();
        StringBuilder sb = new StringBuilder();
        // contacts 表中的联系人
        Cursor cursor = resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[] {
                    ContactsContract.Contacts._ID
                }, null, null, null);

        while (cursor.moveToNext()) {
            long contactId = cursor.getLong(0);
            sb.append(contactId + ",");
        }
        cursor.close();
        String ids1 = "";
        if (sb.length() > 0) {
            ids1 = sb.substring(0, sb.length() - 1);
        }
        cursor = resolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[] {
                    ContactsContract.RawContacts._ID,
                }, ContactsContract.RawContacts._ID + " in (" + ids + ") and "
                        + ContactsContract.RawContacts.CONTACT_ID + " in (" + ids1 + ")", null,
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long key = cursor.getLong(0);
                map.put(key, true);
            }
        }
        return map;
    }

    @Override
    public long getAccountByPhoneOrSkyId(String phone, String skyId) {
        Account account = null;
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append(AccountsColumns.CONTACT_ID + " as contactId ")
                .append(" from ").append(AccountsColumns.TABLE_NAME);
        if (null != skyId) {
            sql.append(" where ").append(AccountsColumns.SKYID).append(" =? ");
            account = queryForObject(Account.class, sql.toString(), new String[] {
                    skyId
            });
        } else if (null != phone) {
            sql.append(" where ").append(AccountsColumns.PHONE).append(" =? ");
            account = queryForObject(Account.class, sql.toString(), new String[] {
                    phone
            });
        }
        if (null == account || account.getContactId() <= 0) {
            return 0;
        } else {
            return account.getContactId();
        }
    }

    @Override
    public void updateContactAccounts(Contact contact) {
        long contactId = contact.getId();
        deleteAccountsByContactId(contactId);
        for (Account account : contact.getAccounts()) {
            addAccount(account);
        }
        try {
            updateLocalContact(contact);
        } catch (Exception e) {
            Log.e(TAG, "修改本地联系人失败");
        }
    }

    @Override
    public ArrayList<Contact> getContactInfoForPhoto() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append("c.").append(ContactsColumns._ID).append(" as id, ")
                .append("c.").append(ContactsColumns.DISPLAY_NAME).append(" as displayname,")
                .append("c.").append(ContactsColumns.PHOTO_ID).append(" as photoId,")
                .append("a.").append(AccountsColumns.PHONE).append(" as phone,")
                .append("a.").append(AccountsColumns.SKYID).append(" as skyid,")
                .append("a.").append(AccountsColumns._ID).append(" as accountId ")
                .append(" from ")
                .append(ContactsColumns.TABLE_NAME).append(" c,")
                .append(AccountsColumns.TABLE_NAME).append(" a ")
                .append("where ")
                .append(" c." + ContactsColumns.SKYID + "=" + SettingsPreferences.getSKYID())
                .append(" and c." + ContactsColumns.DELETED + "=0")
                .append(" and c._id = a.contact_id ")
                .append(" and c.photo_id is not null ");
        Cursor cursor = getSQLiteDatabase().rawQuery(sql.toString(), null);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String displayName = cursor.getString(1);
                String photoId = cursor.getString(2);
                String phone = cursor.getString(3);
                int skyid = cursor.getInt(4);
                long accountId = cursor.getLong(5);
                Contact contact = new Contact();
                contact.setId(id);
                contact = ListUtil.getObject(contacts, contact,
                        ComparatorFactory.getShouxinComparator());
                if (contact != null) {
                    Account account = new Account();
                    account.setPhone(phone);
                    account.setSkyId(skyid);
                    account.setId(accountId);
                    contact.addAccount(account);
                } else {
                    contact = new Contact();
                    contact.setId(id);
                    contact.setPhotoId(photoId);
                    contact.setDisplayname(displayName);
                    Account account = new Account();
                    account.setId(accountId);
                    account.setPhone(phone);
                    account.setSkyId(skyid);
                    contact.addAccount(account);
                    contacts.add(contact);
                }
            }
            cursor.close();
        }
        return contacts;
    }

    @Override
    public HashSet<Integer> getSkyIds() {
        HashSet<Integer> list = new HashSet<Integer>();
        String sql = "select " + AccountsColumns.SKYID + " from " + AccountsColumns.TABLE_NAME
                + " where " + AccountsColumns.SKYID + " is not null group by "
                + AccountsColumns.SKYID;
        ArrayList<String> result = query(sql, null);
        for (String value : result) {
            try {
                list.add(Integer.valueOf(value));
            } catch (Exception e) {
            }
        }
        return list;
    }

    @Override
    public ArrayList<Contact> getContactBySkyid(int skyid) {
        // 排序 黑名单->陌生人->联系人
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append("c.").append(ContactsColumns._ID).append(" as id, ")
                .append(ContactsColumns.USER_TYPE).append(" as userType,")
                .append(ContactsColumns.BLACK_LIST).append(" as blackList")
                .append(" from ")
                .append(ContactsColumns.TABLE_NAME).append(" c,")
                .append(AccountsColumns.TABLE_NAME).append(" a")
                .append(" where ")
                .append("c.").append(ContactsColumns._ID)
                .append(" = a.").append(AccountsColumns.CONTACT_ID)
                .append(" and ")
                .append("c.").append(ContactsColumns.DELETED).append(" = ")
                .append(ContactsColumns.DELETED_NO)
                .append(" and ")
                .append("a.").append(AccountsColumns.SKYID).append(" =? ")
                .append(" order by ").append(ContactsColumns.BLACK_LIST).append(" desc,")
                .append(ContactsColumns.USER_TYPE).append(" desc");
        ArrayList<Contact> contacts = queryWithSort(Contact.class, sql.toString(), new String[] {
                "" + skyid
        });

        return contacts;
    }

    /**
     * 根据手机号码查找ACCOUNTID
     * 
     * @param phone
     * @return
     */
    @Override
    public ArrayList<Long> getAccountIdByPhone(String phone) {
        ArrayList<Long> ids = new ArrayList<Long>();
        if (null != phone) {
            ids = queryAccountIdByPhone2(phone);
        }
        return ids;
    }

    /**
     * 批量重置Accounts表中帐号数据
     * 
     * @return
     */
    @Override
    public int batchResetAccount() {
        ContentValues values = new ContentValues();
        values.put(AccountsColumns.SKYID, 0);
        values.put(AccountsColumns.IS_MAIN, 0);
        values.put(AccountsColumns.SKY_NICKNAME, "");
        values.put(AccountsColumns.SKY_NAME, "");
        return update(AccountsColumns.TABLE_NAME, values, AccountsColumns.SKYID + ">?",
                new String[] {
                    "0"
                });
    }

}
