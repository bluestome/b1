
package android.skymobi.messenger.database.dao;

import android.skymobi.messenger.bean.Address;

import java.util.List;

/**
 * @ClassName: AddressDAO
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-4-17 上午9:46:29
 */
public interface AddressDAO {

    long addAddress(Address address);

    int deleteAddress(long id);

    int deleteAddress(long id, String whereClause);

    int deleteAddress(String ids);

    int deleteAddress(Address address);

    int updateAddress(Address address);

    Address getAddress(long id);

    Address getAddressBySkyId(int skyId);

    Address getAddressByPhone(String phones);

    Address getAddressByAccountId(long accountId);

    List<Address> getAddressList(String whereClause);

    List<Address> getAddressListByIds(String ids);

}
