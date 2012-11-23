
package android.skymobi.messenger.widget;

import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * @ClassName: ZoomImageListener
 * @Description: TODO
 * @author Lv.Lv
 * @date 2012-4-17 下午4:37:28
 */
public class ZoomImageListener implements OnTouchListener {

    private ZoomState mState;
    private final PointF mStartPoint = new PointF();
    private final PointF mMidPoint = new PointF();
    private float mOldDistance;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        int pointNum = event.getPointerCount();
        if (pointNum == 1) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartPoint.set(event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dX = (event.getX() - mStartPoint.x) / v.getWidth();
                    float dY = (event.getY() - mStartPoint.y) / v.getHeight();
                    mState.setPanX(mState.getPanX() + dX);
                    mState.setPanY(mState.getPanY() + dY);
                    mState.notifyObservers();

                    mStartPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        }
        if (pointNum == 2) {
            switch (action) {
                case MotionEvent.ACTION_POINTER_1_DOWN:
                case MotionEvent.ACTION_POINTER_2_DOWN:
                    saveMidPoint(mMidPoint, event);
                    mOldDistance = getDistance(event);
                    break;
                case MotionEvent.ACTION_POINTER_1_UP:
                    // 第一点up，以第二点为起始点
                    mStartPoint.set(event.getX(1), event.getY(1));
                    break;
                case MotionEvent.ACTION_POINTER_2_UP:
                    // 第二点up，以第一点为起始点
                    mStartPoint.set(event.getX(0), event.getY(0));
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDistance = getDistance(event);
                    if (newDistance < 10.0f)
                        break;
                    float dRatio = (newDistance - mOldDistance) / mOldDistance;
                    float zoomRatio = (float) Math.pow(5, dRatio);
                    float oldZoom = mState.getZoom();
                    float newZoom = mState.setZoom(oldZoom * zoomRatio);// 当zoom的值超过限定范围后设置不成功，返回oldZoom
                    zoomRatio = newZoom / oldZoom;
                    // 设置pan，以两点的中点为中心点进行zoom
                    mState.setPanX((1 - zoomRatio) * mMidPoint.x / v.getWidth() + mState.getPanX()
                            * zoomRatio);
                    mState.setPanY((1 - zoomRatio) * mMidPoint.y / v.getHeight() + mState.getPanY()
                            * zoomRatio);
                    mState.notifyObservers();
                    mOldDistance = newDistance;
                    saveMidPoint(mMidPoint, event);
                    break;
            }
        }
        return true;
    }

    /** Calculate the distance between the two fingers */
    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the two fingers */
    private void saveMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void setZoomState(ZoomState state) {
        mState = state;
    }
}
