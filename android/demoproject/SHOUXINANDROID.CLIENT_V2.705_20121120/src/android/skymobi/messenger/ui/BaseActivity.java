
package android.skymobi.messenger.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.ui.handler.AutoLoginHandler;
import android.skymobi.messenger.ui.handler.CheckBindHandler;
import android.skymobi.messenger.ui.handler.UpdateHandler;
import android.skymobi.messenger.utils.CommonPreferences;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @ClassName: BaseActivity
 * @Description: Activity 基类
 * @author Sean.Xie
 * @date 2012-2-7 下午3:29:41
 */
public class BaseActivity extends Activity implements Observer {

    protected static final String TIME_TAG = "Time-consuming";
    protected static final String TAG = BaseActivity.class.getSimpleName();

    protected CoreService mService = null;
    protected Context mContext = null;

    // 自动登录
    private final Handler autoLoginHandler = new AutoLoginHandler(this);
    private final Observer autoLoginCallback = new Observer() {
        @Override
        public void notifyObserver(int what, Object obj) {
            if (obj instanceof UserInfo) {
                autoLoginHandler.sendMessage(autoLoginHandler.obtainMessage(what, obj));
            }
        }
    };

    // 注册后30分钟判断绑定是否成功
    protected final Handler checkBindHandler = new CheckBindHandler(this);
    protected final Observer checkBindCallback = new Observer() {
        @Override
        public void notifyObserver(int what, Object obj) {
            checkBindHandler.sendMessage(checkBindHandler.obtainMessage(what, obj));
        }
    };

