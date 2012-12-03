
package org.bluestome.satelliteweather.biz;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.widget.Toast;

import org.bluestome.satelliteweather.MainApp;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: Biz
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-12-3 下午05:28:38
 */
public class Biz {

    private final String mURL = "http://www.nmc.gov.cn/publish/satellite/fy2.htm";
    private final String mPrefix = "http://image.weather.gov.cn/";
    private final String ACTION_ALARM = "org.bluestome.satelliteweather.alarm";
    private final String APP_FILE_NAME = ".salteliteweather";
    private final String APP_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + APP_FILE_NAME;
    private final String IMAGE_PATH = APP_PATH + File.separator + "images/";
    // 发送统计日志
    private PendingIntent mSender;
    private AlarmRecevier mAlarmRecevier;
    private AlarmManager am;

    /*
     * 初始化设置
     */
    public void initAlarmRecevier() {
        if (null != MainApp.i()) {
            mAlarmRecevier = new AlarmRecevier();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_ALARM);
            MainApp.i().registerReceiver(mAlarmRecevier, intentFilter);
            long firstTime = 0L;
            am = (AlarmManager) MainApp.i().getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                // 发送终端统计数据
                mSender = PendingIntent.getBroadcast(MainApp.i(), 0, new Intent(ACTION_ALARM),
                        0);
                firstTime = SystemClock.elapsedRealtime();
                firstTime += 30 * DateUtils.MINUTE_IN_MILLIS;
                am.setRepeating(AlarmManager.ELAPSED_REALTIME, firstTime,
                        30 * DateUtils.MINUTE_IN_MILLIS, mSender);
            }
        }
    }

    /**
     * 反注册监听器
     */
    public void uninitAlarmRecevier() {
        if (null != MainApp.i()) {
            if (mAlarmRecevier != null) {
                MainApp.i().unregisterReceiver(mAlarmRecevier);
            }
        }
        if (am == null)
            return;
        if (mSender != null) {
            am.cancel(mSender);
        }
    }

    private class AlarmRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ACTION_ALARM)) {
                try {
                    Toast.makeText(MainApp.i(), "时间到了，要执行任务了.", Toast.LENGTH_SHORT);
                    catalog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @throws Exception
     */
    List<String> catalog() throws Exception { // WebsiteBean bean
        List<String> urlList = new ArrayList<String>();
        Parser parser = new Parser();
        parser.setURL(mURL);
        parser.setEncoding("GB2312");

        NodeFilter fileter = new NodeClassFilter(CompositeTag.class);
        NodeList list = parser.extractAllNodesThatMatch(fileter)
                .extractAllNodesThatMatch(
                        new HasAttributeFilter("id", "mycarousel")); // id

        if (null != list && list.size() > 0) {
            CompositeTag div = (CompositeTag) list.elementAt(0);
            parser = new Parser();
            parser.setInputHTML(div.toHtml());
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeList linkList = parser.extractAllNodesThatMatch(linkFilter);
            if (linkList != null && linkList.size() > 0) {
                for (int i = 0; i < linkList.size(); i++) {
                    LinkTag link = (LinkTag) linkList.elementAt(i);
                    String str = link.getLink().replace("view_text_img(", "")
                            .replace(")", "").replace("'", "");
                    if (null != str && str.length() > 0) {
                        final String[] tmps = str.split(",");
                        urlList.add(0, tmps[0]);
                        MainApp.i().getExecutorService().execute(new Runnable() {
                            @Override
                            public void run() {
                                downloadImage(mPrefix + tmps[0]);
                            }
                        });
                    }

                }
            }
        }
        if (null != parser)
            parser = null;
        return urlList;
    }

    /**
     * 下载图片
     * 
     * @param url
     * @return
     */
    private synchronized String downloadImage(String url) {
        URL cURL = null;
        HttpURLConnection connection = null;
        InputStream in = null;
        File file = null;
        try {
            cURL = new URL(url);
            connection = (HttpURLConnection) cURL.openConnection();
            // 获取输出流
            connection.setDoOutput(true);
            connection.setConnectTimeout(5 * 1000);
            connection.setReadTimeout(15 * 1000);
            connection.connect();
            int code = connection.getResponseCode();
            switch (code) {
                case 200:
                    String name = analysisURL(url);
                    file = new File(IMAGE_PATH + File.separator + name);
                    if (file.exists()) {
                        return name;
                    }
                    in = connection.getInputStream();
                    byte[] buffer = new byte[2 * 1024];
                    OutputStream byteBuffer = new FileOutputStream(file, false);
                    int ch;
                    while ((ch = in.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, ch);
                        byteBuffer.flush();
                    }
                    byteBuffer.close();
                    return name;
                default:
                    break;
            }
        } catch (IOException e) {
        } catch (Exception e) {
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * 分析URL,获取图片文件名
     * 
     * @param url
     * @return
     */
    private String analysisURL(String url) {
        int s = url.lastIndexOf("/");
        String name = url.substring(s + 1, url.length());
        if (null == name || name.equals("")) {
            name = String.valueOf(System.currentTimeMillis());
        }
        return name;
    }

}
