
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * @ClassName: SmileyAdapter
 * @Description: 笑脸 Adapter
 * @author Michael.Pan
 * @date 2012-2-15 下午02:59:57
 */
public class SmileyAdapter extends BaseAdapter {
    private static final String TAG = SmileyAdapter.class.getSimpleName();
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final int mPage;
    public final static int PAGE_SIZE = 15;
    // 笑脸资源id定义
    private final int mSmiley[][] = {
            {
                    R.drawable.smiley_0, R.drawable.smiley_1,
                    R.drawable.smiley_2, R.drawable.smiley_3,
                    R.drawable.smiley_4, R.drawable.smiley_5,
                    R.drawable.smiley_6, R.drawable.smiley_7,
                    R.drawable.smiley_8, R.drawable.smiley_9,
                    R.drawable.smiley_10, R.drawable.smiley_11,
                    R.drawable.smiley_12, R.drawable.smiley_13,
                    R.drawable.smiley_back_bg
            }, {
                    R.drawable.smiley_14, R.drawable.smiley_15,
                    R.drawable.smiley_16, R.drawable.smiley_17,
                    R.drawable.smiley_18, R.drawable.smiley_19,
                    R.drawable.smiley_20, R.drawable.smiley_21,
                    R.drawable.smiley_22, R.drawable.smiley_23,
                    R.drawable.smiley_24, R.drawable.smiley_25,
                    R.drawable.smiley_26, R.drawable.smiley_27,
                    R.drawable.smiley_back_bg
            }, {
                    R.drawable.smiley_28, R.drawable.smiley_29,
                    R.drawable.smiley_30, R.drawable.smiley_31,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, R.drawable.smiley_back_bg
            }
    };

    public SmileyAdapter(Context ctx, LayoutInflater inflater, int page) {
        mContext = ctx;
        mInflater = inflater;
        mPage = page;

    }

    @Override
    public int getCount() {
        return mSmiley[mPage].length;
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
        ViewCache mViewCache = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.smiley_grid_item, null);
            mViewCache = new ViewCache();
            mViewCache.mImageView = (ImageView) convertView.findViewById(R.id.smiley_image_item);
            convertView.setTag(mViewCache);
        } else {
            mViewCache = (ViewCache) convertView.getTag();
        }
        if (mSmiley[mPage][position] > 0)
            mViewCache.mImageView.setImageResource(mSmiley[mPage][position]);
        else {
            mViewCache.mImageView.setImageResource(R.drawable.tab_button_trans);
        }
        Log.i(TAG, "getView position = " + position);
        return convertView;
    }

    public class ViewCache {
        ImageView mImageView;
    }

}
