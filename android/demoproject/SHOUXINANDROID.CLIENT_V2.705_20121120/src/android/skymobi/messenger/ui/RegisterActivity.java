
package android.skymobi.messenger.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.bizunit.auth.LoginBU;
import android.skymobi.messenger.bizunit.auth.RegisterBU;
import android.skymobi.messenger.ui.action.RegisterAction;
import android.skymobi.messenger.ui.handler.event.EventMsg;
import android.skymobi.messenger.utils.dialog.DialogTool;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * @ClassName: ActivationActivity
 * @Description: 激活注册
 * @author Sean.Xie
 * @date 2012-2-7 下午4:12:46 edit@hzc
 */
public class RegisterActivity extends TopActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    public static final int BUTTON_RETURN_LOGIN = 1;

    private ImageButton mRegisterButton;

    private RegisterAction mAction;

    RegisterBU registerAO = null;

    LoginBU loginBU = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        checkNetStatus();
        try {
            init();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
    }

    /**
     * 初始化组件
     */
    private void init() {
        mAction = new RegisterAction(this);
        registerAO = new RegisterBU(handler);
        mAction.setRegisterAO(registerAO);

        loginBU = new LoginBU(handler);

        // 免费激活
        mRegisterButton = (ImageButton) findViewById(R.id.register_free_register_btn);
        mRegisterButton.setOnClickListener(mAction);

        // 用户协议
        ToggleButton protocolCheckBox = (ToggleButton) findViewById(R.id.register_protocol_checkbox);
        protocolCheckBox.setOnCheckedChangeListener(mAction);

        // 协议网页链接
        TextView protocolText = (TextView) findViewById(R.id.register_protocol_textlink);
        protocolText.setOnClickListener(mAction);
        initTopBar();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.menu_exit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initTopBar() {
        setTopBarTitle(R.string.login_free_register);
        setTopBarButton(TOPBAR_BUTTON_RIGHTII, R.string.register_login1,
                BUTTON_RETURN_LOGIN, mAction);
    }

    /**
     * 激活失败时的对话框“确定”操作
     */
    DialogInterface.OnClickListener positiveOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            dialog.cancel();
            finish();
        }

    };

    /**
     * 激活失败时的对话框“取消”操作
     */
    DialogInterface.OnClickListener cancelOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }

    };

    private ProgressDialog dialog = null;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EventMsg.EVENT_REGISTER_ING:
                    dialog = DialogTool.createNormalProgressDialog(RegisterActivity.this,
                            android.R.drawable.ic_dialog_info, R.string.tip,
                            getString(R.string.register_waitting));
                    dialog.show();
                    break;
                // 手机被激活过了
                case EventMsg.EVENT_REGISTER_IS_ACTIVATED:
                    dialog.dismiss();
                    DialogTool.createNormalDialog(RegisterActivity.this,
                            android.R.drawable.ic_dialog_info, R.string.tip,
                            getString(R.string.register_already_register),
                            R.string.register_login1, positiveOnClickListener,
                            true, cancelOnClickListener, true).show();
                    break;

                // 注册成功
                case EventMsg.EVENT_REGISTER_SUCCESS:
                    UserInfo userInfo = loginBU.getCurrentUserInfo();
                    loginBU.login(userInfo.name, userInfo.encryptPasswd);
                    registerAO.sendBindSMS();

                    /*-------------激活后登录---------------------**/
                case EventMsg.EVENT_LOGIN_LOGINING:
                    dialog.setMessage(getString(R.string.login_logining));
                    break;
                case EventMsg.EVENT_LOGIN_SUCCESS:
                    dialog.dismiss();
                    SLog.d(TAG, "激活注册成功,跳转至激活引导页填写昵称等...");
                    Intent intent = new Intent(RegisterActivity.this, ActivateGuideActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case EventMsg.EVENT_LOGIN_FAIL:
                case EventMsg.EVENT_REGISTER_FAIL:
                    dialog.dismiss();
                    showToast(R.string.register_error_msg);
                    break;

                /*--------------网络异常情况-------------------------------**/
                case EventMsg.EVENT_NET_ERROR:
                    dialog.dismiss();
                    showToast(R.string.network_error);
                    break;
                case EventMsg.EVENT_TIMEOUT:
                    dialog.dismiss();
                    showToast(R.string.network_timeout);
                    break;

            }
        }
    };
}
