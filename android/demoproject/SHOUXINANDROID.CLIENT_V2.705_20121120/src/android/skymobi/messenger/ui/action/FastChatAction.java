
package android.skymobi.messenger.ui.action;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.bean.ResFile;
import android.skymobi.messenger.bizunit.fastchat.FastChatBU;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.FastChatActivity;
import android.skymobi.messenger.ui.onMediaStatusListener;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.MediaHelper;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.widget.fastchat.MicrophoneView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @ClassName: FastChatAction
 * @Description:
 * @author Sean.Xie
 * @date 2012-10-18 下午3:10:36
 */
public class FastChatAction extends BaseAction implements OnClickListener, OnTouchListener,
        onMediaStatusListener, MicrophoneView.OnFinishListener, SensorEventListener,
        OnCreateContextMenuListener {

    private FastChatBU fastChatBU;
    private Handler fastHandler;
    // 当前语音模式
    private boolean mVoicePlay = false; // true : 有语音播放， false：没有语音在播放
    // 音频管理器
    private AudioManager mAudioMgr;
    // 传感器管理器
    private SensorManager mSensorMgr;
    private Sensor mSensor;
    // 距离传感器的判断阈值
    private float maxSensorRange = 4.0f;

    /**
     * @param activity
     */
    public FastChatAction(BaseActivity activity) {
        super(activity);
        initSensor();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fastchat_guide_start:
                activity.findViewById(R.id.fastchat_guide_start).setVisibility(View.GONE);
                MainApp.saveFastChatSended(true);
                break;
            case R.id.fastchat_guide:
                CommonPreferences.saveFastChatUsed(true);
                activity.findViewById(R.id.fastchat_guide).setVisibility(View.GONE);
                ((FastChatActivity) activity).updateTitleBtn();
                fastChatBU.applyFastChat(SettingsPreferences.getSex());
                break;
            case R.id.topbar_btn_rightII:
                // 测试结束快聊
                int resID = (Integer) v.getTag();
                if (resID == R.string.fastchat_btn_refresh) {
                    fastChatBU.applyFastChat(SettingsPreferences.getSex());
                } else if (resID == R.string.fastchat_btn_end) {
                    showDialog(FastChatActivity.DIALOG_LEAVE);
                }
                break;
            case R.id.fastchat_from_content:
            case R.id.fastchat_to_content:
                // 消息点击
                int pos = (Integer) v.getTag();
                onClickVoiceItem(pos);
                SLog.d("fastchat", "点击消息位置 pos = " + pos);
                break;
            default:
                break;
        }
    }

    public void setFastChatBu(FastChatBU bizBu, Handler handler) {
        fastChatBU = bizBu;
        fastHandler = handler;
    }

    private String mRecordPath = null;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // 没有匹配 成功 提示用户刷新 重现匹配
                if (MainApp.getFastChatCache().getMatchedSkyid() <= 0) {
                    showToast(R.string.fastchat_cannot_send_voice);
                    return false;
                }
                ((FastChatActivity) activity).getMicrophoneView().setTouchStatus(true);
                mRecordPath = recodeVoice(); // 开始录音
                showRecordBoard(true);
            }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                ((FastChatActivity) activity).getMicrophoneView().setTouchStatus(false);
                sendVoice(); // 结束录音并且上传，发送
                showRecordBoard(false);
            }
                break;
            default:
                break;
        }
        return true;
    }

    public static final int LIMIT_LOW_RECORD_TIME = 1000;

    private void sendVoice() {
        SLog.i("fastchat", "sendVoice...");
        long curtime = System.currentTimeMillis();
        long recordTime = MediaHelper.getInstance().stopRecordVoice(curtime);
        // 处理录音文件不存在的情况,发送完成后path置为空
        if (mRecordPath == null) {
            // showToast(R.string.no_sdcard_mount);
            return;
        }
        // 处理录音文件太短的情况
        if (recordTime < LIMIT_LOW_RECORD_TIME) {
            // fix bug: http://redmine.sky-mobi.com/redmine/issues/13337
            if (recordTime > 0) {
                showToast(R.string.chat_record_voice_too_short);
            }
            return;
        }

        ResFile file = new ResFile();
        file.setPath(mRecordPath);
        file.setLength((int) recordTime / 1000);
        file.setVersion(ResFile.VERSION);
        file.setFormat(Constants.VOICE_EXT_NAME);
        file.setId(mService.getMessageModule().addResFile(file));

        android.skymobi.messenger.bean.Message msg = new android.skymobi.messenger.bean.Message();
        msg.setContent(String.valueOf(file.getId()));
        msg.setDate(System.currentTimeMillis());
        msg.setOpt(MessagesColumns.OPT_TO);
        msg.setRead(MessagesColumns.READ_YES);
        msg.setType(MessagesColumns.TYPE_VOICE);
        msg.setStatus(MessagesColumns.STATUS_SENDING);
        msg.setResFile(file);
        MainApp.getFastChatCache().addChatMsg(msg);
        int destSkyid = MainApp.getFastChatCache().getMatchedSkyid();
        SLog.i("fastchat", "destSkyid = " + destSkyid);
        SLog.i("fastchat", "mRecordPath = " + mRecordPath);
        fastChatBU.sendFastChatVoice(msg, file, destSkyid);
        mRecordPath = null; // 发送完成后文件路径置空
    }

    private String recodeVoice() {
        SLog.i("fastchat", "recodeVoice...");
        long curtime = System.currentTimeMillis();
        return MediaHelper.getInstance().startRecordVoice(curtime);
    }

    private void showRecordBoard(boolean bVisiable) {
        ImageView mRecordBoardIV = (ImageView) activity.findViewById(R.id.chat_record_board_iv);
        TextView mRecordBoardTV = (TextView) activity.findViewById(R.id.chat_record_board_tv);
        if (mRecordBoardIV != null) {
            mRecordBoardIV.setVisibility(bVisiable ? View.VISIBLE : View.GONE);
        }
        if (mRecordBoardTV != null && !bVisiable) {
            mRecordBoardTV.setVisibility(View.GONE);
        }
    }

    private final int[] resID = {
            R.drawable.record_process_1,
            R.drawable.record_process_2,
            R.drawable.record_process_3,
            R.drawable.record_process_4,
            R.drawable.record_process_5,
            R.drawable.record_process_6,
            R.drawable.record_process_7,
            R.drawable.record_process_8,
            R.drawable.record_process_9,
    };

    // 播放语音结束回调
    @Override
    public void onPlayCompletion(String path, int position) {
        ArrayList<Message> msgs = MainApp.getFastChatCache().getChatMsg();
        if (msgs.size() < 1 || position < 0 || position >= msgs.size() || mService == null)
            return;
        Message msg = msgs.get(position);
        // 停止播放动画
        ((FastChatActivity) activity).getMyScrollView().stopVoicePlayAnimation(position, msg);
        mVoicePlay = false;
        mAudioMgr.setMode(AudioManager.MODE_NORMAL); // 打开扬声器
        showHhalfBoard(false);
    }

    // 播放语音开始回调
    @Override
    public void onPlayStart(String path, int position) {
        ArrayList<Message> msgs = MainApp.getFastChatCache().getChatMsg();
        if (msgs.size() < 1 || position < 0 || position >= msgs.size() || mService == null)
            return;
        Message msg = msgs.get(position);
        msg.setRead(MessagesColumns.READ_YES);
        // 刷新状态
        ((FastChatActivity) activity).getMyScrollView().refreshList(
                MainApp.getFastChatCache().getChatMsg());
        ((FastChatActivity) activity).getMyScrollView().startVoicePlayAnimation(position, msg);
        mVoicePlay = true;
    }

    @Override
    public void onRecordSoundChanged(final int level) {
        fastHandler.post(new Runnable() {

            @Override
            public void run() {
                ImageView mRecordBoardIV = (ImageView) activity
                        .findViewById(R.id.chat_record_board_iv);
                if (mRecordBoardIV == null || mRecordBoardIV.getVisibility() == View.GONE) {
                    return;
                }
                mRecordBoardIV.setImageResource(resID[level]);
            }
        });
    }

    @Override
    public void onSecondChanged(final int second) {
        fastHandler.post(new Runnable() {

            @Override
            public void run() {
                if (second > 0) {
                    String msg = activity.getResources().getString(
                            R.string.chat_record_voice_too_long,
                            new Object[] {
                                String.valueOf(second)
                            });
                    TextView mRecordBoardTV = (TextView) activity
                            .findViewById(R.id.chat_record_board_tv);
                    mRecordBoardTV.setVisibility(View.VISIBLE);
                    mRecordBoardTV.setText(msg);
                } else {
                    showRecordBoard(false);
                    sendVoice();
                }

            }
        });

    }

    @Override
    public void onFinish(int result, final int[] position) {
        if (result == MicrophoneView.FALL_INVIEW) {
            activity.findViewById(R.id.fastchat_scrollview).setVisibility(View.VISIBLE);
            if (!MainApp.isFastChatSended()) {
                activity.findViewById(R.id.fastchat_guide_start).setVisibility(View.VISIBLE);
            }
        } else if (result == MicrophoneView.FALL_OUTVIEW_MATCHFAIL) {
            // 匹配失败
            showDialog(FastChatActivity.DIALOG_MATCHFAIL);
        } else if (result == MicrophoneView.FALL_OUTVIEW_NET_ERROR) {
            // 联网失败
            showDialog(FastChatActivity.DIALOG_NET_ERROR);
        }
    }

    private void onClickVoiceItem(int position) {
        ArrayList<Message> msgs = MainApp.getFastChatCache().getChatMsg();
        if (msgs.size() < 1 || position < 0 || mService == null)
            return;
        Message msg = msgs.get(position);
        String path = mService.getMessageModule().getPathByContent(msg.getContent());
        MediaHelper.getInstance().playVoice(path, position);
    }

    /**
     * 初始化传感器
     */
    private void initSensor() {
        mAudioMgr = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mAudioMgr.setMode(AudioManager.MODE_NORMAL);
        mSensorMgr = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        maxSensorRange = mSensor.getMaximumRange() / 2;

    }

    public void registerSensor() {
        mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor() {
        mSensorMgr.unregisterListener(this);
    }

    // 打开半透明挡板（1.屏幕变暗 2.脸部贴近屏幕也不会发生误操作）
    private void showHhalfBoard(boolean isOpen) {
        if (isOpen) {
            activity.findViewById(R.id.fastchat_hhalf_board).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.fastchat_hhalf_board).setVisibility(View.GONE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!mVoicePlay)
            return;
        // >maxSensorRange 为远离耳边 , <maxSensorRange为接近耳边

        if (event.values[0] <= maxSensorRange) {
            mAudioMgr.setMode(AudioManager.MODE_IN_CALL); // 听筒模式
            showHhalfBoard(true);
        } else {
            mAudioMgr.setMode(AudioManager.MODE_NORMAL); // 打开扬声器
            showHhalfBoard(false);
        }
        SLog.i("fastchat", "mSensor.getMaximumRange() = " + maxSensorRange);
        SLog.i("fastchat", "onSensorChanged event.values[0] = " + event.values[0]);
        SLog.i("fastchat", "onSensorChanged event.accuracy = " + event.accuracy);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do notings
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        activity.onCreateContextMenu(menu, v, menuInfo);
    }
}
