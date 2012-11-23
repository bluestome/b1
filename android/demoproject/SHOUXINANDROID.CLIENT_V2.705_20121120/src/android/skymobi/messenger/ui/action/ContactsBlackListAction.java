
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.skymobi.messenger.ui.ContactsBlackListDetailActivity;
import android.skymobi.messenger.ui.ContactsDetailActivity;
import android.skymobi.messenger.ui.TopActivity;
import android.view.View;
import android.widget.AdapterView;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class ContactsBlackListAction extends ContactsListAction {

    public ContactsBlackListAction(TopActivity activity) {
        super(activity);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent intent = new Intent(activity, ContactsBlackListDetailActivity.class);
        intent.putExtra(ContactsDetailActivity.CONTACT_ID_FLAG,
                parent.getAdapter().getItemId(position));
        activity.startActivity(intent);
    }

}
