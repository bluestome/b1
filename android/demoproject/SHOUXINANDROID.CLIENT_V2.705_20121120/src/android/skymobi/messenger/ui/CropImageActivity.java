
package android.skymobi.messenger.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.FileUtils;
import android.skymobi.messenger.widget.ZoomImageListener;
import android.skymobi.messenger.widget.ZoomImageView;
import android.skymobi.messenger.widget.ZoomState;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @ClassName: CropImageActivity
 * @Description: 图片剪裁
 * @author Lv.Lv
 * @date 2012-4-18 下午2:17:09
 */
public class CropImageActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = CropImageActivity.class.getSimpleName();
    public static final String EXTRA_OUTPUT_FILEPATH = "output"; // 输出图片保存路径
    public static final String EXTRA_OUTPUT_X = "outputX"; // 输出图片宽度
    public static final String EXTRA_OUTPUT_Y = "outputY"; // 输出图片高度
    public static final String EXTRA_BACKMODE = "backmode"; // 返回模式

    public static final int RECHOOSE = 0; // 返回重新选择
    public static final int RECAPTURE = 1; // 返回重新拍照

    private static final int MAX_PIXELS = 1000 * 1000; // 最大像素数
    private String mFilePath = Constants.LARGE_HEAD_PATH + "crop.jpg";
    private int mBackMode = RECHOOSE;
    private int mOutputX = 480;
    private int mOutputY = 480;

    private ZoomImageView mImageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image);
        init();
    }

    private void init() {
        Uri uri = getIntent().getData();
        SLog.d("Crop", "uri: " + uri.toString());
        Bitmap bitmap = FileUtils.getBitmapWithOrientation(this.getContentResolver(), uri,
                MAX_PIXELS);

        Bundle bdl = getIntent().getExtras();
        if (bdl != null) {
            if (bdl.containsKey(EXTRA_OUTPUT_FILEPATH))
                mFilePath = bdl.getString(EXTRA_OUTPUT_FILEPATH);
            if (bdl.containsKey(EXTRA_OUTPUT_X))
                mOutputX = bdl.getInt(EXTRA_OUTPUT_X);
            if (bdl.containsKey(EXTRA_OUTPUT_Y))
                mOutputY = bdl.getInt(EXTRA_OUTPUT_Y);
            if (bdl.containsKey(EXTRA_BACKMODE))
                mBackMode = bdl.getInt(EXTRA_BACKMODE);
        }

        // 标题
        TextView listTitle = (TextView) findViewById(R.id.list_title);
        listTitle.setText(R.string.crop_title);

        // ZoomImageView
        ZoomState zoomState = new ZoomState();
        ZoomImageListener zoomListener = new ZoomImageListener();
        zoomListener.setZoomState(zoomState);
        mImageView = (ZoomImageView) findViewById(R.id.imageview);
        mImageView.setZoomState(zoomState);
        mImageView.setOnTouchListener(zoomListener);
        mImageView.setCropMode(true);
        mImageView.setImageBitmap(bitmap);

        // save button
        Button saveBtn = (Button) findViewById(R.id.crop_save);
        saveBtn.setOnClickListener(this);
        // cancel button
        Button cancelBtn = (Button) findViewById(R.id.crop_cancel);
        cancelBtn.setOnClickListener(this);
        if (mBackMode == RECAPTURE) {
            cancelBtn.setText(getString(R.string.crop_recapture));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.crop_save:
                Bitmap bm = mImageView.getCropBitmap(mOutputX, mOutputY);
                if (bm != null) {
                    FileUtils.SaveBitmap2File(bm, mFilePath);
                    bm.recycle();
                }
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.crop_cancel:
                Intent data = new Intent();
                data.putExtra(EXTRA_BACKMODE, mBackMode);
                setResult(RESULT_CANCELED, data);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        SLog.d(TAG, "onDestroy releaseBitmap");
        mImageView.releaseBitmap();
        super.onDestroy();
    }

}
