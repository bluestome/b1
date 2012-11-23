package android.skymobi.messenger.dataaccess.bean;

public class Channel {
	
	private String content;
	
	private String []targetNo;
	
	public Channel(String []targetNo,String content){
		this.targetNo= targetNo;
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String[] getTargetNo() {
		return targetNo;
	}

	public void setTargetNo(String[] targetNo) {
		this.targetNo = targetNo;
	}
	
	

}
