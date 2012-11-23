
package android.skymobi.messenger.adapter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.CardDetailActivity;
import android.skymobi.messenger.ui.ChatActivity;
import android.skymobi.messenger.utils.HeaderCache;
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
public class VcardSearchListAdapter extends VcardListAdapter {

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
    public VcardSearchListAdapter(BaseActivity activity) {
        super(activity);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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
                    item.getSignature());
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

        // 头像
        ImageView header = (ImageView) view.findViewById(R.id.contacts_list_item_head);
        HeaderCache.getInstance().getHeader(item.getPhotoId(), item.getDisplayname(), header);

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (dialog == null) {
                    Builder dialogBuilder = new AlertDialog.Builder(activity);
                    String[] menuItems = activity.getResources().getStringArray(
                            R.array.select_dialog_items_vcard);
                    dialogBuilder.setItems(menuItems,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    switch (which) {
                                        case 0: {
                                            Contact contact = items.get(position);
                                            Account destAccount = (Account) activity
                                                    .getIntent()
                                                    .getSerializableExtra("account");
                                            /*
                                             * CoreService .getInstance()
                                             * .getMessageModule()
                                             * .sendCard(destAccount,
                                             * contact.getId());
                                             */
                                            Intent intent = new Intent(activity,
                                                    ChatActivity.class);
                                            ArrayList<Account> accountList = new ArrayList<Account>();
                                            accountList.add(destAccount);
                                            intent.putExtra(
                                                    ChatActivity.ACCOUNTS,
                                                    accountList);
                                            intent.putExtra(ChatActivity.CARD_ACCOUNTID,
                                                    contact.getId());
                                            intent.putExtra(ChatActivity.ACTION,
                                                    ChatActivity.ACTION_SENDCARD);
                                            activity.startActivity(intent);
                                            activity.finish();
                                        }
                                            break;
                                        case 1: {
                                            // 查看详情
                                            Intent intent1 = new Intent(activity,
                                                    CardDetailActivity.class);
                                            Contact contact = items.get(position);
                                            Account destAccount = (Account) activity
                                                    .getIntent()
                                                    .getSerializableExtra(
                                                            "account");
                                            intent1.putExtra(
                                                    CardDetailActivity.ACCOUNT,
                                                    destAccount);
                                            intent1.putExtra(
                                                    CardDetailActivity.CONTACT_ID,
                                                    contact.getId());
                                            intent1.putExtra(
                                                    CardDetailActivity.TYPE, 2);
                                            activity.startActivity(intent1);
                                        }
                                            break;
                                    }
                                    dialog.cancel();
                                    dialog = null;
                                }
                            });
                    dialog = dialogBuilder.create();
                }
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        });
        view.invalidate();
        return view;
    }

}
