
package android.skymobi.messenger.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.PhraseContentAdapter;
import android.skymobi.messenger.adapter.PhraseTitleAdapter;
import android.skymobi.messenger.database.PhraseDatabaseHelper;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.StringUtil;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.skymobi.android.sx.codec.beans.common.MsgType;

import java.util.ArrayList;

/**
 * @ClassName: PickPhraseActivity
 * @Description: TODO
 * @author Michael.Pan
 * @date 2012-3-2 下午03:13:30
 */
public class PickPhraseActivity extends TopActivity implements OnItemClickListener,
        OnClickListener {
    private static final String TAG = PickPhraseActivity.class.getSimpleName();

    private ProgressBar mProgressBar;
    private Gallery mTitleGallery;
    private PhraseTitleAdapter mTitleAdapter;
    private ListView mContentListView;
    private PhraseContentAdapter mContentAdapter;
    private final PhraseDatabaseHelper phraseMgr = PhraseDatabaseHelper.getInstance();
    // Handle message
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_CHATMSG_GETRECOMMEND_BEGIN:
                    showWaitDialog();
                    break;
                case CoreServiceMSG.MSG_CHATMSG_GETRECOMMEND_END:
                    hideWaitDialog();
                    updateTitleList();
                    updatePhraseList();
                    break;
                case CoreServiceMSG.MSG_NET_ERROR:
                    hideWaitDialog();
                    showToast(getString(R.string.network_error) + "\r\n[" + Constants.ERROR_TIP
                            + ":0x" + StringUtil.autoFixZero(ResultCode.getCode()) + "]");
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_phrase);
        initTopBar();
        initTitleList();
        initContentList();
        updateTitleList();
        updatePhraseList();
        // 初始化常用短语
        mService.getMessageModule().syncRecommendedMsg();
    }

    private void initTitleList() {
        mTitleAdapter = new PhraseTitleAdapter(this, getLayoutInflater());
        mTitleGallery = (Gallery) findViewById(R.id.phrase_type_gallery);
        mTitleGallery.setAdapter(mTitleAdapter);
        mTitleGallery.setOnItemClickListener(this);
        mTitleGallery.setSelection(2);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    private void initContentList() {
        mContentListView = (ListView) findViewById(R.id.phrase_content_list);
        mContentAdapter = new PhraseContentAdapter(this, getLayoutInflater(), this);
        mContentListView.setAdapter(mContentAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long rowID) {
        int id = parent.getId();
        switch (id) {
            case R.id.phrase_type_gallery:
                mTitleAdapter.updateSelectBg(position);
                updatePhraseList();
                Log.i(TAG, "title position = " + position);
                break;
            case R.id.phrase_content_list:
                Log.i(TAG, "content position2 = " + position);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag();
        String content = mContentAdapter.getContent(pos);
        Intent intent = new Intent();
        intent.putExtra("Content", content);
        Log.i(TAG, "onClick  position = " + pos + "content = " + content);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    // 更新标题list
    private void updateTitleList() {
        ArrayList<MsgType> titleList = phraseMgr.fetchTitle();
        mTitleAdapter.updateList(titleList);
    }

    // 更新内容list
    private void updatePhraseList() {
        int curMsgTypeID = mTitleAdapter.getSelectedMsgTypeID();
        ArrayList<String> contentList = phraseMgr.fetchPhrase(curMsgTypeID);
        mContentAdapter.updateList(contentList);
    }

    // 显示等待对话框
    private void showWaitDialog() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    // 隐藏等待对方框
    private void hideWaitDialog() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.pick_phrase_title);
    }
}
