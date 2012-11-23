
package android.skymobi.messenger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.bean.DeviceInfo;
import android.skymobi.messenger.bean.NetLocation;
import android.skymobi.messenger.bizunit.lcs.LcsBU;
import android.skymobi.messenger.cache.FastChatCache;
import android.skymobi.messenger.crashreport.CrashHandler;
import android.skymobi.messenger.location.LocationListenner;
import android.skymobi.messenger.logreport.SkymobiclickAgent;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.LifeService;
import android.skymobi.messenger.ui.ChatActivity;
import android.skymobi.messenger.ui.FriendListActivity;
import android.skymobi.messenger.ui.MainActivity;
import android.skymobi.messenger.ui.MessageListActivity;
import android.skymobi.messenger.ui.NearUserActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.utils.PropertiesUtils;
import android.skymobi.messenger.utils.SingleTaskHandler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;
import com.skymobi.android.sx.codec.beans.common.FriendsList;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/** 该类提供全局方法和变量功能，由系统启动时自动构造 */
public class MainApp extends Application {

    private static final String TAG = MainApp.class.getSimpleName();
    private static MainApp instance = null;
    private static final String CHANNEL_NAME = "SKYMOBI_CHANNEL";

    private DeviceInfo mDeviceInfo;
    private PackageInfo pi;
    private int channelId;
    private String channelStr;
    private boolean online;
    // LCS业务类
    private LcsBU lcsBU;
    private NetUserInfo userInfo = null; // 登录用户的个人信息
    private final Map<Class<? extends Activity>, Message> callBackStatus = new HashMap<Class<? extends Activity>, Message>();

    // 所有手信用户在线状态（skyid,boolean）键值对的形式，true表示在线，false表示不在线
    private final static HashMap<Integer, Boolean> onlineStatus = new HashMap<Integer, Boolean>();
    private final Map<String, ArrayList<FriendsList>> receiveFriends = new HashMap<String, ArrayList<FriendsList>>();

    // 保存所有创建的activity列表，退出或者注销的时候先把之前的activity全部清除掉
    private final static Stack<Activity> activityStack = new Stack<Activity>();
    private boolean downloading = false;

    // 保存会话对应的talkReason
    private final static HashMap<Long, String> talkReasons = new HashMap<Long, String>();
    // 保存聊天界面的输入模式
    private int mInputType = ChatActivity.INPUT_TEXT; // 0 :文字 2：语音

    private final List<android.skymobi.messenger.bean.Message> notifyMessages = new ArrayList<android.skymobi.messenger.bean.Message>();// 记录通知消息
    // 保存打招呼的人的记录
    private final static HashMap<Integer, Boolean> greetStatus = new HashMap<Integer, Boolean>();

    private final static HashMap<String, NetLocation> locationBuf = new HashMap<String, NetLocation>();
    private final static byte locationBufCount = 10;

    // 定义全局的快聊的内存聊天记录
    private static FastChatCache fastChatCache = new FastChatCache();

    // 全局邀请入口值
    private int inviteEntrance = -1;

    public static MainApp i() {
        // 判断application是否实例化完成, by zzy
        if (instance == null) {
            Assert.assertTrue("获取到的application 为空", instance != null);
        }
        return instance;
    }

    // 保存同步联系人状态
    private volatile boolean isSyncContacts = false;

    // 保存同步会话状态
    private volatile boolean isSyncThreads = false;
    private volatile long lastSyncThreadsTime = 0;

    // 保存CoreService的实例，只要应用程序在，对象就有效
    private static CoreService bizService = null;

    // 提供一个全局的异步执行队列，新建一个线程按顺序完成提交的任务
    private static SingleTaskHandler taskHandler = new SingleTaskHandler("global-handler");

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        boolean isSaveToFile = PropertiesUtils.getInstance().isSaveFileLog();
        boolean isSaveToDDMS = PropertiesUtils.getInstance().isSaveDDMSLog();
        int level = PropertiesUtils.getInstance().getLogLevel();
        SLog.init(isSaveToDDMS, isSaveToFile, level, true);
        // SLog.init(true, false, SLog.LEVEL_DEBUG, false);

        initStrictMode();
        // startService();

        initDeviceInfo();
        createSdcardDir();
        accessThirdPartyLog();
        initCrashHandle();
        initChannel();
        if (null == lcsBU) {
            lcsBU = new LcsBU();
        }
        lcsBU.initAlarmRecevier();
        // testConfig();

        // init coreservice, by zzy
        if (bizService == null) {
            bizService = new CoreService();
        }

