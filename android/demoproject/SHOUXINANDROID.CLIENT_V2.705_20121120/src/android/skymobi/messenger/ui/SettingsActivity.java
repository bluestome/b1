
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.LaunchActivity;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.SettingsListAdapter;
import android.skymobi.messenger.bean.SettingsItem;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.widget.CornerListView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends TopActivity implements OnClickListener,
        OnItemClickListener {

    public static final int MENU_QUIT = Menu.FIRST + 1; // 退出

    private static int positionbase = 0;
    private static final int POSITION_PRIVACY = positionbase++;
    private static final int POSITION_MSGNOTIFY = positionbase++;
    private static final int POSITION_BLACKLIST = positionbase++;
    // private static final int POSITION_NETWORKSTATISTIC = positionbase++;
    private static final int POSITION_FEEDBACK = positionbase++;
    private static final int POSITION_HELP = positionbase++;
    private static final int POSITION_ABOUT = positionbase++;

    private static final int POSITION_BIND = 0;
    private static final int POSITION_CHANGEPW = 1;
    private static final int POSITION_RESTORE = 2;

    private SettingsModule settingsModule = null;
    private TextView mNicknameTv = null;
    private ImageView mImageSex = null;
    private ImageView mImageHead = null;
    private SettingsListAdapter mSafetyAdapter = null;
    private SettingsListAdapter mSignatureAdapter = null;
    private SettingsListAdapter mLogyoutAdapter = null;
    private SettingsListAdapter adapter = null;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SETTINGS_LOGOUT_FAIL:
                    showToast(R.string.logout_fail);
                    break;
                case CoreServiceMSG.MSG_SETTINGS_LOGOUT_SUCCESS: {
                    showToast(R.string.logout_success);
                    MainApp.i().setUserInfo(null);
                    MainApp.i().setOnline(false);
                    if (null != mService) {
                        mService.cancelNotification();
                    }
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    MainApp.i().closeAllActivity();
                }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_FAIL:
                    // showToast(R.string.network_error);
                    break;
                case CoreServiceMSG.MSG_SETTINGS_DOWNLOAD_HEADPHOTO_SUCCESS:
                    if (mImageHead != null) {
                        String headphoto = SettingsPreferences.getHeadPhoto();
                        HeaderCache.getInstance().getHeader(headphoto, null, mImageHead);
                    }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_SUCCESS: {
                    updateStatus();
                }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_SUCCESS: {
                    updateStatus();
                }
                    break;
                default:
                    break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setttings);
        try {
            settingsModule = mService.getSettingsModule();
        } catch (NullPointerException e) {
            e.printStackTrace();
            finish();
            startActivity(new Intent(this, LaunchActivity.class));
        }
        initTopBar();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        NetUserInfo useinfo = MainApp.i().getNetLUserInfo();
        if (useinfo == null) {
            settingsModule.getUserInfo();
        }

        updateStatus();
    }

    private void init() {

        // 编辑
        Button edit = (Button) findViewById(R.id.edit_btn);
        edit.setOnClickListener(this);

        UserInfo info = CommonPreferences.getUserInfo();
        // 昵称
        mNicknameTv = (TextView) findViewById(R.id.text_nickname);
        mNicknameTv.setText(SettingsPreferences.getNickname());
        // 手信号
        TextView account = (TextView) findViewById(R.id.text_account);
        if (info != null)
            account.append(" " + info.name);
        // 性别设置
        mImageSex = (ImageView) findViewById(R.id.icon_sex);
        // 头像设置
        mImageHead = (ImageView) findViewById(R.id.icon_headphoto);

        mImageHead.setOnClickListener(this);
        SettingsItem item = null;

        // 签名
        List<SettingsItem> list = new ArrayList<SettingsItem>();
        list.add(new SettingsItem(this, R.string.settings_signature,
                R.string.settings_signature_default, true, false, 0, true));
        mSignatureAdapter = new SettingsListAdapter(mContext, list);
        mSignatureAdapter.setResource(R.layout.settings_item);
        CornerListView lv = (CornerListView) findViewById(R.id.item_signature);
        lv.setAdapter(mSignatureAdapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);

        // 手机绑定和密码修改,联系人恢复
        list = new ArrayList<SettingsItem>();
        item = new SettingsItem(mContext, R.string.settings_bind,
                0, true, false, 0, true);
        item.setContent(SettingsPreferences.getMobile());
        list.add(item);
        list.add(new SettingsItem(mContext, R.string.settings_changepassword,
                0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.settings_contacts_restore,
                0, true, false, 0, true));
        mSafetyAdapter = new SettingsListAdapter(mContext, list);
        mSafetyAdapter.setResource(R.layout.settings_item);
        lv = (CornerListView) findViewById(R.id.item_safety);
        lv.setAdapter(mSafetyAdapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);

        // 推荐给可能认识的人，消息提醒，黑名单，流量统计，反馈，关于
        int newImageID = 0;
        newImageID = getNewVersion();

        list = new ArrayList<SettingsItem>();
        list.add(new SettingsItem(mContext, R.string.settings_privacy,
                0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.settings_msg_notify,
                0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.settings_blacklist_mgr,
                0, true, false, 0, true));
        // 屏蔽流量统计
        // list.add(new SettingsItem(mContext,
        // R.string.settings_network_statistic,
        // 0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.settings_feedback,
                0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.settings_help,
                0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.settings_about,
                0, true, false, newImageID, true));
        adapter = new SettingsListAdapter(mContext, list);
        adapter.setResource(R.layout.settings_item);
        lv = (CornerListView) findViewById(R.id.item_management);
        lv.setAdapter(adapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);

        // 注销和退出
        list = new ArrayList<SettingsItem>();
        list.add(new SettingsItem(mContext, R.string.settings_logout,
                0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.settings_exit,
                0, true, false, 0, true));
        mLogyoutAdapter = new SettingsListAdapter(mContext, list);
        mLogyoutAdapter.setResource(R.layout.settings_item);
        lv = (CornerListView) findViewById(R.id.item_exit);
        lv.setAdapter(mLogyoutAdapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);
    }

    /**
     * @param newImageID
     * @return
     */
    private int getNewVersion() {
        int newImageID = 0;
        if (CommonPreferences.getIsNew()) {
            String newversion = CommonPreferences.getAppVerion();
            String localversion = MainApp.i().getPi().versionName;
            // 当前版本已经是最新
            if (newversion != null && localversion != null
                    && newversion.equalsIgnoreCase(localversion)) {
                CommonPreferences.saveIsNew(false);
                newImageID = 0;
            } else {
                newImageID = R.drawable.newversion;
            }
        }
        return newImageID;
    }

    private int getImageIDNewFeatureRestore() {
        int newImageID = 0;
        // 后面的版本不再需要显示new字样了
        // if (!CommonPreferences.getIsCheckedNewFeatureRestore()) {
        // newImageID = R.drawable.newversion;
        // }
        return newImageID;
    }

    // 更新性别ICON
    private void updateStatus() {
        // nickname textview
        if (mNicknameTv != null) {
            mNicknameTv.setText(SettingsPreferences.getNickname().replace("\n", ""));
        }

        // 头像
        if (mImageHead != null) {
            String headphoto = SettingsPreferences.getHeadPhoto();
            HeaderCache.getInstance().getHeader(headphoto, null, mImageHead);
        }

        // Sex Icon
        if (mImageSex != null) {
            if (SettingsPreferences.getSex().equals(SettingsPreferences.Male)) {
                mImageSex.setBackgroundResource(R.drawable.male);
            } else
                mImageSex.setBackgroundResource(R.drawable.female);
        }

        // signature
        if (mSignatureAdapter != null) {
            String signature = SettingsPreferences.getSignature();
            if (!TextUtils.isEmpty(signature)) {
                mSignatureAdapter.updateItem(0, signature);
            } else {
                signature = getString(R.string.settings_signature_default);
                mSignatureAdapter.updateItem(0, signature);
            }
        }

        // bind mobile
        if (mSafetyAdapter != null) {
            mSafetyAdapter.updateItem(POSITION_BIND, SettingsPreferences.getMobile());
        }

        // restore contacts
        if (mSafetyAdapter != null) {
            mSafetyAdapter.updateImage(POSITION_RESTORE, getImageIDNewFeatureRestore());
        }

        // update newversion
        if (null != adapter) {
            adapter.updateImage(POSITION_ABOUT, getNewVersion());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.edit_btn:
                startActivity(new Intent(SettingsActivity.this, PersonalSettingsActivity.class));
                break;
            case R.id.icon_headphoto:
                Intent intent = new Intent(SettingsActivity.this,
                        ImageViewActivity.class);
                intent.putExtra(ImageViewActivity.HEADPHOTO_FILENAME,
                        SettingsPreferences.getHeadPhoto());
                startActivity(intent);
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.item_management) {
            // 推荐我给可能认识的人
            if (position == POSITION_PRIVACY) {
                Intent intent = new Intent(SettingsActivity.this,
                        SettingsPrivacyActivity.class);
                startActivity(intent);
            }
            // 消息提醒
            else if (position == POSITION_MSGNOTIFY) {
                Intent intent = new Intent(SettingsActivity.this,
                        SettingsMsgNotifyActivity.class);
                startActivity(intent);
            }// 黑名单管理
            if (position == POSITION_BLACKLIST) {
                Intent intent = new Intent(this, ContactBlackListActivity.class);
                startActivity(intent);
            }
            // // 流量统计
            // else if (position == POSITION_NETWORKSTATISTIC) {
            // Intent intent = new Intent(SettingsActivity.this,
            // SettingsTrafficActivity.class);
            // startActivity(intent);
            // }
            // 反馈
            else if (position == POSITION_FEEDBACK) {
                startCommitInfoActivity(Constants.SETTINGS_COMMITINFO_FEEDBACK);
            }
            // 帮助
            else if (position == POSITION_HELP) {
                startActivity(new Intent(SettingsActivity.this,
                        HelpActivity.class));
            }
            // 关于
            else if (position == POSITION_ABOUT) {
                Intent intent = new Intent(SettingsActivity.this,
                        AboutActivity.class);
                startActivity(intent);
            }
        }
        // 签名
        else if (parent.getId() == R.id.item_signature) {
            startCommitInfoActivity(Constants.SETTINGS_COMMITINFO_SIGNATURE);
        } else if (parent.getId() == R.id.item_safety) {
            // 手机绑定
            if (position == POSITION_BIND) {
                Intent intent = new Intent(SettingsActivity.this,
                        SettingsBindActivity.class);
                startActivity(intent);
            }
            // 密码修改
            else if (position == POSITION_CHANGEPW) {
                Intent intent = new Intent(SettingsActivity.this,
                        SettingsChangePasswordActivity.class);
                startActivity(intent);
            }
            // 联系人恢复
            else if (position == POSITION_RESTORE) {
                CommonPreferences.saveIsCheckedNewFeatureRestore(true);
                Intent intent = new Intent(SettingsActivity.this,
                        ContactsRestoreActivity.class);
                startActivity(intent);
            }
        } else if (parent.getId() == R.id.item_exit) {
            // 注销
            if (position == 0) {
                showLogoutDialog();
            }
            // 退出
            else if (position == 1) {
                showQuitDialog();
            }
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_QUIT, 0, R.string.quit).setIcon(R.drawable.menu_quit);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_QUIT:
                showQuitDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.tip);
        UserInfo info = CommonPreferences.getUserInfo();
        if (info == null)
            return;
        String name = info.name;
        String msg = getResources().getString(R.string.logout_dialog_msg, new Object[] {
                name
        });
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mService.getSettingsModule().logout();
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                });
        builder.show();
    }

    private void startCommitInfoActivity(int type) {
        Intent intent = new Intent(SettingsActivity.this,
                 SettingsCommitInfoActivity.class);
        intent.putExtra(Constants.SETTINGS_COMMITINFO_TYPE, type);
        startActivity(intent);
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    public void onBackPressed() {
        switchToHome();
    }

    @Override
    public void initTopBar() {
        setTopBarTitle(R.string.settings_title);
    }
}
