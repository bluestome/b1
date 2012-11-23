
package android.skymobi.messenger.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.skymobi.messenger.R;
import android.skymobi.messenger.widget.wheelview.NumericWheelAdapter;
import android.skymobi.messenger.widget.wheelview.OnWheelChangedListener;
import android.skymobi.messenger.widget.wheelview.WheelView;
import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: DateWheelPickerDialog
 * @Description: 日期选择，滚动样式的Picker Dialog， 目前不支持年份选择，只支持月、日选择。返回的月、日从0开始计算
 * @author Lv.Lv
 * @date 2012-3-7 下午4:13:38
 */
public class DateWheelPickerDialog extends WheelPickerDialog {

    // 月份列表，区分大小月
    private static final String[] months_big = {
            "1", "3", "5", "7", "8", "10", "12"
    };
    private static final String[] months_little = {
            "4", "6", "9", "11"
    };

    private static int START_YEAR = 1900;
    private static int END_YEAR = 2012;

    private final List<String> list_big = Arrays.asList(months_big);
    private final List<String> list_little = Arrays.asList(months_little);

    /***
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param month The initial month. The first month of the year should have
     *            value 0.
     * @param day The initial day. The first day of the month should have value
     *            0.
     * @param year The initial year.
     */
    public DateWheelPickerDialog(Context context,
            OnDateSetListener callBack,
            int month,
            int day,
            int year) {
        super(context, callBack, month, day, year);

        mPicker3.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initPicker() {
        int month = mInitialData;
        int day = mInitialData2;
        int year = mInitialData3 - START_YEAR;
        // 月
        mPicker.setAdapter(new NumericWheelAdapter(1, 12, "%02d"));
        mPicker.setCyclic(true);
        mPicker.setLabel(mContext.getString(R.string.month));
        mPicker.setCurrentItem(month);

        // 日
        mPicker2.setCyclic(true);
        resetDayAdapter(year, month);
        mPicker2.setLabel(mContext.getString(R.string.day));
        mPicker2.setCurrentItem(day);

        // 年
        mPicker3.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));
        mPicker3.setCyclic(true);
        mPicker3.setLabel(mContext.getString(R.string.year));
        mPicker3.setCurrentItem(year);

        // 月份改变listener
        OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int year_num = mPicker3.getCurrentItem() + START_YEAR;
                resetDayAdapter(year_num, newValue);
            }
        };

        // 年份改变listener
        OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int year = newValue + START_YEAR;
                int month = mPicker.getCurrentItem();
                resetDayAdapter(year, month);
            }
        };
        mPicker.addChangingListener(wheelListener_month);
        mPicker3.addChangingListener(wheelListener_year);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mCallBack != null) {
            mCallBack.onDateSet(mView,
                    mPicker.getCurrentItem(),
                    mPicker2.getCurrentItem(),
                    mPicker3.getCurrentItem() + START_YEAR);
        }
    }

    /** 根据月份调整当月天数 */
    private void resetDayAdapter(int year, int month) {
        // 月份从0开始
        month = month + 1;

        // 不同月份对应天数不同，分大小月和2月
        if (list_big.contains(String.valueOf(month))) {
            mPicker2.setAdapter(new NumericWheelAdapter(1, 31, "%02d"));
        } else if (list_little.contains(String.valueOf(month))) {
            mPicker2.setAdapter(new NumericWheelAdapter(1, 30, "%02d"));
        } else {
            // 2月天数看是否闰年
            if ((year % 4 == 0 && year % 100 != 0)
                    || year % 400 == 0)
                mPicker2.setAdapter(new NumericWheelAdapter(1, 29, "%02d"));
            else
                mPicker2.setAdapter(new NumericWheelAdapter(1, 28, "%02d"));
        }
    }

    /**
     * @param sTART_YEAR the sTART_YEAR to set
     */
    public void setSTART_YEAR(int sTART_YEAR) {
        START_YEAR = sTART_YEAR;
    }

    /**
     * @param eND_YEAR the eND_YEAR to set
     */
    public void setEND_YEAR(int eND_YEAR) {
        END_YEAR = eND_YEAR;
    }

}