        isFastChatSended = CommonPreferences.isFastChatSended();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
        SLog.w(TAG, "onLowMemory!");
    }

    // 读取配置文件
    /*
     * private void testConfig() { String accessIP =
     * PropertiesUtils.getInstance().getAccessIP(); short accessPort =
     * PropertiesUtils.getInstance().getAccessPort(); String fileUrl =
     * PropertiesUtils.getInstance().getFileURL(); String supUrl =
     * PropertiesUtils.getInstance().getSupURL(); boolean isSaveNetLog =
     * PropertiesUtils.getInstance().isSaveNetLog(); boolean isSaveAppLog =
     * PropertiesUtils.getInstance().isSaveAPPLog(); Log.d(TAG, "accessIP = " +
     * accessIP); Log.d(TAG, "accessPort = " + accessPort); Log.d(TAG,
     * "fileUrl = " + fileUrl); Log.d(TAG, "supUrl = " + supUrl); Log.d(TAG,
     * "isSaveNetLog = " + isSaveNetLog); Log.d(TAG, "isSaveAppLog = " +
     * isSaveAppLog); }
     */

    // 启动strictmode模式
    private void initStrictMode() {
        // StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        // .detectAll().penaltyLog()
        // .build());
        // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
        // .penaltyLog().build());
    }

    /**
     * 
     */
    private void initChannel() {
        try {
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            channelId = appInfo.metaData.getInt(CHANNEL_NAME);
            channelStr = getChannelString();
            SLog.d(TAG, "channelId: " + channelId + " ,channelStr = " + channelStr);
        } catch (Exception e) {
            SLog.e(TAG, "can't find channelId or channelStr");
            e.printStackTrace();
        }
    }

    private String getChannelString() throws Exception {
        InputStream is = null;
        is = this.getAssets().open("skymobi_a");
        if (is.available() != 0) {
            byte[] asset = new byte[is.available()];
            is.read(asset);

            String skymobi_a_str = new String(asset);
            if (!skymobi_a_str.contains("_")) {
                return "0_skymobi1_";
            }
            String skymobi_a_type = skymobi_a_str.subSequence(0, skymobi_a_str.indexOf("_"))
                    .toString();
            // 自有渠道:0_5000 厂商预装:1_XXX 第三方软件推广:3_XXX 第三方平台推广: 4_XXX
            if (skymobi_a_type.equals("1")) {
                String skymobi_a = getSkyMobiA();
                if (null == skymobi_a) {
                    return "1_";
                }
                return "1_" + skymobi_a;
            } else {
                return skymobi_a_str;
            }
        }
        return "0_skymobi1_";
    }

    private static String getSkyMobiA() {
        try {
            Field field = Build.class.getField("SKYMOBI_A");
            String skyMobiA = (String) field.get(null);
            return skyMobiA;
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * 将没有处理的异常进行收集，并且保持到日志文件中
     */
    private void initCrashHandle() {
        CrashHandler cHandler = CrashHandler.getInstance();
        cHandler.init(getApplicationContext());
    }

    /**
     * 保留该函数 是否接入第三方日志报表，读取mainifest.xml中meta data标识位 wing.hu edit at 2012-5-2
     **/
    private void accessThirdPartyLog() {

        try {
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            final boolean isDebug = appInfo.metaData.getBoolean("isDebug");
            SkymobiclickAgent.setDebugMode(isDebug);
            SLog.i(TAG, "is debug Mode?->[" + isDebug + "]");
        } catch (NameNotFoundException e) {
            SLog.w(TAG, "Failed to read the meta data values.");
        }

    }

    public void startLifeService() {
        // 启动后台服务
        Intent intentService = new Intent(this, LifeService.class);
        intentService.putExtra("caller", "MainApp");
        startService(intentService);
    }

    public void stopService() {
        // 先停止服务，防止服务被重启
        Intent intentService = new Intent(this, LifeService.class);
        stopService(intentService);

        // 退出应用程序
        bizService.quit();

        // 反注册提醒
        lcsBU.uninitAlarmRecevier();
    }

    public static void freeMemory() {
        // //尽可能释放内存

        // 关闭所有activity
        // instance.closeAllActivity();

        // 清空缓存
        HeaderCache.getInstance().clearAll();

    }

    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    public void setDeviceInfo(DeviceInfo info) {
        mDeviceInfo = info;
    }

    /**
     * @return the pi
     */
    public PackageInfo getPi() {
        return pi;
    }

    /*
     * 获取IMSI,并写入mDeviceInfo中
     */
    public String getIMSI() {
        if (mDeviceInfo == null) {
            SLog.w(TAG, "获取IMSI号失败,deviceInfo为null!");
            return "";
        }
        if (TextUtils.isEmpty(mDeviceInfo.imsi)) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mDeviceInfo.imsi = tm.getSubscriberId();
        }
        SLog.d(TAG, "获取到的IMSI号:[" + mDeviceInfo.imsi + "]");
        return mDeviceInfo.imsi;
    }

    private void initDeviceInfo() {
        SLog.d(TAG, "初始化设备信息..");
        mDeviceInfo = new DeviceInfo();
        mDeviceInfo.product = android.os.Build.MANUFACTURER;
        mDeviceInfo.modle = android.os.Build.MODEL;
        mDeviceInfo.sdk = (short) android.os.Build.VERSION.SDK_INT;
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mDeviceInfo.imsi = tm.getSubscriberId();// "810260000000007";

        if (mDeviceInfo.imsi == null || mDeviceInfo.imsi.equals("")) {
            mDeviceInfo.imsi = "";
        }
        SLog.d(TAG, "初始化设备信息..IMSI:[" + mDeviceInfo.imsi + "]");
        mDeviceInfo.imei = tm.getDeviceId();
        if (mDeviceInfo.imei == null || mDeviceInfo.imei.equals("")) {
            mDeviceInfo.imei = "";
        }
        SLog.d(TAG, "初始化设备信息..IMEI:[" + mDeviceInfo.imei + "]");
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wmgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wmgr.getDefaultDisplay().getMetrics(dm);
        mDeviceInfo.screenWidth = dm.widthPixels;
        mDeviceInfo.screenHeight = dm.heightPixels;

        if (mDeviceInfo.screenHeight < mDeviceInfo.screenWidth) {
            int temp = mDeviceInfo.screenWidth;
            mDeviceInfo.screenWidth = mDeviceInfo.screenHeight;
            mDeviceInfo.screenHeight = temp;
        }
        Log.e(TAG, "初始化设备信息..屏幕宽:[" + mDeviceInfo.screenWidth + "],屏幕高:["
                + mDeviceInfo.screenHeight + "]");

        // 总内存
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("/proc/meminfo")));
            String firstLine = br.readLine();
            mDeviceInfo.totalMem = Integer.valueOf(firstLine.split("\\s+")[1]) * 1024;
        } catch (Exception e) {
            mDeviceInfo.totalMem = 512 * 1024 * 1024;
            SLog.w(TAG, "初始化设备信息..总内存大小失败:" + e);
        }
        SLog.d(TAG, "初始化设备信息..总内存大小:[" + mDeviceInfo.totalMem + "]");
        try {
            PackageManager pm = getPackageManager();
            pi = pm.getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            SLog.w(TAG, "初始化设备信息..获取package失败:" + e);
        }
        SLog.i(TAG, "初始化设备信息完成!");

    }

    public void putCallbackStatus(Class<? extends Activity> key, Message value) {
        callBackStatus.put(key, value);
    }

    public void removeCallbackStatus(Class<? extends Activity> key) {
        callBackStatus.remove(key);
    }

    public Message getCallbackStatus(Class<? extends Activity> key) {
        Message message = callBackStatus.get(key);
        if (message != null)
            removeCallbackStatus(key);
        return message;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public NetUserInfo getNetLUserInfo() {
        return userInfo;
    }

    public void setUserInfo(NetUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * 创建文件夹
     */
    private void createSdcardDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 创建主目录
            File path = new File(Constants.SHOU_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            // 创建子目录(头像,语音,图片，apk等)
            path = new File(Constants.SOUND_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            path = new File(Constants.HEAD_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            path = new File(Constants.LARGE_HEAD_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            path = new File(Constants.PIC_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            path = new File(Constants.APK_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }

            File nomedia = new File(Constants.SHOU_PATH, Constants.NO_MEDIA);
            if (!nomedia.exists()) {
                try {
                    nomedia.createNewFile();
                } catch (IOException e) {
                    SLog.e(TAG, "create nomedia error!!!");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建文件
     * 
     * @param curtime
     * @return
     */
    public String createNewSoundFile(String curtime) {
        createSdcardDir();
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            return null;
        File f = new File(Constants.SOUND_PATH, String.valueOf(curtime) + ".amr");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "createNewFile error!!!");
                e.printStackTrace();
            }
        }
        return f.getAbsolutePath();
    }

    /**
     * 根据skyID 获取对应用户的在线状态
     * 
     * @param skyID
     * @return
     */
    public boolean getUserOnlineStatus(int skyID) {
        return (onlineStatus.get(skyID) == null) ? false : onlineStatus.get(skyID);
    }

    /**
     * 保存skyID对应用户的在线状态
     * 
     * @param skyID
     * @param status
     */
    public void putUserOnlineStatus(int skyID, boolean status) {
        // SLog.d(TAG, "pub userStatus:" + skyID + "|" + status);
        onlineStatus.put(skyID, status);
    }

    /**
     * 断网下，清除所有用户的在线状态
     */
    public void clearAllOnlineStatus() {
        onlineStatus.clear();
    }

    /**
     * @return the receiveFriends
     */
    public Map<String, ArrayList<FriendsList>> getReceiveFriends() {
        return receiveFriends;
    }

    public void putReceiveFriends(String key, ArrayList<FriendsList> frdList) {
        receiveFriends.put(key, frdList);
    }

    /**
     * 保存同步联系人状态
     */
    public void setStatusSyncContacts(boolean isSyncContacts) {
        this.isSyncContacts = isSyncContacts;
    }

    /**
     * 获取同步联系人状态
     */
    public boolean isSyncContacts() {
        return isSyncContacts;
    }

    /**
     * 保存同步会话状态
     */
    public void setStatusSyncThreads(boolean isSyncThreads) {
        this.isSyncThreads = isSyncThreads;

    }

    /**
     * 获取同步会话状态
     */
    public boolean getStatusSyncThreads() {
        return isSyncThreads;
    }

    /**
     * 设置最后一次同步会话的时间
     */
    public void setLastSyncThreadsTime(long lastSyncThreadsTime) {
        this.lastSyncThreadsTime = lastSyncThreadsTime;
    }

    /**
     * 获取上次同步会话的时间
     * 
     * @return
     */
    public long getLastSyncThreadsTime() {
        return lastSyncThreadsTime;
    }

    private long lastSyncTime = 0;

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
        CommonPreferences.setLastSyncTime(lastSyncTime);
    }

    // 创建一个activity就加到列表中
    public void addActivity(Activity activity) {
        activityStack.push(activity);
    }

    // 清除APP所有activity
    public void closeAllActivity() {
        for (int i = 0; i < activityStack.size(); i++) {
            Activity activity = activityStack.pop();
            if (activity != null && !activity.isFinishing())
                activity.finish();
        }
    }

    /**
     * Activity出栈
     */
    public void popActivity(Activity activity) {
        activityStack.remove(activity);
    }

    /**
     * 获取当前Activity
     * 
     * @return
     */
    public Activity getCurrentActivity() {
        return activityStack.pop();
    }

    /**
     * 获取所有Activity
     * 
     * @return
     */
    public ArrayList<Activity> getAllActiveActivity() {
        ArrayList<Activity> activitys = new ArrayList<Activity>();
        for (int i = 0; i < activityStack.size(); i++) {
            activitys.add(activityStack.get(i));
        }
        return activitys;
    }

    /**
     * 依据既定的规则，确定在聊天界面应该返回到以下哪个Activity =>附近的人列表 =>可能认识的人列表 =>会话列表
     * 
     * @return
     */
    public Class<?> getChatBackActivity() {
        // 在activityStack中寻找历史activity
        // 从后遍历
        for (int i = activityStack.size() - 1; i > 0; i--) {
            Activity act = activityStack.get(i);
            if (act instanceof MessageListActivity) {
                // 从主界面过来，等同于会话列表
                return MainActivity.class;
            } else if (act instanceof NearUserActivity) {
                // 从附近的人过来
                return NearUserActivity.class;

            } else if (act instanceof FriendListActivity) {
                // 从可能认识的人过来
                return FriendListActivity.class;

            }
        }

        // 其他情况返回会话列表
        return MainActivity.class;
    }

    /**
     * @return the downloading
     */
    public boolean isDownloading() {
        return downloading;
    }

    /**
     * @param downloading the downloading to set
     */
    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    /**
     * @return channelId
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * @return Channel String
     */
    public String getChannelStr() {
        return channelStr;
    }

    private int nearFilter = ContactsColumns.SEX_UNKNOW;

    /**
     * @param nearFilter the nearFilter to set
     */
    public void setNearFilter(int nearFilter) {
        this.nearFilter = nearFilter;
    }

    /**
     * @return the nearFilter
     */
    public int getNearFilter() {
        return nearFilter;
    }

    public void putTalkReason(long threadId, String talkReason) {
        talkReasons.put(threadId, talkReason);
    }

    public String getTalkReason(long threadId) {
        String talkReason = talkReasons.get(threadId);
        talkReasons.remove(threadId);
        return talkReason;

    }

    public void putGreetStatus(int skyId, boolean status) {
        greetStatus.put(skyId, status);
    }

    public boolean getGreetStatus(int skyId) {
        return greetStatus.get(skyId) == null ? false : true;
    }

    public void clearGreetStatus() {
        greetStatus.clear();
    }

    public void putLoctionBuf(String cellid, NetLocation location) {
        if (locationBuf.size() > locationBufCount)
            locationBuf.clear();
        locationBuf.put(cellid, location);
    }

    public NetLocation getLocationBuf(String cellid) {
        return locationBuf.get(cellid);
    }

    // 返回内存当前进程可以使用的最大内存
    public int getMaxMemory() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int maxMemory = activityManager.getMemoryClass();
        if (maxMemory > 0) {
            return maxMemory;
        } else {
            return Constants.DEFAULT_MAX_MEMORY;
        }
    }

    // 获取聊天界面输入模式
    public int getInputType() {
        return mInputType;
    }

    // 设置聊天界面输入模式
    public void setInputType(int inputType) {
        if (inputType == ChatActivity.INPUT_VOICE) {
            mInputType = inputType;
        } else {
            mInputType = ChatActivity.INPUT_TEXT;
        }
    }

    public List<android.skymobi.messenger.bean.Message> getNotifyMessages() {
        return notifyMessages;
    }

    public void addNotifyMessages(android.skymobi.messenger.bean.Message message) {
        notifyMessages.add(message);
    }

    public void clearNotifyMessages() {
        notifyMessages.clear();
    }

    private static boolean isLoggedIn = false;

    /**
     * 用户实时登录的状态，登录成功后置为true，掉线或注销或未登录成功就置为false
     * 
     * @author Wing.Hu 20120920
     */
    public static void setLoggedIn(boolean isLoggedIn) {
        MainApp.isLoggedIn = isLoggedIn;
    }

    public static boolean isLoggedIn() {
        return (!CommonPreferences.getLogoutedStatus()) && MainApp.isLoggedIn;
    }

    private final BDLocationListener mLocationListener = new LocationListenner();
    private LocationClient mLocationClient = null;

    public LocationClient getBaiduLocation() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(this);// 须在主线程中声明
            mLocationClient.registerLocationListener(mLocationListener);
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);
            option.setAddrType("detail");
            option.setCoorType("gcj02");// 返回国测局经纬度坐标系
            option.disableCache(true);// 启用/禁用缓存定位
            mLocationClient.setLocOption(option);
        }
        return mLocationClient;
    }

    // 获取全局的快聊聊天记录
    public static FastChatCache getFastChatCache() {
        return fastChatCache;
    }

    /**
     * @return the lcsBU
     */
    public LcsBU getLcsBU() {
        return lcsBU;
    }

    /**
     * @param lcsBU the lcsBU to set
     */
    public void setLcsBU(LcsBU lcsBU) {
        this.lcsBU = lcsBU;
    }

    public static void memoryCount() {
        /*
         * 统计内存 MemoryCounter counter = new MemoryCounter(); SLog.e(TAG,
         * "memoryCount HeaderCache:"
         * +counter.estimate(HeaderCache.getInstance())); SLog.e(TAG,
         * "memoryCount ContactListCache:"
         * +counter.estimate(ContactListCache.getInstance()));
         */
    }

    /**
     * 获取全局的单任务处理器
     * 
     * @return
     */
    public static SingleTaskHandler getTaskHandler() {
        return taskHandler;
    }

    /**
     * 是否发送过快聊消息
     */
    private static boolean isFastChatSended;

    public static boolean isFastChatSended() {
        return isFastChatSended;
    }

    public static void saveFastChatSended(boolean isSended) {
        isFastChatSended = isSended;
        CommonPreferences.saveFastChatSended(isSended);
    }

    /**
     * @return the inviteEntrance
     */
    public int getInviteEntrance() {
        return inviteEntrance;
    }

    /**
     * @param inviteEntrance the inviteEntrance to set
     */
    public void setInviteEntrance(int inviteEntrance) {
        this.inviteEntrance = inviteEntrance;
    }

    private boolean loginFromActivity;

    public void setLoginFromActivity() {
        loginFromActivity = !CommonPreferences.getLoginedStatus()
                || (CommonPreferences.getLoginedStatus()
                && CommonPreferences.getLogoutedStatus());
    }

    public boolean getLoginFromActivity() {
        return loginFromActivity;
    }

}
