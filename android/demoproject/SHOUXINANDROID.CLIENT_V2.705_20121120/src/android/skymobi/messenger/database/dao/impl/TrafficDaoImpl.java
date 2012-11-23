
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.skymobi.messenger.bean.Traffic;
import android.skymobi.messenger.database.dao.TrafficDAO;
import android.skymobi.messenger.provider.SocialMessenger.TrafficColumns;

import java.util.List;

/**
 * @ClassName: TrafficDaoImpl
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-3-20 下午04:08:14
 */
public class TrafficDaoImpl extends BaseImpl implements TrafficDAO {

    /** 查询语句 **/
    private static String QUERY = "select _id as id,date,wifi,wifi_latest as wifiLatest,mobile,mobile_latest as mobileLatest,"
            +
            "app_wifi as appWifi,app_mobile as appMobile from " + TrafficColumns.TABLE_NAME;

    /** 统计语句 **/
    private static String SUM_QUERY = "select 1 as _id,'{DATE}' as date,sum(" + TrafficColumns.WIFI
            + ") as wifi,sum(" + TrafficColumns.WIFI_LATEST + ") as wifiLatest,sum("
            + TrafficColumns.MOBILE + ") as mobile,sum(" + TrafficColumns.MOBILE_LATEST
            + ") as mobileLatest," +
            "sum(" + TrafficColumns.APP_WIFI + ") as appWifi,sum(" + TrafficColumns.APP_MOBILE
            + ") as appMobile from " + TrafficColumns.TABLE_NAME + " where " + TrafficColumns.DATE
            + " like '{DATE}%'";

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
    public TrafficDaoImpl(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    /**
     * 添加记录
     * 
     * @param bean 刘翔对象
     */
    public boolean insert(Traffic bean) {
        synchronized (this) {
            ContentValues traffic = new ContentValues();
            traffic.put(TrafficColumns.DATE, bean.getDate());
            traffic.put(TrafficColumns.WIFI, bean.getWifi());
            traffic.put(TrafficColumns.WIFI_LATEST, bean.getWifiLatest());
            traffic.put(TrafficColumns.MOBILE, bean.getMobile());
            traffic.put(TrafficColumns.MOBILE_LATEST, bean.getMobileLatest());
            traffic.put(TrafficColumns.APP_WIFI, bean.getAppWifi());
            traffic.put(TrafficColumns.APP_MOBILE, bean.getAppMobile());
            long id = insert(TrafficColumns.TABLE_NAME, null, traffic);
            if (id > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 修改指定条件的数据
     * 
     * @param bean 流量对象
     */
    public boolean update(Traffic bean) {
        synchronized (this) {
            String whereClause = " _id=?";
            String[] whereArgs = new String[] {
                    bean.getId() + ""
            };
            ContentValues traffic = new ContentValues();
            traffic.put(TrafficColumns.DATE, bean.getDate());
            traffic.put(TrafficColumns.WIFI, bean.getWifi());
            traffic.put(TrafficColumns.WIFI_LATEST, bean.getWifiLatest());
            traffic.put(TrafficColumns.MOBILE, bean.getMobile());
            traffic.put(TrafficColumns.MOBILE_LATEST, bean.getMobileLatest());
            traffic.put(TrafficColumns.APP_WIFI, bean.getAppWifi());
            traffic.put(TrafficColumns.APP_MOBILE, bean.getAppMobile());
            int result = update(TrafficColumns.TABLE_NAME, traffic, whereClause, whereArgs);
            if (result > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 修改指定条件的数据
     * 
     * @param bean 流量对象
     * @param whereClause 条件
     * @param whereArgs 值
     */
    public boolean update(Traffic bean, String whereClause, String[] whereArgs) {
        synchronized (this) {
            ContentValues traffic = new ContentValues();
            if (null != bean.getDate() && !bean.getDate().equals("")) {
                traffic.put(TrafficColumns.DATE, bean.getDate());
            }
            if (null != bean.getWifi() && bean.getWifi() > 0) {
                traffic.put(TrafficColumns.WIFI, bean.getWifi());
            }
            if (null != bean.getWifiLatest() && bean.getWifiLatest() > 0) {
                traffic.put(TrafficColumns.WIFI_LATEST, bean.getWifiLatest());
            }
            if (null != bean.getMobile() && bean.getMobile() > 0) {
                traffic.put(TrafficColumns.MOBILE, bean.getMobile());
            }
            if (null != bean.getMobileLatest() && bean.getMobileLatest() > 0) {
                traffic.put(TrafficColumns.MOBILE_LATEST, bean.getMobileLatest());
            }
            if (null != bean.getAppWifi() && bean.getAppWifi() > 0) {
                traffic.put(TrafficColumns.APP_WIFI, bean.getAppWifi());
            }
            if (null != bean.getAppMobile() && bean.getAppMobile() > 0) {
                traffic.put(TrafficColumns.APP_MOBILE, bean.getAppMobile());
            }
            int result = update(TrafficColumns.TABLE_NAME, traffic, whereClause, whereArgs);
            if (result > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 根据ID删除数据
     * 
     * @param id 数据ID
     */
    public boolean delete(Integer id) {
        synchronized (this) {
            String whereClause = " _id=?";
            String[] whereArgs = new String[] {
                    id + ""
            };
            int result = delete(TrafficColumns.TABLE_NAME, whereClause, whereArgs);
            if (result > 0) {
                return true;
            }
            return false;
        }
    }

    @Override
    public List<Traffic> query(Traffic bean) {
        StringBuffer sql = new StringBuffer();
        sql.append(QUERY);
        if (null != bean) {
            sql.append("\t where 1=1 ");
            if (null != bean.getId()) {
                sql.append(" and ").append(TrafficColumns._ID).append(" =").append(bean.getId())
                        .append("\t");
            }
            if (null != bean.getDate() && !"".equals(bean.getDate())) {
                sql.append(" and ").append(TrafficColumns.DATE).append(" ='")
                        .append(bean.getDate()).append("'\t");
            }
            if (null != bean.getWifi() && bean.getWifi() > 0) {
                sql.append(" and ").append(TrafficColumns.WIFI).append(" =").append(bean.getWifi())
                        .append("\t");
            }
            if (null != bean.getWifiLatest() && bean.getWifiLatest() > 0) {
                sql.append(" and ").append(TrafficColumns.WIFI_LATEST).append(" =")
                        .append(bean.getWifiLatest()).append("\t");
            }
            if (null != bean.getMobile() && bean.getMobile() > 0) {
                sql.append(" and ").append(TrafficColumns.MOBILE).append(" =")
                        .append(bean.getMobile()).append("\t");
            }
            if (null != bean.getMobileLatest() && bean.getMobileLatest() > 0) {
                sql.append(" and ").append(TrafficColumns.MOBILE_LATEST).append(" =")
                        .append(bean.getMobileLatest()).append("\t");
            }
            if (null != bean.getAppMobile() && bean.getAppMobile() > 0) {
                sql.append(" and ").append(TrafficColumns.APP_MOBILE).append(" =")
                        .append(bean.getAppMobile()).append("\t");
            }
            if (null != bean.getAppWifi() && bean.getAppWifi() > 0) {
                sql.append(" and ").append(TrafficColumns.APP_WIFI).append(" =")
                        .append(bean.getAppWifi()).append("\t");
            }
        }
        sql.append(" order by ").append(TrafficColumns._ID).append(" asc");
        return queryWithSort(Traffic.class, sql.toString());
    }

    /**
     * 执行SQL
     * 
     * @param sql 数据脚本
     */
    public boolean execute(String sql) {
        try {
            executeSQL(sql);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public List<Traffic> query(String sql) {
        return queryWithSort(Traffic.class, sql);
    }

    /**
     * 根据月份统计数据
     * 
     * @param date 时间格式:YYYY-MM
     */
    public Traffic getSum(String date) {
        String sql = SUM_QUERY;
        sql = sql.replace("{DATE}", date);
        List<Traffic> list = queryWithSort(Traffic.class, sql);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据对象获取总记录数
     * 
     * @return
     */
    public int getCount(Traffic bean) {
        int c = 0;
        StringBuffer sql = new StringBuffer(" 1=1 ");
        if (null != bean) {
            if (null != bean.getId()) {
                sql.append(" and ").append(TrafficColumns._ID).append(" =").append(bean.getId())
                        .append("\t");
            }
            if (null != bean.getDate() && !"".equals(bean.getDate())) {
                sql.append(" and ").append(TrafficColumns.DATE).append(" ='")
                        .append(bean.getDate()).append("'\t");
            }
            if (null != bean.getWifi() && bean.getWifi() > 0) {
                sql.append(" and ").append(TrafficColumns.WIFI).append(" =").append(bean.getWifi())
                        .append("\t");
            }
            if (null != bean.getWifiLatest() && bean.getWifiLatest() > 0) {
                sql.append(" and ").append(TrafficColumns.WIFI_LATEST).append(" =")
                        .append(bean.getWifiLatest()).append("\t");
            }
            if (null != bean.getMobile() && bean.getMobile() > 0) {
                sql.append(" and ").append(TrafficColumns.MOBILE).append(" =")
                        .append(bean.getMobile()).append("\t");
            }
            if (null != bean.getMobileLatest() && bean.getMobileLatest() > 0) {
                sql.append(" and ").append(TrafficColumns.MOBILE_LATEST).append(" =")
                        .append(bean.getMobileLatest()).append("\t");
            }
            if (null != bean.getAppMobile() && bean.getAppMobile() > 0) {
                sql.append(" and ").append(TrafficColumns.APP_MOBILE).append(" =")
                        .append(bean.getAppMobile()).append("\t");
            }
            if (null != bean.getAppWifi() && bean.getAppWifi() > 0) {
                sql.append(" and ").append(TrafficColumns.APP_WIFI).append(" =")
                        .append(bean.getAppWifi()).append("\t");
            }
        }
        c = queryCount(TrafficColumns.TABLE_NAME + " ", sql.toString());
        return c;
    }

}
