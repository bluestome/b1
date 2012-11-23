
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsDetailActivity;
import android.skymobi.messenger.ui.ContactsDetailEditActivity;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class ContactsBlacklistDetailEditAction extends ContactsDetailEditAction {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param activity
     */
    public ContactsBlacklistDetailEditAction(BaseActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.contacts_detail_edit_addphone:
                final View view = LayoutInflater.from(activity).inflate(
                        R.layout.contacts_detail_edit_phone,
                        null, false);
                final LinearLayout phones = (LinearLayout) activity
                        .findViewById(R.id.contacts_detail_phones);
                phones.addView(view);

                view.findViewById(R.id.contacts_detail_third).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int childCount = phones.getChildCount();
                                if (childCount > 1) {
                                    phones.removeView(view);
                                } else {
                                    showToast(R.string.contacts_detail_edit_need_phone);
                                }
                            }
                        });

                final ScrollView scrollView = (ScrollView) activity
                        .findViewById(R.id.contacts_detail_scrollview);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;
            case R.id.contacts_detail_edit_del:
                LinearLayout layout = (LinearLayout) activity
                        .findViewById(R.id.contacts_detail_phones);
                int childCount = layout.getChildCount();
                if (childCount > 1) {
                    layout.removeView(activity.findViewById(R.id.contacts_detail_edit_first_phone));
                }
                break;
            // case R.id.contacts_detail_edit_cancel:
            // activity.setResult(Activity.RESULT_CANCELED);
            // finish();
            // break;
            case R.id.topbar_imageButton_rightII:
                String name = activity.getEditorText(R.id.contacts_detail_name_editor);
                String note = activity.getEditorText(R.id.contacts_detail_note_editor);
                LinearLayout phonesLayout = (LinearLayout) activity
                        .findViewById(R.id.contacts_detail_phones);
                Intent currentIntent = activity.getIntent();
                Contact contactPassed = mService.getContactsModule().getContactById(
                        (Long) currentIntent
                                .getSerializableExtra(ContactsDetailActivity.CONTACT_FLAG));
                ArrayList<Account> accounts = new ArrayList<Account>();
                for (int i = 0; i < phonesLayout.getChildCount(); i++) {
                    View child = phonesLayout.getChildAt(i);
                    EditText editor = (EditText) child.findViewById(R.id.contacts_detail_content);
                    if (editor == null) {// 分割线
                        continue;
                    }
                    String text = editor.getText().toString();
                    if (text.length() > 0) {
                        text = AndroidSysUtils.removeHeader(text);
                        if (text.length() == 0) {
                            continue;
                        }
                        Account account = new Account();
                        account.setPhone(text);
                        account.setContactId(contactPassed.getId());
                        accounts.add(account);
                    }
                }
                if (TextUtils.isEmpty(name) || accounts.size() == 0) {
                    showToast(R.string.contacts_detail_edit_need_phone_and_name);
                    return;
                }
                Contact contact = new Contact();
                contact.setDisplayname(name);
                contact.setNote(note);
                contact.setAccounts(accounts);

                String actionType = currentIntent
                        .getStringExtra(ContactsDetailEditActivity.ACTION_TYPE);
                showDialog(ContactsDetailEditActivity.CONTACT_EDIT);
                if (ContactsDetailEditActivity.ACTION_EDIT.equals(actionType)) {
                    contact.setId(contactPassed.getId());
                    contact.setLocalContactId(contactPassed.getLocalContactId());
                    contact.setCloudId(contactPassed.getCloudId());
                    contact.setBlackList(1);
                    mService.getContactsModule().updateContact(contact);
                } else {
                    mService.getContactsModule().addContact(contact, true);
                }
                break;
        }
    }
}
