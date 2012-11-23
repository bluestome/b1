
package android.skymobi.messenger.adapter;

import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.BaseActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-2-3
 * @version 1.0
 */
public class ContactBlackListAdapter extends ContactsBaseAdapter {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param activity
     * @param children
     * @param style
     */
    public ContactBlackListAdapter(BaseActivity activity) {
        super(activity);
        items = new ArrayList<ContactsListItem>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final ContactsListItem item = items.get(position);
        if (item.isGroup()) {
            return view;
        }
        // 在线状态
        if (item.isSkyUser()) {
            ImageView onlineStatus = (ImageView) view
                    .findViewById(R.id.contacts_list_item_online);
            onlineStatus.setVisibility(View.VISIBLE);
        }
        return view;
    }
}
