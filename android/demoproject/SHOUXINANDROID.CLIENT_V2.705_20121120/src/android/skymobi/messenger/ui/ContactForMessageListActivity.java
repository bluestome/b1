
package android.skymobi.messenger.ui;

import android.content.Intent;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactForMessageListAdapter;
import android.skymobi.messenger.adapter.ContactsBaseAdapter;
import android.skymobi.messenger.adapter.PopupMenuAdapter;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.ui.action.ContactForMessageListAction;
import android.skymobi.messenger.ui.action.ContactsListAction;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import java.util.ArrayList;

/**
 * 类说明： 消息会话列表
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class ContactForMessageListActivity extends ContactsListActivity {

    private static final String TAG = ContactForMessageListActivity.class.getSimpleName();
    public static final int REQUEST_CODE = 100;
    ArrayList<String> mList = new ArrayList<String>();

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.search_contacts_add);

        final ArrayList<String> mList = new ArrayList<String>();
        mList.add(getString(R.string.contacts_select_all_contacts));
        mList.add(getString(R.string.contacts_select_all_contacts_cancel));
        final PopupMenuAdapter rightMenuAapter = new PopupMenuAdapter(this, mList);
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_option,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRightPopupMenu(ContactForMessageListActivity.this, rightMenuAapter,
                                findViewById(R.id.topbar_imageButton_rightII),
                                new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                                        // 仅当列表不为空时，全选和全不选才有效
                                        if (((ContactForMessageListAdapter) adapter).getCount() > 0) {
                                            Button multiDeleteButton = (Button) findViewById(R.id.contacts_multi_delete);
                                            if (mList.get(position).equals(getString(
                                                    R.string.contacts_select_all_contacts))) {
                                                ((ContactForMessageListAdapter) adapter)
                                                        .selectAll();
                                                adapter.notifyDataSetChanged();
                                                multiDeleteButton.setEnabled(true);
                                            }
                                            if (mList.get(position).equals(getString(
                                                    R.string.contacts_select_all_contacts_cancel))) {
                                                ((ContactForMessageListAdapter) adapter)
                                                        .clearSections();
                                                adapter.notifyDataSetChanged();
                                                // initAdapterSelection();
                                                multiDeleteButton.setText(getString(R.string.ok));
                                                multiDeleteButton.setEnabled(false);
                                            }
                                        }
                                        dismissPopupMenu();
                                    }
                                });
                    }
                });

        findViewById(R.id.contacts_multi_delete_buttons).setVisibility(View.VISIBLE);
        // 删除按钮
        Button delete = (Button) findViewById(R.id.contacts_multi_delete);
        delete.setOnClickListener(action);
        delete.setEnabled(false);
        delete.setText(R.string.ok);
        // 取消删除
        findViewById(R.id.contacts_multi_cancel).setOnClickListener(action);

    }

    @Override
    protected void initUI() {
        super.initUI();
        contactsListView.setOnItemClickListener(null);
    }

    @Override
    protected void init() {
        super.init();
        initAdapterSelection();
    }

    @Override
    protected ContactsListAction createAction() {
        return new ContactForMessageListAction(this);
    }

    @Override
    protected ContactsBaseAdapter createAdapter() {
        return new ContactForMessageListAdapter(this);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        switch (what) {
            case FLASH_DATA:
                super.notifyObserver(what, obj);
        }
    }

    /**
     * 结束选择
     */
    public void finishSelection() {
        ArrayList<Account> accounts = getSelectAccounts();
        Intent intent = getIntent();
        Intent result = new Intent(this, ChatActivity.class);
        result.putExtra(ChatActivity.ACCOUNTS, accounts);
        result.putExtra(ChatActivity.CONTENT, intent.getStringExtra(ChatActivity.CONTENT));
        startActivity(result);
        adapter.clearSections();
        Button multiDeleteButton = (Button) findViewById(R.id.contacts_multi_delete);
        multiDeleteButton.setText(R.string.ok);
    }

    public ArrayList<Account> getSelectAccounts() {
        Object[] selections = adapter.getSections();
        ArrayList<Account> accounts = new ArrayList<Account>();
        for (Object selection : selections) {
            accounts.add((Account) selection);
        }
        return accounts;
    }

    @SuppressWarnings("unchecked")
    private void initAdapterSelection() {
        ArrayList<Account> accounts = (ArrayList<Account>) getIntent().getSerializableExtra(
                ChatActivity.ACCOUNTS);
        SLog.d(TAG, "初始化帐号数量:" + accounts);
        if (null != accounts)
            adapter.addSelectAccount(accounts);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

}
