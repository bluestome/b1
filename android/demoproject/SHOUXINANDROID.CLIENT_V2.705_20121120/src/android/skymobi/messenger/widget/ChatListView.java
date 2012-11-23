
package android.skymobi.messenger.widget;

import android.content.Context;
import android.skymobi.messenger.R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @ClassName: TestListView
 * @author Sivan.LV
 * @date 2012-3-29 下午1:55:58
 */
public class ChatListView extends ListView implements OnScrollListener {
    private static final String TAG = ChatListView.class.getSimpleName();

    private final static int RELEASE_TO_REFRESH = 0;
    private final static int DROP_TO_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;

    private final static int RATIO = 2;

    private LayoutInflater inflater;

    private LinearLayout headView;

    private TextView tipsTextview;
    // private TextView detailTextView;
    private ImageView arrowImageView;

    private ImageView refreshImageView;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;
    private RotateAnimation refreshAnimation;

    // 用于保证startY的值在一个完整的touch事件中只被记录一次
    private boolean isRecored;

    private int headContentWidth;
    private int headContentHeight;

    private int startY;
    private int firstItemIndex;

    private int state;

    private boolean isBack;

    private OnRefreshListener refreshListener;

    private boolean isRefreshable;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param context
     */
    public ChatListView(Context context) {
        super(context);
        init(context);
    }

    public ChatListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setCacheColorHint(context.getResources().getColor(R.color.transparent));
        inflater = LayoutInflater.from(context);

        headView = (LinearLayout) inflater.inflate(R.layout.chatlistview_head, null);

        arrowImageView = (ImageView) headView
                .findViewById(R.id.head_arrowImageView);

        /*
         * progressBar = (ProgressBar) headView
         * .findViewById(R.id.head_progressBar);
         */
        refreshImageView = (ImageView) headView.findViewById(R.id.head_refresh_item);

        refreshAnimation = new RotateAnimation(0, +360,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        refreshAnimation.setInterpolator(new LinearInterpolator());
        refreshAnimation.setDuration(1000);
        refreshAnimation.setRepeatCount(-1);

        tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
        // detailTextView = (TextView) headView
        // .findViewById(R.id.head_detailTextView);

        measureView(headView);
        headContentHeight = headView.getMeasuredHeight();
        headContentWidth = headView.getMeasuredWidth();

        headView.setPadding(0, -1 * headContentHeight, 0, 0);
        headView.invalidate();

        Log.i("size", "width:" + headContentWidth + " height:"
                + headContentHeight);

        addHeaderView(headView, null, false);
        setOnScrollListener(this);

        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);

