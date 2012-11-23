
package android.skymobi.messenger.service.module;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.bean.ResFile;
import android.skymobi.messenger.bean.Stranger;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.bean.Traffic;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.dataaccess.lcs.ILcsDA;
import android.skymobi.messenger.dataaccess.lcs.LcsDA;
import android.skymobi.messenger.database.PhraseDatabaseHelper;
import android.skymobi.messenger.database.dao.AddressDAO;
import android.skymobi.messenger.database.dao.BaseDAO;
import android.skymobi.messenger.database.dao.ContactsDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.database.dao.FriendsDAO;
import android.skymobi.messenger.database.dao.MessagesDAO;
import android.skymobi.messenger.database.dao.ResFilesDAO;
import android.skymobi.messenger.database.dao.StrangerDAO;
import android.skymobi.messenger.database.dao.TrafficDAO;
import android.skymobi.messenger.database.observer.ThreadsObserver;
import android.skymobi.messenger.network.ChatMsgListener;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.network.module.MessageNetModule;
import android.skymobi.messenger.network.module.NotifyNetModule;
import android.skymobi.messenger.network.module.SettingsNetModule;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.provider.SocialMessenger.ThreadsColumns;
import android.skymobi.messenger.provider.SocialMessenger.TrafficColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.sms.MessageListener;
import android.skymobi.messenger.sms.MessageManager;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.PropertiesUtils;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.text.TextUtils;
import android.util.Log;

import com.skymobi.android.sx.codec.beans.clientbean.NetChatNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetFsDownloadReq;
import com.skymobi.android.sx.codec.beans.clientbean.NetFsDownloadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetRecommendedMsgNewResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetRecommendedMsgTypeResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetMarketingMessageNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetSysMsgNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetUploadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetVCardNotify;
import com.skymobi.android.sx.codec.beans.common.MsgType;
import com.skymobi.android.sx.codec.beans.common.RecommendMsg;
import com.skymobi.android.sx.codec.util.ParserUtils;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: MessageModule
 * @Description: 消息组件
 * @author Michael.Pan
 * @date 2012-2-20 上午11:00:58
 */
