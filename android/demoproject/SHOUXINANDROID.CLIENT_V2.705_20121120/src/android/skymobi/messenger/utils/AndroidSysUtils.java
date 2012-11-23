
package android.skymobi.messenger.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

/**
 * 安卓系统层工具类
 * 
 * @ClassName: AndroidSysUtils
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-3-21 下午01:55:10
 */
public class AndroidSysUtils {
    private static final String TAG = AndroidSysUtils.class.getSimpleName();

    /**
     * 获取网络类型
     * 
     * @param context
     * @return int -1：无网络,也可以理解为当前没有可用网络 0: 移动网络 1: 无线网络
     */
    public static int getNetworkType(Context context) {
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
     * 判断网络是否可用（wifi/mobile 是否可访问）
     * 
     * @param context
     * @return true 表示wifi/mobile 之一可访问，false 表示wifi mobile都不能访问
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager mConnMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean flag = false;
        if ((mWifi != null) && ((mWifi.isAvailable()) || (mMobile.isAvailable()))) {
            if ((mWifi.isConnected()) || (mMobile.isConnected())) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * 
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * 
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String removeHeader(String phone) {
        if (phone != null) {
            // 去掉空格，android 4.0的机器会出现空格
            phone = phone.replace(" ", "");
            // 去掉0086
            if (phone.startsWith("0086")) {
                phone = phone.replaceFirst("0086", "");
            }
            // 去掉+86
            if (phone.startsWith("+86")) {
                phone = phone.replaceFirst("\\+86", "");
            }
        }
        return phone;
    }

    public static String removeSpace(String phone) {
        if (phone != null) {
            phone = phone.replaceAll("-", "");
            phone = phone.replaceAll("\\s+", "");
        }
        return phone;
    }

    public static int getScreenWidthForDip(Context context) {
        return px2dip(context, MainApp.i().getDeviceInfo().screenWidth);
    }

    public static int getScreenWidthForPix(Context context) {
        return MainApp.i().getDeviceInfo().screenWidth;
    }

    /**
     * 获取连接状态
     * 
     * @return boolean true: 连接正常 false: 未连接
     */
    public static boolean getConnectionStatus(String ip, String port) {
        boolean b = false;
        Socket socket;
        int tPort = 80;
        InputStream is;
        try {
            try {
                tPort = Integer.valueOf(port);
            } catch (NumberFormatException nfe) {
            }
            socket = new Socket(ip, tPort);
            is = socket.getInputStream();
            is.read(new byte[] {
                    (byte) 0x8A, (byte) 0xED, (byte) 0x9C, (byte) 0xF3, 0x7E, 0x32, (byte) 0xB9
            });
            is.close();
            socket.close();
            b = true;
        } catch (IOException e) {
        }
        return b;
    }

    /**
     * cpu核数
     */
    public static int getCpuNums() {
        String fileName = "/proc/cpuinfo";// "/proc/cpuinfo";
        int cpuNums = 0;
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(fileName);
            br = new BufferedReader(fr, 8192);
            String line = br.readLine();
            while (line != null) {
                if ("processor".equals(line.split("\\s+")[0])) {
                    cpuNums++;
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "读取CPU数量时错误:" + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
        return cpuNums < 1 ? 1 : cpuNums;
    }

    /**
     * 内存总大小
     */
    public static long getTotalMem() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File("/proc/meminfo")));
            String firstLine = br.readLine();
            return Integer.valueOf(firstLine.split("\\s+")[1]) * 1024;
        } catch (Exception e) {
            Log.e(TAG, "读取手机内存总大小时错误:" + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
        return 512 * 1024 * 1024;
    }

    public static int checkSDCard(Context context) {
        // String status = Environment.getExternalStorageState();
        /*
         * Toast makeText = Toast.makeText(context, R.string.no_sdcard_tip,
         * Toast.LENGTH_SHORT); makeText.setGravity(Gravity.CENTER, 0, 0); if
         * (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) { // SD卡存在 }
         * else if
         * (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) { //
         * 虽然SD存在，但是为只读状态 } else { if
         * (status.equalsIgnoreCase(Environment.MEDIA_REMOVED)) {
         * makeText.show(); return -1; } else if
         * (status.equalsIgnoreCase(Environment.MEDIA_SHARED)) { //
         * 虽然SD卡存在，但是正与PC等相连接 makeText.show(); return -1; } else if
         * (status.equalsIgnoreCase(Environment.MEDIA_BAD_REMOVAL)) { //
         * SD卡在挂载状态下被错误取出 makeText.show(); return -1; } else if
         * (status.equalsIgnoreCase(Environment.MEDIA_CHECKING)) { // 正在检查SD卡
         * makeText.show(); return -1; } else if
         * (status.equalsIgnoreCase(Environment.MEDIA_NOFS)) { //
         * 虽然SD卡存在，但其文件系统不被支持 makeText.show(); return -1; } else if
         * (status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTABLE)) { //
         * 虽然SD卡存在，但是无法被挂载 makeText.show(); return -1; } else if
         * (status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTED)) { //
         * 虽然SD卡存在，但是未被挂载 makeText.show(); return -1; } else { // 其他？ } } return
         * 0;
         */
        if (!isAvailableSDCard(context)) {
            ToastTool.showShort(context, R.string.no_sdcard_tip);
            return -1;
        }
        return 0;
    }

    /**
     * 判断sd是否可用
     */
    public static boolean isAvailableSDCard(Context context) {
        String status = Environment.getExternalStorageState();
        boolean isAvailable = false;
        if (status.equalsIgnoreCase(Environment.MEDIA_REMOVED)
                || status.equalsIgnoreCase(Environment.MEDIA_SHARED)
                || status.equalsIgnoreCase(Environment.MEDIA_BAD_REMOVAL)
                || status.equalsIgnoreCase(Environment.MEDIA_CHECKING)
                || status.equalsIgnoreCase(Environment.MEDIA_NOFS)
                || status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTABLE)
                || status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTED)) {
            isAvailable = false;
        } else {
            isAvailable = true;
        }
        // if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
        // isAvailable = false;
        // }
        SLog.d(TAG, "SDCard stauts:" + isAvailable);
        return isAvailable;
    }

