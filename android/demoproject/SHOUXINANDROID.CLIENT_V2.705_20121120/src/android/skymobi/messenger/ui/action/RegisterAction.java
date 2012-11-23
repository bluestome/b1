
package android.skymobi.messenger.ui.action;

import android.content.Intent;
import android.net.Uri;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.auth.RegisterBU;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.LoginActivity;
import android.skymobi.messenger.ui.RegisterActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * @Description: 激活注册动作类
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52 edit@hzc
 */
public class RegisterAction extends BaseAction implements OnClickListener,
        OnCheckedChangeListener {

    /** 是否同意用户协议 */
    private boolean isAgreeUserAgreement = true;

    public RegisterAction(BaseActivity activity) {
        super(activity);
    }

    private RegisterBU registerAO = null;

    public void setRegisterAO(RegisterBU registerAO) {
        this.registerAO = registerAO;
    }

    /**
     * 用户协议选择事件
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        isAgreeUserAgreement = (isChecked ? true : false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.register_free_register_btn:
                if (isAgreeUserAgreement) {
                    registerAO.register();
                } else {
                    showToast(R.string.register_protocol_nochecked_tip);
                }
                break;
            case RegisterActivity.BUTTON_RETURN_LOGIN:
                activity.finish();
                activity.startActivity(new Intent(activity, LoginActivity.class));
                break;
            case R.id.register_protocol_textlink:
                Uri uri = Uri.parse(activity.getString(R.string.protocol_url));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }
}
