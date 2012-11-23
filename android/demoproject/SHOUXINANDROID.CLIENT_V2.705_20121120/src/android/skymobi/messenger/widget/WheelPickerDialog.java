
package android.skymobi.messenger.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.skymobi.messenger.R;
import android.skymobi.messenger.widget.wheelview.WheelView;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @ClassName: WheelCityPickerDialog
 * @Description: 滚轮样式的 Picker Dialog
 * @author Lv.Lv
 * @date 2012-3-7 上午10:35:45
 */
public abstract class WheelPickerDialog extends AlertDialog implements OnClickListener {

    private static final String DATA = "WheelPickerDialog_data";
    private static final String DATA2 = "WheelPickerDialog_data2";
    private static final String DATA3 = "WheelPickerDialog_data3";

    protected final OnDateSetListener mCallBack;
    protected final Context mContext;

    protected final View mView;
    protected final WheelView mPicker;
    protected final WheelView mPicker2;
    protected final WheelView mPicker3;

    protected int mInitialData;
    protected int mInitialData2;
    protected int mInitialData3;

    /***
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /***
         * @param view The view associated with this listener.
         * @param data The first picker that was set.
         * @param data2 The second picker was set.
         * @param data3 reserved, not used.
         */
        void onDateSet(View view, int data, int data2, int data3);
    }

    /***
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param data The initial data of the first picker.
     * @param data2 The initial data of the second picker.
     * @param data3 reserved, not used.
     */
    public WheelPickerDialog(Context context,
            OnDateSetListener callBack,
            int data,
            int data2,
            int data3) {
        super(context);

        mContext = context;
        mCallBack = callBack;
        mInitialData = data;
        mInitialData2 = data2;
        mInitialData3 = data3;

        setButton(BUTTON_POSITIVE, mContext.getText(R.string.save), this);
        setButton(BUTTON_NEGATIVE, mContext.getText(R.string.cancel), (OnClickListener) null);

        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.pick_data, null);
        mPicker = (WheelView) mView.findViewById(R.id.wheelview_picker);
        mPicker2 = (WheelView) mView.findViewById(R.id.wheelview_picker2);
        mPicker3 = (WheelView) mView.findViewById(R.id.wheelview_picker3);

        setView(mView);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mCallBack != null) {
            mCallBack.onDateSet(mView,
                    mPicker.getCurrentItem(),
                    mPicker2.getCurrentItem(),
                    mPicker3.getCurrentItem());
        }
    }

    public void updateDate(int data, int data2, int data3) {
        mInitialData = data;
        mInitialData2 = data2;
        mInitialData3 = data3;
        initPicker();
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(DATA, mPicker.getCurrentItem());
        state.putInt(DATA2, mPicker2.getCurrentItem());
        state.putInt(DATA3, mPicker3.getCurrentItem());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int data = savedInstanceState.getInt(DATA);
        int data2 = savedInstanceState.getInt(DATA2);
        updateDate(data, data2, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPicker();
    }

    protected abstract void initPicker();
}
