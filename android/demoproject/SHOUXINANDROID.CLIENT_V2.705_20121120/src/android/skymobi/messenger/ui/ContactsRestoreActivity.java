
package android.skymobi.messenger.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactsRestoreListCache;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.view.View;
import android.widget.Button;

import com.skymobi.android.sx.codec.beans.common.RestorableContacts;

/**
 * @ClassName: ContactsRestoreActivity
 * @Description: 恢复联系人
 * @author Lv.Lv
 * @date 2012-8-29 上午10:27:26
 */
public class ContactsRestoreActivity extends TopActivity {

    private static final int REQUEST_RESTORE_LIST = 0x0001; // 恢复联系人列表

    private ProgressDialog mWaitDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_restore);
        initTopBar();
        init();
    }

    private void init() {
        Button restoreBtn = (Button) findViewById(R.id.contacts_restore_btn);
        restoreBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mService.getSettingsModule().getRestorableConacts();
                showWaitDialog(R.string.restore_check_contacts);
            }
        });
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.settings_contacts_restore);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SETTINGS_GET_RESTORECONTACTS_BEGIN:
                    break;
                case CoreServiceMSG.MSG_SETTINGS_GET_RESTORECONTACTS_END:
                    hideWaitDialog();
                    ArrayList<RestorableContacts> list = (ArrayList<RestorableContacts>) msg.obj;
                    if (list == null || list.size() == 0) {
                        showToast(R.string.restore_contacts_none);
                        break;
                    }
                    ContactsRestoreListCache.getInstance().setList(list);
                    Intent intent = new Intent(ContactsRestoreActivity.this,
                            ContactsRestoreListActivity.class);
                    startActivityForResult(intent, REQUEST_RESTORE_LIST);
                    break;
                case CoreServiceMSG.MSG_SETTINGS_GET_RESTORECONTACTS_FAIL:
                    hideWaitDialog();
                    showToast(getString(R.string.network_error));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESTORE_LIST:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    private void showWaitDialog(int resId) {
        if (mWaitDialog == null) {
            mWaitDialog = new ProgressDialog(this);
            mWaitDialog.setMessage(getString(resId));
            mWaitDialog.setIndeterminate(true);
            mWaitDialog.setCancelable(true);
        }
        mWaitDialog.show();
    }

    private void hideWaitDialog() {
        if (mWaitDialog != null) {
            mWaitDialog.cancel();
        }
    }
}
