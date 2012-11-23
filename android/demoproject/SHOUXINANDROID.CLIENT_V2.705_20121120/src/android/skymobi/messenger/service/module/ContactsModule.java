
package android.skymobi.messenger.service.module;

import android.content.ContentValues;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.cache.APPCache;
import android.skymobi.messenger.comparator.CompCallBack;
import android.skymobi.messenger.comparator.ComparatorFactory;
import android.skymobi.messenger.database.dao.ContactsDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.exception.NoNeedSendRequestException;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.ContactBlackListActivity;
import android.skymobi.messenger.ui.ContactMutilDeleteListActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ListUtil;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.skymobi.android.sx.codec.beans.common.VCardContent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @ClassName: ContactsModule
 * @Description: 联系人组件
 * @author Sean.Xie
 * @date 2012-2-13 下午2:13:33
 */
public class ContactsModule extends BaseModule {
    String TAG = ContactsModule.class.getSimpleName();

    private ContactsDAO contactsDAO = null;
    private final ContactsNetModule contactsNetModule = netWorkMgr.getContactsNetModule();

    public ContactsModule(CoreService service) {
        super(service);
        contactsDAO = DaoFactory.getInstance(MainApp.i().getApplicationContext()).getContactsDAO();
    }

    // 同步联系人
    public void syncContacts() {
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {

                // final long bTime = System.currentTimeMillis();
                sync();
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_SYNC_END, null);
                // final long eTime = System.currentTimeMillis();
                // SLog.e(TIME_TAG, "同步联系人耗时:" + (eTime - bTime) + " ms");

                CommonPreferences.saveSyncContactsCount(CommonPreferences
                        .getSyncContactsCount() + 1);
            }
        });
    }

    /**
     * 三者比较
     */

    public synchronized void sync() {
        // 1. 获取三方联系人列表（云端联系人列表、手信列表人列表、本地联系人列表）
        // 2. 对本地联系人和手信联系人采用CM1比较器进行排序后再进行比较
        // 3. 对手信联系人进行后处理（将临时列表加到手信联系人列表）
        // 4. 对手信联系人和云端联系人采用CM2进行排序，然后在使用CM2比较器进行比较
        // 5. 对手信联系人列表和上传云端联系人列表进行后处理（两个临时列表的处理）
        // 6. 写本地通讯录，写手信通讯录
        // 7. 云端联系人操作和对操作结果进行本地处理（将云端id写入手信数据库）
        // 8. 获取状态、简单信息、黑名单信息

        // 如果获取云端联系人抛出异常,不要做同步处理
        SLog.d(TAG, "同步联系人:业务开始同步联系人...");

        /*--------------------------------未绑定状态判断 -----------------------------------------***/
        if (!SettingsPreferences.BIND_LOCAL.equals(SettingsPreferences.getBindStatus())) {

            // 绑定到其他手机
            if (SettingsPreferences.BIND_OTHER.equals(SettingsPreferences.getBindStatus())) {
                SLog.w(TAG, "同步联系人:对方未确认换绑!结束!");
                return;
            } else if (SettingsPreferences.UNBIND.equals(SettingsPreferences.getBindStatus())) {
                // 新注册的用户可以做同步操作
                if ("REGISTER-SUCCESS".equalsIgnoreCase(SettingsPreferences.getBindMessage())) {
                    SLog.d(TAG, "同步联系人:新注册的用户允许同步操作...");
                } else {
                    // 未绑定
                    SLog.i(TAG, "同步联系人:未绑定!结束!");

                    return;

                }
            } else {
                SLog.w(TAG, "同步联系人:未知的绑定状态!结束!");
                return;
            }
        }

        long startTime = System.currentTimeMillis();
        MainApp.i().setStatusSyncContacts(true);
        MainApp.i().setLastSyncTime(System.currentTimeMillis());
        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_SYNC_BEGIN, null);
        processObserver.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS,
                Constants.SYNC_PROCESS_CONTACTS_BEGIN);

        // 获取当前云端联系人列表
        boolean needCloudOperate = false;
        ArrayList<Contact> contactsCloud = null;
        try {
            if (!MainApp.isLoggedIn()) {
                throw new NoNeedSendRequestException("未登录");
            }
            contactsCloud = contactsNetModule.getContactsList(processObserver);
            ComparatorFactory.SortContactList(contactsCloud,
                    ComparatorFactory.getCM2ComparatorForSort(), true);
            needCloudOperate = true;
        }/*
          * catch (NoNeedSendRequestException ne) { // 如果未登录取消同步 SLog.e(TAG,
          * "用户未登录不能从云端获取联系人!"); return; }
          */catch (Exception e) {
            SLog.e(TAG, "同步联系人:获取云端联系人列表失败!" + e.getMessage());
            contactsCloud = new ArrayList<Contact>();
            needCloudOperate = false;
        }

        // 获取当前手信联系人列表
        ArrayList<Contact> contactsShouxin = null;
        try {
            contactsShouxin = contactsDAO.getContactsListForSync();
            ComparatorFactory
                    .SortContactList(contactsShouxin,
                            ComparatorFactory.getCM1Comparator(), true);
        } catch (Exception e) {
            SLog.e(TAG, "同步联系人:获取手信联系人列表失败!" + e.getMessage());
            contactsShouxin = new ArrayList<Contact>();
        }

        // 获取当前本地通讯录联系人列表
        ArrayList<Contact> contactsLocal = null;
        try {
            contactsLocal = contactsDAO.getContactsLocalList();
            ComparatorFactory.SortContactList(contactsLocal, ComparatorFactory.getCM1Comparator(),
                    true);

        } catch (Exception e) {
            SLog.e(TAG, "同步联系人:获取本地通讯录联系人列表失败!" + e.getMessage());
            contactsLocal = new ArrayList<Contact>();
        }

        processObserver.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS,
                Constants.SYNC_PROCESS_CONTACTS_SETP2);

        SLog.i(TAG, "同步联系人:获取联系人全部完成[" + (float) (System.currentTimeMillis() - startTime) / 1000
                + " s]! "
                + "云端[" + contactsCloud.size() + "],"
                + "手信[" + contactsShouxin.size() + "],"
                + "本地[" + contactsLocal.size() + "]");

        // 处理本地联系人
        startTime = System.currentTimeMillis();
        SLog.d(TAG, "同步联系人:本地和手信比较开始... ");

        // 操作手信的集合
        final HashSet<Contact> operateShouXin = new HashSet<Contact>();
        // addList专门存放增加到手信的本地联系人
        final ArrayList<Contact> addList = new ArrayList<Contact>();
        CompCallBack cbCM1 = new CompCallBack() {
            @Override
            public void onCmpMore(Contact lhs, Contact rhs) {
                // 如果rhs中有电话号码
                if (rhs.getLocalContactId() > 0) {
                    rhs.setActionShouXin(Contact.ACTION_LOACL_DELETE);
                    rhs.setSynced(ContactsColumns.SYNC_NO);
                    rhs.setDeleted(ContactsColumns.DELETED_YES);
                    operateShouXin.add(rhs);
                }
            }

            @Override
            public void onCmpLess(Contact lhs, Contact rhs) {
                lhs.setActionShouXin(Contact.ACTION_LOACL_ADD);
                lhs.setSynced(ContactsColumns.SYNC_NO);
                addList.add(lhs);
                operateShouXin.add(lhs);
            }

            @Override
            public void onCmpEqual(Contact lhs, Contact rhs) {
                rhs.setLocalContactId(lhs.getLocalContactId());
                rhs.setActionShouXin(Contact.ACTION_SHOUXIN_LINK_LOCAL);
                rhs.setSynced(ContactsColumns.SYNC_YES);
                operateShouXin.add(rhs);
            }

            @Override
            public void onCmpLTail(Contact lhs) {
                lhs.setActionShouXin(Contact.ACTION_LOACL_ADD);
                lhs.setSynced(ContactsColumns.SYNC_NO);
                addList.add(lhs);
                operateShouXin.add(lhs);
            }

            @Override
            public void onCmpRTail(Contact rhs) {
                // 判断当前绑定状态是否正常,如果正常，则说明当前用户没有换卡，可以执行对手信联系人的删除操作
                if (SettingsPreferences.BIND_LOCAL.equals(SettingsPreferences.getBindStatus())) {
                    // 如果rhs中有电话号码
                    if (rhs.getLocalContactId() > 0) {
                        rhs.setActionShouXin(Contact.ACTION_LOACL_DELETE);
                        rhs.setSynced(ContactsColumns.SYNC_NO);
                        rhs.setDeleted(ContactsColumns.DELETED_YES);
                        operateShouXin.add(rhs);
                    }
                }
            }
        };
        // 比较本地联系人和手信联系人
        ComparatorFactory.ComparaTwoList(contactsLocal, contactsShouxin,
                ComparatorFactory.getCM1Comparator(), cbCM1);
        // 将addlist联系人加入手信联系人列表
        contactsShouxin.addAll(addList);
        SLog.d(TAG, "同步联系人:本地和手信比较结束![" + (float) (System.currentTimeMillis() - startTime) / 1000
                + " s],size=" + contactsShouxin.size());

        startTime = System.currentTimeMillis();
        SLog.d(TAG, "同步联系人:云端和手信比较开始..");

        // 需要云端新增的联系人
        final HashSet<Contact> operateCloud = new HashSet<Contact>();

        CompCallBack cbCM2 = new CompCallBack() {

            @Override
            public void onCmpEqual(Contact lhs, Contact rhs) {
                // 如果手信有此联系人 那么与云端进行关联，手信没有此联系，则直接添加到手信
                if (rhs.getId() > 0) {
                    // 如果之前标示删除那么应该被删除
                    if (rhs.getAction() == Contact.ACTION_LOACL_DELETE
                            || rhs.getDeleted() == ContactsColumns.DELETED_YES) {
                        rhs.setSynced(ContactsColumns.SYNC_NO);
                        rhs.setActionShouXin(Contact.ACTION_LOACL_DELETE);
                        operateShouXin.add(rhs);
                        rhs.setAction(Contact.ACTION_DELETE);
                        operateCloud.add(rhs);
                    } else {
                        // 手信中有,是手信跟云端关联
                        rhs.setActionShouXin(Contact.ACTION_SHOUXIN_LINK_CLOUD);
                        rhs.setSynced(ContactsColumns.SYNC_YES);
                        operateShouXin.add(rhs);
                    }
                } else {
                    // 手信和云端都有 手信中是本地拷贝的,写手信 不写本地 之前是 ACTION_LOCAL_ADD
                    rhs.setActionShouXin(Contact.ACTION_CLOUD_AND_LOCAL);
                    rhs.setSynced(ContactsColumns.SYNC_YES);
                    operateShouXin.add(rhs);
                }
                rhs.setAccounts(lhs.getAccounts());
                rhs.setCloudId(lhs.getCloudId());
                // 把手信的类型替换成云端的类型
                rhs.setUserType(lhs.getUserType());
            }

            @Override
            public void onCmpMore(Contact lhs, Contact rhs) {
                // 也有可能是被删除的 ACTION_LOACL_DELETE
                if (rhs.getDeleted() == ContactsColumns.DELETED_YES
                        || rhs.getAction() == Contact.ACTION_LOACL_DELETE) {
                    rhs.setActionShouXin(Contact.ACTION_LOACL_DELETE);
                    operateShouXin.add(rhs);
                    rhs.setAction(Contact.ACTION_DELETE);
                    operateCloud.add(rhs);
                } else {
                    rhs.setAction(Contact.ACTION_ADD);
                    rhs.setSynced(ContactsColumns.SYNC_NO);
                    operateCloud.add(rhs);
                }
            }

            @Override
            public void onCmpLess(Contact lhs, Contact rhs) {
                lhs.setActionShouXin(Contact.ACTION_ADD);
                lhs.setSynced(ContactsColumns.SYNC_YES);
                operateShouXin.add(lhs);
            }

            @Override
            public void onCmpLTail(Contact lhs) {
                lhs.setActionShouXin(Contact.ACTION_ADD);
                lhs.setSynced(ContactsColumns.SYNC_YES);
                operateShouXin.add(lhs);
            }

            @Override
            public void onCmpRTail(Contact rhs) {
                // 也有可能是被删除的 ACTION_DELETE
                if (rhs.getDeleted() == ContactsColumns.DELETED_YES
                        || rhs.getAction() == Contact.ACTION_LOACL_DELETE) {
                    rhs.setActionShouXin(Contact.ACTION_LOACL_DELETE);
                    operateShouXin.add(rhs);
                    rhs.setAction(Contact.ACTION_DELETE);
                    operateCloud.add(rhs);
                } else {
                    rhs.setAction(Contact.ACTION_ADD);
                    rhs.setSynced(ContactsColumns.SYNC_NO);
                    operateCloud.add(rhs);
                }
            }
        };

        // if (!MainApp.getInstance().isOnline()) {
        // return;
        // }
        if (!contactsCloud.isEmpty()) {
            // 将手信联系人列表重新排序(使用云端比较器)，但是手信列表不能去重
            ComparatorFactory.SortContactList(contactsShouxin,
                    ComparatorFactory.getCM2ComparatorForSort(), false);
            // 将云端和前面比较之后的手信列表进行比较（使用云端比较器）
            ComparatorFactory.ComparaTwoList(contactsCloud, contactsShouxin,
                    ComparatorFactory.getCM2ComparatorForComp(), cbCM2);
        }
        SLog.d(TAG, "同步联系人:云端和手信比较结束[" + (float) (System.currentTimeMillis() - startTime) / 1000
                + " s],size=" + contactsShouxin.size());
        // SLog.e(TAG,
        // " 云端和手信比较结束 ... time =  " + (System.currentTimeMillis() - startTime)
        // + "ms");
        // SLog.e(TAG, "本地和手信比较结束  手信: size = " + contactsShouxin.size());

        // if (!MainApp.getInstance().isOnline()) {
        // return;
        // }
        processObserver.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS,
                Constants.SYNC_PROCESS_CONTACTS_SETP3);

        // 手信联系人写入手信数据库，包括本地通讯录的删除、更新和增加
        startTime = System.currentTimeMillis();
        SLog.d(TAG, "同步联系人:写本地数据库开始... ");
        contactsDAO.batchOperateContact(operateShouXin);
        // SLog.e(TAG,
        // "写本地数据库结束 ... time =  " + (System.currentTimeMillis() - startTime));
        processObserver.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS,
                Constants.SYNC_PROCESS_CONTACTS_SETP4);

        long contactVersion = APPCache.getInstance().getContactVersion();
        if (contactVersion != 0) {
            CommonPreferences.saveContactsLastTimeUpdate(contactVersion);
        }
        // 云端上传，云端操作完成后，对手信数据库进行相应修改
        startTime = System.currentTimeMillis();
        SLog.d(TAG, "同步联系人:云端上传开始... ");
        // boolean isOnLine = MainApp.getInstance().isOnline();
        if (MainApp.isLoggedIn() && needCloudOperate) {
            // contactsCloud 为空标示云端没有更新,
            // 在有更新的情况下,需要上传到云端的联系人比较后会保存在addToCloud列表里面
            if (contactsCloud.isEmpty()) {
                // 去sync表示为NO的加到addToCloud
                for (Contact c : contactsShouxin) {
                    if (c.getSynced() == ContactsColumns.SYNC_NO) {
                        if (c.getDeleted() == ContactsColumns.DELETED_YES) {
                            c.setAction(Contact.ACTION_DELETE);
                        } else {
                            c.setAction(Contact.ACTION_ADD);
                        }
                        operateCloud.add(c);
                    }
                }
            } else {
                // 去Deleted标识为Yes的加到addToCloud,表示本地删除了 那么云端也需要删除
                for (Contact c : contactsShouxin) {
                    if (c.getDeleted() == ContactsColumns.DELETED_YES) {
                        c.setAction(Contact.ACTION_DELETE);
                        operateCloud.add(c);
                    }
                }
            }
            ArrayList<Contact> cloudList = new ArrayList<Contact>();
            for (Contact contact : operateCloud) {
                cloudList.add(contact);
            }
            // 同步云端
            SLog.d(TAG, "同步联系人:同步至云端... ");
            ArrayList<Contact> result = contactsNetModule.uploadContacts(cloudList);
            contactsDAO.beginTransaction();
            for (Contact contact : result) {
                try {
                    if (contact.getSynced() != ContactsColumns.SYNC_YES) {
                        continue;
                    }
                    switch (contact.getAction()) {
                        case Contact.ACTION_ADD: // 新增
                            ContentValues tvalues = new ContentValues();
                            tvalues.put(ContactsColumns.SYNCED, ContactsColumns.SYNC_YES);
                            tvalues.put(ContactsColumns.CLOUD_ID, contact.getCloudId());
                            contactsDAO.updateContactWithValues(tvalues,
                                    ContactsColumns._ID + "=?",
                                    new String[] {
                                    String.valueOf(contact.getId())
                            });
                            break;
                        case Contact.ACTION_DELETE: // 删除
                            contactsDAO.deleteContactForSync(contact.getId());
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            contactsDAO.endTransaction(true);
            SLog.d(TAG, "同步联系人:同步至云端正常结束!");
        } else {
            SLog.w(TAG, "未登陆或获取云端联系人失败, 不需要同步到云端!");
        }
        SLog.i(TAG, "同步联系人:云端完成[" + (float) (System.currentTimeMillis() - startTime) / 1000 + " s]");

        processObserver.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS,
                Constants.SYNC_PROCESS_CONTACTS_SETP5);
        startTime = System.currentTimeMillis();
        SLog.d(TAG, "同步联系人:获取状态,简单信息,黑名单信息开始... ");
        operateCloudContacts();
        SLog.d(TAG,
                "\tzhang同步联系人:获取状态,简单信息,黑名单信息结束 ["
                        + (float) (System.currentTimeMillis() - startTime)
                        / 1000 + " s]");
        MainApp.i().setStatusSyncContacts(false);
        processObserver.notifyObserver(CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS,
                Constants.SYNC_PROCESS_CONTACTS_SETP6);
        SLog.i(TAG, "\tzhang同步联系人全部完成!结束!");
    }

    /**
     * 获取状态,简单信息,黑名单信息
     */
    private void operateCloudContacts() {

        if (!MainApp.isLoggedIn()) {
            return;
        }
        // 开始获取联系人状态
        getContactStauts();
        SLog.e(TAG, " 获取状态 !!!!!!!! ");
        // 开始获取联系人简单信息
        ArrayList<Contact> simpleInfoList = getContactSimpleInfo();

        SLog.e(TAG, " 获取简单信息结束 !!!!!!!! size : " + simpleInfoList.size());
        // 修改联系人账号信息 和头像信息
        ArrayList<Contact> contacts = contactsDAO.getContactInfoForList();
        HashMap<Long, Contact> contactMap = new HashMap<Long, Contact>();
        for (Contact contact : contacts) {
            contactMap.put(contact.getCloudId(), contact);
        }

        // TODO 或者使用SQL批量将ACCOUNTS表中的SKYID>0的数据更新
        // 2012-11-15 @bluestome.zhang 修改redmine bug: #16018,#16019 处理方式优化
        // 1.将Contacts表中的手信联系人类型批量更新为本地用户类型
        long bStart = System.currentTimeMillis();
        contactsDAO.beginTransaction();
        ContentValues tmpValues = new ContentValues();
        tmpValues.put(ContactsColumns.USER_TYPE, ContactsColumns.USER_TYPE_LOACL);
        contactsDAO.updateContactWithValues(tmpValues, ContactsColumns.USER_TYPE + "=?",
                new String[] {
                    String.valueOf(ContactsColumns.USER_TYPE_SHOUXIN)
                });
        contactsDAO.endTransaction(true);
        SLog.d(TAG, "\tzhang 批量更新Contacts手信用户为本地用户表耗时:" + (System.currentTimeMillis() - bStart));
        // 2.将Account表中的4个和手信用户相关的字段批量置空
        bStart = System.currentTimeMillis();
        contactsDAO.beginTransaction();
        contactsDAO.batchResetAccount();
        contactsDAO.endTransaction(true);
        SLog.d(TAG, "\tzhang 批量更新重置Accounts表中手信用户数据耗时:" + (System.currentTimeMillis() - bStart));

        HashMap<String, Account> accountPhoneMap = new HashMap<String, Account>();
        SparseArray<Account> accountSkyIdMap = new SparseArray<Account>();
        contactsDAO.beginTransaction();
        for (Contact contact : simpleInfoList) {
            ContentValues values = new ContentValues();
            values.put(ContactsColumns.USER_TYPE, ContactsColumns.USER_TYPE_SHOUXIN);
            values.put(ContactsColumns.PHOTO_ID, contact.getPhotoId());
            // TODO 将简单信息中获取的联系人签名更新到联系人数据中
            if (!StringUtil.isBlank(contact.getSignature())) {
                values.put(ContactsColumns.SIGNATURE, contact.getSignature());
            } else {
                // 2012-11-05 新增处理签名为空的情况 QC_BUG #102
                values.put(ContactsColumns.SIGNATURE, "");
            }
            contactsDAO.updateContactWithValues(
                    values,
                    ContactsColumns.CLOUD_ID + "=?",
                    new String[] {
                            String.valueOf(contact.getCloudId())
                    });
            SLog.d(TAG, "\tzhang 名称:" + contact.getDisplayname() + "|签名:" + contact.getSignature()
                    + "|cloudId:" + contact.getCloudId()
                    + "|contactId:" + contact.getId());
            for (Account account : contact.getAccounts()) {
                int skyId = account.getSkyId();
                String phone = account.getPhone();
                SLog.d(TAG,
                        "\tzhang 更新帐号信息:skyId:" + skyId + "|phone:" + phone + "|skyName:"
                                + account.getSkyAccount() + "|nickName:" + account.getNickName());
                if (accountPhoneMap.get(phone) == null) {
                    SLog.d(TAG, "\tzhang 根据手机号码修改：" + phone + ",account:" + account.toString());
                    if (contactsDAO.updateAccountByPhone(account) > 0) {
                        accountPhoneMap.put(phone, account);
                    }
                }
                if (accountSkyIdMap.get(skyId) == null) {
                    SLog.d(TAG, "\tzhang 根据SKYID修改：" + skyId + ",account:" + account.toString());
                    Contact c = contactMap.get(contact.getCloudId());
                    if (c != null) {
                        account.setContactId(c.getId());
                    }
                    if (contactsDAO.updateAccountBySkyIdAndContactId(account) > 0) {
                        SLog.d(TAG,
                                "\tzhang 根据SKYID和CONTACTID修改 记录成功!");
                    } else {
                        if (contactsDAO.updateAccountByContactId(account) > 0) {
                            SLog.d(TAG,
                                    "\tzhang 根据CONTACTID修改 记录成功!");
                        }
                    }
                }
            }
        }
        contactsDAO.endTransaction(true);

        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END, null);
        ArrayList<Contact> blackList = new ArrayList<Contact>();
        try {
            blackList = getStrangerBlacklistContactList();
        } catch (Exception e) {
        }
        if (!MainApp.i().isOnline()) {
            return;
        }
        contactsDAO.addStangerBlacklistContacts(blackList);
        SLog.e(TAG, " 黑名单联系人处理结束 !!!!!!!! ");
    }

    /**
     * 获取联系人列表相关信息
     * 
     * @return
     */
    public ArrayList<Contact> getContactInfoForList() {
        return contactsDAO.getContactInfoForList();
    }

    /**
     * 获取黑名单
     * 
     * @return
     */
    public ArrayList<Contact> getContactsBlackList() {
        return contactsDAO.getContactsBlackList(false);
    }

    /**
     * 根据ID获取联系人
     * 
     * @param id
     * @return
     */
    public Contact getContactById(final long id) {
        return contactsDAO.getContactById(id);
    }

    /**
     * 删除指定联系人
     * 
     * @param l
     */
    public void deleteContactByID(final long id) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                deleteContactById(id);
            }
        });
    }

    public void deleteContactById(final long id) {
        final Contact contact = contactsDAO.getContactById(id);
        if (contact == null) {
            service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT, null);
            return;
        } else {
            if (contact.getUserType() == ContactsColumns.USER_TYPE_STRANGER) {
                contactsDAO.deleteContactForSync(id);
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT, contact);
                return;
            }
            // 未同步的本地联系人，直接删除
            else if (contact.getUserType() == ContactsColumns.USER_TYPE_LOACL
                    && contact.getCloudId() == 0) {
                contactsDAO.deleteContactForSync(id);
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT, contact);
                return;
            }
            contactsDAO.deleteContact(contact, true);
            service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT, contact);
            Contact result = contactsNetModule.deleteContact(contact);
            if (result != null) {
                contactsDAO.deleteContactForSync(id);
            }
        }
    }

    /**
     * 批量删除联系人
     * 
     * @param l
     */
    public void deleteContacts(final ArrayList<Contact> contacts) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Contact> list = new ArrayList<Contact>();
                for (Contact contact : contacts) {
                    contact.setAction(Contact.ACTION_DELETE);
                    list.add(contact);
                    if (list.size() == 10) {
                        contactsDAO.deleteContact(list);
                        service.notifyObservers(
                                ContactMutilDeleteListActivity.MULTI_DELETE_CONTACT, list);
                        list = new ArrayList<Contact>();
                    }
                }
                if (!list.isEmpty()) {
                    contactsDAO.deleteContact(list);
                    service.notifyObservers(
                            ContactMutilDeleteListActivity.MULTI_DELETE_CONTACT, list);
                }
                service.notifyObservers(ContactMutilDeleteListActivity.MUL_DELETE_CONTACT_END, null);
                try {
                    ArrayList<Contact> result = contactsNetModule.uploadContacts(contacts);
                    for (Contact c : result) {
                        contactsDAO.deleteContactForSync(c.getId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 添加联系人
     * 
     * @param contact
     */
    public void addContact(final Contact contact, final boolean isSync) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                long id = contactsDAO.addContact(contact);
                if (isSync) {
                    Contact cloudContact = contactsNetModule.addContact(contact);
                    if (cloudContact != null) {
                        contactsDAO.updateContactWithAccounts(cloudContact);
                        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END,
                                cloudContact);
                        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_EDIT_ADD_SUCCESS, id);
                    } else {
                        // 　添加到云端失败,删除之前添加的联系人
                        contactsDAO.deleteContact(id, true);
                        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_EDIT_ADD_FAIL, null);
                    }
                } else {
                    contact.setId(id);
                    service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END,
                            contact);
                }
            }
        });
    }

    public void addContactForResult(final Contact contact, final boolean isSync) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                long id = contactsDAO.addContact(contact);
                contact.setId(id);
                contact.setDeleted(ContactsColumns.DELETED_NO);
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS,
                        contact);
                if (isSync) {
                    Contact cloudContact = contactsNetModule.addContact(contact);
                    if (cloudContact != null) {
                        contactsDAO.updateContactWithAccounts(cloudContact);
                    }
                }
            }
        });
    }

    /**
     * 修改联系人
     * 
     * @param contact
     * @return
     */
    public void updateContact(final Contact contact) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 修改为先提交到云端，云端提交成功后，在保存到本地。
                Contact cloudContact = contactsNetModule.updateContact(
                        contact);
                if (cloudContact != null) {
                    // 云端提交成功，执行本地保存操作
                    contact.setSynced(0);
                    contactsDAO.updateContactWithAccounts(contact);
                    ContentValues values = new ContentValues();
                    values.put(ContactsColumns.PHOTO_ID, cloudContact.getPhotoId());
                    values.put(ContactsColumns.BIRTHDAY, cloudContact.getBirthday());
                    values.put(ContactsColumns.HOMETOWN, cloudContact.getHometown());
                    values.put(ContactsColumns.ORGANIZATION, cloudContact.getOrganization());
                    values.put(ContactsColumns.SCHOOL, cloudContact.getSchool());
                    values.put(ContactsColumns.SEX, cloudContact.getSex());
                    values.put(ContactsColumns.SIGNATURE, cloudContact.getSignature());
                    values.put(ContactsColumns.SYNCED, cloudContact.getSynced());
                    values.put(ContactsColumns.USER_TYPE, cloudContact.getUserType());
                    contactsDAO.updateContactWithValues(values, ContactsColumns._ID + "=?",
                            new String[] {
                            String.valueOf(cloudContact.getId())
                    });
                    ArrayList<Account> accounts = new ArrayList<Account>();
                    ArrayList<Account> oldAccounts = cloudContact.getAccounts();
                    Comparator<Account> accountComparator = ComparatorFactory
                            .getAccountComparator();
                    for (Account account : oldAccounts) {
                        if (!ListUtil.contains(accounts, account, accountComparator)) {
                            accounts.add(account);
                        } else {
                            Account a = ListUtil.getObject(accounts, account, accountComparator);
                            int skyId = account.getSkyId();
                            String phone = account.getPhone();
                            if (skyId != 0 && !TextUtils.isEmpty(phone)) {
                                a.setSkyId(skyId);
                                a.setPhone(phone);
                            }
                        }
                    }
                    cloudContact.setAccounts(accounts);
                    contactsDAO.updateContactAccounts(cloudContact);
                    service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END,
                            cloudContact);
                } else {
                    // 如果网络不通，则写本地
                    contact.setSynced(0);
                    contactsDAO.updateContactWithAccounts(contact);
                }
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_EDIT_UPDATE_SUCCESS,
                        contact.getId());
            }
        });
    }

    /**
     * 加入黑名单
     * 
     * @param contact
     * @return
     */
    public void addContactToBlackList(final long contactId) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Contact contact = getContactById(contactId);
                ArrayList<Account> accounts = contact.getAccounts();
                int skyId = 0;
                for (Account account : accounts) {
                    if (account.getSkyId() > 0) {
                        skyId = account.getSkyId();
                        break;
                    }
                }
                int result = contactsNetModule.addToBlackList((int) contact.getCloudId(), skyId);
                if (ContactsNetModule.NET_SUCCESS == result) {
                    ContentValues values = new ContentValues();
                    values.put(ContactsColumns.SYNCED, ContactsColumns.SYNC_YES);
                    values.put(ContactsColumns.BLACK_LIST, ContactsColumns.BLACK_LIST_YES);
                    contactsDAO.updateContactWithValues(values, ContactsColumns._ID + "=?",
                            new String[] {
                            String.valueOf(contactId)
                    });
                }
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_BLACKLIST_ADD, result);
            }
        });
    }

    /**
     * 解除黑名单
     * 
     * @param contactId
     * @return
     */
    public void removeContactFromBlackList(final long contactId) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Contact contact = removeContactFromBlacklist(contactId);
                service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_BLACKLIST_REMOVE, contact);
            }
        });
    }

    private Contact removeContactFromBlacklist(long contactId) {
        Contact contact = getContactById(contactId);
        if (contact == null) {
            return null;
        }
        int cloudId = 0, skyId = 0;
        if (contact.getCloudId() != 0) {
            cloudId = (int) contact.getCloudId();
        } else {
            if (contact.getUserType() != ContactsColumns.USER_TYPE_LOACL) {
                ArrayList<Account> accounts = contact.getAccounts();
                for (Account account : accounts) {
                    skyId = account.getSkyId();
                    if (skyId > 0) {
                        break;
                    }
                }
            }
        }
        int result = contactsNetModule.removeFromBlackList(cloudId, skyId);
        contact.setAction(result);
        if (ContactsNetModule.NET_SUCCESS == result) {
            boolean isExist = DaoFactory.getInstance(MainApp.i().getApplicationContext())
                    .getFriendsDAO().checkFriendExistByContactId(contactId);
            if (!isExist && contact.getUserType() != ContactsColumns.USER_TYPE_SHOUXIN) {
                contactsDAO.deleteContactForSync(contactId);
            } else {
                ContentValues values = new ContentValues();
                contact.setBlackList(ContactsColumns.BLACK_LIST_NO);
                contact.setSynced(ContactsColumns.SYNC_YES);
                values.put(ContactsColumns.SYNCED, ContactsColumns.SYNC_YES);
                values.put(ContactsColumns.BLACK_LIST, ContactsColumns.BLACK_LIST_NO);
                contactsDAO.updateContactWithValues(values, ContactsColumns._ID + "=?",
                        new String[] {
                        String.valueOf(contactId)
                });
            }
        }

        return contact;
    }

    /**
     * 从黑名单删除
     * 
     * @param contactID
     * @return
     */
    public void deleteFromBlackList(final long contactId) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Contact result = removeContactFromBlacklist(contactId);
                if (result != null && ContactsNetModule.NET_SUCCESS == result.getAction()) {
                    deleteContactById(contactId);
                    service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT_BLACKLIST,
                            result);
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT_BLACKLIST,
                            null);
                }
            }
        });
    }

    /**
     * 清空黑名单
     */
    public void clearBlackList() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Contact> blackList = contactsDAO.getContactsBlackList(true);
                SLog.d(TAG, " blackList: " + blackList);
                int result = ContactsNetModule.NET_ERR;
                for (Contact contact : blackList) {
                    long cloudId = contact.getCloudId(), skyId = 0;
                    try {
                        skyId = contact.getAccounts().get(0).getSkyId();
                        SLog.d(TAG, " blackList: skyid" + skyId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result = contactsNetModule.removeFromBlackList((int) cloudId, (int) skyId);
                    if (ContactsNetModule.NET_SUCCESS == result) {
                        deleteContactById(contact.getId());
                    }
                }
                service.notifyObservers(ContactBlackListActivity.MENU_CLEAR_END, result);
            }
        });
    }

    /**
     * 获取全部联系人状态
     * 
     * @return
     */
    public void getContactStauts() {
        try {
            if (!MainApp.isLoggedIn()) {
                return;
            }
            contactsNetModule.getAllContactStatusList();
            service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS, null);
        } catch (NullPointerException e) {
            Log.w(TAG, "getContactStauts failure");
        }
    }

    /**
     * 获取全部联系人简单信息
     * 
     * @return
     * @return
     */
    public ArrayList<Contact> getContactSimpleInfo() {
        return contactsNetModule.getAllContactSimpleInfo();
    }

    /**
     * 获取陌生人列表
     * 
     * @return
     */
    public ArrayList<Contact> getStrangerBlacklistContactList() {
        return contactsNetModule.getStrangerBlacklistContactList();
    }

    public boolean isContact(List<VCardContent> cardList, String skyId) {
        long contactId = contactsDAO.getAccountByPhoneOrSkyId(null, skyId);
        if (contactId > 0) {
            return true;
        }

        for (VCardContent card : cardList) {
            contactId = contactsDAO.getAccountByPhoneOrSkyId(card.getPhone(), null);
            if (contactId > 0) {
                return true;
            }
        }
        return false;
    }

    public void getContactBySkyID(final long contactId, final int skyId) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Contact contact = contactsNetModule.getContactBySkyID(skyId);
                if (null != contact) {
                    Contact databaseContact = contactsDAO.getContactById(contactId);
                    contact.setId(contactId);
                    ContentValues values = new ContentValues();
                    values.put(ContactsColumns.BIRTHDAY, contact.getBirthday());
                    values.put(ContactsColumns.HOMETOWN, contact.getHometown());
                    values.put(ContactsColumns.ORGANIZATION,
                            contact.getOrganization());
                    values.put(ContactsColumns.PHOTO_ID, contact.getPhotoId());
                    values.put(ContactsColumns.SCHOOL, contact.getSchool());
                    values.put(ContactsColumns.SEX, contact.getSex());
                    values.put(ContactsColumns.SIGNATURE, contact.getSignature());
                    values.put(ContactsColumns.SYNCED, ContactsColumns.SYNC_YES);
                    ArrayList<Account> accounts = contact.getAccounts();
                    String nickName = "";
                    if (accounts != null && !accounts.isEmpty()) {
                        Account netAccount = accounts.get(0);
                        Account account = new Account();
                        account.setMain(1);
                        account.setContactId(contactId);
                        account.setSkyAccount(netAccount.getSkyAccount());
                        account.setSkyId(netAccount.getSkyId());
                        account.setNickName(nickName = netAccount.getNickName());
                        account.setPhone(netAccount.getPhone());
                        contactsDAO.updateAccountBySkyIdAndContactId(account);
                    }
                    if (databaseContact != null
                            && TextUtils.isEmpty(databaseContact.getDisplayname())) {
                        values.put(ContactsColumns.DISPLAY_NAME, nickName);
                    }
                    contactsDAO.updateContactWithValues(values, ContactsColumns._ID + "=?",
                            new String[] {
                            String.valueOf(contactId)
                    });
                    contact = contactsDAO.getContactById(contactId);
                    if (contact != null) {
                        service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_DETAIL_SUCCESS, contact);
                    }
                }
            }
        });
    }

    /**
     * 获取联系人表中所有的有头像的用户
     * 
     * @return
     */
    public ArrayList<Contact> getContactInfoForPhoto() {
        return contactsDAO.getContactInfoForPhoto();
    }

    /**
     * 获取用户所有的skyid数组
     */
    public HashSet<Integer> getSkyIDs() {
        return contactsDAO.getSkyIds();
    }

    public ArrayList<Contact> getContactBySkyid(int skyid) {
        return contactsDAO.getContactBySkyid(skyid);
    }

    /**
     * 根据手机号码查找云端的ID
     * 
     * @param phone
     * @return
     */
    public ArrayList<Contact> getCloudIdByPhone(String phone) {
        ArrayList<Contact> list = new ArrayList<Contact>();
        // 先获取ACCOUNTID
        ArrayList<Long> ids = contactsDAO.getAccountIdByPhone(phone);
        // 在通过ACCOUNTID查找Contacts表中的cloudid
        if (null != ids && ids.size() > 0) {
            for (long cid : ids) {
                ArrayList<Contact> list2 = contactsDAO.getContactByAccountIds(String.valueOf(cid));
                if (null != list2 && list2.size() > 0) {
                    list.addAll(list2);
                }
            }
        }
        return list;
    }

    /**
     * 获取指定联系人简单信息
     * 
     * @param contactId 联系人ID
     * @return
     */
    public ArrayList<Contact> getContactSimpleInfoByContactId(int contactId) {
        return contactsNetModule.getContactSimpleInfoByContactId(contactId);
    }

    /**
     * 更新帐号
     * 
     * @param simpleInfoList
     */
    public void updateContact2(ArrayList<Contact> simpleInfoList) {
        contactsDAO.beginTransaction();
        for (Contact contact : simpleInfoList) {
            ContentValues values = new ContentValues();
            values.put(ContactsColumns.USER_TYPE, ContactsColumns.USER_TYPE_SHOUXIN);
            values.put(ContactsColumns.PHOTO_ID, contact.getPhotoId());
            // TODO 将简单信息中获取的联系人签名更新到联系人数据中
            if (!StringUtil.isBlank(contact.getSignature())) {
                values.put(ContactsColumns.SIGNATURE, contact.getSignature());
            }
            contactsDAO.updateContactWithValues(values,
                    ContactsColumns.CLOUD_ID + "=?", new String[] {
                        String.valueOf(contact.getCloudId())
                    });
            for (Account account : contact.getAccounts()) {
                contactsDAO.updateAccountByPhone(account);
            }
        }
        contactsDAO.endTransaction(true);
    }

    /**
     * 清楚帐号信息
     * 
     * @param simpleInfoList
     */
    public void clearContact(ArrayList<Contact> simpleInfoList, String phone) {
        contactsDAO.beginTransaction();
        for (Contact contact : simpleInfoList) {
            ContentValues values = new ContentValues();
            values.put(ContactsColumns.USER_TYPE, ContactsColumns.USER_TYPE_LOACL);
            values.put(ContactsColumns.PHOTO_ID, "");
            // TODO 将简单信息中获取的联系人签名更新到联系人数据中
            if (!StringUtil.isBlank(contact.getSignature())) {
                values.put(ContactsColumns.SIGNATURE, contact.getSignature());
            }
            contactsDAO.updateContactWithValues(values,
                    ContactsColumns.CLOUD_ID + "=?", new String[] {
                        String.valueOf(contact.getCloudId())
                    });
            for (Account account : contactsDAO.getAccountByContactId(contact.getId())) {
                account.setNickName(null);
                account.setSkyAccount(null);
                account.setSkyId(0);
                account.setPhone(phone);
                account.setMain(0);
                contactsDAO.updateAccount(account);
            }
        }
        contactsDAO.endTransaction(true);
    }

    /**
     * 获取指定联系人ID的状态
     * 
     * @param contactId
     */
    public void getContactStatus(int contactId) {
        try {
            if (!MainApp.isLoggedIn()) {
                return;
            }
            contactsNetModule.getContactsStatusByContactId(contactId);
            service.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS, null);
        } catch (NullPointerException e) {
            Log.w(TAG, "getContactStauts failure");
        }

    }

}
