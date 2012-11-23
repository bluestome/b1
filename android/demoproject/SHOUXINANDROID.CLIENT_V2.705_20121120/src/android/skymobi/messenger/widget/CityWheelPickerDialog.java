
package android.skymobi.messenger.widget;

import android.content.Context;
import android.skymobi.messenger.database.dao.CitysDAO;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.widget.wheelview.ArrayWheelAdapter;
import android.skymobi.messenger.widget.wheelview.OnWheelChangedListener;
import android.skymobi.messenger.widget.wheelview.WheelView;
import android.util.Log;

import java.util.ArrayList;

/**
 * @ClassName: CityWheelPickerDialog
 * @Description: 城市选择，滚动样式的Picker Dialog
 * @author Lv.Lv
 * @date 2012-3-7 下午4:13:38
 */
public class CityWheelPickerDialog extends WheelPickerDialog {

    private final static String TAG = CityWheelPickerDialog.class.getSimpleName();

    private CitysDAO dao;

    /***
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param province The initial province id of the dialog.
     * @param city The initial city id of the dialog.
     */
    public CityWheelPickerDialog(Context context,
            OnDateSetListener callBack,
            int province,
            int city) {
        super(context, callBack, province, city, 0);
    }

    @Override
    protected void initPicker() {
        dao = DaoFactory.getInstance(mContext).getCitysDAO();

        // 省份
        ArrayList<String> list = dao.getAllProvinces();
        if (list == null || list.size() <= 0) {
            Log.e(TAG, "get province error");
            return;
        }
        String[] array = list.toArray(new String[list.size()]);
        mPicker.setAdapter(new ArrayWheelAdapter<String>(array, 6));
        mPicker.setCyclic(true);
        mPicker.setCurrentItem(mInitialData);

        // 城市
        resetCityAdapter(mInitialData);
        mPicker2.setCyclic(false);
        mPicker2.setCurrentItem(mInitialData2);

        // 省份改变listener
        OnWheelChangedListener wheelListener_province = new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                resetCityAdapter(newValue);
            }
        };
        mPicker.addChangingListener(wheelListener_province);

    }

    /** 根据省份调整城市显示 */
    private void resetCityAdapter(int province) {
        ArrayList<String> list = dao.getCitysInProvince(mPicker.getAdapter().getItem(
                (province)));
        if (list == null || list.size() <= 0) {
            Log.e(TAG, "get province error");
            return;
        }
        String[] array = list.toArray(new String[list.size()]);
        mPicker2.setAdapter(new ArrayWheelAdapter<String>(array, 10));
        mPicker2.setCurrentItem(0);
        if (list.size() < mPicker2.getVisibleItems())
            mPicker2.setCyclic(false);
        else
            mPicker2.setCyclic(true);
    }

}
