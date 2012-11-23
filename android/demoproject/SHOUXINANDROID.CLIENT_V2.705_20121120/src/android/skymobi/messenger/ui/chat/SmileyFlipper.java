
package android.skymobi.messenger.ui.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @ClassName: SmileyFlipper
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-10-10 下午4:34:43
 */
public class SmileyFlipper extends ViewGroup {

    private Scroller mScroller;
    private SmileyInterpolator mSmileyInterpolator;
    private VelocityTracker mVelocityTracker;
    private int mScaledTouchSlop;
    private int mPageNo;
    private float lastX;
    private float downX;
    private OnFinshListener mOnFinshListener;

    public SmileyFlipper(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    public SmileyFlipper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int i = getChildCount();
        int j = 0;
        int k = 0;
        while (j < i)
        {
            View localView = getChildAt(j);
            if (localView.getVisibility() != View.GONE)
            {
                int m = localView.getMeasuredWidth();
                localView.layout(k, 0, k + m, localView.getMeasuredHeight());
                k += m;
            }
            j++;
        }
    }

    @Override
    protected void onMeasure(int paramInt1, int paramInt2)
    {
        super.onMeasure(paramInt1, paramInt2);
        int j = getChildCount();
        for (int k = 0; k < j; k++)
            getChildAt(k).measure(paramInt1, paramInt2);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final float x = (int) event.getX();
        boolean flag = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                final int deltaX = (int) (lastX - x);
                if (Math.abs(deltaX) >= mScaledTouchSlop) {
                    flag = true;
                }
                downX = x;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return flag;
    }

    @Override
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(paramMotionEvent);
        float x = paramMotionEvent.getX();
        switch (paramMotionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished())
                    mScroller.abortAnimation();
                downX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                final int deltaX = (int) (downX - x);
                downX = x;

                if (deltaX < 0) {
                    if (getScrollX() > 0) {
                        scrollBy(Math.max(-getScrollX(), deltaX), 0);
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getChildAt(getChildCount() - 1)
                            .getRight() - getScrollX() - getWidth();
                    if (availableToScroll > 0) {
                        scrollBy(Math.min(availableToScroll, deltaX), 0);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                VelocityTracker localVelocityTracker = mVelocityTracker;
                localVelocityTracker.computeCurrentVelocity(1000);
                int k = (int) localVelocityTracker.getXVelocity();
                Log.i("", "-------k:" + k + " mPageNo:" + mPageNo);
                if ((k > 600) && (mPageNo > 0)) {
                    flipperFinish(-1 + mPageNo);
                } else if ((k < -600) && (mPageNo < -1 + getChildCount()))
                {
                    flipperFinish(1 + mPageNo);
                } else {
                    int m = getWidth();
                    flipperFinish((getScrollX() + m / 2) / m);
                }
                if (mVelocityTracker != null)
                {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    private void init(Context context) {
        mSmileyInterpolator = new SmileyInterpolator();
        mScroller = new Scroller(context, mSmileyInterpolator);
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private void flipperFinish(int paramInt)
    {
        int i = Math.max(0, Math.min(paramInt, -1 + getChildCount()));
        if (getScrollX() != i * getWidth())
        {
            int j = i * getWidth() - getScrollX();
            mScroller.startScroll(getScrollX(), 0, j, 0, 2 * Math.abs(j));
            mPageNo = i;
            invalidate();
        }
        if (mOnFinshListener != null)
            mOnFinshListener.onFinish(mPageNo);
    }

    @Override
    public void computeScroll()
    {
        if (mScroller.computeScrollOffset())
        {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }

    }

    public void setOnFinshListener(OnFinshListener l) {
        this.mOnFinshListener = l;
    }

    interface OnFinshListener {
        public void onFinish(int pageNo);
    }

}
