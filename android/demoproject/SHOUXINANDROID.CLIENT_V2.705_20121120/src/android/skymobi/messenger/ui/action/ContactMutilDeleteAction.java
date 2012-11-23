
package android.skymobi.messenger.ui.action;

import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactMutilDeleteAdapter;
import android.skymobi.messenger.ui.ContactMutilDeleteListActivity;
import android.skymobi.messenger.ui.TopActivity;
import android.view.View;

/**
 * @ClassName: ContactMutilDeleteAction
 * @author Sean.Xie
 * @date 2012-4-19 上午11:44:17
 */
public class ContactMutilDeleteAction extends ContactsListAction {

    private ContactMutilDeleteAdapter adapter = null;

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
    public ContactMutilDeleteAction(TopActivity activity) {
        super(activity);
    }

    public void setAdapter(ContactMutilDeleteAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.contacts_multi_delete:
                showDialog(ContactMutilDeleteListActivity.MULTI_DELETE_CONTACT_CONFIRM);
                break;
            case R.id.contacts_multi_cancel:
                if (adapter != null) {
                    adapter.selectAllorNone();
                }
                break;
        }
    }

}
