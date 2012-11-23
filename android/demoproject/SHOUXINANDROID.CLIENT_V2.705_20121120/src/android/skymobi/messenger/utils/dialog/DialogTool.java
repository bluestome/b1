
package android.skymobi.messenger.utils.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.skymobi.messenger.R;
import android.view.View;

/**
 * 对话框封装类
 */
public class DialogTool {

    private DialogTool() {
    }

    /**
     * 创建普通的进度框
     * 
     * @param ctx 上下文
     * @param iconId 图标，如：R.drawable.icon
     * @param title 标题
     * @param message 显示内容
     * @return
     */
    public static android.app.ProgressDialog createNormalProgressDialog(Context ctx, int iconId,
            int title, String message) {
        return createNormalProgressDialog(ctx, iconId, title, message, false);
    }

    public static android.app.ProgressDialog createNormalProgressDialog(Context ctx, int iconId,
            int title, String message, boolean cancelable) {
        android.app.ProgressDialog dialog = new android.app.ProgressDialog(ctx);
        dialog.setMessage(message);
        dialog.setIcon(iconId);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        return dialog;
    }

    /**
     * 创建普通对话框
     * 
     * @param ctx 上下文 必填
     * @param iconId 图标，如：R.drawable.icon 必填
     * @param title 标题 必填
     * @param message 显示内容 必填
     * @param btnName 按钮名称 必填
     * @param listener 监听器，需实现android.content.DialogInterface.OnClickListener接口
     *            必填
     * @return
     */
    public static Dialog createNormalDialog(Context ctx, int iconId,
            int title, CharSequence message, int btnName,
            OnClickListener listener) {
        return createNormalDialog(ctx, iconId, title, message, btnName, listener, false, null);
    }

    public static Dialog createNormalDialog(Context ctx, int iconId,
            int title, CharSequence message, int btnName,
            OnClickListener listener, boolean isNegativeButton, OnClickListener cancelListener) {
        return createNormalDialog(ctx, iconId,
                 title, message, btnName,
                 listener, isNegativeButton, cancelListener, false);
    }

