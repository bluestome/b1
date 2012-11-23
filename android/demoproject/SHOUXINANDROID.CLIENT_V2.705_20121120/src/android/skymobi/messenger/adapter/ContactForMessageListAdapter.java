
package android.skymobi.messenger.adapter;

import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.ContactsListActivity;
import android.skymobi.messenger.utils.SearchUtil.PinyinResult;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-2-3
 * @version 1.0
 */
public class ContactForMessageListAdapter extends ContactsBaseAdapter {

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
    public ContactForMessageListAdapter(BaseActivity activity) {
        super(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final ContactsListItem item = items.get(position);
        if (item.isGroup()) {
            return view;
        }

        ArrayList<Account> accounts = item.getAccounts();
        int size = accounts.size();
        final View subView = view.findViewById(R.id.contacts_list_sub_item);
        final CheckBox selectCheckBox = (CheckBox) view
                .findViewById(R.id.contacts_list_item_select_checkbox);
        View descView = view.findViewById(R.id.contacts_list_item_child_desc);
        Holder holder = (Holder) view.getTag();
        if (size > 1) {
            LinearLayout accountsLayout = (LinearLayout) view
                    .findViewById(R.id.contacts_list_sub_item);
            accountsLayout.removeAllViews();
            view.findViewById(R.id.item_more).setVisibility(View.VISIBLE);
            selectCheckBox.setVisibility(View.GONE);

            final View root = view;
            descView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int flag = subView.getVisibility();
                    subView.setVisibility(flag == View.VISIBLE ? View.GONE
                            : View.VISIBLE);
                    root.invalidate();
                }
            });

            for (Account account : accounts) {
                // 该部分代码用于控制控制多手机号码的的联系人在列表中是否显示扩展的信息。
                if (selectedAcconts.contains(account)) {
                    subView.setVisibility(View.VISIBLE);
                } else {
                    subView.setVisibility(View.GONE);
                }
                if (account.getSkyId() != 0) {
                    try {
                        String accountShowText = account.getPhone();
                        if (TextUtils.isEmpty(accountShowText)) {
                            accountShowText = account.getSkyAccount();
                            if (TextUtils.isEmpty(accountShowText)) {
                                accountShowText = account.getNickName();
                            }
                        }

                        boolean status = MainApp.i().getUserOnlineStatus(
                                account.getSkyId());
                        if (status) {
                            addChildView(
                                    accountsLayout,
                                    R.layout.contacts_detail_item,
                                    activity.getString(R.string.contacts_detail_shouxin_number),
                                    activity.getString(
                                            R.string.contacts_list_sub_item_number,
                                            accountShowText), position, account);
                        } else {
                            addChildView(
                                    accountsLayout,
                                    R.layout.contacts_detail_item,
                                    activity.getString(R.string.contacts_detail_phone),
                                    accountShowText, position, account);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    addChildView(accountsLayout, R.layout.contacts_detail_item,
                            activity.getString(R.string.contacts_detail_phone),
                            account.getPhone(), position, account);
                }
            }
        } else {

            Account account = item.getAccounts().get(0);
            String text = TextUtils.isEmpty(account.getPhone()) ? account
                    .getSkyAccount() : account.getPhone();
            boolean status = MainApp.i().getUserOnlineStatus(account.getSkyId());
            if (status) {
                text = activity.getString(R.string.contacts_list_sub_item_number, text);
            }
            holder.signatureView.setText(text);
            view.findViewById(R.id.item_more).setVisibility(View.GONE);
            selectCheckBox.setVisibility(View.VISIBLE);
            subView.setVisibility(View.GONE);
            if (selectedAcconts.contains(item.getAccounts().get(0))) {
                selectCheckBox.setChecked(true);
            } else {
                selectCheckBox.setChecked(false);
            }
            descView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectCheckBox.setChecked(!selectCheckBox.isChecked());
                    if (selectCheckBox.isChecked()) {
                        selectedAcconts.add(item.getAccounts().get(0));
                    } else {
                        selectedAcconts.remove(item.getAccounts().get(0));
                    }

                    Button multiDeleteButton = (Button) ((ContactsListActivity) activity)
                            .findViewById(R.id.contacts_multi_delete);
                    int count = selectedAcconts.size();
                    String text = activity.getString(R.string.ok);
                    if (count > 0) {
                        text = activity.getString(
                                R.string.contacts_search_ok_btn_text,
                                count);
                        multiDeleteButton.setEnabled(true);
                    } else {
                        multiDeleteButton.setEnabled(false);
                    }
                    multiDeleteButton.setText(text);
                }

            });
        }

        // 显示搜索的高亮
        if (item.getHightlightType() == PinyinResult.TYPE_PINYIN) {
            SpannableStringBuilder style = new SpannableStringBuilder(
                    holder.displayNameView.getText());
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
                    holder.signatureView.getText());
            style.setSpan(new ForegroundColorSpan(
                    android.skymobi.messenger.utils.Constants.SEARCH_COLOR),
                    positions[0], positions[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.signatureView.setText(style);
        }

        view.invalidate();
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
        } else {
            selectCheckBox.setChecked(false);
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

    public void selectAll() {
        selectedAcconts.clear();
        for (ContactsListItem item : items) {
            if (item.isGroup())
                continue;
            selectedAcconts.addAll(item.getAccounts());
        }
        changeButtonText();
    }

    /**
     * 设置删除按钮文字
     */
    protected void changeButtonText() {
        Button multiDeleteButton = (Button) ((ContactsListActivity) activity)
                .findViewById(R.id.contacts_multi_delete);
        int count = selectedAcconts.size();
        String text = activity
                .getString(R.string.ok);
        if (count > 0) {
            text = activity.getString(
                    R.string.contacts_search_ok_btn_text,
                    count);
            multiDeleteButton.setEnabled(true);
        } else {
            multiDeleteButton.setEnabled(false);
        }
        multiDeleteButton.setText(text);
    }

}
