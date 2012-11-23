
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.adapter.ContactsDetailDialogAdapter;
import android.skymobi.messenger.adapter.PopupMenuAdapter;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bizunit.contact.InformBU;
import android.skymobi.messenger.network.module.ContactsNetModule;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.ContactsDetailAction;
import android.skymobi.messenger.ui.action.InformAction;
import android.skymobi.messenger.ui.handler.event.EventMsg;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Base64;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 类说明： 消息会话列表
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class ContactsDetailActivity extends TopActivity {
    private final static String TAG = ContactsDetailActivity.class.getSimpleName();

    public final static String CONTACT_ID_FLAG = "CONTACT_ID";
    public final static String CONTACT_FLAG = "CONTACT";
    public final static String CONTACT_DISTANCE = "DISTANCE";
    public static final String CURRENT_CONTACT = "current_contact";
    public static final String CONTACT_TYPE = "contact_type";

    public static final int SHOW = 320;// 刪除

    public static final int DETAIL = 220;// 刪除
    public final static int CALL = 211; // 打電話
    public final static int MESSAGE = 212;// 發短信
    public static final int DELETE = 213;// 刪除
    public static final int VCARD = 214;// 发名片
    public static final int INVITE = 215;// 邀请开通
    public static final int BLACK = 216;// 加黑
    public static final int EDIT = 217;// 編輯
    public static final int UNBLACK = 218;// 編輯
    public static final int FIRST_BLACK = 219;// 首次添加黑名单提示
    public static final int INVITE_NOT_PHONENUMBER = 220; // 被邀请方不是手机号码
    public static final int INVITE_NO_NUMBER = 221; // 被邀请方没有号码

    public static final int DELETING = 301;
    public static final int BLACKING = 302;
    public static final int UNBLACKING = 303;// 編輯

    protected ContactsDetailAction action;
    protected long contactId;
    private String displayName = "";
    protected int skyId = 0;
    protected int distance = -1;

    private ArrayList<Account> accounts = new ArrayList<Account>();

    // 初始化组件
    private View callView = null;
    private View messageView = null;
    private View vcardView = null;
    private View deleteView = null;
    private View blacklistView = null;
    private View inviteView = null;
    private View headView = null;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_detail);
        Intent intent = getIntent();
        distance = intent.getIntExtra(CONTACT_DISTANCE, -1);
        action = createAction();
        initViews();
        init();
        // initTopBar依赖的标识在 init中设置，所以它们顺序不能改变
        initTopBar();
    }

    /**
     * @return
     */
    protected ContactsDetailAction createAction() {
        return new ContactsDetailAction(this);
    }

    /**
     * 初始化
     */
    protected void init() {
        SLog.d(TAG, "init...");
        // 标题
        Intent intent = getIntent();
        contactId = intent.getLongExtra(CONTACT_ID_FLAG, 0);

        Contact contact = mService.getContactsModule().getContactById(contactId);
        if (contact == null) {
            finish();
            return;
        }
        if (contact.isSkyUser()) {
            ArrayList<Account> accounts = contact.getAccounts();
            for (Account account : accounts) {
                if (account.isMain()) {
                    skyId = account.getSkyId();
                    break;
                }
            }
            SLog.d(TAG, "skyid=" + skyId);
        }
        initDetail(contact);
        getDetailShouxinInfo(contact);
    }

    /**
     * 加载基本内容
     * 
     * @param contact
     */
    protected void initDetail(Contact contact) {
        initButtons();
        action.setContact(contact);
        displayName = contact.getDisplayname();
        if (contact.isSkyUser()) {
            // 显示头像区
            findViewById(R.id.contacts_detail_header_layout).setVisibility(View.VISIBLE);
            // 显示加入黑名单
            findViewById(R.id.contacts_detail_blacklist).setVisibility(View.VISIBLE);

            // 显示手信详情区
            findViewById(R.id.contacts_detail_shouxin).setVisibility(View.VISIBLE);
            // 隐藏手机详情区
            findViewById(R.id.contacts_detail_local).setVisibility(View.GONE);
            // 隐藏拨号
            findViewById(R.id.contacts_detail_call_layout).setVisibility(View.GONE);
            // 隐藏邀请开通
            findViewById(R.id.contacts_detail_invite).setVisibility(View.GONE);

            // 设置头像区
            initHeader(contact);

            // 性别
            if (contact.getSex() == ContactsColumns.SEX_FEMALE) {
                ImageView sexView = (ImageView) findViewById(R.id.contacts_detail_sex_image);
                sexView.setImageResource(R.drawable.female);
            }
            // 设置详情
            // 签名
            if (TextUtils.isEmpty(contact.getSignature())) {
                findViewById(R.id.contacts_detail_shouxin_signature).setVisibility(View.GONE);
            } else {
                findViewById(R.id.contacts_detail_shouxin_signature).setVisibility(View.VISIBLE);
                setTextViewValue(R.id.contacts_detail_signature_content, contact.getSignature());
            }

            LinearLayout shouxinNameLayout = ((LinearLayout) findViewById(R.id.contacts_detail_shouxin_name));
            shouxinNameLayout.removeAllViews();
            // 姓名
            if (!TextUtils.isEmpty(contact.getDisplayname())) {
                addChildView(shouxinNameLayout, getString(R.string.contacts_detail_name),
                        contact.getDisplayname().replace("\n", ""));
            }
            // 备注
            if (!TextUtils.isEmpty(contact.getNote())) {
                addChildView(shouxinNameLayout, getString(R.string.contacts_detail_note),
                        contact.getNote());
            }
            int childCount = shouxinNameLayout.getChildCount();
            if (childCount == 0) {
                shouxinNameLayout.setVisibility(View.GONE);
            } else {
                shouxinNameLayout.setVisibility(View.VISIBLE);
            }

            LinearLayout shouxinAccountLayout = ((LinearLayout) findViewById(R.id.contacts_detail_shouxin_account));
            shouxinAccountLayout.removeAllViews();
            this.accounts = contact.getAccounts();
            // 号码
            List<Account> accounts = contact.getAccounts();
            if (accounts.size() > 0) {
                boolean hasSetOnline = false;
                String skyAccountName = "";
                String skyNickName = "";
                for (Account account : accounts) {
                    boolean status = MainApp.i().getUserOnlineStatus(account.getSkyId());
                    if (!hasSetOnline && account.getSkyId() > 0) {
                        skyAccountName = StringUtil.convertNull(account.getSkyAccount());
                        skyNickName = StringUtil.convertNull(account.getNickName());

                        if (status && contact.getBlackList() == ContactsColumns.BLACK_LIST_NO) { // 不是黑名单
                            hasSetOnline = true;
                            skyAccountName = getString(
                                    R.string.contacts_list_sub_item_number, skyAccountName);
                        }

                        if (account.isMain()) {
                            if (TextUtils.isEmpty(skyNickName)) {
                                skyNickName = skyAccountName;
                            }
                        }
                    }

                    // 增加账号
                    if (!TextUtils.isEmpty(account.getPhone())) {
                        addChildView(shouxinAccountLayout,
                                getString(R.string.contacts_detail_phone), account.getPhone());
                        // 如果有手机号码,显示拨号
                        findViewById(R.id.contacts_detail_call_layout).setVisibility(
                                View.VISIBLE);
                    } else {
                        if (account.getSkyId() > 0 && !TextUtils.isEmpty(account.getSkyAccount())) {
                            addChildView(shouxinAccountLayout,
                                    getString(R.string.contacts_detail_shouxin_number),
                                    account.getSkyAccount());
                        }
                    }
                }
                View view = findViewById(R.id.contact_header_account);
                if (TextUtils.isEmpty(skyNickName.trim()) && TextUtils.isEmpty(skyAccountName)) {
                    view.setVisibility(View.INVISIBLE);
                    shouxinAccountLayout.setVisibility(View.INVISIBLE);
                } else {
                    setTextViewValue(R.id.contacts_detail_nick_name, skyNickName.trim());
                    setTextViewValue(
                            R.id.contacts_detail_name,
                            getString(R.string.contacts_detail_shouxin_account,
                                    skyAccountName));

                    view.setVisibility(View.VISIBLE);
                    shouxinAccountLayout.setVisibility(View.VISIBLE);
                }
            } else {
                shouxinAccountLayout.setVisibility(View.GONE);
            }

            // 生日等信息区
            LinearLayout shouxinBirthdayLayout = ((LinearLayout) findViewById(R.id.contacts_detail_shouxin_birthday));
            shouxinBirthdayLayout.removeAllViews();
            // 生日
            if (contact.getBirthday() != 0) {
                addChildView(shouxinBirthdayLayout, getString(R.string.contacts_detail_birthday),
                        DateUtil.getFormatDate(contact.getBirthday()));
            }
            // 暂时屏蔽地区显示项
            // // 地区
            // if (!TextUtils.isEmpty(contact.getHometown())) {
            // addChildView(shouxinBirthdayLayout,
            // getString(R.string.contacts_detail_hometown),
            // contact.getHometown());
            // }
            // 学校
            if (!TextUtils.isEmpty(contact.getSchool())) {
                addChildView(shouxinBirthdayLayout, getString(R.string.contacts_detail_school),
                        contact.getSchool());
            }
            // 单位
            if (!TextUtils.isEmpty(contact.getOrganization())) {
                addChildView(shouxinBirthdayLayout,
                        getString(R.string.contacts_detail_corporation), contact.getOrganization());
            }
            int childCount2 = shouxinBirthdayLayout.getChildCount();
            if (childCount2 == 0) {
                shouxinBirthdayLayout.setVisibility(View.GONE);
            } else {
                shouxinBirthdayLayout.setVisibility(View.VISIBLE);
            }
        } else {
            // 显示手机详情区
            findViewById(R.id.contacts_detail_local).setVisibility(View.VISIBLE);
            // 显示拨号
            findViewById(R.id.contacts_detail_call_layout).setVisibility(View.VISIBLE);
            // 隐藏头像区
            findViewById(R.id.contacts_detail_header_layout).setVisibility(View.GONE);
            // 隐藏加入黑名单
            findViewById(R.id.contacts_detail_blacklist).setVisibility(View.GONE);
            // 隐藏手信详情区
            findViewById(R.id.contacts_detail_shouxin).setVisibility(View.GONE);

            // 设置联系人详情
            LinearLayout localDetailLayout = (LinearLayout) findViewById(R.id.contacts_detail_local);
            localDetailLayout.removeAllViews();
            // 姓名
            View displayNameView = LayoutInflater.from(this).inflate(R.layout.contacts_detail_item,
                    null);
            setTextViewValue(displayNameView, R.id.contacts_detail_title,
                    getString(R.string.contacts_detail_name));
            if (contact.getDisplayname() != null) {
                String displayname = contact.getDisplayname().replace("\n", "");
                setTextViewValue(displayNameView, R.id.contacts_detail_content, displayname);
            }
            displayNameView.findViewById(R.id.contacts_detail_third).setVisibility(View.VISIBLE);
            localDetailLayout.addView(displayNameView);

            accounts = contact.getAccounts();
            // 电话
            if (contact.getAccounts().size() != 0) {

                boolean dispalyInvite = true;
                for (Account account : contact.getAccounts()) {
                    if (null != account.getPhone() && dispalyInvite) {
                        dispalyInvite = false;
                        // 显示邀请开通
                        findViewById(R.id.contacts_detail_invite).setVisibility(View.VISIBLE);
                    }
                    String phone = account.getPhone();
                    if (!TextUtils.isEmpty(phone))
                        addChildView(localDetailLayout, getString(R.string.contacts_detail_phone),
                                phone);
                }
            }
            // 备注
            if (!TextUtils.isEmpty(contact.getNote())) {
                addChildView(localDetailLayout, getString(R.string.contacts_detail_note),
                        contact.getNote());
            }
        }
        findViewById(R.id.contacts_detail_scrollview).setVisibility(View.VISIBLE);
    }

    protected void initViews() {
        // 初始化组件
        callView = findViewById(R.id.contacts_detail_call);
        messageView = findViewById(R.id.contacts_detail_send_message);
        vcardView = findViewById(R.id.contacts_detail_send_vcard);
        deleteView = findViewById(R.id.contacts_detail_delete);
        blacklistView = findViewById(R.id.contacts_detail_blacklist);
        inviteView = findViewById(R.id.contacts_detail_invite);
        headView = findViewById(R.id.contacts_detail_head);
    }

    protected void initButtons() {
        callView.setOnClickListener(action);
        callView.setOnTouchListener(action);
        messageView.setOnClickListener(action);
        messageView.setOnTouchListener(action);
        vcardView.setOnClickListener(action);
        deleteView.setOnClickListener(action);
        blacklistView.setOnClickListener(action);
        inviteView.setOnClickListener(action);
        headView.setOnClickListener(action);
    }

    /**
     * 添加手信子VIew
     * 
     * @param localDetailLayout
     * @param title
     * @param content
     */
    private void addChildView(LinearLayout localDetailLayout, String title, String content) {
        addChildView(localDetailLayout, R.layout.contacts_detail_item, title, content, "");
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
            String content, String... third) {
        if (localDetailLayout.getChildCount() != 0) {
            View sepatator = LayoutInflater.from(this).inflate(
                    R.layout.contacts_detail_item_separator,
                    null);
            localDetailLayout.addView(sepatator);
        }
        View view = LayoutInflater.from(this).inflate(layoutResID, null);
        setTextViewValue(view, R.id.contacts_detail_title, title);
        setTextViewValue(view, R.id.contacts_detail_content, content);
        if (third != null && third.length > 0 && !TextUtils.isEmpty(third[0])) {
            view.findViewById(R.id.contacts_detail_third).setVisibility(View.VISIBLE);
            setTextViewValue(view, R.id.contacts_detail_third, third[0]);
        } else {
            view.findViewById(R.id.contacts_detail_third).setVisibility(View.GONE);
        }
        localDetailLayout.addView(view);
    }

    /**
     * 加载头像
     * 
     * @param contact
     */
    private void initHeader(Contact contact) {
        ImageView imageView = (ImageView) findViewById(R.id.contacts_detail_head);
        HeaderCache.getInstance().getHeader(contact.getPhotoId(), contact.getDisplayname(),
                imageView);
    }

    protected final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case CoreServiceMSG.MSG_CONTACTS_DELETE_CONTACT:
                        removeDialog(DELETING);
                        Contact contact = (Contact) msg.obj;
                        if (contact == null) {
                            showToast(R.string.contacts_list_del_failed);
                        } else {
                            long id = contact.getId();
                            ContactListCache.getInstance().removeItemById(id);
                            ContactListCache.getInstance().removeContactMap(contact);
                            showToast(R.string.contacts_list_del_sucess);
                            Intent data = new Intent();
                            data.putExtra(CURRENT_CONTACT, contact);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                        break;
                    case CoreServiceMSG.MSG_CONTACTS_BLACKLIST_REMOVE:
                        removeDialog(UNBLACKING);
                        Contact contact1 = (Contact) msg.obj;
                        int resultCode = ContactsNetModule.NET_FAILED;
                        if (contact1 != null) {
                            resultCode = contact1.getAction();
                        }
                        onBlackListFinish(UNBLACK, resultCode);
                        break;
                    case CoreServiceMSG.MSG_CONTACTS_BLACKLIST_ADD:
                        removeDialog(BLACKING);
                        onBlackListFinish(BLACK, (Integer) msg.obj);
                        break;
                    case CoreServiceMSG.MSG_CONTACTS_DETAIL_SUCCESS:
                        Contact respContact = (Contact) msg.obj;
                        // 保证返回的联系人信息和请求的一致
                        if (null != respContact && contactId == respContact.getId()) {
                            initDetail(respContact);
                            ContactListCache.getInstance().resetContactItem(
                                    respContact);
                        }
                        break;
                    // 举报发送失败
                    case EventMsg.EVENT_INFORM_FAILED:
                        // showToast(R.string.inform_failed);
                        ToastTool.showShort(ContactsDetailActivity.this, R.string.inform_failed);
                        break;
                    // 举报发送成功
                    case EventMsg.EVENT_INFORM_SUCCESS:
                        ToastTool.showShort(ContactsDetailActivity.this, R.string.inform_thanks);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void notifyObserver(int what, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, obj));
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        AlertDialog a = (AlertDialog) dialog;
        final List<Account> adapterData = new ArrayList<Account>();
        switch (id) {
            case VCARD:
                adapterData.addAll(createAccountsForAdapter());
                ContactsDetailDialogAdapter adapterVcard = new ContactsDetailDialogAdapter(
                        adapterData, this);
                a.getListView().setAdapter(adapterVcard);
                break;
            case CALL:
                final ArrayList<Account> tmp = new ArrayList<Account>();
                for (Account account : accounts) {
                    if (null != account.getPhone() && !"".equals(account.getPhone())) {
                        account.setData1(getString(R.string.contacts_detail_phone));
                        tmp.add(account);
                    }
                }
                ContactsDetailDialogAdapter adapter = new ContactsDetailDialogAdapter(tmp,
                        this);
                a.getListView().setAdapter(adapter);
                break;
            case MESSAGE:
            case INVITE:
                adapterData.addAll(createAccountsForAdapter());
                ContactsDetailDialogAdapter adapterMessage = new ContactsDetailDialogAdapter(
                        adapterData, this);
                a.getListView().setAdapter(adapterMessage);
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        final List<Account> adapterData = new ArrayList<Account>();
        final int tid = id;
        switch (id) {
            case VCARD:
                dialogBuilder.setTitle(displayName);
                adapterData.addAll(createAccountsForAdapter());
                ContactsDetailDialogAdapter adapterVcard = new ContactsDetailDialogAdapter(
                        adapterData, this);
                dialogBuilder.setSingleChoiceItems(adapterVcard, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent searchVCardIntent = new Intent(ContactsDetailActivity.this,
                                        VcardListActivity.class);
                                // searchVCardIntent.putExtra("account",
                                // accounts.get(which));
                                AlertDialog a = (AlertDialog) dialog;
                                ContactsDetailDialogAdapter adapter = (ContactsDetailDialogAdapter) a
                                        .getListView().getAdapter();
                                searchVCardIntent.putExtra("account",
                                        getClickAccout(adapter.getItem(which)));
                                startActivity(searchVCardIntent);
                                dialog.cancel();
                            }
                        });
                break;
            case CALL:
                dialogBuilder.setTitle(displayName);
                // 修改根据电话号码非空情况下，才将该电话添加到显示的列表中
                final ArrayList<Account> tmp = new ArrayList<Account>();
                for (Account account : accounts) {
                    if (null != account.getPhone() && !"".equals(account.getPhone())) {
                        account.setData1(getString(R.string.contacts_detail_phone));
                        tmp.add(account);
                    }
                }
                ContactsDetailDialogAdapter adapter = new ContactsDetailDialogAdapter(tmp,
                        this);
                dialogBuilder.setSingleChoiceItems(adapter, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 修改从adapterData列表中获取which位置的值异常的情况,应该从tmp列表中获取号码
                                Uri uri = Uri.parse("tel:" + tmp.get(which).getPhone());
                                Intent it = new Intent(Intent.ACTION_CALL, uri);
                                startActivity(it);
                                dialog.cancel();
                            }
                        });
                break;
            case MESSAGE:
            case INVITE:
                dialogBuilder.setTitle(displayName);
                adapterData.addAll(createAccountsForAdapter());
                ContactsDetailDialogAdapter adapterMessage = new ContactsDetailDialogAdapter(
                        adapterData, this);
                dialogBuilder.setSingleChoiceItems(adapterMessage, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ContactsDetailActivity.this,
                                        ChatActivity.class);
                                AlertDialog a = (AlertDialog) dialog;
                                ArrayList<Account> accountList = new ArrayList<Account>();
                                // accountList.add(accounts.get(which));
                                // accountList.add(getClickAccout(adapterData.get(which)));
                                ContactsDetailDialogAdapter adapter = (ContactsDetailDialogAdapter) a
                                        .getListView().getAdapter();
                                accountList.add(getClickAccout(adapter
                                        .getItem(which)));
                                intent.putExtra(ChatActivity.ACCOUNTS, accountList);
                                SLog.d(TAG, "\tzhang 邀请对话框中的tid=" + tid + ",INVITE=" + INVITE);
                                if (tid == INVITE) {
                                    // 获取被邀请方的手机号码
                                    String destPhone = accountList.get(0).getPhone() == null ? ""
                                            : accountList.get(0).getPhone();
                                    String phones = SettingsPreferences.getMobile() == null ? ""
                                            : SettingsPreferences.getMobile();
                                    phones = phones + "," + destPhone;
                                    byte[] eb = AndroidSysUtils.inviteEncode(phones);
                                    String selfPhone = "";
                                    if (null != eb) {
                                        selfPhone = Base64.encode(eb);
                                    }
                                    MainApp.i().setInviteEntrance(Constants.INVITE_ENTRANCE_UNKNOW);
                                    MainApp.i().setInviteEntrance(
                                            Constants.INVITE_ENTRANCE_CONTACTS);
                                    String inviteText = getInviteSting();
                                    intent.putExtra(ChatActivity.CONTENT, inviteText + selfPhone);
                                    startActivity(intent);
                                }
                                startActivity(intent);
                                dialog.cancel();
                            }
                        });
                break;
            case DELETE:
                Contact contact = mService.getContactsModule().getContactById(contactId);
                if (contact.getLocalContactId() > 0) {
                    dialogBuilder.setMessage(R.string.contacts_list_delete_confirm);
                } else {
                    dialogBuilder.setMessage(R.string.contacts_detail_delete_tip);
                }
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDialog(DELETING);
                        mService.getContactsModule().deleteContactByID(contactId);
                        dialog.cancel();
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
            case DELETING:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_list_del_waiting));
            case BLACK:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setMessage(R.string.contacts_detail_blacklist_tip);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDialog(BLACKING);
                        mService.getContactsModule().addContactToBlackList(contactId);
                        dialog.cancel();
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
            case BLACKING:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_list_blacklist_waitting));
            case UNBLACKING:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_list_blacklist_remove_waitting));
            case UNBLACK:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setMessage(R.string.contacts_detail_blacklist_remove_tip);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDialog(UNBLACKING);
                        mService.getContactsModule().removeContactFromBlackList(contactId);
                        dialog.cancel();
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
            case FIRST_BLACK:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setMessage(R.string.contacts_black_list_firstadd_tip);
                dialogBuilder.setPositiveButton(R.string.iknow,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                showDialog(ContactsDetailActivity.BLACK);
                            }
                        });

                break;
        }
        dialog = dialogBuilder.create();
        return dialog;
    }

    /**
     * 显示在线状态
     * 
     * @return
     */
    protected ArrayList<Account> createAccountsForAdapter() {
        ArrayList<Account> result = new ArrayList<Account>();
        for (Account account : accounts) {
            Account temp = new Account();
            // 判断当前帐号是否有SKYID
            if (account.getSkyId() > 0) {
                temp.setId(account.getId());
                // 提示语为"帐号"
                temp.setData1(getString(R.string.contacts_detail_shouxin_number));
                Boolean status = false;
                try {
                    status = MainApp.i().getUserOnlineStatus(account.getSkyId());
                } catch (Exception e) {
                    status = false;
                } finally {
                    if (null == status) {
                        status = false;
                    }
                }
                if (status) {
                    if (!TextUtils.isEmpty(account.getPhone())) {
                        temp.setData1(getString(R.string.contacts_detail_phone));
                        temp.setPhone(getString(R.string.contacts_list_sub_item_number,
                                account.getPhone()));
                    } else {
                        temp.setPhone(getString(R.string.contacts_list_sub_item_number,
                                account.getSkyAccount()));
                    }
                } else {
                    if (!TextUtils.isEmpty(account.getPhone())) {
                        temp.setData1(getString(R.string.contacts_detail_phone));
                        temp.setPhone(account.getPhone());
                    } else {
                        temp.setPhone(account.getSkyAccount());
                    }
                }
                // 该帐号显示在列表最开始的地方
                result.add(0, temp);

            } else {
                temp.setId(account.getId());
                // 提示语为:电话
                temp.setData1(getString(R.string.contacts_detail_phone));
                // 如果没有SKYID，则说明当前号码没有关联的SKYID，则直接显示号码
                if (null != account.getPhone() && !"".equals(account.getPhone())) {
                    temp.setPhone(account.getPhone());
                }
                result.add(temp);
            }

        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT:
                if (resultCode == RESULT_OK) {
                    Contact contact = mService.getContactsModule().getContactById(contactId);
                    initDetail(contact);
                    data.putExtra(CURRENT_CONTACT, contact);
                    setResult(RESULT_OK, data);
                }
                break;
        }
    }

    protected void onBlackListFinish(int action, int result) {
        switch (result) {
            case ContactsNetModule.NET_SUCCESS:
                Intent data = new Intent();
                Contact contact = mService.getContactsModule().getContactById(contactId);
                Log.e("ContactsDetailActivity", "contactId: " + contactId + ",contact: "
                        + contact);
                data.putExtra(FriendDetailActivity.CURRENT_CONTACT, contact);
                if (BLACK == action) {
                    ContactsListItem item = new ContactsListItem();
                    item.setId(contactId);
                    ContactListCache.getInstance().recreateItems(
                            mService.getContactsModule().getContactInfoForList());
                    showToast(R.string.contacts_list_blacklist_sucess);
                } else if (UNBLACK == action) {
                    ContactListCache.getInstance().recreateItems(
                            mService.getContactsModule().getContactInfoForList());
                    showToast(R.string.contacts_list_blacklist_unblack_sucess);
                }
                setResult(RESULT_OK, data);
                finish();
                break;
            case ContactsNetModule.NET_ERR:
                showToast(getString(R.string.net_error) + "\r\n[" + Constants.ERROR_TIP + ":0x"
                        + StringUtil.autoFixZero(ResultCode.getCode()) + "]");
                break;
            case ContactsNetModule.NET_FAILED:
                if (BLACK == action)
                    showToast(R.string.contacts_list_blacklist_failed);
                else if (UNBLACK == action)
                    showToast(R.string.contacts_list_blacklist_unblack_failed);
                break;
        }
    }

    private Account getClickAccout(Account temp) {
        // Account result = new Account();
        for (Account account : accounts) {
            if (temp.getId() == account.getId())
                return account;
        }

        throw new RuntimeException("accounts error!");

    }

    /**
     * 查看详情时获取手信资料
     */
    private void getDetailShouxinInfo(Contact contact) {
        // 获取联系人详情
        ArrayList<Account> accounts = contact.getAccounts();
        int skyId = 0;
        for (Account account : accounts) {
            // 修改联系人详情信息
            if (account.getSkyId() > 0) {
                skyId = account.getSkyId();
            }
            if (account.isMain()) {
                break;
            }
        }
        // 只查看skyid存在的手信用户的详情
        if (skyId > 0) {
            mService.getContactsModule().getContactBySkyID(contact.getId(), skyId);
        }
    }

    /**
     * @return the distance
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /** 举报动作监听器 */
    protected InformAction informAction;

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.contacts_detail_title);

        ArrayList<String> mList = new ArrayList<String>();
        mList.add(getString(R.string.contacts_detail_edit_menu));

        if (skyId > 0) {
            mList.add(getString(R.string.inform_menu));
            informAction = new InformAction(this);
            informAction.setSkyId(skyId);
            informAction.setInformAO(new InformBU(handler));
        }

        final PopupMenuAdapter rightMenuAapter = new PopupMenuAdapter(this, mList);
        OnClickListener menuListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRightPopupMenu(ContactsDetailActivity.this, rightMenuAapter,
                        findViewById(R.id.topbar_imageButton_rightII),
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                                String itemName = parent.getItemAtPosition(position)
                                        .toString();
                                if (itemName
                                        .equals(getString(R.string.contacts_detail_edit_menu))) {
                                    // 进入编辑页面
                                    Intent resultIntent = new Intent(ContactsDetailActivity.this,
                                            ContactsDetailEditActivity.class);
                                    resultIntent.putExtra(ContactsDetailActivity.CONTACT_FLAG,
                                            contactId);
                                    resultIntent.putExtra(ContactsDetailEditActivity.ACTION_TYPE,
                                            ContactsDetailEditActivity.ACTION_EDIT);
                                    ContactsDetailActivity.this.startActivityForResult(
                                            resultIntent, ContactsDetailActivity.EDIT);
                                } else if (itemName
                                        .equals(getString(R.string.inform_menu))) {
                                    // 弹出举报框
                                    informAction.showInformDialog();
                                }
                                dismissPopupMenu();
                            }

                        });
            }
        };

        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII,
                R.drawable.topbar_btn_option, menuListener);

    }

    /**
     * 获取邀请内容，默认获取服务端短信，如果从服务端获取邀请内容为空，则取本地的邀请内容
     * 
     * @return
     */
    private String getInviteSting() {
        String text = CommonPreferences
                .getInviteConfigContent(Constants.INVITE_CONFIGURATION_SMS_TYPE);
        return TextUtils.isEmpty(text) ? getString(R.string.contacts_detail_invite_content) : text;
    }

}
