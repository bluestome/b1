
package android.skymobi.messenger.adapter;

import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsListActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-2-3
 * @version 1.0
 */
public class ContactMutilDeleteAdapter extends ContactsBaseAdapter {

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
    public ContactMutilDeleteAdapter(BaseActivity activity) {
        super(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final ContactsListItem item = items.get(position);
        if (item.isGroup()) {
            return view;
        }

        final CheckBox checkBox = (CheckBox) view
                .findViewById(R.id.contacts_list_item_select_checkbox);
        checkBox.setVisibility(View.VISIBLE);
        if (this.selectedItems.contains(item)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        final View root = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("before ischecked " + checkBox.isChecked());
                checkBox.setChecked(!checkBox.isChecked());
                System.out.println("after ischecked " + checkBox.isChecked());
                if (checkBox.isChecked()) {
                    selectedItems.add(item);
                } else {
                    selectedItems.remove(item);
                }
                changeButtonText();
                root.invalidate();
            }
        });
        view.invalidate();
        return view;
    }

    public void selectAllorNone() {
        int selectCount = selectedItems.size();
        int totalcount = getContactsCount();
        if (selectCount != totalcount) {
            selectedItems.clear();
            for (ContactsListItem item : items) {
                if (item.isGroup())
                    continue;
                selectedItems.add(item);
            }
        } else {
            selectedItems.clear();
        }
        changeButtonText();
        notifyDataSetChanged();
    }

    /**
     * 设置删除按钮文字
     */
    public void changeButtonText() {
        Button multiDeleteButton = (Button) ((ContactsListActivity) activity)
                .findViewById(R.id.contacts_multi_delete);
        Button seleceAllButton = (Button) ((ContactsListActivity) activity)
                .findViewById(R.id.contacts_multi_cancel);
        int count = selectedItems.size();
        int totalcount = getContactsCount();

        String text = activity
                .getString(R.string.contacts_detail_delete);
        if (count > 0) {
            text = activity.getString(
                    R.string.contacts_multi_del_btn_text,
                    count);
            multiDeleteButton.setEnabled(true);
        } else {
            multiDeleteButton.setEnabled(false);
        }
        multiDeleteButton.setText(text);

        if (count > 0 && count == totalcount) {
            seleceAllButton.setText(R.string.contacts_select_all_contacts_cancel);
        } else {
            seleceAllButton.setText(R.string.contacts_select_all_contacts);
        }
    }

    int mContactsCount = 0;
    int mItemsCount = 0;

    private int getContactsCount() {
        if (mContactsCount == 0 || mItemsCount != items.size()) {
            mItemsCount = items.size();
            mContactsCount = 0;
            for (ContactsListItem item : items) {
                if (item.isGroup())
                    continue;
                mContactsCount++;
            }
        }
        return mContactsCount;
    }
}
