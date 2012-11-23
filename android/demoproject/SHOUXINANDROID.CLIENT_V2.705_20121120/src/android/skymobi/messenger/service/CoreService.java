
package android.skymobi.messenger.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.ContactsContract;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.adapter.MessageListCache;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Threads;
import android.skymobi.messenger.bean.User;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.broadcast.NetChangedReceiver;
import android.skymobi.messenger.broadcast.SDCardReceiver;
import android.skymobi.messenger.cache.APPCache;
import android.skymobi.messenger.database.MessengerDatabaseHelper;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.database.observer.ContactsObserver;
import android.skymobi.messenger.database.observer.SMSObserver;
import android.skymobi.messenger.database.observer.ThreadsObserver;
import android.skymobi.messenger.network.BindChangeListener;
import android.skymobi.messenger.network.NetWorkListener;
import android.skymobi.messenger.network.NetWorkMgr;
import android.skymobi.messenger.service.module.CommonModule;
import android.skymobi.messenger.service.module.ContactsModule;
import android.skymobi.messenger.service.module.FastChatModule;
import android.skymobi.messenger.service.module.FriendModule;
import android.skymobi.messenger.service.module.MessageModule;
import android.skymobi.messenger.service.module.NearUserModule;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.service.module.StrangerModule;
import android.skymobi.messenger.sms.MessageReceiver;
import android.skymobi.messenger.sms.SendMsgHandler;
import android.skymobi.messenger.ui.ChatActivity;
import android.skymobi.messenger.ui.LoginActivity;
import android.skymobi.messenger.ui.MainActivity;
import android.skymobi.messenger.ui.Observer;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.TimeUtils;
import android.text.TextUtils;
import android.util.Log;

import com.skymobi.android.sx.codec.beans.clientbean.NetBindChangeNotify;
import com.skymobi.android.sx.codec.beans.clientbean.NetOnlineStateChangeNotify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * by zzy 2012-10-17, 
 * 修改CoreService为一个普通的业务对象，其Service只能由LifeService替换
 * 1.去掉继承extends Service
 * 2.init方法放在构造函数中
 * 3.去除onbind/onCreate/onDestroy
 * 4.需要使用到Context/ContextWrapper的地方，统一使用MainApp.i()
 * 5.提供一个退出方法Quit，完成原先onDestroy的流程
 */
public class CoreService { // by zzy extends Service {

    private static final String TAG = CoreService.class.getSimpleName();

    /*-----------初始化数据触发时机-------**/
    private static final int TRIGGER_TIME_GET_ONLINE_USER_STATUS = 1000 * 5; // 5秒后触发获取用户在线状态
    private static final int TRIGGER_TIME_GET_UNIFIED_COPYWRITER = 1000 * 15;// 15秒后获取统一文案
    private static final int TRIGGER_TIME_GET_USER_INFO = 1000 * 20; // 20秒后获取用户最新基本信息
    private static final int TRIGGER_TIME_GET_OFFLINE_MESSAGE = 1000 * 30; // 30秒后开始接收离线的消息
    private static final int TRIGGER_TIME_GET_IS_RECOMMENDED = 1000 * 35;// 35后获取是否可被推荐
    private static final int TRIGGER_TIME_RECALS_RECOMMEND_FRIENDS = 1000 * 60 * 2;// 2分钟后让服务端重新计算推荐好友
    private static final int TRIGGER_TIME_GET_SYSTEM_EXCITING_SMS = 1000 * 60 * 5;// 5分钟后获取系统下发的精彩短语
    private static final int TRIGGER_TIME_CHECK_BIND_STATUS = Constants.CHECK_BIND_INTERVAL; // 30分钟后判断用户绑定状态
    private static final int TRIGGER_TIME_LCS_DEVICELOG_INFO = 1000 * 5; // 20秒后发送终端信息日志

    // Binder返回service实例
    // private final ServiceBinder mServiceBinder = new ServiceBinder();
    // CallBackList列表，用户service给UI转换各类响应
    private final Set<Observer> mCallbackList = new HashSet<Observer>();

    // 线程池
    private ExecutorService mThreadPool;
    // 同步线程池
    private ExecutorService mSyncPool;

    // 各个事务模块
    private CommonModule commonModule;
    private MessageModule messageModule;
    private ContactsModule contactsModule;
    private FriendModule friendModule;
    private SettingsModule settingsModule;
    private NearUserModule nearUserModule;
    private StrangerModule strangerModule;
    private FastChatModule fastChatModule;
    // NotificationManager
    private NotificationManager mNotifiMgr;
    private Class<? extends Activity> clazz;
    public static final int NEW_MESSAGS_NOTIFICATION_ID = 0x1000;
    public static final int NEW_FASTCHAT_NOTIFICATION_ID = 0x1001;
    private static volatile CoreService sInstance = null;

    // 新增网络状态锁，防止出现多次重连
    private ContactsObserver contactObserver;

