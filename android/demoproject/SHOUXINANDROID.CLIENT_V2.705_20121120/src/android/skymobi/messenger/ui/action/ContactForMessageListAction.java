
package android.skymobi.messenger.ui.action;

import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.ContactForMessageListActivity;
import android.skymobi.messenger.ui.TopActivity;
import android.view.View;
import android.widget.AdapterView;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class ContactForMessageListAction extends ContactsListAction {

    public ContactForMessageListAction(TopActivity activity) {
        super(activity);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.contacts_multi_delete:
                ((ContactForMessageListActivity) activity).finishSelection();
                break;
            case R.id.contacts_multi_cancel:
                activity.finish();
                break;
        }
    }
}
