
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.FriendDetailActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class FriendListAction extends BaseAction implements OnItemClickListener {

    public FriendListAction(BaseActivity activity) {
        super(activity);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        ContactsListItem item = (ContactsListItem) parent.getAdapter().getItem(position);
        Intent intent = new Intent(activity, FriendDetailActivity.class);
        intent.putExtra(FriendDetailActivity.FRIEND_ID_FLAG,
                item.getId());
        activity.startActivity(intent);
    }

}
