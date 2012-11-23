
package android.skymobi.messenger.ui.action;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.contact.InformBU;
import android.skymobi.messenger.ui.BaseActivity;
import android.skymobi.messenger.utils.dialog.DialogTool;

/**
 * @ClassName: InformAction
 * @Description: 举报动作类
 * @author dylan.zhao
 * @date 2012-9-17 上午11:19:26
 */

public class InformAction extends BaseAction implements DialogInterface.OnClickListener {

    private final static String TAG = InformAction.class.getSimpleName();

    /** 被举报者的skyId */
    private int skyId;

    /** 举报的类型 */
    private int typeId;

    private InformBU informAO = null;

    public InformAction(BaseActivity activity) {
        super(activity);
    }

    public void setInformAO(InformBU informaAO) {
        this.informAO = informaAO;
    }

    /**
     * 显示举报对话框。 动作监听器为informAction。默认的按钮“确定”在左，“取消”在右，这里进行了调换
     */
    public void showInformDialog() {

        SLog.d(TAG, "显示举报对话框");

        Dialog dialog = DialogTool.createRadioDialog(activity, 0,
                        activity.getString(R.string.inform_dialog_title),
                        R.array.inform_list_type, this,
                        activity.getString(R.string.cancel), this,
                        activity.getString(R.string.ok), this);
        dialog.setCancelable(true);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            /**
             * 对话框显示的时候，使“确定”按钮不可用
             */
            @Override
            public void onShow(DialogInterface dlg) {
                ((AlertDialog) dlg).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            }
        });
        dialog.show();
    }

    /**
     * 处理对话框点击事件
     * 
     * @param which>=0 点击的是单选按钮
     * @param which==BUTTON_NEGATIVE 是右边按钮，即“确定”，而不是“取消”
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which >= 0) {
            typeId = which;
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            SLog.d(TAG,
                    "举报skyId:" + skyId + "   typeId:" + typeId);

            // 终端数组[typeId] int：0-骚扰信息；1-色情信息； 2-个人资料不当； 3-垃圾广告
            // 服务端举报类型表 byte： 1-骚扰信息；2-色情信息； 3-个人资料不当；4-垃圾广告

            informAO.inform(skyId, (byte) (typeId + 1));
        }
    }

    public void setSkyId(final int id) {
        skyId = id;
    }

}
