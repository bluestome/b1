package android.skymobi.messenger.dataaccess.dao.config;

import android.skymobi.messenger.dataaccess.bean.AttrConfig;

public interface IConfigDao {

	/**
	 * 将一些属性的标识位信息保存至配置表，一般是key,value的简单信息
	 * 
	 * @param config 属性keyvalue对象
	 * */
	public boolean saveToConfig(AttrConfig config);
	
	/**
	 * 获取属性
	 * 
	 * @param attrName 属性名称
	 * @return 属性对象
	 * */
	public AttrConfig getAttrConfig(final String attrName);
}
