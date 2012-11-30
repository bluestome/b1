
package com.example.cameracapture;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static String TAG = MainActivity.class.getSimpleName();

    TextView fileSize = null;

    TextView fileDownloading = null;

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
                        int f = Integer.parseInt(fileDownloading.getText().toString());
                        int f2 = (Integer) msg.obj;
                        fileDownloading.setText(String.valueOf((f + f2)));
                        break;
                    case 3:
                        Toast.makeText(getContext(), "访问异常", Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(getContext(), "耗时:" + msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fileSize = (TextView) findViewById(R.id.length);
        fileSize.setText("0");
        fileDownloading = (TextView) findViewById(R.id.downloading);
        fileDownloading.setText("0");

        new Thread(rDownload).start();
    }

    // 下载线程
    private final Runnable rDownload = new Runnable() {
        @Override
        public void run() {
            URL nUrl = null;
            HttpURLConnection connection = null;
            InputStream is = null;
            long s1 = System.currentTimeMillis();
            try {
                nUrl = new URL(
                        "http://www.6188.com/upload_6188s/flashAll/20121129/1354150405qsHrfp.jpg");
                long start = System.currentTimeMillis();
                connection = (HttpURLConnection) nUrl.openConnection();
                Log.d(TAG, "\tzhang 打开连接耗时：" + (System.currentTimeMillis() - start));
                // 设置连接主机超时
                connection.setConnectTimeout(5 * 1000);
                // 设置从主机读取数据超时
                connection.setReadTimeout(15 * 1000);
                connection.connect();
                int code = connection.getResponseCode();
                Log.d(TAG, "\tzhang 响应码:" + code);
                if (code == 200) {
                    // 请求正常
                    int length = connection.getContentLength();
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = length;
                    mHandler.sendMessage(msg);
                    is = connection.getInputStream();
                    OutputStream os = null;
                    byte[] buffer = new byte[8 * 1024];
                    File file = new File(Environment.getExternalStorageDirectory()
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
                } else {
                    mHandler.sendEmptyMessage(3);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                long sp = System.currentTimeMillis() - s1;
                Log.d(TAG, "\tzhang 总共耗时：" + sp);
                Message msg = new Message();
                msg.what = 4;
                msg.obj = sp;
                mHandler.sendMessage(msg);
            }
        }
    };

    private Context getContext() {
        return this;
    }
}
