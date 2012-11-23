package android.skymobi.messenger.dataaccess.auth;

import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.dataaccess.IDA;
import android.skymobi.messenger.dataaccess.bean.Channel;
import android.skymobi.messenger.dataaccess.bean.SettingUser;

public interface IAuthDA extends IDA{
	
	/**
	 * 从comm配置文件中获取当前登录用户的基本信息
	 * */
	public android.skymobi.messenger.bean.UserInfo getCurrentUserInfo();
	
	/**
	 * 将用户信息保存至comm配置文件
	 * 
	 * @param userInfo 用户信息
	 * */
	public void saveUserInfoToPreferences(UserInfo userInfo);
	
	/**
	 * 保存至setting配置文件
	 * 
	 * */
	public void saveSettingUserInfoToPreferences(SettingUser setting);
	
	/**
	 * 获取setting配置
	 * 
	 * */
	public SettingUser getSettingUserInfo();
	
	
	/**
	 * 保存激活后绑定手机的通道号
	 * 
	 * @param to 多个目标短信通道号，最终保存时用","号分隔
	 * @param content  短信内容
	 * */
	public void saveBindChannel(Channel channel);
	
	/**
	 * 获取绑定的短信号码，该短信号在注册激活后将下发
	 * */
	public Channel getBindChannel();
	
	/**
	 * 
	 * 判断是否可以再次绑定
	 * 
	 * */
	public boolean isCanSendBind();
	
	/**
	 * 
	 * 判断绑定状态
	 * 
	 * */
	public boolean isBindLocal();
	
	/**
	 * 累加用户登录次数
	 * */
	public int addLoginTimes();
	
	
}
