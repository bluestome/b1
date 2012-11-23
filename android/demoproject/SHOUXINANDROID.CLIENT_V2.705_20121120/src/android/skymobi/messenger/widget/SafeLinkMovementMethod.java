
package android.skymobi.messenger.widget;

import android.content.ActivityNotFoundException;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @ClassName: SafeLinkMovementMethod
 * @Description: catch the ActivityNotFoundException to avoid crash
 * @author Lv.Lv
 * @date 2012-6-7 下午4:43:20
 */
public class SafeLinkMovementMethod extends LinkMovementMethod {
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        boolean res = false;
        try {
            res = super.onTouchEvent(widget, buffer, event);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static SafeLinkMovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new SafeLinkMovementMethod();

        return sInstance;
    }

    private static SafeLinkMovementMethod sInstance = null;
}
