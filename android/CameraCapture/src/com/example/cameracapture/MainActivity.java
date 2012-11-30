
package com.example.cameracapture;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements OnClickListener {

    private static String TAG = MainActivity.class.getSimpleName();

    TextView fileSize = null;
    TextView fileDownloading = null;
    TextView showLog = null;
    EditText rateText = null;
    EditText downloadUrl = null;
    Button btnStart = null;
    Button btnPause = null;
    ScrollView scrollView = null;
    LinearLayout mLayout;
    int i = 0;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null != msg) {
                switch (msg.what) {
                    case 1:
                        int length = (Integer) msg.obj;
                        if (length == 0) {
                            fileSize.setText("0");
                        } else {
                            fileSize.setText(String.valueOf(length));
                        }
                        break;
                    case 2:
                        if ((i > 0) && (i % 50 == 0)) {
                            showLog.setText("");
                            // 调用系统垃圾回收一次
                            System.gc();
                            i = 0;
                        }
                        int f = Integer.parseInt(fileDownloading.getText().toString().trim());
                        int f2 = (Integer) msg.obj;
                        String s2 = showLog.getText().toString();
                        showLog.setText(s2 + "\r\n当前获取:" + f2 + " bytes");
                        fileDownloading.setText(String.valueOf((f + f2)) + "\r\n");
                        i++;
                        break;
                    case 3:
                        Toast.makeText(getContext(), "访问异常", Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(getContext(), "耗时:" + msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    case 5:
                        String log = (String) msg.obj;
                        String old = showLog.getText().toString();
                        showLog.setText(old + log);
                        break;
                    case 7:
                        downloadUrl.setText(String.valueOf(msg.obj));
                        break;
                    case 99:
                        fileDownloading.setText("0");
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int off = mLayout.getMeasuredHeight() - scrollView.getHeight();
                        if (off > 0) {
                            scrollView.scrollTo(0, off);
                        }
                    }
                });
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);

        fileSize = (TextView) findViewById(R.id.length);
        fileSize.setText("0");
        fileDownloading = (TextView) findViewById(R.id.downloading);
        fileDownloading.setText("0");

        rateText = (EditText) findViewById(R.id.edit_rate);
        downloadUrl = (EditText) findViewById(R.id.edit_download_url);
        downloadUrl.setText("");

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        mLayout = (LinearLayout) findViewById(R.id.linearlayout);

        showLog = (TextView) findViewById(R.id.text_show_log);
        showLog.setText("");
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);

        btnPause = (Button) findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(this);

    }

    // 下载线程
    private final Runnable rDownload = new Runnable() {
        @Override
        public void run() {
            int it = 2;
            mHandler.sendEmptyMessage(99);
            String rate = rateText.getText().toString();
            if (null == rate || rate.equals("")) {
                rate = "2";
            }
            it = Integer.parseInt(rate);
            File file = null;
            URL nUrl = null;
            HttpURLConnection connection = null;
            InputStream is = null;
            long s1 = System.currentTimeMillis();
            Message msg = new Message();
            try {
                String url = downloadUrl.getText().toString();
                if (null == url || url.equals("")) {
                    url = getString(R.string.default_download_url);
                    // "http://221.179.7.248/CSSPortal/attach/Widget/upload/ProductShowHall/5235534f-578e-4389-a1a3-208e6f2f2dc1-2010-09-08-05-47-39.jpg?nocache=1348198990423";
                    msg = new Message();
                    msg.what = 7;
                    msg.obj = url;
                    mHandler.sendMessage(msg);
                }
                nUrl = new URL(url);
                long start = System.currentTimeMillis();
                connection = (HttpURLConnection) nUrl.openConnection();
                Log.d(TAG, "\tzhang 打开连接耗时：" + (System.currentTimeMillis() - start));
                msg.what = 5;
                msg.obj = Html.fromHtml(getString(
                        R.string.connection_connecting,
                        "打开连接")).toString() + "\r\n";
                mHandler.sendMessage(msg);
                start = System.currentTimeMillis();
                // 设置连接主机超时
                connection.setConnectTimeout(5 * 1000);
                // 设置从主机读取数据超时
                connection.setReadTimeout(15 * 1000);
                connection.connect();
                msg = new Message();
                msg.what = 5;
                msg.obj = Html.fromHtml(getString(
                        R.string.connection_connecting,
                        "正在连接")).toString() + "\r\n";
                mHandler.sendMessage(msg);
                Log.d(TAG, "\tzhang 连接耗时：" + (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();
                int code = connection.getResponseCode();
                Log.d(TAG, "\tzhang 获取响应耗时：" + (System.currentTimeMillis() - start));
                Log.d(TAG, "\tzhang 响应码:" + code);
                if (code == 200) {
                    msg = new Message();
                    msg.what = 5;
                    msg.obj = Html.fromHtml(getString(
                            R.string.connection_connected,
                            "连接成功")).toString() + "\r\n";
                    mHandler.sendMessage(msg);
                    // 请求正常
                    int length = connection.getContentLength();
                    msg = new Message();
                    msg.what = 1;
                    msg.obj = length;
                    mHandler.sendMessage(msg);
                    is = connection.getInputStream();
                    OutputStream os = null;
                    byte[] buffer = new byte[it * 1024];
                    file = new File(Environment.getExternalStorageDirectory()
                            + File.separator + "1354150405qsHrfp.jpg");
                    if (file.exists()) {
                        file = new File(Environment.getExternalStorageDirectory()
                                + File.separator + System.currentTimeMillis() + ".jpg");
                    }
                    try {
                        os = new FileOutputStream(file, true);
                        int pos;
                        while ((pos = is.read(buffer)) != -1) {
                            os.write(buffer, 0, pos);
                            os.flush();
                            msg = new Message();
                            msg.what = 2;
                            msg.obj = pos;
                            mHandler.sendMessage(msg);
                            SystemClock.sleep(30);
                        }
                    } catch (Exception e) {
                        throw new IOException(e);
                    } finally {
                        if (null != os) {
                            os.flush();
                            os.close();
                        }
                        if (null != is) {
                            is.close();
                        }
                    }
                    long sp = System.currentTimeMillis() - s1;
                    Log.d(TAG, "\tzhang 总共耗时：" + sp);
                    msg = new Message();
                    msg.what = 4;
                    msg.obj = sp;
                    mHandler.sendMessage(msg);
                    msg = new Message();
                    msg.what = 5;
                    msg.obj = "\r\n\r\n下载成功，耗时:" + sp + " ms\r\n";
                    mHandler.sendMessage(msg);
                    if (null != file && file.exists()) {
                        file.delete();
                    }
                } else {
                    msg = new Message();
                    msg.what = 5;
                    msg.obj = "\r\n连接失败,响应码:" + code + "\r\n";
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                msg = new Message();
                msg.what = 5;
                msg.obj = "\r\n连接异常:" + e.getMessage() + "\r\n";
                mHandler.sendMessage(msg);
            }
        }
    };

    private Context getContext() {
        return this;
    }

    @Override
    public void onClick(View v) {
        if (null != v) {
            switch (v.getId()) {
                case R.id.btn_start:
                    // 开始
                    new Thread(rDownload).start();
                    break;
                case R.id.btn_pause:
                    // 暂停
                    fileSize.setText("0");
                    fileDownloading.setText("0");
                    showLog.setText("");
                    downloadUrl.setText("");
                    rateText.setText("");
                    break;
            }
        }
    }

}
