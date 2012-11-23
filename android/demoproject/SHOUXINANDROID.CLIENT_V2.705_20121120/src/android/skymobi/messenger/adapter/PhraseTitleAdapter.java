
package android.skymobi.messenger.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.skymobi.messenger.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.skymobi.android.sx.codec.beans.common.MsgType;

/**
 * @ClassName: PhraseTitleAdapter
 * @Description: TODO
 * @author Michael.Pan
 * @date 2012-3-2 下午06:11:10
 */
public class PhraseTitleAdapter extends BaseAdapter {

    private static final String TAG = PhraseTitleAdapter.class.getSimpleName();
    private LayoutInflater mInflater = null;
    private Context mContext = null;
    List<MsgType> mTitleList = new ArrayList<MsgType>();
    private int mSelectedPos = 0;

    /**
     * @param pickPhraseActivity
     * @param layoutInflater
     */
    public PhraseTitleAdapter(Context ctx, LayoutInflater layoutInflater) {
        mInflater = layoutInflater;
        mContext = ctx;
        mSelectedPos = 0;
    }

    @Override
    public int getCount() {
        return mTitleList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateSelectBg(int position) {
        mSelectedPos = position;
        notifyDataSetChanged();
    }

    public int getSelectedMsgTypeID() {
        if (mTitleList.size() <= 0)
            return -1;
        return mTitleList.get(mSelectedPos).getMsgTypeId();
    }

    public void updateList(ArrayList<MsgType> list) {
        mTitleList.clear();
        mTitleList.addAll(list);
        notifyDataSetChanged();
    }

    public int getMsgID(int position) {
        if (position < 0 || position > mTitleList.size() - 1)
            return 0;
        return mTitleList.get(position).getMsgTypeId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView position = " + position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pick_phrase_title_item, null);
            holder = new ViewHolder();
            holder.title = (Button) convertView.findViewById(R.id.pick_phrase_title_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(mTitleList.get(position).getMsgTypeName());
        if (mSelectedPos == position) {
            holder.title.setBackgroundResource(R.drawable.phrase_btn_pressed);
        } else {
            holder.title.setBackgroundResource(R.drawable.phrase_btn_normal);
        }
        return convertView;
    }

    static class ViewHolder {
        Button title;
    }
}
