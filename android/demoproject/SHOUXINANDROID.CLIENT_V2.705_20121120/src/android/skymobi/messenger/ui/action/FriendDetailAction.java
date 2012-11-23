
package android.skymobi.messenger.ui.action;

import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsDetailActivity;
import android.skymobi.messenger.ui.FriendDetailActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.view.View;

/**
 * @ClassName: FriendDetailAction
 * @Description: 好友列表操作
 * @author Anson.Yang
 * @date 2012-3-5 下午2:09:40
 */
public class FriendDetailAction extends ContactsDetailAction {

    public FriendDetailAction(BaseActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.contacts_detail_send_message:
            case R.id.contacts_detail_send_vcard:
                super.onClick(v);
                break;
            case R.id.contacts_detail_blacklist:
                if (CommonPreferences.getIsFirstAddBlack()) {
                    activity.showDialog(ContactsDetailActivity.FIRST_BLACK);
                    CommonPreferences.saveIsFirstAddBlack(false);
                } else {
                    activity.showDialog(ContactsDetailActivity.BLACK);
                }
                break;
            case R.id.friend_detail_add:
                ((FriendDetailActivity) activity).addContactFromFriend();
                break;
        }
    }
}
