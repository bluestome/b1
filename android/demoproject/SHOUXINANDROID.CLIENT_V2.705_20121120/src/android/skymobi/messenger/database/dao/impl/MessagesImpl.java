
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.bean.SMSThreads;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.database.dao.AddressDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.database.dao.MessagesDAO;
import android.skymobi.messenger.exception.MessengerException;
import android.skymobi.messenger.provider.SocialMessenger.AddressColumns;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.provider.SocialMessenger.ThreadsColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.Observer;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.TimeUtils;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @ClassName: MessagesImpl
 * @Description: 消息实现
 * @author Sean.Xie
 * @date 2012-2-9 上午11:13:33
 */
public class MessagesImpl extends BaseImpl implements MessagesDAO {

    public MessagesImpl(Context context) {
        super(context);
    }

    @Override
    public boolean syncLocalThreads(Observer ob) {
        Cursor cursorLocalThreads = null;
        try {
            cursorLocalThreads = resolver
                    .query(Uri.parse("content://sms/"),
                            new String[] {
                                "t._id,t.date,t.message_count,recipient_ids as phones,t.snippet,t.read,t.snippet_cs from threads t where (select count(thread_id) from sms where thread_id = t._id) > 0 -- "
                            },
                            null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                cursorLocalThreads = resolver
                        .query(Uri.parse("content://sms/"),
                                new String[] {
                                    "t._id,t.date,t.message_count,recpt_id as phones,t.snippet,t.read,t.snippet_cs from threads t where (select count(thread_id) from sms where thread_id = t._id) > 0 -- "
                                },
                                null, null, null);
            } catch (Exception ex) {
            }
        }
        if (null == cursorLocalThreads) {
            Log.i(TAG, "无法获取本地短信会话");
            return false;
        }
        if (ob != null) {
            ob.notifyObserver(CoreServiceMSG.MSG_THREADS_SYNC_BEGIN, null);
        }
        super.beginTransaction();

        StringBuilder shouxinSQL = new StringBuilder();
        shouxinSQL.append("select ").append(ThreadsColumns._ID)
                .append(" as id,").append(ThreadsColumns.DATE)
                .append(" as date,").append(ThreadsColumns._COUNT)
                .append(" as count,").append(ThreadsColumns.CONTENT)
                .append(" as content,").append(ThreadsColumns.READ)
                .append(" as read,").append(ThreadsColumns.LOCAL_THREADS_ID)
                .append(" as localThreadsID,").append(ThreadsColumns.PHONES)
                .append(" as phones,").append(ThreadsColumns.ADDRESS_IDS).append(" as addressIds")
                .append(" from ")
                .append(ThreadsColumns.TABLE_NAME);
        try {
            Map<String, Threads> threadsShouxinMap = query(
                    Threads.class,
                    "AddressIds",
                    shouxinSQL.toString());
            final long beginTime = System.currentTimeMillis();
            // ArrayList<String> sqls = new ArrayList<String>();
            // int maxAddressId = super.getMaxId(AddressColumns.TABLE_NAME,
            // 1000);
            // 将本地短信会话与手信消息会话对比
            Map<Long, SMSThreads> localThreadsMap = new HashMap<Long, SMSThreads>();
            while (cursorLocalThreads.moveToNext()) {
                SMSThreads smsThreads = new SMSThreads();
                smsThreads.setId(cursorLocalThreads.getLong(0));
                smsThreads.setDate(cursorLocalThreads.getLong(1));
                smsThreads.setCount(cursorLocalThreads.getInt(2));
                smsThreads.setPhones(cursorLocalThreads.getString(3));
                smsThreads.setContent(cursorLocalThreads.getString(4));
                smsThreads.setRead(cursorLocalThreads.getInt(5));

                // 目标ID非数字，不处理
                if (!TextUtils.isDigitsOnly(smsThreads.getPhones().replace(" ",
                        ""))) {
                    continue;
                }

                boolean isMMS = 0 != cursorLocalThreads.getInt(6) ? true
                        : false;
                if (isMMS) {
                    Cursor cursorForContent = resolver
                            .query(Uri.parse("content://sms/"),
                                    new String[] {
                                        " body,status,type from sms where thread_id ="
                                                + smsThreads.getId()
                                                + " order by _id desc limit 1 -- "
                                    },
                                    null, null, null);
                    if (cursorForContent.moveToNext()) {
                        smsThreads.setContent(cursorForContent.getString(0));
                        smsThreads.setStatus(AndroidSysUtils.getStatus(cursorForContent.getInt(1),
                                cursorForContent.getInt(2)));
                    }
                    closeCursor(cursorForContent);
                }
                Cursor cursorForAddress = resolver
                        .query(Uri.parse("content://sms/"),
                                new String[] {
                                    "address from canonical_addresses where _id in ("
                                            + smsThreads.getPhones().replace(" ",
                                                    ",") + ") -- "
                                }, null, null,
                                null);
                StringBuilder phones = new StringBuilder();

                AddressDAO addressDAO = DaoFactory.getInstance(context).getAddressDAO();
                List<Address> addressList = new ArrayList<Address>();
                while (cursorForAddress.moveToNext()) {
                    String phone = AndroidSysUtils.removeHeader(cursorForAddress.getString(0));
                    Address address = addressDAO.getAddressByPhone(AndroidSysUtils
                            .removeHeader(phone.replaceAll("'", "''")));
                    if (null == address) {
                        address = new Address();
                        address.setPhone(phone);
                        address.setId(addressDAO.addAddress(address));
                    }
                    addressList.add(address);
                    phones.append(cursorForAddress.getString(0));
                    phones.append(",");
                }
                if (phones.length() > 0) {
                    smsThreads.setPhones(phones.substring(0,
                            phones.length() - 1));
                }

                StringBuilder ids = new StringBuilder();
                String addressIds = "";
                for (Address address : addressList) {
                    ids.append(address.getId()).append(",");
                }
                if (ids.length() > 0) {
                    addressIds = ids.substring(0, ids.length() - 1);
                }
                closeCursor(cursorForAddress);
                localThreadsMap.put(smsThreads.getId(), smsThreads);

                // Address address =
                // addressDAO.getAddressByPhone(smsThreads.getPhones());

                if (null != addressIds
                        && threadsShouxinMap.containsKey(addressIds)) {
                    Threads shouxinThreads = threadsShouxinMap.get(addressIds);
                    // 建立对应关系 同步内容和时间
                    if (shouxinThreads.getRead() != smsThreads
                            .getRead()) {

                        executeSQL(
                                "update "
                                        + ThreadsColumns.TABLE_NAME
                                        + " set "
                                        + ThreadsColumns.LOCAL_THREADS_ID
                                        + "=?,"
                                        + ThreadsColumns.READ
                                        + "=? where "
                                        + ThreadsColumns._ID
                                        + "=? and ((select count("
                                        + MessagesColumns._ID
                                        + ") from "// 1、本地会话已读，网络消息有未读，不更新|2、本地会话未读，更新手信会话
                                        + MessagesColumns.TABLE_NAME + " where "
                                        + MessagesColumns.THREADS_ID + "=? and "
                                        + MessagesColumns.SMS_ID + "=0 and " + MessagesColumns.READ
                                        + "=0)=0 or " + smsThreads.getRead() + "="
                                        + MessagesColumns.READ_NO + ")",
                                new String[] {
                                        smsThreads.getId() + "",
                                        smsThreads.getRead() + "",
                                        shouxinThreads.getId() + "",
                                        shouxinThreads.getId() + ""
                                });
                    }
                } else {
                    ContentValues values = new ContentValues();
                    values.put(ThreadsColumns.CONTENT, smsThreads.getContent());
                    values.put(ThreadsColumns._COUNT, smsThreads.getCount());
                    values.put(ThreadsColumns.DATE, smsThreads.getDate());
                    values.put(ThreadsColumns.LOCAL_THREADS_ID,
                            smsThreads.getId());
                    values.put(ThreadsColumns.PHONES, smsThreads.getPhones());
                    values.put(ThreadsColumns.READ, smsThreads.getRead());
                    values.put(ThreadsColumns.STATUS, smsThreads.getStatus());
                    // Log.e(TAG, "smsThreads.getStatus() = " +
                    // smsThreads.getStatus());
                    // values.put(ThreadsColumns.READ, Message.READ_YES);
                    // values.put(ThreadsColumns.ACCOUNT_IDS, accountID);
                    values.put(ThreadsColumns.ADDRESS_IDS, addressIds);
                    insert(ThreadsColumns.TABLE_NAME, null, values);
                }
            }
            closeCursor(cursorLocalThreads);

            for (Entry<String, Threads> entry : threadsShouxinMap.entrySet()) {
                Threads value = entry.getValue();
                long localThreadsID = value.getLocalThreadsID();

                if (localThreadsID == 0)
                    continue;
                // 用map缓存，可以节省取本地数据库的操作时间
                if (localThreadsMap.get(localThreadsID) == null) {
                    // 加入批处理
                    // sqls.add(_generateDeleteMessageSql(localThreadsID,
                    // value.getId()));
                    // 这里只删除对应的短信，不能删除会话
                    executeSQL("delete from " + MessagesColumns.TABLE_NAME +
                            " where " + MessagesColumns.LOCAL_THREADS_ID + "=? and "
                            + MessagesColumns.TYPE + "=? and " +
                            MessagesColumns.THREADS_ID + "=?", new String[] {
                            localThreadsID + "", MessagesColumns.TYPE_SMS + "",
                            value.getId() + ""
                    });

                }
            }
            // 执行批处理
            // // executeWithTransaction(sqls);
            super.endTransaction(true);
            final long endTime = System.currentTimeMillis();
            SLog.d(TIME_TAG,
                    "同话本地消息会话syncLocalThreads()耗时:"
                            + TimeUtils.getTimeconsuming(beginTime, endTime) + " sec");
        } catch (Exception e) {
            throw new MessengerException(e);
        }
        if (ob != null) {
            ob.notifyObserver(CoreServiceMSG.MSG_THREADS_SYNC_END, null);
        }
        if (ob != null) {
            ob.notifyObserver(CoreServiceMSG.MSG_MESSAGES_SYNC_BEGIN, null);
        }
        syncLocalMessage(false);
        if (ob != null) {
            ob.notifyObserver(CoreServiceMSG.MSG_MESSAGES_SYNC_END, null);
        }
        updateThreadsContent();
        return true;
    }

