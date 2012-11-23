
package android.skymobi.messenger.ui.action;

import android.skymobi.messenger.R;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.SetNickNameActivity;
import android.skymobi.messenger.utils.CommonPreferences;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * @ClassName: ContactsListAction
 * @Description: 联系人列表动作
 * @author Sean.Xie
 * @date 2012-2-22 下午5:02:52
 */
public class SetNickNameAction extends BaseAction implements OnClickListener {

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
    public SetNickNameAction(BaseActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.topbar_btn_rightII:
                EditText editor = (EditText) (activity.findViewById(R.id.nick_name_editor));
                String text = editor.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    showToast(R.string.nice_name_no_input);
                } else if (text.trim().length() < 2) {
                    showToast(R.string.nice_name_too_short);
                } else if (text.trim().length() > 12) {
                    showToast(R.string.nice_name_too_long);
                } else {
                    showDialog(SetNickNameActivity.DIALOG_CODE);
                    UserInfo info = CommonPreferences.getUserInfo();
                    mService.getCommonModule().setNickname(
                            info.skyid,
                            info.token,
                            ((EditText) (activity.findViewById(R.id.nick_name_editor))).getText()
                                    .toString().replace("\n", "")
                                    .trim());
                }
                break;
        }
    }
}
