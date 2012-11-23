
package android.skymobi.messenger.widget.fastchat;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.ui.action.FastChatAction;
import android.skymobi.messenger.utils.DateUtil;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @ClassName: MyScrollView 自定义的ScrollView
 * @Description:
 * @author Sean.Xie
 * @date 2012-10-19 下午4:13:24
 */
public class BackableScrollView extends ScrollView implements OnGestureListener {

    private static final long LIMIT_DELTA_TIME = 5 * 60 * 1000; // 5分钟

    private GestureDetector mGestureDetector = null;
    private Context mContext = null;
    private LinearLayout mLayoutMsgs = null;
    private final ArrayList<Message> msgList = new ArrayList<Message>();
    // 单个消息的点击事件
    private FastChatAction mListener = null;

    private Scroller mScroller;

    public BackableScrollView(Context context) {
        super(context);
        init(context);
    }

    public BackableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BackableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 初始化
     * 
     * @param context
     */
    private void init(Context context) {
        mContext = context;
        mGestureDetector = new GestureDetector(this);
        mScroller = new Scroller(mContext);
        mLayoutMsgs = (LinearLayout) findViewById(R.id.fastchat_msgs);
    }

    /**
     * 计算滚动高度
     * 
     * @return
     */
    private int getMeasureHeight() {
        int heightMeasureSpec;
        int rootHeight = MainApp.i().getDeviceInfo().screenHeight;
        if (rootHeight >= 800) {
            heightMeasureSpec = 460;
        } else if (rootHeight >= 480) {
            heightMeasureSpec = 260;
        } else {
            heightMeasureSpec = 160;
        }
        return heightMeasureSpec;
    }