    public static Dialog createNormalDialog(Context ctx, int iconId,
            int title, CharSequence message, int btnName,
            OnClickListener listener, boolean isNegativeButton, OnClickListener cancelListener,
            boolean cancelable) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ctx);
        // 设置对话框的图标
        builder.setIcon(iconId);
        // 设置对话框的标题
        builder.setTitle(title);
        // 设置对话框的显示内容
        builder.setMessage(message);
        // 添加按钮，android.content.DialogInterface.OnClickListener.OnClickListener
        builder.setPositiveButton(btnName, listener);
        if (isNegativeButton && cancelListener != null) {
            builder.setNeutralButton(R.string.cancel, cancelListener);
        }
        // 创建一个普通对话框
        dialog = builder.create();
        dialog.setCancelable(cancelable);
        return dialog;
    }

    public static Dialog createNormalDialog(Context ctx, int iconId,
            int title, CharSequence message, int btnLeftID,
            OnClickListener listener, int btnRightID, OnClickListener cancelListener,
            boolean cancelable) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ctx);
        // 设置对话框的图标
        builder.setIcon(iconId);
        // 设置对话框的标题
        builder.setTitle(title);
        // 设置对话框的显示内容
        builder.setMessage(message);
        // 添加按钮，android.content.DialogInterface.OnClickListener.OnClickListener
        builder.setPositiveButton(btnLeftID, listener);
        builder.setNeutralButton(btnRightID, cancelListener);
        // 创建一个普通对话框
        dialog = builder.create();
        dialog.setCancelable(cancelable);
        return dialog;
    }

    /**
     * 创建列表对话框
     * 
     * @param ctx 上下文 必填
     * @param iconId 图标，如：R.drawable.icon 必填
     * @param title 标题 必填
     * @param itemsId 字符串数组资源id 必填
     * @param listener 监听器，需实现android.content.DialogInterface.OnClickListener接口
     *            必填
     * @return
     */
    public static Dialog createListDialog(Context ctx, int iconId,
            String title, int itemsId, OnClickListener listener) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                ctx);
        // 设置对话框的图标
        builder.setIcon(iconId);
        // 设置对话框的标题
        builder.setTitle(title);
        // 添加按钮，android.content.DialogInterface.OnClickListener.OnClickListener
        builder.setItems(itemsId, listener);
        // 创建一个列表对话框
        dialog = builder.create();
        return dialog;
    }

    /**
     * 创建单选按钮对话框
     * 
     * @param ctx 上下文 必填
     * @param iconId 图标，如：R.drawable.icon 必填
     * @param title 标题 必填
     * @param itemsId 字符串数组资源id 必填
     * @param listener 
     *            单选按钮项监听器，需实现android.content.DialogInterface.OnClickListener接口
     *            必填
     * @param btnName 按钮名称 必填
     * @param listener2
     *            按钮监听器，需实现android.content.DialogInterface.OnClickListener接口 必填
     * @return
     */
    public static Dialog createRadioDialog(Context ctx, int iconId,
            String title, int itemsId, OnClickListener listener,
            String btnName, OnClickListener listener2) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                ctx);
        // 设置对话框的图标
        builder.setIcon(iconId);
        // 设置对话框的标题
        builder.setTitle(title);
        // 0: 默认第一个单选按钮被选中
        builder.setSingleChoiceItems(itemsId, 0, listener);
        // 添加一个按钮
        builder.setPositiveButton(btnName, listener2);
        // 创建一个单选按钮对话框
        dialog = builder.create();
        return dialog;
    }

    /**
     * 创建单选按钮对话框，带两个button
     * 
     * @param ctx 上下文 必填
     * @param iconId 图标，如：R.drawable.icon 必填
     * @param title 标题 必填
     * @param itemsId 字符串数组资源id 必填
     * @param listener 
     *            单选按钮项监听器，需实现android.content.DialogInterface.OnClickListener接口
     *            必填
     * @param leftBtnName 按钮名称-资源ID 必填
     * @param leftBtnListener按钮监听器
     *            ，需实现android.content.DialogInterface.OnClickListener接口 必填
     * @param rightBtnName 按钮名称-资源ID
     * @param rightBtnListener按钮监听器
     *            ，需实现android.content.DialogInterface.OnClickListener接口 必填 @return
     */
    public static Dialog createRadioDialog(Context ctx, int iconId,
            String title, int itemsId, OnClickListener listener,
            String leftBtnName, OnClickListener leftBtnListener,
            String rightBtnName, OnClickListener rightBtnListener
            ) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                ctx);
        // 设置对话框的图标
        builder.setIcon(iconId);
        // 设置对话框的标题
        builder.setTitle(title);
        // -1: 默认没有单选按钮被选中
        builder.setSingleChoiceItems(itemsId, -1, listener);
        // 添加一个按钮
        builder.setPositiveButton(leftBtnName, leftBtnListener);
        builder.setNegativeButton(rightBtnName, rightBtnListener);
        // 创建一个单选按钮对话框
        dialog = builder.create();
        return dialog;
    }

    /**
     * 创建复选对话框
     * 
     * @param ctx 上下文 必填
     * @param iconId 图标，如：R.drawable.icon 必填
     * @param title 标题 必填
     * @param itemsId 字符串数组资源id 必填
     * @param flags 初始复选情况 必填
     * @param listener 单选按钮项监听器，需实现android.content.DialogInterface.
     *            OnMultiChoiceClickListener接口 必填
     * @param btnName 按钮名称 必填
     * @param listener2
     *            按钮监听器，需实现android.content.DialogInterface.OnClickListener接口 必填
     * @return
     */
    public static Dialog createCheckBoxDialog(
            Context ctx,
            int iconId,
            String title,
            int itemsId,
            boolean[] flags,
            android.content.DialogInterface.OnMultiChoiceClickListener listener,
            String btnName, OnClickListener listener2) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                ctx);
        // 设置对话框的图标
        builder.setIcon(iconId);
        // 设置对话框的标题
        builder.setTitle(title);
        builder.setMultiChoiceItems(itemsId, flags, listener);
        // 添加一个按钮
        builder.setPositiveButton(btnName, listener2);
        // 创建一个复选对话框
        dialog = builder.create();
        return dialog;
    }

    /**
     * 根据 view 创建对话框，带两个 button
     * 
     * @param ctx 上下文 必填
     * @param iconId 图标，如：R.drawable.icon 必填, 没有则填0
     * @param title 标题-资源ID 必填
     * @param view 子视图，必填
     * @param leftBtnName 按钮名称-资源ID 必填
     * @param leftBtnListener按钮监听器
     *            ，需实现android.content.DialogInterface.OnClickListener接口 必填
     * @param rightBtnName 按钮名称-资源ID 必填
     * @param rightBtnListener按钮监听器
     *            ，需实现android.content.DialogInterface.OnClickListener接口 必填
     * @return
     */
    public static Dialog createDialogByView(
            Context ctx,
            int iconId,
            int title,
            View view,
            int leftBtnName,
            OnClickListener leftBtnListener,
            int rightBtnName,
            OnClickListener rightBtnListener) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                ctx);

        // 设置对话框的图标
        if (iconId > 0) {
            builder.setIcon(iconId);
        }
        // 设置对话框的标题
        builder.setTitle(title);

        // 添加子View
        builder.setView(view);

        // 添加按钮
        builder.setPositiveButton(leftBtnName, leftBtnListener);
        builder.setNegativeButton(rightBtnName, rightBtnListener);
        dialog = builder.create();
        return dialog;
    }
}