        state = DONE;
        isRefreshable = false;
    }

    public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2,
            int arg3) {
        firstItemIndex = firstVisiableItem;
    }

    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try{
            if (isRefreshable) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (firstItemIndex == 0 && !isRecored) {
                            isRecored = true;
                            startY = (int) event.getY();
                            Log.i(TAG, "在down时候记录当前位置‘");
                        }
                        break;
    
                    case MotionEvent.ACTION_UP:
    
                        if (state != REFRESHING && state != LOADING) {
                            if (state == DONE) {
                                // 什么都不做
                            }
                            if (state == DROP_TO_REFRESH) {
                                state = DONE;
                                changeHeaderViewByState();
    
                                Log.i(TAG, "由下拉刷新状态，到done状态");
                            }
                            if (state == RELEASE_TO_REFRESH) {
                                state = REFRESHING;
                                changeHeaderViewByState();
                                onRefresh();
    
                                Log.i(TAG, "由松开刷新状态，到done状态");
                            }
                        }
    
                        isRecored = false;
                        isBack = false;
    
                        break;
    
                    case MotionEvent.ACTION_MOVE:
                        int tempY = (int) event.getY();
    
                        if (!isRecored && firstItemIndex == 0) {
                            Log.i(TAG, "在move时候记录下位置");
                            isRecored = true;
                            startY = tempY;
                        }
    
                        if (state != REFRESHING && isRecored && state != LOADING) {
    
                            if (state == RELEASE_TO_REFRESH) {
    
                                super.setSelection(0);
    
                                if (((tempY - startY) / RATIO < headContentHeight)
                                        && (tempY - startY) > 0) {
                                    state = DROP_TO_REFRESH;
                                    changeHeaderViewByState();
    
                                    Log.i(TAG, "由松开刷新状态转变到下拉刷新状态");
                                }
                                else if (tempY - startY <= 0) {
                                    state = DONE;
                                    changeHeaderViewByState();
    
                                    Log.i(TAG, "由松开刷新状态转变到done状态");
                                }
                                else {
                                }
                            }
                            if (state == DROP_TO_REFRESH) {
    
                                super.setSelection(0);
    
                                if ((tempY - startY) / RATIO >= headContentHeight) {
                                    state = RELEASE_TO_REFRESH;
                                    isBack = true;
                                    changeHeaderViewByState();
    
                                    Log.i(TAG, "由done或者下拉刷新状态转变到松开刷新");
                                }
                                else if (tempY - startY <= 0) {
                                    state = DONE;
                                    changeHeaderViewByState();
    
                                    Log.i(TAG, "由DOne或者下拉刷新状态转变到done状态");
                                }
                            }
    
                            if (state == DONE) {
                                if (tempY - startY > 0) {
                                    state = DROP_TO_REFRESH;
                                    changeHeaderViewByState();
                                }
                            }
    
                            if (state == DROP_TO_REFRESH) {
                                headView.setPadding(0, -1 * headContentHeight
                                        + (tempY - startY) / RATIO, 0, 0);
    
                            }
    
                            if (state == RELEASE_TO_REFRESH) {
                                headView.setPadding(0, (tempY - startY) / RATIO
                                        - headContentHeight, 0, 0);
                            }
    
                        }
    
                        break;
                }
            }
            return super.onTouchEvent(event);
        }catch(Exception e){
            return false;
        }
    }

    private void changeHeaderViewByState() {
        switch (state) {
            case RELEASE_TO_REFRESH:
                arrowImageView.setVisibility(View.VISIBLE);
                refreshImageView.setVisibility(View.GONE);
                refreshImageView.clearAnimation();
                tipsTextview.setVisibility(View.VISIBLE);
                arrowImageView.clearAnimation();
                arrowImageView.startAnimation(animation);

                tipsTextview.setText(R.string.chat_realse_to_refresh);

                Log.i(TAG, "当前状态，松开刷新");
                break;
            case DROP_TO_REFRESH:
                refreshImageView.setVisibility(View.GONE);
                refreshImageView.clearAnimation();
                tipsTextview.setVisibility(View.VISIBLE);

                arrowImageView.clearAnimation();
                arrowImageView.setVisibility(View.VISIBLE);
                if (isBack) {
                    isBack = false;
                    arrowImageView.clearAnimation();
                    arrowImageView.startAnimation(reverseAnimation);

                    tipsTextview.setText(R.string.chat_drop_to_refresh);
                } else {
                    tipsTextview.setText(R.string.chat_drop_to_refresh);
                }
                Log.i(TAG, "当前状态，下拉刷新");
                break;

            case REFRESHING:

                headView.setPadding(0, 0, 0, 0);

                refreshImageView.setVisibility(View.VISIBLE);
                tipsTextview.setVisibility(View.GONE);
                refreshImageView.startAnimation(refreshAnimation);
                arrowImageView.clearAnimation();
                arrowImageView.setVisibility(View.GONE);
                // tipsTextview.setText(R.string.chat_refresh);
                Log.i(TAG, "当前状态,正在刷新...");
                break;
            case DONE:
                headView.setPadding(0, -1 * headContentHeight, 0, 0);

                refreshImageView.setVisibility(View.GONE);
                refreshImageView.clearAnimation();
                arrowImageView.clearAnimation();
                arrowImageView.setImageResource(R.drawable.arrow_down_icon);
                tipsTextview.setText(R.string.chat_drop_to_refresh);
                Log.i(TAG, "当前状态，done");
                break;
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
        isRefreshable = true;
    }

    public void setRefreshable(boolean isRefreshable) {
        this.isRefreshable = isRefreshable;
    }

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public void onRefreshComplete() {
        state = DONE;
        changeHeaderViewByState();
        invalidateViews();
    }

    private void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void setAdapter(BaseAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    protected void onSizeChanged(int w, final int h, int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h < oldh) {
            int curSelecttion = getAdapter().getCount() > 0 ?
                    getAdapter().getCount() : 0;
            setSelection(curSelecttion);
        }

    }

    @Override
    public void setSelection(int position) {
        int realPosition = getHeaderViewsCount() + position;
        super.setSelection(realPosition);
    }
}
