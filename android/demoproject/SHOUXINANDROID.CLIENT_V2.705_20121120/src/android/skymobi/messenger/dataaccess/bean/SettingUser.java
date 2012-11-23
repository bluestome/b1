package android.skymobi.messenger.dataaccess.bean;

public class SettingUser {

	private int skyid =0 ;
	
	private String nickName= null; //昵称
	 
	private String sex = null;//性别
	
	private String reserve0= null; //保留位
	
	
	public SettingUser(int skyid){
		this.skyid = skyid;
	}


	public int getSkyid() {
		return skyid;
	}


	public void setSkyid(int skyid) {
		this.skyid = skyid;
	}


	public String getNickName() {
		return nickName;
	}


	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getReserve0() {
		return reserve0;
	}

	public void setReserve0(String reserve0) {
		this.reserve0 = reserve0;
	}

	@Override
	public String toString(){
	    return "skyid:"+skyid+",nickName:"+nickName+",sex:"+sex+",reserve0:"+reserve0;
	}
}
