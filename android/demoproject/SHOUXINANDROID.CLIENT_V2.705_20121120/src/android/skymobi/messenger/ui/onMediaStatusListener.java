
package android.skymobi.messenger.ui;

/**
 * @ClassName: onMediaStatusListener
 * @Description: TODO
 * @author Michael.Pan
 * @date 2012-3-26 下午04:34:17
 */
public interface onMediaStatusListener {
    /**
     * 播放对应语音完成
     * 
     * @param path
     * @param position
     */
    public void onPlayCompletion(String path, int position);

    /**
     * 开始播放指定语音
     * 
     * @param path
     * @param position
     */
    public void onPlayStart(String path, int position);

    /**
     * 录音的音量改变
     * 
     * @param level
     */
    public void onRecordSoundChanged(int level);

    /**
     * 录音倒计时,当录音最后10s开始倒计时(录音最长60s)
     * 
     * @param second
     */
    public void onSecondChanged(int second);
}
