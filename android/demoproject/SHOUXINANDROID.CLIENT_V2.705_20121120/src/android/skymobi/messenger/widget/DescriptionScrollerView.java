
package android.skymobi.messenger.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class DescriptionScrollerView extends FrameLayout {
    private Scroller scroller;
    private Drawable drawable;

    private PointF last = new PointF();
    private PointF downPoint = new PointF();

    private final int TOUCH_SLOP = ViewConfiguration.get(getContext())
            .getScaledTouchSlop();

    public DescriptionScrollerView(Context context) {
        super(context);
        scroller = new Scroller(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        boolean flag = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last.x = x;
                break;
            case MotionEvent.ACTION_MOVE:
                final int deltaX = (int) (last.x - x);
                if (Math.abs(deltaX) > TOUCH_SLOP) {
                    flag = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return flag;
    }

    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        final int width = getWidth();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                downPoint.x = x;
                break;
            case MotionEvent.ACTION_MOVE:
                final int deltaX = (int) (last.x - x);
                if (Math.abs(deltaX) < TOUCH_SLOP) {
                    break;
                }
                last.x = x;
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
            case MotionEvent.ACTION_CANCEL:
                float distance = downPoint.x - x;
                float distanceMax = 20;
                int dx = 0;
                if (distance > distanceMax) {
                    dx = getScrollX() / width + 1;
                    if (dx > getChildCount() - 1) {
                        dx = getChildCount() - 1;
                    }
                } else if (distance < 0 - distanceMax) {
                    dx = getScrollX() / width;
                    if (dx < 0) {
                        dx = 0;
                    }
                } else {
                    dx = (getScrollX() + width / 2) / width;
                    if (dx < 0) {
                        dx = 0;
                    }
                    if (dx > getChildCount() - 1) {
                        dx = getChildCount() - 1;
                    }
                }
                dx *= width;
                dx -= getScrollX();
                scroller.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx) * 3);
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getWidth();
        final int count = getChildCount();
        int height = getHeight();
        int childLeft = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.layout(childLeft, 0, childLeft + width, height);
            childLeft += width;
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable d) {
        super.setBackgroundDrawable(drawable);
        drawable = d;
        super.setBackgroundDrawable(null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (null != drawable) {
            drawable.setBounds(0, 0, getChildCount() * getWidth(), getHeight());
            drawable.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }
}
