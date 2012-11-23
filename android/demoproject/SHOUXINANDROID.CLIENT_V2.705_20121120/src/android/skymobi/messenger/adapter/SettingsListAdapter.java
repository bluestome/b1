
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.SettingsItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * @ClassName: SettingsListAdapter
 * @Description: 设置界面adpter
 * @author Lv.Lv
 * @date 2012-2-27 下午5:00:28
 */
public class SettingsListAdapter extends BaseAdapter {

    private List<SettingsItem> mlist = null;

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private int mResource;

    /**
     * @param list
     */
    public SettingsListAdapter(Context context, List<SettingsItem> list) {
        mContext = context;
        mlist = list;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * @return the resource ID
     */
    public int getmResource() {
        return mResource;
    }

    /**
     * @param resource the resource ID to set
     */
    public void setResource(int resource) {
        this.mResource = resource;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateItem(int position, String content) {
        if (position < 0 || position >= mlist.size()) {
            return;
        }
        mlist.get(position).setContent(content);
        notifyDataSetChanged();
    }

    public void updateImage(int position, int imageId) {
        if (position < 0 || position >= mlist.size()) {
            return;
        }
        mlist.get(position).setmImageID(imageId);
        notifyDataSetChanged();
    }

    public void setChecked(int position, boolean checked) {
        if (position < 0 || position >= mlist.size()) {
            return;
        }
        mlist.get(position).setCheckedCheckBtn(checked);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, null);
            holder = new ViewHolder();
            holder.more = (ImageView) convertView.findViewById(R.id.settings_item_more);
            holder.title = (TextView) convertView.findViewById(R.id.settings_item_title);
            holder.content = (TextView) convertView.findViewById(R.id.settings_item_content);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.settings_item_checkbox);
            holder.image = (ImageView) convertView.findViewById(R.id.settings_item_newversion);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SettingsItem item = mlist.get(position);
        if (item.isContainMoreBtn()) {
            holder.more.setVisibility(View.VISIBLE);
            if (holder.checkbox != null) {
                holder.checkbox.setVisibility(View.GONE);
                holder.checkbox.setChecked(item.isCheckedCheckBtn());
            }
        } else {
            holder.more.setVisibility(View.GONE);
            if (item.isContainCheckBtn() && holder.checkbox != null) {
                holder.checkbox.setVisibility(View.VISIBLE);
                holder.checkbox.setChecked(item.isCheckedCheckBtn());
            }
        }
        if (holder.image != null) {
            if (item.getImageID() != 0) {
                holder.image.setImageResource(item.getImageID());
                holder.image.setVisibility(View.VISIBLE);
            } else {
                holder.image.setVisibility(View.GONE);
            }
        }
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());
        return convertView;
    }

    static class ViewHolder {
        TextView title;
        TextView content;
        ImageView more;
        ImageView image;
        CheckBox checkbox;
    }

}
