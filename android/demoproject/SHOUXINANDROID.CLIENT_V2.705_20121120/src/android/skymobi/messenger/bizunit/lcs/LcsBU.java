
package android.skymobi.messenger.bizunit.lcs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.SystemClock;
import android.skymobi.app.c2v.RevData;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bizunit.BaseBU;
import android.skymobi.messenger.dataaccess.DAManager;
import android.skymobi.messenger.dataaccess.lcs.ILcsDA;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.text.format.DateUtils;

import com.skymobi.android.sx.codec.beans.lcs.ActionDataInfo;
import com.skymobi.android.sx.codec.beans.lcs.DataInfo;
import com.skymobi.android.sx.codec.beans.lcs.LcsAndroidComplexResponse;
import com.skymobi.android.sx.codec.beans.lcs.LcsLogStatisticsRequest;
import com.skymobi.android.sx.codec.beans.lcs.LcsLogStatisticsResponse;
import com.skymobi.devicelog.DeviceLog;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @ClassName: LcsBU
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-10-22 下午01:57:12
 */
public class LcsBU extends BaseBU {

    private final static String TAG = LcsBU.class.getSimpleName();
    // 行为日志类型
    private final static String ACTIONLOG_TYPE = "ACTIONLOG_TYPE";
    // 数据日志类型
    private final static String DATALOG_TYPE = "DATALOG_TYPE";
    private static final String ACTION_ALARM = "android.skymobi.messenger.alarm";
    private PendingIntent mSender;
    private AlarmRecevier mAlarmRecevier;
    private AlarmManager am;
    private ILcsDA lcsDA;

    private final HashMap<Integer, String> typeMap = new HashMap<Integer, String>();

    public LcsBU() {
        super(null);
        lcsDA = (ILcsDA) DAManager.get(DAManager.DA_LCS);
    }

