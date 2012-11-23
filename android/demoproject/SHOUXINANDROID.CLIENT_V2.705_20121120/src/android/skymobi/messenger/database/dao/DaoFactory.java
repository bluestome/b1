
package android.skymobi.messenger.database.dao;

import android.content.Context;
import android.skymobi.messenger.database.dao.impl.AddressImpl;
import android.skymobi.messenger.database.dao.impl.BaseImpl;
import android.skymobi.messenger.database.dao.impl.CitysImpl;
import android.skymobi.messenger.database.dao.impl.ContactsImpl;
import android.skymobi.messenger.database.dao.impl.FriendsImpl;
import android.skymobi.messenger.database.dao.impl.MessagesImpl;
import android.skymobi.messenger.database.dao.impl.ResFilesImpl;
import android.skymobi.messenger.database.dao.impl.StrangerImpl;
import android.skymobi.messenger.database.dao.impl.TrafficDaoImpl;
import android.skymobi.messenger.database.dao.impl.UsersImpl;

/**
 * @ClassName: DaoFactory
 * @Description: 工厂类
 * @author Sean.Xie
 * @date 2012-2-9 下午5:21:13
 */
public class DaoFactory {

    private static BaseDAO baseDAO = null;
    private static MessagesDAO messagesDAO = null;
    private static ContactsDAO contactsDAO = null;
    private static FriendsDAO friendsDAO = null;
    private static CitysDAO citysDAO = null;
    private static TrafficDAO trafficDAO = null;
    private static ResFilesDAO resfilesDAO = null;
    private static UsersDAO usersDAO = null;
    private static AddressDAO addressDAO = null;
    private static StrangerDAO strangerDAO = null;

    private static DaoFactory factory = null;

    private DaoFactory(Context context) {
        baseDAO = new BaseImpl(context);
        messagesDAO = new MessagesImpl(context);
        contactsDAO = new ContactsImpl(context);
        friendsDAO = new FriendsImpl(context);
        citysDAO = new CitysImpl(context);
        trafficDAO = new TrafficDaoImpl(context);
        resfilesDAO = new ResFilesImpl(context);
        usersDAO = new UsersImpl(context);
        addressDAO = new AddressImpl(context);
        strangerDAO = new StrangerImpl(context);
    }

    public synchronized static DaoFactory getInstance(Context context) {
        if (factory == null) {
            factory = new DaoFactory(context);
        }
        return factory;
    }

    public MessagesDAO getMessagesDAO() {
        return messagesDAO;
    }

    public ContactsDAO getContactsDAO() {
        return contactsDAO;
    }

    /**
     * @return the friendsDAO
     */
    public FriendsDAO getFriendsDAO() {
        return friendsDAO;
    }

    public CitysDAO getCitysDAO() {
        return citysDAO;
    }

    /**
     * @return the trafficDAO
     */
    public TrafficDAO getTrafficDAO() {
        return trafficDAO;
    }

    public ResFilesDAO getResfilesDAO() {
        return resfilesDAO;
    }

    public UsersDAO getUsersDAO() {
        return usersDAO;
    }

    public AddressDAO getAddressDAO() {
        return addressDAO;
    }

    public StrangerDAO getStrangerDAO() {
        return strangerDAO;
    }

    public BaseDAO getBaseDAO() {
        return baseDAO;
    }
}
