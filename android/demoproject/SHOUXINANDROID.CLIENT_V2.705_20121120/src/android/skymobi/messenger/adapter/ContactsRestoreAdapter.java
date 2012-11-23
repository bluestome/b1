
package android.skymobi.messenger.adapter;

import java.util.ArrayList;
import java.util.List;

import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsRestoreListActivity;
import android.skymobi.messenger.utils.HeaderCache;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.skymobi.android.sx.codec.beans.common.RestorableContacts;

/**
 * @ClassName: ContactsRestoreAdapter
 * @Description: TODO
 * @author Lv.Lv
 * @date 2012-8-30 上午10:00:32
 */
public class ContactsRestoreAdapter extends BaseAdapter {

    private List<RestorableContacts> list = null;
    private final BaseActivity activity;
    private LayoutInflater inflater = null;
    private List<RestorableContacts> selectedItems = null;

    public ContactsRestoreAdapter(BaseActivity activity) {
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
        this.selectedItems = new ArrayList<RestorableContacts>();
        this.list = ContactsRestoreListCache.getInstance().getList();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 全选 or 全不选
    public void selectAllorNone() {
        int selectCount = selectedItems.size();
        int totalCount = list.size();
        if (selectCount != totalCount) {
            selectedItems.clear();
            selectedItems.addAll(list);
        } else {
            selectedItems.clear();
        }

        changeButtonText();
        notifyDataSetChanged();
    }

    public List<RestorableContacts> getSelectedItems() {
        return selectedItems;
    }

    public int getSelectedCount() {
        return selectedItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contacts_list_item_child, null);
            holder = new ViewHolder();
            holder.infoLayout = convertView.findViewById(R.id.contacts_list_item_info_layout);
            holder.displayNameView = (TextView) convertView
                    .findViewById(R.id.contacts_list_item_display_name);
            holder.headerView = (ImageView) convertView.findViewById(R.id.contacts_list_item_head);
            holder.signatureView = (TextView) convertView
                    .findViewById(R.id.contacts_list_item_signature);
            holder.checkBox = (CheckBox) convertView
                    .findViewById(R.id.contacts_list_item_select_checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final RestorableContacts item = list.get(position);

        holder.infoLayout.setVisibility(View.VISIBLE);
        // 昵称
        String displayname = item.getContactName();
        if (TextUtils.isEmpty(displayname)) {
            displayname = item.getPersonNickname();
        }
        holder.displayNameView.setText(displayname);
        // 签名（显示电话或者账号）
        String phone = item.getPhone();
        if (TextUtils.isEmpty(phone)) {
            phone = item.getUserName();
        }
        holder.signatureView.setText(phone);
        // 头像
        HeaderCache.getInstance().getHeader(item.getImageHead(), displayname,
                holder.headerView);
        // 选中框
        holder.checkBox.setVisibility(View.VISIBLE);
        holder.checkBox.setChecked(selectedItems.contains(item));

        final View view = convertView;
        final CheckBox checkBox = holder.checkBox;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
                System.out.println("after ischecked " + checkBox.isChecked());
                if (checkBox.isChecked()) {
                    selectedItems.add(item);
                } else {
                    selectedItems.remove(item);
                }
                changeButtonText();
                view.invalidate();
            }
        });

        return convertView;
    }

    static class ViewHolder {
        public TextView displayNameView;
        public TextView signatureView;
        public ImageView headerView;
        public CheckBox checkBox;

        public View infoLayout;
    }

    /**
     * 设置恢复按钮文字
     */
    private void changeButtonText() {
        Button restoreBtn = (Button) ((ContactsRestoreListActivity) activity)
                .findViewById(R.id.restore_restore_btn);
        Button selectAllBtn = (Button) ((ContactsRestoreListActivity) activity)
                .findViewById(R.id.restore_selectall_btn);

        int totalCount = list.size();
        int count = selectedItems.size();
        String text = activity
                .getString(R.string.restore_restore);
        if (count > 0) {
            text = activity.getString(
                    R.string.restore_restore_multi_btn_text,
                    count);
            restoreBtn.setEnabled(true);
        } else {
            restoreBtn.setEnabled(false);
        }
        restoreBtn.setText(text);

        if (count > 0 && count == totalCount) {
            selectAllBtn.setText(R.string.contacts_select_all_contacts_cancel);
        } else {
            selectAllBtn.setText(R.string.contacts_select_all_contacts);
        }
    }
}