    /*
     * private String _generateUpdateSMSThreadsSql(final SMSThreads smsThreads,
     * final long shouxinThreadsId) { StringBuffer sql = new StringBuffer("");
     * sql.append("update ").append(ThreadsColumns.TABLE_NAME).append(" set ");
     * sql.append(ThreadsColumns.DATE).append(smsThreads.getDate()).append(",");
     * sql
     * .append(ThreadsColumns.CONTENT).append(toString(smsThreads.getContent()
     * )).append(",");
     * sql.append(ThreadsColumns.LOCAL_THREADS_ID).append(smsThreads
     * .getId()).append(",");
     * sql.append(ThreadsColumns.READ).append(smsThreads.getRead()).append(",");
     * sql
     * .append(" where ").append(ThreadsColumns._ID).append(shouxinThreadsId);
     * SLog.d(SQLTAG, sql.toString()); return sql.toString(); } private String
     * _generateInsertThreadsSql(final SMSThreads smsThreads, final long
     * addressId) { StringBuffer sql = new StringBuffer("");
     * sql.append("insert into ").append(ThreadsColumns.TABLE_NAME);
     * sql.append("("); sql.append(ThreadsColumns.CONTENT).append(",");
     * sql.append(ThreadsColumns._COUNT).append(",");
     * sql.append(ThreadsColumns.DATE).append(",");
     * sql.append(ThreadsColumns.LOCAL_THREADS_ID).append(",");
     * sql.append(ThreadsColumns.PHONES).append(",");
     * sql.append(ThreadsColumns.READ).append(",");
     * sql.append(ThreadsColumns.ADDRESS_IDS); sql.append(")values(");
     * sql.append(toString(smsThreads.getContent())).append(",");
     * sql.append(smsThreads.getCount()).append(",");
     * sql.append(smsThreads.getDate()).append(",");
     * sql.append(smsThreads.getId()).append(",");
     * sql.append(toString(smsThreads.getPhones())).append(",");
     * sql.append(smsThreads.getRead()).append(","); sql.append(addressId);
     * sql.append(")"); SLog.d(SQLTAG, sql.toString()); return sql.toString(); }
     * private String _generateDeleteMessageSql(final long localThreadsID, final
     * long threadsId) { StringBuffer sql = new StringBuffer("");
     * sql.append("delete from "); sql.append(MessagesColumns.TABLE_NAME);
     * sql.append(" where ");
     * sql.append(MessagesColumns.LOCAL_THREADS_ID).append
     * ("=").append(localThreadsID); sql.append(" and ");
     * sql.append(MessagesColumns
     * .TYPE).append("=").append(MessagesColumns.TYPE_SMS); sql.append(" and ");
     * sql.append(MessagesColumns.THREADS_ID).append("=").append(threadsId);
     * SLog.d(SQLTAG, sql.toString()); return sql.toString(); } private String
     * _generateInsertAddressSql(final Address address) { StringBuffer sql = new
     * StringBuffer("");
     * sql.append("insert into ").append(AddressColumns.TABLE_NAME);
     * sql.append("("); sql.append("_id").append(",");
     * sql.append(AddressColumns.PHONE).append(",");
     * sql.append(AddressColumns.SKYID).append(",");
     * sql.append(AddressColumns.ACCOUNTID); sql.append(")values(");
     * sql.append(address.getId()).append(",");
     * sql.append(toString(address.getPhone())).append(",");
     * sql.append(address.getSkyId()).append(",");
     * sql.append(address.getAccountId()); sql.append(")"); SLog.d(SQLTAG,
     * sql.toString()); return sql.toString(); }
     */

