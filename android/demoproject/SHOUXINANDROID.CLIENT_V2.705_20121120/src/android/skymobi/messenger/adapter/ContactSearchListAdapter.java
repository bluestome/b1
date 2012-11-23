
package android.skymobi.messenger.adapter;

import android.os.SystemClock;
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
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-2-3
 * @version 1.0
 */
public class ContactSearchListAdapter extends ContactsBaseAdapter {

    private ArrayList<ContactsListItem> selectItems;

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
    public ContactSearchListAdapter(BaseActivity activity) {
        super(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            View view = super.getView(position, convertView, parent);
            final ContactsListItem item = items.get(position);

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
                // 签名
                TextView signature = (TextView) view
                        .findViewById(R.id.contacts_list_item_signature);
                signature.setText(item.getSignature());
            } else if (item.getHightlightType() == PinyinResult.TYPE_NUMBER) {
                // 昵称
                TextView displayName = (TextView) view
                        .findViewById(R.id.contacts_list_item_display_name);
                displayName.setText(item.getDisplayname());
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
        } catch (Exception e) {
            SystemClock.sleep(300);
            e.printStackTrace();
            return getView(position, convertView, parent);
        }
    }

}
