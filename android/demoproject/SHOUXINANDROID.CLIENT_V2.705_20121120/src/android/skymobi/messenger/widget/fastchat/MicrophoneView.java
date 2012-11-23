
package android.skymobi.messenger.widget.fastchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.skymobi.messenger.R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @ClassName: fastchatMicrophoneView
 * @Description:
 * @author Sean.Xie
 * @date 2012-10-17 上午10:29:22
 */
public class MicrophoneView extends View {

    public final static int FALL_INVISIBLE = -1; // 隐藏不可见
    public final static int FALL_STOP = 0;
    public final static int FALL_INVIEW = 1; // 下落显示(匹配成功)
    public final static int FALL_OUTVIEW_MATCHFAIL = 2; // 下落掉下（匹配失败）
    public final static int FALL_OUTVIEW_NET_ERROR = 3; // 下落掉下（联网失败）

    private static Bitmap bitmap = null;
    private static Bitmap downBitmap = null;
    private static Bitmap disableBitmap = null;
    private Matrix matrix;

    private int position; // 是否可移动

    private int dy; // y轴位移量
    private float t = 0; // 自由落体运动时间

    private OnFinishListener mListener;
    private OnTouchListener touchListener;

    private boolean touchStatus; // 是否按下
    private boolean disableStatus; // 是否可用

    int[] touchPosition = new int[4];

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
    public MicrophoneView(Context context) {
        super(context);
        if (bitmap == null) {
            Bitmap org = BitmapFactory.decodeResource(getResources(),
                    R.drawable.fastchat_microphone);
            Bitmap orgDown = BitmapFactory.decodeResource(getResources(),
                    R.drawable.fastchat_microphone_down);
            Bitmap orgDisable = BitmapFactory.decodeResource(getResources(),
                    R.drawable.fastchat_microphone_disable);
            bitmap = org.copy(Config.ARGB_8888, true);
            downBitmap = orgDown.copy(Config.ARGB_8888, true);
            disableBitmap = orgDisable.copy(Config.ARGB_8888, true);
            org.recycle();
            orgDown.recycle();
            orgDisable.recycle();
        }

        matrix = new Matrix();
        matrix.setTranslate(0, 0);

    }

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public MicrophoneView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param context
     * @param attrs
     */
    public MicrophoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (position > FALL_STOP) {
            calculateY();
            invalidate();
        }
        // canvas 宽高
        int bitmapHeight = bitmap.getHeight();
        int rootWidth = getRootView().getWidth();
        // 图像宽高
        int bmW = bitmap.getWidth();
        if (position == FALL_INVISIBLE) {
            matrix.setTranslate((rootWidth - bmW) / 2, -bitmapHeight);
        } else {
            matrix.setTranslate((rootWidth - bmW) / 2, dy - bitmapHeight);
        }
        if (touchStatus) {
            canvas.drawBitmap(downBitmap, matrix, null);
        } else if (disableStatus) {
            canvas.drawBitmap(disableBitmap, matrix, null);
        } else {
            canvas.drawBitmap(bitmap, matrix, null);
        }
    }

    /**
     * 计算Y轴位移量
     */
    private void calculateY() {
        t += 0.1;
        dy = (int) ((9.8f * t * t) / 2f);
        int mainLayoutHeight = getRootView().findViewById(R.id.fastchat_main_layout).getHeight();
        if (mainLayoutHeight > bitmap.getHeight()) {
            mainLayoutHeight = bitmap.getHeight();
        }
        if (dy >= mainLayoutHeight && position == FALL_INVIEW) {
            dy = mainLayoutHeight;
            position = FALL_STOP;
            if (mListener != null) {
                int[] position = getPosition();
                mListener.onFinish(FALL_INVIEW, position);
            }
        } else if (dy >= mainLayoutHeight * 2 && position == FALL_OUTVIEW_MATCHFAIL) {
            dy = bitmap.getHeight() + getRootView().getHeight();
            position = FALL_STOP;
            if (mListener != null) {
                mListener.onFinish(FALL_OUTVIEW_MATCHFAIL, null);
            }
        } else if (dy >= mainLayoutHeight * 2 && position == FALL_OUTVIEW_NET_ERROR) {
            dy = bitmap.getHeight() + getRootView().getHeight();
            position = FALL_STOP;
            if (mListener != null) {
                mListener.onFinish(FALL_OUTVIEW_NET_ERROR, null);
            }
        }
    }

    /**
     * 计算xy轴坐标
     * 
     * @return
     */
    private int[] getPosition() {
        int recorderHeight = bitmap.getHeight() / 3;
        int recorderWidth = bitmap.getWidth();
        // 话筒L坐标
        touchPosition[0] = (getRootView().getWidth() - recorderWidth) / 2;
        // 话筒T坐标
        touchPosition[1] = dy - recorderHeight;
        // 话筒R坐标
        touchPosition[2] = touchPosition[0] + recorderWidth;
        // 话筒B坐标
        touchPosition[3] = dy;

        return touchPosition;
    }

    /**
     * 以何种方式移动，带动画
     * 
     * @param moveable
     */
    public void movePosition(int position) {
        movePosition(position, true);
    }

    /**
     * 以何种方式移动，是否带动画
     * 
     * @param moveable
     */
    public void movePosition(int position, boolean bWithAni) {
        this.position = position;
        if (!bWithAni) {
            t = 20; // 确保一次刷新就刷出来,没有下落动画
        } else {
            t = 0;
            dy = 0;
        }
        disableStatus = false;
        invalidate();
    }

    /**
     * 设置回调
     * 
     * @param callback
     */
    public void setOnFinishListener(OnFinishListener listener) {
        this.mListener = listener;
    }

    public void setOnTouchListener(OnTouchListener listener) {
        this.touchListener = listener;
    }

    public void setTouchStatus(boolean status) {
        this.touchStatus = status;
        invalidate();
    }

    public void setDisableStatus(boolean status) {
        this.disableStatus = status;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchListener == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        if (x >= touchPosition[0] && x <= touchPosition[2] && y >= touchPosition[1]
                && y <= touchPosition[3]) {
            return touchListener.onTouch(this, event);
        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                return false;
            }
            MotionEvent eventNew = MotionEvent.obtain(event);
            eventNew.setAction(MotionEvent.ACTION_UP);
            return touchListener.onTouch(this, eventNew);
        }
    }

    /**
     * @ClassName: onFinishListener
     * @Description: 快聊绳子下落完成的监听
     * @author Sean.Xie
     * @date 2012-10-17 下午3:13:38
     */
    public interface OnFinishListener {

        public static final int l = 0, t = 1, r = 2, b = 3;

        /**
         * @param result 0 :表示匹配成功 ; 1: 表示匹配失败; 2:联网失败
         * @param position 位置信息 对应L,T,R,B
         */
        void onFinish(int result, int[] position);

    }
}
