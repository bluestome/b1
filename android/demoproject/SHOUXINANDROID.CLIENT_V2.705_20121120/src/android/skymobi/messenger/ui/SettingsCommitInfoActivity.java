
package android.skymobi.messenger.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;

/**
 * @ClassName: SettingsCommitInfoActivity
 * @Description: 提交部分设置信息页面，包括昵称、学校、单位等信息。
 * @author Lv.Lv
 * @date 2012-3-5 下午4:47:13
 */
public class SettingsCommitInfoActivity extends TopActivity {

    private static final String TAG = SettingsCommitInfoActivity.class.getSimpleName();
    private static final int MIN_LINES_FEEDBACK = 8;
    private static final int MAX_LENGTH_FEEDBACK = 150; // 反馈最长有效字符数为150
    private static final int MAX_LENGTH_NICKNAME = 12; // 昵称最长有效字符数为12
    private static final int MAX_LENGTH_SIGNATURE = 36; // 签名最长有效字符数为36
    private static final int MAX_LENGTH_SCHOOL = 50; // 学校最长有效字符数为50
    private static final int MAX_LENGTH_CORPORATION = 50; // 单位最长有效字符数为50
    private int mType = 0;

    private EditText mContentEditor = null;
    private String mOrigEditorText = null;

    private SettingsModule settingsModule = null;
    private NetUserInfo mUserInfo = null;
    private ProgressDialog mWaitDialog = null;
    // Handle message
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SETTINGS_MODIFYPWD_FAIL:
                case CoreServiceMSG.MSG_SETTINGS_SET_NICKNAME_FAIL:
                case CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_FAIL:
                case CoreServiceMSG.MSG_SETTINGS_FEEDBACK_FAIL:
                    hideWaitDialog();
                    // if (null != msg.obj && msg.obj instanceof String) { //
                    // 显示服务端返回的异常信息
                    if (null != msg.obj && msg.obj instanceof Integer) { // 显示服务端返回的异常信息
                        showToast(getString(R.string.network_error) + "\r\n[" + Constants.ERROR_TIP
                                + ":0x" + StringUtil.autoFixZero((Integer) msg.obj) + "]");
                    } else if (null != msg.obj && msg.obj instanceof String) {
                        showToast((String) msg.obj);
                    } else {
                        showToast(R.string.network_error);
                    }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_ILLEGAL:
                    // 用户昵称不合法
                    if (mWaitDialog != null && mWaitDialog.isShowing()) {
                        hideWaitDialog();
                        showToast(R.string.setting_nickname_illegal);
                    }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_FEEDBACK_SUCCESS:
                    hideWaitDialog();
                    showToast(R.string.settings_feedback_success);
                    finish();
                    break;
                case CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_SUCCESS: {
                    hideWaitDialog();
                    showToast(R.string.settings_userinfo_success);
                    NetUserInfo useinfo = (NetUserInfo) msg.obj;
                    MainApp.i().setUserInfo(useinfo);
                    mUserInfo = useinfo;
                    finish();
                }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_SUCCESS: {
                    NetUserInfo useinfo = (NetUserInfo) msg.obj;
                    mUserInfo = useinfo;
                }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_commit_info);
        settingsModule = mService.getSettingsModule();
        mUserInfo = MainApp.i().getNetLUserInfo();
        if (mUserInfo == null) {
            settingsModule.getUserInfo();
        }
        init();
        initTopBar();
    }

    private void init() {
        Bundle bdl = getIntent().getExtras();
        if (bdl != null) {
            mType = (Integer) bdl.get(Constants.SETTINGS_COMMITINFO_TYPE);
        }

        // 输入框
        mContentEditor = (EditText) findViewById(R.id.settings_info_editor);
        switch (mType) {
            case Constants.SETTINGS_COMMITINFO_NICKNAME: {
                mContentEditor.setHint(R.string.settings_nickname_tip);
                mOrigEditorText = SettingsPreferences.getNickname();
                setEditorText(mContentEditor, mOrigEditorText);
                mContentEditor.setFilters(new InputFilter[] {
                        new InputFilter.LengthFilter(MAX_LENGTH_NICKNAME)
                });
            }
                break;
            case Constants.SETTINGS_COMMITINFO_SCHOOL: {
                mContentEditor.setHint(R.string.settings_school_tip);
                mOrigEditorText = SettingsPreferences.getSchool();
                setEditorText(mContentEditor, mOrigEditorText);
                mContentEditor.setFilters(new InputFilter[] {
                        new InputFilter.LengthFilter(MAX_LENGTH_SCHOOL)
                });
            }
                break;
            case Constants.SETTINGS_COMMITINFO_CORPORATION: {
                mContentEditor.setHint(R.string.settings_corporation_tip);
                mOrigEditorText = SettingsPreferences.getCorporation();
                setEditorText(mContentEditor, mOrigEditorText);
                mContentEditor.setFilters(new InputFilter[] {
                        new InputFilter.LengthFilter(MAX_LENGTH_CORPORATION)
                });
            }
                break;
            case Constants.SETTINGS_COMMITINFO_SIGNATURE: {
                mOrigEditorText = SettingsPreferences.getSignature();
                setEditorText(mContentEditor, mOrigEditorText);
                mContentEditor.setFilters(new InputFilter[] {
                        new InputFilter.LengthFilter(MAX_LENGTH_SIGNATURE)
                });
                mContentEditor.setHint(R.string.settings_signature_tip);
            }
                break;
            case Constants.SETTINGS_COMMITINFO_FEEDBACK: {
                mContentEditor.setHint(R.string.settings_feedback_tip);
                // mContentEditor.setMinLines(MIN_LINES_FEEDBACK);
                mContentEditor.setFilters(new InputFilter[] {
                        new InputFilter.LengthFilter(MAX_LENGTH_FEEDBACK)
                });
            }
                break;
        }

    }

    private void setEditorText(EditText editor, String text) {
        if (TextUtils.isEmpty(text) || editor == null)
            return;

        editor.setText(text);
        editor.setSelection(text.length());
    }

    public void onCommitBtnClick() {
        String inputText = mContentEditor.getText().toString();

        // 昵称和反馈不能为空
        if (inputText.trim().length() <= 0
                && (mType == Constants.SETTINGS_COMMITINFO_NICKNAME || mType == Constants.SETTINGS_COMMITINFO_FEEDBACK)) {
            showToast(R.string.settings_enter_text_empty);
            return;
        }

        // 输入文字与初始内容相同，直接返回
        if (inputText.equals(mOrigEditorText)) {
            finish();
            return;
        }

        if (mUserInfo == null) {
            mHandler.sendMessage(Message.obtain(mHandler,
                    CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_FAIL, null));
            return;
        }

        switch (mType) {
            case Constants.SETTINGS_COMMITINFO_NICKNAME: {
                inputText = inputText.trim();
                if (inputText.length() < 2) {
                    showToast(R.string.nice_name_too_short);
                } else if (inputText.length() > 12) {
                    showToast(R.string.nice_name_too_long);
                } else {
                    mUserInfo.setPersonnickname(inputText.replace("\n", ""));
                    settingsModule.setNetUserInfo(mUserInfo, settingsModule.TYPE_PERSONNICKNAME);
                    showWaitDialog();
                }
            }
                break;
            case Constants.SETTINGS_COMMITINFO_SCHOOL: {
                mUserInfo.setUschoolgraduated(inputText);
                settingsModule.setNetUserInfo(mUserInfo, settingsModule.TYPE_SCHOOL);
                showWaitDialog();
            }
                break;
            case Constants.SETTINGS_COMMITINFO_CORPORATION: {
                mUserInfo.setUcorporation(inputText);
                settingsModule.setNetUserInfo(mUserInfo, settingsModule.TYPE_CORPORATION);
                showWaitDialog();
            }
                break;
            case Constants.SETTINGS_COMMITINFO_SIGNATURE: {
                mUserInfo.setUsignature(inputText);
                settingsModule.setNetUserInfo(mUserInfo, settingsModule.TYPE_SIGNATURE);
                showWaitDialog();
            }
                break;
            case Constants.SETTINGS_COMMITINFO_FEEDBACK: {
                String nickName = SettingsPreferences.getNickname();
                settingsModule.feedBack(nickName, inputText);
                showWaitDialog();
            }
                break;
            default:
                break;
        }
    }

    private void showWaitDialog() {
        mWaitDialog = new ProgressDialog(this);
        mWaitDialog.setMessage(getString(R.string.settings_waitting));
        mWaitDialog.setIndeterminate(true);
        mWaitDialog.setCancelable(true);
        mWaitDialog.show();
    }

    private void hideWaitDialog() {
        if (mWaitDialog != null) {
            mWaitDialog.cancel();
        }
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        switch (mType) {
            case Constants.SETTINGS_COMMITINFO_NICKNAME: {
                OnClickListener click = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommitBtnClick();
                    }
                };
                setTopBarTitle(R.string.settings_nickname);
                setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_save, click);
            }
                break;
            case Constants.SETTINGS_COMMITINFO_SCHOOL: {
                OnClickListener click = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommitBtnClick();
                    }
                };
                setTopBarTitle(R.string.contacts_detail_school);
                setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_save, click);
            }
                break;
            case Constants.SETTINGS_COMMITINFO_CORPORATION: {
                OnClickListener click = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommitBtnClick();
                    }
                };
                setTopBarTitle(R.string.contacts_detail_corporation);
                setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_save, click);
            }
                break;
            case Constants.SETTINGS_COMMITINFO_SIGNATURE: {
                OnClickListener click = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommitBtnClick();
                    }
                };
                setTopBarTitle(R.string.contacts_detail_signature_title);
                setTopBarButton(TOPBAR_IMAGE_BUTTON_RIGHTII, R.drawable.topbar_btn_save, click);
            }
                break;
            case Constants.SETTINGS_COMMITINFO_FEEDBACK: {
                OnClickListener click = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommitBtnClick();
                    }
                };
                setTopBarTitle(R.string.settings_feedback);
                setTopBarButton(TOPBAR_BUTTON_RIGHTII, R.string.settings_commit, click);
            }
                break;
            default:
                break;
        }
    }

}