    // 更新
    protected final Handler updateHandler = new UpdateHandler(this);
    protected final Observer updateCallback = new Observer() {
        @Override
        public void notifyObserver(int what, Object obj) {
            updateHandler.sendMessage(updateHandler.obtainMessage(what, obj));
        }
    };

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     */
    public BaseActivity() {
        super();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SLog.d(TAG,
                "构造activity..service is null:" + (CoreService.getInstance() == null));
        super.onCreate(savedInstanceState);
        mContext = this;
        mService = CoreService.getInstance();
        // fix bug: 进入应用内应该是调整媒体音量，而不是铃声音量
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        MainApp.i().addActivity(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mService != null) {
            mService.setCurrentClass(this.getClass());
            mService.unregisterCallBack(this);
            mService.unregisterCallBack(autoLoginCallback);
            // 在界面不可见时,如果需要接受下载响应,会出现问题 anson.yang@20121013
            mService.unregisterCallBack(updateCallback);
            mService.unregisterCallBack(checkBindCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null) {
            if (!(this instanceof LaunchActivity) && !MainApp.i().isOnline()
                    && CommonPreferences.getLoginedStatus() == true
                    && CommonPreferences.getLogoutedStatus() == false) {
                mService.registerCallBack(autoLoginCallback);

                Message message = MainApp.i().getCallbackStatus(MainActivity.class);
                if (null != message) {
                    this.notifyObserver(message.what, message.obj);
                }
            }
            mService.registerCallBack(checkBindCallback);
            mService.registerCallBack(updateCallback);
            mService.registerCallBack(this);
            mService.setCurrentClass(null);
        }
        Message message = MainApp.i().getCallbackStatus(this.getClass());
        if (null != message) {
            this.notifyObserver(message.what, message.obj);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApp.i().popActivity(this);
    }

    /**
     * @param what
     * @param obj
     */
    @Override
    public void notifyObserver(int what, Object obj) {

    }

    /**
     * 检查网络
     * 
     * @return
     */
    public boolean checkNetStatus() {
        boolean netSataus = false;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                netSataus = networkInfo.isAvailable();
            }
        }

        if (!netSataus) {
            Builder b = new AlertDialog.Builder(this).setTitle(
                    R.string.register_no_useful_net_title)
                    .setMessage(R.string.register_no_useful_net_message);
            b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                }
            }).setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            }).show();
        }

        return netSataus;
    }

    /**
     * 显示toast
     * 
     * @param tip
     */
    public void showToast(String tip) {
        Toast toast = Toast.makeText(this, tip,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 显示toast
     * 
     * @param tip
     */
    public void showToast(int tip) {
        showToast(getString(tip));
    }

    public ProgressDialog showProgressDialog(String message) {
        return showProgressDialog(null, message);
    }

    public ProgressDialog showProgressDialog(String title, String message) {
        return showProgressDialog(title, message, true);
    }

    public ProgressDialog showProgressDialog(String title, String message, boolean cancelable) {
        return ProgressDialog.show(this, title, message, true, cancelable);
    }

    public String getEditorText(int id) {
        String value = null;
        EditText editor = (EditText) findViewById(id);
        if (editor != null) {
            Editable editable = editor.getText();
            if (editable != null) {
                value = editable.toString().trim();
            }
        }
        return value;
    }

    /**
     * @param resID
     * @param text
     */
    public void setTextViewValue(int resID, String text) {
        ((TextView) findViewById(resID)).setText(text);
    }

    public void setTextViewValue(View view, int resID, String text) {
        ((TextView) (view.findViewById(resID))).setText(text);
    }

    /**
     * @param resID
     * @param text
     */
    public void setEditTextValue(int resID, String text) {
        if (!TextUtils.isEmpty(text))
            ((EditText) findViewById(resID)).setText(text);
    }

    public void showQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.tip);
        builder.setMessage(R.string.quit_msg);
        builder.setPositiveButton(getResources().getString(R.string.quit_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        switchToHome();
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.quit_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        MainApp.i().setOnline(false);
                        MainApp.i().stopService();
                    }

                });
        builder.show();
    }

    /**
     * 隐藏电话号码
     * 
     * @param mobile
     * @return
     */
    public String changeMobile(String mobile) {
        StringBuilder value = new StringBuilder(mobile.substring(0, 3));
        value.append("****");
        value.append(mobile.substring(mobile.length() - 4, mobile.length()));
        return value.toString();
    }

    /**
     * @param smsTo
     * @param smsContent
     */
    public void sendActivateSMSMsg(String smsTo, String smsContent) {
        mService.getMessageModule().sendActivateSMSMsg(smsTo, smsContent);
    }

    // 切换到后台，但是不退出APP和HOME键不同
    public void switchToHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        try {
            startActivity(intent);
        } catch (Exception e) {
            // http://redmine.sky-mobi.com/redmine/issues/13091
            e.printStackTrace();
            finish();
        }
    }

    // 隐藏软键盘
    public void hideSystemSoftKeyboard(EditText edit) {
        if (edit == null)
            return;
        InputMethodManager inputMgr = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMgr.hideSoftInputFromWindow(edit.getWindowToken(),
                0);
    }

    // 显示软键盘
    public void showSystemSoftKeyboard(EditText edit) {
        if (edit == null)
            return;
        InputMethodManager inputMgr = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMgr.showSoftInput(edit, 0);
    }

    // start: anson.yang 20120920修改
    /**
     * 启动到下一个界面,根据登陆的状态
     */
    public void nextPage() {
        boolean logined = CommonPreferences.getLoginedStatus();
        boolean logouted = CommonPreferences.getLogoutedStatus();
        if (logined) {
            if (logouted) {
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
        } else {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    public int getCurrentVersion() {
        return MainApp.i().getPi().versionCode == 0 ? -1
                : MainApp.i().getPi().versionCode;
    }

    // end: anson.yang 20120920修改

    // 给activty 切换加上动画效果
    @Override
    public void startActivity(Intent intent) {
        if (getParent() != null) {
            getParent().startActivity(intent);
        } else {
            super.startActivity(intent);
        }
        overridePendingTransition(R.anim.move_right_in, R.anim.move_left_out);

    }

    // 给activty 切换加上动画效果
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.move_right_in, R.anim.move_left_out);
    }

    // 给activty 切换加上动画效果
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.move_left_in, R.anim.move_right_out);
    }
}
