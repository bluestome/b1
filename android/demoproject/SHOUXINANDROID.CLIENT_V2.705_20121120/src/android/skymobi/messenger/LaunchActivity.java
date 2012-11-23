
package android.skymobi.messenger;

import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.os.Handler;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.logreport.FreeSkyAgent;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.LifeService;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.ui.DescriptionActivity;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.CommonPreferences;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LaunchActivity extends BaseActivity {
    private static final String TAG = "LaunchActivity";
    public static final int SHORTCUT_DIALOG = 101; // 退出
    public static final int MENU_QUIT = Menu.FIRST + 1; // 退出
    private boolean showMenu = false;
    private ProgressBar downloadProgress;
    private TextView downloadPercent;

    // 定义一个handle 去定时检查CoreService是否初始化完成
    private final Handler mHandler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SLog.d(TAG, "on create Begin");
        setContentView(R.layout.launch);

        downloadProgress = (ProgressBar) findViewById(R.id.download_progressbar);
        downloadPercent = (TextView) findViewById(R.id.download_progressPercent);
        FreeSkyAgent.initAgent(this);
        if (LifeService.isRunning()) {
            // 服务已经启动，说明应用已经在后台运行，那么直接跳过启动页
            // nextPage(); 暂时注释，防止不能检查更新，待2.7.5更新模块修改后放开 by zzy
            // this.finish();
        } else {
            MainApp.i().startLifeService();
        }

        // 检查bizService是否初始化完成
        mHandler.postDelayed(rCheckService, 500);

        SLog.d(TAG, "on create End");
    }

    private void addShortcut() {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT"); // 快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name)); //
        shortcut.putExtra("duplicate", false); // 不允许重复创建
        Intent intent = new Intent(this, LaunchActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent); // 快捷方式的图标
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this,
                R.drawable.ic_launcher);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        sendBroadcast(shortcut);
        CommonPreferences.setShortCutSetting(true);
    }

    // 定时500ms检查一次CoreService是否实例化完成
    private final Runnable rCheckService = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "check service after 1000ms");
            if (CoreService.getInstance() == null) {
                mHandler.postDelayed(rCheckService, 1000);
            } else {
                try {
                    if (!CommonPreferences.getShortCutSetting()
                            && !AndroidSysUtils.hasShortcut(mContext,
                                    "com.android.launcher.permission.WRITE_SETTINGS")) {
                        // 判断是否存在快捷方式
                        addShortcut();
                        // 发送终端信息日志
                        MainApp.i().getLcsBU().uploadLcsComplexLog();
                    } else {
                        SLog.d(TAG, "图标已经添加，不需要在添加图标了!");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "图标添加出现异常,执行默认添加图标");
                }
                checkUpdate();
            }
        }
    };

    /**
     * service启动完成后，进行检查更新等操作
     */
    private void checkUpdate() {
        mService = CoreService.getInstance();
        mService.registerCallBack(updateCallback);
        long lastDownloadTime = CommonPreferences.getLastDownloadTime();
        int loginTimes = CommonPreferences.getLoginTimes();

        boolean isForce = CommonPreferences.getIsForce();
        String newversion = CommonPreferences.getAppVerion();
        String localversion = MainApp.i().getPi().versionName;

        int checkAfterTimes = CommonPreferences.getCheckAfterTimes(); // 登录的次数
        int checkInterval = CommonPreferences.getCheckInterval();// 间隔的天数
        long checkMillis = DateUtils.DAY_IN_MILLIS * checkInterval;

        boolean isCheckTimes = checkInterval > 0 ? (loginTimes >= checkAfterTimes) : true;
        boolean isCheckInterval = lastDownloadTime > 0 ? (System.currentTimeMillis()
                - lastDownloadTime >= checkMillis) : true;
        if (((isCheckTimes || isCheckInterval))
                || (isForce && !newversion.equals(localversion))) {
            CommonPreferences.saveLastDownloadTime(System.currentTimeMillis());
            if (loginTimes >= checkAfterTimes && !isForce) {
                CommonPreferences.saveLoginTimes(0);
            }
            mService.getSettingsModule().checkUpdate(true);
        } else {
            startNextPage();
        }
    }

    /**
     * 启动下一界面
     */
    public void startNextPage() {
        // anson.yang 20120920修改
        // 对比前一次版本(如果第一次安装,默认 0)与当前版本,如果两个版本不一致,则显示引导页
        int lastDescVersion = CommonPreferences.getLastDescVerion();
        int currentVersion = MainApp.i().getPi().versionCode;
        // boolean descReadedStatus = CommonPreferences.getDescReadedStatus();
        SLog.d(TAG, "lastDescVersion:" + lastDescVersion + ",currentVersion:" + currentVersion);
        finish();
        if (currentVersion == lastDescVersion || currentVersion == 0) {
            nextPage();
        } else {
            startActivity(new Intent(this, DescriptionActivity.class));
        }
    }

    public void progress(int size) {
        SLog.d(TAG, "size:" + size);
        downloadProgress.setProgress(size);
        float num = (float) downloadProgress.getProgress() / (float) downloadProgress.getMax();
        SLog.d(TAG, "num:" + num);
        int percent = (int) (num * 100);
        downloadPercent.setText(percent + "%");
        SLog.d(TAG, "percent:" + percent);
        if (downloadProgress.getProgress() == downloadProgress.getMax()) { // 下载完成
                                                                           // 直接安装

        }
    }

    public void enableProcess(boolean enable, int fileLen) {
        downloadProgress.setMax(fileLen);
        showMenu = enable;
        findViewById(R.id.download_layout).setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (showMenu) {
            menu.clear();
            menu.add(0, MENU_QUIT, 0, R.string.quit).setIcon(R.drawable.menu_quit);
        }
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MainApp.i().stopService();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
