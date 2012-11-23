
package android.skymobi.messenger.ui;

import android.content.Context;
import android.os.Bundle;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.widget.PopupMenu;
import android.skymobi.messenger.widget.PopupMenu.MenuLocation;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

/**
 * @ClassName: TopActivity
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-7-31 上午10:09:53
 */
public abstract class TopActivity extends BaseActivity {

    private static final String TAG = TopActivity.class.getSimpleName();

    protected Button mBtnLeftI;// 居左按钮I
    protected Button mBtnLeftII;// 居左按钮II
    protected ImageButton mImageBtnLeftI;// 居左按钮I
    protected ImageButton mImageBtnLeftII;// 居左按钮II
    protected Button mBtnRightI;// 居右按钮I
    protected Button mBtnRightII;// 居右按钮II
    protected ImageButton mImageBtnRightI;// 居右按钮I
    protected ImageButton mImageBtnRightII;// 居右按钮II
    protected Button mBtnMiddle;// 中间按钮II
    protected ImageButton mImageBtnMiddle;// 中间按钮I

    protected View mRelativeLayoutLeftI; // 居左边布局I 文字和图片
    protected RelativeLayout mRelativeLayoutTitle;// 主副标题布局

    protected TextView mTitle;// 标题

    protected LinearLayout mBtnLayoutLeftI;
    protected LinearLayout mBtnLayoutLeftII;
    protected LinearLayout mBtnLayoutRightI;
    protected LinearLayout mBtnLayoutRightII;
    protected LinearLayout mBtnLayoutMiddle;

    protected OnClickListener mFinishActivity;

    public static final int TOPBAR_BUTTON_LEFTI = 0x01;// 居左按钮I
    public static final int TOPBAR_BUTTON_LEFTII = 0x02;// 居左按钮II
    public static final int TOPBAR_IMAGE_BUTTON_LEFTI = 0x04;// 居左按钮I(ImageButton)
    public static final int TOPBAR_IMAGE_BUTTON_LEFTII = 0x08;// 居左按钮I(ImageButton)

    public static final int TOPBAR_BUTTON_RIGHTI = 0x10;// 居右按钮I
    public static final int TOPBAR_BUTTON_RIGHTII = 0x20;// 居右按钮II
    public static final int TOPBAR_IMAGE_BUTTON_RIGHTI = 0x40;// 居右按钮I(ImageButton)
    public static final int TOPBAR_IMAGE_BUTTON_RIGHTII = 0x80;// 居右按钮II(ImageButton)

    public static final int TOPBAR_BUTTON_MIDDLE = 0x100;// 中间按钮
    public static final int TOPBAR_IMAGE_BUTTON_MIDDLE = 0x200;// 中间按钮(ImageButton)

    public static final int TOPBAR_TITEL = 0x400;// 标题

    public static final int TOPBAR_RELATIVELAYOUT_LEFTI = 0x500; // 居左边布局I 文字和图片

    abstract public void initTopBar();

    private PopupMenu popupMenu;

    // 最多缓存两个
    private final HashMap<String, PopupMenu> popupMenuMap = new HashMap<String, PopupMenu>(2);

