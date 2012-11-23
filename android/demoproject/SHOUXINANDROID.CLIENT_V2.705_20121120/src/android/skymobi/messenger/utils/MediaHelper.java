
package android.skymobi.messenger.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.os.SystemClock;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.ui.onMediaStatusListener;

import java.io.IOException;
import java.util.Calendar;

/**
 * @ClassName: MediaHelper
 * @Description: 媒体录制和播放
 * @author Michael.Pan
 * @date 2012-3-20 下午03:29:59
 */
public class MediaHelper implements OnCompletionListener {

    private static Object classLock = HeaderCache.class;
    private static MediaHelper sInstance = null;

    // 播放
    private MediaPlayer mediaPlayer = null;
    private onMediaStatusListener mListener = null;
    private String mCurPlayPath = ""; // 当前播放语音的路径
    private int mCurPostion = -1; // 当前播放语音在列表中的位置

    // 录音
    private MediaRecorder mediaRecoder = null;
    private long startRecordTime = 0;
    private long stopRecordTime = 0;
    private String lastRecordPath = null; // 最后一次录音的位置
    private volatile boolean isRecordRunning = false;

    public static MediaHelper getInstance() {
        synchronized (classLock) {
            if (sInstance == null) {
                sInstance = new MediaHelper();
            }
            return sInstance;
        }
    }

    private MediaHelper() {
        mCurPlayPath = ""; // 当前播放语音的路径
        mCurPostion = -1; // 当前播放语音在列表中的位置
    }

    public String startRecordVoice(long curtime) {
        lastRecordPath = MainApp.i().createNewSoundFile(String.valueOf(curtime));
        if (lastRecordPath == null) {
            return lastRecordPath;
        }
        if (mediaRecoder != null) {
            mediaRecoder.release();
            mediaRecoder = null;
        }

        mediaRecoder = new MediaRecorder();
        isRecordRunning = true;
        startRecordTime = curtime;
        // mediaRecoder.reset();
        mediaRecoder.setAudioSource(AudioSource.MIC);
        mediaRecoder.setOutputFormat(OutputFormat.RAW_AMR);
        mediaRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecoder.setOutputFile(lastRecordPath);
        // 录音进度线程
        new MaxAmplitudeThread().start();
        // 录音倒计时
        new CountDownThread().start();

        try {
            mediaRecoder.prepare();
            mediaRecoder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopMediaPlayer();
        return lastRecordPath;
    }

    public long stopRecordVoice(long curtime) {
        if (!isRecordRunning) {
            return 0;
        }
        isRecordRunning = false;
        stopRecordTime = curtime;
        if (lastRecordPath != null && mediaRecoder != null) {
            try {
                mediaRecoder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecoder.release();
            mediaRecoder = null;
        }
        return (stopRecordTime - startRecordTime);
    }

    public void playVoice(String path, int position) {
        // 处理播放路径为空的情况
        if (path == null || position < 0) {
            return;
        }
        // 发通知结束掉上一段语音播放
        if (mListener != null)
            mListener.onPlayCompletion(mCurPlayPath, mCurPostion);

        // 如果和上次播放的文件路径相同，则表示暂停或者重新播放操作，否则表示播放另外一段语音
        if (path.equals(mCurPlayPath) && mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            // mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.setVolume(AudioManager.STREAM_MUSIC, AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.start();
                mListener.onPlayStart(path, position);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCurPlayPath = path;
        mCurPostion = position;
    }

    public void setLister(onMediaStatusListener listener) {
        mListener = listener;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mListener != null && mCurPostion != -1) {
            mListener.onPlayCompletion(mCurPlayPath, mCurPostion);
        }
    }

    public class MaxAmplitudeThread extends Thread implements Runnable {
        @Override
        public void run() {
            while (isRecordRunning) {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    int maxAmp = 0;
                    if (null != mediaRecoder)
                        maxAmp = mediaRecoder.getMaxAmplitude();

                    if (maxAmp > 1) {
                        // 最大值2^16 那么我们分成8级音量， 直接除以2^12即可
                        // int temp = softFilter(maxAmp);
                        mListener.onRecordSoundChanged(maxAmp >> 12);
                        // Log.e("MaxAmplitudeThread",
                        // "MaxAmplitudeThread is running = "
                        // + (maxAmp >> 12));
                    }
                } catch (IllegalStateException e) {

                } catch (RuntimeException re) {
                }

                SystemClock.sleep(50);
            }
        }
    }

    // 倒计时线程runnable
    public class CountDownThread extends Thread implements Runnable {
        @Override
        public void run() {
            while (isRecordRunning) {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                // 最长时间减去录音时间等于倒计时
                int second = (int) (Constants.MAX_RECORD_TIME - (Calendar.getInstance()
                        .getTimeInMillis() - startRecordTime) / 1000);
                if (second < Constants.COUNTDOWN_TIME)
                    mListener.onSecondChanged(second);
                SystemClock.sleep(1000);
            }
        }
    }

    public void release() {
        // 下面这行代码主要是处理
        // 针对部分机型可以在录音的情况下按返回键
        // 停止计时，这里有一个按住录音，再按返回键，导致计时继续的问题
        stopRecordVoice(0);
        stopMediaPlayer();

        if (mediaRecoder != null) {
            mediaRecoder.release();
            mediaRecoder = null;
        }
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();

            mediaPlayer.release();
            mediaPlayer = null;

        }
        if (mListener != null)
            mListener.onPlayCompletion(mCurPlayPath, mCurPostion);
    }

}