    public void scrollDown() {
        int scrollHeight = getMeasureHeight();
        mLayoutMsgs.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int msgsH = mLayoutMsgs.getMeasuredHeight();
        if (msgsH - scrollHeight > 0) {
            isScrollEvent = false;
            mScroller.startScroll(0, mScroller.getFinalY(), 0,
                    msgsH - scrollHeight - mScroller.getFinalY(), 1500);
            invalidate();
        }else{
            mScroller.setFinalY(0);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mGestureDetector.onTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = mGestureDetector.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScrollEvent = false;
                        int finalY = mScroller.getFinalY();
                        // 滚动范围
                        int range = computeVerticalScrollRange() - getHeight();
                        if (range < 0) {
                            range = 0;
                        }
                        // 终点大于范围
                        if (finalY > range) {
                            mScroller.setFinalY(range);
                        } else if (finalY < 0) {
                            // 终点小于0
                            mScroller.setFinalY(0);
                        }
                        layout(0, 0, getWidth(), getHeight());
                    }
                }, 0);
                break;
        }
        return result;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        isScrollEvent = true;
        mScroller.startScroll(0, mScroller.getFinalY(), 0, (int) distanceY);
        invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    private boolean isScrollEvent;

    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int scrollHeight = getHeight();
            int childHeight = computeVerticalScrollRange();
            int maxScrollRange = childHeight - scrollHeight;
            if (maxScrollRange < 0) {
                maxScrollRange = 0;
            }
            int currY = mScroller.getCurrY();
            if (isScrollEvent) {
                // 底部越界
                if (maxScrollRange < currY) {
                    mScroller.abortAnimation();
                    int t = (maxScrollRange - currY) / 2;
                    layout(0, t, getWidth(), t + scrollHeight);
                } else if (0 > currY) {
                    // 上部越界
                    mScroller.abortAnimation();
                    int t = (-currY) / 2;
                    layout(0, t, getWidth(), t + scrollHeight);
                } else {
                    scrollTo(0, currY);
                }
            } else {
                if (maxScrollRange >= currY && 0 <= currY) {
                    scrollTo(0, currY);
                } else {
                    mScroller.abortAnimation();
                    layout(0, 0, getWidth(), scrollHeight);
                }
            }
            postInvalidate();
        }
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
            final float velocityY) {
        int scrollHeight = getHeight();
        int childHeight = computeVerticalScrollRange();
        int maxScrollRange = childHeight - scrollHeight;
        if (maxScrollRange > 0) {
            mScroller.fling(0, mScroller.getFinalY(), 0, -(int) velocityY, 0, 0, 0,
                    maxScrollRange);
            isScrollEvent = false;
            invalidate();
        }
        return false;
    }

    // 更新消息列表
    public void refreshList(ArrayList<Message> list) {
        getLayoutParams().height = getMeasureHeight();
        requestLayout();
        msgList.clear();
        msgList.addAll(list);
        mLayoutMsgs = (LinearLayout) findViewById(R.id.fastchat_msgs);
        mLayoutMsgs.removeAllViews();
        for (int i = 0; i < msgList.size(); i++) {
            View childView = LayoutInflater.from(mContext).inflate(R.layout.fastchat_msg, null);
            // 对view的渲染部分
            TextView timeTV = (TextView) childView.findViewById(R.id.fast_chat_time);
            LinearLayout lyFrom = (LinearLayout) childView.findViewById(R.id.fastchat_msg_you);
            LinearLayout lyTo = (LinearLayout) childView.findViewById(R.id.fastchat_msg_me);
            Button fromBT = (Button) childView.findViewById(R.id.fastchat_from_content);
            ImageView readStatusIV = (ImageView) childView
                    .findViewById(R.id.fastchat_from_read_status);
            Button toBT = (Button) childView.findViewById(R.id.fastchat_to_content);

            TextView lengthFromTV = (TextView) childView
                    .findViewById(R.id.fastchat_from_voice_length);
            TextView lengthToTV = (TextView) childView
                    .findViewById(R.id.fastchat_to_voice_length);
            ClockView sendClockView = (ClockView) childView
                    .findViewById(R.id.fastchat_sending_status);
            ImageView sendStatusIV = (ImageView) childView.findViewById(R.id.fastchat_send_status);

            Message msg = msgList.get(i);
            checkTimeTV(timeTV, i);
            if (msg.getOpt() == MessagesColumns.OPT_FROM) {
                // 收到的语音消息
                lyFrom.setVisibility(View.VISIBLE);
                lyTo.setVisibility(View.INVISIBLE);
                lengthFromTV.setVisibility(View.VISIBLE);
                lengthFromTV.setText(msg.getResFile().getLength() + "\"");
                if (msg.getRead() == MessagesColumns.READ_NO) {
                    readStatusIV.setVisibility(View.VISIBLE);
                } else {
                    readStatusIV.setVisibility(View.GONE);
                }
                fromBT.setOnClickListener(mListener);
                fromBT.setOnCreateContextMenuListener(mListener);
                fromBT.setTag(i);
            } else {
                // 发送到语音消息
                lyFrom.setVisibility(View.INVISIBLE);
                lyTo.setVisibility(View.VISIBLE);
                lengthToTV.setVisibility(View.VISIBLE);
                lengthToTV.setText(msg.getResFile().getLength() + "\"");
                if (msg.getStatus() == MessagesColumns.STATUS_SUCCESS) {
                    sendClockView.setVisibility(View.GONE);
                    sendStatusIV.setVisibility(View.GONE);
                } else if (msg.getStatus() == MessagesColumns.STATUS_SENDING) {
                    sendClockView.setVisibility(View.VISIBLE);
                    sendClockView.startAnimation();
                    sendStatusIV.setVisibility(View.GONE);
                } else {
                    sendClockView.setVisibility(View.GONE);
                    sendStatusIV.setVisibility(View.VISIBLE);
                }
                toBT.setOnClickListener(mListener);
                toBT.setOnCreateContextMenuListener(mListener);
                toBT.setTag(i);
            }
            // 添加到父view中
            mLayoutMsgs.addView(childView);
        }
    }

    /**
     * 更新时间
     * 
     * @param tv
     * @param position
     */
    private void checkTimeTV(TextView tv, int position) {
        TextView tvTime = tv;
        if (tv == null) {
            return;
        }
        long lastMsgTime = (position == 0) ? 0 : msgList.get(position -
                1).getDate();
        long time = msgList.get(position).getDate();

        // 5分钟之内就不显示时间
        if (time - lastMsgTime > LIMIT_DELTA_TIME) {
            tvTime.setVisibility(View.VISIBLE);
            tvTime.setText(DateUtil.formatTimeForChatList(mContext, time));
        } else {
            tvTime.setVisibility(View.GONE);
        }
    }

    // 设置ITEM点击事件监听器
    public void setClickListener(FastChatAction listener) {
        mListener = listener;
    }

    // 启动ITEM上语音播放动画,
    public void startVoicePlayAnimation(final int pos, final Message msg) {
        View itemView = mLayoutMsgs.getChildAt(pos);
        if (itemView == null)
            return;
        ImageView ivVoicePlay = null;
        if (msg.getOpt() == MessagesColumns.OPT_FROM) {
            ivVoicePlay = (ImageView) itemView.findViewById(R.id.fastchat_from_voice_play);
        } else {
            ivVoicePlay = (ImageView) itemView.findViewById(R.id.fastchat_to_voice_play);
        }
        if (ivVoicePlay == null)
            return;
        AnimationDrawable anim = null;
        if (msg.getOpt() == MessagesColumns.OPT_FROM) {
            anim = (AnimationDrawable) mContext.getResources()
                    .getDrawable(R.drawable.fastchat_voice_from_image);
        } else {
            anim = (AnimationDrawable) mContext.getResources()
                    .getDrawable(R.drawable.fastchat_voice_to_image);
        }
        if (anim == null)
            return;
        ivVoicePlay.setBackgroundDrawable(anim);
        anim.start();
    }

    // 结束ITEM上语音播放动画
    public void stopVoicePlayAnimation(final int pos, final Message msg) {
        View itemView = mLayoutMsgs.getChildAt(pos);
        if (itemView == null)
            return;
        ImageView ivVoicePlay = null;
        if (msg.getOpt() == MessagesColumns.OPT_FROM) {
            ivVoicePlay = (ImageView) itemView.findViewById(R.id.fastchat_from_voice_play);
        } else {
            ivVoicePlay = (ImageView) itemView.findViewById(R.id.fastchat_to_voice_play);
        }
        if (ivVoicePlay == null)
            return;
        if (msg.getOpt() == MessagesColumns.OPT_FROM) {
            ivVoicePlay.setBackgroundResource(R.drawable.fastchat_from_voice_play);
        } else {
            ivVoicePlay.setBackgroundResource(R.drawable.fastchat_to_voice_play);
        }
    }

}
