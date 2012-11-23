
package android.skymobi.messenger.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.skymobi.messenger.R;
import android.skymobi.messenger.bizunit.auth.LoginBU;
import android.skymobi.messenger.bizunit.auth.RegisterBU;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.dialog.DialogTool;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @ClassName: SettingsBindActivity
 * @Description: 绑定手机
 * @author Lv.Lv ,hzc
 * @date 2012-3-13 上午10:57:07
 */
public class SettingsBindActivity extends TopActivity {
    // Dialog ID
    public static final int DIALOG_BIND_CHANGE = 0x1000;

    RegisterBU registerBU = null;
    LoginBU loginBU = null;

    private Button mBindBtn = null;
    private TextView textview = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_bind);
        
        registerBU = new RegisterBU(null);
        loginBU = new LoginBU(null);
        initTopBar();
        init();
    }

    private void init() {
        // 绑定号码layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_bind);
        // 已绑定号码
        textview = (TextView) findViewById(R.id.text_bind_number);

        // 绑定button
        mBindBtn = (Button) findViewById(R.id.settings_bind_btn);
        mBindBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //如果已经绑定成功了
                if(loginBU.isBindLocal()){
                    Intent intent = new Intent(SettingsBindActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.TAG_EXTRA, R.id.main_tab_settings);
                    startActivity(intent);
                    finish();
                }else
                    showDialog(DIALOG_BIND_CHANGE);
            }
        });
        boolean isBind = loginBU.isBindLocal();
        // 未绑定或绑定了其他手机imsi
        if ((!isBind)) {
        	// 不可见
            layout.setVisibility(View.GONE); 
            
            //绑定的按钮可见
            mBindBtn.setVisibility(View.VISIBLE);
            mBindBtn.setEnabled(true);
            mBindBtn.setText(R.string.settings_bind_local);
          
        }
        // 已绑定
        else {
        	//可见
        	layout.setVisibility(View.VISIBLE);
            String bindNumber = SettingsPreferences.getMobile();
            textview.setText(bindNumber);
            
            //不可见
            mBindBtn.setVisibility(View.GONE);
            mBindBtn.setEnabled(false);
           
            
           
            
        }
    }
    
    private ProgressDialog bindingDialog =  null;

    @Override
    protected Dialog onCreateDialog(int id) {
        
        
        Dialog dialog = null;
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.tip);
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        switch (id) {
            case DIALOG_BIND_CHANGE: 
            	//规定时间内没有绑定成功，可以再次绑定
            	if(registerBU.isCanSendBind()){
            		dialogBuilder.setMessage(R.string.un_bound_tip);
                	dialogBuilder.setCancelable(false);
                	/*按钮调换，将“绑定按钮”放在右边**/
                	dialogBuilder.setNegativeButton(R.string.bound, new Dialog.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                         	//if(true)return ;
                             //String smsTo = result[0];
                            // String smsContent = result[1];
                            // sendActivateSMSMsg(smsTo, smsContent);
                         	dialog.dismiss();
                         	bindingDialog = DialogTool.createNormalProgressDialog(SettingsBindActivity.this, 
                         			android.R.drawable.ic_dialog_info,
                         			R.string.tip, 
                         			getString(R.string.bounding),true);
                         	bindingDialog.show();
                         	registerBU.sendBindSMS(); 
                         	waitBind();
                             
                         }
                     }).setPositiveButton(R.string.cancel, new Dialog.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     });
            	}else{
            		//正在绑定中
            		dialogBuilder.setMessage(R.string.bounding_detail_tip);
                	dialogBuilder.setCancelable(true);
                	dialogBuilder.setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            	}
            	
        
                break;

                
        }
        dialog = dialogBuilder.create();
        
        return dialog;
    }
    

   
    
    public void waitBind() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                
            	while(true){
            		if(!loginBU.isBindLocal()){
            		    if(bindingDialog==null || (bindingDialog!=null&&!bindingDialog.isShowing())){
                            return ;
                        }
            			SystemClock.sleep(Constants.WAIT_BIND_STATUS_INTERVAL);
            			continue;
            		}else  break;
            	}
            	if(bindingDialog!=null){
                    bindingDialog.dismiss();
                    bindingDialog = null;
                }
            	Intent intent = new Intent(SettingsBindActivity.this, MainActivity.class);
            	intent.putExtra(MainActivity.TAG_EXTRA, R.id.main_tab_settings);
            	startActivity(intent);
            	finish();
            }
        }).start();
    }


    @Override
    public void initTopBar() {
        setTopBarButton(TOPBAR_IMAGE_BUTTON_LEFTI, R.drawable.topbar_btn_back, mFinishActivity);
        setTopBarTitle(R.string.settings_bind);
        
    }
}
