
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactListCache;
import android.skymobi.messenger.adapter.PopupMenuAdapter;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Friend;
import android.skymobi.messenger.bizunit.contact.InformBU;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.action.FriendDetailAction;
import android.skymobi.messenger.ui.action.InformAction;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.RegexUtil;
import android.skymobi.messenger.utils.ResultCode;
import android.skymobi.messenger.utils.StringUtil;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @ClassName: FriendDetailActivity
 * @Description: 好友详细信息
 * @author Anson.Yang
 * @date 2012-3-1 下午6:06:40
 */
public class FriendDetailActivity extends ContactsDetailActivity {
    public static final String TAG = FriendDetailActivity.class.getSimpleName();
    public static final int ADD = 900;

    public static final String FRIEND_ID_FLAG = "FRIEND_ID_FLAG";
    public static final String FRIEND_FLAG = "FRIEND_FLAG";
    public static final String FRIEND_CONTACT = "frined_contact";

    private static final byte DISPLAY_REASON = 0;

    private Friend friend = null;
    private long friendId;
    // private int skyId;
    private byte contactType;

    protected final FriendDetailAction action = new FriendDetailAction(this);
    protected View callLayout = null;
    protected LinearLayout deleteLayout = null;
    protected LinearLayout addLayout = null;
    protected LinearLayout reasonLayout = null;
    protected TextView sendMessage = null;
    protected TextView detailReason = null;

    @Override
    protected void init() {

        SLog.d(TAG, "init...");
        createViews();
        initBottomView();

        friendId = getIntent().getLongExtra(FRIEND_ID_FLAG, 0);
        contactType = getIntent().getByteExtra(CONTACT_TYPE, DISPLAY_REASON);
        if (null != mService) {
            friend = mService.getFriendModule().getFriendById(friendId);
        }
        if (friend == null) {
            finish();
        }
        contactId = friend.getContactId();
        initDetail(friend);

        reasonLayout.setVisibility(View.VISIBLE);
        displayDetailReason(friend, detailReason);

        if (contactType != DISPLAY_REASON) {
            reasonLayout.setVisibility(View.GONE);
        }

        addLayout.setOnClickListener(action);
        skyId = getSkyidFromAccouts();
        mService.getFriendModule().getFriendInfo(friend.getContactId(), skyId);
    }

    @Override
    protected void initDetail(Contact contact) {
        super.initDetail(contact);
    }

    protected void createViews() {
        callLayout = findViewById(R.id.contacts_detail_call_layout);
        deleteLayout = (LinearLayout) findViewById(R.id.contacts_detail_delete);
        addLayout = (LinearLayout) findViewById(R.id.friend_detail_add);
        sendMessage = (TextView) findViewById(R.id.contacts_detail_send_message_text);
        reasonLayout = (LinearLayout) findViewById(R.id.friend_recommend_reason);
        detailReason = (TextView) findViewById(R.id.friend_recommend_detail);
    }

