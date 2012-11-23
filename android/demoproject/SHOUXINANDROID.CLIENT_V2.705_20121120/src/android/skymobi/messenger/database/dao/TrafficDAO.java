
package android.skymobi.messenger.database.dao;

import android.skymobi.messenger.bean.Traffic;

import java.util.List;

/**
 * @ClassName: TrafficDAO
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-3-20 下午03:54:48
 */
public interface TrafficDAO {

    /**
     * 添加记录
     * 
     * @param bean
     * @return
     */
    boolean insert(Traffic bean);

    /**
     * 修改指定ID的记录
     * 
     * @param bean
     * @return
     */
    boolean update(Traffic bean);

    /**
     * 修改指定条件的数据
     * 
     * @param bean 流量对象
     * @param whereClause 条件
     * @param whereArgs 值
     */
    boolean update(Traffic bean, String whereClause, String[] whereArgs);

    /**
     * 根据ID删除记录
     * 
     * @param id
     * @return
     */
    boolean delete(Integer id);

    /**
     * 查询数据
     * 
     * @param bean
     * @return
     */
    List<Traffic> query(Traffic bean);

    /**
     * 根据对象获取总记录数
     * 
     * @return
     */
    int getCount(Traffic bean);

    /**
     * 根据SQL查询数据
     * 
     * @param bean
     * @return
     */
    List<Traffic> query(String sql);

    /**
     * 获取指定月份的统计数据
     * 
     * @param date YYYY-MM
     * @return
     */
    Traffic getSum(String date);

    /**
     * 执行SQL语句
     * 
     * @param sql
     * @return
     */
    boolean execute(String sql);
}
