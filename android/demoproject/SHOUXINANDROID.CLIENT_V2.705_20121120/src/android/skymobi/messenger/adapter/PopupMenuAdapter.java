
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * @ClassName: GroupAdapter
 * @Description: TODO
 * @author Anson.Yang
 * @date 2012-7-31 上午11:29:02
 */
public class PopupMenuAdapter extends BaseAdapter {
    private List<String> mList;
    private final Context mContext;

    public PopupMenuAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_item, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.popupMenuItem = (TextView) convertView.findViewById(R.id.popup_menu_item);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.popupMenuItem.setText(mList.get(position));

        return convertView;
    }

    static class ViewHolder {
        TextView popupMenuItem;
    }

    public void updateAdapter(List<String> list) {
        this.mList = list;
        notifyDataSetChanged();
    }
}
