
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactBlackListAdapter;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.adapter.ContactsBaseAdapter;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.adapter.PopupMenuAdapter;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.ContactsBlackListAction;
import android.skymobi.messenger.ui.action.ContactsListAction;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

/**
 * 类说明： 消息会话列表
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class ContactBlackListActivity extends ContactsListActivity {

    private static final int MENU_CLEAR = 30; // 清空菜单
    public static final int MENU_CLEAR_END = 256;

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.settings_blacklist_mgr);
        ArrayList<String> mList = new ArrayList<String>();
        mList.add(getString(R.string.contacts_black_list_clear_menu));
        final PopupMenuAdapter rightMenuAapter = new PopupMenuAdapter(this, mList);
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_option,
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRightPopupMenu(ContactBlackListActivity.this, rightMenuAapter,
                                findViewById(R.id.topbar_imageButton_rightII),
                                new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                                        if (null != mService.getContactsModule()
                                                .getContactsBlackList()
                                                && mService.getContactsModule()
                                                        .getContactsBlackList().size() > 0) {
                                            showDialog(MENU_CLEAR);
                                        } else {
                                            // TODO 提示
                                            showToast(R.string.settings_blacklist_no_list);
                                        }
                                        dismissPopupMenu();
                                    }
                                });
                    }
                });
    }

    @Override
    protected ContactsListAction createAction() {
        return new ContactsBlackListAction(this);
    }

    @Override
    protected ContactsBaseAdapter createAdapter() {
        return new ContactBlackListAdapter(this);
    }

    protected ArrayList<Contact> getList() {
        return mService.getContactsModule().getContactsBlackList();
    }

    private final Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT_BLACKLIST:
                    Contact contact = (Contact) msg.obj;
                    if (contact != null) {
                        adapter.removeItemById(contact.getId());
                        adapter.notifyDataSetChanged();
                    } else {
                        showToast(R.string.contacts_black_list_failed);
                    }
                    removeDialog(ContactsDetailActivity.DELETING);
                    break;
                case MENU_CLEAR_END:
                    int result = (Integer) msg.obj;
                    if (result != ContactsNetModule.NET_SUCCESS) {
                        showToast(R.string.contacts_black_list_failed);
                    }
                    ContactListCache.getInstance().recreateItems(
                            mService.getContactsModule().getContactInfoForList());
                    initAdapterItems();
                    removeDialog(MENU_CLEAR_END);
                    break;
                case FLASH_DATA:
                    adapter.setItems((ArrayList<ContactsListItem>) msg.obj);
                    adapter.notifyDataSetChanged();
                    break;
                case CoreServiceMSG.MSG_CONTACTS_BLACKLIST_REMOVE:
                    removeDialog(ContactsDetailActivity.UNBLACKING);
                    Contact contact1 = (Contact) msg.obj;
                    if (contact1 != null && contact1.getAction() == ContactsNetModule.NET_SUCCESS) {
                        adapter.removeItemById(contact1.getId());
                        adapter.notifyDataSetChanged();
                        removeDialog(ContactsDetailActivity.UNBLACKING);
                        ContactListCache.getInstance().recreateItems(
                                mService.getContactsModule().getContactInfoForList());
                        showToast(R.string.contacts_list_blacklist_unblack_sucess);
                    } else {
                        showToast(R.string.contacts_list_blacklist_unblack_failed);
                    }
                    break;
            }
        }
    };

    @Override
    public void notifyObserver(int what, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, obj));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        switch (id) {
            case MENU_CLEAR:
                dialogBuilder.setMessage(R.string.contacts_black_list_clear);
                dialogBuilder.setCancelable(false);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showDialog(MENU_CLEAR_END);
                        mService.getContactsModule().clearBlackList();
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
            case MENU_CLEAR_END:
                ProgressDialog clearBlackListDialog = new ProgressDialog(this);
                clearBlackListDialog.setTitle(R.string.tip);
                clearBlackListDialog.setMessage(getString(R.string.message_list_dialog_wait));
                clearBlackListDialog.setIndeterminate(true);
                return clearBlackListDialog;
            case ContactsDetailActivity.UNBLACKING:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_list_blacklist_remove_waitting), true);
            case ContactsDetailActivity.DELETING:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_list_del_waiting), true);
        }
        dialog = dialogBuilder.create();
        return dialog;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        int position = ((AdapterContextMenuInfo) menuInfo).position - mHeadViewCount;
        if (position >= 0) {
            menu.clear();
            menu.setHeaderTitle(adapter.getItem(position).getDisplayname());
            menu.add(ITEM_FIRST, ITEM_FIRST, ITEM_FIRST, R.string.contacts_black_list_remove);
            menu.add(ITEM_SECOND, ITEM_SECOND, ITEM_SECOND, R.string.contacts_black_list_delete);
        } else {
            super.onCreateContextMenu(menu, v, menuInfo);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position - mHeadViewCount;
        if (position >= 0) {
            long id = adapter.getItem(info.position).getId();
            switch (item.getItemId()) {
                case ITEM_FIRST:
                    showDialog(ContactsDetailActivity.UNBLACKING);
                    mService.getContactsModule().removeContactFromBlackList(id);
                    break;
                case ITEM_SECOND:
                    showDialog(ContactsDetailActivity.DELETING);
                    mService.getContactsModule().deleteFromBlackList(id);
                    break;
            }
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        // menu.add(0, MENU_CLEAR, 0,
        // R.string.contacts_black_list_clear_menu).setIcon(
        // R.drawable.menu_clear_all);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CLEAR:
                showDialog(MENU_CLEAR);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        initAdapterItems();
        super.onResume();
    }

    /**
     * 获取联系人
     * 
     * @return
     */
    protected void initAdapterItems() {
        ArrayList<Contact> contacts = getList();
        ArrayList<ContactsListItem> children = new ArrayList<ContactsListItem>();
        // "#" 组
        ArrayList<ContactsListItem> sharpList = new ArrayList<ContactsListItem>();
        char lastLetter = 32;
        String groupName = "#";
        boolean hasSharpGroup = false;
        for (int i = 0; i < contacts.size(); i++) {
            String pinyin = contacts.get(i).getPinyin();
            if (pinyin != null && pinyin.length() > 0) {
                char current = pinyin.charAt(0);
                if (current >= 'a' && current <= 'z') {
                    if (lastLetter != current) {
                        lastLetter = current;
                        ContactsListItem group = new ContactsListItem();
                        group.setGroup(true);
                        groupName = String.valueOf(lastLetter).toUpperCase();
                        group.setGroupName(groupName);
                        children.add(group);
                    }
                } else {
                    groupName = "#";
                    if (!hasSharpGroup) {
                        hasSharpGroup = true;
                        ContactsListItem group = new ContactsListItem();
                        group.setGroup(true);
                        group.setGroupName(groupName);
                        sharpList.add(group);
                    }
                }
            } else {
                groupName = "#";
                if (!hasSharpGroup) {
                    hasSharpGroup = true;
                    ContactsListItem group = new ContactsListItem();
                    group.setGroup(true);
                    group.setGroupName(groupName);
                    sharpList.add(group);
                }
            }
            ContactsListItem item = new ContactsListItem();
            item.setId(contacts.get(i).getId());
            item.setPinyin(pinyin);
            item.setPhotoId(contacts.get(i).getPhotoId());
            item.setUserType(contacts.get(i).getUserType());
            String displayName = contacts.get(i).getDisplayname();
            String nickName = contacts.get(i).getNickName();
            String phone = contacts.get(i).getPhone();
            if (!TextUtils.isEmpty(displayName)) {
                item.setDisplayname(displayName);
            } else if (!TextUtils.isEmpty(nickName)) {
                item.setDisplayname(nickName);
            } else {
                item.setDisplayname(phone);
            }
            String signature = contacts.get(i).getSignature();
            if (!TextUtils.isEmpty(signature)) {
                item.setSignature(signature);
            } else if (!TextUtils.isEmpty(phone)) {
                item.setSignature(phone);
            } else {
                item.setSignature("");
            }
            item.setAccounts(contacts.get(i).getAccounts());
            item.setGroupName(groupName);
            if ("#".equals(item.getGroupName())) {
                sharpList.add(item);
            } else {
                children.add(item);
            }
        }
        children.addAll(sharpList);
        handler.sendMessage(handler.obtainMessage(FLASH_DATA, children));
    }

    @Override
    protected void initAdapterAndData() {
        adapter = createAdapter();
        contactsListView.setAdapter(adapter);
        contactsListView.removeHeaderView(mPinnedSearch);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
