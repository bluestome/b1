
package android.skymobi.messenger.sms;

import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.text.TextUtils;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 实现短信的收发功能
 */
public class MessageManager {
    public int send(String to, String content) {
        if (TextUtils.isEmpty(to) || TextUtils.isEmpty(content)) {
            Log.e(TAG, "send error!");
            return -1;
        }

        Message msg = new Message(this.getSequence(), to, content);
        // 加入到短信队列，并且可能发送下一条短信
        sendMsgs.add(msg);
        sendNextMsg();
        return msg.getSeq();
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public static MessageManager getInstance() {
        synchronized (classLock) {
            if (sInstance == null) {
                sInstance = new MessageManager();
                sInstance.init();
            }
            return sInstance;
        }
    }

    // 发送成功or失败
    protected void onSend(int sequence, int result) {
        stopCheckTimeout();
        if (this.listener != null) {
            this.listener.onSendSMS(sequence, result);
        }
        // 修改发送状态，并且发送下一条短信
        isSending = false;
        sendNextMsg();
        Log.i(TAG, "onSend (" + sequence + ", " + result + ")");
    }

    // 接收新的消息
    protected void onReceive(String from, String content, long receiveTime) {
        if (this.listener != null) {
            this.listener.onReceiveSMS(from, content, receiveTime);
        }
        Log.i(TAG, "onReceive (" + from + ", " + content + ", " + receiveTime + ")");
    }

    // 用于判断收到的消息发送�?是否有效
    protected boolean isValidated(String from) {
        // return toNumbers.contains(from);
        return true;
    }

    private MessageManager() {
        // nothing
    }

    private void init() {
        // 单一线程池，短信发送按顺序发送，保证前一个发送完成后，发送后面一条
        sendExes = Executors.newSingleThreadExecutor();
        sendMsgs = new Vector<Message>();
    }

    synchronized private int getSequence() {
        return sequence++;
    }

    // 发送下一条消息（必须满足两个条件：没有短信在发送，短信发送队列不为空）
    synchronized private void sendNextMsg() {
        if (sendMsgs.size() > 0 && !isSending) {
            Message msg = sendMsgs.remove(0);
            sendExes.execute(new SendMsgHandler(msg));
            isSending = true;
            curSmsSeq = msg.getSeq();
            stopCheckTimeout();
            startCheckTimeOut();
        }
    }

    private void startCheckTimeOut() {
        mCheckTimeout = new Timer();
        mCheckTimeout.schedule(new TimerTask() {

            @Override
            public void run() {
                Log.i(TAG, "-----TimeOut:" + curSmsSeq);
                if (listener != null) {
                    listener.onSendSMS(curSmsSeq, MessagesColumns.STATUS_FAILED);
                }
                // 修改发送状态，并且发送下一条短信
                isSending = false;
                sendNextMsg();

            }
        }, 15000);
    }

    private void stopCheckTimeout() {
        if (mCheckTimeout != null)
            mCheckTimeout.cancel();
    }

    private static MessageManager sInstance = null;
    private ExecutorService sendExes = null;
    // 发送短信队列
    private Vector<Message> sendMsgs = null;
    // 发送状态, true 表示正在发送，false表示空闲状态
    private volatile boolean isSending = false;
    private int sequence = 1;
    private final static String TAG = MessageManager.class.getSimpleName();
    private MessageListener listener = null;
    private static Object classLock = MessageManager.class;
    private Timer mCheckTimeout;
    private static int curSmsSeq;
}
