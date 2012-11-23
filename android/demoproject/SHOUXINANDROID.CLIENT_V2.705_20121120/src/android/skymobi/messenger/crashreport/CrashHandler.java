
package android.skymobi.messenger.crashreport;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.PropertiesUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: CrashHandler
 * @Description: UncaughtException处理，但发送没有捕获的异常，我们用该类来接管app,并且将错误报告记录下来。
 * @author Michael.Pan
 * @date 2012-5-26 下午12:48:24
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();
    private static Object classLock = CrashHandler.class;

    // 系统默认的uncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // crash handle实例
    private static CrashHandler sInstance = null;
    // context
    private Context mContext = null;
    // 用来存储设备信息和异常信息
    private final Map<String, String> infos = new HashMap<String, String>();

    // 用于格式化日期,作为日志文件名的一部分
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static CrashHandler getInstance() {
        synchronized (classLock) {
            if (sInstance == null) {
                sInstance = new CrashHandler();
            }
            return sInstance;
        }
    }

    private CrashHandler() {
    }

    public void init(Context ctx) {
        mContext = ctx;
        // 获取系统默认的uncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandle为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    // 当uncaughtExcepton发生时会转入该函数来处理
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        if (mDefaultHandler != null)
            mDefaultHandler.uncaughtException(thread, ex);

    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     * 
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        boolean isSaveFileLog = PropertiesUtils.getInstance().isSaveFileLog();
        if (ex == null || !isSaveFileLog) {
            return false;
        }
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     * 
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
                infos.put("pruduct", android.os.Build.MANUFACTURER);
                infos.put("modle", android.os.Build.MODEL);
                infos.put("SDK", String.valueOf(android.os.Build.VERSION.SDK_INT));
            }
        } catch (NameNotFoundException e) {
        }
    }

    /**
     * 保存错误信息到文件中
     * 
     * @param ex
     * @return 返回文件名称,将ex写入到sdcard文件中
     */
    private void saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();

        // 加入崩溃标题
        long timestamp = System.currentTimeMillis();
        String time = formatter.format(new Date());
        String title = "\n\n\n\n crash-" + time + "-" + timestamp + "\n";
        sb.append(title);
        // 加入崩溃信息
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        // 记录崩溃的调用栈
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File path = new File(Constants.CRASH_PATH);
                if (!path.exists()) {
                    path.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(Constants.CRASH_PATH + "crash.log",
                        true);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
        }
    }
}
