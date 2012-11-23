
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.CommonModule;
import android.skymobi.messenger.ui.action.SetNickNameAction;
import android.skymobi.messenger.utils.CommonPreferences;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @ClassName: SetNickNameActivity
 * @Description: 设置昵称
 * @author Sean.Xie
 * @date 2012-2-13 下午5:05:19
 */
public class SetNickNameActivity extends TopActivity {

    public static final String INTENT_PARAM = "SET_NICKNAME_ACTIVITY";

    public static final int DIALOG_CODE = 0;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SUCCESS:
                    mService.getSettingsModule().getUserInfo();
                    startActivity(new Intent(SetNickNameActivity.this, MainActivity.class));
                    finish();
                    break;
                case CoreServiceMSG.MSG_FAILED:
                    UserInfo info = (UserInfo) msg.obj;
                    showToast(info.resultHint);
                    break;
            }
            removeDialog(DIALOG_CODE);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_nick_name);
        // service为空 则重启，等待service重启完成
        try {
            CommonModule module = mService.getCommonModule();
            init();
        } catch (NullPointerException e) {
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
    }

    /**
     * 初始化
     */
    private void init() {
        // SetNickNameAction action = new SetNickNameAction(this);
        UserInfo info = CommonPreferences.getUserInfo();
        // 标题
        TextView nickNameTitle = (TextView) findViewById(R.id.nick_name_tip_title);
        if (info != null) {
            nickNameTitle.setText(getString(R.string.nice_name_title_pre,
                    info.nickname));
        }

        // 编辑框
        EditText editText = (EditText) findViewById(R.id.nick_name_editor);
        if (info != null) {
            editText.setHint(info.nickname);
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        initTopBar();
        // 按钮
        // Button save = (Button) findViewById(R.id.nick_name_save);
        // Button letter = (Button) findViewById(R.id.nick_name_latter);
        // save.setOnClickListener(action);
        // letter.setOnClickListener(action);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        if (obj instanceof UserInfo) {
            handler.sendMessage(handler.obtainMessage(what, obj));
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        dialogBuilder.setMessage(R.string.nice_name_waitting_set);
        Dialog dialog = dialogBuilder.create();
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_BUTTON_RIGHTII, R.string.save, new SetNickNameAction(this));
        setTopBarTitle(R.string.nick_name_title);

    }

    @Override
    public void onBackPressed() {
        switchToHome();
    }

}