    /**
     * |*TOPBAR_BUTTON_RETURN*|*TOPBAR_TITEL*|*TOPBAR_BUTTON_RIGHTII*|*
     * TOPBAR_BUTTON_RIGHTI*|
     * 
     * @param views
     */
    /*
     * public void initTopBarView(int views) { if ((views &
     * TOPBAR_IMAGE_BUTTON_LEFTI) != 0) { mBtnLayoutLeftI = (LinearLayout)
     * findViewById(R.id.topbar_btnLayout_leftI);
     * mBtnLayoutLeftI.setVisibility(View.VISIBLE); mImageBtnLeftI =
     * (ImageButton) mBtnLayoutLeftI.findViewById(R.id.topbar_imageBtn_leftI);
     * mImageBtnLeftI.setVisibility(View.VISIBLE); } if ((views & TOPBAR_TITEL)
     * != 0) { mTitle = (TextView) findViewById(R.id.topbar_title);
     * mTitle.setText(R.string.register_login1);
     * mTitle.setVisibility(View.VISIBLE); } if ((views & TOPBAR_BUTTON_RIGHTI)
     * != 0 || (views & TOPBAR_IMAGE_BUTTON_RIGHTI) != 0) { mBtnLayoutRightI =
     * (LinearLayout) findViewById(R.id.topbar_btnLayout_rightI);
     * mBtnLayoutRightI.setVisibility(View.VISIBLE); if ((views &
     * TOPBAR_BUTTON_RIGHTI) != 0) { mBtnRightI = (Button)
     * mBtnLayoutRightI.findViewById(R.id.topbar_btn_rightI);
     * mBtnRightI.setVisibility(View.VISIBLE); } if ((views &
     * TOPBAR_IMAGE_BUTTON_RIGHTI) != 0) { mImageBtnRightI = (ImageButton)
     * mBtnLayoutRightI .findViewById(R.id.topbar_imageButton_rightI);
     * mImageBtnRightI.setVisibility(View.VISIBLE); } } if ((views &
     * TOPBAR_BUTTON_RIGHTII) != 0 || (views & TOPBAR_IMAGE_BUTTON_RIGHTII) !=
     * 0) { mBtnLayoutRightII = (LinearLayout)
     * findViewById(R.id.topbar_btnLayout_rightII);
     * mBtnLayoutRightII.setVisibility(View.VISIBLE); if ((views &
     * TOPBAR_BUTTON_RIGHTII) != 0) { mBtnRightII = (Button)
     * mBtnLayoutRightII.findViewById(R.id.topbar_btn_rightII);
     * mBtnRightII.setVisibility(View.VISIBLE); } if ((views &
     * TOPBAR_IMAGE_BUTTON_RIGHTII) != 0) { mImageBtnRightII = (ImageButton)
     * mBtnLayoutRightII .findViewById(R.id.topbar_imageButton_rightII);
     * mImageBtnRightII.setVisibility(View.VISIBLE); } } initTopBarResorce(); }
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mFinishActivity = new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackButtonPressed();
            }
        };
    }

    /**
     * 当topbar上返回按钮按下时被回调，可供子类修改其实现方法
     */
    protected void onBackButtonPressed() {
        finish();
    }

