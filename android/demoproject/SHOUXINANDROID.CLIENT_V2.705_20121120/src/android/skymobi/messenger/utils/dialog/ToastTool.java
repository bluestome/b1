package android.skymobi.messenger.utils.dialog;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * 
 * toast封装
 * 
 * */
public class ToastTool {
	
	private ToastTool(){}
	
	public static void show(Context context, int message,
			boolean _short) {
		if (_short) {
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	public static void showShort(Context context, int message) {
		show(context, message, true);
	}

	public static void showLong(Context context, int message) {
		show(context, message, false);
	}

	public static void showAtCenter(Context context, int message,
			boolean _short) {
		Toast toast = null;
		if (_short) {
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		} else {
			toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		}
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void showAtCenterShort(Context context, int message) {
		showAtCenter(context, message, true);
	}

	public static void showAtCenterLong(Context context, int message) {
		showAtCenter(context, message, false);
	}

	public static void showWithPic(Context context, int drawable,
			int message, boolean _short) {
		Toast toast = null;
		if (_short) {
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		} else {
			toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		}
		ImageView image = new ImageView(context);
		image.setImageResource(drawable);
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setHorizontalGravity(LinearLayout.HORIZONTAL);
		linearLayout.addView(image);
		linearLayout.addView(toast.getView());
		toast.setView(linearLayout);
		toast.show();
	}

	public static void showWithPicShort(Context context, int drawable,
			int message) {
		showWithPic(context, drawable, message, true);
	}

	public static void showWithPicLong(Context context, int drawable,
			int message) {
		showWithPic(context, drawable, message, false);
	}

	public static void showWithPicAtCenter(Context context, int drawable,
            int message, boolean _short, boolean _vertical) {
		Toast toast = null;
		if (_short) {
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		} else {
			toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		}
		toast.setGravity(Gravity.CENTER, 0, 0);
		ImageView image = new ImageView(context);
		image.setImageResource(drawable);
        if (_vertical) {
            LinearLayout linearLayout = (LinearLayout) toast.getView();
            linearLayout.addView(image, 0);
        } else {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.addView(image);
            linearLayout.addView(toast.getView());
            toast.setView(linearLayout);
        }
		toast.show();
	}

	public static void showWithPicAtCenterShort(Context context, int drawable,
			int message) {
        showWithPicAtCenter(context, drawable, message, true, false);
	}

	public static void showWithPicAtCenterLong(Context context, int drawable,
			int message) {
        showWithPicAtCenter(context, drawable, message, false, false);
	}
}
