
package android.skymobi.messenger.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.MessageMultiDeleteListAdapter;
import android.skymobi.messenger.bean.Threads;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

/**
 * @ClassName: MessageMultiDeleteListActivity
 * @Description: 批量删除会话列表界面
 * @author Michael.Pan
 * @date 2012-7-24 上午09:34:57
 */
public class MessageMultiDeleteListActivity extends MessageListActivity implements
        android.view.View.OnClickListener {

    private static final String TAG = MessageMultiDeleteListActivity.class.getSimpleName();

    private Button mDeleteBtn;
    private Button mSelectAllBtn;
    private LinearLayout mLyButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewStatus();
        SLog.d(TAG, "MessageMultiDeleteListActivity onCreate");
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.message_multi_delete_title);
    }

    private void initViewStatus() {

        // 初始化网络状态
        if (mLayoutNet != null)
            mLayoutNet.setVisibility(View.GONE);

        mMsgAdapter = new MessageMultiDeleteListAdapter(this, getLayoutInflater(), mMessageModule);
        mListView.setAdapter(mMsgAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(this);

        mLyButtons = (LinearLayout) findViewById(R.id.message_list_delete_buttons);
        mLyButtons.setVisibility(View.VISIBLE);

        mLyButtons.setVisibility(View.VISIBLE);
        mDeleteBtn = (Button) findViewById(R.id.message_list_delete);

        mDeleteBtn.setOnClickListener(this);
        mDeleteBtn.setEnabled(false);
        mSelectAllBtn = (Button) findViewById(R.id.message_list_selectall);
        mSelectAllBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.message_list_delete: {
                // 如果有短信则弹出对话框显示，否则不弹出对话框显示
                if (isContaintSMS()) {
                    removeDialog(DIALOG_CLEAR_ALL);
                    showDialog(DIALOG_CLEAR_ALL);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            clearAllSelectThreads();
                        }
                    }.start();
                }
            }
                break;
            case R.id.message_list_selectall:
                ((MessageMultiDeleteListAdapter) mMsgAdapter).selectAllorNone();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onOwnResume() {
        mHandler.sendEmptyMessage(PRIVATE_MSG_UPDATE);
        updateList();
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        super.notifyObserver(what, obj);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.clear();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private boolean isContaintSMS() {
        boolean isContaintSMS = false;
        List<Threads> list = mMsgAdapter.getSelectList();
        for (Threads threads : list) {
            try {
                if (threads.getAddressList().get(0).getPhone() != null) {
                    isContaintSMS = true;
                    SLog.d(TAG, "isContaintSMS = " + isContaintSMS);
                    break;
                }
            } catch (NullPointerException e) {
                isContaintSMS = false;
            }

        }
        return isContaintSMS;
    }
}
