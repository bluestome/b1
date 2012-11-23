
package android.skymobi.messenger.sms;

public interface MessageListener {

    /**
     * @param sequence
     * @param result
     */
    void onSendSMS(int sequence, int result);

    /**
     * @param from
     * @param content
     * @param receiveTime
     */
    void onReceiveSMS(String from, String content, long receiveTime);
}
