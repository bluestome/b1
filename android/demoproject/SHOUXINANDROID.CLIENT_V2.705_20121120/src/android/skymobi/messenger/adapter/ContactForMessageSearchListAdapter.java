
package android.skymobi.messenger.adapter;

import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsListActivity;
import android.skymobi.messenger.utils.SearchUtil.PinyinResult;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-2-3
 * @version 1.0
 */
public class ContactForMessageSearchListAdapter extends ContactForMessageListAdapter {

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
    public ContactForMessageSearchListAdapter(BaseActivity activity) {
        super(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final ContactsListItem item = items.get(position);
        if (item.isGroup()) {
            return view;
        }

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
            TextView displayName = (TextView) view
                    .findViewById(R.id.contacts_list_item_display_name);
            displayName.setText(style);
        } else if (item.getHightlightType() == PinyinResult.TYPE_NUMBER) {
            int[] positions = item.getPositions();
            SpannableStringBuilder style = new SpannableStringBuilder(
                    item.getPhone());
            style.setSpan(new ForegroundColorSpan(
                    android.skymobi.messenger.utils.Constants.SEARCH_COLOR),
                    positions[0], positions[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            TextView signature = (TextView) view
                    .findViewById(R.id.contacts_list_item_signature);
            signature.setText(style);
        } else {
            // 昵称
            TextView displayName = (TextView) view
                    .findViewById(R.id.contacts_list_item_display_name);
            displayName.setText(item.getDisplayname());
            // 签名
            TextView signature = (TextView) view
                    .findViewById(R.id.contacts_list_item_signature);
            signature.setText(item.getSignature());
        }
        return view;
    }

    /**
     * 添加子View
     * 
     * @param localDetailLayout
     * @param layoutResID
     * @param title
     * @param content
     */
    @Override
    protected void addChildView(LinearLayout localDetailLayout, int layoutResID, String title,
            String content, final int position, final Account account) {
        if (localDetailLayout.getChildCount() != 0) {
            View sepatator = LayoutInflater.from(activity).inflate(
                    R.layout.contacts_detail_item_separator,
                    null);
            localDetailLayout.addView(sepatator);
        }
        View view = LayoutInflater.from(activity).inflate(layoutResID, null);
        (activity).setTextViewValue(view, R.id.contacts_detail_title, title);
        (activity).setTextViewValue(view, R.id.contacts_detail_content, content);

        final CheckBox selectCheckBox = (CheckBox) view
                .findViewById(R.id.contacts_list_item_select_checkbox);
        selectCheckBox.setVisibility(View.VISIBLE);
        if (selectedAcconts.contains(account)) {
            selectCheckBox.setChecked(true);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCheckBox.setChecked(!selectCheckBox.isChecked());
                if (selectCheckBox.isChecked()) {
                    selectedAcconts.add(account);
                } else {
                    selectedAcconts.remove(account);
                }
                Button multiDeleteButton = (Button) ((ContactsListActivity) activity)
                        .findViewById(R.id.contacts_multi_delete);
                int count = selectedAcconts.size();
                String text = activity.getString(R.string.ok);
                if (count > 0) {
                    text = activity.getString(R.string.contacts_search_ok_btn_text,
                            count);
                    multiDeleteButton.setEnabled(true);
                } else {
                    multiDeleteButton.setEnabled(false);
                }
                multiDeleteButton.setText(text);
            }
        });
        localDetailLayout.addView(view);
    }

    @Override
    public Object[] getSections() {
        return selectedAcconts.toArray();
    }

}
