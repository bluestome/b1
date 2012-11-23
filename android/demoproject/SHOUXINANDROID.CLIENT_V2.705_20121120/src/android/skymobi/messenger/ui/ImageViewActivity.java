
package android.skymobi.messenger.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.ImageUtils;
import android.skymobi.messenger.utils.StringUtil;
import android.skymobi.messenger.widget.ZoomImageView;
import android.skymobi.messenger.widget.ZoomState;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ProgressBar;

import java.io.File;

/**
 * @ClassName: ImageViewActivity
 * @Description: 显示图片
 * @author Lv.Lv
 * @date 2012-4-16 下午1:55:28
 */
public class ImageViewActivity extends BaseActivity implements OnTouchListener {

    private static final String TAG = ImageViewActivity.class.getSimpleName();

    public static final String HEADPHOTO_FILENAME = "headphoto";

    private ProgressBar mProgressBar = null;
    private String mFilePath = null;
    private ZoomImageView mImageView = null;
    private Bitmap mBm = null;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SETTINGS_DOWNLOAD_HEADPHOTO_FAIL:
                    hideWaitDialog();
                    if (null != msg.obj) {
                        showToast(getString(R.string.network_error) + "\r\n[" + Constants.ERROR_TIP
                                + ":0x" + StringUtil.autoFixZero((Integer) msg.obj) + "]");
                    } else {
                        showToast(R.string.network_error);
                    }
                    finish();
                    break;
                case CoreServiceMSG.MSG_SETTINGS_DOWNLOAD_HEADPHOTO_SUCCESS:
                    hideWaitDialog();
                    mBm = ImageUtils.JpegToBitmap(mFilePath);
                    if (mBm != null)
                        mImageView.setImageBitmap(mBm);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);
        init();
    }

    private void init() {
        mImageView = (ZoomImageView) findViewById(R.id.imageview);
        ZoomState zoomState = new ZoomState();
        mImageView.setZoomState(zoomState);
        mImageView.setOnTouchListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        Bundle bdl = getIntent().getExtras();
        if (bdl == null) {
            return;
        }
        String filename = bdl.getString(HEADPHOTO_FILENAME);
        if (TextUtils.isEmpty(filename)) {
            finish();
            return;
        }
        mFilePath = getHeadPhotoFilePath(filename, true);
        File file = new File(mFilePath);
        // 不存在，下载
        if (!file.exists()) {
            SettingsModule settingsModule = mService.getSettingsModule();
            settingsModule.downloadImage(filename, Constants.SETTINGS_LARGE_HEAD_WIDTH);
            showWaitDialog();
            mBm = ImageUtils.JpegToBitmap(getHeadPhotoFilePath(filename, false));
        } else {
            mBm = ImageUtils.JpegToBitmap(mFilePath);
        }

        if (mBm != null)
            mImageView.setImageBitmap(mBm);
    }

    // 通过url获取文件全路径，isLarge = true 返回大图路径 false 返回小图路径
    private String getHeadPhotoFilePath(String filename, boolean isLarge) {
        if (TextUtils.isEmpty(filename))
            return "";

        if (isLarge)
            return Constants.LARGE_HEAD_PATH + filename;
        else
            return Constants.HEAD_PATH + filename;
    }

    private void showWaitDialog() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideWaitDialog() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.imageview) {
            // progressbar不可见的时候，touch即退出
            if (!(mProgressBar != null && mProgressBar.isShown())) {
                finish();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        SLog.d(TAG, "onDestroy releaseBitmap");
        mImageView.releaseBitmap();
        super.onDestroy();
    }

}
