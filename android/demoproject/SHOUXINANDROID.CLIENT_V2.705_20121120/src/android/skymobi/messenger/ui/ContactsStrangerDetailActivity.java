
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.ContactsStrangerAction;
import android.view.View;

import java.util.ArrayList;

/**
 * @ClassName: ContactsStrangerDetailActivity
 * @Description: 本地陌生人详情
 * @author Anson.Yang
 * @date 2012-5-2 下午6:09:35
 */
public class ContactsStrangerDetailActivity extends ContactsDetailActivity {

    public static final String STRANGER_PHONE_FLAG = "STRANGER_PHONE_FLAG";
    public static final String STRANGER_NICKNAME_FLAG = "STRANGER_NICKNAME_FLAG";
    public static final int DIALOG_SAVE_CONTACT = 10;

    private Contact contact;

    private final ContactsStrangerAction action = new ContactsStrangerAction(this);

    @Override
    protected void init() {

        String phone = getIntent().getStringExtra(STRANGER_PHONE_FLAG);
        String nickName = getIntent().getStringExtra(STRANGER_NICKNAME_FLAG);

        contact = new Contact();
        contact.setPhone(phone);
        contact.setNickName(nickName);
        contact.setDisplayname(nickName);

        ArrayList<Account> accounts = new ArrayList<Account>();

        Account account = new Account();
        account.setPhone(phone);
        account.setNickName(nickName);
        accounts.add(account);

        contact.setAccounts(accounts);

        findViewById(R.id.friend_detail_add).setOnClickListener(action);

        initDetail(contact);
        initBottomViews();
    }

    private void initBottomViews() {
        // 隐藏删除按钮
        findViewById(R.id.contacts_detail_delete).setVisibility(View.GONE);
        findViewById(R.id.friend_detail_add).setVisibility(View.VISIBLE);
    }

    @Override
    protected void initDetail(Contact contact) {
        contact.setUserType(ContactsColumns.USER_TYPE_LOACL);
        super.initDetail(contact);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END:
                    removeDialog(DIALOG_SAVE_CONTACT);
                    if (msg.obj == null) {
                        showToast(R.string.contacts_black_list_failed);
                    } else {
                        contact = (Contact) msg.obj;
                        Intent data = new Intent();
                        data.putExtra(FriendDetailActivity.CURRENT_CONTACT, contact);
                        setResult(RESULT_OK, data);

                        Intent intent = new Intent(ContactsStrangerDetailActivity.this,
                                ContactsDetailActivity.class);
                        intent.putExtra(CONTACT_ID_FLAG, contact.getId());
                        startActivity(intent);
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 本地陌生人，不能编辑，不可以举报。
     */
    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.contacts_detail_title);
    }

    public void addContact() {
        showDialog(DIALOG_SAVE_CONTACT);
        mService.getContactsModule().addContact(contact, true);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_SAVE_CONTACT:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_detail_saving));
        }
        dialog = dialogBuilder.create();
        return dialog;
    }
}
