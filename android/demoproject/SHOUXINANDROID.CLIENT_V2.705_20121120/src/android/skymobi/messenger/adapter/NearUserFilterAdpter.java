
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: NearUserFilter
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-7-5 上午9:28:47
 */
public class NearUserFilterAdpter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Context mContext;
    List<Integer> resIDs = new ArrayList<Integer>();

    public NearUserFilterAdpter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        // resIDs.add(R.layout.nearuser_filterlist_female);
        // resIDs.add(R.layout.nearuser_filterlist_male);
        // resIDs.add(R.layout.nearuser_filterlist_all);
    }

    @Override
    public int getCount() {
        return resIDs.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = null;
        itemView = mInflater.inflate(resIDs.get(position), null);
        return itemView;
    }

}
