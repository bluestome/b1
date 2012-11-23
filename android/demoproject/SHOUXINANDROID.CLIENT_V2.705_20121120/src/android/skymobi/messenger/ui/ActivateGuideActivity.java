
package android.skymobi.messenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.auth.LoginBU;
import android.skymobi.messenger.ui.action.ActivateGuideAction;
import android.skymobi.messenger.ui.handler.event.EventMsg;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * @ClassName: ActivateGuideActivity
 * @Description: 激活引导，强制设置昵称和性别
 * @author dylan.zhao
 * @date 2012-9-13 上午09:23:25
 */
public class ActivateGuideActivity extends TopActivity {

    public static final String TAG = ActivateGuideActivity.class
            .getSimpleName();

    protected EditText nickNameEt = null; // 昵称输入框
    protected View sexFemale = null;// 女
    protected View sexMale = null; // 男

    private ActivateGuideAction action = null;
    private LoginBU loginBU = null;

    public ActivateGuideActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_guide);
        loginBU = new LoginBU(handler);
        action = new ActivateGuideAction(this);
        action.setLoginBU(loginBU);
        try {
            init();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
    }

    /**
     * 屏蔽返回键 hzc@20120924
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void init() {

        nickNameEt = (EditText) findViewById(R.id.set_nick_editor);
        nickNameEt.setFocusable(true);
        nickNameEt.setFocusableInTouchMode(true);
        nickNameEt.requestFocus();

        sexMale = findViewById(R.id.set_sex_male);
        sexFemale = findViewById(R.id.set_sex_female);
        sexFemale.setBackgroundResource(R.drawable.set_sex_left_bg);

        initTopBar();

        nickNameEt.addTextChangedListener(action);
        sexMale.setOnClickListener(action);
        sexFemale.setOnClickListener(action);
    }

    @Override
    public void initTopBar() {
        // 设置标题
        super.setTopBarTitle(R.string.set_nick_sex_title);

        // 设置顶部条的保存按钮
        setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII,
                R.drawable.topbar_btn_save_gray, action);
        findViewById(R.id.topbar_imageButton_rightII).setEnabled(false);
    }

    protected Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EventMsg.EVENT_ACTIVATE_GUIDE_SUCCESS:
                break;
            case EventMsg.EVENT_SET_USERINFO_FAIL:
               // ToastTool.showAtCenterShort(ActivateGuideActivity.this,R.string.update_userinfo_fail);
                break;
            }
            startActivity(new Intent(ActivateGuideActivity.this,  MainActivity.class));
            finish();
    }
    };
}