public class MessageModule extends BaseModule implements MessageListener,
        ChatMsgListener {
    private static final String TAG = MessageModule.class.getSimpleName();
    private final MessageNetModule messageNetModule;
    private final NotifyNetModule notifyNetModule;
    private final SettingsNetModule settingsNetModule;
    private final ContactsNetModule contactsNetModule;
    private final MessageManager smsManager = MessageManager.getInstance();
    private final ILcsDA lcsDA;
    private BaseDAO mBaseDAO = null;
    private MessagesDAO mMessagesDAO = null;
    private ContactsDAO mContactsDAO = null;
    private ResFilesDAO mResFilesDAO = null;
    private FriendsDAO friendsDAO = null;
    private AddressDAO mAddressDAO = null;
    private TrafficDAO trafficDao = null;
    private StrangerDAO strangerDAO = null;
    private final PhraseDatabaseHelper phraseMgr = PhraseDatabaseHelper
            .getInstance();

    // 处理短信的回执和原短信息的对应关系
    private final Map<Integer, Long> mSMSMap = new HashMap<Integer, Long>();
    private final Map<Long, Boolean> mSelfSMSMap = new HashMap<Long, Boolean>();
    public final static String RECEVIE_FRIENDS = "recevie_friends";

    public MessageModule(CoreService service) {
        super(service);
        settingsNetModule = netWorkMgr.getSettingsNetModule();
        messageNetModule = netWorkMgr.getMessageNetModule();
        notifyNetModule = netWorkMgr.getNotifyNetModule();
        contactsNetModule = netWorkMgr.getContactsNetModule();
        notifyNetModule.setChatMsgListener(this);
        smsManager.setMessageListener(this);
        daoFactory = DaoFactory.getInstance(MainApp.i());
        mBaseDAO = daoFactory.getBaseDAO();
        mMessagesDAO = daoFactory.getMessagesDAO();
        mContactsDAO = daoFactory.getContactsDAO();
        mResFilesDAO = daoFactory.getResfilesDAO();
        friendsDAO = daoFactory.getFriendsDAO();
        mAddressDAO = daoFactory.getAddressDAO();
        trafficDao = daoFactory.getTrafficDAO();
        strangerDAO = daoFactory.getStrangerDAO();
        lcsDA = new LcsDA();
    }

    /**
     * 同步会话，暂时先考虑同步一次
     */
    public void syncSMSThreads(final boolean bforceSync) {
        if (MainApp.i().getStatusSyncThreads()) {
            return;
        }
        MainApp.i().setStatusSyncThreads(true);
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis();
                // if (isBind) {
                service.notifyObservers(
                        CoreServiceMSG.MSG_THREADS_SYNC_BEGIN, 0);
                long stime = System.currentTimeMillis();
                mMessagesDAO.syncLocalThreads(processObserver);
                long etime = System.currentTimeMillis();
                Log.e("Time-consuming", "syncLocalThreads = " + (etime - stime) + "ms");

                CommonPreferences.saveSyncThreadsCount(CommonPreferences
                        .getSyncThreadsCount() + 1);
                service.notifyObservers(
                        CoreServiceMSG.MSG_THREADS_SYNC_END, 0);
                MainApp.i().setLastSyncThreadsTime(curTime);
                // }
                MainApp.i().setStatusSyncThreads(false);

            }
        });
    }

    private static boolean isSyncMessages = false;

    /**
     * 同步固定id会话的SMS短信消息(暂时未使用该接口)
     * 
     * @param threadID
     */
    public void syncSMSMessageByThreadID(final long threadID) {
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "syncMessages Begin = " + System.currentTimeMillis());
                service.notifyObservers(CoreServiceMSG.MSG_MESSAGES_SYNC_BEGIN,
                        0);
                mMessagesDAO.syncLocalMessageByThreadsID(threadID);
                service.notifyObservers(CoreServiceMSG.MSG_MESSAGES_SYNC_END, 0);
                Log.i(TAG, "syncMessages End = " + System.currentTimeMillis());
            }
        });
    }

    /**
     * 同步固定id会话的SMS短信消息
     * 
     * @param threadID
     */
    public void syncSMSMessageByThreadID(final long threadID, final int start, final int limit) {
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("Thread", "syncSMSMessageByThreadID start, thread id ="
                        + Thread.currentThread().getId());
                // long startTime = SystemClock.currentThreadTimeMillis();
                Map<String, Integer> map = new HashMap<String, Integer>();
                map.put("start", start);
                map.put("limit", limit);
                // service.notifyObservers(CoreServiceMSG.MSG_MESSAGES_SYNC_BEGIN,map);
                mMessagesDAO.syncLocalMessageByThreadsID(threadID, -1, -1);
                mMessagesDAO.updateThreadsContent(threadID);

                service.notifyObservers(CoreServiceMSG.MSG_MESSAGES_SYNC_END, map);
                // long endTime = SystemClock.currentThreadTimeMillis();
                /*
                 * Log.i("Thread", "syncSMSMessageByThreadID, thread id =" +
                 * Thread.currentThread().getId() + ",execute time = " +
                 * (endTime - startTime)); Log.i("Thread",
                 * "mThreadPool.getActiveCount() = " +
                 * mThreadPool.getActiveCount() +
                 * " ,mThreadPool.getPoolSize() = " + mThreadPool.getPoolSize()
                 * + " ,mThreadPool.getTaskCount() =" +
                 * mThreadPool.getTaskCount());
                 */
            }
        });
    }

    /**
     * 发送绑定的短信
     * 
     * @param tos 目标号码
     * @param content 短信内容
     * @param isBindSMS 是否是绑定的短信，其他的功能调用该函数时应置为false 20121009 by hzc
     */
    public void sendActivateSMSMsg(final String[] tos, final String content, final boolean isBindSMS) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                boolean isSendSuccess = false;
                int i = 1;
                for (String to : tos) {
                    if (StringUtils.isNotBlank(to) && (!"null".equalsIgnoreCase(to))) {
                        SLog.d(TAG, "发送绑定手机的短信[" + (i++) + "]:" + to);
                        int seq = smsManager.send(to, content);
                        // 记录发送对应的<seq ,msgId>键值对
                        mSMSMap.put(seq, 0L);
                        SLog.i(TAG, "sendSMSMsg seq = " + seq);
                        isSendSuccess = true;
                    }
                }
                if (isBindSMS) {
                    // 保存发起绑定的时间
                    if (isSendSuccess) {
                        SLog.d(TAG, "保存发送激活短信的时间点..");
                        CommonPreferences.saveChangeBindSendSMSTime(System.currentTimeMillis());
                    } else {
                        SLog.d(TAG, "发送激活短信失败，重置发送激活短信的时间点..");
                        CommonPreferences.saveChangeBindSendSMSTime(-1L);
                    }
                }
            }
        });
    }

    public void sendActivateSMSMsg(final String[] tos, final String content) {
        sendActivateSMSMsg(tos, content, true);
    }

    public void sendActivateSMSMsg(final String to, final String content, final boolean isBindSMS) {
        sendActivateSMSMsg(StringUtils.split(to, "|"), content, isBindSMS);
    }

    public void sendActivateSMSMsg(final String to, final String content) {
        sendActivateSMSMsg(to, content, true);
    }

    /**
     * 发送SMS消息
     * 
     * @param smsMsg
     */
    public void sendSMSMsg(final Message smsMsg) {
        Address address = new Address();
        final Threads threads = new Threads();
        threads.setDate(smsMsg.getDate());
        threads.setContent(smsMsg.getContent());
        threads.setPhones(smsMsg.getPhones());
        threads.setRead(smsMsg.getRead());
        threads.setStatus(AndroidSysUtils.getStatus(smsMsg.getStatus(), 0));

        // beginTransaction();
        try {
            address.setPhone(smsMsg.getPhones());
            Account account = getAccountByAddress(address);
            if (null != account) {// 在联系人列表。获取phone，skyid,accountId
                address = getAddressByAccount(account);
            }
            if (address.getId() <= 0) {
                address = getAddressByAddressAdd(address);
            } else {
                mAddressDAO.updateAddress(address);
            }
            threads.setAddressIds(String.valueOf(address.getId()));

            if (smsMsg.getThreadsID() > 0) {
                threads.setId(smsMsg.getThreadsID());
                mMessagesDAO.updateThreads(threads);
            } else {

                Threads t = mMessagesDAO.getThreadsByAddressId(address.getId());
                if (null == t) {
                    threads.setId(mMessagesDAO.addThreads(threads));
                } else {
                    threads.setId(t.getId());
                    mMessagesDAO.updateThreads(threads);
                }
                smsMsg.setThreadsID(threads.getId());
            }

            if (smsMsg.getId() > 0) {
                mMessagesDAO.updateMessage(smsMsg);
            } else {
                smsMsg.setId(mMessagesDAO.addMessage(smsMsg));
            }
        } finally {
            // endTransaction(true);
        }

        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                doSendSMS(threads, smsMsg, true);
            }
        });
    }

    /**
     * 发送文字网络消息
     * 
     * @param msg
     */
    public void sendChatTextMsg(final Message msg) {
        final Address address = msg.getAddressList().get(0);
        final Threads threads = new Threads();
        // beginTransaction();
        try {
            Address updAddress = new Address();
            updAddress.setId(address.getId());
            updAddress.setPhone(address.getPhone());
            updAddress.setSkyId(address.getSkyId());
            Account account = getAccountByAddress(updAddress);
            if (null != account) {// 在联系人列表。获取phone，accountId
                updAddress = getAddressByAccount(account);
            }

            if (updAddress.getId() <= 0) {
                updAddress = getAddressByAddressAdd(updAddress);
            } else {
                mAddressDAO.updateAddress(updAddress);
            }

            threads.setDate(msg.getDate());
            threads.setContent(msg.getContent());
            threads.setPhones(msg.getPhones());
            threads.setRead(msg.getRead());
            threads.setStatus(msg.getStatus());
            threads.setDisplayName(msg.getNickName());
            threads.setAddressIds(String.valueOf(updAddress.getId()));
            if (msg.getThreadsID() > 0) {
                threads.setId(msg.getThreadsID());
                mMessagesDAO.updateThreads(threads);
            } else {
                Threads t = mMessagesDAO.getThreadsByAddressId(updAddress.getId());
                if (null == t) {
                    threads.setId(mMessagesDAO.addThreads(threads));
                } else {
                    threads.setId(t.getId());
                    mMessagesDAO.updateThreads(threads);
                }

                msg.setThreadsID(threads.getId());
            }

            if (msg.getId() > 0) {
                mMessagesDAO.updateMessage(msg);
            } else {
                msg.setId(mMessagesDAO.addMessage(msg));
            }
        } finally {
            // endTransaction(true);
        }
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                NetResponse resp = messageNetModule.sendChatTextMsg(
                        String.valueOf(address.getSkyId()),
                        msg.getContent(), msg.getTalkReason());

                // 发送网络文本消息次数
                lcsDA.saveNetTextCount(lcsDA.getNetTextCount() + 1);
                if (resp.isSuccess()) {
                    Log.i(TAG, "sendChatTextMsg .............success");
                    msg.setStatus(MessagesColumns.STATUS_SUCCESS);
                } else {
                    if (resp.getResultCode() == -1) {
                        resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                    }
                    ResultCode.setCode(resp.getResultCode());
                    Log.i(TAG, "sendChatTextMsg .............failure");
                    msg.setStatus(MessagesColumns.STATUS_FAILED);
                }
                // beginTransaction();
                try {
                    threads.setStatus(msg.getStatus());
                    mMessagesDAO.updateThreads(threads);
                    mMessagesDAO.updateMessage(msg);
                } finally {
                    // endTransaction(true);
                }
                service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_TEXTMSG_SEND_END,
                        null);
                if (msg.getTalkReason() != null) {
                    if (!MainApp.i().getGreetStatus(address.getSkyId())) {
                        MainApp.i().putGreetStatus(address.getSkyId(), true);
                        lcsDA.saveBuddyPeopleCount(lcsDA.getBuddyPeopleCount() + 1);
                    }
                    lcsDA
                            .saveClickBuddyCount(lcsDA.getClickBuddyCount() + 1);
                }
                lcsDA.saveNetTextCount(lcsDA.getNetTextCount() + 1);
            }
        });
    }

    /**
     * 发送文字网络消息
     * 
     * @param msg
     */
    public void sendChatMultiMsg(final Message msg,
            final List<Address> addressList) {
        // 更新手信数据库中相应会话内容
        // beginTransaction();
        try {
            StringBuilder addressIds = new StringBuilder();
            Long[] ids = new Long[addressList.size()];
            int i = 0;
            for (Address address : addressList) {
                if (address.getId() <= 0) {
                    address.setId(mAddressDAO.addAddress(address));
                } else {
                    mAddressDAO.updateAddress(address);
                }
                ids[i++] = address.getId();
            }
            if (ids != null && ids.length > 0) {
                Arrays.sort(ids);
                for (i = 0; i < ids.length; i++) {
                    if (ids[i] != null && (ids[i]) != 0) {
                        addressIds.append(ids[i] + ",");
                    } else {
                        addressIds.append(",");
                    }

                }
            }

            msg.setStatus(MessagesColumns.STATUS_SUCCESS);
            Threads threads = new Threads();
            threads.setDate(msg.getDate());
            threads.setContent(msg.getContent());
            threads.setRead(msg.getRead());
            threads.setStatus(msg.getStatus());
            threads.setAddressIds(addressIds.substring(0, addressIds.length() - 1));
            threads.setDisplayName(msg.getNickName());
            if (msg.getThreadsID() > 0) {
                threads.setId(msg.getThreadsID());
                mMessagesDAO.updateThreads(threads);
            } else {
                Threads t = mMessagesDAO.getThreadsByAddressId(threads.getAddressIds());
                if (null == t) {
                    threads.setId(mMessagesDAO.addThreads(threads));
                } else {
                    threads.setId(t.getId());
                    mMessagesDAO.updateThreads(threads);
                }

                msg.setThreadsID(threads.getId());
            }

            if (msg.getId() > 0) {
                mMessagesDAO.updateMessage(msg);
            } else {
                msg.setId(mMessagesDAO.addMessage(msg));
            }
        } finally {
            // endTransaction(true);
        }
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {

                final StringBuilder sBuilder = new StringBuilder();
                for (Address address : addressList) {
                    final int skyid = address.getSkyId();
                    final String toPhone = address.getPhone();
                    final boolean status = MainApp.i().getUserOnlineStatus(skyid);
                    // 对发不在线并且有号码，或者只有电话号码的时候发送短信，其余情况发生网络消息
                    if (skyid < 0 || (!status && toPhone != null)) {
                        smsManager.send(toPhone, msg.getContent());
                    } else {
                        sBuilder.append(skyid + ",");
                    }
                }
                SLog.d(TAG, "sendChatMultiMsg sBuilder.toString() = " + sBuilder.toString());
                messageNetModule.sendChatTextMsg(sBuilder.toString(),
                        msg.getContent(), msg.getTalkReason());

                service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_TEXTMSG_SEND_END,
                        null);
                lcsDA.saveMassMULTICount(lcsDA.getMassMULTICount() + 1);
                SLog.i(TAG, "sendChatMultiMsg .............end");
            }
        });
    }

    /**
     * 发送语音网络消息
     * 
     * @param msg
     */
    public void sendChatVoiceMsg(final Message msg, final ResFile file,
            final Address address) {
        final Threads threads = new Threads();
        // beginTransaction();
        try {
            Address updAddress = new Address();
            updAddress.setId(address.getId());
            updAddress.setPhone(address.getPhone());
            updAddress.setSkyId(address.getSkyId());
            Account account = getAccountByAddress(updAddress);
            if (null != account) {// 在联系人列表。获取phone，accountId
                updAddress = getAddressByAccount(account);
            }

            if (updAddress.getId() <= 0) {
                updAddress = getAddressByAddressAdd(updAddress);
            } else {
                mAddressDAO.updateAddress(updAddress);
            }

            threads.setDate(msg.getDate());
            threads.setContent(MainApp.i().getResources()
                    .getString(R.string.message_list_voice_item));
            threads.setRead(msg.getRead());
            threads.setAddressIds(String.valueOf(updAddress.getId()));

            threads.setDisplayName(msg.getNickName());
            threads.setStatus(msg.getStatus());
            if (msg.getThreadsID() > 0) {
                threads.setId(msg.getThreadsID());
                mMessagesDAO.updateThreads(threads);
            } else {
                Threads t = mMessagesDAO.getThreadsByAddressId(threads.getAddressIds());
                if (null == t) {
                    threads.setId(mMessagesDAO.addThreads(threads));
                } else {
                    threads.setId(t.getId());
                    mMessagesDAO.updateThreads(threads);
                }

                msg.setThreadsID(threads.getId());
            }
            if (msg.getId() > 0) {
                mMessagesDAO.updateMessage(msg);
            } else {
                msg.setId(mMessagesDAO.addMessage(msg));
            }
        } finally {
            // endTransaction(true);
        }
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                FileInputStream in = null;
                byte[] body = null;
                try {
                    in = new FileInputStream(new File(file.getPath()));
                    body = new byte[in.available()];
                    in.read(body);
                    in.close();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "sendChatVoiceMsg--->FileNotFoundException");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "sendChatVoiceMsg--->IOException");
                    e.printStackTrace();
                }

                String fileUrl = PropertiesUtils.getInstance().getFileURL();
                UserInfo info = CommonPreferences.getUserInfo();
                // 上传语音文件
                NetUploadResponse resp = settingsNetModule.uploadFs(fileUrl, info.skyid,
                        info.token, Constants.VOICE_EXT_NAME, body);
                Log.i(TAG, "uploadVoice-->resp.isSuccess() = " + resp.isSuccess()
                        + ",ResultCode = " + resp.getResultCode());
                // 发送语音文件
                NetResponse respSend = null;

                if (resp.isSuccess()) {

                    file.setUrl(resp.getMd5());
                    file.setSize(body.length);
                    long stime = System.currentTimeMillis();
                    respSend = messageNetModule.sendChatVoiceMsg(
                            String.valueOf(address.getSkyId()), file);
                    // 发送语音消息统计
                    lcsDA.saveNetVOICECount(lcsDA.getNetVOICECount() + 1);
                    long etime = System.currentTimeMillis();
                    SLog.e(
                            "Time-consuming",
                            "sendChatVoiceMsg = " + (etime - stime) + "ms" + ", resp.isFailed() = "
                                    + respSend.isFailed() + ",ResultCode = "
                                    + respSend.getResultCode());
                    Log.i(TAG, "sendChatVoiceMsg-->respSend.isSuccess() =  " + respSend.isSuccess());
                } else {
                    if (resp.getResultCode() == -1) {
                        resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                    }
                    ResultCode.setCode(resp.getResultCode());
                }
                // beginTransaction();
                try {
                    mResFilesDAO.updateFile(file);

                    if (respSend != null && respSend.isSuccess()) {
                        msg.setStatus(MessagesColumns.STATUS_SUCCESS);
                    } else {
                        msg.setStatus(MessagesColumns.STATUS_FAILED);
                    }
                    threads.setStatus(msg.getStatus());
                    mMessagesDAO.updateThreads(threads);
                    mMessagesDAO.updateMessage(msg);
                } finally {
                    // endTransaction(true);
                }
                service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_VOICEMSG_SEND_END, null);
                lcsDA.saveNetVOICECount(lcsDA.getNetVOICECount() + 1);
            }
        });
    }

    /**
     * 发送名片
     * 
     * @param destSkyids
     * @param msg
     * @param accountIDs
     * @param contactID 名片的contactID
     */
    public void sendCardMsg(final Address address, final Message msg,
            final Map<String, Object> mapCard) {
        final Threads threads = new Threads();
        // beginTransaction();
        try {
            // 更新手信数据库中相应会话内容
            // Address address = ad;
            Address updAddress = new Address();
            updAddress.setId(address.getId());
            updAddress.setPhone(address.getPhone());
            updAddress.setSkyId(address.getSkyId());
            Account account = getAccountByAddress(updAddress);
            if (null != account) {// 在联系人列表。获取phone，accountId
                updAddress = getAddressByAccount(account);
            }

            if (updAddress.getId() <= 0) {
                updAddress = getAddressByAddressAdd(updAddress);
            } else {
                mAddressDAO.updateAddress(updAddress);
            }
            threads.setDate(msg.getDate());
            threads.setContent(MainApp.i().getResources()
                    .getString(R.string.message_list_card_item));
            threads.setRead(msg.getRead());
            threads.setStatus(msg.getStatus());
            threads.setAddressIds(String.valueOf(updAddress.getId()));

            threads.setDisplayName(msg.getNickName());
            if (msg.getThreadsID() > 0) {
                threads.setId(msg.getThreadsID());
                mMessagesDAO.updateThreads(threads);
            } else {
                Threads t = mMessagesDAO.getThreadsByAddressId(threads.getAddressIds());
                if (null == t) {
                    threads.setId(mMessagesDAO.addThreads(threads));
                } else {
                    threads.setId(t.getId());
                    mMessagesDAO.updateThreads(threads);
                }

                msg.setThreadsID(threads.getId());
            }

            if (msg.getId() > 0) {
                mMessagesDAO.updateMessage(msg);
            } else {
                msg.setId(mMessagesDAO.addMessage(msg));
            }
        } finally {
            // endTransaction(true);
        }

        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                NetResponse respSend = messageNetModule.sendCardMsg(
                        String.valueOf(address.getSkyId()), mapCard);
                Log.i(TAG, "card msg content = " + ParserUtils.encodeVCard(mapCard));
                // 发送名片次数
                lcsDA.saveNetCARDCount(lcsDA.getNetCARDCount() + 1);
                if (respSend != null && respSend.isSuccess()) {
                    msg.setStatus(MessagesColumns.STATUS_SUCCESS);
                } else {
                    if (respSend.getResultCode() == -1) {
                        respSend.setResult(Constants.NET_ERROR, respSend.getResultHint());
                    }
                    ResultCode.setCode(respSend.getResultCode());
                    msg.setStatus(MessagesColumns.STATUS_FAILED);
                }
                threads.setStatus(msg.getStatus());
                // beginTransaction();
                try {
                    mMessagesDAO.updateThreads(threads);
                    mMessagesDAO.updateMessage(msg);
                } finally {
                    // endTransaction(true);
                }

                service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_CARDMSG_SEND_END, null);
                lcsDA.saveNetCARDCount(lcsDA.getNetCARDCount() + 1);
            }
        });
    }

    /**
     * 发送名片,根据发送对象的地址信息选择发送短信或者是网络名片
     */
    /*
     * public void sendCard(Account destAccount, long contactID) { if
     * (destAccount == null || (destAccount.getSkyId() < 1 &&
     * destAccount.getPhone() == null)) { Log.e(TAG, "send card param error!");
     * return; } Message msg = new Message();
     * msg.setDate(System.currentTimeMillis());
     * msg.setOpt(android.skymobi.messenger.bean.Message.OPT_TO);
     * msg.setRead(android.skymobi.messenger.bean.Message.READ_YES);
     * msg.setStatus(android.skymobi.messenger.bean.Message.STATUS_SENDING);
     * Address address = getAddressByAccount(destAccount); //
     * address.setSkyId(destAccount.getSkyId()); //
     * address.setPhone(destAccount.getPhone()); //
     * address.setAccountId(destAccount.getId()); if (destAccount.getSkyId() >
     * 0) { // 发网络名片
     * msg.setType(android.skymobi.messenger.bean.Message.TYPE_CARD);
     * sendCardMsg(address, msg, contactID); } else if (destAccount.getPhone()
     * != null) { // 发短信名片
     * msg.setType(android.skymobi.messenger.bean.Message.TYPE_SMS); Map<String,
     * Object> cardMap = mContactsDAO.getCardByContactId(contactID); String
     * cardName = (String) cardMap.get(NetVCardNotify.CONTACT_NAME);
     * List<VCardContent> cardlist = (List)
     * cardMap.get(NetVCardNotify.CONTACT_DETAIL_LIST); StringBuffer sBuffer =
     * new StringBuffer(); sBuffer.append(MainApp.getInstance().getResources()
     * .getString(R.string.card_detail_item_name) + ":" + cardName + ";"); //
     * 添加手机号 for (VCardContent vc : cardlist) { if (vc.getPhone() != null &&
     * !vc.getPhone().equalsIgnoreCase("")) { sBuffer.append("\n" +
     * MainApp.getInstance().getResources()
     * .getString(R.string.card_detail_item_phone) + ":" + vc.getPhone() + ";");
     * } } msg.setContent(sBuffer.toString());
     * msg.setPhones(destAccount.getPhone()); sendSMSMsg(msg); } }
     */

    /**
     * 获取推荐短语
     * 
     * @param msgTypeID
     * @param start
     * @param page
     */
    public void syncRecommendedMsg() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                long sTime = System.currentTimeMillis();
                // 短信类型版本号，该参数由服务端下发
                long msgUpdatetime = CommonPreferences.getSyncRecommendMsgTypeTime();
                SLog.d(TAG, "消息类型版本号:" + msgUpdatetime);
                NetGetRecommendedMsgTypeResponse resp = messageNetModule
                        .getRecommendMsgType(msgUpdatetime);
                if (!resp.isNetError() && resp.isSuccess()) {
                    if (resp.isHasUpdate()) {
                        // 保存服务端最新的推荐短信类型版本号
                        CommonPreferences.saveSyncRecommendMsgTypeTime(resp.getUpdateTime());
                        service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_GETRECOMMEND_BEGIN,
                                null);
                        ArrayList<MsgType> list = resp.getMsgTypeList();
                        phraseMgr.addMsgTypeList(list);
                        for (MsgType msgType : list) {
                            if (msgType.getAction() == 2) {
                                SLog.d(TAG,
                                        "类型[" + msgType.getMsgTypeId() + "|"
                                                + msgType.getMsgTypeName() + "]已经标识为删除");
                                continue;
                            }
                            if (msgType.getUpdate() == 1) {
                                SLog.d(TAG,
                                        "类型[" + msgType.getMsgTypeId() + "|"
                                                + msgType.getMsgTypeName() + "] 有更新");
                                int pageIndex = 1;
                                // 是否执行循环的判断条件
                                boolean isContinue = true;
                                // 批量获取短信类别下的推荐短信列表
                                long msgListVersion = CommonPreferences
                                        .getSyncRecommendMsgListVersion(msgType.getMsgTypeId());
                                do {
                                    NetGetRecommendedMsgNewResponse response = messageNetModule
                                            .getRecommendMsgNew(msgType.getMsgTypeId(),
                                                    msgListVersion, pageIndex, 50, 100);
                                    if (response.isSuccess()) {
                                        if (response.isHasUpdate()) {
                                            SLog.d(TAG, "短信[" + msgType.getMsgTypeId() + "|"
                                                    + msgType.getMsgTypeName() + "]de版本号:"
                                                    + response.getUpdateTime());
                                            // 更新短信类型列表的版本号
                                            CommonPreferences.saveSyncRecommendMsgListVersion(
                                                    response.getUpdateTime(),
                                                    msgType.getMsgTypeId());
                                            // 分页算法
                                            int totalSize = response.getTotalSize();
                                            int totalPage = totalSize / 50
                                                    + ((totalSize % 50 > 0) ? 1 : 0);
                                            ArrayList<RecommendMsg> textList = response
                                                    .getTextMessage();
                                            if (null != textList && textList.size() > 0) {
                                                phraseMgr.addPhraseList2(textList,
                                                        msgType.getMsgTypeId());
                                            }
                                            if (totalPage >= pageIndex) {
                                                pageIndex++;
                                            } else {
                                                SLog.d(TAG, "没有分页数据");
                                                isContinue = false;
                                            }
                                        } else {
                                            SLog.d(TAG, "暂无短信列表更新");
                                            isContinue = false;
                                        }
                                    } else {
                                        if (response.getResultCode() == -1) {
                                            response.setResult(Constants.NET_ERROR,
                                                    response.getResultHint());
                                        }
                                        ResultCode.setCode(response.getResultCode());
                                        SLog.d(TAG, "短信列表更新失败");
                                        isContinue = false;
                                    }
                                } while (isContinue);
                            }
                        }
                        service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_GETRECOMMEND_END,
                                resp);
                    } else {
                        SLog.d(TAG, "暂无推荐短信更新");
                    }
                } else {
                    if (resp.getResultCode() == -1) {
                        resp.setResult(Constants.NET_ERROR, resp.getResultHint());
                    }
                    ResultCode.setCode(resp.getResultCode());
                    service.notifyObservers(CoreServiceMSG.MSG_NET_ERROR, resp);
                    Log.e(TAG, "resp.isNetError() = " + resp.isNetError());
                    Log.e(TAG, "resp.isSuccess() = " + resp.isSuccess());
                }
                long eTime = System.currentTimeMillis();
                SLog.e("Time-consuming",
                        "getRecommendedMsg =  " + (eTime - sTime) + "ms");
            }
        });
    }

    /**
     * 发送SMS消息的回调通知
     */
    @Override
    public void onSendSMS(final int sequence, final int result) {
        if (mSMSMap.isEmpty())
            return;
        Log.i("2012.6.13", "[onSendSMS] sequence=" + sequence + " result=" + result);
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 通过<seq ,msgId>键值对 找到原始短信的ID
                Long msgID = mSMSMap.get(sequence);
                Log.i("2012.6.13", "[onSendSMS] msgID=" + msgID);
                if (null != msgID && msgID != 0) {// 非激活短信
                    mSMSMap.remove(sequence);
                    Message msg = mMessagesDAO.getMessageByID(msgID.longValue());
                    msg.setStatus(result);
                    mMessagesDAO.updateMessage(msg);
                }
                service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SMSMSG_SEND_END,
                        result);
            }
        });

    }

    public void onSendSMS(final Message msg, final boolean self) {
        msg.setOpt(MessagesColumns.OPT_TO);
        msg.setType(MessagesColumns.TYPE_SMS);
        Address address = new Address();
        Threads threads = new Threads();
        threads.setDate(msg.getDate());
        threads.setContent(msg.getContent());
        threads.setPhones(msg.getPhones());
        threads.setRead(msg.getRead());
        threads.setLocalThreadsID(msg.getLocalThreadsID());

        address.setPhone(msg.getPhones());
        Account account = getAccountByAddress(address);
        if (null != account) {// 在联系人列表。获取phone，accountId
            address = getAddressByAccount(account);
        }

        if (address.getId() <= 0) {
            address = getAddressByAddressAdd(address);
        } else {
            mAddressDAO.updateAddress(address);
        }

        threads.setAddressIds(String.valueOf(address.getId()));
        Threads t = mMessagesDAO.getThreadsByAddressId(threads.getAddressIds());
        if (null == t) {
            threads.setId(mMessagesDAO.addThreads(threads));
        } else {
            threads.setId(t.getId());
            mMessagesDAO.updateThreads(threads);
        }

        msg.setThreadsID(threads.getId());
        msg.setId(mMessagesDAO.addMessage(msg));

        doSendSMS(threads, msg, self);
    }

    /**
     * 接收消息的回调通知
     */
    @Override
    public void onReceiveSMS(final String from, final String content,
            final long receiveTime) {
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                // 添加到手机短信数据库 mmssms.db
                ContentValues values = new ContentValues();
                values.put("date", receiveTime);
                values.put("read", MessagesColumns.READ_NO);
                values.put("type", MessagesColumns.OPT_FROM);
                values.put("address", from);
                values.put("body", content);
                if (mSelfSMSMap.size() > 100) {
                    // 未绑定是不清除map的，所以这里达到一定数量清除
                    mSelfSMSMap.clear();
                }
                mSelfSMSMap.put(receiveTime, true);
                Log.i(TAG, "-----mSelfSMSMap put date " + receiveTime);
                Message msg = mMessagesDAO.addSMSForMessage(values);
                msg.setContent(content);
                msg.setPhones(from);
                msg.setDate(receiveTime);
                doReceiveSMS(msg);
            }
        });
    }

    public void onReceiveSMS(final Message msg) {
        doReceiveSMS(msg);
    }

    private void doReceiveSMS(Message msg) {
        // beginTransaction();
        try {
            msg.setOpt(MessagesColumns.OPT_FROM);
            msg.setType(MessagesColumns.TYPE_SMS);
            msg.setRead(MessagesColumns.READ_NO);
            Address address = new Address();
            address.setPhone(msg.getPhones());
            Account account = getAccountByAddress(address);
            if (null != account) {// 在联系人列表。获取phone，accountId
                address = getAddressByAccount(account);
            }

            if (address.getId() <= 0) {
                address = getAddressByAddressAdd(address);
            } else {
                mAddressDAO.updateAddress(address);
            }
            Threads threads = new Threads();
            threads.setContent(msg.getContent());
            threads.setPhones(msg.getPhones());
            threads.setRead(msg.getRead());
            threads.setDate(msg.getDate());
            threads.setLocalThreadsID(msg.getLocalThreadsID());
            threads.setAddressIds(String.valueOf(address.getId()));

            Threads t = mMessagesDAO.getThreadsByAddressId(address.getId());
            if (null == t) {
                threads.setId(mMessagesDAO.addThreads(threads));
            } else {
                threads.setId(t.getId());
                mMessagesDAO.updateThreads(threads);
            }

            msg.setThreadsID(threads.getId());
            msg.setStatus(MessagesColumns.STATUS_SUCCESS);
            msg.setSequence_id(mMessagesDAO.getSequenceId(true,
                    msg.getSms_id()));
            mMessagesDAO.addMessage(msg);
        } finally {
            // endTransaction(true);
        }
        service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_SMSMSG_RECEIVE, msg);
        service.showNotification(msg);

    }

    public void doSendSMS(Threads threads, Message msg, boolean self) {
        if (msg.getLocalThreadsID() <= 0) {
            // 添加到手机短信数据库 mmssms.db
            ContentValues values = new ContentValues();
            values.put("date", msg.getDate());
            values.put("read", msg.getRead());
            values.put("type", msg.getOpt());
            values.put("address", msg.getPhones());
            values.put("body", msg.getContent());
            mSelfSMSMap.put(msg.getDate(), true);
            Log.i(TAG, "-----mSelfSMSMap put date " + msg.getDate());
            Message message = mMessagesDAO.addSMSForMessage(values);
            msg.setSms_id(message.getSms_id());
            msg.setLocalThreadsID(message.getLocalThreadsID());
            if (msg.getSms_id() != 0)
                msg.setStatus(msg.getStatus());
            else
                msg.setStatus(MessagesColumns.STATUS_FAILED);
            // beginTransaction();
            try {
                mMessagesDAO.updateMessage(msg);
                threads.setStatus(AndroidSysUtils.getStatus(msg.getStatus(), 0));//
                threads.setLocalThreadsID(msg.getLocalThreadsID());
                mMessagesDAO.updateThreads(threads);
            } finally {
                // endTransaction(true);
            }
        }

        if (msg.getSms_id() != 0 && self) {
            // 统计短信发送次数
            lcsDA.saveSingleSmsCount(lcsDA.getSingleSmsCount() + 1);
            long msgId = msg.getId();

            doInviteCount(msg.getContent(), msg.getPhones());

            // 保证插入数据库成功以后，才能进行发送短信
            int seq = smsManager.send(msg.getPhones(),
                    msg.getContent());
            // 记录发送对应的<seq ,msgId>键值对
            mSMSMap.put(seq, msgId);
            Log.i(TAG, "sendSMSMsg seq = " + seq);
        }
    }

    private boolean selfSendReceive(long date) {
        Log.i(TAG, "-----selfSendReceive date:" + date);
        boolean ret = mSelfSMSMap.get(date) != null ? true : false;
        mSelfSMSMap.remove(date);
        return ret;
    }

    private void selfSmsDone(long date) {
        mSelfSMSMap.remove(date);
    }

    /**
     * 网络消息的回调通知
     */
    @Override
    public void onNotify(int what, Object obj) {
        Calendar c = Calendar.getInstance();
        long receiveTime = c.getTimeInMillis();
        switch (what) {
            case CoreServiceMSG.MSG_CHATMSG_TEXTMSG_RECEIVE: {
                Log.i(TAG, "receive TEXT Msg.....");
                NetChatNotify nmy = (NetChatNotify) obj;
                String nickname = nmy.getNickname();
                receiveTime = DateUtil.getLongTimeByStamp(nmy.getTimestamp());
                int skyID = nmy.getSkyid();
                Log.i(TAG, "skyID = " + skyID);
                // TODO　 获取用户在线状态
                String content = nmy.getMsgContent();
                // addChatMsg(nickname, skyID, content, receiveTime,
                // Message.TYPE_TEXT);
                List<Message> messages = new ArrayList<Message>();
                Message message = new Message();
                message.setContent(content);
                message.setDate(receiveTime);
                message.setType(MessagesColumns.TYPE_TEXT);
                message.setTalkReason(nmy.getTalkReason());
                messages.add(message);
                addChatMsg(nickname, skyID, messages);
            }
                break;
            case CoreServiceMSG.MSG_CHATMSG_VOICEMSG_RECEIVE: {
                Log.i(TAG, "receive Voice Msg.....");
                NetChatNotify nmy = (NetChatNotify) obj;
                String nickname = nmy.getNickname();
                int skyID = nmy.getSkyid();
                receiveTime = DateUtil.getLongTimeByStamp(nmy.getTimestamp());
                String md5 = nmy.getAudio().getMd5();
                UserInfo info = CommonPreferences.getUserInfo();
                ArrayList<NetFsDownloadReq> downloadList = new ArrayList<NetFsDownloadReq>();
                downloadList.add(new NetFsDownloadReq(md5, 0));
                String fileUrl = PropertiesUtils.getInstance().getFileURL();
                String path = MainApp.i().createNewSoundFile(md5);
                Message message = new Message();
                NetFsDownloadResponse resp = null;
                int dc = 0;
                boolean needTry = false;
                // 　重试下载
                ResFile file = new ResFile();
                file.setPath(path);
                do {
                    SLog.d(TAG, "\t>>>>>>>> [" + info.skyid + "]\t 下载语音文件[" + md5 + "] " + dc);
                    resp = settingsNetModule.downloadFs(fileUrl,
                            info.skyid,
                            info.token, downloadList);
                    if (resp == null || resp.isFailed()) {
                        SLog.d(TAG, "\t>>>>>>>> [" + dc + "] 下载失败,需要重试去 [" + info.skyid
                                + "]\t 下载语音文件 ");
                        needTry = true;
                    }

                    if (null != resp && resp.isSuccess()) {
                        needTry = false;
                    }
                } while (needTry && (++dc < 2));
                if (resp.isSuccess() && resp.getFileList().size() > 0 && path != null) {
                    byte[] data = resp.getFileList().get(0).getFileData();
                    try {
                        FileOutputStream out = new FileOutputStream(path);
                        out.write(data);
                        out.flush();
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    message.setStatus(MessagesColumns.STATUS_SUCCESS);
                    file.setSize(resp.getFileList().get(0).getFileSize());
                    file.setLength(nmy.getAudio().getAudioLen());
                } else {
                    Log.e(TAG, "Voice download fail!!!");
                    message.setStatus(MessagesColumns.STATUS_FAILED);
                    file.setSize(0);
                    file.setLength(nmy.getAudio().getAudioLen());
                }
                // 保存文件数据
                file.setVersion(ResFile.VERSION);
                file.setFormat(Constants.VOICE_EXT_NAME);
                file.setUrl(md5);
                long fileId = mResFilesDAO.addFile(file);

                // 添加消息数据
                List<Message> messages = new ArrayList<Message>();
                message.setContent(String.valueOf(fileId));
                message.setDate(receiveTime);
                message.setType(MessagesColumns.TYPE_VOICE);
                message.setTalkReason(nmy.getTalkReason());
                messages.add(message);
                addChatMsg(nickname, skyID, messages);
            }
                break;
            case CoreServiceMSG.MSG_CHATMSG_CARDMSG_RECEIVE: {
                Log.i(TAG, "receive Card Msg.....");
                NetVCardNotify nmy = (NetVCardNotify) obj;
                String nickname = nmy.getNickname();
                int skyID = nmy.getSkyid();
                Map cardMap = nmy.getvCardContentMap();
                receiveTime = DateUtil.getLongTimeByStamp(nmy.getTimestamp());
                /*
                 * addChatMsg(nickname, skyID, ParserUtils.encodeVCard(cardMap),
                 * receiveTime, Message.TYPE_CARD);
                 */
                List<Message> messages = new ArrayList<Message>();
                Message message = new Message();
                message.setContent(String.valueOf(ParserUtils.encodeVCard(cardMap)));
                message.setDate(receiveTime);
                message.setType(MessagesColumns.TYPE_CARD);
                messages.add(message);
                addChatMsg(nickname, skyID, messages);

            }
                break;
            case CoreServiceMSG.MSG_CHATMSG_MARKETMSG_RECEIVE: {
                NetMarketingMessageNotify nmy = (NetMarketingMessageNotify) obj;
                int skyID = Constants.HELPER_SKY_ID;// 小助手消息
                String name = Constants.HELPER_NAME;
                StringBuffer content = new StringBuffer("");
                if (nmy.getTitle() != null) {
                    content.append(nmy.getTitle());
                }
                if (nmy.getContent() != null) {
                    content.append("\n" + nmy.getContent());
                }
                if (nmy.getUrl() != null) {
                    content.append(nmy.getUrl());
                }
                /*
                 * addChatMsg(name, skyID, content.toString(), receiveTime,
                 * Message.TYPE_TEXT);
                 */
                List<Message> messages = new ArrayList<Message>();
                Message message = new Message();
                message.setContent(content.toString());
                message.setDate(receiveTime);
                message.setType(MessagesColumns.TYPE_TEXT);
                messages.add(message);
                addChatMsg(name, skyID, messages);
                Log.i(TAG, "receive 小助手（market） Msg.....");
            }
                break;
            case CoreServiceMSG.MSG_CHATMSG_SYSTEMMSG_RECEIVE: {
                NetSysMsgNotify nmy = (NetSysMsgNotify) obj;
                int skyID = Constants.HELPER_SKY_ID;// 小助手消息
                String name = Constants.HELPER_NAME;
                String content = nmy.getMsgContent();
                receiveTime = DateUtil.getLongTimeByStamp(nmy.getTimestamp());
                /*
                 * addChatMsg(name, skyID, content, receiveTime,
                 * Message.TYPE_TEXT);
                 */
                List<Message> messages = new ArrayList<Message>();
                Message m = new Message();
                m.setContent(content);
                m.setDate(receiveTime);
                m.setType(MessagesColumns.TYPE_TEXT);
                messages.add(m);
                addChatMsg(name, skyID, messages);
                Log.i(TAG, "receive 小助手（system） Msg.....");

                // 对于系统消息，可能是注册或者绑定
                /**
                 * 1：激活成功 2：绑定成功 3：换绑成功 4：重置密码成功
                 */
                if (null != service.getHandler()) {
                    switch (nmy.getResultType()) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            ArrayList<String> msgList = new ArrayList<String>();
                            msgList.add(content);
                            android.os.Message message = new android.os.Message();
                            message.what = CoreServiceMSG.MSG_SMSMSG_RECEIVE_COMMON_NET;
                            message.obj = msgList;
                            service.getHandler().sendMessage(message);
                            break;
                    }
                }
            }
                break;
            case CoreServiceMSG.MSG_CHATMSG_FRIENDSMSG_RECEIVE: {
                // NetFriendsMsgNotify nmy = (NetFriendsMsgNotify) obj;
                // int skyID = Constants.HELPER_SKY_ID; // 小助手消息
                // String name = Constants.HELPER_NAME;
                // ArrayList<FriendsList> list = nmy.getFriendsList();
                // int skyid = SettingsPreferences.getSKYID();
                // if (skyid > 0) {
                // receiveAddFriendMsg(receiveTime, skyID, name, list);
                // MainApp.getInstance().getReceiveFriends().clear();
                // } else {
                // MainApp.getInstance().putReceiveFriends(RECEVIE_FRIENDS,
                // list);
                // }
                // Log.i(TAG, "receive 小助手（friend） Msg.....");
            }
                break;
            case CoreServiceMSG.MSG_TRAFFIC_NOTIFY_MSG:
                // 流量统计入库操作
                if (null != obj) {
                    Long pack = (Long) obj;
                    if (pack > 0) {
                        insertTraffic(pack);
                    }
                }
                break;
            default:
                break;
        }
    }

    /*
     * public void addChatMsg(final String nickname, final int skyID, final
     * String content, final long receiveTime, final int type) {
     */
    public void addChatMsg(final String nickname, final int skyID, final List<Message> messages) {
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                // 更新手信数据库中相应会话内容
                for (Message message : messages) {
                    Threads threads = new Threads();
                    if (message.getType() == MessagesColumns.TYPE_VOICE) {
                        threads.setContent(MainApp.i().getResources()
                                .getString(R.string.message_list_voice_item));
                    } else if (message.getType() == MessagesColumns.TYPE_CARD) {
                        threads.setContent(MainApp.i().getResources()
                                .getString(R.string.message_list_card_item));
                    } else if (message.getType() == MessagesColumns.TYPE_FRD) {
                        threads.setContent(MainApp.i().getResources()
                                .getString(R.string.message_list_frd_item));
                    }
                    else {
                        threads.setContent(message.getContent());
                    }
                    // beginTransaction();
                    Message msg = new Message();
                    try {
                        Address address = new Address();
                        address.setSkyId(skyID);
                        Account account = getAccountByAddress(address);
                        if (null != account) {// 在联系人列表。获取phone，accountId
                            address = getAddressByAccount(account);
                        }

                        if (address.getId() <= 0) {
                            address = getAddressByAddressAdd(address);
                        } else {
                            mAddressDAO.updateAddress(address);
                        }

                        threads.setDisplayName(nickname);
                        // threads.setAccountIds(accountID);\
                        threads.setAddressIds(String.valueOf(address.getId()));
                        threads.setRead(MessagesColumns.READ_NO);
                        threads.setDate(message.getDate());
                        /*
                         * long threadID =
                         * mMessagesDAO.getThreadsByPhonesOrAccountIds( null,
                         * accountID);
                         */

                        Threads t = mMessagesDAO.getThreadsByAddressId(address.getId());
                        // Log.i(TAG, "addChatMsg threadID = " + threadID);
                        if (null == t) {
                            threads.setId(mMessagesDAO.addThreads(threads));
                            ArrayList<Contact> clist = mContactsDAO.getContactBySkyid(skyID);
                            if (!strangerDAO.checkExistsSkyid(skyID)) {
                                // TODO　判断联系人是否为空
                                if (null == clist || clist.size() == 0) {
                                    // 陌生人表和联系人表都找不到该用户
                                    Contact contact = contactsNetModule.getContactBySkyID(skyID);
                                    if (null != contact && contact.getAccounts().size() > 0) {
                                        contact.setSkyid(skyID);
                                        String nickName = contact.getAccounts().get(0)
                                                .getNickName();
                                        String skyName = contact.getAccounts().get(0)
                                                .getSkyAccount();
                                        long id = strangerDAO.addStranger(contact, nickName,
                                                skyName);
                                        contact.setId(id);
                                    }
                                } else {
                                    // 如果查到的联系人属于陌生人或者LBS陌生人，则也要加到陌生人表中
                                    for (Contact contact : clist) {
                                        // TODO 添加为陌生人
                                        if (null != contact && contact.getAccounts().size() > 0) {
                                            if (contact.getUserType() == ContactsColumns.USER_TYPE_STRANGER
                                                    || contact.getUserType() == ContactsColumns.USER_TYPE_LBS_STRANGER) {
                                                contact.setSkyid(skyID);
                                                String nickName = contact.getAccounts().get(0)
                                                        .getNickName();
                                                String skyName = contact.getAccounts().get(0)
                                                        .getSkyAccount();
                                                long id = strangerDAO.addStranger(contact,
                                                        nickName, skyName);
                                                contact.setId(id);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            threads.setId(t.getId());
                            mMessagesDAO.updateThreads(threads);
                        }

                        // 更新手信数据库中相应短信内容
                        msg.setContent(message.getContent());
                        msg.setDate(message.getDate());
                        msg.setOpt(MessagesColumns.OPT_FROM);
                        msg.setType(message.getType());
                        msg.setRead(MessagesColumns.READ_NO);
                        msg.setThreadsID(threads.getId());
                        msg.setStatus(MessagesColumns.STATUS_SUCCESS);
                        msg.setSequence_id(mMessagesDAO.getSequenceId(false,
                                msg.getSms_id()));
                        msg.setTalkReason(message.getTalkReason());
                        if (!TextUtils.isEmpty(msg.getTalkReason()))
                            MainApp.i().putTalkReason(threads.getId(),
                                    msg.getTalkReason());
                        mMessagesDAO.addMessage(msg);
                    } finally {
                        // endTransaction(true);
                    }
                    Log.i(TAG, "addChatMsg from = " + nickname + ",content = "
                            + message.getContent());
                    service.notifyObservers(CoreServiceMSG.MSG_CHATMSG_TEXTMSG_RECEIVE,
                            msg);
                    msg.setContent(threads.getContent());
                    service.showNotification(msg);
                }
            }
        });
    }

    public void retryDownloadVoiceMsg(final Message msg, final ResFile file, final int pos)
            throws Exception {
        mSyncPool.execute(new Runnable() {
            @Override
            public void run() {
                String md5 = file.getUrl();
                UserInfo info = CommonPreferences.getUserInfo();
                ArrayList<NetFsDownloadReq> downloadList = new ArrayList<NetFsDownloadReq>();
                downloadList.add(new NetFsDownloadReq(md5, 0));
                String fileUrl = PropertiesUtils.getInstance().getFileURL();
                String path = file.getPath();
                NetFsDownloadResponse resp = null;
                int dc = 0;
                boolean needTry = false;
                do {
                    Log.d(TAG, "\t>>>>>>>> retryDownloadVoiceMsg [" + info.skyid + "]\t 下载语音文件["
                            + md5
                            + "] " + dc);
                    resp = settingsNetModule.downloadFs(fileUrl,
                            info.skyid,
                            info.token, downloadList);
                    if (resp == null || resp.isFailed()) {
                        Log.d(TAG, "\t>>>>>>>> retryDownloadVoiceMsg [" + dc + "] 下载失败,需要重试去 ["
                                + info.skyid
                                + "]\t 下载语音文件 ");
                        needTry = true;
                    }

                    if (null != resp && resp.isSuccess()) {
                        needTry = false;
                    }
                } while (needTry && (++dc < 2));
                if (null != resp && resp.isSuccess() && resp.getFileList().size() > 0
                        && path != null) {
                    byte[] data = resp.getFileList().get(0).getFileData();
                    try {
                        FileOutputStream out = new FileOutputStream(path);
                        out.write(data);
                        out.flush();
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    msg.setStatus(MessagesColumns.STATUS_SUCCESS);
                    file.setSize(resp.getFileList().get(0).getFileSize());
                    file.setLength(file.getLength());

                    // 更新Message 和 ResFile 数据记录
                    mMessagesDAO.updateMessage(msg);
                    mResFilesDAO.updateFile(file);

                    // MediaHelper.getInstance().playVoice(path, pos);
                    // 提示重新下载成功
                    service.notifyObservers(CoreServiceMSG.MSG_VOICE_RETRY_SUCCESS,
                            pos);
                } else {
                    // 提示重新下载失败
                    service.notifyObservers(CoreServiceMSG.MSG_VOICE_RETRY_FAILED,
                            null);
                }
            }
        });
    }

    /**
     * @param msgId
     */
    public void deleteMessage(long msgId) {
        mMessagesDAO.deleteMessage(msgId);
    }

    /**
     * @param message
     */
    public void deleteMessage(Message message) {
        if (message.getType() == MessagesColumns.TYPE_FRD) {
            friendsDAO.deleteFriendAndContactWithTransaction(Long.valueOf(message.getContent()));
        }
        mMessagesDAO.deleteMessage(message.getId());
        mMessagesDAO.updateThreadsContent(message.getThreadsID());
        if (message.getSms_id() > 0)
            mMessagesDAO.deleteSMS(message.getSms_id());
    }

    /**
     * 通过threads ID获取该会话的消息列表
     * 
     * @param mCurThreadsId
     * @param start
     * @param count
     * @return
     */
    public List<android.skymobi.messenger.bean.Message> getMessageList(
            long mCurThreadsId, int start, int count) {
        if (mCurThreadsId <= 0) {
            return new ArrayList<android.skymobi.messenger.bean.Message>();
        }
        return mMessagesDAO.getMessageList(mCurThreadsId, start, count);
    }

    /**
     * 通过条件获取该会话的消息列表
     * 
     * @param whereClause
     * @return
     */
    public List<android.skymobi.messenger.bean.Message> getMessageList(String whereClause) {

        return mMessagesDAO.getMessageList(-1, -1, -1, whereClause);
    }

    /**
     * 更新会话的读状态
     * 
     * @param mCurThreadsId
     * @return
     */
    public void updateReadStatus(final long mCurThreadsId) {
        // 手信会话已读状态更新阻塞完成，避免线程没跑，返回到会话界面，状态未更新。本地短信会话，可以在线程里完成
        mMessagesDAO.updateThreadsReadStauts(mCurThreadsId,
                MessagesColumns.READ_YES);

        Threads threads = mMessagesDAO.getThreadsByID(mCurThreadsId);
        if (threads == null)
            return;
        mMessagesDAO.updateLocalThreadsReadStauts(threads.getLocalThreadsID(),
                MessagesColumns.READ_YES);

    }

    /**
     * 异步执行将流量统计入库操作
     * 
     * @param pack
     */
    public void insertTraffic(final Long pack) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Context ctx = MainApp.i();
                int type = AndroidSysUtils.getNetworkType(ctx);
                if (type == -1 || !service.isNetConnected()) {
                    // 没有网络或者联网失败，则不执行入库操作
                    return;
                }
                Traffic traffic = null;
                String cdate = DateUtil.getCurrentDate();
                // 判断网络类型
                switch (type) {
                    case 0:
                        // 移动网络
                        traffic = new Traffic();
                        traffic.setDate(cdate);
                        // 流量算法:使用流量+TCP头(20字节)+IP头(20字节)
                        traffic.setAppMobile(Long.valueOf(pack).intValue() + 20 + 20);
                        break;
                    case 1:
                        // 无线网络
                        traffic = new Traffic();
                        traffic.setDate(cdate);
                        // 流量算法:使用流量+TCP头(20字节)+IP头(20字节)
                        traffic.setAppWifi(Long.valueOf(pack).intValue() + 20 + 20);
                        break;
                }
                // 将流量数据入库
                if (null != trafficDao) {
                    // 流量对象非空判断
                    if (null != traffic) {
                        Traffic tmp = new Traffic();
                        tmp.setDate(cdate);
                        // 判断是否存在同一天的数据
                        List<Traffic> list = trafficDao.query(tmp);
                        if (null != list && list.size() > 0) {
                            // 存在则更新当前数据
                            Traffic tmp2 = list.get(0);
                            // 构造更新数据,需要将数据库中的数据累增
                            traffic.setAppMobile(tmp2.getAppMobile() + traffic.getAppMobile());
                            traffic.setAppWifi(tmp2.getAppWifi() + traffic.getAppWifi());
                            traffic.setMobile(tmp2.getMobile() + traffic.getMobile());
                            traffic.setMobileLatest(tmp2.getMobileLatest()
                                    + traffic.getMobileLatest());
                            traffic.setWifi(tmp2.getWifi() + traffic.getWifi());
                            traffic.setWifiLatest(tmp2.getWifiLatest() + traffic.getWifiLatest());
                            trafficDao.update(traffic, TrafficColumns.DATE + "=?",
                                    new String[] {
                                        cdate
                                    });
                        } else {
                            // 不存在当天数据，就添加当天数据
                            trafficDao.insert(traffic);
                        }
                    }
                }
            }
        });
    }

    /**
     * 获取未读的会话列表
     * 
     * @return
     */
    public List<Threads> getUnreadThreads() {
        List<Threads> threadsList = mMessagesDAO.getUnreadThreads();
        for (Threads threads : threadsList) {
            threads.setAddressList(getAddressList(threads.getAddressIds()));
        }
        return threadsList;
    }

    /**
     * 获取未读短信的总条数
     * 
     * @return
     */
    public int getTotalUnreadMessageCount() {
        return mMessagesDAO.getTotalUnreadMessageCount();
    }

    /**
     * 获取未读会话的条数
     * 
     * @return
     */
    public int getUnreadThreadsCount() {
        return getUnreadThreads().size();
    }

    /**
     * 通过会话ID获取该会话未读的消息列表
     */
    public List<Message> getUnreadMessage(long threadsID) {
        return mMessagesDAO.getUnreadMessageByThreadsID(threadsID);
    }

    /**
     * 通过会话ID获取该会话未读的消息条数
     */
    public int getUnreadMessageCount(long threadsID) {
        return getUnreadMessage(threadsID).size();
    }

    /**
     * 获取会话列表
     * 
     * @return
     */
    public List<Threads> getThreadsList() {
        List<Threads> threadsList = new ArrayList<Threads>();
        List<Threads> tempList = mMessagesDAO.getThreadsList();
        for (Threads t : tempList) {
            if (threadsList.size() > 0) {
                Threads lastThreads = threadsList.get(threadsList.size() - 1);
                if (lastThreads.getId() == t.getId()) {
                    Address address = new Address();
                    address.setId(t.getAddressList().get(0).getId());
                    address.setPhone(t.getAddressList().get(0).getPhone());
                    address.setSkyId(t.getAddressList().get(0).getSkyId());
                    lastThreads.addAddress(address);
                } else {
                    threadsList.add(t);
                }
            } else {
                threadsList.add(t);
            }
        }
        return threadsList;
    }

    // public void updateAddressForThreadsList(List<Threads> threadsList) {
    // for (Threads threads : threadsList) {
    // List<Address> addressList = getAddressList(threads.getAddressIds());
    // for (Address address : addressList) {
    // Account account = getAccountByAddress(address);
    // if (null != account) {
    // address.setSkyId(account.getSkyId());
    // address.setPhone(account.getPhone());
    // }
    // }
    // threads.setAddressList(addressList);
    // }
    // }

    /**
     * 根据Id获取会话列表
     * 
     * @param id
     * @return
     */
    public Threads getThreadsById(long id) {
        return mMessagesDAO.getThreadsByID(id);
    }

    /**
     * 通过会话ID删除会话
     * 
     * @param threadId
     */
    public void removeThreads(Threads threads) {
        mMessagesDAO.removeThreads(threads.getId());
        if (threads.getLocalThreadsID() > 0)
            mMessagesDAO.removeLocalThreads(threads.getLocalThreadsID());
        mAddressDAO.deleteAddress(threads.getAddressIds());
    }

    /**
     * 通过会话ID删除会话
     * 
     * @param threadId
     */
    public void removeThreads(List<Threads> threadsList) {
        StringBuilder threadsIds = new StringBuilder();
        StringBuilder LocalThreadsIds = new StringBuilder();
        StringBuilder addressIds = new StringBuilder();
        for (Threads threads : threadsList) {
            threadsIds.append(threads.getId() + ",");
            LocalThreadsIds.append(threads.getLocalThreadsID() + ",");
            addressIds.append(threads.getAddressIds() + ",");
        }
        if (threadsIds.length() > 0) {
            mMessagesDAO.delete(ThreadsColumns.TABLE_NAME,
                    "_id in(" + threadsIds.substring(0, threadsIds.length() - 1) + ")", null);
        }

        if (LocalThreadsIds.length() > 0) {
            mMessagesDAO.removeLocalThreadsList(LocalThreadsIds.substring(0,
                    LocalThreadsIds.length() - 1));
        }

        mAddressDAO.deleteAddress(addressIds.substring(0, addressIds.length() - 1));
    }

    /**
     * 通过addressId获取threads
     * 
     * @param addressIds
     * @return
     */
    public Threads getThreadsByAddressIds(String addressIds) {
        return mMessagesDAO.getThreadsByAddressId(addressIds);
    }

    /**
     * 通过电话号码查看联系人的名字
     * 
     * @param phones
     * @return
     */
    /*
     * public String getDisplayName(Threads threads) { String displayName =
     * mMessagesDAO.getContactByPhonesOrAccountIds( threads.getPhones(),
     * threads.getAccountIds()); // 如果名字和accountids一样 则显示threads的displayname if
     * (displayName.equalsIgnoreCase(threads.getAccountIds())) { Account account
     * = getAccountByAccountID(threads.getAccountIds()); if (account != null) {
     * // 如果陌生手机号码，直接显示手机号吗 // 如果是陌生手信用户，直接显示昵称 displayName = account.getPhone()
     * != null ? account.getPhone() : account .getNickName(); } } return
     * displayName; }
     */

    /**
     * 根据addressId获取显示名称
     * 
     * @param addressIds
     * @return
     */

    /*
     * public String getDisplayName(Threads threads) { List<Address> addressList
     * = getAddressList(threads.getAddressIds()); return getDisplayName(threads,
     * addressList);
     */

    /**
     * 根据addressList获取显示名称
     * 
     * @param addressList
     * @return
     */
    public String getDisplayName(Threads threads, List<Address> addressList) {
        StringBuilder displayName = new StringBuilder();
        for (Address address : addressList) {
            // // 首先从联系人列表缓存中取名字
            String name = ContactListCache.getInstance()
                    .getDisplayNameBySkyIdOrPhone(threads,
                            AndroidSysUtils.removeHeader(address.getPhone()), address.getSkyId());
            // 如果查到的是空，表示是陌生人，那么我们用会话的displayname代替掉
            if (null == name && address.getSkyId() > 0 && threads != null) {
                name = threads.getDisplayName();
            }
            // 如果name仍然为空，那么我们就显示空字符串而不是null
            name = (name == null) ? "" : name;
            displayName.append(name + ",");

        }
        return displayName.length() > 1 ? displayName.substring(0, displayName.length() - 1) : "";
    }

    /**
     * 根据addressList获取PhotoId
     * 
     * @param addressList
     * @return
     */
    public ArrayList<String> getPhotoIds(Threads threads, List<Address> addressList) {
        ArrayList<String> photoIds = new ArrayList<String>();
        for (Address address : addressList) {
            // // 首先从联系人列表缓存中取名字
            String id = ContactListCache.getInstance()
                    .getPhotoIdByAccountIdOrSkyIdOrPhone(0,
                            AndroidSysUtils.removeHeader(address.getPhone()), address.getSkyId());
            if (!StringUtil.isBlank(id))
                photoIds.add(id);
        }
        if (photoIds.size() == 0) {
            // 从陌生人表中拿斯凯用户的图片ID
            for (Address address : addressList) {
                Stranger stranger = strangerDAO.fetch(address.getSkyId());
                if (null != stranger) {
                    if (!StringUtil.isBlank(stranger.getPhotoId())) {
                        photoIds.add(stranger.getPhotoId());
                    }
                }
            }
        }
        return photoIds;
    }

    /**
     * 通过phones或者skyID, nikcname获取accountIDs（一定会有值，因为没有account会主动加一条account）
     * 
     * @param phones
     * @param skyID skyAccount
     * @param skyAccount
     * @param nickname
     * @return
     */
    /*
     * public String getAccoutIdByPhoneOrSkyid(String phones, int skyID, String
     * skyAccount, String nickname) { return
     * mContactsDAO.getAccoutIdByPhoneOrSkyid(phones, skyID, skyAccount,
     * nickname); }
     */

    public Account getAccountByAddress(Address address) {
        return mContactsDAO.getAccoutByAddress(address);
    }

    /**
     * 通过accountId获取Account
     * 
     * @param accountID
     * @return
     */
    public Account getAccountByAccountID(long accountID) {
        return mContactsDAO.getAccountByAccountID(accountID);
    }

    /**
     * 通过contact id获取名片map结构体
     * 
     * @param contactID
     * @return
     */
    public Map<String, Object> getCardByContactId(long contactID) {
        return mContactsDAO.getCardByContactId(contactID);
    }

    /**
     * 根据messages表的content获取path
     * 
     * @param content
     * @return
     */
    public String getPathByContent(String content) {
        if (null != content && !TextUtils.isEmpty(content)) {
            ResFile file = mResFilesDAO.getFile(Long.valueOf(content));
            if (null != file) {
                return file.getPath();
            }
        }
        return null;
    }

    /**
     * 根据messages表的content获取path
     * 
     * @param content
     * @return
     */
    public ResFile getResFileByContent(String content) {
        ResFile file = null;
        if (null != content && !TextUtils.isEmpty(content)) {
            file = mResFilesDAO.getFile(Long.valueOf(content));
        }
        return file;
    }

    public long addResFile(ResFile file) {
        return mResFilesDAO.addFile(file);
    }

    public Address getAddress(long id) {
        return mAddressDAO.getAddress(id);
    }

    public List<Address> getAddressList(String ids) {
        return mAddressDAO.getAddressListByIds(ids);
    }

    public Address getAddressBySkyIdOrPhone(int skyId, String phone) {
        Address address = null;
        if (null == address && null != phone) {
            address = mAddressDAO.getAddressByPhone(phone);
        }
        if (null == address && skyId > 0) {
            address = mAddressDAO.getAddressBySkyId(skyId);
        }

        return address;

    }

    public Address getAddressByAccount(Account account) {
        Address address = getAddressBySkyIdOrPhone(account.getSkyId(),
                account.getPhone());

        if (null == address) {
            address = new Address();
        }
        // if (account.getId() > 0)
        // address.setAccountId(account.getId());
        if (!TextUtils.isEmpty(account.getPhone()))
            address.setPhone(account.getPhone());
        if (account.getSkyId() > 0)
            address.setSkyId(account.getSkyId());

        return address;

    }

    public Address getAddressByAddress(Address address) {
        return getAddressBySkyIdOrPhone(address.getSkyId(),
                address.getPhone());

    }

    public Address getAddressByAddressAdd(Address a) {
        Address address = getAddressBySkyIdOrPhone(a.getSkyId(),
                a.getPhone());
        if (null == address) {
            address = new Address();
        }
        // if (a.getAccountId() > 0)
        // address.setAccountId(a.getAccountId());
        if (!TextUtils.isEmpty(a.getPhone()))
            address.setPhone(a.getPhone());
        if (a.getSkyId() > 0)
            address.setSkyId(a.getSkyId());
        if (address.getId() <= 0)
            address.setId(mAddressDAO.addAddress(address));
        return address;

    }

    public long AddAddress(Address address) {
        return mAddressDAO.addAddress(address);
    }

    public int getMessageCountToday(Threads threads) {
        return mMessagesDAO.getMessageList(threads.getId(), -1, -1, "date> and date< ").size();
    }

    public void deleteUserData() {
        mMessagesDAO.deleteUserData();
        CommonPreferences.saveContactsLastTimeUpdate(0);
    }

    public String getDistanceText(int distance) {
        StringBuilder distanceStr = new StringBuilder();
        if (distance < 1000) {
            distanceStr.append(distance).append("米内");
        } else {
            distanceStr.append(distance / 1000).append("公里内");
        }

        return distanceStr.toString();

    }

    public int updateThreadsDraft(long id, String draft) {
        if (id <= 0)
            return 0;
        Threads threads = new Threads();
        threads.setId(id);
        threads.setDraft(draft);
        return mMessagesDAO.updateThreads(threads);
    }

    public void checkSMS() {
        if (MainApp.i().getLastSyncThreadsTime() == 0) {// 如果没有同步过，先进行一次同步
            syncSMSThreads(true);
            return;
        }
        mSyncPool.execute(new Runnable() {

            @Override
            public void run() {
                Log.i(TAG, "-----checkSMS:" + MessageModule.class);
                Cursor cursorLocalMessage = null;
                ThreadsObserver threadsObserver = service.getThreadsObserver();
                long lastCursor = mMessagesDAO.getMaxMessagesSmsId();
                long currentMaxSMSID = 0;
                ContentResolver resolver = MainApp.i().getContentResolver();
                cursorLocalMessage = resolver.query(Uri.parse("content://sms/"),
                        new String[] {
                            "MAX(_id),count(*)"
                        }, null, null, null);
                if (null == cursorLocalMessage) {
                    Log.e(TAG, "-----无法获取本地短信");
                    return;
                }

                if (cursorLocalMessage.getCount() > 0
                        && cursorLocalMessage.moveToNext()) {
                    currentMaxSMSID = cursorLocalMessage.getLong(0);
                    // smsCount = cursorLocalMessage.getLong(1);
                }
                cursorLocalMessage.close();
                if (currentMaxSMSID == 0) {
                    return;
                }
                Log.e(TAG, "-----lastCursor=" + lastCursor + " currentMaxSMSID="
                        + currentMaxSMSID);
                if (lastCursor < currentMaxSMSID) {
                    Log.i(TAG, "-----新增短信");
                    cursorLocalMessage = resolver.query(Uri.parse("content://sms/"),
                            new String[] {
                                    "_id", "thread_id", "address", "date", "read",
                                    "type", "body", "status"
                            }, "_id>? and type<>3", new String[] {
                                lastCursor + ""
                            }, null);

                    if (null == cursorLocalMessage) {
                        Log.i(TAG, "-----无法获取本地短信");
                        return;
                    }
                    while (cursorLocalMessage.moveToNext()) {
                        android.skymobi.messenger.bean.Message msg = new android.skymobi.messenger.bean.Message();
                        msg.setSms_id(cursorLocalMessage.getLong(0));
                        msg.setLocalThreadsID(cursorLocalMessage.getLong(1));
                        msg.setPhones(AndroidSysUtils.removeHeader(cursorLocalMessage.getString(2)));
                        msg.setDate(cursorLocalMessage.getLong(3));
                        msg.setRead(cursorLocalMessage.getInt(4));
                        msg.setType(cursorLocalMessage.getInt(5));
                        msg.setOpt(msg.getType());
                        msg.setContent(cursorLocalMessage.getString(6));
                        msg.setStatus(AndroidSysUtils.getStatus(cursorLocalMessage.getInt(7),
                                cursorLocalMessage.getInt(5)));
                        Log.i(TAG,
                                "-----" + msg.toString());
                        if (msg.getOpt() == MessagesColumns.OPT_FROM) {
                            if (msg.getRead() == MessagesColumns.READ_NO) {
                                Log.i(TAG, "-----收到短信");
                                if (selfSendReceive(msg.getDate())) {
                                    Log.i(TAG, "-----通过手信拦截，不处理这个onChange");
                                } else {
                                    Log.i(TAG, "-----本地自带短信收到");
                                    onReceiveSMS(msg);
                                    threadsObserver.setIgnore(threadsObserver.getIgnore() + 1);
                                }
                            }
                        } else if (msg.getOpt() == MessagesColumns.OPT_TO
                                || msg.getOpt() == MessagesColumns.OPT_SENDING
                                || msg.getOpt() == MessagesColumns.OPT_SENDSTART) {
                            Log.i(TAG, "-----发送短信");
                            if (selfSendReceive(msg.getDate())) {
                                Log.i(TAG, "-----通过手信发送，不处理这个onChange");
                                // onSendSMS(msg, true);
                            } else {
                                Log.i(TAG, "-----本地自带短信发送");
                                threadsObserver.setIgnore(threadsObserver.getIgnore()
                                        + 2);
                                // 默认成功
                                msg.setStatus(MessagesColumns.STATUS_SUCCESS);
                                onSendSMS(msg, false);
                            }

                        }
                    }
                    // lastCursor = currentMaxSMSID;

                } else if (lastCursor >= currentMaxSMSID) {
                    Log.i(TAG, "-----短信状态更新/删除");
                    mMessagesDAO.syncLocalMessage(true);
                    mMessagesDAO.updateThreadsContent(-1);
                    service.notifyObservers(
                            CoreServiceMSG.MSG_MESSAGES_SYNC_END, 0);
                    // lastCursor = currentMaxSMSID;
                }

                if (cursorLocalMessage != null) {
                    cursorLocalMessage.close();
                }

            }
        });

    }

    public void beginTransaction() {
        mBaseDAO.beginTransaction();
    }

    public void endTransaction(boolean isSuccess) {
        mBaseDAO.endTransaction(isSuccess);
    }

    public void createRegisterSMS(String content) {
        ContentValues values = new ContentValues();
        long receiveTime = System.currentTimeMillis();
        values.put("date", System.currentTimeMillis());
        values.put("read", MessagesColumns.READ_NO);
        values.put("type", MessagesColumns.OPT_FROM);
        values.put("address", "106550771605");
        values.put("body", content);
        mSelfSMSMap.put(receiveTime, true);
        Log.i(TAG, "-----mSelfSMSMap put date " + receiveTime);
        mMessagesDAO.addSMSForMessage(values);
    }

    /**
     * 发送邀请统计日志
     * 
     * @param msgContent 实际短信内容
     * @param destPhone 目标手机号码
     */
    private void doInviteCount(String msgContent, String destPhone) {
        SLog.d(TAG, "\t>>>> 进入邀请发送方法");
        if (StringUtil.isBlank(msgContent)) {
            SLog.d(TAG, "\t>>>> 短信内容为空,不发送邀请统计信息");
            return;
        }
        // 获取邀请文案
        String text = null;
        int inviteEntrance = MainApp.i().getInviteEntrance();

        // 获取服务端下发的短信邀请文案,获取当前邀请入口
        if (StringUtil.isBlank(text)
                && inviteEntrance == Constants.INVITE_ENTRANCE_CONTACTS) {
            text = CommonPreferences
                    .getInviteConfigContent(Constants.INVITE_CONFIGURATION_SMS_TYPE);
        }
        // 获取服务端下发的语音邀请文案,获取当前邀请入口
        if (StringUtil.isBlank(text)
                && inviteEntrance == Constants.INVITE_ENTRANCE_CHAT_VOICE) {
            text = CommonPreferences
                    .getInviteConfigContent(Constants.INVITE_CONFIGURATION_VOICE_TYPE);
        }
        // 获取本地内置的邀请文案
        if (StringUtil.isBlank(text)) {
            text = MainApp.i().getApplicationContext()
                    .getString(R.string.chat_voice_invite_context);
        }

        if (null != msgContent && msgContent.length() > 15 && msgContent.contains("http://")) {
            try {
                String tmpMsgContent = msgContent.substring(0, 15);
                String tmpText = text.substring(0, 15);
                if (tmpMsgContent.equals(tmpText)) {
                    MainApp.i().getLcsBU().sendInviteLog(SettingsPreferences.getMobile(),
                            destPhone, MainApp.i().getInviteEntrance());
                } else {
                    SLog.d(TAG, "\tzhang 邀请文案与本地或者服务端文案不一致,server=" + msgContent + ",local=" + text
                            + "," + tmpMsgContent
                            + "," + tmpText + ",inviteEntrance=" + inviteEntrance);
                }
            } catch (Exception e) {
                SLog.e(TAG, "发送邀请数据出现异常,异常信息为：" + e.getMessage());
            }
        } else {
            SLog.d(TAG, "发送统计数据条件未达到，短信内容不符合发送规则!");
        }
        // 重置邀请入口参数为-1
        MainApp.i().setInviteEntrance(-1);
    }
}