    @Override
    public int getThreadsCount() {
        return queryCount(ThreadsColumns.TABLE_NAME, null);
    }

    @Override
    public List<Threads> getThreadsList() {
        return getThreadsList(-1, -1);
    }

    @Override
    public List<Threads> getThreadsList(int start, int count) {
        return getThreadsList(
                start,
                count,
                "t.address_ids=a._id or  t.address_ids like '%,'||substr(a._id,1) or  t.address_ids like substr(a._id,1)||',%' or  t.address_ids like '%,'||substr(a._id,1)||',%'");
    }

    @Override
    public List<Threads> getThreadsList(int start, int count, String whereClause) {
        try {
            StringBuilder shouxinSQL = new StringBuilder();
            shouxinSQL.append("select ").append("t." + ThreadsColumns._ID)
                    .append(" as id,").append("t." + ThreadsColumns.DATE)
                    .append(" as date,").append("t." + ThreadsColumns.READ)
                    .append(" as read,").append("t." + ThreadsColumns._COUNT)
                    .append(" as count,").append("t." + ThreadsColumns.CONTENT)
                    .append(" as content,")
                    .append("t." + ThreadsColumns.LOCAL_THREADS_ID)
                    .append(" as localThreadsID,").append("t." + ThreadsColumns.PHONES)
                    .append(" as phones,").append("t." + ThreadsColumns.ACCOUNT_IDS)
                    .append(" as draft,")
                    .append("t." + ThreadsColumns.DISPLAY_NAME)
                    .append(" as displayName,").append("t." + ThreadsColumns.STATUS)
                    .append(" as status,")
                    .append("t." + ThreadsColumns.ADDRESS_IDS)
                    .append(" as addressIds,").append("a." + AddressColumns._ID)
                    .append(" as addressId,")
                    .append("a." + AddressColumns.PHONE)
                    .append(" as phone,").append("a." + AddressColumns.SKYID)
                    .append(" as skyId")
                    .append(" from ")
                    .append(ThreadsColumns.TABLE_NAME + " t ").append(" CROSS JOIN ")
                    .append(AddressColumns.TABLE_NAME + " a ");
            if (!TextUtils.isEmpty(whereClause)) {
                shouxinSQL.append(" where ").append(whereClause);
            }
            shouxinSQL.append(" order by date desc");
            if (start != -1 && count != -1) {
                shouxinSQL.append(" limit ").append(count).append(" offset ")
                        .append(start);
            }
            return queryWithSort(Threads.class, shouxinSQL.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MessengerException(e);
        }
    }

    @Override
    public long addThreads(Threads threads) {
        if (threads == null)
            return -1;
        ContentValues values = new ContentValues();
        values.put(ThreadsColumns.CONTENT, threads.getContent());
        values.put(ThreadsColumns.DATE, threads.getDate());
        values.put(ThreadsColumns.PHONES, threads.getPhones());
        values.put(ThreadsColumns.ACCOUNT_IDS, threads.getAccountIds());
        values.put(ThreadsColumns.READ, threads.getRead());
        values.put(ThreadsColumns.LOCAL_THREADS_ID, threads.getLocalThreadsID());
        values.put(ThreadsColumns.DISPLAY_NAME, threads.getDisplayName());
        values.put(ThreadsColumns.ADDRESS_IDS, threads.getAddressIds());
        values.put(ThreadsColumns.STATUS, threads.getStatus());
        values.put(ThreadsColumns._COUNT, 1);
        return insert(ThreadsColumns.TABLE_NAME, null, values);
    }

    @Override
    public int updateThreads(Threads threads) {
        if (threads == null || threads.getId() <= 0)
            return 0;
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(threads.getContent())) {
            values.put(ThreadsColumns.CONTENT, threads.getContent());
        }
        if (0 != threads.getDate()) {
            values.put(ThreadsColumns.DATE, threads.getDate());
        }

        if (-1 != threads.getStatus()) {
            values.put(ThreadsColumns.STATUS, threads.getStatus());
        }
        if (-1 != threads.getRead()) {
            values.put(ThreadsColumns.READ, threads.getRead());
        }
        if (0 != threads.getLocalThreadsID()) {
            values.put(ThreadsColumns.LOCAL_THREADS_ID,
                    threads.getLocalThreadsID());
        }

        if (!TextUtils.isEmpty(threads.getDisplayName())) {
            values.put(ThreadsColumns.DISPLAY_NAME, threads.getDisplayName());
        }

        // 使用ACCOUNT_IDS存储草稿
        if (null != threads.getDraft()) {
            values.put(ThreadsColumns.ACCOUNT_IDS, threads.getDraft());
        }

        if (!TextUtils.isEmpty(threads.getAddressIds())) {
            values.put(ThreadsColumns.ADDRESS_IDS, threads.getAddressIds());
        }

        int rows = update(ThreadsColumns.TABLE_NAME, values, "_id=?",
                new String[] {
                    threads.getId() + ""
                });
        return rows;
    }

