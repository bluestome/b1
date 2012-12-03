
package org.bluestome.satelliteweather;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.Toast;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements OnClickListener {

    static String TAG = MainActivity.class.getSimpleName();
    static String mURL = "http://www.nmc.gov.cn/publish/satellite/fy2.htm";
    static String mPrefix = "http://image.weather.gov.cn/";
    Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    TextView showLog = null;
    Button btnStart = null;
    Button btnPlay = null;
    ScrollView scrollView = null;
    LinearLayout mLayout;
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
                    case 0x0103:
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
                        break;
                    case 0x0106:
                        Toast.makeText(getContext(), "开始播放", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x0107:
                        Toast.makeText(getContext(), "结束播放", Toast.LENGTH_SHORT).show();
                        break;
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
        mLayout = (LinearLayout) findViewById(R.id.linearlayout);

        showLog = (TextView) findViewById(R.id.text_show_log);
        showLog.setText("");

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);

        btnPlay = (Button) findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        btnPlay.setEnabled(false);

        imgView = (ImageView) findViewById(R.id.imageView1);
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
                        // executorService.execute(new Runnable() {
                        // @Override
                        // public void run() {
                        // Log.d(TAG, "正在下载图片:" + tmps[0]);
                        // Message msg = new Message();
                        // msg.what = 0x0102;
                        // msg.obj = "正在下载图片:" + tmps[0];
                        // mHandler.sendMessage(msg);
                        // loadImageFromUrl(mPrefix + tmps[0]);
                        // }
                        // });
                    }

                }
            }
        }
        if (null != parser)
            parser = null;
        return urlList;
    }

    Runnable rParserHtml = new Runnable() {
        @Override
        public void run() {
            try {
                mList = catalog();
                if (null != mList && mList.size() > 0) {
                    Message msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "从站点获取图片地址成功，数量为:" + mList.size();
                    mHandler.sendMessage(msg);
                    mHandler.sendEmptyMessage(0x0104);
                } else {
                    Message msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "从站点获取图片地址失败，数量为:" + mList.size();
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                Message msg = new Message();
                msg.what = 0x0102;
                msg.obj = e.getMessage();
                mHandler.sendMessage(msg);
            }
        }
    };

    Runnable rDownloadImg = new Runnable() {
        @Override
        public void run() {
            if (null != mList && mList.size() > 0) {
                mHandler.sendEmptyMessage(0x0106);
                for (String tmp : mList) {
                    Drawable drawable = loadImageFromUrl(mPrefix + tmp);
                    if (null != drawable) {
                        Message msg = new Message();
                        msg = new Message();
                        msg.what = 0x0105;
                        msg.obj = drawable;
                        mHandler.sendMessage(msg);
                        Log.d(TAG, "通知更新图片");
                        SystemClock.sleep(1000L);
                    }
                }
                mHandler.sendEmptyMessage(0x0107);
            } else {
                Message msg = new Message();
                msg.what = 0x0102;
                msg.obj = "没有可用图片\r\n";
                mHandler.sendMessage(msg);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (null != v) {
            switch (v.getId()) {
                case R.id.btn_start:
                    btnPlay.setEnabled(false);
                    if (null != mList) {
                        mList.clear();
                    }
                    showLog.setText("");
                    new Thread(rParserHtml).start();
                    break;
                case R.id.btn_play:
                    new Thread(rDownloadImg).start();
                    break;
            }
        }
    }

    // 从网络上取数据方法
    protected Drawable loadImageFromUrl(String imageUrl) {
        Drawable drawable = null;
        SoftReference<Drawable> softReference = null;
        try {
            if (!imageCache.containsKey(imageUrl)) {
                Log.d(TAG, "图片缓存中不存在[" + imageUrl + "]，从服务器中下载");
                drawable = Drawable.createFromStream(new URL(imageUrl).openStream(),
                        "image.png");
                if (null != drawable) {
                    softReference = new SoftReference<Drawable>(drawable);
                    imageCache.put(imageUrl, softReference);
                }
            } else {
                Log.d(TAG, "图片缓存中存在[" + imageUrl + "]，直接获取");
                softReference = imageCache.get(imageUrl);
                if (null != softReference) {
                    drawable = softReference.get();
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
}
