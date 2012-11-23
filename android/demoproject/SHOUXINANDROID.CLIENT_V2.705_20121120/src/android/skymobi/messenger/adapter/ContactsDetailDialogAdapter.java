
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Account;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ContactsDetailDialogAdapter
 * @author Sean.Xie
 * @date 2012-2-24 下午6:07:12
 */
public class ContactsDetailDialogAdapter extends BaseAdapter {

    List<Account> accounts = new ArrayList<Account>();

    private Context context;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param accounts
     * @param context
     */
    public ContactsDetailDialogAdapter(List<Account> accounts, Context context) {
        super();
        this.accounts = accounts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Account getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.contacts_detail_item,
                null);
        ((TextView) (view.findViewById(R.id.contacts_detail_title))).setText(accounts.get(position)
                .getData1());
        ((TextView) (view.findViewById(R.id.contacts_detail_content))).setText(accounts.get(
                position).getPhone());
        return view;
    }
}
