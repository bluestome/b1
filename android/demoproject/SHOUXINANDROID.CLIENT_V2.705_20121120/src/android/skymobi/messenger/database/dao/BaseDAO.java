
package android.skymobi.messenger.database.dao;

/**
 * @ClassName: BaseDAO
 * @author Sean.Xie
 * @date 2012-5-12 上午12:11:51
 */
public interface BaseDAO {
    /**
     * 开始事务
     */
    public void beginTransaction();

    /**
     * 结束事务
     * 
     * @param isSuccess
     */
    public void endTransaction(boolean isSuccess);
}
