
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.exception.ServiceIsNullException;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.StringUtil;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @ClassName: SearchFriendActivity
 * @Description: 精确查找好友
 * @author Anson.Yang
 * @date 2012-7-3 下午2:32:29
 */
public class SearchFriendActivity extends ContactBaseActivity {
    private final static String TAG = "SearchFriendActivity";
    private String mAccountName;

    private final static int SEARCH_FRIEND = 100;
    private final static int NOT_FOUND = 200;

    private EditText mInputView = null;
    private Drawable mIconClear = null;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SEARCH_FRIEND_SUCCESS:
                    Contact contact = (Contact) msg.obj;
                    Log.i(TAG, "search friend success" + contact);
                    if (null != contact && contact.getAccounts().size() > 0
                            && contact.getAccounts().get(0).getSkyId() > 0) {
                        showDetail(contact.getAccounts().get(0).getSkyId(), contact, -1,Constants.CONTACT_TYPE_SEARCH);
                    }
                    removeDialog(SEARCH_FRIEND);
                    break;
                case CoreServiceMSG.MSG_SEARCH_FRIEND_NET_ERROR:
                case CoreServiceMSG.MSG_SEARCH_FRIEND_FAIL:
                    removeDialog(SEARCH_FRIEND);
                    showToast(getString(R.string.search_friend_fail) + "\r\n["
                            + Constants.ERROR_TIP + ":0x"
                            + StringUtil.autoFixZero(ResultCode.getCode()) + "]");
                    break;
                case CoreServiceMSG.MSG_SEARCH_FRIEND_NOTFOUND:
                    removeDialog(SEARCH_FRIEND);
                    showDialog(NOT_FOUND);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_friend);
        try {
            if(mService==null)throw new ServiceIsNullException();
        } catch (ServiceIsNullException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
        initTopBar();
        init();
    }

    private void init() {
        mIconClear = getResources().getDrawable(R.drawable.text_input_delete);
        mInputView = (EditText) findViewById(R.id.search_friend_account);
        mInputView.addTextChangedListener(mTextChangedListener);
        mInputView.setOnTouchListener(mInputViewOnTouch);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        switch (id) {
            case SEARCH_FRIEND:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.search_friend_syncing), true, true);
            case NOT_FOUND:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
                dialogBuilder.setMessage(R.string.search_friend_not_found);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                break;
            default:
                break;
        }
        dialog = dialogBuilder.create();
        return dialog;
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    protected void onResume() {
        // 自动弹出软键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager m = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                m.showSoftInput(mInputView, 0);
            }
        }, 200);

        super.onResume();
    }

    private final TextWatcher mTextChangedListener = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                mInputView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        null, null);
            } else {
                mInputView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        mIconClear, null);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    };

    private final OnTouchListener mInputViewOnTouch = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    int curX = (int) event.getX();
                    if (curX > v.getWidth() - mIconClear.getMinimumWidth()
                            && !TextUtils.isEmpty(mInputView.getText())) {
                        return true; // consume this event
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    int curX = (int) event.getX();
                    if (curX > v.getWidth() - mIconClear.getMinimumWidth()
                            && !TextUtils.isEmpty(mInputView.getText())) {
                        mInputView.setText("");
                    }
                    break;
                }
            }
            return false;
        }
    };

    @Override
    public void initTopBar() {
        // 最好将类中的action使用起来
        OnClickListener click = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAccountName = AndroidSysUtils
                        .removeHeader(getEditorText(R.id.search_friend_account));
                if (TextUtils.isEmpty(mAccountName)) {
                    showToast(R.string.search_friend_no_account);
                } else {
                    showDialog(SEARCH_FRIEND);
                    mService.getFriendModule().searchFriend(mAccountName);
                }
            }
        };
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.search_contacts_title);
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.contacts_list_search_btn, click);
    }

}
