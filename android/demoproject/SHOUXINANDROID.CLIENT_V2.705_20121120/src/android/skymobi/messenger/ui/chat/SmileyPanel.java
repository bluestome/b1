
package android.skymobi.messenger.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.SmileyAdapter;
import android.skymobi.messenger.ui.ChatActivity;
import android.skymobi.messenger.ui.chat.SmileyFlipper.OnFinshListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * @ClassName: SmileyPannel
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-10-10 下午5:33:23
 */
public class SmileyPanel extends LinearLayout {

    private SmileyFlipper mSmileyFlipper;
    private DotView mDotView;
    private ArrayList<GridView> mSmileyGrid;
    private ChatActivity mActivity;
    private Context mContext;

    public SmileyPanel(Context context) {
        super(context);
        init(context);
    }

    public SmileyPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.smiley_panel, this);
        mSmileyFlipper = (SmileyFlipper) findViewById(R.id.smiley_panel_flipper);
        OnFinshListener onFlipperFinish = new OnFinshListener() {

            @Override
            public void onFinish(int pageNo) {
                mDotView.setPageNo(pageNo);
            }
        };
        mSmileyFlipper.setOnFinshListener(onFlipperFinish);
        mDotView = (DotView) findViewById(R.id.smiley_panel_dot);
        mDotView.setVisibility(View.VISIBLE);
        mDotView.initPage(3);
        mContext = context;
        mActivity = (ChatActivity) mContext;
        initSmileyGrid();
    }

    private void initSmileyGrid() {
        mSmileyGrid = new ArrayList<GridView>();
        for (int i = 0; i < 3; i++) {
            GridView localSmileyGrid = (GridView) inflate(mContext, R.layout.smiley_grid, null);
            SmileyAdapter mSmileyAdapter = new SmileyAdapter(mContext,
                    ((Activity) mContext).getLayoutInflater(), i);
            localSmileyGrid.setAdapter(mSmileyAdapter);
            localSmileyGrid.setOnItemClickListener(new SmileyGridListener(i, mContext));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT);
            mSmileyGrid.add(localSmileyGrid);
            mSmileyFlipper.addView(localSmileyGrid, new LinearLayout.LayoutParams(lp));
        }
    }

    // @Override
    // public void onItemClick(AdapterView<?> parent, View view, int position,
    // long id) {
    // mActivity.onItemClick(parent, view, position, id);
    //
    // }
}
