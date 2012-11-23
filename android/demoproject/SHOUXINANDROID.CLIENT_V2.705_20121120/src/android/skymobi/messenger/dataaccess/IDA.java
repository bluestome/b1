package android.skymobi.messenger.dataaccess;

import android.skymobi.messenger.dataaccess.bean.AttrConfig;

public interface IDA {

	public  void saveToConfig(AttrConfig config);
	
	public  AttrConfig getAttrConfig(final String attrName);
	
}
