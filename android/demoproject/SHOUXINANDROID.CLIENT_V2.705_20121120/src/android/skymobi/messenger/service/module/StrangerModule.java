
package android.skymobi.messenger.service.module;

import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Stranger;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.database.dao.StrangerDAO;
import android.skymobi.messenger.service.CoreService;

/**
 * @ClassName: StrangerModule
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-9-6 上午10:51:42
 */
public class StrangerModule extends BaseModule {

    private final StrangerDAO strangerDAO;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param service
     */
    public StrangerModule(CoreService service) {
        super(service);
        strangerDAO = DaoFactory.getInstance(MainApp.i().getApplicationContext()).getStrangerDAO();
    }

    /**
     * 保存陌生人
     * 
     * @param stranger
     * @return
     */
    public boolean saveStranger(Stranger stranger) {
        boolean isAdd = false;
        int skyid = stranger.getSkyid();
        if (skyid > 0) {
            if (strangerDAO.checkExistsSkyid(skyid)) {
                isAdd = strangerDAO.update(stranger);
            } else {
                long id = strangerDAO.addStranger(stranger);
                if (id > 0)
                    isAdd = true;
            }
        }
        return isAdd;
    }

    /**
     * 保存陌生人
     * 
     * @param contact
     * @param nickName
     * @param skyName
     * @return
     */
    public boolean saveStranger(Contact contact, String nickName, String skyName) {
        boolean isAdd = false;
        int skyid = contact.getSkyid();
        if (skyid > 0) {
            if (strangerDAO.checkExistsSkyid(skyid)) {
                isAdd = strangerDAO.update(contact, nickName, skyName);
            } else {
                long id = strangerDAO.addStranger(contact, nickName, skyName);
                if (id > 0)
                    isAdd = true;
            }
        }
        return isAdd;
    }

    /**
     * 删除指定skyid信息
     * 
     * @param skyid
     * @return
     */
    public boolean delete(int skyid) {
        if (skyid > 0 && strangerDAO.checkExistsSkyid(skyid))
            return strangerDAO.delete(skyid);
        return false;
    }

    /**
     * 查找指定SKYID的帐号
     * 
     * @param skyid
     * @return
     */
    public Stranger fetch(int skyid) {
        return strangerDAO.fetch(skyid);
    }
}
