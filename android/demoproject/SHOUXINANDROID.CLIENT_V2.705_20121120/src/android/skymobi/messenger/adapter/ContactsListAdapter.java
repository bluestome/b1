
package android.skymobi.messenger.adapter;

import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.SearchUtil.PinyinResult;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
public class ContactsListAdapter extends ContactsBaseAdapter {

    protected ArrayList<ContactsListItem> onlineItems = new ArrayList<ContactsListItem>();

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
     */
    public ContactsListAdapter(BaseActivity activity) {
        super(activity);
        // 删除联系人 和 查看联系人列表中公用一个list 确保数据一致性 ContactMutilDeleteAdapter 中一样
        items = ContactListCache.getInstance().getListItems();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final ContactsListItem item = items.get(position);
        if (item.isGroup()) {
            return view;
        }

        final Holder holder = (Holder) view.getTag();
        // 显示搜索的高亮
        if (item.getHightlightType() == PinyinResult.TYPE_PINYIN) {
            SpannableStringBuilder style = new SpannableStringBuilder(
                    item.getDisplayname());
            int[] positions = item.getPositions();
            if (positions == null) {
                return view;
            }
            for (int i = 0; i < positions.length && positions[i] > 0; i++) {
                style.setSpan(new ForegroundColorSpan(
                        android.skymobi.messenger.utils.Constants.SEARCH_COLOR),
                        positions[i] - 1, positions[i],
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            holder.displayNameView.setText(style);
        } else if (item.getHightlightType() == PinyinResult.TYPE_NUMBER) {
            int[] positions = item.getPositions();
            SpannableStringBuilder style = new SpannableStringBuilder(
                    item.getPhone());
            style.setSpan(new ForegroundColorSpan(
                    android.skymobi.messenger.utils.Constants.SEARCH_COLOR),
                    positions[0], positions[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.signatureView.setText(style);
        } else {

        }

        // 在线状态
        if (item.isSkyUser()) {
            ImageView onlineStatus = (ImageView) view
                    .findViewById(R.id.contacts_list_item_online);
            onlineStatus.setVisibility(View.VISIBLE);

            boolean status = getUserOnlineStatusByAccounts(item.getAccounts());
            if (status) {
                onlineStatus.setBackgroundResource(R.drawable.online);
            } else {
                onlineStatus.setBackgroundResource(R.drawable.offline);
            }
        } else {
            view.findViewById(R.id.contacts_list_item_online).setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    public void displayOnlineContact() {
        onlineItems.clear();
        items = ContactListCache.getInstance().getListItems();

        ContactsListItem groupItem = null;
        String groupName = null;
        ContactsListItem item = null;
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            if (item.isGroup()) {
                groupItem = item;
                groupName = item.getGroupName();
            }

            if (item.isSkyUser()) {
                boolean status = getUserOnlineStatusByAccounts(item.getAccounts());
                if (status) {
                    if (item.getGroupName().equals(groupName) && null != groupItem) {
                        onlineItems.add(groupItem);
                        groupItem = null;
                        groupName = null;
                    }
                    onlineItems.add(item);
                }
            }
        }

        items = onlineItems;
    }

    public void displayAllContact() {
        items = ContactListCache.getInstance().getListItems();
    }

    public ArrayList<ContactsListItem> getItems() {
        return items;
    }
}
