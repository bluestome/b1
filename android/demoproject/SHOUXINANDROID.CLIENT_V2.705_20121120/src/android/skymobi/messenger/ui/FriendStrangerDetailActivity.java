
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.StringUtil;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * @ClassName: FriendStrangerDetailActivity
 * @author Sean.Xie
 * @date 2012-7-3 下午2:08:51
 */
public class FriendStrangerDetailActivity extends FriendDetailActivity {

    public static final String FRIEND_SKYID = "friend_skyid";
    public static final String FRIEND = "friend";
    // public static final String CONTACT_TYPE ="contact_type";

    private static final String TAG = "FriendStrangerDetailActivity";

    // private int skyId = 0;
    private Contact contact = null;

    private static final int WAITTING_DIALOG = 101;

    private byte contactType;

    private Dialog dismissDialog = null;
    private Runnable timeoutRunnable = null;

    @Override
    protected void init() {
        SLog.d(TAG, "init...");
        createViews();
        initBottomView();

        reasonLayout.setVisibility(View.GONE);
        addLayout.setOnClickListener(action);

        contact = (Contact) getIntent().getSerializableExtra(FRIEND);
        contactType = getIntent().getByteExtra(CONTACT_TYPE, (byte) 0);

        SLog.d(TAG, "get contactType:" + contactType);
        if (contact == null) {

            skyId = getIntent().getIntExtra(FRIEND_SKYID, 0);
            showDialog(WAITTING_DIALOG);
            Log.i(TAG, "getFriendInfo");

            setTimeout();

            if (mService.getFriendModule().checkStrangerExists(skyId)) {
                // 临时会话的陌生人分支|获取陌生人详情
                mService.getFriendModule().getStrangerDetailInfo(skyId);
            } else {
                mService.getFriendModule().getFriendInfo(0, skyId);
            }
        } else {
            ArrayList<Account> accounts = contact.getAccounts();
            if (TextUtils.isEmpty(contact.getDisplayname())) {
                for (Account account : accounts) {
                    if (!TextUtils.isEmpty(account.getNickName())) {
                        contact.setDisplayname(account.getNickName());
                        break;
                    }
                    if (account.isMain()) {
                        skyId = account.getSkyId();
                    }
                }
            }
            initDetail(contact);
        }
    }

    /**
     * 设置超时消除对话框
     */
    public void setTimeout() {
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                // 超时时将等待框的提示框去掉
                SLog.d(TAG, "timeoutRunnable run");
                if (dismissDialog != null && dismissDialog.isShowing()) {
                    CoreService.getInstance().notifyObservers(
                            CoreServiceMSG.MSG_FRIENDS_DETAIL_FAIL, skyId);
                }
            }
        };
        mHandler.postDelayed(timeoutRunnable, 20 * 1000);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        switch (id) {
            case WAITTING_DIALOG:
                if (null != timeoutRunnable) {
                    mHandler.removeCallbacks(timeoutRunnable);
                    timeoutRunnable = null;
                }
                dismissDialog = ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.friend_detail_syncing), true, true);
                return dismissDialog;
            case BLACK:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
                dialogBuilder.setMessage(R.string.contacts_detail_blacklist_tip);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showDialog(BLACKING);
                        mService.getFriendModule().addStrangerToBlackList(contact);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                break;
            default:
                return super.onCreateDialog(id);
        }
        dialog = dialogBuilder.create();
        return dialog;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_FRIENDS_DETAIL_SUCCESS:
                    contact = (Contact) msg.obj;
                    if (null != contact && contactId == contact.getId()) {
                        SLog.d(TAG, "contact:" + contact);
                        initDetail(contact);
                    }
                    removeDialog(WAITTING_DIALOG);
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS:
                    removeDialog(ADD);
                    if (msg.obj == null) {
                        showToast(R.string.contacts_black_list_failed);
                    } else {
                        contact = (Contact) msg.obj;
                        Intent data = new Intent();
                        data.putExtra(CURRENT_CONTACT, contact);
                        setResult(RESULT_OK, data);
                        ToastTool.showShort(FriendStrangerDetailActivity.this,
                                R.string.add_friend_success);
                        Intent intent = new Intent(FriendStrangerDetailActivity.this,
                                ContactsDetailActivity.class);
                        intent.putExtra(CONTACT_DISTANCE, distance);
                        intent.putExtra(CONTACT_ID_FLAG, contact.getId());
                        finish();
                        startActivity(intent);
                    }
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ADD_BLACKLIST:
                    int result = (Integer) msg.obj;
                    removeDialog(BLACKING);
                    onBlackListFinish(BLACK, result);
                    break;
                case CoreServiceMSG.MSG_FRIENDS_DETAIL_FAIL:
                    SLog.d(TAG, "FriendStrangerDetailActivity error");
                    removeDialog(WAITTING_DIALOG);
                    showToast(getString(R.string.friend_detail_getError) + "\r\n["
                            + Constants.ERROR_TIP + ":0x"
                            + StringUtil.autoFixZero(ResultCode.getCode()) + "]");
                    finish();
                    break;
            }
        }
    };

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(mHandler.obtainMessage(what, obj));
    }

    @Override
    public void addContactFromFriend() {
        showDialog(ADD);
        contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
        mService.getFriendModule().addFriendToCloud(contact, contactType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MESSAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Contact contact = (Contact) data.getSerializableExtra(ChatActivity.CONTACT);
                if (contact != null) {
                    Intent intent = new Intent(this, ContactsDetailActivity.class);
                    intent.putExtra(CONTACT_DISTANCE, distance);
                    intent.putExtra(CONTACT_ID_FLAG, contact.getId());
                    startActivity(intent);
                }
            }
            finish();
        }
    }
}
