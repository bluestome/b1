
package android.skymobi.messenger.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.CommonPreferences;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.TimeZone;

public class MessageReceiver extends BroadcastReceiver {
    public static final String ACTION_RECEIVE = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = MessageReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // 只有在登录成功的情况下才可以接收短信
        if (intent.getAction().equals(ACTION_RECEIVE) && !CommonPreferences.getLogoutedStatus()) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                Log.i(TAG, "receive msg.........");
                // 合并多条短信
                SmsMessage sms = msgs[0];
                StringBuilder body = new StringBuilder();

                for (SmsMessage msg : msgs) {
                    if (msg != null)
                        body.append(msg.getDisplayMessageBody());
                    Log.i(TAG, "body = " + body);
                }
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                long timestampMillis = sms.getTimestampMillis();
                if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                    TimeZone timeZone = TimeZone.getDefault();
                    Log.i(TAG, "-----getOffset:" + timeZone.getOffset(timestampMillis));
                    timestampMillis -= timeZone.getOffset(timestampMillis);
                }

                if (sms != null
                        && MessageManager.getInstance().isValidated(sms.getOriginatingAddress())) {
                    String from = AndroidSysUtils.removeHeader(sms.getOriginatingAddress());
                    MessageManager.getInstance().onReceive(from, body.toString(),
                            timestampMillis);
                }

            }
            abortBroadcast(); // 停发收到sms短信广播,否则导致数据库会写入收到两条短信
        } else if (intent.getAction().equals(SendMsgHandler.ACTION_SENT)) { // 发送回执信息
            int result = (getResultCode() == Activity.RESULT_OK) ? MessagesColumns.STATUS_SUCCESS
                    : MessagesColumns.STATUS_FAILED;
            Log.v(TAG, "result = " + result);
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int sequence = bundle.getInt(SendMsgHandler.SEQUENCE_KEY);
                MessageManager.getInstance().onSend(sequence, result);
            }
        }
    }
}
