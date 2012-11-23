package android.skymobi.messenger.ui.handler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.auth.RegisterBU;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.BaseActivity;

public class CheckBindHandler extends Handler {
    private static final String TAG = "CheckBindHandler";
    private final BaseActivity activity;


    public CheckBindHandler(BaseActivity activity) {
        this.activity = activity;
    }
    

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CoreServiceMSG.MSG_CHECK_BIND:
            	//SLog.d(TAG, "上层收到判断绑定的通知..."+activity.isFinishing());
            	if(!activity.isFinishing()){
            		//SLog.d(TAG, "上层-----------------------------");
            		showDialog(CoreServiceMSG.MSG_CHECK_BIND);
            	}
            	break;
        }
    }
    
    private void showDialog(int code) {
        Dialog dialog = createDialog(code);
        dialog.show();
       
    }
    

    private Dialog createDialog(int code) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        switch (code) {
            case CoreServiceMSG.MSG_CHECK_BIND:
                dialogBuilder.setMessage(R.string.un_bound_tip);
                /*按钮调换，将“绑定按钮”放在右边**/
                dialogBuilder.setNegativeButton(R.string.bound,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                RegisterBU registerAO = new RegisterBU(null);
                                registerAO.sendBindSMS();
                            }
                        });
                dialogBuilder.setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                              
                            }
                        });
                break;
           
        }
        return dialogBuilder.create();
    }

   
}