
package android.skymobi.messenger.ui;

import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.provider.SocialMessenger.ContactsColumns;
import android.skymobi.messenger.ui.action.ContactsBlacklistDetailAction;
import android.skymobi.messenger.ui.action.ContactsDetailAction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 类说明： 消息会话列表
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class ContactsBlackListDetailActivity extends ContactsDetailActivity {

    @Override
    protected void initButtons() {
        findViewById(R.id.contacts_detail_call).setEnabled(false);
        findViewById(R.id.contacts_detail_send_message).setEnabled(false);
        findViewById(R.id.contacts_detail_send_vcard).setEnabled(false);
        ((ImageView) findViewById(R.id.contacts_detail_send_vcard_icon)).setImageResource(
                R.drawable.contacts_detail_vcard_disable);
        findViewById(R.id.contacts_detail_blacklist).setOnClickListener(action);

        // 设置解除黑名单icon
        ((ImageView) findViewById(R.id.contacts_detail_blacklist_icon))
                .setImageResource(R.drawable.contacts_detail_black_remove);

        // 发短信不可用效果
        ((TextView) findViewById(R.id.contacts_detail_send_message))
                .setBackgroundResource(R.drawable.gray_btn_bg);
        ((TextView) findViewById(R.id.contacts_detail_send_message_text)).setEnabled(false);
        ((ImageView) findViewById(R.id.contacts_detail_send_message_icon))
                .setImageResource(R.drawable.contacts_detail_message_disable);
        // 打电话不可用效果
        ((TextView) findViewById(R.id.contacts_detail_call))
                .setBackgroundResource(R.drawable.gray_btn_bg);
        ((TextView) findViewById(R.id.contacts_detail_call_text)).setEnabled(false);
        ((ImageView) findViewById(R.id.contacts_detail_call_icon))
                .setImageResource(R.drawable.contacts_detail_call_disable);

    }
    
    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.contacts_detail_title);
    }
    
    @Override
    protected ContactsDetailAction createAction() {
        return new ContactsBlacklistDetailAction(this);
    }

    @Override
    protected void initDetail(Contact contact) {
        if (contact.getUserType() == ContactsColumns.USER_TYPE_STRANGER
                || contact.getUserType() == ContactsColumns.USER_TYPE_LBS_STRANGER) {
            findViewById(R.id.contacts_detail_delete).setVisibility(View.GONE);
            findViewById(R.id.friend_detail_add).setVisibility(View.VISIBLE);
            findViewById(R.id.friend_detail_add).setEnabled(false);
            ((ImageView) findViewById(R.id.contacts_detail_add_icon)).setImageResource(
                    R.drawable.contacts_detail_add_disable);
        } else {
            findViewById(R.id.contacts_detail_delete).setEnabled(false);
            ((ImageView) findViewById(R.id.contacts_detail_delete_icon)).setImageResource(
                    R.drawable.contacts_detail_delete_disable);
        }
        super.initDetail(contact);
        TextView tv = (TextView) findViewById(R.id.contacts_detail_blacklist_remove);
        tv.setText(R.string.contacts_black_list_remove);
    }

}
