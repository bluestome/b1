
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.ui.BaseActivity;
import android.view.MotionEvent;
import android.view.View;

/**
 * @ClassName: BaseAction
 * @author Sean.Xie
 * @date 2012-2-24 下午12:30:19
 */
public class BaseAction {

    protected BaseActivity activity;
    protected CoreService mService;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param activity
     */
    public BaseAction(BaseActivity activity) {
        this.activity = activity;
        mService = CoreService.getInstance();
    }

    /**
     * @param intent
     */
    protected void startActivity(Intent intent) {
        activity.startActivity(intent);
    }

    /**
     * @param dialogLoginCode
     */
    protected void showDialog(int dialogLoginCode) {
        activity.showDialog(dialogLoginCode);
    }

    /**
     * @param loginNull
     */
    protected void showToast(int tip) {
        activity.showToast(tip);
    }

    /**
     * 
     */
    protected void finish() {
        activity.finish();
    }

    /**
     * Touch 事件背景改变
     * 
     * @param view 组件
     * @param action 行为
     * @param downBG 按下背景
     * @param upBG 弹起背景
     */
    protected void setTouchBackground(View view, int action, int downBG, int upBG) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                view.setBackgroundResource(downBG);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.setBackgroundResource(upBG);
                break;
        }
    }
}
