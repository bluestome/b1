
package android.skymobi.messenger.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.skymobi.messenger.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

/**
 * @ClassName: PopupMenu
 * @Description: 弹出菜单
 * @author Anson.Yang
 * @date 2012-8-3 下午4:31:06
 */
public class PopupMenu {
    private final Context context;
    private ListView listView;
    private PopupWindow popupWindow;
    private OnItemClickListener onItemClickListener;
    private View view;

    /**
     * 默认 的popMenu
     * 
     * @param ctx
     * @param adapter
     */
    public PopupMenu(Context ctx, ListAdapter adapter, MenuLocation menuLocation) {
        this.context = ctx;
        initMenuView(adapter, menuLocation);
        createPopupWindow();
    }

    public PopupMenu(Context ctx, ListAdapter adapter, MenuLocation menuLocation, int width) {
        this.context = ctx;
        initMenuView(adapter, menuLocation);
        createPopupWindow(width);
    }

    /**
     * @param ctx
     * @param adapter 列表数据
     * @param menuLocation menu的位置(左,右)
     * @param width menu的width
     * @param height menu的height
     */
    public PopupMenu(Context ctx, ListAdapter adapter, MenuLocation menuLocation, int width,
            int height) {
        this.context = ctx;
        initMenuView(adapter, menuLocation);
        createPopupWindow(width, height);

    }

    private void initMenuView(ListAdapter adapter, MenuLocation menuLocation) {
        switch (menuLocation) {
            case LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.popup_menu_left, null);
                break;
            case RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.popup_menu_right, null);
                break;
            default:
                throw new AssertionError();
        }
        listView = (ListView) view.findViewById(R.id.popup_menu);
        listView.setAdapter(adapter);
    }

    private void createPopupWindow() {
        if (null == popupWindow) {
            popupWindow = new PopupWindow(view, context.getResources().getDimensionPixelSize(
                    R.dimen.popup_menu_width), LayoutParams.WRAP_CONTENT);
        }
    }

    private void createPopupWindow(int width) {
        if (null == popupWindow) {
            popupWindow = new PopupWindow(view, width, LayoutParams.WRAP_CONTENT);
        }
    }

    private void createPopupWindow(int width, int height) {
        if (null == popupWindow) {
            popupWindow = new PopupWindow(view, width, height);
        }
    }

    /**
     * 显示popMenu,之前需要设置itemListener,否则点击列表没有效果
     * 
     * @param parent
     * @param xoff
     * @param yoff
     */
    public void showPopupMenu(View parent, int xoff, int yoff) {
        showAsDropDown(parent, xoff, yoff);
        if (null != onItemClickListener) {
            listView.setOnItemClickListener(onItemClickListener);
        }
    }

    private void showAsDropDown(View parent, int xoff, int yoff) {
        // 聚焦
        popupWindow.setFocusable(true);
        // 允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击"返回Back"也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAsDropDown(parent, xoff, yoff);
        // popupWindow.showAtLocation(parent, Gravity.RIGHT | Gravity.TOP, xoff,
        // yoff);
        popupWindow.update();
    }

    /**
     * @return the popupWindow
     */
    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    /**
     * @param onItemClickListener the onItemClickListener to set
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public enum MenuLocation {
        LEFT, RIGHT
    }
}
