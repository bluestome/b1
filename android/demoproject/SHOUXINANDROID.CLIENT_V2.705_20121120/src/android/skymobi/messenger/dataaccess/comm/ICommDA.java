package android.skymobi.messenger.dataaccess.comm;

import android.skymobi.messenger.dataaccess.IDA;


/**
 * 
 * 读取配置文件的数据访问接口
 * 
 * */
public interface ICommDA extends IDA{
	
	public final static byte COMMON_PREFERENCES = 0x1;
	public final static byte SETTINGS_PREFERENCES = 0x2;
	public final static byte LCS_PREFERENCES = 0x3;
	
	public final static String KEY_CP_="";
	
	/**
	 * 保存数据至配置文件
	 * 
	 * @param key 键
	 * @param obj 值
	 * @param flag 配置文件标识位
	 * 
	 * */
	public void save(final String key,final Object obj,final byte flag);
	
	/**
	 * 保存数据至配置文件
	 * 
	 * @param key 键
	 * @param keyParams 键的参数值，可以最终组合key值，必须按顺序传入。如key="KEY_{0}_{1}",keyParams则为new String[]{"A","B"}，最终key="KEY_A_B"
	 * @param obj 值
	 * @param flag 配置文件标识位
	 * 
	 * */
	public void save(final String key,final String[] keyParams,final Object obj,final byte flag);
	
	/**
	 * 从配置文件中获取值
	 * 
	 * @param key 键
	 * @param flag 配置文件标识位
	 * @return obj 值
	 * 
	 * */
	public Object get(final String key,final byte flag);
	
	/**
	 * 从配置文件中获取值
	 * 
	 * @param key 键
	 * @param keyParams 键的参数值，可以最终组合key值，必须按顺序传入。如key="KEY_{0}_{1}",keyParams则为new String[]{"A","B"}，最终key="KEY_A_B"
	 * @param flag 配置文件标识位
	 * @return obj 值
	 * 
	 * */
	public Object get(final String key,final String[] keyParams,final byte flag);

}
