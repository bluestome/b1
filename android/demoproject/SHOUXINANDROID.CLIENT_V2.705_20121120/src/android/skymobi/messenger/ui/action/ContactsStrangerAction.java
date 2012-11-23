
package android.skymobi.messenger.ui.action;

import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsStrangerDetailActivity;
import android.view.View;

/**
 * @ClassName: ContactsStrangerAction
 * @author Anson.Yang
 * @date 2012-5-2 下午7:43:58
 */
public class ContactsStrangerAction extends ContactsDetailAction {

    public ContactsStrangerAction(BaseActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.friend_detail_add:
                ((ContactsStrangerDetailActivity) activity).addContact();
                ((ContactsStrangerDetailActivity) activity).findViewById(R.id.friend_detail_add)
                        .setClickable(false);
                break;
        }
    }
}
