
package android.skymobi.messenger.utils;

import android.os.Environment;
import android.skymobi.common.log.SLog;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * 属性文件工具类
 * 
 * @ClassName: PropertiesUtils
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-4-12 下午10:47:08
 */
public class PropertiesUtils {

    // 属性文件名
    private static final String CONFIG_NAME = "config.ini";
    // access服务器地址和端口
    private static final String ACCESS_IP = "access_ip";
    private static final String ACCESS_PORT = "access_port";
    // 文件服务器地址
    private static final String FILE_URL = "file_url";
    // SUP更新服务器的地址
    private static final String SUP_URL = "sup_url";
    // 写文件日志开关
    private static final String SAVE_TO_FILE = "save_log_to_sdcard";
    // 写DDMS日志开关
    private static final String SAVE_TO_DDMS = "save_log_to_ddms";
    // 日志输出等级
    private static final String LOG_LEVEL = "log_level";

    private static InputStream inputFile;
    private static Properties Pt = new Properties();
    private static PropertiesUtils sInstance = null;
    private static Object classLock = PropertiesUtils.class;
    private static final String TAG = PropertiesUtils.class.getSimpleName();

    public static PropertiesUtils getInstance() {
        synchronized (classLock) {
            if (sInstance == null) {
                sInstance = new PropertiesUtils();
                initConfiguration();
            }
            return sInstance;
        }
    }

    // 写文件日志开关 返回 true 保存 false 不保存
    public boolean isSaveFileLog() {
        String strSaveFileLog = Pt.getProperty(SAVE_TO_FILE, "off").trim();
        SLog.d(TAG, "strSaveFileLog = " + strSaveFileLog);
        if (strSaveFileLog.equalsIgnoreCase("on")) {
            return true;
        } else {
            return false;
        }
    }

    // 写DDMS日志开关 返回 true 保存 false 不保存
    public boolean isSaveDDMSLog() {
        String strSaveDDMSLog = Pt.getProperty(SAVE_TO_DDMS, "off").trim();
        SLog.d(TAG, "strSaveDDMSLog = " + strSaveDDMSLog);
        if (strSaveDDMSLog.equalsIgnoreCase("on")) {
            return true;
        } else {
            return false;
        }
    }

    // 日志输出等级
    public int getLogLevel() {
        String level = Pt.getProperty(LOG_LEVEL, "1");
        return Integer.valueOf(level);
    }

    // 获取Access IP地址
    public String getAccessIP() {
        return Pt.getProperty(ACCESS_IP, "");
    }

    // 获取Access 端口
    public short getAccessPort() {
        String port = Pt.getProperty(ACCESS_PORT, "0");
        return Short.parseShort(port);
    }

    // 获取文件服务器URL
    public String getFileURL() {
        return Pt.getProperty(FILE_URL, "http://sfs.skymobiapp.com:7002/");
    }

    public String getSupURL() {
        return Pt.getProperty(SUP_URL, "http://sup.skymobiapp.com:6011/");
    }

    private static void initConfiguration() {
        try {
            inputFile = new FileInputStream(new File(Environment.getExternalStorageDirectory()
                    + File.separator + CONFIG_NAME));
            Pt.load(inputFile);
            inputFile.close();
        } catch (Exception e) {
            Log.e(TAG, "config.ini is not found....");
        }
    }
}