    protected TopActivity setTopBarTitle(int resId) {
        mTitle = (TextView) findViewById(R.id.topbar_title);
        mTitle.setText(resId);
        mTitle.setVisibility(View.VISIBLE);
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.topbar_title_layout);
        mRelativeLayoutTitle.setVisibility(View.GONE);
        return this;
    }

    protected TopActivity setTopBarTitle(String t) {
        mTitle = (TextView) findViewById(R.id.topbar_title);
        mTitle.setText(t);
        mTitle.setVisibility(View.VISIBLE);
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.topbar_title_layout);
        mRelativeLayoutTitle.setVisibility(View.GONE);
        return this;
    }

    protected TopActivity setTopBarDoubleTitle(int mainResId, int secResId) {
        TextView mainTitle = (TextView) findViewById(R.id.topbar_maintitle);
        TextView subTitle = (TextView) findViewById(R.id.topbar_subtitle);
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.topbar_title_layout);
        mRelativeLayoutTitle.setVisibility(View.VISIBLE);
        mTitle = (TextView) findViewById(R.id.topbar_title);
        mTitle.setVisibility(View.GONE);
        if (mainResId > 0) {
            mainTitle.setText(mainResId);
            mainTitle.setVisibility(View.VISIBLE);
        } else {
            mainTitle.setVisibility(View.GONE);
        }

        if (secResId > 0) {
            subTitle.setText(secResId);
            subTitle.setVisibility(View.VISIBLE);
        } else {
            subTitle.setVisibility(View.GONE);
        }
        return this;
    }

    protected TopActivity setTopBarTitle(String t1, String t2) {
        setTopBarTitle(t1, t2, 0xFFFFFF);
        return this;
    }

    protected TopActivity setTopBarTitle(String t1, String t2, int color) {
        if (TextUtils.isEmpty(t2)) {
            setTopBarTitle(t1);
            return this;
        }
        TextView mainTitle = (TextView) findViewById(R.id.topbar_maintitle);
        TextView subTitle = (TextView) findViewById(R.id.topbar_subtitle);
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.topbar_title_layout);
        mRelativeLayoutTitle.setVisibility(View.VISIBLE);
        mTitle = (TextView) findViewById(R.id.topbar_title);
        mTitle.setVisibility(View.GONE);
        if (null != t1) {
            mainTitle.setText(t1);
            mainTitle.setVisibility(View.VISIBLE);
        } else {
            mainTitle.setVisibility(View.GONE);
        }

        if (null != t2) {
            subTitle.setText(t2);
            subTitle.setTextColor(color);
            subTitle.setVisibility(View.VISIBLE);
        } else {
            subTitle.setVisibility(View.GONE);
        }
        return this;
    }

    protected View setTopBarMainTitle(String title) {
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.topbar_title_layout);
        mRelativeLayoutTitle.setVisibility(View.VISIBLE);
        mTitle = (TextView) findViewById(R.id.topbar_title);
        mTitle.setVisibility(View.GONE);
        TextView mainTitle = (TextView) findViewById(R.id.topbar_maintitle);
        if (null != title) {
            mainTitle.setText(title);
            mainTitle.setVisibility(View.VISIBLE);
        } else {
            mainTitle.setVisibility(View.GONE);
        }
        return mainTitle;
    }

    protected View setTopBarSubTitle(String title) {
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.topbar_title_layout);
        mRelativeLayoutTitle.setVisibility(View.VISIBLE);
        mTitle = (TextView) findViewById(R.id.topbar_title);
        mTitle.setVisibility(View.GONE);
        TextView subTitle = (TextView) findViewById(R.id.topbar_subtitle);
        if (null != title) {
            subTitle.setText(title);
            subTitle.setVisibility(View.VISIBLE);
        } else {
            subTitle.setVisibility(View.GONE);
        }
        return subTitle;
    }

    protected View setTopBarButton(int button, int resId, OnClickListener l) {
        View view = null;
        switch (button) {
            case TOPBAR_BUTTON_LEFTI:
            case TOPBAR_IMAGE_BUTTON_LEFTI:
            case TOPBAR_RELATIVELAYOUT_LEFTI: {
                mBtnLayoutLeftI = (LinearLayout) findViewById(R.id.topbar_btnLayout_leftI);
                mBtnLayoutLeftI.setVisibility(View.VISIBLE);
                if (button == TOPBAR_BUTTON_LEFTI) {
                    mBtnLeftI = (Button) mBtnLayoutLeftI.findViewById(R.id.topbar_btn_leftI);
                    mBtnLeftI.setVisibility(View.VISIBLE);
                    mBtnLeftI.setText(resId);
                    mBtnLeftI.setOnClickListener(l);
                    view = mBtnLeftI;
                } else if (button == TOPBAR_IMAGE_BUTTON_LEFTI) {
                    mImageBtnLeftI = (ImageButton) mBtnLayoutLeftI
                            .findViewById(R.id.topbar_imageButton_leftI);
                    mImageBtnLeftI.setVisibility(View.VISIBLE);
                    mImageBtnLeftI.setImageResource(resId);
                    mImageBtnLeftI.setOnClickListener(l);
                    view = mImageBtnLeftI;
                } else if (button == TOPBAR_RELATIVELAYOUT_LEFTI) {
                    mRelativeLayoutLeftI = mBtnLayoutLeftI
                            .findViewById(R.id.topbar_linearLayout_leftI);
                    mRelativeLayoutLeftI.setVisibility(View.VISIBLE);
                    mRelativeLayoutLeftI.setOnClickListener(l);
                    view = mRelativeLayoutLeftI;
                }
            }
                break;
            case TOPBAR_BUTTON_LEFTII:
            case TOPBAR_IMAGE_BUTTON_LEFTII: {
                mBtnLayoutLeftII = (LinearLayout) findViewById(R.id.topbar_btnLayout_leftII);
                mBtnLayoutLeftII.setVisibility(View.VISIBLE);
                if (button == TOPBAR_BUTTON_LEFTII) {
                    mBtnLeftII = (Button) mBtnLayoutLeftII.findViewById(R.id.topbar_btn_leftII);
                    mBtnLeftII.setVisibility(View.VISIBLE);
                    mBtnLeftII.setText(resId);
                    mBtnLeftII.setOnClickListener(l);
                    view = mBtnLeftII;
                } else if (button == TOPBAR_IMAGE_BUTTON_LEFTII) {
                    mImageBtnLeftII = (ImageButton) mBtnLayoutLeftII
                            .findViewById(R.id.topbar_imageButton_leftII);
                    mImageBtnLeftII.setVisibility(View.VISIBLE);
                    mImageBtnLeftII.setImageResource(resId);
                    mImageBtnLeftII.setOnClickListener(l);
                    view = mImageBtnLeftII;
                }
            }
                break;
            case TOPBAR_BUTTON_RIGHTI:
            case TOPBAR_IMAGE_BUTTON_RIGHTI: {
                mBtnLayoutRightI = (LinearLayout) findViewById(R.id.topbar_btnLayout_rightI);
                mBtnLayoutRightI.setVisibility(View.VISIBLE);
                if (button == TOPBAR_BUTTON_RIGHTI) {
                    mBtnRightI = (Button) mBtnLayoutRightI.findViewById(R.id.topbar_btn_rightI);
                    mBtnRightI.setVisibility(View.VISIBLE);
                    mBtnRightI.setText(resId);
                    mBtnRightI.setOnClickListener(l);
                    view = mBtnRightI;
                } else if (button == TOPBAR_IMAGE_BUTTON_RIGHTI) {
                    mImageBtnRightI = (ImageButton) mBtnLayoutRightI
                            .findViewById(R.id.topbar_imageButton_rightI);
                    mImageBtnRightI.setVisibility(View.VISIBLE);
                    mImageBtnRightI.setImageResource(resId);
                    mImageBtnRightI.setOnClickListener(l);
                    view = mImageBtnRightI;
                }
            }
                break;
            case TOPBAR_BUTTON_RIGHTII:
            case TOPBAR_IMAGE_BUTTON_RIGHTII: {
                mBtnLayoutRightII = (LinearLayout) findViewById(R.id.topbar_btnLayout_rightII);
                mBtnLayoutRightII.setVisibility(View.VISIBLE);
                if (button == TOPBAR_BUTTON_RIGHTII) {
                    mBtnRightII = (Button) mBtnLayoutRightII.findViewById(R.id.topbar_btn_rightII);
                    mBtnRightII.setVisibility(View.VISIBLE);
                    mBtnRightII.setText(resId);
                    mBtnRightII.setTag(resId);
                    mBtnRightII.setOnClickListener(l);
                    view = mBtnRightII;
                } else if (button == TOPBAR_IMAGE_BUTTON_RIGHTII) {
                    mImageBtnRightII = (ImageButton) mBtnLayoutRightII
                            .findViewById(R.id.topbar_imageButton_rightII);
                    mImageBtnRightII.setVisibility(View.VISIBLE);
                    mImageBtnRightII.setImageResource(resId);
                    mImageBtnRightII.setTag(resId);
                    mImageBtnRightII.setOnClickListener(l);
                    view = mImageBtnRightII;
                }
            }
                break;
            case TOPBAR_BUTTON_MIDDLE:
            case TOPBAR_IMAGE_BUTTON_MIDDLE: {
                mBtnLayoutMiddle = (LinearLayout) findViewById(R.id.topbar_btnLayout_middle);
                mBtnLayoutMiddle.setVisibility(View.VISIBLE);
                if (button == TOPBAR_BUTTON_MIDDLE) {
                    mBtnMiddle = (Button) mBtnLayoutMiddle.findViewById(R.id.topbar_btn_middle);
                    mBtnMiddle.setVisibility(View.VISIBLE);
                    mBtnMiddle.setText(resId);
                    mBtnMiddle.setOnClickListener(l);
                    view = mBtnMiddle;
                } else if (button == TOPBAR_IMAGE_BUTTON_MIDDLE) {
                    mImageBtnMiddle = (ImageButton) mBtnLayoutMiddle
                            .findViewById(R.id.topbar_imageButton_middle);
                    mImageBtnMiddle.setVisibility(View.VISIBLE);
                    mImageBtnMiddle.setImageResource(resId);
                    mImageBtnMiddle.setOnClickListener(l);
                    view = mImageBtnMiddle;
                }
            }
                break;
        }
        return view;
    }

    protected View setTopBarButton(int button, int resId, int id, OnClickListener l) {
        View view = setTopBarButton(button, resId, l);
        view.setId(id);
        return view;
    }

    // 设置Topbar中button的显示隐藏属性
    protected void setTopBarBtnVisible(int button, boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        switch (button) {
            case TOPBAR_BUTTON_LEFTI:
            case TOPBAR_IMAGE_BUTTON_LEFTI:
            case TOPBAR_RELATIVELAYOUT_LEFTI: {
                mBtnLayoutLeftI = (LinearLayout) findViewById(R.id.topbar_btnLayout_leftI);
                mBtnLayoutLeftI.setVisibility(View.VISIBLE);
                if (button == TOPBAR_BUTTON_LEFTI) {
                    mBtnLeftI = (Button) mBtnLayoutLeftI.findViewById(R.id.topbar_btn_leftI);
                    mBtnLeftI.setVisibility(visibility);
                } else if (button == TOPBAR_IMAGE_BUTTON_LEFTI) {
                    mImageBtnLeftI = (ImageButton) mBtnLayoutLeftI
                            .findViewById(R.id.topbar_imageButton_leftI);
                    mImageBtnLeftI.setVisibility(visibility);
                } else if (button == TOPBAR_RELATIVELAYOUT_LEFTI) {
                    mRelativeLayoutLeftI = mBtnLayoutLeftI
                            .findViewById(R.id.topbar_linearLayout_leftI);
                    mRelativeLayoutLeftI.setVisibility(visibility);
                }
            }
                break;
            case TOPBAR_BUTTON_LEFTII:
            case TOPBAR_IMAGE_BUTTON_LEFTII: {
                mBtnLayoutLeftII = (LinearLayout) findViewById(R.id.topbar_btnLayout_leftII);
                mBtnLayoutLeftII.setVisibility(visibility);
                if (button == TOPBAR_BUTTON_LEFTII) {
                    mBtnLeftII = (Button) mBtnLayoutLeftII.findViewById(R.id.topbar_btn_leftII);
                    mBtnLeftII.setVisibility(visibility);
                } else if (button == TOPBAR_IMAGE_BUTTON_LEFTII) {
                    mImageBtnLeftII = (ImageButton) mBtnLayoutLeftII
                            .findViewById(R.id.topbar_imageButton_leftII);
                    mImageBtnLeftII.setVisibility(visibility);
                }
            }
                break;
            case TOPBAR_BUTTON_RIGHTI:
            case TOPBAR_IMAGE_BUTTON_RIGHTI: {
                mBtnLayoutRightI = (LinearLayout) findViewById(R.id.topbar_btnLayout_rightI);
                mBtnLayoutRightI.setVisibility(visibility);
                if (button == TOPBAR_BUTTON_RIGHTI) {
                    mBtnRightI = (Button) mBtnLayoutRightI.findViewById(R.id.topbar_btn_rightI);
                    mBtnRightI.setVisibility(visibility);
                } else if (button == TOPBAR_IMAGE_BUTTON_RIGHTI) {
                    mImageBtnRightI = (ImageButton) mBtnLayoutRightI
                            .findViewById(R.id.topbar_imageButton_rightI);
                    mImageBtnRightI.setVisibility(visibility);
                }
            }
                break;
            case TOPBAR_BUTTON_RIGHTII:
            case TOPBAR_IMAGE_BUTTON_RIGHTII: {
                mBtnLayoutRightII = (LinearLayout) findViewById(R.id.topbar_btnLayout_rightII);
                mBtnLayoutRightII.setVisibility(visibility);
                if (button == TOPBAR_BUTTON_RIGHTII) {
                    mBtnRightII = (Button) mBtnLayoutRightII.findViewById(R.id.topbar_btn_rightII);
                    mBtnRightII.setVisibility(visibility);
                } else if (button == TOPBAR_IMAGE_BUTTON_RIGHTII) {
                    mImageBtnRightII = (ImageButton) mBtnLayoutRightII
                            .findViewById(R.id.topbar_imageButton_rightII);
                    mImageBtnRightII.setVisibility(visibility);
                }
            }
                break;
            case TOPBAR_BUTTON_MIDDLE:
            case TOPBAR_IMAGE_BUTTON_MIDDLE: {
                mBtnLayoutMiddle = (LinearLayout) findViewById(R.id.topbar_btnLayout_middle);
                mBtnLayoutMiddle.setVisibility(visibility);
                if (button == TOPBAR_BUTTON_MIDDLE) {
                    mBtnMiddle = (Button) mBtnLayoutMiddle.findViewById(R.id.topbar_btn_middle);
                    mBtnMiddle.setVisibility(visibility);
                } else if (button == TOPBAR_IMAGE_BUTTON_MIDDLE) {
                    mImageBtnMiddle = (ImageButton) mBtnLayoutMiddle
                            .findViewById(R.id.topbar_imageButton_middle);
                    mImageBtnMiddle.setVisibility(visibility);
                }
            }
                break;
        }
    }

    /**
     * 返回 viewId对应的view
     * 
     * @param button
     * @param resId
     * @param l
     * @param viewId
     * @return
     */
    protected View getTopBarViewNoDivider(int button, int resId, OnClickListener l, int viewId) {
        View view = setTopBarButton(button, resId, l);
        findViewById(R.id.topbar_divider_leftI).setVisibility(View.GONE);
        findViewById(R.id.topbar_divider_leftII).setVisibility(View.GONE);
        return view.findViewById(viewId);
    }

    protected View getTopBarView(int button, int resId, OnClickListener l, int viewId) {
        View view = setTopBarButton(button, resId, l);
        return view.findViewById(viewId);
    }

    /**
     * @param adapter 显示的列表数据
     * @param menuLocation 菜单的位置,Left or Right
     * @param parent 在哪个View下弹出
     * @param xoff
     * @param yoff
     * @param onItemClickListener
     */
    public void showPopupMenu(Context ctx, ListAdapter adapter, MenuLocation menuLocation,
            View parent,
            int xoff, int yoff, OnItemClickListener onItemClickListener) {
        AndroidSysUtils.hideSystemSoftKeyboard(ctx, parent);
        String menuKey = ctx.getClass().getSimpleName() + menuLocation.toString();
        SLog.d(TAG, "menuKey :" + menuKey);
        if (popupMenuMap.containsKey(menuKey)) {
            SLog.d(TAG, "get from popupMenuMap by" + menuKey);
            popupMenu = popupMenuMap.get(menuKey);

        } else {
            SLog.d(TAG, "create new popupMenu by:" + menuKey);
            popupMenu = new PopupMenu(ctx, adapter, menuLocation);
            popupMenu.setOnItemClickListener(onItemClickListener);
            popupMenuMap.put(menuKey, popupMenu);
        }

        popupMenu.showPopupMenu(parent, xoff, yoff);
    }

    public void showRightPopupMenu(Context ctx, ListAdapter adapter, View parent,
            OnItemClickListener onItemClickListener) {
        int leftXoff = getResources().getDimensionPixelSize(R.dimen.popup_menu_width);
        int rightYoff = getResources().getDimensionPixelSize(R.dimen.popup_menu_right_yoff);
        int rightOffset = getResources().getDimensionPixelSize(R.dimen.popup_menu_right_offest);

        showPopupMenu(ctx, adapter, MenuLocation.RIGHT, parent, -(leftXoff / 2 + rightOffset),
                rightYoff,
                onItemClickListener);
    }

    public void showLeftPopupMenu(Context ctx, ListAdapter adapter, View parent,
            OnItemClickListener onItemClickListener) {
        int leftXoff = getResources().getDimensionPixelSize(R.dimen.popup_menu_width);
        int leftYoff = getResources().getDimensionPixelSize(R.dimen.popup_menu_left_yoff);
        int leftOffset = getResources().getDimensionPixelSize(R.dimen.popup_menu_left_offest);
        showPopupMenu(ctx, adapter, MenuLocation.LEFT, parent, -((leftXoff / 2) - leftOffset),
                leftYoff,
                onItemClickListener);
    }

    public void dismissPopupMenu() {
        if (null != popupMenu && null != popupMenu.getPopupWindow()
                && popupMenu.getPopupWindow().isShowing()) {
            popupMenu.getPopupWindow().dismiss();
        }
    }

    /**
     * @return the popupMenu
     */
    public PopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * @param popupMenu the popupMenu to set
     */
    public void setPopupMenu(PopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }
}
