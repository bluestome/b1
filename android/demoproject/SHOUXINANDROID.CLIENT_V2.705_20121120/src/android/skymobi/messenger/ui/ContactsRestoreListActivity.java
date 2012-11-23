
package android.skymobi.messenger.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactsRestoreAdapter;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.CommonPreferences;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.skymobi.android.sx.codec.beans.common.RestorableContacts;

/**
 * @ClassName: ContactsRestoreListActivity
 * @Description: 恢复联系人列表
 * @author Lv.Lv
 * @date 2012-8-29 下午4:13:04
 */
public class ContactsRestoreListActivity extends TopActivity implements OnClickListener {

    private ContactsRestoreAdapter mAdapter = null;
    private ProgressDialog mWaitDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_restore_list);
        initTopBar();
        init();
    }

    private void init() {
        TextView tipText = (TextView) findViewById(R.id.restore_tip);
        long time = CommonPreferences.getContactsLastTimeUpdate();
        int format_flags = DateUtils.FORMAT_ABBREV_ALL |
                DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                DateUtils.FORMAT_CAP_AMPM |
                DateUtils.FORMAT_24HOUR |
                DateUtils.FORMAT_SHOW_YEAR |
                DateUtils.FORMAT_SHOW_DATE |
                DateUtils.FORMAT_SHOW_TIME;
        tipText.setText(getString(R.string.restore_contacts_choose_tip,
                new Object[] {
                    DateUtils.formatDateTime(this, time, format_flags)
                }));

        mAdapter = new ContactsRestoreAdapter(this);
        ListView contactsView = (ListView) findViewById(R.id.contacts_listview);
        contactsView.setAdapter(mAdapter);

        Button restoreBtn = (Button) findViewById(R.id.restore_restore_btn);
        Button selectAllBtn = (Button) findViewById(R.id.restore_selectall_btn);
        restoreBtn.setOnClickListener(this);
        selectAllBtn.setOnClickListener(this);
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.restore_contacts_choose);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.restore_restore_btn:
                showWaitDialog(R.string.restore_sync_title);
                List<RestorableContacts> selected = mAdapter.getSelectedItems();
                ArrayList<RestorableContacts> restorableContacts = new ArrayList<RestorableContacts>();
                for (RestorableContacts item : selected) {
                    restorableContacts.add(item);
                }
                mService.getSettingsModule().restoreContacts(restorableContacts);
                break;
            case R.id.restore_selectall_btn:
                mAdapter.selectAllorNone();
                break;
            default:
                break;
        }

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_BEGIN:
                    break;
                case CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_END:
                    hideWaitDialog();
                    showToast(getString(R.string.restore_success,
                            new Object[] {
                                mAdapter.getSelectedCount()
                            }));
                    setResult(RESULT_OK);
                    finish();
                    break;
                case CoreServiceMSG.MSG_SETTINGS_RESTORE_CONTACTS_FAIL:
                    hideWaitDialog();
                    showToast(getString(R.string.network_error));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    private void showWaitDialog(int resId) {
        if (mWaitDialog == null) {
            mWaitDialog = new ProgressDialog(this);
            mWaitDialog.setMessage(getString(resId));
            mWaitDialog.setIndeterminate(true);
            mWaitDialog.setCancelable(false);
        }
        mWaitDialog.show();
    }

    private void hideWaitDialog() {
        if (mWaitDialog != null) {
            mWaitDialog.cancel();
        }
    }
}
