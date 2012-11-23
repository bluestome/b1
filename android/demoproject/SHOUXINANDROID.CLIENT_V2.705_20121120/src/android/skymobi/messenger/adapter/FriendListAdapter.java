
package android.skymobi.messenger.adapter;

import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.utils.RegexUtil;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @ClassName: FriendListAdapter
 * @Description: 推荐好友列表adapter
 * @author Anson.Yang
 * @date 2012-2-28 下午9:35:35
 */
public class FriendListAdapter extends BaseAdapter {

    private ArrayList<ContactsListItem> items = new ArrayList<ContactsListItem>();
    private BaseActivity activity = null;

    /**
     * @param activity
     * @param children
     * @param style
     */
    public FriendListAdapter(BaseActivity activity) {
        this.activity = activity;
        this.items = new ArrayList<ContactsListItem>();
    }

    public void setItems(ArrayList<ContactsListItem> items) {
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ContactsListItem item = items.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.friend_list_item,
                    null);
            holder = new ViewHolder();
            // 头像
            holder.friend_header = (ImageView) convertView
                    .findViewById(R.id.friend_list_item_head);
            // 昵称
            holder.friend_displayName = (TextView) convertView
                    .findViewById(R.id.friend_list_item_nickname);
            // 签名
            holder.friend_reason = (TextView) convertView
                    .findViewById(R.id.friend_list_item_reason);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 姓名
        holder.friend_displayName.setText(item.getDisplayname());
        // 签名
        String sign = null;
        if (null != item.getSignature()) {
            sign = RegexUtil.getRegexString(item.getSignature(), "(\\d+)", 0);
        }
        if (null != sign
                && item.getSignature().contains(
                        activity.getString(R.string.friend_recommendReason_flag))) {
            holder.friend_reason.setText(Html.fromHtml(activity.getString(
                    R.string.friend_recommendReason,
                    sign)));
        } else {
            holder.friend_reason.setText(item.getSignature());
        }

        // 头像
        HeaderCache.getInstance().getHeader(item.getPhotoId(),
                holder.friend_displayName.getText().toString(),
                holder.friend_header);
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    static class ViewHolder {
        ImageView friend_header;
        TextView friend_displayName;
        TextView friend_reason;

    }

}
