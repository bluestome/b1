
package android.skymobi.messenger.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

/**
 * @ClassName: HandClock
 * @Description:
 * @author Sean.Xie
 * @date 2012-7-31 下午7:43:22
 */
public class DrawClock extends View {

    private int time = 0;
    private int widthBg = 0;
    private int heightBg = 0;
    private long currentTime = 0;

    Handler handler = new Handler();

    public DrawClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 只取宽高，不解码
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.clockbg, opts);
        widthBg = opts.outWidth;
        heightBg = opts.outHeight;
        SLog.d("ClockView", "widthBg = " + widthBg + "heightBg = " + heightBg);
        setBackgroundResource(R.drawable.clockbg);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setColor(0xFF979899);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        drawClockPointer(canvas, paint); // 画制时钟的指针
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthBg, heightBg);
    }

    /**
     * 时钟指针画制函数
     * 
     * @param canvas
     * @param paint
     */
    private void drawClockPointer(Canvas canvas, Paint paint) {
        int px = getMeasuredWidth();
        int py = getMeasuredHeight();

        /*-------------------------获得当前时间小时和分钟数---------------------*/
        int mHour;
        int mMinutes;
        mHour = time / 60;
        mMinutes = time % 60;
        /*-------------------------获得当前时间---------------------*/

        float hDegree = (mHour + (float) mMinutes / 60) / 12 * 360;
        float mDegree = mMinutes / 60f * 360;

        // 分针－－－－－－－－－－－
        canvas.save();
        canvas.rotate(mDegree, px / 2, py / 2);
        Path path1 = new Path();
        path1.moveTo(px / 2, py / 2);
        path1.lineTo(px / 2, (int) (py / 5f));
        canvas.drawPath(path1, paint);
        canvas.restore();

        // 时针－－－－－－－－－－－－－－－－－－
        canvas.save();
        canvas.rotate(hDegree, px / 2, py / 2);
        Path path2 = new Path();
        path2.moveTo(px / 2, py / 2);
        path2.lineTo(px / 2, (py * 2 / 7));
        canvas.drawPath(path2, paint);
        canvas.restore();
        time += 1;

        if (currentTime != 0) {
            final Calendar mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(currentTime);
            int hour = mCalendar.get(Calendar.HOUR);
            int minutes = mCalendar.get(Calendar.MINUTE);
            if (mHour == hour && mMinutes == minutes) {
                return;
            }
        }
        // SLog.d("clock", "draw clock......");
        handler.postDelayed(r, 25);
    }

    private final Runnable r = new Runnable() {

        @Override
        public void run() {
            invalidate();
        }
    };

    public void startAnimation() {
        handler.post(r);
    }

    public void stopAnimation() {
        handler.removeCallbacks(r);
    }

    public void setTime(long time) {
        currentTime = time;
    }
}
