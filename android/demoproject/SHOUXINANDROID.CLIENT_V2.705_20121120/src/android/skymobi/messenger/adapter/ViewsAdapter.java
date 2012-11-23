
package android.skymobi.messenger.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * @ClassName: ViewsAdapter
 * @Description: 通用显示ListView页面
 * @author Anson.Yang
 * @date 2012-2-26 下午8:14:17
 */
public class ViewsAdapter extends BaseAdapter {
    private final List<View> views;

    public ViewsAdapter(List<View> views) {
        this.views = views;
    }

    @Override
    public int getCount() {
        return null != views ? views.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return views.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = views.get(position);
        }
        return convertView;
    }

}
