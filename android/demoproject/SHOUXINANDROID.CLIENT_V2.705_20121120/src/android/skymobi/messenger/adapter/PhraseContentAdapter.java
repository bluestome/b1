
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: PhraseContentAdapter
 * @Description: TODO
 * @author Michael.Pan
 * @date 2012-3-5 下午03:12:34
 */
public class PhraseContentAdapter extends BaseAdapter {

    private static final String TAG = PhraseTitleAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<String> mContentList = new ArrayList<String>();
    private final OnClickListener mClickListener;

    /**
     * @param pickPhraseActivity
     * @param layoutInflater
     */
    public PhraseContentAdapter(Context ctx, LayoutInflater layoutInflater, OnClickListener l) {
        mContext = ctx;
        mInflater = layoutInflater;
        mClickListener = l;
    }

    @Override
    public int getCount() {
        return mContentList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getContent(int position) {
        return mContentList.get(position);
    }

    public void updateList(ArrayList<String> list) {
        mContentList.clear();
        mContentList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView position = " + position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pick_phrase_list_item, null);
            holder = new ViewHolder();
            holder.content = (TextView) convertView.findViewById(R.id.pick_phrase);
            holder.pick = (Button) convertView.findViewById(R.id.pick_phrase_btn);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        holder.content.setText(mContentList.get(position));
        holder.pick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(position);
                mClickListener.onClick(v);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView content;
        Button pick;
    }
}
