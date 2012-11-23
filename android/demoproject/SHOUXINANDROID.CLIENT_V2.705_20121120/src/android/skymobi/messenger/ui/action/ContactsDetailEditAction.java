
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.comparator.ComparatorFactory;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsDetailActivity;
import android.skymobi.messenger.ui.ContactsDetailEditActivity;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.ListUtil;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class ContactsDetailEditAction extends BaseAction implements OnClickListener {

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
    public ContactsDetailEditAction(BaseActivity activity) {
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
                if (phones.getChildCount() > 0) {
                    ((EditText) view.findViewById(R.id.contacts_detail_content)).setHint("");
                }
                view.findViewById(R.id.contacts_detail_content).requestFocus();
                phones.addView(view);
                EditText phoneView = (EditText) view.findViewById(R.id.contacts_detail_content);
                phoneView.addTextChangedListener(((ContactsDetailEditActivity) activity).watcher);
                final Intent intent = activity.getIntent();
                if (intent != null) {
                    String actionType = activity.getIntent()
                            .getStringExtra(ContactsDetailEditActivity.ACTION_TYPE);
                    if (ContactsDetailEditActivity.ACTION_EDIT.equals(actionType)) {
                        Contact contact = mService.getContactsModule().getContactById(
                                (Long) intent
                                        .getSerializableExtra(ContactsDetailActivity.CONTACT_FLAG));
                        if (contact.isSkyUser()) {
                            phoneView.setHint(null);
                        }
                    }
                }
                view.findViewById(R.id.contacts_detail_third).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View save = activity
                                        .findViewById(R.id.topbar_imageButton_rightI);
                                int childCount = phones.getChildCount();
                                if (childCount > 1) {
                                    phones.removeView(view);
                                    Long contactId = (Long) intent
                                            .getSerializableExtra(ContactsDetailActivity.CONTACT_FLAG);
                                    if (contactId != null && contactId > 0) {
                                        Contact contactPassed = mService
                                                .getContactsModule()
                                                .getContactById(contactId);
                                        EditText phoneEdit = ((EditText) (phones.getChildAt(0)
                                                .findViewById(R.id.contacts_detail_content)));
                                        if (!contactPassed.isSkyUser()) {
                                            phoneEdit.setHint(activity
                                                    .getString(R.string.contacts_detail_edit_necessary));
                                        } else {
                                            phoneEdit.setHint("");
                                        }
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
                                String name = activity
                                        .getEditorText(R.id.contacts_detail_name_editor);
                                if (TextUtils.isEmpty(name)) {
                                    save.setEnabled(false);
                                    return;
                                }
                                for (int i = 0; i < phones.getChildCount(); i++) {
                                    View child = phones.getChildAt(i);
                                    EditText editor = (EditText) child
                                            .findViewById(R.id.contacts_detail_content);
                                    if (editor == null) {// 分割线
                                        continue;
                                    }
                                    String text = editor.getText().toString();
                                    if (text.length() > 0) {
                                        if (!TextUtils.isEmpty(name)) {
                                            save.setEnabled(true);
                                            return;
                                        }
                                    } else {
                                        save.setEnabled(false);
                                    }
                                }
                            }
                        });

                final ScrollView scrollView = (ScrollView) activity
                        .findViewById(R.id.contacts_detail_scrollview);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        EditText phoneView = (EditText) view
                                .findViewById(R.id.contacts_detail_content);
                        phoneView.setFocusable(true);
                        phoneView.requestFocus();
                        phoneView.focusSearch(View.FOCUS_DOWN);
                    }
                });
                break;
            case R.id.contacts_detail_edit_del:
                LinearLayout layout = (LinearLayout) activity
                        .findViewById(R.id.contacts_detail_phones);
                int childCount = layout.getChildCount();
                if (childCount > 1) {
                    layout.removeView(activity.findViewById(R.id.contacts_detail_edit_first_phone));
                } else {
                    View ve = layout.getChildAt(0);
                    if (ve != null) {
                        EditText et = (EditText) ve
                                .findViewById(R.id.contacts_detail_content);
                        if (et != null) {
                            et.setText("");
                        }
                    }
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
                ArrayList<Account> phoneAccounts = new ArrayList<Account>();
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
                        phoneAccounts.add(account);
                    }
                }
                if (TextUtils.isEmpty(name)) {
                    activity.showToast(R.string.contacts_detail_edit_no_input);
                    break;
                }
                Contact contact = new Contact();
                contact.setDisplayname(name);
                contact.setNote(note);

                Intent currentIntent = activity.getIntent();
                String actionType = currentIntent
                        .getStringExtra(ContactsDetailEditActivity.ACTION_TYPE);
                if (ContactsDetailEditActivity.ACTION_EDIT.equals(actionType)) {
                    Contact contactPassed = mService.getContactsModule().getContactById(
                            (Long) currentIntent
                                    .getSerializableExtra(ContactsDetailActivity.CONTACT_FLAG));
                    if (phoneAccounts.size() < 1 && !contactPassed.isSkyUser()) {
                        activity.showToast(R.string.contacts_detail_edit_no_input);
                        break;
                    }
                    ArrayList<Account> passedAccounts = contactPassed.getAccounts();
                    Comparator<Account> accountComparator = ComparatorFactory
                            .getAccountComparator();
                    ArrayList<Account> accounts = new ArrayList<Account>();
                    // 将带skyid的账号再次传到云端
                    for (Account account : passedAccounts) {
                        Account obj = ListUtil.getObject(phoneAccounts, account, accountComparator);
                        if (obj != null) {
                            if (!ListUtil.contains(accounts, account, accountComparator)) {
                                account.setContactId(contactPassed.getId());
                                accounts.add(account);
                            }
                        } else if (account.getSkyId() > 0) {
                            Account a = new Account();
                            a.setPhone("");
                            a.setId(account.getId());
                            a.setNickName(account.getNickName());
                            a.setOnline(account.isOnline());
                            a.setMain(account.getMain());
                            a.setSkyAccount(account.getSkyAccount());
                            a.setSkyId(account.getSkyId());
                            a.setData1(account.getData1());
                            if (!ListUtil.contains(accounts, a, accountComparator)) {
                                a.setContactId(contactPassed.getId());
                                accounts.add(a);
                                contact.setUserType(ContactsColumns.USER_TYPE_SHOUXIN);
                            }
                        }
                    }
                    for (Account account : phoneAccounts) {
                        boolean result = ListUtil.contains(accounts, account, accountComparator);
                        if (!result) {
                            account.setContactId(contactPassed.getId());
                            accounts.add(account);
                        }
                    }
                    contact.setId(contactPassed.getId());
                    contact.setLocalContactId(contactPassed.getLocalContactId());
                    contact.setCloudId(contactPassed.getCloudId());
                    contact.setUserType(contactPassed.getUserType());
                    contact.setBlackList(contactPassed.getBlackList());
                    contact.setAccounts(accounts);
                    if (!contact.equals(contactPassed)) {
                        mService.getContactsModule().updateContact(contact);
                        showDialog(ContactsDetailEditActivity.CONTACT_EDIT);
                    } else {
                        finish();
                    }
                } else {
                    if (phoneAccounts.size() < 1) {
                        activity.showToast(R.string.contacts_detail_edit_no_input);
                        break;
                    }
                    showDialog(ContactsDetailEditActivity.CONTACT_EDIT);
                    contact.setAccounts(phoneAccounts);
                    mService.getContactsModule().addContact(contact, true);
                }
                v.setClickable(false);
                break;
        }
    }

}
