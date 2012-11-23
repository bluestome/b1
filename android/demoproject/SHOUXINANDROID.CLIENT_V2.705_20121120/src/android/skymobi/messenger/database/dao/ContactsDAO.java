
package android.skymobi.messenger.database.dao;

import android.content.ContentValues;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.bean.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @ClassName: ContactsDAO
 * @Description: 联系人相关数据库操作
 * @author Sean.Xie
 * @date 2012-2-7 下午4:08:13
 */
public interface ContactsDAO extends BaseDAO {

    public void batchOperateContact(HashSet<Contact> contacts);

    /**
     * 获取完整联系人信息
     * 
     * @return
     */
    ArrayList<Contact> getContactsListForSync();

    /**
     * 添加联系人
     * 
     * @return
     */
    long addContact(Contact contact);

    /**
     * 删除联系人
     * 
     * @param contact
     * @param isDeleteLocalContact 是否删除本地对应联系人
     * @return
     */
    boolean deleteContact(Contact contact, boolean isDeleteLocalContact);

    /**
     * 删除联系人
     * 
     * @param contactId
     * @param isDeleteLocalContact 是否删除本地对应联系人
     * @return
     */
    Contact deleteContact(long contactId, boolean isDeleteLocalContact);

    /**
     * 同步删除联系人
     */
    void deleteContactForSync(long id);

    /**
     * 修改联系人
     * 
     * @param contact
     * @return
     */
    int updateContactWithAccounts(Contact contact);

    /**
     * 根据指定值修改联系人
     * 
     * @param id
     * @param values
     * @return
     */
    int updateContactWithValues(long id, ContentValues values, String... whereClause);

    /**
     * 根据ID找联系人
     * 
     * @param id
     * @return
     */
    Contact getContactById(long id);

    /**
     * 获取联系人账号
     * 
     * @param contactId
     * @return
     * @throws Exception
     */
    ArrayList<Account> getAccountByContactId(long contactId);

    /**
     * 通过电话或者skyid获取account id
     * 
     * @param skyid
     * @param nickname
     * @return
     */
    String getAccoutIdByPhoneOrSkyid(String phone, int skyid, String skyAccount, String nickname);

    /**
     * 通过accountid查account
     * 
     * @param accountID
     * @return
     */
    Account getAccountByAccountID(long accountId);

    /**
     * 获取黑名单列表
     * 
     * @return
     */
    ArrayList<Contact> getContactsBlackList(boolean withAccounts);

    /**
     * 获取本地联系人列表
     * 
     * @return
     */
    ArrayList<Contact> getContactsLocalList();

    /**
     * 通过contactID获取名片map对象
     * 
     * @param contactID
     * @return
     */
    Map<String, Object> getCardByContactId(long contactID);

    /**
     * 通过账号Id找联系人
     * 
     * @param ids
     * @return
     */
    ArrayList<Contact> getContactByAccountIds(String ids);

    /**
     * 修改账号
     * 
     * @param account
     * @return
     */
    long updateAccount(Account account);

    long updateAccountByPhone(Account account);

    long updateAccountBySkyIdAndContactId(Account account);

    /**
     * 根据联系人ID修改ACCOUNT
     * 
     * @param account
     * @return
     */
    long updateAccountByContactId(Account account);

    /**
     * @param blackContacts
     */
    void addStangerBlacklistContacts(ArrayList<Contact> blackContacts);

    /**
     * 获取sim卡中联系人
     * 
     * @return
     */
    HashMap<String, Contact> getSimContactList();

    /**
     * @return
     */
    ArrayList<Contact> getContactInfoForList();

    /**
     * @param address
     * @return
     */
    Account getAccoutByAddress(Address address);

    /**
     * @param ids
     */
    HashMap<Long, Boolean> checkLocalContactByIds(String ids);

    long getAccountByPhoneOrSkyId(String phone, String skyId);

    /**
     * 修改联系人账号
     * 
     * @param contact
     */
    public void updateContactAccounts(Contact contact);

    /**
     * 获取联系人表中所有的有头像的用户
     * 
     * @return
     */
    public ArrayList<Contact> getContactInfoForPhoto();

    /**
     * 获取SKYID
     * 
     * @return
     */
    HashSet<Integer> getSkyIds();

    /**
     * 根据skyid获取对应的contact,黑名单用户优先
     * 
     * @param skyid
     * @return
     */
    ArrayList<Contact> getContactBySkyid(int skyid);

    /**
     * @param contacts
     * @return
     */
    int deleteContact(ArrayList<Contact> contacts);

    /**
     * 根据手机号码查找ACCOUNTID
     * 
     * @param phone
     * @return
     */
    ArrayList<Long> getAccountIdByPhone(String phone);

    /**
     * 根据指定条件更新数据，该方法用于规范SQL写法，不在where中出现条件=值的情况， 例如：select * from table where
     * col=1 修改为 select * from table where col=? 具体的值应该出现在args[]中
     * 
     * @param values
     * @param whereClause
     * @param args
     * @return
     */
    int updateContactWithValues(ContentValues values, String whereClause, String[] args);

    /**
     * 批量重置Accounts中手信用户的帐号数据
     * 
     * @return
     */
    int batchResetAccount();
}
