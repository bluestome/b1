package android.skymobi.messenger.dataaccess.auth;

import android.skymobi.common.log.SLog;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.dataaccess.BasicDA;
import android.skymobi.messenger.dataaccess.bean.Channel;
import android.skymobi.messenger.dataaccess.bean.SettingUser;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.SettingsPreferences;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class AuthDA extends BasicDA implements IAuthDA {

	private static final String TAG = AuthDA.class.getSimpleName();
	
	@Override
	public UserInfo getCurrentUserInfo(){
		UserInfo userInfo = CommonPreferences.getUserInfo();
		if(userInfo==null)userInfo = new UserInfo();
		SLog.i(TAG, "从配置文件中获取当前登录用户基本信息:"+userInfo.toString());
		return userInfo;
	}
	
	
	@Override
	public void saveUserInfoToPreferences(UserInfo userInfo) {
		UserInfo temp = getCurrentUserInfo();
		if(StringUtils.isBlank(userInfo.name)){
			userInfo.name = temp.name;
		}
		if(StringUtils.isBlank(userInfo.nickname)){
			userInfo.nickname = temp.nickname;
		}
		if(StringUtils.isBlank(userInfo.token)){
			userInfo.token = temp.token;
		}
		if(StringUtils.isBlank(userInfo.token)){
			userInfo.token = temp.token;
		}
		SLog.i(TAG, "保存登录用户基本信息至配置文件:"+userInfo.toString());
		CommonPreferences.setUserInfo(userInfo);
	}

	@Override
	public void saveSettingUserInfoToPreferences(SettingUser settingUser) {
	    if(settingUser.getSkyid()<=0){
	        throw new RuntimeException("非法的skyid!");
	    }
		SettingsPreferences.saveSKYID(settingUser.getSkyid());
	
		if(StringUtils.isNotBlank(settingUser.getNickName())){
			SettingsPreferences.saveNickname(settingUser.getNickName());
		}
		if(StringUtils.isNotBlank(settingUser.getSex())){
			SettingsPreferences.saveSex(settingUser.getSex());
		}
		if(StringUtils.isNotBlank(settingUser.getReserve0())){
			SettingsPreferences.saveReserve0(settingUser.getReserve0());
		}	
		SLog.i(TAG, "保存登录用户基本信息至setting配置文件:"+settingUser.toString());
	}
	
	public SettingUser getSettingUserInfo(){
		SettingUser user = new SettingUser(SettingsPreferences.getSKYID());
		user.setNickName(SettingsPreferences.getNickname());
		user.setReserve0(SettingsPreferences.getReserve0());
		user.setSex(SettingsPreferences.getSex());
		SLog.i(TAG, "从setting配置文件中获取用户基本信息:"+user.toString());
		return user;
	}

	@Override
	public void saveBindChannel(Channel channel) {
		String toMobile = "";
		for(String to:channel.getTargetNo()){
			if(!StringUtils.isBlank(to)){
				toMobile+=(to+"|");
			}
		}
		if(StringUtils.isNotBlank(toMobile)){
		    SLog.d(TAG, "保存通道号["+toMobile+"]");
		    CommonPreferences.setActiviteSMSTo(toMobile);
		    CommonPreferences.setActiviteSMSContent(channel.getContent());
		}else{
		    SLog.w(TAG, "通道号未获取到，保留原来的通道号["+CommonPreferences.getActiviteSMSTo()+"]，通道内容：["+CommonPreferences.getActiviteSMSContent()+"]");
		} 
        
	}
	
	@Override
	public Channel getBindChannel(){
		return new Channel(StringUtils.split(CommonPreferences.getActiviteSMSTo(),"|"),CommonPreferences.getActiviteSMSContent());
	}

	
	
	@Override
	public boolean isCanSendBind(){
		long bindTime = CommonPreferences.getChangeBindSendSMSTime(); //发送短信绑定的时间
		boolean isBindLocal = this.isBindLocal();
		//特殊处理
        if(bindTime!=0 && (isBindLocal)){
            CommonPreferences.saveChangeBindSendSMSTime(0L);
        }
		//0表求已经绑定完成
		if(bindTime==0 && isBindLocal){
			SLog.d(TAG, "已经绑定成功!不能再次发起绑定!bindTime="+bindTime);
			return false;
		}
		//如果是bindTime ==-1默认值，意思是绑定的短信没有发送过，可以再次绑定
		if(bindTime ==-1 ){
			SLog.w(TAG, "从没有发送过激活短信!!!可以再次发送绑定短信...");
			return true;
		}
		
		SLog.d(TAG, "上一次绑定时间:"+new Date(bindTime));
		if( System.currentTimeMillis()-bindTime>=Constants.CHECK_BIND_INTERVAL){
			return true;
		}else {
			SLog.d(TAG, "不能频繁的发起绑定!");
			return false;
		}
	}
	
	public boolean isBindLocal(){
		 boolean isBind = false;
		 String bindstatus = SettingsPreferences.getBindStatus();
	     if (SettingsPreferences.UNBIND.equals(bindstatus)) {
	    	 SLog.d(TAG, "绑定状态-->未绑定!");
	    	 isBind = false;
	     }
	     else if (SettingsPreferences.BIND_OTHER.equals(bindstatus)) {
	    	 SLog.d(TAG, "绑定状态-->绑定了其他手机!");
	    	 isBind = false;
	     }
	     else  if(StringUtils.isBlank(SettingsPreferences.getMobile())){
	    	 SLog.d(TAG, "绑定状态-->手机号为空!");
	    	 SettingsPreferences.saveBindInfo(SettingsPreferences.UNBIND, "");
	    	 isBind = false;
	     }else{
	    	 isBind = true;
	     }
	    
	     SLog.i(TAG, "是否绑定本地状态:"+isBind);
	     return isBind;
	}
	
	public int addLoginTimes(){
		SLog.d(TAG, "累加用户登录次数..");
		CommonPreferences.saveLoginTimes(CommonPreferences.getLoginTimes() + 1);
		return CommonPreferences.getLoginTimes();
	}
}
