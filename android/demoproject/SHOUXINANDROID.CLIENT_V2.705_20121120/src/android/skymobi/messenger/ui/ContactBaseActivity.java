
package android.skymobi.messenger.ui;

import android.content.Intent;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * @ClassName: ContactBaseActivity
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-7-6 上午10:11:02
 */
public class ContactBaseActivity extends TopActivity {
    private final static String TAG = ContactBaseActivity.class.getSimpleName();

    protected void showDetail(int skyid, Contact contact, int distance,byte contactType) {
        // 排序 黑名单->陌生人->联系人
        ArrayList<Contact> contacts = mService.getContactsModule().getContactBySkyid(skyid);
        Log.i(TAG, "get contact: " + contacts);
        if (null != contacts && contacts.size() > 0 && null != contacts.get(0)) {
            Contact findContact = contacts.get(0);
            Log.i(TAG, "find contact: " + findContact);

            if (findContact.getBlackList() == ContactsColumns.BLACK_LIST_YES) { // 是黑名单
                Log.i(TAG, "in blacklist");
                Intent intent = new Intent(this, ContactsBlackListDetailActivity.class);
                intent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                        findContact.getId());
                intent.putExtra(ContactsDetailActivity.CONTACT_DISTANCE, distance);
                startActivity(intent);
                if (findContact.getUserType() == ContactsColumns.USER_TYPE_STRANGER
                        || findContact.getUserType() == ContactsColumns.USER_TYPE_LBS_STRANGER) {
                    showToast(R.string.search_friend_isBlackStranger);
                } else {
                    showToast(R.string.search_friend_isBlackContact);
                }
            } else {
                if (findContact.getUserType() == ContactsColumns.USER_TYPE_STRANGER) { // 陌生人(推荐好友列表)
                    Log.i(TAG, "is contactStranger");
                    long friendId = mService.getFriendModule().getFriendIdByContactId(
                            findContact.getId());
                    if (friendId > 0) {
                        Intent frdIntent = new Intent(this, FriendDetailActivity.class);
                        frdIntent.putExtra(FriendDetailActivity.FRIEND_ID_FLAG, friendId);
                        frdIntent.putExtra(ContactsDetailActivity.CONTACT_DISTANCE, distance);
                        frdIntent.putExtra(FriendDetailActivity.CONTACT_TYPE, contactType);
                        startActivity(frdIntent);
//                         showToast(R.string.search_friend_isStranger);
                    }
                } else { // 联系人
                    Log.i(TAG, "is contact");
                    Intent contactIntent = new Intent(this, ContactsDetailActivity.class);
                    contactIntent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                            findContact.getId());
                    contactIntent.putExtra(ContactsDetailActivity.CONTACT_DISTANCE, distance);
                    this.startActivity(contactIntent);
                    showToast(R.string.search_friend_iscontact);
                }
            }
        } else {// 陌生人
            // showToast(R.string.search_friend_isStranger);
            Log.i(TAG, "is stranger");
            if (null != contact) {
                Intent intent = new Intent(this, FriendStrangerDetailActivity.class);
                intent.putExtra(ContactsDetailActivity.CONTACT_DISTANCE, distance);
                intent.putExtra(FriendStrangerDetailActivity.FRIEND, contact);
                intent.putExtra(FriendStrangerDetailActivity.CONTACT_TYPE, contactType);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, FriendStrangerDetailActivity.class);
                intent.putExtra(FriendStrangerDetailActivity.FRIEND_SKYID, skyid);
                intent.putExtra(ContactsDetailActivity.CONTACT_DISTANCE, distance);
                intent.putExtra(FriendStrangerDetailActivity.CONTACT_TYPE, contactType);
                startActivity(intent);
            }
        }
    }

    @Override
    public void initTopBar() {
        // TODO Auto-generated method stub

    }
}
