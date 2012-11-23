
package android.skymobi.messenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactsGroupSendAdapter;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.database.dao.ContactsDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @ClassName: ContactsGroupSendActivity
 * @Description: 群发联系人查看页面
 * @author Lv.Lv
 * @date 2012-4-6 上午9:56:34
 */
public class ContactsGroupSendActivity extends TopActivity {

    private static final String TAG = ContactsGroupSendActivity.class.getSimpleName();
    private static final int REQUEST_CONTACT_DETAIL = 0x01;

    private ArrayList<Contact> mContactsList = null;
    private ArrayList<Account> mAccountsList = null;
    GridView mGridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_group_send);
        init();
        initTopBar();
    }

    @Override
    public void initTopBar() {
        setTopBarTitle(getString(R.string.groupsend_contacts_title));
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        
    }
    
    private void init() {
//        // 标题
//        TextView listTitle = (TextView) findViewById(R.id.list_title);
//        listTitle.setText(R.string.groupsend_contacts_title);

        mAccountsList = (ArrayList<Account>) getIntent()
                .getSerializableExtra(ChatActivity.ACCOUNTS);

        StringBuilder ids = new StringBuilder();
        for (Account account : mAccountsList) {
            ids.append(account.getId() + ",");
        }
        if (ids.length() == 0) {
            return;
        }

        String accountIds = ids.substring(0, ids.length() - 1);
        if (accountIds == null) {
            Log.i(TAG, "no account id");
            return;
        }
        ContactsDAO contactsDAO =
                DaoFactory.getInstance(mContext).getContactsDAO();
        mContactsList =
                contactsDAO.getContactByAccountIds(accountIds);

        ContactsGroupSendAdapter adapter = new ContactsGroupSendAdapter(mContext, mContactsList);
        mGridview = (GridView) findViewById(R.id.gridview_contacts);
        mGridview.setAdapter(adapter);
        mGridview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = mContactsList.get(position);
                if(null != contact){
                    // 判断是否为黑名单
                    if(contact.getBlackList() == ContactsColumns.BLACK_LIST_YES){
                        Intent intent = new Intent(ContactsGroupSendActivity.this,
                                ContactsBlackListDetailActivity.class);
                        intent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                                contact.getId());
                        startActivityForResult(intent, REQUEST_CONTACT_DETAIL);
                    }else if(contact.getBlackList() == ContactsColumns.BLACK_LIST_NO){
                        Intent intent = new Intent(ContactsGroupSendActivity.this,
                                ContactsDetailActivity.class);
                        intent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                                mContactsList.get(position).getId());
                        startActivityForResult(intent, REQUEST_CONTACT_DETAIL);
                    }
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONTACT_DETAIL:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    if (null != data) {
                        Contact contact = (Contact) data
                                .getSerializableExtra(ContactsDetailActivity.CURRENT_CONTACT);
                        for (int i = 0; i < mContactsList.size(); i++) {
                            if (mContactsList.get(i).getId() == contact.getId()) {
                                mContactsList.set(i, contact);
                            }
                        }
                        ContactsGroupSendAdapter adapter = new ContactsGroupSendAdapter(mContext,
                                mContactsList);
                        mGridview.setAdapter(adapter);
                    }
                }
                break;
        }
    }
}
