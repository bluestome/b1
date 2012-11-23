
package android.skymobi.messenger.network;

import android.content.pm.PackageInfo;
import android.skymobi.app.SXClient;
import android.skymobi.app.net.pool.ConnectionPoolFactory;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.DeviceInfo;
import android.skymobi.messenger.logreport.FreeSkyAgent;
import android.skymobi.messenger.network.module.CommonNetModule;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.network.module.MessageNetModule;
import android.skymobi.messenger.network.module.NearUserNetModule;
import android.skymobi.messenger.network.module.NotifyNetModule;
import android.skymobi.messenger.network.module.SettingsNetModule;
import android.skymobi.messenger.utils.PropertiesUtils;
import android.skymobi.messenger.utils.StringUtil;

import com.skymobi.android.sx.codec.TerminalInfo;

/**
 * @ClassName: NetWorkMgr
 * @Description: 实现网络收发处理
 * @author Michael.Pan
 * @date 2012-2-7 上午09:30:25
 */
public class NetWorkMgr {
    private static final String TAG = NetWorkMgr.class.getSimpleName();
    private static NetWorkMgr sInstance = null;

    private CommonNetModule commonModule = null;
    private MessageNetModule messageNetModule = null;
    private ContactsNetModule contactsNetModule = null;
    private NotifyNetModule notifyNetModule = null;
    private SettingsNetModule settingsNetModule = null;
    private TerminalInfo terminalInfo = null;
    private NearUserNetModule nearUserNetModule = null;

    private SXClient client = null;

    private NetWorkMgr() {
    }

    public synchronized static NetWorkMgr getInstance() {
        if (sInstance == null) {
            sInstance = new NetWorkMgr();

            sInstance.init();

        }
        return sInstance;
    }

    private void init() {
        // SLog.d(TAG, "NetWorkMgr init  start...");
        // long start = System.currentTimeMillis();
        notifyNetModule = new NotifyNetModule();

        client = new SXClient(notifyNetModule);

        terminalInfo = new TerminalInfo();
        DeviceInfo deviceInfo = MainApp.i().getDeviceInfo();
        PackageInfo pi = MainApp.i().getPi();
        terminalInfo.setHsman(deviceInfo.product);
        terminalInfo.setHstype(deviceInfo.modle);
        terminalInfo.setImsi(deviceInfo.imsi);
        terminalInfo.setImei(deviceInfo.imei);
        terminalInfo.setVersion(deviceInfo.sdk);
        terminalInfo.setWidth(deviceInfo.screenWidth);
        terminalInfo.setHeight(deviceInfo.screenHeight);
        terminalInfo.setMem(deviceInfo.totalMem);
        terminalInfo.setAppver(pi.versionCode);
        terminalInfo.setEnter(MainApp.i().getChannelId()); // channel

        terminalInfo.setReserved1((byte) 1);// 终端支持压缩
        terminalInfo.setReserved3((short) pi.versionCode);
        client.setTerminal(terminalInfo);
        // 新的渠道号获取方式
        // String channelStr = MainApp.i().getChannelStr();
        String SkySdk = FreeSkyAgent.getSkySdkChannel();
        String channelStr = MainApp.i().getChannelStr() + SkySdk;
        if (!StringUtil.isBlank(channelStr)) {
            // 只有在渠道字符串获取不为空时才添加该字段值
            client.setChannelNo(channelStr.getBytes());
        }

        commonModule = new CommonNetModule(client);
        messageNetModule = new MessageNetModule(client);
        contactsNetModule = new ContactsNetModule(client);
        settingsNetModule = new SettingsNetModule(client);
        nearUserNetModule = new NearUserNetModule(client);

        /*
         * 从配置文件中读取 *
         */
        String accessIp = PropertiesUtils.getInstance().getAccessIP();
        short accessPort = PropertiesUtils.getInstance().getAccessPort();
        if (accessIp != null && (!accessIp.equals("")) && accessPort > 0) {
            client.setServer(accessIp, accessPort);
        }

        client.setConnectionPoolFactory(new ConnectionPoolFactory());
        client.setPool(client.getConnectionPoolFactory().newPool());

        // long end = System.currentTimeMillis();
        // SLog.d(TAG, "NetWorkMgr init end ... dtime = " + (end - start));
    }

    public void startConnectAccess() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                SLog.d(TAG, "开始连接至access...");
                final long start = System.currentTimeMillis();
                client.getConnectionPool().start();
                try {
                    client.doConnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SLog.d(
                        TAG,
                        "连接至access完成，耗时: "
                                + (float) (System.currentTimeMillis() - start)
                                / 1000 + " s");
            }
        }).start();
    }

    public CommonNetModule getCommonModule() {
        return commonModule;
    }

    public MessageNetModule getMessageNetModule() {
        return messageNetModule;
    }

    public ContactsNetModule getContactsNetModule() {
        return contactsNetModule;
    }

    public NotifyNetModule getNotifyNetModule() {
        return notifyNetModule;
    }

    public SettingsNetModule getSettingsNetModule() {
        return settingsNetModule;
    }

    /**
     * @return the terminalInfo
     */
    public TerminalInfo getTerminalInfo() {
        return terminalInfo;
    }

    public NearUserNetModule getNearUserNetModule() {
        return nearUserNetModule;
    }

    public SXClient getClient() {
        return this.client;
    }
}