    /**
     * 获取存储卡的剩余容量，单位为字节
     * 
     * @param filePath
     * @return
     */
    public static long getAvailableStore() {
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(path.getPath());
            long blocSize = statFs.getBlockSize();
            long availaBlock = statFs.getAvailableBlocks();
            return availaBlock * blocSize;
        } catch (Exception e) {
            // 某些情况下文件系统会出错
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean getSimCard(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        switch (tm.getSimState()) {
            case TelephonyManager.SIM_STATE_ABSENT: // 无卡
            case TelephonyManager.SIM_STATE_UNKNOWN: // 未知
                return false;
            default:
                break;
        }

        if (TextUtils.isEmpty(tm.getSubscriberId())) {
            return false;
        }

        return true;
    }

    // 0-complete ， 64-pending ， 128-failed
    // ALL=0;INBOX=1;SENT=2;DRAFT=3;OUTBOX=4;FAILED=5;QUEUED=6;
    public static int getStatus(int orgStatus, int type) {
        int ret = MessagesColumns.STATUS_SUCCESS;
        switch (orgStatus) {
            case 0:
                ret = MessagesColumns.STATUS_SUCCESS;
                break;
            case 64:
                ret = MessagesColumns.STATUS_SENDING;
                break;
            case 128:
                ret = MessagesColumns.STATUS_FAILED;
                break;
        }
        if (type == 5) {
            ret = MessagesColumns.STATUS_FAILED;
        }
        return ret;
    }

    public static String getUnreadCountStr(int unreadCnt) {
        if (unreadCnt > 99) {
            return "99+";
        } else {
            return String.valueOf(unreadCnt);
        }
    }

    /**
     * 通过上下文和权限找出对应的权威路径
     * 
     * @param context
     * @param permission
     * @return
     */
    private static String getAuthorityFromPermission(Context context, String permission) {
        if (permission == null)
            return null;
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_PROVIDERS);
        if (packs != null) {
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (permission.equals(provider.readPermission))
                            return provider.authority;
                        if (permission.equals(provider.writePermission))
                            return provider.authority;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 判断是否已经创建了桌面快捷方式
     * 
     * @see 该方法依赖终端条件比较强，需要对终端各类参数进行判断过滤
     *      对4.0的支持目前还没有方案,貌似4.0有管理快捷图标唯一的功能，不需要应用来控制
     * @return
     */
    public static boolean hasShortcut(Context ctx, String permission) {
        String url = null;
        String authority = getAuthorityFromPermission(ctx, permission);
        if (null != authority) {
            url = "content://" + authority + "/favorites?notify=true";
        }
        if (null != url && !url.equals("")) {
            ContentResolver cr = ctx.getContentResolver();
            Cursor cursor = cr.query(Uri.parse(url), new String[] {
                    "title"
            }, "title=?", new String[] {
                    MainApp.i().getString(R.string.app_name)
            },
                    null);
            if (cursor != null && cursor.moveToNext() && cursor.getCount() > 0) {
                cursor.close();
                return true;
            } else {
                SLog.d(AndroidSysUtils.class.getSimpleName(), "访问launcher.db的URL [" + url + "] 失败");
            }
        } else {
            SLog.d(AndroidSysUtils.class.getSimpleName(), "访问launcher.db的URL构造 [" + url + "] 失败");
        }
        return false;
    }

    /**
     * 对手机号码字符串进行BCD的编码
     * 
     * @return byte[]
     */
    public static byte[] inviteEncode(String tmpPhone) {
        byte[] body = null;
        // 号码分割
        String[] phones = tmpPhone.split(",");
        // 判断手机号码数量是不是2个
        if (null != phones && phones.length == 2) {
            body = new byte[13];
            // 最终BYTE数组的下标
            int index = 0;
            // 最终的BYTE数组
            for (String phone : phones) {
                if (phone.length() == 11) {
                    // 循环获取每个手机号码中的每一个字符
                    for (int i = 0; i < 11; i++) {
                        char x1 = phone.charAt(i);
                        if ((i + 1) < phone.length()) {
                            char x2 = phone.charAt(i + 1);
                            // 字符运算
                            byte t = (byte) ((x1 & 0xF) | ((x2 << 4) & 0xF0));
                            // 对BYTE进行赋值
                            body[index++] = t;
                            i++;
                        } else {
                            // 默认为十六进制'A'为缺省字符
                            int x2 = 10;
                            // 字符运算
                            byte t = (byte) ((x1 & 0xF) | ((x2 << 4) & 0xF0));
                            // 对BYTE进行赋值
                            body[index++] = t;
                        }
                    }
                }
                // 中间第6位是分隔符','的值
                if (index == 6) {
                    body[index++] = ',';
                }
            }
        }
        return body;
    }

    public static void hideSystemSoftKeyboard(Context context, View view) {
        if (context == null || view == null)
            return;
        InputMethodManager inputMgr = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMgr.hideSoftInputFromWindow(view.getWindowToken(),
                0);
    }

}
