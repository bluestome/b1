
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactMutilDeleteAdapter;
import android.skymobi.messenger.adapter.ContactsBaseAdapter;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.ui.action.ContactMutilDeleteAction;
import android.skymobi.messenger.ui.action.ContactsListAction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

/**
 * 类说明： 消息会话列表
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class ContactMutilDeleteListActivity extends ContactsListActivity {

    public static final int MULTI_DELETE_CONTACT_CONFIRM = 257;
    public static final int MULTI_DELETE_CONTACT = 254;
    public static final int MUL_DELETE_CONTACT_END = 255;

    private ProgressDialog multiDeleteDialog = null;
    private int deleteCount = 1; // 删除个数

    /**
     * 初始化UI
     */
    @Override
    public void initTopBar() {

        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.contacts_multi_del_title);

        findViewById(R.id.contacts_multi_delete_buttons).setVisibility(View.VISIBLE);
        // 删除按钮
        findViewById(R.id.contacts_multi_delete).setOnClickListener(action);
        findViewById(R.id.contacts_multi_delete).setEnabled(false);
        // 取消按钮，作为全选/全不选使用
        Button selectAllBtn = (Button) findViewById(R.id.contacts_multi_cancel);
        selectAllBtn.setText(R.string.contacts_select_all_contacts);
        selectAllBtn.setOnClickListener(action);
    }

    @Override
    protected void initUI() {
        super.initUI();
        contactsListView.setOnItemClickListener(null);
    }

    @Override
    protected void initAdapterAndData() {
        adapter = createAdapter();
        contactsListView.setAdapter(adapter);
        // removeHeaderView 需要在setAdapter之后
        contactsListView.removeHeaderView(mPinnedSearch);
    }

    @Override
    protected ContactsBaseAdapter createAdapter() {
        ContactMutilDeleteAdapter adapter = new ContactMutilDeleteAdapter(this);
        if (action != null) {
            ((ContactMutilDeleteAction) action).setAdapter(adapter);
        }
        return adapter;
    }

    @Override
    protected ContactsListAction createAction() {
        return new ContactMutilDeleteAction(this);
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MULTI_DELETE_CONTACT:
                    adapter.removeItems((ArrayList<Contact>) msg.obj);
                    adapter.notifyDataSetChanged();
                    multiDeleteDialog.setProgress(deleteCount += 10);
                    Button multiDeleteButton = (Button) findViewById(R.id.contacts_multi_delete);
                    multiDeleteButton.setText(R.string.contacts_detail_delete);
                    break;
                case MUL_DELETE_CONTACT_END:
                    removeDialog(MULTI_DELETE_CONTACT);
                    showToast(R.string.contacts_list_del_sucess);
                    contactsListView.invalidate();
                    contactsListView.requestLayout();
                    ((ContactMutilDeleteAdapter) adapter).changeButtonText();
                    finish();
                    break;
            }
        }
    };

    @Override
    public void notifyObserver(int what, Object obj) {
        switch (what) {
            case FLASH_DATA:
                super.notifyObserver(what, obj);
            default:
                handler.sendMessage(handler.obtainMessage(what, obj));
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        switch (id) {
            case MULTI_DELETE_CONTACT_CONFIRM:
                dialogBuilder.setMessage(R.string.contacts_multi_del_local_confirm);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mService.getContactsModule().deleteContacts(adapter.getSectionsList());
                        showDialog(MULTI_DELETE_CONTACT);
                        adapter.clearSections();
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
            case MULTI_DELETE_CONTACT:
                multiDeleteDialog = new ProgressDialog(this);
                multiDeleteDialog.setTitle(R.string.tip);
                multiDeleteDialog.setMessage(getString(R.string.message_list_dialog_wait));
                multiDeleteDialog.setIndeterminate(false);
                multiDeleteDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                multiDeleteDialog.setCancelable(false);
                multiDeleteDialog.setMax(adapter.getSectionsList().size());
                multiDeleteDialog.setProgress(0);
                return multiDeleteDialog;
        }
        dialog = dialogBuilder.create();
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case MULTI_DELETE_CONTACT_CONFIRM:
                AlertDialog alertDialog = (AlertDialog) dialog;
                if (isContainLocal(adapter.getSectionsList())) {
                    alertDialog.setMessage(getText(R.string.contacts_multi_del_local_confirm));
                } else {
                    alertDialog.setMessage(getText(R.string.contacts_multi_del_confirm));
                }
                break;
        }
        super.onPrepareDialog(id, dialog);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.clear();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean isContainLocal(ArrayList<Contact> contacts) {
        for (Contact contact : contacts) {
            if (contact.getLocalContactId() > 0)
                return true;
        }
        return false;
    }
}
