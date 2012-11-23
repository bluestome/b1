
package android.skymobi.messenger.database.dao;

import android.skymobi.messenger.bean.ResFile;

/**
 * @ClassName: FilesDAO
 * @Description: 文件操作
 * @author Sivan.LV
 * @date 2012-3-30 上午10:31:03
 */
public interface ResFilesDAO {

    /**
     * 添加文件
     * 
     * @return
     */
    long addFile(ResFile file);

    /**
     * 删除文件
     * 
     * @param file
     * @return
     */
    boolean deleteFile(ResFile file);

    /**
     * 删除文件
     * 
     * @param id
     */
    boolean deleteFile(long id);

    /**
     * 更新
     * 
     * @param file
     */
    long updateFile(ResFile file);

    /**
     * 查询
     * 
     * @param id
     * @return
     */
    ResFile getFile(long id);

    /**
     * 更新/添加接口（如果带photoID就做更新操作，否则使用添加操作）
     * 
     * @param file
     * @return file id
     */
    long updateOrAdd(ResFile file);
}
