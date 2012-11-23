
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.skymobi.messenger.bean.Address;
import android.skymobi.messenger.database.dao.AddressDAO;
import android.skymobi.messenger.provider.SocialMessenger.AddressColumns;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.text.TextUtils;

import java.util.List;

/**
 * @ClassName: AddressImpl
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-4-17 上午9:51:07
 */
public class AddressImpl extends BaseImpl implements AddressDAO {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param context
     */
    public AddressImpl(Context context) {
        super(context);
    }

    @Override
    public long addAddress(Address address) {
        if (null == address)
            return -1;

        ContentValues values = new ContentValues();
        values.put(AddressColumns.PHONE, address.getPhone());
        values.put(AddressColumns.SKYID, address.getSkyId());
        return insert(AddressColumns.TABLE_NAME, null, values);
    }

    @Override
    public int deleteAddress(long id) {
        return delete(AddressColumns.TABLE_NAME, BaseColumns._ID + "=?", new String[] {
                String.valueOf(id)
        });
    }

    @Override
    public int deleteAddress(String ids) {
        String[] idList = ids.split(",");
        beginTransaction();
        for (String id : idList) {
            deleteAddress(Long.valueOf(id), "(select count(_id) from threads where address_ids='"
                    + id
                    + "' or address_ids like '%,"
                    + id + "' or address_ids like '" + id + ",%' or address_ids like '%," + id
                    + ",%')=0");
        }
        endTransaction(true);
        return 0;

    }

    @Override
    public int deleteAddress(Address address) {
        if (null == address)
            return -1;
        deleteAddress(address.getId());
        return 0;
    }

    @Override
    public int updateAddress(Address address) {
        if (null == address || address.getId() <= 0)
            return -1;
        ContentValues values = new ContentValues();

        if (!TextUtils.isEmpty(address.getPhone())) {
            values.put(AddressColumns.PHONE, address.getPhone());
        }

        if (address.getSkyId() > 0) {
            values.put(AddressColumns.SKYID, address.getSkyId());
        }

        return update(AddressColumns.TABLE_NAME, values, BaseColumns._ID + "=?", new String[] {
                String.valueOf(address.getId())
        });
    }

    @Override
    public Address getAddress(long id) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(AddressColumns._ID).append(" as id,")
                .append(AddressColumns.PHONE).append(" as phone,").append(AddressColumns.SKYID)
                .append(" as skyid").append(" from ").append(AddressColumns.TABLE_NAME)
                .append(" where id=").append(id);
        return queryForObject(Address.class, sql.toString());
    }

    @Override
    public List<Address> getAddressList(String whereClause) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(AddressColumns._ID).append(" as id,")
                .append(AddressColumns.PHONE).append(" as phone,").append(AddressColumns.SKYID)
                .append(" as skyid,").append(AddressColumns.ACCOUNTID).append(" as accountId")
                .append(" from ").append(AddressColumns.TABLE_NAME)
                .append(" where ").append(whereClause);
        return queryWithSort(Address.class, sql.toString());
    }

    @Override
    public Address getAddressBySkyId(int skyId) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" skyid='").append(skyId).append("'");

        List<Address> addressList = getAddressList(whereClause.toString());
        /*
         * Address address = addressList.size() > 0 ? addressList.get(0) : null;
         * if (null == address) { address = new Address();
         * address.setSkyId(skyId); address.setId(addAddress(address)); } return
         * address;
         */
        return addressList.size() > 0 ? addressList.get(0) : null;
    }

    @Override
    public Address getAddressByPhone(String phones) {
        if (null == phones)
            throw new RuntimeException("参数不能为空");
        phones = AndroidSysUtils.removeHeader(phones);
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" phone='").append(phones).append("'");

        List<Address> addressList = getAddressList(whereClause.toString());
        /*
         * Address address = addressList.size() > 0 ? addressList.get(0) : null;
         * if (null == address) { address = new Address();
         * address.setPhones(phones); address.setId(addAddress(address)); }
         * return address;
         */
        return addressList.size() > 0 ? addressList.get(0) : null;
    }

    @Override
    public List<Address> getAddressListByIds(String ids) {
        StringBuilder whereClause = new StringBuilder();
        StringBuilder order = new StringBuilder();
        int i = 0;
        // 保证按id顺序排列
        for (String id : ids.split(",")) {
            i++;
            order.append(" when ").append("'" + id + "' then " + i);
        }
        whereClause.append(AddressColumns._ID).append(" in(").append(ids).append(")")
                .append(" order by case ").append("id").append(order.toString()).append(" end");
        return getAddressList(whereClause.toString());
    }

    @Override
    public Address getAddressByAccountId(long accountId) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" accountId='").append(accountId).append("'");

        List<Address> addressList = getAddressList(whereClause.toString());
        /*
         * Address address = addressList.size() > 0 ? addressList.get(0) : null;
         * if (null == address) { address = new Address();
         * address.setAccountId(accountId); address.setId(addAddress(address));
         * } return address;
         */
        return addressList.size() > 0 ? addressList.get(0) : null;
    }

    @Override
    public int deleteAddress(long id, String whereClause) {

        return delete(AddressColumns.TABLE_NAME, BaseColumns._ID + "=? and " + whereClause,
                new String[] {
                    String.valueOf(id)
                });
    }
}