    @Override
    public boolean updateThreadsReadStauts(long id, int readStatus) {
        ContentValues values = new ContentValues();
        values.put(ThreadsColumns.READ, readStatus);
        int rows = update(ThreadsColumns.TABLE_NAME, values, "_id=?",
                new String[] {
                    id + ""
                });
        values.clear();
        values.put(MessagesColumns.READ, MessagesColumns.READ_YES);
        rows = update(MessagesColumns.TABLE_NAME, values,
                MessagesColumns.THREADS_ID + "=?", new String[] {
                    id + ""
                });
        return rows > 0 ? true : false;
    }

    // @Override
    // public boolean updateThreadsContent(long id, String content) {
    // ContentValues values = new ContentValues();
    // values.put(ThreadsColumns.CONTENT, content);
    // int rows = update(ThreadsColumns.TABLE_NAME, values, "_id=?",
    // new String[] {
    // id + ""
    // });
    // return rows > 0 ? true : false;
    // }

    private boolean updateThreadsByMessage(long id, Message message) {
        if (null == message) {
            return false;
        }
        switch (message.getType()) {
            case MessagesColumns.TYPE_VOICE:
                message.setContent(MainApp.i().getResources()
                        .getString(R.string.message_list_voice_item));
                break;
            case MessagesColumns.TYPE_CARD:
                message.setContent(MainApp.i().getResources()
                        .getString(R.string.message_list_card_item));
                break;
            case MessagesColumns.TYPE_FRD:
                message.setContent(MainApp.i().getResources()
                        .getString(R.string.message_list_frd_item));
                break;
            default:
                break;
        }

        ContentValues values = new ContentValues();
        values.put(ThreadsColumns.CONTENT, message.getContent());
        values.put(ThreadsColumns.DATE, message.getDate());
        values.put(ThreadsColumns.STATUS, message.getStatus());
        int rows = update(ThreadsColumns.TABLE_NAME, values, "_id=?",
                new String[] {
                    id + ""
                });
        return rows > 0 ? true : false;
    }

    @Override
    public boolean updateLocalThreadsReadStauts(long id, int readStatus) {
        ContentValues values = new ContentValues();
        values.put("read", readStatus);
        CoreService.getInstance().getThreadsObserver().setLocked(true);
        int rows = 0;
        if (MessagesColumns.READ_YES == readStatus) {
            rows = resolver.update(Uri.parse("content://sms/conversations/" + id), values, null,
                    null);
        } else {
            // rows = resolver.update(Uri.parse("content://sms/conversations/"),
            // values,
            // "_id=?",
            // new String[] {
            // String.valueOf(id)
            // });

        }
        CoreService.getInstance().getThreadsObserver().setLocked(false);
        return rows > 0 ? true : false;
    }

    @Override
    public int updateLocalSMSReadStautsByThreadId(long threadId, int readStatus) {
        ContentValues values = new ContentValues();
        values.put("read", readStatus);
        CoreService.getInstance().getThreadsObserver().setLocked(true);
        int rows = resolver.update(Uri.parse("content://sms/inbox/"), values, "thread_id=?",
                new String[] {
                    "" + threadId
                });
        CoreService.getInstance().getThreadsObserver().setLocked(false);
        return rows;
    }

    @Override
    public boolean removeThreads(Threads threads) {
        return removeThreads(threads.getId());
    }

    @Override
    public boolean removeThreads(long threadsID) {
        int rows = delete(ThreadsColumns.TABLE_NAME, "_id=?",
                new String[] {
                    threadsID + ""
                });
        return rows > 0 ? true : false;
    }

    @Override
    public boolean removeLocalThreads(long threadsID) {
        CoreService.getInstance().getThreadsObserver().setLocked(true);
        boolean ret = resolver.delete(Uri.parse("content://sms/conversations/" + threadsID), null,
                null) > 0 ? true
                : false;
        CoreService.getInstance().getThreadsObserver().setLocked(false);
        return ret;

    }

    @Override
    public boolean removeLocalThreadsList(String threadsIDs) {
        CoreService.getInstance().getThreadsObserver().setLocked(true);
        boolean ret = resolver.delete(Uri.parse("content://mms-sms/conversations/"), "_id in("
                + threadsIDs + ")",
                null) > 0 ? true
                : false;
        CoreService.getInstance().getThreadsObserver().setLocked(false);
        return ret;

    }

