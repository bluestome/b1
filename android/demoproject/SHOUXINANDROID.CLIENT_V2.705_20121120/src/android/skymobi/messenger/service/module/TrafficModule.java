
package android.skymobi.messenger.service.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.skymobi.messenger.broadcast.MobileStateChangeReciever;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.utils.DateUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 流量统计模块
 * 
 * @ClassName: TrafficModule
 * @author Bluestome.Zhang
 * @date 2012-3-20 下午03:01:40
 */
public class TrafficModule extends BaseModule {

    /** 系统流量文件 **/
    final public String DEV_FILE = "/proc/self/net/dev";

    /** 以太网信息所在行头 **/
    final static String ETHLINE = "lo";//
    /** 移动网络信息所在行头 **/
    final static String GPRSLINE = "rmnet0";
    /** 无线网络信息所在行头 **/
    final static String WIFILINE = "tiwlan0";
    /** 网络类型 **/
    static int NETWORK_TYPE = -1;
    /** 以太网数据 **/
    String[] ethdata = {
            "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0"
    };
    /** 移动网数据 **/
    String[] gprsdata = {
            "0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0"
    };
    /** 无线网数据 **/
    String[] wifidata = {
            "0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0"
    };

    /** Intent过滤器类 **/
    IntentFilter mFilter = new IntentFilter();

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param service
     */
    public TrafficModule(CoreService service) {
        super(service);
        mFilter.addAction(MobileStateChangeReciever.NEW_SHOUXI_NETWORK_CHANGE);
    }

    /**
     * 消息监听器，主要监听来自网络状态的消息,该接收器主要处理系统流量时需要用到
     */
    private final BroadcastReceiver nBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (!bundle.containsKey("network_disconnect")) {
                int type = bundle.getInt("network_type");
                NETWORK_TYPE = type;
                switch (type) {
                    case ConnectivityManager.TYPE_MOBILE:
                        // 移动网络,关机后，数据重置
                        break;
                    case ConnectivityManager.TYPE_WIFI:
                        // 无线网络,WIFI关闭后，数据重置
                        break;
                    default:
                        break;
                }
                Integer[] results = readdev();
                // 获取系统当前日期，写入流量表
                String cdate = DateUtil.getCurrentDate();
                // 判断当前日期下是否存在mobile_latest和wifi_latest值 select * from
                // tbl_traffic where date = 'cdate'
            } else {
                Integer disconnect = bundle.getInt("network_disconnect");
                if (disconnect == 1) {
                    // 系统文件中获取流量数据入库保存
                    Integer[] results = readdev();
                    // 获取系统当前日期
                }
            }
        }
    };

    /**
     * 获取网络类型
     * 
     * @param context
     * @return int 0: 移动网络 1: 无线网络
     */
    private int getNetworkType(Context context) {
        int networkType = -1;
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = connManager.getActiveNetworkInfo();
        if (null != network && network.isAvailable() && network.isConnected()) { // 判断网络是否可用
            networkType = network.getType();
        }
        return networkType;
    }

    /**
     * 读取系统流量文件中的数据
     * 
     * @return Integer[] 结果数据组 [0] = 移动网络数据 [1] = 无线网络数据
     */
    public Integer[] readdev() {
        FileReader fstream = null;
        Integer[] results = {
                0, 0
        };
        try {
            fstream = new FileReader(DEV_FILE);
        } catch (FileNotFoundException e) {

        }
        BufferedReader in = new BufferedReader(fstream, 500);
        String line;
        StringBuffer sb = new StringBuffer();
        String[] segs;
        String[] netdata;

        int count = 0;
        int k;
        int j;
        try {
            while ((line = in.readLine()) != null) {
                segs = line.trim().split(":");
                sb.append(line.trim()).append("\r\n");
                if (line.trim().startsWith(ETHLINE)) { // 本地流量
                    netdata = segs[1].trim().split(" ");
                    String tmp2 = "";
                    for (k = 0, j = 0; k < netdata.length; k++) {
                        if (netdata[k].length() > 0) {
                            ethdata[j] = netdata[k];
                            tmp2 += ethdata[j];
                            if (k < (netdata.length - 1)) {
                                tmp2 += ",";
                            }
                            j++;
                        }
                    }
                } else if (line.trim().startsWith(GPRSLINE)) { // 移动网络流量
                    netdata = segs[1].trim().split(" ");
                    String tmp3 = "";
                    for (k = 0, j = 0; k < netdata.length; k++) {
                        if (netdata[k].length() > 0) {
                            gprsdata[j] = netdata[k];
                            tmp3 += gprsdata[j];
                            if (k < (netdata.length - 1)) {
                                tmp3 += ",";
                            }
                            j++;
                        }
                    }
                } else if (line.trim().startsWith(WIFILINE)) { // WIFI
                                                               // 流量数据，该字段需要增量增加
                    netdata = segs[1].trim().split(" ");
                    String tmp3 = "";
                    for (k = 0, j = 0; k < netdata.length; k++) {
                        if (netdata[k].length() > 0) {
                            wifidata[j] = netdata[k];
                            tmp3 += wifidata[j];
                            if (k < (netdata.length - 1)) {
                                tmp3 += ",";
                            }
                            j++;
                        }
                    }
                }
                count++;
            }
            fstream.close();
            sb = null;
            // [0] 接收数据 [8] 发送数据
            int sysGPRSTraffic = (Integer.valueOf(gprsdata[0]) + Integer.valueOf(gprsdata[8]));
            results[0] = sysGPRSTraffic;
            // [0] 接收数据 [8] 发送数据
            int sysWIFITraffic = (Integer.valueOf(wifidata[0]) + Integer.valueOf(wifidata[8]));
            results[1] = sysWIFITraffic;
        } catch (IOException e) {
        } catch (Exception e) {
        }
        return results;
    }

}
