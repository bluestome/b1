
package android.skymobi.messenger.ui.action;

import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.auth.LoginBU;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

import org.apache.commons.lang.StringUtils;

public class ActivateGuideAction extends BaseAction implements OnClickListener, TextWatcher {

    public static final String TAG = ActivateGuideAction.class.getSimpleName();

    LoginBU loginBU = null;

    public ActivateGuideAction(BaseActivity activity) {
        super(activity);

    }

    public void setLoginBU(LoginBU loginBU) {
        this.loginBU = loginBU;
    }

    // 选中的性别: 2女 ，1男，默认女
    private final static String FEMALE = SettingsPreferences.Female;
    private final static String MALE = SettingsPreferences.Male;
    private String sex = FEMALE;

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            // 保存
            case R.id.topbar_imageButton_rightII:
                final EditText nickNameEt = (EditText) (activity.findViewById(R.id.set_nick_editor));
                String nickName = nickNameEt.getText().toString();
                if (StringUtils.trimToEmpty(nickName).length() < 2) {
                    ToastTool.showAtCenterLong(activity,
                            R.string.nice_name_too_short);
                } else {
                    loginBU.saveActivateGuide(nickName, sex);
                    activity.hideSystemSoftKeyboard((EditText) activity
                            .findViewById(R.id.set_nick_editor));
                }
                break;
            case R.id.set_sex_female:
                SLog.d(TAG, "选中了女性选项..");
                sex = FEMALE;
                (activity.findViewById(R.id.set_sex_female))
                        .setBackgroundResource(R.drawable.set_sex_left_bg);
                (activity.findViewById(R.id.set_sex_male))
                        .setBackgroundResource(0);
                break;
            case R.id.set_sex_male:
                SLog.d(TAG, "选中了男性选项..");
                sex = MALE;
                (activity.findViewById(R.id.set_sex_male))
                        .setBackgroundResource(R.drawable.set_sex_right_bg);
                (activity.findViewById(R.id.set_sex_female))
                        .setBackgroundResource(0);
                break;
            default:
                SLog.d(TAG, "其他选项:" + id);
                break;

        }
    }

    /**
     * 昵称输入框内容有变化时，如果有填写文字，则使保存按钮可点，否则，保存按钮不可点
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (StringUtils.isBlank(
                ((EditText) activity.findViewById(R.id.set_nick_editor))
                        .getText().toString())) {
            ((ImageButton) (activity.findViewById(R.id.topbar_imageButton_rightII)))
                    .setImageResource(R.drawable.topbar_btn_save_gray);
            (activity.findViewById(R.id.topbar_imageButton_rightII)).setEnabled(false);
        } else {
            ((ImageButton) (activity.findViewById(R.id.topbar_imageButton_rightII)))
                    .setImageResource(R.drawable.topbar_btn_save);
            (activity.findViewById(R.id.topbar_imageButton_rightII)).setEnabled(true);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // 必须重写的接口方法
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // 必须重写的接口方法
    }

}
