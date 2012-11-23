
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: AppAdapter
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-10-19 下午2:32:34
 */
public class AppAdapter extends BaseAdapter {
    // private final int apps[][];
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<AppGridItem> items = new ArrayList<AppGridItem>();

    public AppAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public AppGridItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getMode(int position) {
        return getItem(position).getAction();
    }

    public void addItem(int resId, int textId, int action) {
        AppGridItem item = new AppGridItem();
        item.resId = resId;
        item.textId = textId;
        item.action = action;
        items.add(item);
    }

    public void addItem(int position, int resId, int textId, int action) {
        AppGridItem item = new AppGridItem();
        item.resId = resId;
        item.textId = textId;
        item.action = action;
        items.add(position, item);
    }

    public void remove(int postion) {
        items.remove(postion);
    }

    class ViewHolder {
        ImageView app_image;
        TextView app_text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.app_grid_item, null);
            holder.app_image = (ImageView) convertView.findViewById(R.id.app_grid_item);
            holder.app_text = (TextView) convertView.findViewById(R.id.app_grid_item_name);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        holder.app_image.setBackgroundResource(items.get(position).getResId());
        holder.app_text.setText(items.get(position).getTextId());
        return convertView;
    }

    class AppGridItem {
        private int resId;
        private int textId;
        private int action;

        public int getResId() {
            return resId;
        }

        public void setResId(int resId) {
            this.resId = resId;
        }

        public int getTextId() {
            return textId;
        }

        public void setTextId(int textId) {
            this.textId = textId;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }
    }
}
