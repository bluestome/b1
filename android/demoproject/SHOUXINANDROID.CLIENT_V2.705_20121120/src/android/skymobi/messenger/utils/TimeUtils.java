package android.skymobi.messenger.utils;

public class TimeUtils {
	 
	private TimeUtils(){}
	
	
	public static float getTimeconsuming(final long bTime,final long eTime){
		return ((float)(eTime-bTime))/1000;
	}

}
