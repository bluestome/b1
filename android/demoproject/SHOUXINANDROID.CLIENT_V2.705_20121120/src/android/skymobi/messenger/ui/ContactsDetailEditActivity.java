
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.ContactsDetailEditAction;
import android.skymobi.messenger.utils.HeaderCache;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ContactsDetailEditActivity extends TopActivity {

    // 下面定义的常量 将作为Dialog的id 又做为 activity respose code
    public static final int CONTACT_NEW = 200; // 新增联系人成功
    public static final int CONTACT_NEW_FAILE = 0x100 + 200; // 新增联系人失败
    public static final int CONTACT_EDIT = 202; // 修改联系人成功
    public static final int CONTACT_EDIT_FAILE = 0x101 + 202; // 修改联系人失败

    // Intent参数 定义action类型
    public static final String ACTION_TYPE = "ACTION_TYPE";
    public static final String ACTION_NEW = "ACTION_NEW";
    public static final String ACTION_EDIT = "ACTION_EDIT";

    private ContactsDetailEditAction action = null;
    private Contact contact = null;

    public TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_detail_edit);
        try {
            initTopBar();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);

        action = new ContactsDetailEditAction(this);

        String type = getIntent().getStringExtra(ACTION_TYPE);

        EditText nameView = (EditText) findViewById(R.id.contacts_detail_name_editor);
        nameView.addTextChangedListener(watcher);
        // 标题
        // TextView listTitle = (TextView) findViewById(R.id.list_title);
        if (ACTION_NEW.equals(type)) {
            setTopBarTitle(R.string.contacts_detail_edit_title_new);
            // listTitle.setText(R.string.contacts_detail_edit_title_new);
        } else {
            setTopBarTitle(R.string.contacts_detail_edit_title);
            // listTitle.setText(R.string.contacts_detail_edit_title);
        }

        Button addPhone = (Button) findViewById(R.id.contacts_detail_edit_addphone);
        addPhone.setOnClickListener(action);

        ImageView editDel = (ImageView) findViewById(R.id.contacts_detail_edit_del);
        editDel.setOnClickListener(action);

        // Button cancel = (Button)
        // findViewById(R.id.contacts_detail_edit_cancel);
        // cancel.setOnClickListener(action);

        // 备注
        EditText noteView = (EditText) findViewById(R.id.contacts_detail_note_editor);
        noteView.addTextChangedListener(watcher);
        Intent currentIntent = getIntent();
        String actionType = currentIntent.getStringExtra(ACTION_TYPE);
        if (ACTION_EDIT.equals(actionType)) {
            contact = mService.getContactsModule().getContactById((Long) currentIntent
                    .getSerializableExtra(ContactsDetailActivity.CONTACT_FLAG));

            if (contact.isSkyUser()) {
                findViewById(R.id.contacts_detail_header_layout).setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.contacts_detail_name_editor)).setHint(null);
                initHeader();
                // 昵称
                TextView nickNameView = (TextView) findViewById(R.id.contacts_detail_nick_name);
                for (Account account : contact.getAccounts()) {
                    if (account.getSkyId() > 0) {
                        nickNameView.setText(account.getNickName().trim());
                        // 手信号
                        setTextViewValue(
                                R.id.contacts_detail_shouxin_name,
                                getString(R.string.contacts_detail_shouxin_account,
                                        account.getSkyAccount()));
                    }
                    if (account.isMain()) {
                        break;
                    }
                }
                // 性别
                if (contact.getSex() == ContactsColumns.SEX_FEMALE) {
                    ImageView sexView = (ImageView) findViewById(R.id.contacts_detail_sex_image);
                    sexView.setImageResource(R.drawable.female);
                }
            }

            noteView.setText(contact.getNote());
            // 删除所有号码
            LinearLayout phonesLayout = (LinearLayout) findViewById(R.id.contacts_detail_phones);
            phonesLayout.removeAllViews();
            boolean hasPhones = false;
            // 号码
            List<Account> accounts = contact.getAccounts();
            if (accounts.size() > 0) {
                // 姓名
                setTextViewValue(R.id.contacts_detail_name_editor,
                        contact.getDisplayname());
                for (Account account : accounts) {
                    if (!TextUtils.isEmpty(account.getPhone())) {
                        hasPhones = true;
                        addPhoneChildView(phonesLayout, getString(R.string.contacts_detail_phone),
                                account.getPhone());
                    }
                }
            }
            if (!hasPhones) {
                addPhoneChildView(phonesLayout, getString(R.string.contacts_detail_phone), "");
            }
            if (contact.isSkyUser()) {
                ((EditText) (phonesLayout.getChildAt(0).findViewById(R.id.contacts_detail_content)))
                        .setHint("");
            }
        } else {
            EditText phoneView = (EditText) findViewById(R.id.contacts_detail_content);
            phoneView.addTextChangedListener(watcher);
        }

        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_save,
                action);
    }

    /**
     * @param findViewById
     * @param string
     * @param phone
     */
    private void addPhoneChildView(LinearLayout localDetailLayout, String title, String phone) {
        addChildView(localDetailLayout, R.layout.contacts_detail_edit_phone, title, phone);
    }

    /**
     * 添加子View
     * 
     * @param localDetailLayout
     * @param layoutResID
     * @param title
     * @param content
     */
    private void addChildView(LinearLayout localDetailLayout, int layoutResID, String title,
            String content) {
        final View view = LayoutInflater.from(this).inflate(layoutResID, null);
        final LinearLayout phones = (LinearLayout) findViewById(R.id.contacts_detail_phones);
        setTextViewValue(view, R.id.contacts_detail_title, title);
        setTextViewValue(view, R.id.contacts_detail_content, content);
        EditText phoneView = (EditText) view.findViewById(R.id.contacts_detail_content);
        phoneView.addTextChangedListener(watcher);
        view.findViewById(R.id.contacts_detail_third).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int childCount = phones.getChildCount();
                        if (childCount > 1) {
                            phones.removeView(view);

                            Intent intent = getIntent();
                            Contact contactPassed = mService
                                    .getContactsModule()
                                    .getContactById(
                                            (Long) intent
                                                    .getSerializableExtra(ContactsDetailActivity.CONTACT_FLAG));
                            EditText phoneEdit = ((EditText) (phones.getChildAt(0)
                                    .findViewById(R.id.contacts_detail_content)));
                            if (!contactPassed.isSkyUser()) {
                                phoneEdit
                                        .setHint(getString(R.string.contacts_detail_edit_necessary));
                            } else {
                                phoneEdit.setHint("");
                            }
                        } else {
                            View ve = phones.getChildAt(0);
                            if (ve != null) {
                                EditText et = (EditText) ve
                                        .findViewById(R.id.contacts_detail_content);
                                if (et != null) {
                                    et.setText("");
                                }
                            }
                        }
                    }
                });

        localDetailLayout.addView(view);
        if (phones.getChildCount() > 1) {
            phoneView.setHint("");
        }
    }

    /**
     * 加载头像
     * 
     * @param contact
     */
    private void initHeader() {
        ImageView imageView = (ImageView) findViewById(R.id.contacts_detail_head);
        HeaderCache.getInstance().getHeader(contact.getPhotoId(), contact.getDisplayname(),
                imageView);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        switch (id) {
            case CONTACT_EDIT:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_detail_saving), true, false);
        }
        return dialog;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_CONTACTS_EDIT_ADD_SUCCESS:
                case CoreServiceMSG.MSG_CONTACTS_EDIT_UPDATE_SUCCESS:
                    ContactListCache.getInstance().recreateItems(
                            mService.getContactsModule().getContactInfoForList());
                    removeDialog(ContactsDetailEditActivity.CONTACT_EDIT);
                    if (msg.what == CoreServiceMSG.MSG_CONTACTS_EDIT_ADD_SUCCESS) {
                        showToast(getString(R.string.contacts_list_new_sucess,
                                getEditorText(R.id.contacts_detail_name_editor)));
                    } else {
                        showToast(R.string.friend_save_success);
                    }
                    Intent data = new Intent();
                    data.putExtra("data", (Long) msg.obj);
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case CoreServiceMSG.MSG_CONTACTS_EDIT_ADD_FAIL:
                case CoreServiceMSG.MSG_CONTACTS_EDIT_UPDATE_FAIL:
                    // 将按钮置为可点击
                    findViewById(R.id.topbar_imageButton_rightII).setClickable(true);
                    removeDialog(ContactsDetailEditActivity.CONTACT_EDIT);
                    showToast(R.string.friend_save_failed);
                    break;
            }
        }
    };

    @Override
    public void notifyObserver(int what, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, obj));
    }
}
