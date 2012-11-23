
package android.skymobi.messenger.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.skymobi.messenger.R;
import android.util.AttributeSet;
import android.view.View;

import java.util.Observable;
import java.util.Observer;

/**
 * @ClassName: ZoomImageView
 * @Description: 可以zoom的ImageView，支持对居中的正方形区域进行crop
 * @author Lv.Lv
 * @date 2012-4-17 下午3:12:19
 */
public class ZoomImageView extends View implements Observer {
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint mRectPaint = new Paint();
    private final Paint mMaskPaint = new Paint();
    private final Rect mRectSrc = new Rect();
    private final Rect mRectDst = new Rect();
    private final Rect mRectCrop = new Rect(); // crop框默认为居中的正方形
    private float mAspectQuotient;
    private Bitmap mBitmap = null;
    private ZoomState mZoomState = null;
    private boolean mIsCropMode = false;

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && mZoomState != null) {
            int viewWidth = this.getWidth();
            int viewHeight = this.getHeight();
            int bitmapWidth = mBitmap.getWidth();
            int bitmapHeight = mBitmap.getHeight();
            float panX = mZoomState.getPanX();
            float panY = mZoomState.getPanY();

            float zoomX = getFitZoom();
            float zoomY = zoomX;

            // Setup source and destination rectangles
            mRectSrc.left = (int) ((bitmapWidth / 2 - panX * viewWidth / zoomX));
            mRectSrc.top = (int) ((bitmapHeight / 2 - panY * viewHeight / zoomY));
            mRectSrc.right = (int) (mRectSrc.left + viewWidth / zoomX);
            mRectSrc.bottom = (int) (mRectSrc.top + viewHeight / zoomY);
            // mRectDst的坐标值是相对于其本身的view，而getLeft()等接口获取的值是相对于屏幕的。
            // 要经过转换
            mRectDst.left = 0;
            mRectDst.top = 0;
            mRectDst.right = this.getRight() - this.getLeft();
            mRectDst.bottom = this.getBottom() - this.getTop();

            // 滑出屏幕左侧
            if (mRectSrc.left > bitmapWidth) {
                mRectSrc.left = bitmapWidth - 2;
                mZoomState.setPanX(-zoomX / 2 * bitmapWidth / viewWidth);
            }
            // 滑出屏幕右侧
            if (mRectSrc.right < 0) {
                mRectSrc.right = 2;
                mZoomState.setPanX(1 + zoomX / 2 * bitmapWidth / viewWidth);
            }
            // 滑出屏幕上方
            if (mRectSrc.top > bitmapHeight) {
                mRectSrc.top = bitmapHeight - 2;
                mZoomState.setPanY(-zoomY / 2 * bitmapHeight / viewHeight);
            }
            // 滑出屏幕下方
            if (mRectSrc.bottom < 0) {
                mRectSrc.bottom = 2;
                mZoomState.setPanY(1 + zoomY / 2 * bitmapHeight / viewHeight);
            }

            // Adjust source rectangle so that it fits within the source image.
            if (mRectSrc.left < 0) {
                mRectDst.left += -mRectSrc.left * zoomX;
                mRectSrc.left = 0;
            }
            if (mRectSrc.right > bitmapWidth) {
                mRectDst.right -= (mRectSrc.right - bitmapWidth) * zoomX;
                mRectSrc.right = bitmapWidth;
            }
            if (mRectSrc.top < 0) {
                mRectDst.top += -mRectSrc.top * zoomY;
                mRectSrc.top = 0;
            }
            if (mRectSrc.bottom > bitmapHeight) {
                mRectDst.bottom -= (mRectSrc.bottom - bitmapHeight) * zoomY;
                mRectSrc.bottom = bitmapHeight;
            }

            // 把bitmap中被mRectSrc指定的区域draw到mRectDst指定的区域中
            canvas.drawBitmap(mBitmap, mRectSrc, mRectDst, mPaint);

            if (mIsCropMode) {
                if (viewWidth < viewHeight) {
                    canvas.drawRect(0, 0, mRectCrop.right, mRectCrop.top, mMaskPaint);
                    canvas.drawRect(0, mRectCrop.bottom, mRectCrop.right, viewHeight, mMaskPaint);
                    canvas.drawRect(mRectCrop.left + 2, mRectCrop.top, mRectCrop.right - 2,
                            mRectCrop.bottom, mRectPaint);
                } else {
                    canvas.drawRect(0, 0, mRectCrop.left, mRectCrop.bottom, mMaskPaint);
                    canvas.drawRect(mRectCrop.right, 0, viewWidth, mRectCrop.bottom, mMaskPaint);
                    canvas.drawRect(mRectCrop.left + 2, mRectCrop.top, mRectCrop.right - 2,
                            mRectCrop.bottom, mRectPaint);
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        init();
    }

    public void setZoomState(ZoomState zoomState) {
        if (mZoomState != null) {
            mZoomState.deleteObserver(this);
        }
        mZoomState = zoomState;
        mZoomState.addObserver(this);
        invalidate();
    }

    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        this.calculateAspectQuotient();
        invalidate();
    }

    public void setCropMode(boolean isCropMode) {
        mIsCropMode = isCropMode;
        invalidate();
    }

    public void releaseBitmap() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public Bitmap getCropBitmap(int width, int height) {
        Bitmap bm = null;

        if (mBitmap != null && mZoomState != null) {
            try {
                bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                bm = null;
            }
            float zoomX = getFitZoom();
            float zoomY = zoomX;

            // mRectCrop和mRectDst重叠部分区域，即落在mRectCrop中的图片显示区域
            Rect intersectRect = new Rect();
            if (intersectRect.setIntersect(mRectCrop, mRectDst)) {
                int leftOffset = intersectRect.left - mRectDst.left;
                int rightOffset = intersectRect.right - mRectDst.right;
                int topOffset = intersectRect.top - mRectDst.top;
                int bottomOffset = intersectRect.bottom - mRectDst.bottom;

                // 与intersectRect对应的Bitmap中区域，即数据源区域
                Rect srcRect = new Rect();
                srcRect.left = (int) (mRectSrc.left + leftOffset / zoomX);
                srcRect.right = (int) (mRectSrc.right + rightOffset / zoomX);
                srcRect.top = (int) (mRectSrc.top + topOffset / zoomY);
                srcRect.bottom = (int) (mRectSrc.bottom + bottomOffset / zoomY);

                // 目标区域，坐标相对于mRectCrop而言
                Rect dstRect = new Rect(intersectRect);
                dstRect.left -= mRectCrop.left;
                dstRect.right -= mRectCrop.left;
                dstRect.top -= mRectCrop.top;
                dstRect.bottom -= mRectCrop.top;

                // 坐标缩放，从mRectCrop的坐标系到bm的坐标系
                float ratio = Math.min((float) width / (mRectCrop.right - mRectCrop.left),
                        (float) height / (mRectCrop.bottom - mRectCrop.top));
                dstRect.top *= ratio;
                dstRect.bottom *= ratio;
                dstRect.left *= ratio;
                dstRect.right *= ratio;

                Canvas canvas = new Canvas(bm);
                canvas.drawBitmap(mBitmap, srcRect, dstRect, mPaint);
            }
        }

        return bm;
    }

    @Override
    public void update(Observable observable, Object data) {
        this.invalidate();
    }

    /**
     * 初始状态，即mZoomState.getZoom()为1.0时 若为crop模式，返回zoom值使图片fit out到crop框中
     * 若为普通模式，返回zoom值使图片fit in到整个显示区域
     * 
     * @return
     */
    private float getFitZoom() {
        float fitZoom = 1.0f;
        if (mBitmap != null && mZoomState != null) {
            int viewWidth = this.getWidth();
            int viewHeight = this.getHeight();
            int bitmapWidth = mBitmap.getWidth();
            int bitmapHeight = mBitmap.getHeight();

            if (mIsCropMode) {
                float zoom = mZoomState.getZoom();
                float aspectQuotient = (float) bitmapWidth / bitmapHeight;
                fitZoom = Math.max(zoom, zoom * aspectQuotient) * viewWidth / bitmapWidth;
            } else {
                fitZoom = mZoomState.getZoomX(mAspectQuotient) * viewWidth / bitmapWidth;
            }
        }
        return fitZoom;
    }

    private void calculateAspectQuotient() {
        if (mBitmap != null) {
            mAspectQuotient = (((float) mBitmap.getWidth() / mBitmap.getHeight()) / ((float) this
                    .getWidth() / this.getHeight()));
        }
    }

    private void init() {
        this.calculateAspectQuotient();

        mMaskPaint.setStyle(Paint.Style.FILL);
        mMaskPaint.setColor(this.getResources().getColor(R.color.halftransparent));

        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(Color.WHITE);
        mRectPaint.setStrokeWidth(4);

        if (this.getWidth() < this.getHeight()) {
            mRectCrop.left = 0;
            mRectCrop.top = (this.getHeight() - this.getWidth()) / 2;
            mRectCrop.right = this.getWidth();
            mRectCrop.bottom = mRectCrop.top + this.getWidth();
        } else {
            mRectCrop.left = (this.getWidth() - this.getHeight()) / 2;
            mRectCrop.top = 0;
            mRectCrop.right = mRectCrop.left + this.getHeight();
            mRectCrop.bottom = this.getHeight();
        }
    }
}
