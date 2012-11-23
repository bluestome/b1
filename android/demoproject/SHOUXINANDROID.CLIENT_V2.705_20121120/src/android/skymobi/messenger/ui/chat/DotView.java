
package android.skymobi.messenger.ui.chat;

import android.content.Context;
import android.skymobi.messenger.R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * @ClassName: DotView
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-10-11 下午1:52:17
 */
public class DotView extends LinearLayout {

    private Context mContext;

    public DotView(Context context) {
        super(context);
        init(context);
    }

    public DotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void initPage(int pageSize) {
        if (pageSize < 0)
            return;

        for (int i = 0; i < pageSize; i++) {
            ImageView localImageView = (ImageView) View.inflate(mContext,
                    R.layout.page_control_image, null);
            localImageView.setImageResource(R.drawable.page_control_unselected);
            addView(localImageView);
        }
        ((ImageView) getChildAt(0)).setImageResource(R.drawable.page_control_selected);
    }

    public void setPageNo(int pageNo) {
        if (pageNo < 0 || pageNo >= getChildCount())
            return;
        for (int i = 0; i < getChildCount(); i++) {
            if (i == pageNo) {
                ((ImageView) getChildAt(i)).setImageResource(R.drawable.page_control_selected);
            } else {
                ((ImageView) getChildAt(i)).setImageResource(R.drawable.page_control_unselected);
            }
        }
    }

    private void init(Context context) {
        mContext = context;
    }
}