    public CoreService() {
        super();

        initThreadPool();
        if (mThreadPool != null) {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    init();
                    sInstance = CoreService.this;
                }
            });
        }
    }

    /*
     * @Override public IBinder onBind(Intent arg0) { SLog.d(TAG, "onBind..");
     * return mServiceBinder; } // WakeLock wakeLock = null;
     * @Override public void onCreate() { SLog.d(TAG, "onCreate..");
     * super.onCreate(); initThreadPool(); if (mThreadPool != null) {
     * mThreadPool.execute(new Runnable() {
     * @Override public void run() { init(); sInstance = CoreService.this; } });
     * } }
     */
    public void registerObserverForContacts() {
        SLog.d(TAG, "注册通讯录观察者对象..");
        if (contactObserver == null) {
            contactObserver = new ContactsObserver(mHandler, this);
        }
        MainApp.i().getContentResolver().registerContentObserver(
                ContactsContract.Data.CONTENT_URI, true, contactObserver);
    }

    public void unregisterObserverForContacts() {
        SLog.d(TAG, "反注册通讯录观察者对象..");
        if (null != contactObserver) {
            MainApp.i().getContentResolver().unregisterContentObserver(contactObserver);
        }
    }

    public void quit() {

        SLog.d(TAG, "quit..");

        unregisterObserverForContacts();
        // 反注册SMS receiver
        uninitSMSReceiver();
        // 反注册监听短信数据库
        unregisterObserverForSMSThreads();
        // 反注册net change receiver
        uninitNetChangeReceiver();

        // 清除通知栏
        cancelNotification();

        // NetWorkMgr.getInstance().getClient().logout();
        // 关闭数据库
        SLog.i(TAG, "关闭数据库..");
        MessengerDatabaseHelper.getInstance(MainApp.i()).shutdown();

        // 关闭线程池
        SLog.i(TAG, "关闭线程池..");
        if (mThreadPool != null) {
            mThreadPool.shutdownNow();
            mThreadPool = null;
        }
        SLog.i(TAG, "关闭同步线程池..");
        if (mSyncPool != null) {
            mSyncPool.shutdownNow();
            mSyncPool = null;
        }
        SLog.i(TAG, "关闭所有activity..");
        MainApp.i().closeAllActivity();
        SLog.i(TAG, "kill进程[" + Process.myPid() + "]!");
        Process.killProcess(Process.myPid());

    }

    /*
     * @Override public void onStart(Intent intent, int startId) { SLog.d(TAG,
     * "onStart..startId:"+startId); super.onStart(intent, startId);
     * Notification notice = new Notification(); notice.flags =
     * Notification.FLAG_FOREGROUND_SERVICE; startForeground(startId, notice); }
     * @Override public int onStartCommand(Intent intent, int flags, int
     * startId) { SLog.d(TAG,
     * "onStartCommand..flags:"+flags+",startId:"+startId); flags =
     * START_STICKY; return super.onStartCommand(intent, flags, startId); }
     * @Override public boolean onUnbind(Intent intent) { SLog.d(TAG,
     * "onUnbind.."); return super.onUnbind(intent); } public class
     * ServiceBinder extends Binder { public CoreService getService() { return
     * CoreService.this; } }
     */
    private boolean isConnected = true;
    // 网络类型
    private int netType;

    /**
     * Description:注册回调，由UI Activity来注册
     * 
     * @param obs
     */
    public synchronized void registerCallBack(Observer obs) {
        if (obs != null)
            mCallbackList.add(obs);
    }

    /**
     * Description:注销回调，由UI Activity来注销
     * 
     * @param obs
     */
    public synchronized void unregisterCallBack(Observer obs) {
        if (obs != null)
            mCallbackList.remove(obs);
    }

    MessageReceiver smsReceiver = null;

    // 为了提高优先级，动态注册一个短信收发receiver
    private void initSMSReceiver() {
        SLog.d(TAG, "注册短信接收广播..");
        smsReceiver = new MessageReceiver();
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.setPriority(Integer.MAX_VALUE);
        smsIntentFilter.addAction(MessageReceiver.ACTION_RECEIVE);
        smsIntentFilter.addAction(SendMsgHandler.ACTION_SENT);
        MainApp.i().registerReceiver(smsReceiver, smsIntentFilter);
    }

    // 注销短信收发receiver
    private void uninitSMSReceiver() {
        SLog.d(TAG, "反注册短信接收广播..");
        if (smsReceiver != null)
            MainApp.i().unregisterReceiver(smsReceiver);
    }

    NetChangedReceiver netChangedReceiver = null;

    // 动态注册网络状态变化的receiver
    private void initNetChangeReceiver() {
        SLog.d(TAG, "初始化网络切换接收广播..");
        netChangedReceiver = new NetChangedReceiver();
        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.setPriority(Integer.MAX_VALUE);
        netIntentFilter.addAction(NetChangedReceiver.ACTION_NET);
        MainApp.i().registerReceiver(netChangedReceiver, netIntentFilter);
    }

    SDCardReceiver sdCardReceiver = null;

    // 动态注册sd卡拨插变化的receiver
    private void initSDCardChangeReceiver() {
        SLog.d(TAG, "初始化SDcard监控接收广播..");
        sdCardReceiver = new SDCardReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);// 如果SDCard未安装,并通过USB大容量存储共享返回
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard已卸掉,如果SDCard是存在但没有被安装
        filter.addAction(Intent.ACTION_MEDIA_CHECKING); // 表明对象正在磁盘检查
        filter.addAction(Intent.ACTION_MEDIA_EJECT); // 物理的拔出 SDCARD
        filter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
        /*
         * filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
         * filter.addAction(Intent.ACTION_MEDIA_BUTTON);
         * filter.addAction(Intent.ACTION_MEDIA_NOFS);
         * filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
         * filter.setPriority(Integer.MAX_VALUE);
         */
        filter.addDataScheme("file");
        MainApp.i().registerReceiver(sdCardReceiver, filter);
    }

    // 注销网络变化的receiver
    private void uninitNetChangeReceiver() {
        SLog.d(TAG, "反注册网络切换接收广播..");
        if (netChangedReceiver != null)
            MainApp.i().unregisterReceiver(netChangedReceiver);
    }

    private void init() {
        SLog.d(TAG, "全面初始化一些基础信息..");
        final long beginInitTime = System.currentTimeMillis();
        // 初始化短信receiver
        initSMSReceiver();
        // 注册联系人数据库变化监听
        registerObserverForContacts();
        // 注册短信数据库变化监听
        registerObserverForSMSThreads();
        // 初始化网络状态变化的receiver
        initNetChangeReceiver();
        // 监控sd卡拨插变化的receiver
        initSDCardChangeReceiver();
        // 初始化数据库
        SLog.d(TAG, "初始化数据库..");
        MessengerDatabaseHelper.getInstance(MainApp.i());
        SLog.i(
                TAG,
                "步骤：初始化数据库完成，已耗时:"
                        + (System.currentTimeMillis() - beginInitTime) + " ms");
        // 重新获取一次IMSI。某些机型在刚开机的情况下可能获取不到IMSI
        SLog.d(TAG, "获取IMSI号.." + MainApp.i().getIMSI());

        SLog.i(
                TAG,
                "步骤：获取IMSI号，已耗时:"
                        + (System.currentTimeMillis() - beginInitTime) + " ms");
        // 初始化access连接
        SLog.d(TAG, "初始化网络业务连接接口..");
        NetWorkMgr.getInstance().getNotifyNetModule()
                .setNetWorkListener(onNetWorkListener);
        NetWorkMgr.getInstance().getNotifyNetModule()
                .setContactOnlineStatusListener(onLineStatusListener);
        NetWorkMgr.getInstance().getNotifyNetModule().setBindChangeListener(bindChangeListener);
        SLog.i(
                TAG,
                "步骤：初始化网络业务连接接口，已耗时:"
                        + (System.currentTimeMillis() - beginInitTime) + " ms");
        // NotificationManager
        SLog.d(TAG, "初始化业务模块..");
        mNotifiMgr = (NotificationManager) MainApp.i().getSystemService(
                Context.NOTIFICATION_SERVICE);
        // Modules initialize
        commonModule = new CommonModule(this);
        messageModule = new MessageModule(this);
        contactsModule = new ContactsModule(this);
        friendModule = new FriendModule(this);
        settingsModule = new SettingsModule(this);
        nearUserModule = new NearUserModule(this);
        strangerModule = new StrangerModule(this);
        fastChatModule = new FastChatModule(this);
        SLog.i(
                TAG,
                "步骤：初始化业务模块，已耗时:"
                        + (System.currentTimeMillis() - beginInitTime) + " ms");
        // 在所有的回调设置完成后，启动access连接，这个接口务必只能调用一次
        SLog.d(TAG, "启动连接..");
        NetWorkMgr.getInstance().startConnectAccess();
        SLog.i(
                TAG,
                "步骤：启动连接服务端，已耗时:"
                        + (System.currentTimeMillis() - beginInitTime) + " ms");
        // 初始化联系人列表数据
        SLog.d(TAG, "初始化联系人列表数据..");
        ContactListCache.getInstance().recreateItems(
                contactsModule.getContactInfoForList());
        ContactListCache.getInstance().recreatePhotosForSMS(
                contactsModule.getContactInfoForPhoto());
        SLog.i(
                TAG,
                "步骤：初始化联系人列表数据等，已耗时:"
                        + (System.currentTimeMillis() - beginInitTime) + " ms");
        SLog.w(TAG,
                "已全部初始化完成，总耗时:"
                        + ((float) (System.currentTimeMillis() - beginInitTime))
                        / 1000 + " s");
    }

    /**
     * Description:线程池的创建
     */
    private static final int CORE_POOL_SIZE = 4;

    private void initThreadPool() {
        SLog.d(TAG, "初始化线程池..");
        // 用于普通网络请求和响应处理
        PriorityThreadFactory threadFactory = new PriorityThreadFactory(
                "shouxin-threadpool", Process.THREAD_PRIORITY_BACKGROUND);
        mThreadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE,
                threadFactory);

        // 用户同步处理
        PriorityThreadFactory threadFactory2 = new PriorityThreadFactory(
                "shouxin-syncpool", Process.THREAD_PRIORITY_LESS_FAVORABLE);
        mSyncPool = Executors.newSingleThreadExecutor(threadFactory2);
    }

    private final BindChangeListener bindChangeListener = new BindChangeListener() {

        @Override
        public void onNotify(final NetBindChangeNotify notify) {
            mThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    if (null != notify) {
                        /**
                         * 备注： 1、终端在收到绑定通知时，根据通知中的phone在通讯录中查找是否有该联系人，
                         * 如有则拿该联系人的contactId去服务端获取状态和简单用户信息；
                         * 2、终端在收到解绑通知时，根据通知中的phone在通讯录中查找是否有该联系人
                         * ，如有则清除该联系人与手信相关的属性；
                         **/
                        // 根据通知中的phone在通讯录中查找是否有该联系人
                        ArrayList<Contact> cl =
                                contactsModule.getCloudIdByPhone(notify.getPhone());
                        Contact ctmp = null;
                        if (null != cl) {
                            switch (notify.getNotifyType()) {
                                case 1:
                                    for (Contact c : cl) {
                                        // TODO 根据联系人ID查找有无Account
                                        ctmp = contactsModule.getContactById(c.getId());
                                        if (null != ctmp) {
                                            // 需要调用获取指定联系人ID的简单信息接口
                                            ArrayList<Contact> list = contactsModule
                                                    .getContactSimpleInfoByContactId(Long.valueOf(
                                                            c.getCloudId())
                                                            .intValue());
                                            contactsModule.updateContact2(list);
                                        }
                                    }
                                    // 刷新联系人列表
                                    notifyObservers(CoreServiceMSG.MSG_CONTACTS_SYNC_STATUS_END,
                                            null);
                                    break;
                                case 2:
                                    // 解绑通知|如有则清除该联系人与手信相关的属性
                                    contactsModule.clearContact(cl, notify.getPhone());
                                    // 刷新联系人列表
                                    notifyObservers(CoreServiceMSG.MSG_CONTACTS_SYNC_STATUS_END,
                                            null);
                                    break;
                            }
                        }
                        // 获取联系人简单状态
                        for (Contact c : cl) {
                            contactsModule.getContactStatus(Long.valueOf(c.getCloudId()).intValue());
                        }
                    } else {
                        SLog.d(TAG, "\tzhang:绑定/解绑更改状态通知为空");
                    }
                }
            });
        }
    };
    /**
     * 网络层状态通知会到此方法 Description: 服务器响应
     */
    private final NetWorkListener onNetWorkListener = new NetWorkListener() {

        @Override
        public void onNotify(int what, Object obj) {
            if (NetWorkListener.ON_LINE == what
                    || NetWorkListener.RE_ON_LINE == what) {
                isConnected = true;
            } else {
                isConnected = false;
            }
            // 通知MessageListActivity中，展现网络状态提示
            notifyObservers(CoreServiceMSG.MSG_NET_STATUE_CHANGE, null);
            SLog.d(TAG, "onNotify.what:" + what);
            SLog.d(TAG, "onNotify.isConnect:" + isConnected);
            switch (what) {
                case NetWorkListener.ON_LINE: {
                    SLog.d(TAG, "onNotify:网络层连接成功并且用户未登录");
                    MainApp.setLoggedIn(false); // wing.hu@20120920
                    setCurrentClass(MainActivity.class);
                    autoLogin();
                }
                    break;
                case NetWorkListener.RE_ON_LINE: {
                    if (!CommonPreferences.getLogoutedStatus()) {
                        SLog.d(TAG, "onNotify:网络层重新连接成功并且用户已经登录过");
                        MainApp.setLoggedIn(true); // wing.hu@20120920
                        postOfflineMsgRequest();
                        postGetOnlineStatus();
                    } else {
                        SLog.d(TAG, "onNotify:网络层重新连接成功并且用户未登录过");
                        MainApp.setLoggedIn(false); // wing.hu@20120920
                        setCurrentClass(MainActivity.class);
                        autoLogin();
                    }
                }
                    break;
                case NetWorkListener.OFF_LINE: {
                    SLog.d(TAG, "onNotify:网络层连接失败，在线状态置为下线");
                    MainApp.setLoggedIn(false); // wing.hu@20120920
                    MainApp.i().clearAllOnlineStatus();
                    notifyObservers(CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS, null);
                }
                default:
                    break;
            }

        }
    };

    /**
     * 自动登录
     */
    public void autoLogin() {
        MainApp.setLoggedIn(false); // wing.hu@20120920
        UserInfo info = CommonPreferences.getUserInfo();
        if (CommonPreferences.getLoginedStatus()
                && !CommonPreferences.getLogoutedStatus()) {
            if (info != null && commonModule != null) {
                SLog.d(TAG, "\t>>>>>> autoLogin:执行自动登录");
                String lastUsername = CommonPreferences.getLastLoginName();
                if (TextUtils.isEmpty(lastUsername)) {
                    lastUsername = info.name;
                }
                byte[] pwd = info.encryptPasswd;
                commonModule.login(lastUsername, pwd);
            }
        }
    }

    /**
     * 登录成功后，5s后获取好友在线状态信息
     */
    private void postGetOnlineStatus() {
        SLog.d(TAG, "获取好友在线状态..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (MainApp.isLoggedIn()) {
                    contactsModule.getContactStauts();
                }
            }
        };
        mHandler.postDelayed(r, TRIGGER_TIME_GET_ONLINE_USER_STATUS);
    }

    /**
     * 注册成功后，30分钟后检查是否绑定成功
     */
    private void postGetbindStatus() {
        SLog.d(TAG, "检查绑定状态..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String status = SettingsPreferences.getBindStatus();
                if (!SettingsPreferences.BIND_LOCAL.equals(status)) {
                    notifyObservers(CoreServiceMSG.MSG_CHECK_BIND, null);
                }
            }
        };
        mHandler.postDelayed(r, TRIGGER_TIME_CHECK_BIND_STATUS);
    }

    /**
     * 登录成功后，30s后发送接收离线消息请求
     */
    private void postOfflineMsgRequest() {
        SLog.d(TAG, "发送接收离线消息请求..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // 发送接收离线消息请求
                if (MainApp.isLoggedIn()) {
                    commonModule.activeOfflineMsgPush();
                }
            }
        };
        mHandler.postDelayed(r, TRIGGER_TIME_GET_OFFLINE_MESSAGE);
    }

    /**
     * 登录成功后，20s后获取用户信息&是否推荐的设置
     */
    private void postSettingsPropertyForUserInfo() {
        SLog.d(TAG, "获取用户信息..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (MainApp.isLoggedIn()) {
                    // 获取用户信息
                    settingsModule.getUserInfo();
                }
            }
        };
        mHandler.postDelayed(r, TRIGGER_TIME_GET_USER_INFO);
    }

    /**
     * 登录成功后，20s后获取用户信息&是否推荐的设置
     */
    private void postSettingsPropertyForIsRecommend() {
        SLog.d(TAG, "获取是否推荐的设置..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (MainApp.isLoggedIn()) {
                    // 获取是否推荐
                    settingsModule.getRecommend();
                }
            }
        };
        mHandler.postDelayed(r, TRIGGER_TIME_GET_IS_RECOMMENDED);
    }

    /**
     * 登录成功后，120s后触发服务端重新计算推荐好友
     */
    private void postFireReCalsRecommendsFriends() {
        SLog.d(TAG, "发送重新计算推荐好友的请求..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (MainApp.isLoggedIn()) {
                    friendModule.fireReCalsRecommendsFriends();
                }
            }
        };

        mHandler.postDelayed(r, TRIGGER_TIME_RECALS_RECOMMEND_FRIENDS);
    }

    /**
     * 登录成功后，5分钟后触发推荐短语获取请求和响应
     */
    private void postGetRecommendedMsg() {
        SLog.d(TAG, "获取推荐短语..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (MainApp.isLoggedIn()) {
                    messageModule.syncRecommendedMsg();
                }
            }
        };

        mHandler.postDelayed(r, TRIGGER_TIME_GET_SYSTEM_EXCITING_SMS);
    }

    /**
     * 获取邀请文案
     */
    private void postGetInviteConfiguration() {
        SLog.d(TAG, "获取邀请方案..");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (MainApp.isLoggedIn()) {
                    commonModule.getInviteConfig();
                }
            }
        };
        mHandler.postDelayed(r, TRIGGER_TIME_GET_UNIFIED_COPYWRITER);
    }

    // 登录成功后处理的一些事务
    private synchronized void loginSuccess(final int what, final Object obj) {
        SLog.d(TAG, "登录成功后，业务处理中..");
        final long bTime = System.currentTimeMillis();
        UserInfo info = (UserInfo) obj;
        MainApp.i().setLoginFromActivity();
        UserInfo userInfo = CommonPreferences.getUserInfo();
        if (null == userInfo || userInfo.skyid != info.skyid
                || what == CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME) {
            SLog.d(TAG, "不同的用户使用，清理数据..");
            messageModule.deleteUserData();
            SettingsPreferences.clear();
            ContactListCache.getInstance().clearListItems();
            SettingsPreferences.saveSKYID(info.skyid);
            CommonPreferences.saveContactsLastTimeUpdate(0);
            CommonPreferences.saveSyncThreadsCount(0);
            CommonPreferences.saveSyncContactsCount(0);
        }
        // 设置在线状态
        MainApp.i().setOnline(true);
        // 获取设置界面网络端存储的属性值
        postSettingsPropertyForUserInfo();
        postSettingsPropertyForIsRecommend();
        // 获取邀请文案
        postGetInviteConfiguration();
        // 发送获取离线消息请求
        postOfflineMsgRequest();
        // 触发服务端重新计算推荐好友
        postFireReCalsRecommendsFriends();
        // 触发获取推荐短语请求
        postGetRecommendedMsg();

        // 保存用户信息到数据库
        User user = DaoFactory.getInstance(MainApp.i()).getUsersDAO()
                .getUserBySkyID(info.skyid);
        if (user == null) {
            user = new User();
            user.setSkyid(info.skyid);
        }
        SLog.d(TAG, "保存登录状态..");
        CommonPreferences.saveLoginStatus(info);
        // 如果是绑定其他的手机（对方未确认换绑），不做任何同步操作
        // 同步都放到主界面显示之后进行
        // if (!MainApp.i().getLoginFromActivity()) {
        // messageModule.syncSMSThreads(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                contactsModule.syncContacts();

            }
        }, 1500);
        // }
        // 登录成功，就行预取会话列表操作
        MessageListCache.getInstance().UpdateList(messageModule.getThreadsList());

        SLog.d("Time-consuming",
                "loginSuccess 耗时:" + TimeUtils.getTimeconsuming(bTime, System.currentTimeMillis())
                        + " sec");
    }

    /**
     * 发送回调消息
     * 
     * @param what : 消息类型
     * @param wparam ：消息参数１
     * @param lparam 　：消息参数2
     * @param obj ：消息参数3
     */
    public synchronized void notifyObservers(final int what, final Object obj) {
        // Loger.getInstance().logd(TAG,"收到回调通知.."+what);

        if (this.clazz != null) {
            Message message = new Message();
            message.what = what;
            message.obj = obj;
            if (message.what != CoreServiceMSG.MSG_CHATMSG_SMSMSG_RECEIVE
                    && message.what != CoreServiceMSG.MSG_CHATMSG_TEXTMSG_RECEIVE
                    && message.what != CoreServiceMSG.MSG_CHATMSG_CARDMSG_RECEIVE
                    && message.what != CoreServiceMSG.MSG_CHATMSG_VOICEMSG_RECEIVE
                    && message.what != CoreServiceMSG.MSG_CHATMSG_MARKETMSG_RECEIVE
                    && message.what != CoreServiceMSG.MSG_CHATMSG_SYSTEMMSG_RECEIVE
                    && message.what != CoreServiceMSG.MSG_CHATMSG_FRIENDSMSG_RECEIVE)
                MainApp.i().putCallbackStatus(getCurrentClass(),
                        message);
        }
        switch (what) {
            case CoreServiceMSG.MSG_LOGIN_SUCCESS:
                SLog.i(TAG, "LOGIN_SUCCESS!");
                MainApp.setLoggedIn(true); // wing.hu@20120920
                loginSuccess(what, obj);
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME:
                SLog.w(TAG, "LOGIN_USERNAME_BIND_IMSI_NOT_SAME!");
                loginSuccess(what, obj);
                break;
            case CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_UNBIND:
                SLog.w(TAG, "LOGIN_USERNAME_BIND_IMSI_UNBIND!");
                break;
            case CoreServiceMSG.MSG_REGISTER_SUCESS:
                SLog.i(TAG, "注册成功...");
                CommonPreferences.setRegisterFlag(true);
                SettingsPreferences.saveBindInfo(SettingsPreferences.UNBIND,
                        "REGISTER-SUCCESS");
                messageModule.deleteUserData();
                ContactListCache.getInstance().clearListItems();
                // N分钟后检查绑定是否成功
                postGetbindStatus();
                break;
            case CoreServiceMSG.MSG_BIND_SUCESS:
            case CoreServiceMSG.MSG_REBIND_SUCESS:
                SLog.d(TAG, "绑定换绑通知...");
                MainApp.i().setOnline(true);
                CommonPreferences.saveChangeBindSendSMSTime(0L);
                /*
                 * 如果刚注册成功，不再次做绑定！！！ *
                 */
                if (SettingsPreferences.UNBIND.equals(SettingsPreferences.getBindStatus())
                        && "REGISTER-SUCCESS".equals(SettingsPreferences.getBindMessage())) {
                    SLog.w(TAG, "注册时刚做完同步联系人的操作，不再次做同步!");
                    SettingsPreferences.saveBindInfo(SettingsPreferences.BIND_LOCAL, "SUCCESS");
                } else {
                    /*-----绑定成功，重置绑定时间为0.@see AuthDA.isCanSendBind------**/
                    SettingsPreferences.saveBindInfo(SettingsPreferences.BIND_LOCAL,
                            "SUCCESS");

                    // 同步会话列表
                    messageModule.syncSMSThreads(true);
                    contactsModule.syncContacts();
                }

                // 获取用户信息
                settingsModule.getUserInfo(what);
                break;
            case CoreServiceMSG.MSG_CONTACTS_SYNC_END:
            case CoreServiceMSG.MSG_CONTACTS_SYNC_STATUS_END:
            case CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END:
            case CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS:
            case CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_END:
                ContactListCache.getInstance().recreateItems(
                        getContactsModule().getContactInfoForList());
                break;
        }
        for (Observer observer : mCallbackList) {
            observer.notifyObserver(what, obj);
        }
    }

    /**
     * @return
     */
    private Class<? extends Activity> getCurrentClass() {
        return this.clazz;
    }

    public void setCurrentClass(Class<? extends Activity> clazz) {
        this.clazz = clazz;
    }

    public ExecutorService getThreadPool() {
        return mThreadPool;
    }

    public ExecutorService getSyncPool() {
        return mSyncPool;
    }

    public CommonModule getCommonModule() {
        return commonModule;
    }

    public MessageModule getMessageModule() {
        return messageModule;
    }

    public ContactsModule getContactsModule() {
        return contactsModule;
    }

    /**
     * @return the friendModule
     */
    public FriendModule getFriendModule() {
        return friendModule;
    }

    public SettingsModule getSettingsModule() {
        return settingsModule;
    }

    public NearUserModule getNearUserModule() {
        return nearUserModule;
    }

    public StrangerModule getStrangerModule() {
        return strangerModule;
    }

    public FastChatModule getFastChatModule() {
        return fastChatModule;
    }

    public NotificationManager getNotificationManager() {
        return mNotifiMgr;
    }

    /**
     * 显示顶部通知栏通知
     * 
     * @param content
     * @param 是否原样显示消息
     */
    public void showNotification(android.skymobi.messenger.bean.Message message) {

        String title = MainApp.i().getResources().getString(R.string.app_name);
        String notifiTail = MainApp.i().getResources().getString(R.string.notifi_content);
        String notifiContent = message.getContent();
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        int unreadThreadsCnt = messageModule.getUnreadThreadsCount();
        int unreadMessageCnt = messageModule.getTotalUnreadMessageCount();
        Log.i(TAG, "unreadThreadsCnt = " + unreadThreadsCnt
                + ", unreadMessageCnt = " + unreadMessageCnt);
        // 没有未读消息或者没有未读会话
        if (unreadMessageCnt <= 0 || unreadThreadsCnt <= 0)
            return;

        List<Threads> unreadThreads = messageModule.getUnreadThreads();

        if (unreadThreadsCnt > 1) { // 多个未读会话
            intent.setClass(MainApp.i(), MainActivity.class);
            notifiContent = String.valueOf(unreadMessageCnt) + notifiTail;
        } else if (unreadThreads.size() > 0 && unreadMessageCnt > 1) { // 单个会话多条消息
            Threads threads = unreadThreads.get(0);
            intent.putExtra(ChatActivity.ADDRESSIDS, threads.getAddressIds());
            intent.setClass(MainApp.i(), MainActivity.class);
            notifiContent = String.valueOf(unreadMessageCnt) + notifiTail;
            title = messageModule.getDisplayName(threads,
                    threads.getAddressList());
        } else if (unreadThreads.size() > 0) { // 单条消息
            Threads threads = unreadThreads.get(0);
            intent.setClass(MainApp.i(), MainActivity.class);
            intent.putExtra(ChatActivity.ADDRESSIDS, threads.getAddressIds());
            notifiContent = message.getContent();
            title = messageModule.getDisplayName(threads,
                    threads.getAddressList());
        }

        // 如果在注销状态，直接让用户跳转到登陆界面，不再跳转到会话界面或者聊天界面
        if (CommonPreferences.getLogoutedStatus()) {
            intent.setClass(MainApp.i(), LoginActivity.class);
        }

        String tickerText = title + ":" + notifiContent;
        Notification notification = new Notification(
                R.drawable.new_message_notification, tickerText,
                System.currentTimeMillis());

        notification.defaults = Notification.DEFAULT_LIGHTS;

        if (SettingsPreferences.getSoundStatus()) {
            notification.defaults = notification.defaults
                    | Notification.DEFAULT_SOUND;
        }

        if (SettingsPreferences.getVibrateStatus()) {
            long[] vibrate = {
                    0, 200, 100, 200, 100, 200
            };
            notification.vibrate = vibrate;
        }
        notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;

        PendingIntent pt = PendingIntent.getActivity(MainApp.i(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(MainApp.i(), title, notifiContent, pt);
        MainApp.i().addNotifyMessages(message);
        mNotifiMgr.notify(NEW_MESSAGS_NOTIFICATION_ID, notification);
        Log.i(TAG, "notify ok mNotifiMgr = " + mNotifiMgr + ", notification = "
                + notification);
    }

    /**
     * 取消顶部栏通知
     */
    public void cancelNotification() {
        if (mNotifiMgr != null)
            mNotifiMgr.cancel(NEW_MESSAGS_NOTIFICATION_ID);
        MainApp.i().clearNotifyMessages();
    }

    /**
     * 判断当前网络是否还在连接状态
     * 
     * @return
     */
    public boolean isNetConnected() {
        return isConnected;
    }

    /**
     * 获取当前网络类型
     * 
     * @return
     */
    public int getNetType() {
        return netType;
    }

    public static CoreService getInstance() {
        return sInstance;
    }

    /**
     * 在线状态监听器
     */
    private final NetWorkListener onLineStatusListener = new NetWorkListener() {
        @Override
        public void onNotify(int what, Object obj) {
            NetOnlineStateChangeNotify status = (NetOnlineStateChangeNotify) obj;
            // 包含此Skyid 的联系人
            MainApp.i().putUserOnlineStatus(status.getSkyid(),
                    status.getStatus() == 0 ? false : true);
            notifyObservers(CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS, obj);
        }
    };

    private SMSObserver smsObserver;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (CoreServiceMSG.MSG_SMSMSG_RECEIVE_COMMON_NET == msg.what
                    || CoreServiceMSG.MSG_SMSMSG_RECEIVE_COMMON == msg.what) {
                String bindSuccess = MainApp.i().getString(R.string.sms_bind_sucess_pre);
                String rebindSuccess = MainApp.i().getString(R.string.sms_rebind_sucess_pre);
                // 新增换绑判断字符
                String rebindSuccess1 = MainApp.i().getString(R.string.sms_rebind_sucess_pre_1);
                String registerSuccess = MainApp.i().getString(R.string.sms_register_sucess_pre);
                // 新增注册判断字符
                String registerSuccess1 = MainApp.i().getString(R.string.sms_register_sucess_pre_1);
                @SuppressWarnings("unchecked")
                List<String> smss = (List<String>) msg.obj;
                for (String sms : smss) {
                    if (sms.contains(bindSuccess)
                            && sms.startsWith(bindSuccess)) {
                        notifyObservers(CoreServiceMSG.MSG_BIND_SUCESS, sms);
                        unregisterObserverForCommonSMS();
                        break;
                    } else if (sms.contains(registerSuccess)
                            && sms.startsWith(registerSuccess)
                            && sms.contains(registerSuccess1)) {
                        // 2.6变更
                        if (!APPCache.getInstance().isManualLogined()) {
                            notifyObservers(CoreServiceMSG.MSG_BIND_SUCESS, sms);
                        }
                        unregisterObserverForCommonSMS();
                        break;
                    } else if (sms.contains(rebindSuccess)
                            && sms.startsWith(rebindSuccess)
                            && sms.contains(rebindSuccess1)) {
                        // 重新获取用户信息
                        settingsModule.getUserInfo();
                        notifyObservers(CoreServiceMSG.MSG_REBIND_SUCESS, sms);
                        unregisterObserverForCommonSMS();
                        break;
                    }
                }
            }
        }
    };

    public void registerObserverForCommonSMS() {
        if (smsObserver == null) {
            smsObserver = new SMSObserver(mHandler, this);
        }
        MainApp.i().getContentResolver().registerContentObserver(
                Uri.parse("content://sms"), true, smsObserver);
    }

    public void unregisterObserverForCommonSMS() {
        if (null != smsObserver) {
            MainApp.i().getContentResolver().unregisterContentObserver(smsObserver);
        }
    }

    private ThreadsObserver threadsObserver;

    private final Handler mCheckSMSHandler = new Handler();

    public void registerObserverForSMSThreads() {
        if (threadsObserver == null) {
            threadsObserver = new ThreadsObserver(mCheckSMSHandler, this);
        }
        MainApp.i().getContentResolver().registerContentObserver(
                Uri.parse("content://mms-sms/complete-conversations"), true,
                threadsObserver);
    }

    public void unregisterObserverForSMSThreads() {
        if (null != threadsObserver) {
            MainApp.i().getContentResolver().unregisterContentObserver(threadsObserver);
        }
    }

    public ThreadsObserver getThreadsObserver() {
        return threadsObserver;
    }

    /**
     * @return the handler
     */
    public Handler getHandler() {
        return mHandler;
    }

    // 最大SMSid
    private long maxSMSID = 0;

    public ArrayList<String> inComeSMS() {
        ArrayList<String> messages = new ArrayList<String>();
        long currentMaxSMSID = DaoFactory.getInstance(MainApp.i())
                .getMessagesDAO().getMaxLocalSmsId();
        if (maxSMSID < currentMaxSMSID) {
            Cursor cursorLocalMessage = MainApp.i().getContentResolver().query(
                    Uri.parse("content://sms/"), new String[] {
                        // "_id", "address", "body"`
                    "body"
                    }, "_id>?", new String[] {
                        maxSMSID + ""
                    }, null);

            if (null == cursorLocalMessage) {
                Log.i(TAG, "无法获取本地短信");
                return messages;
            }
            while (cursorLocalMessage.moveToNext()) {
                messages.add(cursorLocalMessage.getString(0));
            }
            maxSMSID = currentMaxSMSID;
            cursorLocalMessage.getCount();
        }
        return messages;
    }

    public void setSMSMaxID() {
        maxSMSID = DaoFactory.getInstance(MainApp.i())
                .getMessagesDAO().getMaxLocalSmsId();
    }

}
