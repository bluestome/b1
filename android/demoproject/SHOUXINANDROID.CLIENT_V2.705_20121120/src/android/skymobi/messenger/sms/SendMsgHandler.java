
package android.skymobi.messenger.sms;

import android.app.PendingIntent;
import android.content.Intent;
import android.skymobi.messenger.MainApp;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

public class SendMsgHandler implements Runnable {

    private Message msg = null;
    private static final String TAG = SendMsgHandler.class.getSimpleName();
    public final static String ACTION_SENT = "SMSN.ACTION.SMS_SENT";
    public final static String SEQUENCE_KEY = "__MSG_SEQUENCE__";

    public SendMsgHandler(Message msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            Intent sent = new Intent();
            sent.setAction(SendMsgHandler.ACTION_SENT);
            sent.putExtra(SEQUENCE_KEY, msg.getSeq());
            PendingIntent sentIntent = PendingIntent.getBroadcast(MainApp.i(), 0, sent,
                    PendingIntent.FLAG_ONE_SHOT);
            ArrayList<String> contents = SmsManager.getDefault().divideMessage(msg.getContent());
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            for (int i = 0; i < contents.size(); i++) {
                sentIntents.add(sentIntent);
                Log.i(TAG, "contents = " + contents.get(i));
            }
            SmsManager.getDefault().sendMultipartTextMessage(msg.getTo(), null, contents,
                    sentIntents, null);
            Log.i(TAG, "sendMultipartTextMessage msg.getSeq() = " + msg.getSeq()
                    + " ,msg.getTo() = " + msg.getTo());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
