
package android.skymobi.messenger.widget;

import android.content.Context;
import android.skymobi.messenger.R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * @ClassName: CornerListView
 * @Description: 自定义圆角ListView
 * @author Anson.Yang
 * @date 2012-2-24 下午7:16:06
 */
public class CornerListView extends ListView {

    public CornerListView(Context context) {
        super(context);
    }

    public CornerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // this.setBackgroundResource(R.drawable.corner_list_item_single_normal);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                int itemnum = pointToPosition(x, y);

                if (itemnum == AdapterView.INVALID_POSITION) {
                    break;
                } else {
                    if (itemnum == 0) {
                        if (itemnum == (getAdapter().getCount() - 1)) {
                            // 只有一项
                            setSelector(R.drawable.corner_list_single);
                        } else {
                            // 第一项
                            setSelector(R.drawable.corner_list_first);
                        }
                    } else if (itemnum == (getAdapter().getCount() - 1)) {
                        // 最后一项
                        setSelector(R.drawable.corner_list_last);
                    } else {
                        // 中间项
                        setSelector(R.drawable.corner_list_middle);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * @param itemheight the height of item, in unit of dip
     */
    public void setListViewHeight(int itemheight) {
        int height = DipToPixels(itemheight);
        final int cnt = getAdapter().getCount();

        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height * cnt + (getDividerHeight() * (cnt - 1));
        setLayoutParams(lp);
    }

    private int DipToPixels(int dip) {
        final float SCALE = getContext().getResources().getDisplayMetrics().density;
        float valueDips = dip;
        int valuePixels = (int) (valueDips * SCALE);
        return valuePixels;
    }

}