    protected void initBottomView() {
        callLayout.setVisibility(View.GONE);
        deleteLayout.setVisibility(View.GONE);
        addLayout.setVisibility(View.VISIBLE);
        sendMessage.setText(R.string.friend_detail_send_message);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Contact contact = null;
            switch (msg.what) {
                case CoreServiceMSG.MSG_FRIENDS_DETAIL_SUCCESS:
                    contact = (Contact) msg.obj;
                    if (null != contact && contactId == contact.getId()) {
                        initDetail(contact);
                    }
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS:
                    removeDialog(ADD);
                    if (msg.obj == null) {
                        showToast(R.string.contacts_black_list_failed);
                        friend.setId(friendId);
                    } else {
                        contact = (Contact) msg.obj;
                        Intent data = new Intent();
                        data.putExtra(CURRENT_CONTACT, contact);
                        setResult(RESULT_OK, data);
                        ToastTool.showShort(FriendDetailActivity.this, R.string.add_friend_success);
                        Intent intent = new Intent(FriendDetailActivity.this,
                                ContactsDetailActivity.class);
                        intent.putExtra(CONTACT_ID_FLAG, contact.getId());
                        finish();
                        startActivity(intent);
                    }
                    break;
                case CoreServiceMSG.MSG_CONTACTS_ADD_BLACKLIST:
                    int result = (Integer) msg.obj;
                    removeDialog(BLACKING);
                    onBlackListFinish(BLACK, result);
                    break;
                case CoreServiceMSG.MSG_CONTACTS_IS_INCONTACT:
                    removeDialog(ADD);
                    showToast(R.string.contacts_list_is_exist);
                    break;
                case CoreServiceMSG.MSG_FRIENDS_DETAIL_FAIL:
                    int respSkyId = (Integer) msg.obj;
                    SLog.d("FriendDetailActivity", "get friendInfo fail skyid:" + skyId
                            + ",respSkyId" + respSkyId);
                    if (skyId == respSkyId) {
                        showToast(R.string.friend_detail_getError + "\r\n[" + Constants.ERROR_TIP
                                + ":0x" + StringUtil.autoFixZero(ResultCode.getCode()) + "]");
                    }
                    break;
            }
        }
    };

    private void displayDetailReason(Friend friend, TextView
            reason) {
        if (!TextUtils.isEmpty(friend.getDetailReason())) {
            boolean containsPhones = friend.getDetailReason().contains(
                    getString(R.string.friend_detailReason_flag));

            String names = RegexUtil
                    .getRegexString(friend.getDetailReason(), "\\<(.*?)\\>", 1);
            String num = RegexUtil.getRegexString(friend.getDetailReason(),
                    getString(R.string.friend_regex_flag), 0);
            if (null != num)
                num = RegexUtil.getRegexString(num, "(\\d+)", 0);
            if (null != names) {
                int len = null != num ? Integer.valueOf(num) : names
                        .split(getString(R.string.friend_regex_split)).length;
                String display = null;
                if (len <= 5) {
                    if (containsPhones) {
                        display = getString(R.string.friend_detailReason_havePhones_less,
                                friend.getDisplayname(), names);
                    } else {
                        display = getString(R.string.friend_detailReason_less,
                                friend.getDisplayname(), names);
                    }
                } else {
                    if (containsPhones) {
                        display = getString(R.string.friend_detailReason_havePhones,
                                friend.getDisplayname(),
                                names, num);
                    } else {
                        display = getString(R.string.friend_detailReason,
                                friend.getDisplayname(),
                                names, num);
                    }

                }
                reason.setText(Html.fromHtml(display));
            } else {
                reason.setText(friend.getDetailReason());
            }
        } else {
            reasonLayout.setVisibility(View.GONE);
        }
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
            case BLACKING:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_list_blacklist_waitting));
            case ADD:
                return ProgressDialog.show(this, getString(R.string.tip),
                        getString(R.string.contacts_detail_saving), true, true);
            case BLACK:
                dialogBuilder.setTitle(R.string.tip);
                dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
                dialogBuilder.setMessage(R.string.contacts_detail_blacklist_tip);
                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showDialog(BLACKING);
                        mService.getFriendModule().addFriendToBlackList(friend);
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
            default:
                return super.onCreateDialog(id);
        }
        dialog = dialogBuilder.create();
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * 将推荐好友添加为联系人
     */
    public void addContactFromFriend() {
        showDialog(ADD);
        if (null != friend && null != friend.getAccounts()
                && friend.getAccounts().size() > 0) {
            // 推荐好友被修改后账号可能多个
            int skyid = -1;
            skyid = getSkyidFromAccouts();
            // 如果该推荐联系人已经在本地,则提示好友添加失败
            boolean result = ContactListCache.getInstance().isInContactsList(
                    skyid);
            SLog.d("FriendDetailActivity", "isInContactsList: " + result);
            if (result) {
                // 删除该推荐人
                mHandler.sendEmptyMessage(CoreServiceMSG.MSG_CONTACTS_IS_INCONTACT);
                mService.getFriendModule().deteleFriendById(friend.getId());
                mService.notifyObservers(CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS, null);// 失败要发送通知，可以关闭等待框
            } else {
                friend.setUserType(ContactsColumns.USER_TYPE_STRANGER);
                mService.getFriendModule().addFriendToCloud(friend,
                        CoreServiceMSG.MSG_CONTACTS_ADD_STATUS_SUCCESS);
            }
        }
    }

    /**
     * @param skyid
     * @return
     */
    private int getSkyidFromAccouts() {
        int skyid = -1;
        ArrayList<Account> accounts = friend.getAccounts();
        for (Account account : accounts) {
            if (account.getSkyId() > 0) {
                skyid = account.getSkyId();
                break;
            }
        }
        return skyid;
    }

    @Override
    public void initTopBar() {

        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.contacts_detail_title);

        ArrayList<String> mList = new ArrayList<String>();
        mList.add(getString(R.string.inform_menu));

        informAction = new InformAction(this);
        informAction.setSkyId(skyId);
        informAction.setInformAO(new InformBU(handler));

        final PopupMenuAdapter rightMenuAapter = new PopupMenuAdapter(this, mList);
        OnClickListener menuListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRightPopupMenu(FriendDetailActivity.this, rightMenuAapter,
                        findViewById(R.id.topbar_imageButton_rightII),
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                                String itemName = parent.getItemAtPosition(position)
                                        .toString();
                                if (itemName
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

}
