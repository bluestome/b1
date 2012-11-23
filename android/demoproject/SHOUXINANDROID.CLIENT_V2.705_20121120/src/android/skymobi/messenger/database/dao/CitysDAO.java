
package android.skymobi.messenger.database.dao;

import java.util.ArrayList;

/**
 * @ClassName: CitysDAO
 * @Description: 城市数据库操作
 * @author Lv.Lv
 * @date 2012-3-8 下午2:54:59
 */
public interface CitysDAO {

    /**
     * 获取所有省份列表
     * 
     * @return
     */
    ArrayList<String> getAllProvinces();

    /**
     * 获取指定省份的城市列表
     * 
     * @param provice 指定的省份
     * @return
     */
    ArrayList<String> getCitysInProvince(String provice);

    /**
     * @param id the index id in the array list returned by getAllProvinces()
     * @return the name of the province
     */
    String getProvinceName(int id);

    /**
     * @param province the province
     * @param id the index id in the array list returned by
     *            getCitysInProvince(province)
     * @return
     */
    String getCityName(String province, int id);

    void closeDatabase();
}
