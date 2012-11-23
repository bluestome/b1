
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsDetailActivity;
import android.skymobi.messenger.ui.ContactsDetailEditActivity;
import android.view.MotionEvent;
import android.view.View;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class ContactsBlacklistDetailAction extends ContactsDetailAction {

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
    public ContactsBlacklistDetailAction(BaseActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.contacts_detail_blacklist:
                activity.showDialog(ContactsDetailActivity.UNBLACK);
                break;
            case R.id.contacts_detail_edit_btn:
            case R.id.topbar_imageButton_rightII:
                Intent resultIntent = new Intent(activity, ContactsDetailEditActivity.class);
                resultIntent.putExtra(ContactsDetailActivity.CONTACT_FLAG, contact.getId());
                resultIntent.putExtra(ContactsDetailEditActivity.ACTION_TYPE,
                        ContactsDetailEditActivity.ACTION_EDIT);
                activity.startActivityForResult(resultIntent, ContactsDetailActivity.EDIT);
                break;
        }
    }

    /**
     * @param accounts
     */
    @Override
    public void setContact(Contact contact) {
        this.contact = contact;
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
}