    /*
     * 初始化设置
     */
    public void initAlarmRecevier() {
        SLog.d(TAG, "\tzhang初始化提醒接收器");
        if (null != MainApp.i()) {
            mAlarmRecevier = new AlarmRecevier();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_ALARM);
            MainApp.i().registerReceiver(mAlarmRecevier, intentFilter);

            am = (AlarmManager) MainApp.i().getSystemService(Context.ALARM_SERVICE);

            mSender = PendingIntent.getBroadcast(MainApp.i(), 0, new Intent(ACTION_ALARM),
                    0);
            long firstTime = SystemClock.elapsedRealtime();
            firstTime += DateUtils.HOUR_IN_MILLIS;

            if (am != null) {
                am.setRepeating(AlarmManager.ELAPSED_REALTIME, firstTime,
                        DateUtils.HOUR_IN_MILLIS, mSender);
            }
        }
        SLog.d(TAG, "\t 结束初始化提醒接收器");
    }

    /**
     * 反注册监听器
     */
    public void uninitAlarmRecevier() {
        SLog.d(TAG, "\tzhang反注册Alarm接收广播..");
        if (null != MainApp.i()) {
            if (mAlarmRecevier != null) {
                MainApp.i().unregisterReceiver(mAlarmRecevier);
            }
        }
        if (mSender != null && am != null) {
            am.cancel(mSender);
        }

    }

    /**
     * 发送LCS统计数据之数据统计信息
     */
    private boolean sendStatisticsLog(final ArrayList<DataInfo> dataInfoList) {
        LcsLogStatisticsRequest req = new LcsLogStatisticsRequest();
        req.setSkyid(SettingsPreferences.getSKYID());
        req.setAppid(Constants.APPID);
        PackageInfo pi = MainApp.i().getPi();
        if (null != pi)
            req.setApp_ver(pi.versionCode);
        req.setDataInfoList(dataInfoList);
        typeMap.put(req.getSeqid(), DATALOG_TYPE);
        return client.sendBean(req);
    }

    /**
     * 发送LCS统计数据之行为数据信息
     */
    private boolean sendActionLog(final ArrayList<ActionDataInfo> dataInfoList) {
        LcsLogStatisticsRequest req = new LcsLogStatisticsRequest();
        req.setSkyid(SettingsPreferences.getSKYID());
        req.setAppid(Constants.APPID);
        PackageInfo pi = MainApp.i().getPi();
        if (null != pi)
            req.setApp_ver(pi.versionCode);
        req.setActionDataInfoList(dataInfoList);
        typeMap.put(req.getSeqid(), ACTIONLOG_TYPE);
        return client.sendBean(req);
    }

    /*
     * 上传统计数据日志 需要新增发送策略
     */
    public boolean uploadStatisticsLog() {
        SLog.d(TAG, "\tzhang上传统计数据");
        ArrayList<DataInfo> list = new ArrayList<DataInfo>();
        // 短信统计 需要从DA中取相关信息
        int smsCount = lcsDA.getSingleSmsCount();
        int which_hour = (int) (System.currentTimeMillis() / (3600 * 1000));
        if (smsCount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) smsCount);
            info.setData_code(ILcsDA.MSGCODE_DATA_STATISTIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_SINGLE_SMS);
            info.setMsg_type(ILcsDA.MSG_TYPE_TEXT);
            info.setWhich_hour(which_hour);
            list.add(info);
        }
        // 名片统计
        int netCardCount = lcsDA.getNetCARDCount();
        if (netCardCount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) netCardCount);
            info.setData_code(ILcsDA.MSGCODE_DATA_STATISTIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_SINGLE_NET);
            info.setMsg_type(ILcsDA.MSG_TYEP_CARD);
            info.setWhich_hour(which_hour);
            list.add(info);
        }
        // 网络消息统计
        int netTextcount = lcsDA.getNetTextCount();
        if (netTextcount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) netTextcount);
            info.setData_code(ILcsDA.MSGCODE_DATA_STATISTIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_SINGLE_NET);
            info.setMsg_type(ILcsDA.MSG_TYPE_TEXT);
            info.setWhich_hour(which_hour);
            list.add(info);
        }
        // 网络语言消息统计
        int netVoicecount = lcsDA.getNetVOICECount();
        if (netVoicecount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) netVoicecount);
            info.setData_code(ILcsDA.MSGCODE_DATA_STATISTIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_SINGLE_NET);
            info.setMsg_type(ILcsDA.MSG_TYPE_VOICE);
            info.setWhich_hour(which_hour);
            list.add(info);
        }
        // 群发消息统计
        int massMultiCount = lcsDA.getMassMULTICount();
        if (massMultiCount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) massMultiCount);
            info.setData_code(ILcsDA.MSGCODE_DATA_STATISTIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_MASS_MULTI);
            info.setMsg_type(ILcsDA.MSG_TYPE_TEXT);
            info.setWhich_hour(which_hour);
            list.add(info);
        }

        // 对附近的人打招呼的次数
        int clickBuddyCount = lcsDA.getClickBuddyCount();
        if (clickBuddyCount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) clickBuddyCount);
            info.setData_code(ILcsDA.MSGCODE_DATA_LBSSTATIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_SINGLE_SMS);
            info.setMsg_type(ILcsDA.MSG_TYPE_CLICK_BUDDY);
            info.setWhich_hour(which_hour);
            list.add(info);
        }

        // 附近的人点击次数
        int clickLbsCount = lcsDA.getClickLbsCount();
        if (clickLbsCount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) clickLbsCount);
            info.setData_code(ILcsDA.MSGCODE_DATA_LBSSTATIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_DEFAULT);
            info.setMsg_type(ILcsDA.MSG_TYPE_CLICK_LBS);
            info.setWhich_hour(which_hour);
            list.add(info);
        }

        // 跟几个人打了招呼
        int buddyPeopleCount = lcsDA.getBuddyPeopleCount();
        if (buddyPeopleCount > 0) {
            DataInfo info = new DataInfo();
            info.setCount((short) buddyPeopleCount);
            info.setData_code(ILcsDA.MSGCODE_DATA_LBSSTATIC);
            info.setMsg_dest(ILcsDA.MSG_DEST_DEFAULT);
            info.setMsg_type(ILcsDA.MSG_TYPE_BUDDY_PEOPLE);
            info.setWhich_hour(which_hour);
            list.add(info);
        }

        // 同时判断最后一次发送时间与现在的时间间隔是否大于一个小时，如果大于一个小时，则说明上一次发送失败。
        long spendTime = System.currentTimeMillis() - CommonPreferences.getLcsLastSendTime();
        if (list.size() > 0
                && (spendTime > DateUtils.HOUR_IN_MILLIS)) {
            return sendStatisticsLog(list);
        } else {
            SLog.d(TAG,
                            "sendStatisticsLog list size is "
                                    + list.size()
                                    + ",or [" + spendTime + " < " + DateUtils.HOUR_IN_MILLIS
                                    + "]!");
            return false;
        }
    }

    /**
     * 发送
     */
    public boolean sendInviteLog(String srcPhone, String desPhone, int where) {
        SLog.d(TAG, "\tzhang发送邀请数据");
        int which_hour = (int) (System.currentTimeMillis() / 1000);
        ActionDataInfo info = new ActionDataInfo();
        info.setAction(2);
        info.setTimeStr(String.valueOf(System.currentTimeMillis()));
        info.setTime(which_hour);
        if (where == -1) {
            // 默认从联系人界面
            info.setWhere(3);
        } else {
            info.setWhere(where);
        }
        info.setReserve1(srcPhone);
        info.setReserve2(desPhone);
        info.setResult(0);
        SLog.d(TAG,
                "sendActionLog send an invite sms message srcPhone = " + srcPhone
                        + ", desPhones = " + desPhone + ",result=" + info.getResult() + ",where="
                        + info.getWhere());
        ArrayList<ActionDataInfo> list = new ArrayList<ActionDataInfo>();
        list.add(info);
        return sendActionLog(list);
    }

    /*
     * 上传复合信息日志
     */
    public boolean uploadLcsComplexLog() {
        SLog.d(TAG, "\tzhang上传复合数据数据");
        // 发送终端能力信息 从DA中获取
        int lastDescVersion = CommonPreferences.getLastDescVerion();
        int currentVersion = MainApp.i().getPi().versionCode;
        SLog.d(TAG, "\tzhang文件中存储的版本:" + lastDescVersion + "|当前程序版本:" + currentVersion);
        // 判断版本不一样，并且终端信息未发送,存在问题，就是lastDescVersion参数只要到介绍页面才会修改，如果用户没有到介绍页面，这个时候可能会存在
        // 执行多次当前方法
        if (lastDescVersion != currentVersion) {
            DeviceLog devLog = DeviceLog.getInstance();
            if (null != devLog) {
                // 简单的发送日志的策略
                long times = System.currentTimeMillis();
                SLog.d(TAG, "\tzhang当前(" + times + "%2)运算结果为：" + (times % 2));
                if ((times % 2) == 0) {
                    String json = devLog.getTerminalInfo4Json(MainApp.i());
                    client.getNetBiz().lcsLogComplexMessage(json);
                }
            }
        }
        return false;
    }

    @Override
    public void revData(RevData data) {
        // 处理请求超时了
        if (data.getReqBean() != null) {
            if (data.getReqBean() instanceof LcsLogStatisticsResponse) {
                SLog.w(TAG, "发送日志统计数据超时");
                return;
            } else if (data.getReqBean() instanceof LcsAndroidComplexResponse) {
                SLog.w(TAG, "发送Android复合接口超时");
                return;
            } else {
                return;
            }
        }

        // LCS统计数据响应结果
        if (data.getReqBean() instanceof LcsLogStatisticsResponse) {
            LcsLogStatisticsResponse resp = (LcsLogStatisticsResponse) data.getReqBean();
            if (null != resp && resp.getResponseCode() == 200) {
                SLog.d(TAG, "sendStatisticsLog send success!");
                // DA 操作清理数据统计值
                if (null != typeMap.get(resp.getSeqid())
                        && typeMap.get(resp.getSeqid()).equals(DATALOG_TYPE)) {
                    lcsDA.clear();
                    MainApp.i().clearGreetStatus();
                    CommonPreferences.saveLcsLastSendTime(System.currentTimeMillis());
                }
                return;
            } else {
                SLog.d(TAG,
                        "sendStatisticsLog send failed!");
                return;
            }
        }
    }

    private class AlarmRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ACTION_ALARM)) {
                uploadStatisticsLog();
            }
        }
    }

    /**
     * @return the lcsDA
     */
    public ILcsDA getLcsDA() {
        return lcsDA;
    }

    /**
     * @param lcsDA the lcsDA to set
     */
    public void setLcsDA(ILcsDA lcsDA) {
        this.lcsDA = lcsDA;
    }

}
