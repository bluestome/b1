package org.bluestome.satelliteweather;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	static String mURL = "http://www.nmc.gov.cn/publish/satellite/fy2.htm";
	
	static String mPrefix = "http://image.weather.gov.cn/";

	static String TAG = MainActivity.class.getSimpleName();

	TextView showLog = null;
	Button btnStart = null;
	Button btnPlay = null;
	ScrollView scrollView = null;
	LinearLayout mLayout;
	ImageView imgView = null;
	List<String> mList = null;

	private Handler mHandler = new Handler() {
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
						break;
					case 0x0105:
						Drawable drawable = (Drawable)msg.obj;
						imgView.setImageDrawable(drawable);
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
						String[] tmps = str.split(",");
						urlList.add(0,tmps[0]);
					}

				}
			}
		}
		if (null != parser)
			parser = null;
		return urlList;
	}

	Runnable rParserHtml = new Runnable() {
		public void run() {
			try {
				mList = catalog();
				if(null != mList && mList.size() > 0){
					Message msg = new Message();
					msg.what = 0x0102;
					msg.obj = "从站点获取图片地址成功，数量为:"+mList.size();
					mHandler.sendMessage(msg);
					
				}else{
					Message msg = new Message();
					msg.what = 0x0102;
					msg.obj = "从站点获取图片地址失败，数量为:"+mList.size();
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
	
	Runnable rDownloadImg = new Runnable(){
		public void run(){
			if(null != mList && mList.size() > 0){
				for (String tmp : mList) {
					Message msg = new Message();
					msg.what = 0x0102;
					msg.obj = "正在下载图片...\r\n"+tmp;
					mHandler.sendMessage(msg);
					Drawable drawable = loadImageFromUrl(mPrefix+tmp);
					msg = new Message();
					msg.what = 0x0105;
					msg.obj = drawable;
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
					if(null != mList){
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
        try {
            return Drawable.createFromStream(new URL(imageUrl).openStream(),
                    "image.png");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
