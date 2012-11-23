
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.SettingsListAdapter;
import android.skymobi.messenger.adapter.ViewsAdapter;
import android.skymobi.messenger.bean.SettingsItem;
import android.skymobi.messenger.database.dao.CitysDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.service.module.SettingsModule;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.DateUtil;
import android.skymobi.messenger.utils.FileUtils;
import android.skymobi.messenger.utils.HeaderCache;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.skymobi.messenger.utils.dialog.ToastTool;
import android.skymobi.messenger.widget.CityWheelPickerDialog;
import android.skymobi.messenger.widget.CornerListView;
import android.skymobi.messenger.widget.DateWheelPickerDialog;
import android.skymobi.messenger.widget.WheelPickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: PersonalSettingsActivity
 * @Description: TODO
 * @author Lv.Lv
 * @date 2012-2-29 下午3:16:49
 */
public class PersonalSettingsActivity extends TopActivity implements
        OnItemClickListener, OnCheckedChangeListener {

    private static final String TAG = PersonalSettingsActivity.class.getSimpleName();
    private static final int DIALOG_SETHEADPHOTO = 0;
    private static final int DIALOG_BIRTHDAY = 1;
    private static final int DIALOG_HOMETOWN = 2;
    private static final int SELECT_PHOTO = 0;
    private static final int CAPTURE_PHOTO = 1;
    private static final int CROP_PHOTO = 2;

    private final String mCropFilePath = Constants.LARGE_HEAD_PATH + "crop.jpg"; // crop出来文件生成路径
    private final String mCaptureFilePath = Constants.LARGE_HEAD_PATH + "capture.jpg"; // 拍照文件生成路径
    private Uri mTempFileUri = null; // 保存调用camera拍照生成的文件
    private Bitmap mCropBitmap = null;
    private SettingsModule settingsModule = null;
    private NetUserInfo mUserInfo = null;
    private ImageView mHeadPhoto = null;
    private TextView mNicknameTv = null;
    private RadioButton mMaleButton = null;
    private RadioButton mFemaleButton = null;
    private SettingsListAdapter mOthersAdapter = null;
    private ProgressDialog mWaitDialog = null;
    private boolean mIsIniting = false; // 正在初始化，防止出现“信息修改成功”的错误报告
    private boolean mDelayUploadImage = false; // 当mUserInfo为null时，等待mUserInfo获取后再上传
    // Handle message
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_FAIL:
                case CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_FAIL:
                    // 如果mDelayUploadImage为true，则表示正在做延时等待上传头像的处理，不隐藏等待对话框，也不显示网络异常提醒
                    // TODO 重新载入当前界面
                    String birthday = SettingsPreferences.getBirthday();
                    // 更新生日为最开始的生日
                    mOthersAdapter.updateItem(0, birthday);
                    if (mWaitDialog != null && mWaitDialog.isShowing() && !mDelayUploadImage) {
                        hideWaitDialog();
                        if (null != msg.obj) {
                            showToast(getString(R.string.network_error) + "\r\n["
                                    + Constants.ERROR_TIP + ":0x"
                                    + StringUtil.autoFixZero((Integer) msg.obj) + "]");
                        } else {
                            showToast(R.string.network_error);
                        }
                    }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_ILLEGAL:
                    // 用户昵称不合法
                    if (mWaitDialog != null && mWaitDialog.isShowing() && !mDelayUploadImage) {
                        hideWaitDialog();
                        showToast(R.string.setting_nickname_illegal);
                    }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_SUCCESS:
                    NetUserInfo useinfo = (NetUserInfo) msg.obj;
                    mUserInfo = useinfo;
                    if (mDelayUploadImage) {
                        mDelayUploadImage = false;
                        settingsModule.uploadImage(FileUtils.Bitmap2Bytes(mCropBitmap));
                        showWaitDialog(getString(R.string.settings_uploading_headphoto));
                    }
                    updateStatus();
                    break;
                case CoreServiceMSG.MSG_SETTINGS_SET_USERINFO_SUCCESS: {
                    if (mWaitDialog != null && mWaitDialog.isShowing()) {
                        hideWaitDialog();
                        showToast(R.string.settings_userinfo_success);
                    }
                    NetUserInfo userinfo = (NetUserInfo) msg.obj;
                    mUserInfo = userinfo;
                }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_UPLOAD_HEADPHOTO_FAIL:
                    hideWaitDialog();
                    if (null != msg.obj) {
                        showToast(getString(R.string.network_error) + "\r\n[" + Constants.ERROR_TIP
                                + ":0x" + StringUtil.autoFixZero((Integer) msg.obj) + "]");
                    } else {
                        showToast(R.string.network_error);
                    }
                    break;
                case CoreServiceMSG.MSG_SETTINGS_UPLOAD_HEADPHOTO_SUCCESS:
                    String headphoto = (String) msg.obj;
                    FileUtils.SaveBitmap2File(mCropBitmap, Constants.LARGE_HEAD_PATH
                            + headphoto);

                    // 小头像保存
                    Bitmap bitmap = Bitmap.createScaledBitmap(mCropBitmap,
                            Constants.SETTINGS_SMALL_HEAD_WIDTH,
                            Constants.SETTINGS_SMALL_HEAD_WIDTH, true);
                    FileUtils.SaveBitmap2File(bitmap, Constants.HEAD_PATH
                            + headphoto);
                    if (checkUserInfoValid()) {
                        mUserInfo.setUuidPortrait(headphoto);
                        SettingsPreferences.saveHeadPhoto(headphoto);
                        HeaderCache.getInstance().getHeader(headphoto, null, mHeadPhoto);
                        settingsModule.setNetUserInfo(mUserInfo, SettingsModule.TYPE_HEADEPHOT);
                    }
                    hideWaitDialog();
                    showToast(R.string.settings_upload_headphoto_success);
                    mCropBitmap.recycle();
                    mCropBitmap = null;
                    break;
                case CoreServiceMSG.MSG_SETTINGS_RESERVE:
                    initServiceModule();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personalsetttings);
        if (savedInstanceState != null) {
            mTempFileUri = savedInstanceState.getParcelable("mTempFileUri");
        }
        initServiceModule();
        mIsIniting = true;
        initTopBar();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        mIsIniting = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideWaitDialog();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("mTempFileUri", mTempFileUri);
    }

    /***
     * 初始化service相关数据。 在切换到后台（比如启动camera）且内存不足的情况下，activity和service会被kill掉，
     * 切换回来后，有可能service还没启动好。此时需要等待service启动。
     */
    private void initServiceModule() {
        if (mService == null) {
            mService = CoreService.getInstance();
        }
        if (mService == null) {
            if (mWaitDialog == null || !mWaitDialog.isShowing()) {
                showWaitDialog(getString(R.string.settings_sync_wait));
            }
            mHandler.sendMessageDelayed(Message.obtain(mHandler,
                    CoreServiceMSG.MSG_SETTINGS_RESERVE, null), 300);
        } else {
            mService.registerCallBack(this);
            hideWaitDialog();
            settingsModule = mService.getSettingsModule();
            mUserInfo = MainApp.i().getNetLUserInfo();
            if (mUserInfo == null) {
                settingsModule.getUserInfo();
                if (mDelayUploadImage) {
                    showWaitDialog(getString(R.string.settings_uploading_headphoto));
                }
            } else {
                if (mDelayUploadImage) {
                    mDelayUploadImage = false;
                    settingsModule.uploadImage(FileUtils.Bitmap2Bytes(mCropBitmap));
                    showWaitDialog(getString(R.string.settings_uploading_headphoto));
                }
            }
        }
    }

    private void init() {
        LayoutInflater inflater = getLayoutInflater();

        // 设置头像
        List<View> views = new ArrayList<View>();
        View setphoto = inflater.inflate(R.layout.set_headphoto_item, null);
        TextView tv = (TextView) setphoto.findViewById(R.id.text_setheadphoto);
        tv.setText(R.string.settings_setheadphoto);
        mHeadPhoto = (ImageView) setphoto.findViewById(R.id.icon_setheadphoto);
        views.add(setphoto);

        ViewsAdapter adapter = new ViewsAdapter(views);
        CornerListView lv = (CornerListView) findViewById(R.id.item_setheadphoto);
        lv.setAdapter(adapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEADPHOTO_HEIGHT);
        lv.setOnItemClickListener(this);

        // 设置昵称&性别
        views = new ArrayList<View>();
        View setnickname = inflater.inflate(R.layout.personalsettings_item, null);
        TextView title = (TextView) setnickname.findViewById(R.id.settings_item_title);
        title.setText(R.string.settings_nickname);
        mNicknameTv = (TextView) setnickname.findViewById(R.id.settings_item_content);
        views.add(setnickname);

        View setsex = inflater.inflate(R.layout.set_sex_item, null);
        mMaleButton = (RadioButton) setsex.findViewById(R.id.radiomale);
        mMaleButton.setOnCheckedChangeListener(this);
        mFemaleButton = (RadioButton) setsex.findViewById(R.id.radiofemale);
        // mFemaleButton.setOnCheckedChangeListener(this);

        views.add(setsex);

        adapter = new ViewsAdapter(views);
        lv = (CornerListView) findViewById(R.id.item_nickname_sex);
        lv.setAdapter(adapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);

        // 其它设置
        List<SettingsItem> list = new ArrayList<SettingsItem>();
        list.add(new SettingsItem(mContext, R.string.contacts_detail_birthday,
                0, true, false, 0, true));
        // fix bug: 临时屏蔽掉地区显示和设置选项
        // list.add(new SettingsItem(mContext,
        // R.string.contacts_detail_hometown,
        // 0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.contacts_detail_school,
                0, true, false, 0, true));
        list.add(new SettingsItem(mContext, R.string.contacts_detail_corporation,
                0, true, false, 0, true));
        mOthersAdapter = new SettingsListAdapter(mContext, list);
        mOthersAdapter.setResource(R.layout.personalsettings_item);
        lv = (CornerListView) findViewById(R.id.item_otherinfo);
        lv.setAdapter(mOthersAdapter);
        lv.setListViewHeight(Constants.SETTINGS_ITEM_HEIGHT);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        switch (parentId) {
            case R.id.item_setheadphoto:
                /*
                 * 先判断有没有sdcard,再判断SDcard是否已满 *
                 */
                if (!AndroidSysUtils.isAvailableSDCard(mContext)) {
                    ToastTool.showShort(mContext, R.string.no_sdcard_tip);
                    return;
                } else if (AndroidSysUtils.getAvailableStore() < Constants.SDCARD_MIN_CAPACITY) {
                    ToastTool.showAtCenterShort(mContext, R.string.sdcard_full);
                    return;
                }
                showDialog(DIALOG_SETHEADPHOTO);
                break;
            case R.id.item_nickname_sex:
                // 设置昵称
                if (position == 0) {
                    startCommitInfoActivity(Constants.SETTINGS_COMMITINFO_NICKNAME);
                }
                break;
            case R.id.item_otherinfo:
                // 设置生日
                if (position == 0) {
                    showDialog(DIALOG_BIRTHDAY);
                }
                // // 设置地区
                // else if (position == 1) {
                // showDialog(DIALOG_HOMETOWN);
                // }
                // 设置学校
                else if (position == 1) {
                    startCommitInfoActivity(Constants.SETTINGS_COMMITINFO_SCHOOL);
                }
                // 设置单位
                else if (position == 2) {
                    startCommitInfoActivity(Constants.SETTINGS_COMMITINFO_CORPORATION);
                }
                break;
            default:
                break;
        }
    }

    private void startCommitInfoActivity(int type) {
        Intent intent = new Intent(PersonalSettingsActivity.this,
                SettingsCommitInfoActivity.class);
        intent.putExtra(Constants.SETTINGS_COMMITINFO_TYPE, type);
        startActivity(intent);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_SETHEADPHOTO:
                return new AlertDialog.Builder(PersonalSettingsActivity.this)
                        .setTitle(R.string.settings_setheadphoto)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setItems(R.array.select_dialog_items_photo,
                                setHeadphotoDlgListener)
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                })
                        .create();
            case DIALOG_BIRTHDAY: {
                String birthday = SettingsPreferences.getBirthday();
                DateWheelPickerDialog dlg = new DateWheelPickerDialog(this, mBirthdaySetListener,
                        Integer.parseInt(DateUtil.getFormatMonth(birthday)) - 1,
                        Integer.parseInt(DateUtil.getFormatDate(birthday)) - 1,
                        Integer.parseInt(DateUtil.getYear(birthday)));
                dlg.setEND_YEAR(DateUtil.getCurrentYear());
                dlg.setSTART_YEAR(DateUtil.get100YearBefore2());
                dlg.setTitle(R.string.contacts_detail_birthday);
                return dlg;
            }
            case DIALOG_HOMETOWN: {
                CityWheelPickerDialog dlg = new CityWheelPickerDialog(this,
                        mHometownSetListener,
                        0, 0);
                dlg.setTitle(R.string.contacts_detail_hometown);
                return dlg;
            }
            default:
                break;
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    startCropPhoto(selectedImage, SELECT_PHOTO);
                }
                break;
            case CAPTURE_PHOTO:
                if (resultCode == RESULT_OK) {
                    startCropPhoto(mTempFileUri, CAPTURE_PHOTO);
                }
                break;
            case CROP_PHOTO:
                hideWaitDialog();
                if (resultCode == RESULT_OK) {
                    mCropBitmap = BitmapFactory.decodeFile(mCropFilePath);
                    if (mCropBitmap == null)
                        return;
                    if (checkUserInfoValid()) {
                        settingsModule.uploadImage(FileUtils.Bitmap2Bytes(mCropBitmap));
                    } else {
                        /***
                         * 在切换到后台（比如启动camera）且内存不足的情况下，activity和service会被kill掉，
                         * 切换回来后 ，有可能service还没启动好，settingsModule可能为null。
                         * 此处要做非null判断
                         */
                        if (settingsModule != null) {
                            settingsModule.getUserInfo();
                        }
                        mDelayUploadImage = true;
                    }
                    showWaitDialog(getString(R.string.settings_uploading_headphoto));
                } else if (resultCode == RESULT_CANCELED) {
                    if (data != null) {
                        Bundle bdl = data.getExtras();
                        if (bdl != null) {
                            int backmode = bdl.getInt(CropImageActivity.EXTRA_BACKMODE);
                            if (backmode == CropImageActivity.RECHOOSE) {
                                startSelectPhoto();
                            } else if (backmode == CropImageActivity.RECAPTURE) {
                                startCapturePhoto();
                            }
                        }
                    }
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // 修改头像对话框Listener
    private final DialogInterface.OnClickListener setHeadphotoDlgListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == 0) {
                startCapturePhoto();
            } else if (which == 1) {
                startSelectPhoto();
            }
        }
    };

    // 生日设置Listener
    private final WheelPickerDialog.OnDateSetListener mBirthdaySetListener =
            new WheelPickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(View view, int monthOfYear, int dayOfMonth,
                        int year) {
                    // 判断当前月和日期是否大于当前时间的月和日期。
                    String parten = "00";
                    DecimalFormat decimal = new DecimalFormat(parten);
                    String birthday = year + "-" + decimal.format(monthOfYear + 1) + "-"
                            + decimal.format(dayOfMonth + 1);
                    // 与原先一致，返回
                    if (birthday.equals(SettingsPreferences.getBirthday())) {
                        return;
                    }
                    long timeBirthday = DateUtil.getDate(birthday).getTime();
                    long timeToday = System.currentTimeMillis();
                    long time100YearBefore = DateUtil.get100YearBefore();
                    SLog.d("setting", "timeBirthday = " + timeBirthday);
                    SLog.d("setting", "timeToday = " + timeToday);
                    SLog.d("setting", "time100YearBefore = " + time100YearBefore);
                    if (timeBirthday > timeToday) {
                        showToast(R.string.setting_birthday_error_more);
                        return;
                    }

                    if (timeBirthday < time100YearBefore) {
                        showToast(R.string.setting_birthday_error_less);
                        return;
                    }

                    CornerListView lv = (CornerListView) findViewById(R.id.item_otherinfo);
                    TextView tv = (TextView) lv.getChildAt(0).findViewById(
                            R.id.settings_item_content);
                    tv.setText(DateUtil.getFormatYearMonthAndDate(birthday));

                    if (checkUserInfoValid()) {
                        tv.invalidate();
                        showWaitDialog(getString(R.string.settings_waitting));
                        mUserInfo.setUbirthday(birthday);
                        settingsModule.setNetUserInfo(mUserInfo, settingsModule.TYPE_BIRTHDAY);
                    }
                }
            };

    // 地区设置Listener
    private final WheelPickerDialog.OnDateSetListener mHometownSetListener =
            new WheelPickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(View view, int provice, int city,
                        int reserved) {
                    CitysDAO dao = DaoFactory.getInstance(mContext).getCitysDAO();
                    String provincename = dao.getProvinceName(provice);
                    String cityname = dao.getCityName(provincename, city);

                    CornerListView lv = (CornerListView) findViewById(R.id.item_otherinfo);
                    TextView tv = (TextView) lv.getChildAt(1).findViewById(
                            R.id.settings_item_content);
                    if (cityname != null && !cityname.equals(provincename)) {
                        tv.setText(provincename + " " + cityname);
                    } else {
                        tv.setText(provincename);
                    }

                    if (checkUserInfoValid()) {
                        mUserInfo.setUprovince(provincename);
                        mUserInfo.setUcity(cityname);
                        settingsModule.setNetUserInfo(mUserInfo, settingsModule.TYPE_HOMETOWN);
                    }
                }
            };

    private void startSelectPhoto() {
        try {
            Intent intent = new Intent(
                    Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent,
                    getString(R.string.settings_from_gallery)),
                    SELECT_PHOTO);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showToast(getString(R.string.settings_photo_picker_notfound));
        }
    }

    private void startCapturePhoto() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (mTempFileUri == null) {
                File file = new File(mCaptureFilePath);
                mTempFileUri = Uri.fromFile(file);
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempFileUri);
            startActivityForResult(cameraIntent, CAPTURE_PHOTO);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showToast(getString(R.string.settings_photo_picker_notfound));
        }
    }

    /**
     * 启动crop image activity
     * 
     * @param uri 用于crop的文件uri
     * @param requestCode 区分图片是拍照生成还是选择而来
     */
    private void startCropPhoto(Uri uri, int requestCode) {
        Intent intent = new Intent(PersonalSettingsActivity.this, CropImageActivity.class);
        intent.setData(uri);
        intent.putExtra(CropImageActivity.EXTRA_OUTPUT_FILEPATH, mCropFilePath);
        intent.putExtra(CropImageActivity.EXTRA_OUTPUT_X, 480);
        intent.putExtra(CropImageActivity.EXTRA_OUTPUT_Y, 480);
        intent.putExtra(CropImageActivity.EXTRA_BACKMODE,
                requestCode == CAPTURE_PHOTO ? CropImageActivity.RECAPTURE
                        : CropImageActivity.RECHOOSE);
        try {
            startActivityForResult(intent, CROP_PHOTO);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showToast(getString(R.string.settings_photo_picker_notfound));
        }
    }

    @Override
    public void notifyObserver(int what, Object obj) {
        mHandler.sendMessage(Message.obtain(mHandler, what, obj));
    }

    // 显示内容更新
    private void updateStatus() {
        // 头像
        if (mHeadPhoto != null) {
            String headphoto = SettingsPreferences.getHeadPhoto();
            HeaderCache.getInstance().getHeader(headphoto, null, mHeadPhoto);
        }
        // 昵称
        if (mNicknameTv != null) {
            mNicknameTv.setText(SettingsPreferences.getNickname());
        }
        // 性别
        boolean isMale = SettingsPreferences.getSex().equals(SettingsPreferences.Male);
        if (mMaleButton != null && mFemaleButton != null) {
            mMaleButton.setChecked(isMale);
            mFemaleButton.setChecked(!isMale);
        }

        if (mOthersAdapter != null) {
            // 生日
            String birthday = SettingsPreferences.getBirthday();
            String formatbirthday = (birthday.length() == 0) ? "" : DateUtil
                    .getFormatYearMonthAndDate(birthday);
            mOthersAdapter.updateItem(0, formatbirthday);
            // 地区
            // String place = SettingsPreferences.getProvince();
            // String city = SettingsPreferences.getCity();
            // // 直辖市省份和城市名一样，如“北京”，只显示直辖市名
            // if (!city.equals(place)) {
            // place = place.concat(" ").concat(city);
            // }
            // mOthersAdapter.updateItem(1, place);
            // 学校
            String school = SettingsPreferences.getSchool();
            mOthersAdapter.updateItem(1, school);
            // 单位
            String corp = SettingsPreferences.getCorporation();
            mOthersAdapter.updateItem(2, corp);
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mIsIniting) {
            return;
        }

        if (!checkUserInfoValid()) {
            return;
        }

        if (buttonView.getId() == R.id.radiomale) {
            String sexStr = isChecked ? SettingsPreferences.Male : SettingsPreferences.Female;
            mUserInfo.setUsex(sexStr);
            settingsModule.setNetUserInfo(mUserInfo, settingsModule.TYPE_SEX);
        }
    }

    private void showWaitDialog(String message) {
        if (mWaitDialog == null) {
            mWaitDialog = new ProgressDialog(this);
            mWaitDialog.setIndeterminate(true);
            mWaitDialog.setCancelable(false);
        }
        mWaitDialog.setMessage(message);
        mWaitDialog.show();
    }

    private void hideWaitDialog() {
        if (mWaitDialog != null) {
            mWaitDialog.cancel();
        }
    }

    private boolean checkUserInfoValid() {
        if (mUserInfo == null) {
            mHandler.sendMessage(Message.obtain(mHandler,
                    CoreServiceMSG.MSG_SETTINGS_GET_USERINFO_FAIL, null));
            if (settingsModule != null) {
                settingsModule.getUserInfo();
            }
            return false;
        }
        return true;
    }

    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.settings_personal);
    }
}
