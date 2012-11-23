
package android.skymobi.messenger.adapter;

import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Constants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: InputModeAdapter
 * @Description: 输入模式切换
 * @author Michael.Pan
 * @date 2012-2-29 下午07:28:11
 */
public class InputModeAdapter extends BaseAdapter {

    private static final String TAG = InputModeAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private final Context mContext;

    List<Integer> resIDs = new ArrayList<Integer>();

    public InputModeAdapter(Context ctx, LayoutInflater inflater) {
        mContext = ctx;
        mInflater = inflater;
        resIDs.add(R.layout.phrase_list_item);
        resIDs.add(R.layout.smiley_list_item);
        resIDs.add(R.layout.text_list_item);
        resIDs.add(R.layout.voice_list_item);

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

        Padding p = getPadding(position, resIDs.size());
        itemView.setPadding(p.left, p.top,
                p.right, p.bottom);
        return itemView;
    }

    public void updateAdapter(int mode)
    {
        resIDs.clear();

        if (0 != (mode & Constants.INPUT_MODE_PHRASE)) {
            resIDs.add(R.layout.phrase_list_item);
        }

        if (0 != (mode & Constants.INPUT_MODE_SMILEY)) {
            resIDs.add(R.layout.smiley_list_item);
        }

        if (0 != (mode & Constants.INPUT_MODE_TEXT)) {
            resIDs.add(R.layout.text_list_item);
        }

        if (0 != (mode & Constants.INPUT_MODE_VOICE)) {
            resIDs.add(R.layout.voice_list_item);
        }

        notifyDataSetChanged();

    }

    /*
     * private int getPaddingLeft(int position, int size) { int[] paddingLefts =
     * { 186, 120, 65, 30 }; return paddingLefts[4 - size + position]; }
     */

    /*
     * private Padding getPadding(int position, int size) { int r =
     * AndroidSysUtils.getScreenWidthForDip(mContext); int height =
     * AndroidSysUtils.px2dip(mContext, 80); Padding p = new Padding(); p.left =
     * AndroidSysUtils.dip2px(mContext, (int) (r - Math.sqrt(Math.pow(r, 2) -
     * Math.pow((size - position) * height, 2)))); return p; }
     */

    private Padding getPadding(int position, int size) {
        int[] paddingLefts = {
                186,
                120,
                65,
                30
        };
        Padding p = new Padding();

        p.left = AndroidSysUtils.dip2px(mContext, paddingLefts[4 - size + position]);
        if (1 == position || 3 == position) {
            p.bottom = AndroidSysUtils.dip2px(mContext, 10);
        }

        return p;
    }

    class Padding {
        int left;
        int right;
        int top;
        int bottom;
    }

}
