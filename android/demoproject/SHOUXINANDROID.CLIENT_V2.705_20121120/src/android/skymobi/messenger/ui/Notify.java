
package android.skymobi.messenger.ui;

/**
 * @ClassName: Notify
 * @Description: 定义notify类型等，UI界面收到notify后根据相应的notify类型进行相应处理
 * @author Michael.Pan
 * @date 2012-2-7 下午04:38:51
 */
public class Notify {
    public static final int NOTIFY_BASE = 0xF0000;
    // 激活成功
    public static final int NOTIFY_ACTIVATE_SUCCESS = NOTIFY_BASE + 1;
    // 激活失败
    public static final int NOTIFY_ACTIVATE_FAIL = NOTIFY_BASE + 2;
}
