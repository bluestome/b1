
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.PopupMenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * @ClassName: PopupMenuMultiAdapter
 * @Description: TODO
 * @author Anson.Yang
 * @date 2012-7-31 上午11:29:02
 */
public class PopupMenuMultiAdapter extends BaseAdapter {
    private List<PopupMenuItem> mList;
    private final Context mContext;

    public PopupMenuMultiAdapter(Context context, List<PopupMenuItem> list) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_multi_item,
                    null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.popupMenuText = (TextView) convertView.findViewById(R.id.popup_menu_multi_text);
            holder.popuoMenuImage = (ImageView) convertView
                    .findViewById(R.id.popup_menu_multi_image);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        PopupMenuItem item = mList.get(position);
        holder.popupMenuText.setText(item.getText());
        holder.popuoMenuImage.setImageResource(item.getResId());

        return convertView;
    }

    static class ViewHolder {
        TextView popupMenuText;
        ImageView popuoMenuImage;
    }

    public void updateAdapter(List<PopupMenuItem> list) {
        this.mList = list;
    }
}
