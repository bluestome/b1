
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.net.Uri;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ChatActivity;
import android.skymobi.messenger.ui.ContactsDetailActivity;
import android.skymobi.messenger.ui.ContactsDetailEditActivity;
import android.skymobi.messenger.ui.ImageViewActivity;
import android.skymobi.messenger.ui.VcardListActivity;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Base64;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ImageUtils;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class ContactsDetailAction extends BaseAction implements OnClickListener, OnTouchListener {

    private final static String TAG = ContactsDetailAction.class.getSimpleName();

    private List<Account> accounts;
    private Contact contact;

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
    public ContactsDetailAction(BaseActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.contacts_detail_call: {
                List<String> phones = new ArrayList<String>();
                if (accounts != null) {
                    for (Account account : accounts) {
                        if (!TextUtils.isEmpty(account.getPhone())) {
                            phones.add(account.getPhone());
                        }
                    }
                }
                if (accounts != null && phones.size() == 1) {
                    Uri uri = Uri.parse("tel:" + phones.get(0));
                    Intent it = new Intent(Intent.ACTION_CALL, uri);
                    startActivity(it);
                } else {
                    activity.showDialog(ContactsDetailActivity.CALL);
                }
            }
                break;
            case R.id.contacts_detail_send_message:
                if (accounts != null && accounts.size() == 1) {
                    Intent intent = new Intent(activity,
                            ChatActivity.class);
                    ArrayList<Account> accountList = new ArrayList<Account>();
                    accountList.add(accounts.get(0));
                    intent.putExtra(ChatActivity.ACCOUNTS, accountList);
                    if (activity instanceof ContactsDetailActivity) {
                        String talkReason = getNearUserTalkReason();
                        intent.putExtra(ChatActivity.TALK_REASON,
                                talkReason);
                    }

                    if (contact.getUserType() == ContactsColumns.USER_TYPE_STRANGER) {
                        /*
                         * intent.putExtra(ChatActivity.CONTACT_STRANGER,
                         * ChatActivity.CONTACT_STRANGER);
                         */
                        // intent.putExtra(ChatActivity.CONTACT, contact);
                    }
                    activity.startActivityForResult(intent, ContactsDetailActivity.MESSAGE);
                } else {
                    activity.showDialog(ContactsDetailActivity.MESSAGE);
                }
                break;
            case R.id.contacts_detail_send_vcard:
                if (accounts != null && accounts.size() == 1) {
                    Intent searchVCardIntent = new Intent(activity,
                            VcardListActivity.class);
                    searchVCardIntent.putExtra("account", contact.getAccounts().get(0));
                    activity.startActivityForResult(searchVCardIntent,
                            ContactsDetailActivity.MESSAGE);
                } else {
                    activity.showDialog(ContactsDetailActivity.VCARD);
                }
                break;
            case R.id.contacts_detail_delete:
                activity.showDialog(ContactsDetailActivity.DELETE);
                break;
            case R.id.contacts_detail_blacklist:
                if (CommonPreferences.getIsFirstAddBlack()) {
                    activity.showDialog(ContactsDetailActivity.FIRST_BLACK);
                    CommonPreferences.saveIsFirstAddBlack(false);
                } else {
                    activity.showDialog(ContactsDetailActivity.BLACK);
                }
                break;
            case R.id.contacts_detail_invite:
                if (accounts != null && accounts.size() == 1) {
                    Intent intent = new Intent(activity,
                            ChatActivity.class);
                    ArrayList<Account> accountList = new ArrayList<Account>();
                    accountList.add(accounts.get(0));
                    intent.putExtra(ChatActivity.ACCOUNTS,
                            accountList);
                    // 获取被邀请方的手机号码
                    String destPhone = accounts.get(0).getPhone() == null ? "" : accounts.get(0)
                            .getPhone();
                    // 如果被邀请方的号码不是手机号码，则需要提示用户当前
                    String phones = SettingsPreferences.getMobile() == null ? ""
                            : SettingsPreferences.getMobile();
                    phones = phones + "," + destPhone;
                    byte[] eb = AndroidSysUtils.inviteEncode(phones);
                    String selfPhone = "";
                    if (null != eb) {
                        selfPhone = Base64.encode(eb);
                    }
                    String inviteText = getInviteSting();
                    intent.putExtra(ChatActivity.CONTENT, inviteText + selfPhone);
                    if (activity instanceof ContactsDetailActivity) {
                        intent.putExtra(ContactsDetailActivity.CONTACT_DISTANCE,
                                getNearUserTalkReason());
                    }
                    MainApp.i().setInviteEntrance(Constants.INVITE_ENTRANCE_UNKNOW);
                    MainApp.i().setInviteEntrance(
                            Constants.INVITE_ENTRANCE_CONTACTS);
                    startActivity(intent);
                } else {
                    activity.showDialog(ContactsDetailActivity.INVITE);
                }
                break;
            case R.id.contacts_detail_edit_btn:
            case R.id.topbar_imageButton_rightII:
                Intent resultIntent = new Intent(activity, ContactsDetailEditActivity.class);
                resultIntent.putExtra(ContactsDetailActivity.CONTACT_FLAG, contact.getId());
                resultIntent.putExtra(ContactsDetailEditActivity.ACTION_TYPE,
                        ContactsDetailEditActivity.ACTION_EDIT);
                activity.startActivityForResult(resultIntent, ContactsDetailActivity.EDIT);
                break;
            case R.id.contacts_detail_head:
                // 检查sd卡begin ,hzc@20120919

                boolean isAvailable = AndroidSysUtils.isAvailableSDCard(activity);
                if (!isAvailable) {
                    ToastTool.showShort(activity, R.string.no_sdcard_tip);
                    return;
                } else if (AndroidSysUtils.getAvailableStore() < Constants.SDCARD_MIN_CAPACITY) {
                    ToastTool.showAtCenterShort(activity, R.string.sdcard_full);
                    return;
                }
                // ------------end---------------
                String photoID = contact.getPhotoId();
                SLog.d(TAG, "大头像id:" + photoID);
                if (!ImageUtils.isImageUrl(photoID)) {
                    SLog.d(TAG, "查看大头像时,检查到url非法!");
                    break;
                }

                Intent intent = new Intent(activity, ImageViewActivity.class);
                intent.putExtra(ImageViewActivity.HEADPHOTO_FILENAME, photoID);
                startActivity(intent);
                break;
        }
    }

    /**
     * @param accounts
     */
    public void setContact(Contact contact) {
        this.contact = contact;
        this.accounts = contact.getAccounts();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.contacts_detail_call:
                setTouchBackground(activity.findViewById(R.id.contacts_detail_call_icon),
                        event.getAction(), R.drawable.contacts_detail_call_selected,
                        R.drawable.contacts_detail_call_normal);
                break;
            case R.id.contacts_detail_send_message:
                setTouchBackground(activity.findViewById(R.id.contacts_detail_send_message_icon),
                        event.getAction(), R.drawable.contacts_detail_message_selected,
                        R.drawable.contacts_detail_message_normal);
                break;
        }
        return false;
    }

    private String getNearUserTalkReason() {
        int distance = ((ContactsDetailActivity) activity)
                .getDistance();
        if (distance >= 0)
            return MainApp.i().getText(R.string.nearuser_distance_tip)
                    + mService.getMessageModule().getDistanceText(distance);
        else
            return null;
    }

    /**
     * 获取邀请内容，默认获取服务端短信，如果从服务端获取邀请内容为空，则取本地的邀请内容
     * 
     * @return
     */
    private String getInviteSting() {
        String text = CommonPreferences
                .getInviteConfigContent(Constants.INVITE_CONFIGURATION_SMS_TYPE);
        return TextUtils.isEmpty(text) ? activity
                .getString(R.string.contacts_detail_invite_content) : text;
    }
}
