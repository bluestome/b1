package android.skymobi.messenger.dataaccess;

import android.skymobi.messenger.dataaccess.bean.AttrConfig;
import android.skymobi.messenger.dataaccess.dao.config.ConfigDao;
import android.skymobi.messenger.dataaccess.dao.config.IConfigDao;

public class BasicDA extends AbsBasicDA implements IDA {
	
	private IConfigDao configDao = null;
	
	public BasicDA(){
		configDao = new ConfigDao();
	}

	@Override
	public void saveToConfig(AttrConfig config) {
		configDao.saveToConfig(config);
	}

	@Override
	public AttrConfig getAttrConfig(String attrName) {
		return configDao.getAttrConfig(attrName);
	}

}
