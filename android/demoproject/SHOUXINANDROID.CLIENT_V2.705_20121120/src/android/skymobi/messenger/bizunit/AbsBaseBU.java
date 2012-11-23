package android.skymobi.messenger.bizunit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.skymobi.app.SXClient;
import android.skymobi.app.c2v.IWhirligigListener;
import android.skymobi.app.c2v.RegistrationCenter;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.network.NetWorkMgr;
import android.skymobi.messenger.service.CoreService;

public abstract class AbsBaseBU  implements IWhirligigListener{
	
	protected final static int SUCCESS = 200;
	
	protected SXClient client;
	
	public AbsBaseBU(){
		client = NetWorkMgr.getInstance().getClient();
	}
	
	/**
	 * 判断是否是登录状态，部分请求如果不是登录状态，不能发起!!!
	 * @return true 登录状态
	 * 
	 * */
	protected boolean isLoggedIn(){
		return  MainApp.isLoggedIn();
	}

	
	@Override
	public boolean agree(int id) {
		return RegistrationCenter.isFind(this, id);
	}
	
	protected AbsBaseBU getContext(){
		return this;
	}
	
}
