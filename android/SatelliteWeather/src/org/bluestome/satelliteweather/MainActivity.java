
package org.bluestome.satelliteweather;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.bluestome.satelliteweather.common.Constants;
import org.bluestome.satelliteweather.utils.FileUtils;
import org.bluestome.satelliteweather.utils.HttpClientUtils;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements OnClickListener {

    static String TAG = MainActivity.class.getSimpleName();
    static Map<String, String> imageCache = new HashMap<String, String>();
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    TextView showLog = null;
    Button btnStart = null;
    Button btnPlay = null;
    Button btnClearConsole = null;
    ScrollView scrollView = null;
    LinearLayout mLayout;
    LinearLayout mLayout2;
    ImageView imgView = null;
    List<String> mList = null;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null != msg) {
                switch (msg.what) {
                    case 0x0101:
                        String url = (String) msg.obj;
                        String s2 = showLog.getText().toString();
                        showLog.setText(s2 + "\r\n" + url);
                        break;
                    case 0x0102:
                        String ex = (String) msg.obj;
                        String old = showLog.getText().toString();
                        showLog.setText(old + "\r\n" + ex);
                        break;
                    case 0x0104:
                        if (!btnPlay.isEnabled()) {
                            btnPlay.setEnabled(true);
                        }
                        break;
                    case 0x0105:
                        Drawable drawable = (Drawable) msg.obj;
                        BitmapDrawable bd = (BitmapDrawable) drawable;
                        Bitmap bm = bd.getBitmap();
                        imgView.setImageBitmap(bm);
                        System.gc();
                        break;
                    case 0x0106:
                        break;
                    case 0x0107:
                        break;
                }
                if (showLog.getText().toString().length() > 0) {
                    btnClearConsole.setEnabled(true);
                }
                post(new Runnable() {
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
        setContentView(R.layout.main);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setVisibility(View.VISIBLE);

        mLayout = (LinearLayout) findViewById(R.id.linearlayout);

        mLayout2 = (LinearLayout) findViewById(R.id.linearlayout_image);
        mLayout2.setVisibility(View.GONE);

        showLog = (TextView) findViewById(R.id.text_show_log);
        showLog.setText("");

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);

        btnPlay = (Button) findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        // btnPlay.setEnabled(false);

        btnClearConsole = (Button) findViewById(R.id.btn_clear_console);
        btnClearConsole.setOnClickListener(this);
        btnClearConsole.setEnabled(false);

        imgView = (ImageView) findViewById(R.id.imageView1);

        init();
    }

    /**
     * 初始化文件目录
     */
    private void init() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 创建目录
            File path = new File(Constants.APP_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            // 创建图片目录
            path = new File(Constants.SATELINE_CLOUD_IMAGE_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
        }

    }

    /**
     * @throws Exception
     */
    List<String> catalog() throws Exception { // WebsiteBean bean
        List<String> urlList = new ArrayList<String>();
        Message msg = new Message();
        msg.what = 0x0102;
        msg.obj = "开始获取网页";
        mHandler.sendMessage(msg);
        byte[] body = HttpClientUtils.getBody(Constants.SATELINE_CLOUD_URL);
        if (null == body || body.length == 0) {
            msg = new Message();
            msg.what = 0x0102;
            msg.obj = "获取服务端返回的内容为空";
            mHandler.sendMessage(msg);
            return urlList;
        }
        Parser parser = new Parser();
        String html = new String(body, "GB2312");
        parser.setInputHTML(html);
        parser.setEncoding("GB2312");
        msg = new Message();
        msg.what = 0x0102;
        msg.obj = "开始分析网页";
        mHandler.sendMessage(msg);
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
            msg = new Message();
            msg.what = 0x0102;
            msg.obj = "开始分析页面子元素";
            mHandler.sendMessage(msg);
            if (linkList != null && linkList.size() > 0) {
                for (int i = 0; i < linkList.size(); i++) {
                    LinkTag link = (LinkTag) linkList.elementAt(i);
                    String str = link.getLink().replace("view_text_img(", "")
                            .replace(")", "").replace("'", "");
                    if (null != str && str.length() > 0) {
                        final String[] tmps = str.split(",");
                        urlList.add(0, tmps[0]);
                        if (!imageCache.containsKey(tmps[0])) {
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadImageFromUrl(Constants.PREFIX_SATELINE_CLOUD_IMG_URL
                                            + tmps[0]);
                                }
                            });
                        }
                    }

                }
            }
        }
        if (null != parser)
            parser = null;
        msg = new Message();
        msg.what = 0x0102;
        msg.obj = "解析结束";
        mHandler.sendMessage(msg);
        return urlList;
    }

    Runnable rParserHtml = new Runnable() {
        @Override
        public void run() {
            Message msg = null;
            try {
                String lastModifyTime = HttpClientUtils
                        .getLastModifiedByUrl(Constants.SATELINE_CLOUD_URL);
                if (null != lastModifyTime
                        && !lastModifyTime.equals(MainApp.i().getLastModifyTime())) {
                    long s1 = System.currentTimeMillis();
                    mList = catalog();
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "从网页解析耗时:" + (System.currentTimeMillis() - s1) + " ms";
                    mHandler.sendMessage(msg);
                    if (null != mList && mList.size() > 0) {
                        MainApp.i().setLastModifyTime(lastModifyTime);
                        msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = "从站点获取图片地址成功，数量为:" + mList.size();
                        mHandler.sendMessage(msg);
                    } else {
                        msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = "从站点获取图片地址失败，数量为:" + mList.size();
                        mHandler.sendMessage(msg);
                    }
                } else {
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "当前数据已经是最新数据不需要再处理\r\n";
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                msg = new Message();
                msg.what = 0x0102;
                msg.obj = e.getMessage();
                mHandler.sendMessage(msg);
            }
        }
    };

    Runnable rDownloadImg = new Runnable() {
        @Override
        public void run() {
            // 先从本地文件开始入手
            File dir = new File(Constants.SATELINE_CLOUD_IMAGE_PATH);
            File[] files = dir.listFiles();
            FileUtils.sortFilesByFileName(files);
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File s = files[i];
                    Log.d(TAG, s.getName());
                    Drawable drawable = null;
                    try {
                        drawable = Drawable.createFromStream(
                                new FileInputStream(new File(s.getAbsolutePath())), "image.png");
                    } catch (FileNotFoundException e) {
                        Message msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = s.getName() + "找不到\r\n";
                        mHandler.sendMessage(msg);
                    }
                    if (null != drawable) {
                        Message msg = new Message();
                        msg = new Message();
                        msg.what = 0x0105;
                        msg.obj = drawable;
                        mHandler.sendMessage(msg);
                        SystemClock.sleep(30L);
                    }
                }
            } else {
                if (null != mList && mList.size() > 0) {
                    for (String tmp : mList) {
                        Drawable drawable = loadImageFromUrl(Constants.PREFIX_SATELINE_CLOUD_IMG_URL
                                + tmp);
                        if (null != drawable) {
                            Message msg = new Message();
                            msg = new Message();
                            msg.what = 0x0105;
                            msg.obj = drawable;
                            mHandler.sendMessage(msg);
                            SystemClock.sleep(30L);
                        }
                    }
                } else {
                    Message msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "没有可用图片\r\n";
                    mHandler.sendMessage(msg);
                }
            }
        }
    };

    Runnable rPlayImg = new Runnable() {
        @Override
        public void run() {
            // 先从本地文件开始入手
            File dir = new File(Constants.SATELINE_CLOUD_IMAGE_PATH);
            File[] files = dir.listFiles();
            FileUtils.sortFilesByFileName(files);
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File s = files[i];
                    Log.d(TAG, s.getName());
                    Drawable drawable = null;
                    try {
                        drawable = Drawable.createFromStream(
                                new FileInputStream(new File(s.getAbsolutePath())), "image.png");
                    } catch (FileNotFoundException e) {
                        Message msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = s.getName() + "找不到\r\n";
                        mHandler.sendMessage(msg);
                    }
                    if (null != drawable) {
                        Message msg = new Message();
                        msg = new Message();
                        msg.what = 0x0105;
                        msg.obj = drawable;
                        mHandler.sendMessage(msg);
                        SystemClock.sleep(30L);
                    }
                }
            } else {
                if (null != mList && mList.size() > 0) {
                    for (String tmp : mList) {
                        Drawable drawable = loadImageFromUrl(Constants.PREFIX_SATELINE_CLOUD_IMG_URL
                                + tmp);
                        if (null != drawable) {
                            Message msg = new Message();
                            msg = new Message();
                            msg.what = 0x0105;
                            msg.obj = drawable;
                            mHandler.sendMessage(msg);
                            SystemClock.sleep(30L);
                        }
                    }
                } else {
                    Message msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "没有可用图片\r\n";
                    mHandler.sendMessage(msg);
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (null != v) {
            switch (v.getId()) {
                case R.id.btn_start:
                    scrollView.setVisibility(View.VISIBLE);
                    mLayout2.setVisibility(View.GONE);
                    if (null != mList) {
                        mList.clear();
                    }
                    showLog.setText("");
                    new Thread(rParserHtml).start();
                    break;
                case R.id.btn_play:
                    mLayout2.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    new Thread(rDownloadImg).start();
                    break;
                case R.id.btn_clear_console:
                    showLog.setText("");
                    btnClearConsole.setEnabled(false);
                    break;
            }
        }
    }

    // 从网络上取数据方法
    protected Drawable loadImageFromUrl(String imageUrl) {
        Drawable drawable = null;
        FileInputStream fis = null;
        try {
            if (!imageCache.containsKey(imageUrl)) {
                Log.d(TAG, "图片缓存中不存在，从服务器中下载");
                String name = downloadImage(imageUrl);
                if (null != name && name.length() > 0 && !name.equals("")) {
                    String dir = Constants.SATELINE_CLOUD_IMAGE_PATH + File.separator + name;
                    fis = new FileInputStream(new File(dir));
                    Log.d(TAG, "图片缓存中存在，路径为：" + dir);
                    drawable = Drawable.createFromStream(fis, "image.png");
                    if (null != drawable) {
                        imageCache.put(imageUrl, name);
                    }
                }
            } else {
                Log.d(TAG, "图片缓存中存在，从本地直接获取");
                String name = imageCache.get(imageUrl);
                if (null != name && name.length() > 0 && !name.equals("")) {
                    String dir = Constants.SATELINE_CLOUD_IMAGE_PATH + File.separator + name;
                    Log.d(TAG, "图片缓存中存在，路径为：" + dir);
                    fis = new FileInputStream(new File(dir));
                    drawable = Drawable.createFromStream(fis, "image.png");
                    if (null != drawable) {
                        imageCache.put(imageUrl, name);
                    }
                }
            }
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = 0x0102;
            msg.obj = e.getMessage();
            mHandler.sendMessage(msg);
        }
        return drawable;
    }

    private Context getContext() {
        return this;
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
        Message msg = null;
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
                    file = new File(Constants.SATELINE_CLOUD_IMAGE_PATH + File.separator + name);
                    if (file.exists()) {
                        msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = "已经存在文件:" + name;
                        mHandler.sendMessage(msg);
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
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "请求到服务端失败,错误码:" + code;
                    mHandler.sendMessage(msg);
                    break;
            }
        } catch (IOException e) {
            msg = new Message();
            msg.what = 0x0102;
            msg.obj = e.getMessage();
            mHandler.sendMessage(msg);
        } catch (Exception e) {
            msg = new Message();
            msg.what = 0x0102;
            msg.obj = e.getMessage();
            mHandler.sendMessage(msg);
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
