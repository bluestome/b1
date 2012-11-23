package android.skymobi.messenger.bizunit;

import android.os.Handler;
import android.os.Message;
import android.skymobi.messenger.MainApp;


public  abstract class BaseBU extends AbsBaseBU {
	
	private Handler handler = null;
	
	protected BaseBU(Handler handler){
		super();
		this.handler = handler;
	}
	

	protected void submit(Runnable runnable) {
		//将任务提交给全局单队列处理器
	    MainApp.getTaskHandler().post(runnable);
	}
	
	protected boolean sendEventMsg(Message msg){
		if(handler==null)return false;
		return handler.sendMessage(msg);
	}
	
	protected boolean sendEventMsg(int what){
		if(handler==null)return false;
		return handler.sendEmptyMessage(what);
	}
	
    protected boolean sendEventMsg(int what, Object obj) {
        if (handler == null)
            return false;
        return handler.sendMessage(Message.obtain(handler, what, obj));
    }

}
