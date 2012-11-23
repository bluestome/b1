
package android.skymobi.messenger.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.adapter.ContactsBaseAdapter;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.adapter.ContactsListAdapter;
import android.skymobi.messenger.adapter.PopupMenuAdapter;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.ContactsListAction;
import android.skymobi.messenger.widget.PinnedHeaderListView;
import android.skymobi.messenger.widget.SideBar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 类说明： 消息会话列表
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class ContactsListActivity extends TopActivity {

    public static final int TOUCH_MOVE_DOWN = 251; // 字母顺序表移动和按下
    public static final int TOUCH_UP = 252; // 字母表弹起

    protected static final int MENU_MULTI_DELETE = 10; // 删除菜单
    protected static final int MENU_QUIT = 20; // 退出菜单

    protected static final int ITEM_FIRST = 1;
    protected static final int ITEM_SECOND = 2;
    private static final int DIALOG_DELETE_CONFIRM = 0x10;

    public static final int FLASH_DATA = 29;
    public static final int FLASH_ONLINE_DATA = 30;

    protected ListView contactsListView = null;
    protected ContactsBaseAdapter adapter;
    private TextView mDialogText = null;

    protected ContactsListAction action = null;

    public static final String ACCOUNTIDS = "AccountIDs";

    protected static final int EACHLOOP = 5;

    protected long mCurContactID = 0;
    protected int mHeadViewCount = 0;
    protected View mPinnedSearch;
    protected View mPinnedHeader;

    private boolean isOnlineType = false;
    private TextView mOnlineTypeView;
    private EditText mSearchText;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_list);
        contactsListView = (ListView) findViewById(R.id.contacts_listview);
        try {
            init();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
    }

    /**
     * 初始化
     */
    protected void init() {
        initUI();

        initAdapterAndData();
    }

    /**
     * 初始化UI
     */
    protected void initUI() {
        syncDialog();
        action = createAction();
        if (contactsListView instanceof PinnedHeaderListView) {
            PinnedHeaderListView pinnedHeaderList = (PinnedHeaderListView) contactsListView;
            mPinnedHeader = LayoutInflater.from(this).inflate(
                    R.layout.contacts_list_item_group, contactsListView, false);

            mPinnedSearch = LayoutInflater.from(this).inflate(
                    R.layout.contacts_list_item_search, contactsListView, false);
            pinnedHeaderList.addHeaderView(mPinnedSearch);
            pinnedHeaderList.setPinnedHeaderView(mPinnedHeader);

            mHeadViewCount = pinnedHeaderList.getHeaderViewsCount();

            action.setPinnedHeader(mPinnedHeader);
        }

        action.setContactsListView(contactsListView);
        contactsListView.setOnItemClickListener(action);
        contactsListView.setOnScrollListener(action);
        registerForContextMenu(contactsListView);
        initTopBar();

        mSearchText = (EditText) mPinnedSearch.findViewById(R.id.contacts_list_item_search);
        SideBar indexBar = (SideBar) findViewById(R.id.sideBar);
        indexBar.setListView(contactsListView);
    }

    @Override
    public void initTopBar() {
        mOnlineTypeView = (TextView) getTopBarViewNoDivider(TOPBAR_RELATIVELAYOUT_LEFTI, 0, action,
                R.id.topbat_textview_leftI);
        mOnlineTypeView.setText(R.string.contacts_list_all);
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTI, R.drawable.contacts_list_new_btn, action);
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_option, action);
    }

    /**
     * 初始化数据
     */
    protected void initAdapterAndData() {
        adapter = createAdapter();
        contactsListView.setAdapter(adapter);
    }

    /**
     * 创建Action
     * 
     * @return
     */
    protected ContactsListAction createAction() {
        return new ContactsListAction(this);
    }

    /**
     * 创建Adapter
     * 
     * @param children
     * @return
     */
    protected ContactsBaseAdapter createAdapter() {
        return new ContactsListAdapter(this);
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 字母顺序表 touch_move or touch_down
                case TOUCH_MOVE_DOWN:
                    // TextView groupTipRow = (TextView)
                    // findViewById(R.id.contacts_list_group_tip);
                    // groupTipRow.setText(msg.obj.toString());
                    break;
                // 字母顺序表 touch_up
                case TOUCH_UP:
                    contactsListView.setOnScrollListener(action);
                    break;
                // 同步联系人状态完成
                case CoreServiceMSG.MSG_CONTACTS_SYNC_STATUS_END:
                    if (isOnlineType) {
                        refreshOnlineContact();
                    }
                    adapter.notifyDataSetChanged();
                    removeDialog(CoreServiceMSG.MSG_CONTACTS_SYNC_END);
                    break;
                // 同步联系人完成
                case CoreServiceMSG.MSG_CONTACTS_SYNC_END:
                case CoreServiceMSG.MSG_CONTACTS_SYNC_FAILED:
                    adapter.notifyDataSetChanged();
                    removeDialog(CoreServiceMSG.MSG_CONTACTS_SYNC_END);
                    if (msg.what == CoreServiceMSG.MSG_CONTACTS_SYNC_FAILED) {
                        showToast(R.string.contacts_list_sync_failed);
                    }
                    break;
                case CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT:
                    Contact contact = (Contact) msg.obj;
                    if (contact != null) {
                        ContactListCache.getInstance().removeItemById(contact.getId());
                        adapter.removeItemById(contact.getId());
                        adapter.notifyDataSetChanged();
                        // ContactListCache.getInstance().recreateItems(
                        // mService.getContactsModule().getContactInfoForList());
                        ContactListCache.getInstance().removeContactMap(contact);
                        showToast(R.string.contacts_list_del_sucess);
                        removeDialog(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT);
                    } else {
                        showToast(R.string.contacts_list_del_failed);
                    }
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ClOUD_SYNC_END:
                case CoreServiceMSG.MSG_CONTACTS_ONLINE_STATUS:
                    if (isOnlineType) {
                        refreshOnlineContact();
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case FLASH_DATA:
                    flashData(msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSearchText.addTextChangedListener(action);
        mSearchText.clearFocus();
        initContactByType();
        setOnlineType();
        resume();
    }

    protected void resume() {
        mDialogText = (TextView)
                LayoutInflater.from(this).inflate(R.layout.contacts_list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(mDialogText, lp);
        SideBar indexBar = (SideBar) findViewById(R.id.sideBar);
        indexBar.setTextView(mDialogText, handler);
        indexBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = findViewById(R.id.contacts_list_item_search);
                if (null != view) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            pause();
            clearSearchText();
            clearInitSearchSetting();
        } catch (Exception e) {
        }
    }

    protected void pause() {
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(mDialogText);
    }

    // @Override
    // protected void onStop() {
    // super.onStop();
    // clearSearchText();
    // clearInitSearchSetting();
    // }

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
            case CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_list_del_waiting), true);
            case DIALOG_DELETE_CONFIRM: {
                dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
                dialogBuilder.setMessage(R.string.contacts_list_delete_confirm);
                dialogBuilder.setPositiveButton(
                        getResources().getString(R.string.contacts_list_btn_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                deleteContact(mCurContactID);
                            }
                        });
                dialogBuilder.setNegativeButton(
                        getResources().getString(R.string.contacts_list_btn_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }

                        });
            }
                break;
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
            menu.add(R.string.contacts_list_del_tip);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position - mHeadViewCount;
        mCurContactID = adapter.getItem(position).getId();
        // 与本地有关联，需要弹出确认对话框
        if (adapter.getItem(position).getLocalContactId() > 0) {
            showDialog(DIALOG_DELETE_CONFIRM);
        } else {
            deleteContact(mCurContactID);
        }
        return true;
    }

    private void deleteContact(long id) {
        mService.getContactsModule().deleteContactByID(id);
        showDialog(CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_QUIT, 0, R.string.quit).setIcon(R.drawable.menu_quit);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_QUIT:
                showQuitDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void syncDialog() {
        // if (MainApp.getInstance().isSyncContacts() &&
        // CommonPreferences.getSyncContactsCount() < 1) {
        // showDialog(CoreServiceMSG.MSG_CONTACTS_SYNC_END);
        // }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        try {
            switch (requestCode) {
                case ContactsDetailEditActivity.CONTACT_EDIT:
                case ContactsDetailEditActivity.CONTACT_NEW:
                    if (resultCode == Activity.RESULT_OK && data != null && adapter != null) {
                        contactsListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                contactsListView.setSelection(adapter.getPositionById(data
                                        .getLongExtra("data", 0l)).getPosition());
                            }
                        }, 200);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        String searchText = mSearchText.getText().toString();
        if (TextUtils.isEmpty(searchText)) {
            switchToHome();
        } else {
            mSearchText.setText(null);
        }
    }

    public void showPopupMenu() {
        final List<String> mList;
        String[] mPopmenuArray = getResources().getStringArray(R.array.contacts_list_popmenu);
        // mList.add(getString(R.string.message_clear_all_contacts));
        mList = Arrays.asList(mPopmenuArray);

        PopupMenuAdapter rightMenuAapter = new PopupMenuAdapter(this, mList);
        showRightPopupMenu(this, rightMenuAapter, findViewById(R.id.topbar_imageButton_rightII),
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dismissPopupMenu();
                        String type = mList.get(position);
                        if (type.equals(getString(R.string.contacts_list_newcontact))) {
                            Intent intent = new Intent(ContactsListActivity.this,
                                    ContactsDetailEditActivity.class);
                            intent.putExtra(ContactsDetailEditActivity.ACTION_TYPE,
                                    ContactsDetailEditActivity.ACTION_NEW);
                            startActivityForResult(intent,
                                    ContactsDetailEditActivity.CONTACT_NEW);
                        } else {
                            Intent intent = new Intent(ContactsListActivity.this,
                                    ContactMutilDeleteListActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    public void showLeftPopupMenu() {
        final List<String> mList;
        String[] mArray = getResources().getStringArray(R.array.contacts_list_type);
        mList = Arrays.asList(mArray);

        PopupMenuAdapter leftMenuAapter = new PopupMenuAdapter(this, mList);
        showLeftPopupMenu(this, leftMenuAapter, findViewById(R.id.topbar_contact_select),
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dismissPopupMenu();
                        String type = mList.get(position);
                        if (type.equals(getString(R.string.contacts_list_all))) {
                            mOnlineTypeView.setText(R.string.contacts_list_all);
                            if (isOnlineType) {
                                mSearchText.removeTextChangedListener(action);
                                mSearchText.setText(null);
                                action.showPinnedHeader();// 设置组可见
                                ((ContactsListAdapter) adapter).displayAllContact();
                                adapter.notifyDataSetChanged();
                                mSearchText.addTextChangedListener(action);
                            }
                            isOnlineType = false;
                        } else {
                            mOnlineTypeView.setText(R.string.contacts_list_allonline);
                            if (!isOnlineType) { // 当前不是在线状态选项
                                mSearchText.removeTextChangedListener(action);
                                mSearchText.setText(null);
                                action.showPinnedHeader();
                                refreshOnlineContact();
                                adapter.notifyDataSetChanged();
                                mSearchText.addTextChangedListener(action);
                            }
                            // 设置在线类别的数据
                            ContactListCache.getInstance().setOnlineItems(getItems());
                            isOnlineType = true;
                        }

                        setOnlineType();
                    }
                });
    }

    private void initContactByType() {
        String searchText = mSearchText.getText().toString();
        if (TextUtils.isEmpty(searchText)) {
            if (isOnlineType) {
                refreshOnlineContact();
                ContactListCache.getInstance().setOnlineItems(getItems());
            } else {
                adapter.setItems(ContactListCache.getInstance().getListItems());
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setOnlineType() {
        // 设置在线和全部的类别,用于根据类别显示数据
        action.setOnlineType(isOnlineType);
        ContactListCache.getInstance().setSelectOnlineType(isOnlineType);
    }

    private void clearSearchText() {
        mSearchText.removeTextChangedListener(action);
        mSearchText.setText(null);
    }

    private void clearInitSearchSetting() {
        action.showPinnedHeader();
        action.setOnlineType(false);
        ContactListCache.getInstance().setSelectOnlineType(false);
        ContactListCache.getInstance().getOnlineItems().clear();
    }

    private void flashData(Object obj) {
        contactsListView.requestLayout();
        adapter.setItems((ArrayList<ContactsListItem>) obj);
        adapter.notifyDataSetChanged();
    }

    private void refreshOnlineContact() {
        ((ContactsListAdapter) adapter).displayOnlineContact();
    }

    public ArrayList<ContactsListItem> getItems() {
        return ((ContactsListAdapter) adapter).getItems();
    }
}