    @Override
    public Threads getThreadsByID(long threadsID) {
        String whereClause = "t." + ThreadsColumns._ID + "=" + threadsID;
        List<Threads> list = getThreadsList(-1, -1, whereClause);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public List<Message> getMessageList() {
        return getMessageList(-1, -1, -1);
    }

    @Override
    public List<Message> getMessageList(long threadsID, int start, int count) {
        List<Message> messages = getMessageList(threadsID, start, count, null);
        for (Message message : messages) {
            if (message.getType() == MessagesColumns.TYPE_VOICE
                    && !TextUtils.isEmpty(message.getContent())) {
                message.setResFile(DaoFactory.getInstance(MainApp.i()).getResfilesDAO()
                        .getFile(Long.valueOf(message.getContent())));
            }
        }
        return messages;
    }

    @Override
    public List<Message> getMessageList(long threadsID, int start, int count,
            String whereClause) {
        try {
            String threadsWhere = " 1=1 ";
            if (threadsID != -1) {
                threadsWhere = MessagesColumns.THREADS_ID + "=" + threadsID;
            }

            if (TextUtils.isEmpty(whereClause)) {
                whereClause = threadsWhere;
            } else {
                whereClause = "(" + whereClause + ") and " + threadsWhere;
            }
            String shouxinSQL = createSelectMessagesSQL(whereClause,
                    // " order by sequence_id asc, sms_id desc,_id asc", start,
                    " order by _id desc", start,
                    count);
            return queryWithSortDESC(Message.class, shouxinSQL.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MessengerException(e);
        }
    }

    @Override
    public Message getMessageByID(long msgID) {
        String whereClause = MessagesColumns._ID + "=" + msgID;
        List<Message> list = getMessageList(-1, -1, -1, whereClause);
        return list.get(0);
    }

    @Override
    public boolean syncLocalMessage(boolean noInsert) {
        String whereClause = MessagesColumns.SMS_ID + " is not null";
        String localWhereClause = "type<>3";// 不同步草稿
        return syncLocalMessage(whereClause, localWhereClause, -1, -1, noInsert);
    }

    @Override
    public boolean syncLocalMessageByThreadsID(long id) {
        return syncLocalMessageByThreadsID(id, -1, -1);
    }

    @Override
    public boolean syncLocalMessageByThreadsID(long id, int start, int limit) {
        if (id <= 0)
            return true;
        String shouxinWhereClause = MessagesColumns.THREADS_ID + "=" + id
                + " and " + MessagesColumns.SMS_ID + " is not null";

        // 手信会话和本地会话对应关系
        StringBuilder threadsSQL = new StringBuilder();
        threadsSQL.append("select ").append(ThreadsColumns._ID)
                .append(" as id,").append(ThreadsColumns.LOCAL_THREADS_ID)
                .append(" as localThreadsID").append(" from ")
                .append(ThreadsColumns.TABLE_NAME).append(" where ")
                .append(ThreadsColumns.LOCAL_THREADS_ID).append(" is not null");
        Map<String, String> threadsBylocalThreads = queryMap(
                threadsSQL.toString(), null);
        String localWhereClause = "thread_id="
                + threadsBylocalThreads.get(id + "");
        return syncLocalMessage(shouxinWhereClause, localWhereClause, start, limit, false);
    }

    private synchronized boolean syncLocalMessage(String shouxinWhereClause,
            String localWhereClause, int start, int limit, boolean noInsert) {
        String localPageLimit = "_id asc";
        if (-1 != start && -1 != limit) {
            localPageLimit = " limit " + limit + " offset " + start;
        }

        Cursor cursorLocalMessage = resolver.query(Uri.parse("content://sms/"),
                new String[] {
                        "_id", "thread_id", "address", "date", "read",
                        "type", "body", "status"
                }, localWhereClause, null,
                localPageLimit);
        if (null == cursorLocalMessage) {
            Log.i(TAG, "无法获取本地短信");
            return false;
        }
        String shouxinSQL = createSelectMessagesSQL(shouxinWhereClause, null,
                -1, -1);
        // beginTransaction();
        try {
            Map<String, Message> messageMap = query(Message.class,
                    MessagesColumns.SMS_ID.replaceFirst(MessagesColumns.SMS_ID
                            .substring(0, 1),
                            MessagesColumns.SMS_ID.substring(0, 1)
                                    .toUpperCase()), shouxinSQL);
            Map<String, Message> localMessageMap = new HashMap<String, Message>();

            // 本地会话和手信会话对应关系
            StringBuilder threadsSQL = new StringBuilder();
            threadsSQL.append("select ")
                    .append(ThreadsColumns.LOCAL_THREADS_ID)
                    .append(" as localThreadID,").append(ThreadsColumns._ID)
                    .append(" as id").append(" from ")
                    .append(ThreadsColumns.TABLE_NAME).append(" where ")
                    .append(ThreadsColumns.LOCAL_THREADS_ID)
                    .append(" is not null");
            Map<String, String> localThreadsByThreads = queryMap(
                    threadsSQL.toString(), null);
            beginTransaction();
            while (cursorLocalMessage.moveToNext()) {
                Message localMessage = new Message();
                localMessage.setSms_id(cursorLocalMessage.getLong(0));
                localMessage.setLocalThreadsID(cursorLocalMessage.getLong(1));
                localMessage.setPhones(cursorLocalMessage.getString(2));
                localMessage.setDate(cursorLocalMessage.getLong(3));
                localMessage.setRead(cursorLocalMessage.getInt(4));
                localMessage.setType(cursorLocalMessage.getInt(5));
                localMessage.setContent(cursorLocalMessage.getString(6));
                localMessage.setStatus(AndroidSysUtils.getStatus(cursorLocalMessage.getInt(7),
                        cursorLocalMessage.getInt(5)));
                localMessageMap.put(String.valueOf(localMessage.getSms_id()),
                        localMessage);

                ContentValues values = new ContentValues();
                values.put(MessagesColumns.CONTENT, localMessage.getContent());
                values.put(MessagesColumns.DATE, localMessage.getDate());
                values.put(MessagesColumns.LOCAL_THREADS_ID,
                        localMessage.getLocalThreadsID());
                values.put(MessagesColumns.OPT, localMessage.getType());
                values.put(MessagesColumns.PHONE, localMessage.getPhones());
                values.put(MessagesColumns.READ, localMessage.getRead());
                values.put(MessagesColumns.SMS_ID, localMessage.getSms_id());
                values.put(MessagesColumns.STATUS, localMessage.getStatus());
                values.put(
                        MessagesColumns.THREADS_ID,
                        localThreadsByThreads.get(localMessage
                                .getLocalThreadsID() + ""));
                values.put(MessagesColumns.SEQUENCE_ID,
                        localMessage.getSms_id());
                // 本地同步到手信
                if (!messageMap.containsKey(String.valueOf(localMessage
                        .getSms_id()))) {
                    if (!noInsert)
                        insert("messages", null, values);
                } else {
                    Message message = messageMap.get(String
                            .valueOf(localMessage.getSms_id()));
                    if ((message.getOpt() == MessagesColumns.OPT_TO &&
                            message.getStatus() != localMessage.getStatus())
                            || (message.getRead() != localMessage.getRead() &&
                            message.getRead() == MessagesColumns.READ_NO)) {
                        update("messages", values, MessagesColumns.SMS_ID
                                + "=?", new String[] {
                                localMessage.getSms_id()
                                        + ""
                        });
                    }
                }
            }
            endTransaction(true);
            cursorLocalMessage.close();

            // 这里还是需要比对本地短信，本地删除，手信里面也要进行删除
            for (Entry<String, Message> entry : messageMap.entrySet()) {
                String key = entry.getKey();
                Message shouxinMessage =
                        entry.getValue();
                if (shouxinMessage.getSms_id() != 0) {

                    if (localMessageMap.get(String.valueOf(shouxinMessage.getSms_id())) == null) {
                        executeSQL("delete from " +
                                MessagesColumns.TABLE_NAME + " where " + MessagesColumns.SMS_ID +
                                "=" + key);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new MessengerException(e);
        } finally {
            // endTransaction(true);
        }
        return true;
    }

    /**
     * 拼装 查询Messages SQL
     * 
     * @param whereClause
     * @param orderClause
     * @param start
     * @param count
     * @return
     */
    private String createSelectMessagesSQL(String whereClause,
            String orderClause, int start, int count) {
        StringBuilder shouxinSQL = new StringBuilder();
        shouxinSQL.append("select ").append(MessagesColumns._ID)
                .append(" as id,").append(MessagesColumns.CONTENT)
                .append(" as content,").append(MessagesColumns.MEDIA)
                .append(" as media,").append(MessagesColumns.READ)
                .append(" as read,")
                .append(MessagesColumns.TYPE)
                .append(" as type,")
                .append(MessagesColumns.OPT)
                .append(" as opt,")
                .append(MessagesColumns.STATUS)
                .append(" as status,")
                .append(MessagesColumns.SMS_ID)
                .append(" as sms_id,")
                .append(MessagesColumns.PHONE)
                .append(" as phones,")
                // --
                .append(MessagesColumns.DATE).append(" as date,")
                .append(MessagesColumns.LOCAL_THREADS_ID)
                .append(" as localThreadsID,")
                .append(MessagesColumns.THREADS_ID).append(" as threadsID,")
                .append(MessagesColumns.SEQUENCE_ID).append(" as sequence_id")
                .append(" from ").append(MessagesColumns.TABLE_NAME)
                .append(" ");
        if (!TextUtils.isEmpty(whereClause)) {
            shouxinSQL.append(" where ").append(whereClause).append(" ");
        }
        if (!TextUtils.isEmpty(orderClause)) {
            shouxinSQL.append(orderClause);
        }
        if (start != -1 && count != -1) {
            shouxinSQL.append(" limit ").append(count).append(" offset ")
                    .append(start);
        }
        return shouxinSQL.toString();
    }

    @Override
    public long addMessage(Message message) {
        ContentValues values = new ContentValues();
        values.put(MessagesColumns.CONTENT, message.getContent());
        values.put(MessagesColumns.DATE, message.getDate());
        values.put(MessagesColumns.LOCAL_THREADS_ID,
                message.getLocalThreadsID());
        values.put(MessagesColumns.MEDIA, message.getMedia());
        values.put(MessagesColumns.OPT, message.getOpt());
        values.put(MessagesColumns.PHONE, message.getPhones());
        values.put(MessagesColumns.READ, message.getRead());
        values.put(MessagesColumns.SMS_ID, message.getSms_id());
        values.put(MessagesColumns.STATUS, message.getStatus());
        values.put(MessagesColumns.THREADS_ID, message.getThreadsID());
        values.put(MessagesColumns.TYPE, message.getType());
        values.put(MessagesColumns.SEQUENCE_ID, message.getSequence_id());
        return insert(MessagesColumns.TABLE_NAME, null, values);
    }

    @Override
    public long updateMessage(Message message) {
        if (message == null || message.getId() <= 0) {
            throw new MessengerException("参数不能为空 且id必须大于0");
        }
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(message.getContent())) {
            values.put(MessagesColumns.CONTENT, message.getContent());
        }
        if (0 != message.getDate()) {
            values.put(MessagesColumns.DATE, message.getDate());
        }
        if (0 != message.getLocalThreadsID()) {
            values.put(MessagesColumns.LOCAL_THREADS_ID,
                    message.getLocalThreadsID());
        }
        if (!TextUtils.isEmpty(message.getMedia())) {
            values.put(MessagesColumns.MEDIA, message.getMedia());
        }
        if (0 != message.getOpt()) {
            values.put(MessagesColumns.OPT, message.getOpt());
        }
        if (!TextUtils.isEmpty(message.getPhones())) {
            values.put(MessagesColumns.PHONE, message.getPhones());
        }
        // if (0 != message.getRead()) {
        values.put(MessagesColumns.READ, message.getRead());
        // }
        if (0 != message.getSms_id()) {
            values.put(MessagesColumns.SMS_ID, message.getSms_id());
        }
        // if (0 != message.getStatus()) {
        values.put(MessagesColumns.STATUS, message.getStatus());
        // }
        if (0 != message.getThreadsID()) {
            values.put(MessagesColumns.THREADS_ID, message.getThreadsID());
        }
        if (0 != message.getType()) {
            values.put(MessagesColumns.TYPE, message.getType());
        }
        return update(MessagesColumns.TABLE_NAME, values, "_id=?",
                new String[] {
                    message.getId() + ""
                });
    }

    @Override
    public boolean deleteMessage(Message message) {
        return deleteMessage(message.getId());
    }

    @Override
    public boolean deleteMessage(long id) {
        int rows = delete(MessagesColumns.TABLE_NAME, "_id=?",
                new String[] {
                    id + ""
                });
        return rows > 0 ? true : false;
    }

    @Override
    public Uri addSMS(ContentValues values) {
        Uri ret;
        if (values.getAsInteger("type") == MessagesColumns.OPT_FROM) {
            ret = resolver.insert(Uri.parse("content://sms/inbox"), values);
        } else {
            ret = resolver.insert(Uri.parse("content://sms/sent"), values);
        }
        return ret;
    }

    @Override
    public boolean deleteSMS(long smsId) {
        CoreService.getInstance().getThreadsObserver().setLocked(true);
        boolean ret = resolver.delete(
                ContentUris.withAppendedId(Uri.parse("content://sms"), smsId), null,
                null) > 0 ? true : false;
        CoreService.getInstance().getThreadsObserver().setLocked(false);
        return ret;
    }

    /**
     * 新增短信到本地数据库 1. 在本地数据库中添加短信 2. 获取该短信的本地数据库中的ThreadID和id
     * 3.如果是未读短信，则还需要修改本地对应会话的未读状态
     **/
    @Override
    public Message addSMSForMessage(ContentValues values) {
        Message message = new Message();
        ;
        Uri uri = addSMS(values);
        // 有可能插入本地数据库失败
        if (null != uri) {
            Cursor cursor = resolver.query(uri,
                    new String[] {
                            "thread_id", "_id"
                    }, null, null, null);

            if (cursor.moveToNext()) {
                // message = new Message();
                message.setLocalThreadsID(cursor.getLong(0));
                message.setSms_id(cursor.getLong(1));
                // message.setSequence_id(cursor.getLong(1));
            }
            if (cursor != null)
                cursor.close();

            // if (values.getAsInteger("read").intValue() ==
            // MessagesColumns.READ_NO)
            // updateLocalThreadsReadStauts(message.getLocalThreadsID(),
            // MessagesColumns.READ_NO);
        }
        return message;
    }

    @Override
    public List<Threads> getUnreadThreads() {
        String whereClause = "t." + ThreadsColumns.READ + "=" + MessagesColumns.READ_NO;
        return getThreadsList(-1, -1, whereClause);
    }

    @Override
    public int getTotalUnreadMessageCount() {
        String whereClause = MessagesColumns.READ + "=" + MessagesColumns.READ_NO + " and "
                + MessagesColumns.THREADS_ID + " is not null";
        List<Message> list = getMessageList(-1, -1, -1, whereClause);
        return list.size();
    }

    @Override
    public List<Message> getUnreadMessageByThreadsID(long threadsID) {
        String whereClause = MessagesColumns.READ + "=" + MessagesColumns.READ_NO;
        return getMessageList(threadsID, -1, -1, whereClause);
    }

    @Override
    public String getContactByPhonesOrAccountIds(String phones,
            String accountIds) {
        /*
         * if (phones == null && accountIds == null) throw new
         * RuntimeException("参数不能同时为空");
         */
        Map<String, String> phoneMap = new HashMap<String, String>();
        StringBuilder sql = new StringBuilder(
                "select c.display_name as displayname, a._id as id,a.phone as phone from  contacts c , accounts a where c._id = a.contact_id and (1=2 ");
        String key = "Phone";
        if (phones != null) {
            for (String phone : phones.split(",")) {
                phone = AndroidSysUtils.removeHeader(phone);
                phoneMap.put(phone, phone);
                sql.append(" or ").append("a.phone='").append(phone).append("'");
            }
            key = "Phone";
        } else if (null != accountIds) {
            for (String accountId : accountIds.split(",")) {
                phoneMap.put(accountId, accountId);
                sql.append(" or ").append("a._id='").append(accountId)
                        .append("'");
            }
            key = "Id";
        }
        sql.append(")");
        StringBuilder result = new StringBuilder();
        try {

            Map<String, Contact> map = query(
                    Contact.class,
                    key,
                    sql.toString());
            for (Map.Entry<String, String> entry : phoneMap.entrySet()) {
                if (map.containsKey(entry.getKey())) {
                    if (!TextUtils.isEmpty(map.get(entry.getKey())
                            .getDisplayname())) {
                        result.append(map.get(entry.getKey()).getDisplayname())
                                .append(",");
                    } else if (!TextUtils.isEmpty(map.get(entry.getKey())
                            .getDisplayname())) {
                        result.append(map.get(entry.getKey()).getDisplayname())
                                .append(",");
                    } else {
                        result.append(entry.getKey()).append(",");
                    }
                } else {
                    result.append(entry.getKey()).append(",");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.length() == 0) {
            return "";
        }
        return result.substring(0, result.length() - 1);
    }

    @Override
    public String getContactByAccountIdOrSkyIdOrPhone(long accountId, String phone, int skyid) {

        StringBuilder sql = new StringBuilder(
                "select c.display_name as displayname, a._id as id,a.phone as phone from  contacts c , accounts a where c._id = a.contact_id and (1=2 ");
        String displayName = null;

        if (accountId > 0) {
            sql.append(" or ").append("a._id='").append(accountId)
                    .append("'");
        }
        if (skyid > 0) {
            sql.append(" or ").append("a.skyid='").append(skyid).append("'");
            // displayName = String.valueOf(skyid);
        }
        if (null != phone) {
            sql.append(" or ").append("a.phone='").append(phone)
                    .append("'");
            if (skyid <= 0)
                displayName = phone;
        }
        sql.append(")");
        Contact contact = queryForObject(Contact.class, sql.toString());

        return contact != null ? contact.getDisplayname() : displayName;

    }

    @Override
    public long getSequenceId(boolean isSms, long sms_id) {
        // 初始化sequenceId
        // 删除短信后，smsId会降，需要重新获取
        // long messagesSequenceId = getMessagesSequenceId();
        // long maxLocalSmsId = getMaxLocalSmsId();
        // return messagesSequenceId > maxLocalSmsId ? messagesSequenceId :
        // maxLocalSmsId;
        return 0;
    }

    /**
     * 获取sms表最大id
     * 
     * @return
     * @author Sivan.LV
     */
    @Override
    public long getMaxLocalSmsId() {
        long value;
        Cursor cursorLocalMessage = resolver.query(Uri.parse("content://sms/"),
                new String[] {
                    "MAX(_id)"
                }, null, null, null);
        if (null == cursorLocalMessage) {
            Log.e(TAG, "无法获取本地短信");
            return 0;
        }

        if (cursorLocalMessage.getCount() > 0
                && cursorLocalMessage.moveToNext()) {
            value = cursorLocalMessage.getLong(0);
        } else {
            Log.i(TAG, "没有本地短信");
            value = 0;
        }
        cursorLocalMessage.close();
        return value;
    }

    /**
     * 获取messages表最大sms_id
     * 
     * @return
     */
    @Override
    public long getMaxMessagesSmsId() {
        StringBuilder sql = new StringBuilder();
        sql.append("select MAX(sms_id) as max_sms_id")
                .append(" from ").append(MessagesColumns.TABLE_NAME);

        List<String> result = query(sql.toString(), null);
        try {
            if (result.size() > 0) {
                return Long.valueOf(result.get(0));
            }
        } catch (Exception e) {
        }
        return 0;

    }

    /**
     * 获取messages表最大sequenceId
     * 
     * @return
     * @author Sivan.LV
     */
    private long getMessagesSequenceId() {
        StringBuilder sql = new StringBuilder();
        sql.append("select MAX(sequence_id) as max_sequence_id")
                .append(" from ").append(MessagesColumns.TABLE_NAME);

        List<String> result = query(sql.toString(), null);
        try {
            if (result.size() > 0) {
                return Long.valueOf(result.get(0));
            }
        } catch (Exception e) {
        }
        return 0;

    }

    /**
     * 保存当前的sequenceId
     */
    private static volatile long sequenceId = -1;

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public Threads getThreadsByAddressId(String AddressId) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("t." + ThreadsColumns.ADDRESS_IDS).append("='").append(AddressId)
                .append("'");
        List<Threads> list = getThreadsList(-1, -1, whereClause.toString());
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public Threads getThreadsByAddressId(long AddressId) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("t." + ThreadsColumns.ADDRESS_IDS).append("='").append(AddressId)
                .append("'");
        List<Threads> list = getThreadsList(-1, -1, whereClause.toString());
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public void deleteUserData() {
        ArrayList<String> sqls = new ArrayList<String>();
        sqls.add("delete from accounts where _id <> 1");
        sqls.add("delete from address");
        sqls.add("delete from contacts where _id <> 1");
        sqls.add("delete from files");
        sqls.add("delete from friends");
        sqls.add("delete from messages");
        sqls.add("delete from threads");
        // 删除users表
        sqls.add("delete from users");

        executeWithTransaction(sqls);

    }

    @Override
    public Message getLatestMessageByThreadsId(long id) {
        List<Message> messages = getMessageList(id, 0, 1);
        return messages.size() > 0 ? messages.get(0) : null;
    }

    private void updateThreadsContent() {
        List<Threads> threadList = getThreadsList();
        updateThreadsContent(threadList);

    }

    // @Override
    // public void updateThreadsContent(long id) {
    // if (id == -1) {
    // updateThreadsContent();
    // return;
    // }
    // Message message = getLatestMessageByThreadsId(id);
    // if (null != message) {
    // switch (message.getType()) {
    // case MessagesColumns.TYPE_VOICE:
    // message.setContent(MainApp.i().getResources()
    // .getString(R.string.message_list_voice_item));
    // break;
    // case MessagesColumns.TYPE_CARD:
    // message.setContent(MainApp.i().getResources()
    // .getString(R.string.message_list_card_item));
    // break;
    // case MessagesColumns.TYPE_FRD:
    // message.setContent(MainApp.i().getResources()
    // .getString(R.string.message_list_frd_item));
    // break;
    // default:
    // break;
    // }
    // updateThreadsByMessage(id, message);
    // } else {
    // updateThreadsContent(id, "");
    // }
    // }

    private void updateThreadsContent(List<Threads> threadsList) {
        if (threadsList.size() == 0) {
            return;
        }
        StringBuilder threadsIn = new StringBuilder();
        for (Threads threads : threadsList) {
            threadsIn.append(threads.getId() + ",");
        }

        StringBuilder shouxinSQL = new StringBuilder();
        shouxinSQL.append("select ").append(MessagesColumns.THREADS_ID)
                .append(" as threadsID,").append(MessagesColumns._ID)
                .append(" as id,").append(MessagesColumns.CONTENT)
                .append(" as content,").append(MessagesColumns.MEDIA)
                .append(" as media,").append(MessagesColumns.READ)
                .append(" as read,")
                .append(MessagesColumns.TYPE)
                .append(" as type,")
                .append(MessagesColumns.OPT)
                .append(" as opt,")
                .append(MessagesColumns.STATUS)
                .append(" as status,")
                .append(MessagesColumns.SMS_ID)
                .append(" as sms_id,")
                .append(MessagesColumns.PHONE)
                .append(" as phones,")
                // --
                .append(MessagesColumns.DATE).append(" as date,")
                .append(MessagesColumns.LOCAL_THREADS_ID)
                .append(" as localThreadsID,")

                .append(MessagesColumns.THREADS_ID).append(" as threadsID,")
                .append(MessagesColumns.SEQUENCE_ID).append(" as sequence_id")
                .append(" from ").append(MessagesColumns.TABLE_NAME)
                .append(" where threadsID in(")
                .append(threadsIn.substring(0, threadsIn.length() - 1))
                // .append(") order by sequence_id desc, sms_id asc,_id desc");
                // .append(")").append(" GROUP by threadsID ")
                .append(") order by _id asc");
        Map<String, Message> messagesMap = query(Message.class, "ThreadsID", shouxinSQL.toString());
        Log.i("sivan.lv111", "messagesMap size:" + messagesMap.size());
        beginTransaction();
        for (Threads threads : threadsList) {
            Message message = messagesMap.get(String.valueOf(threads.getId()));
            if (null == message) {
                message = new Message();
                message.setContent("");
                message.setType(MessagesColumns.TYPE_TEXT);
                message.setDate(threads.getDate());
            }
            updateThreadsByMessage(threads.getId(), message);
        }
        endTransaction(true);

    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return super.delete(table, whereClause, whereArgs);
    }

    @Override
    public void updateThreadsContent(long threadsId) {
        List<Threads> threadsList = new ArrayList<Threads>();
        Threads threads = getThreadsByID(threadsId);
        if (threads != null) {
            threadsList.add(threads);
            updateThreadsContent(threadsList);
        }

    }

}
