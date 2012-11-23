
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CardDetailAdapter
 * @Description: Card Adapter
 * @author Michael.Pan
 * @date 2012-3-12 下午06:03:27
 */
public class CardDetailAdapter extends BaseAdapter {

    private static final String TAG = CardDetailAdapter.class.getSimpleName();
    private LayoutInflater mInflater = null;
    private Context mContext = null;
    List<CardItem> mCardList = new ArrayList<CardItem>();

    public CardDetailAdapter(Context ctx, LayoutInflater layoutInflater) {
        mContext = ctx;
        mInflater = layoutInflater;

    }

    @Override
    public int getCount() {
        return mCardList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(int keyId, String value) {
        mCardList.add(new CardItem(keyId, value));
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView position = " + position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.card_detail_list_item, null);
            holder = new ViewHolder();
            holder.keyTV = (TextView) convertView.findViewById(R.id.card_detail_item_key);
            holder.valueTV = (TextView) convertView.findViewById(R.id.card_detail_item_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.keyTV.setText(mCardList.get(position).keyId);
        holder.valueTV.setText(mCardList.get(position).value);
        return convertView;
    }

    public class CardItem {
        int keyId;
        String value;

        public CardItem(int keyId, String value) {
            this.keyId = keyId;
            this.value = value;
        }
    }

    static class ViewHolder {
        TextView keyTV;
        TextView valueTV;
    }

}
