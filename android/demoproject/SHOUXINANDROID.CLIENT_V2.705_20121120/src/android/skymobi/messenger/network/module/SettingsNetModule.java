
package android.skymobi.messenger.network.module;

import android.skymobi.app.net.event.ISXListener;
import android.util.Log;

import com.skymobi.android.sx.codec.TerminalInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetDownloadImageRespInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetFsDownloadReq;
import com.skymobi.android.sx.codec.beans.clientbean.NetFsDownloadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetSetRecommendResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetImageDownloadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetRestorableContactsResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetRestoreContactsResp;
import com.skymobi.android.sx.codec.beans.clientbean.NetSetUserInfoResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetSupResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetUploadResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfo;
import com.skymobi.android.sx.codec.beans.clientbean.NetUserInfoResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetmodifyPwdResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @ClassName: SettingsNetModule
 * @Description: TODO
 * @author Michael.Pan
 * @date 2012-3-8 下午02:29:22
 */
public class SettingsNetModule extends BaseNetModule {

    /**
     * @param netClient.getBiz()
     */
    public SettingsNetModule(ISXListener netClient) {
        super(netClient);
    }

    /**
     * 注销接口
     * 
     * @param token
     * @return
     */
    public NetResponse logout(String token) {
        return netClient.getBiz().logout(token);
    }

    /**
     * 设置用户信息
     * 
     * @param token
     * @param nui
     * @return
     */
    public NetSetUserInfoResponse setNetUserInfo(String token, NetUserInfo nui) {
        return netClient.getBiz().setUserInfo(token, nui);
    }

    /**
     * 修改密码
     * 
     * @param token
     * @param oldPwd
     * @param newPwd
     * @return
     */
    public NetmodifyPwdResponse modifyPwd(String token, String oldPwd, String newPwd) {
        return netClient.getBiz().modifyPwd(token, oldPwd, newPwd);
    }

    /**
     * 用户反馈
     * 
     * @param nickName
     * @param content
     * @return
     */
    public NetResponse feedBack(String nickName, String content) {
        return netClient.getBiz().feedBack(nickName, content);
    }

    /**
     * 是否推荐, 是否共享地理位置
     * 
     * @param isRecommend
     * @param isShareLBS
     * @return
     */
    public NetResponse setRecommend(boolean isRecommend, boolean isShareLBS) {
        return netClient.getBiz().setRecommend(isRecommend, !isShareLBS);
    }

    /**
     * 获取是否设置为可推荐
     * 
     * @return
     */
    public NetGetSetRecommendResponse getRecommend() {
        return netClient.getBiz().getSetRecommendValue();
    }

    /**
     * 获取用户自身的详细信息
     * 
     * @param token
     * @return
     */
    public NetUserInfoResponse getUserInfo(String token) {
        return netClient.getBiz().getUserInfo(token);
    }

    /**
     * 上传文件
     * 
     * @param url
     * @param body
     * @return
     */
    public NetUploadResponse uploadFs(String url, int skyid, String token, String fileExtName,
            byte[] body) {
        return netClient.getBiz().uploadFs(url, skyid, token, fileExtName, body);
    }

    /**
     * 上传图片
     * 
     * @param url
     * @param body
     * @param fileExtName
     * @param skyid
     * @param token
     * @return
     */
    public NetUploadResponse uploadImage(String url, byte[] body, String fileExtName, int skyid,
            String token) {
        return netClient.getBiz().sfsUploadImage(url, body, fileExtName, skyid, token);
    }

    /**
     * 下载文件
     * 
     * @param url
     * @param md5
     * @return
     */
    public NetFsDownloadResponse downloadFs(String url, int skyid, String token,
            ArrayList<NetFsDownloadReq> downloadList) {
        Log.e("test", "downloadFs............url = " + url + ",skyid = " + skyid);
        return netClient.getBiz().downloadFs(url, skyid, token, downloadList);
    }

    /**
     * 下载图片
     * 
     * @param url
     * @param skyid
     * @param token
     * @param reqList
     * @return
     */
    public NetImageDownloadResponse downloadImage(String url, int skyid, String token,
            ArrayList<NetDownloadImageRespInfo> reqList) {
        // Log.e("test", "sfsDownloadImage............url = " + url +
        // ",skyid = " + skyid);
        NetImageDownloadResponse response = null;
        try {
            response = netClient.getBiz().sfsDownloadImage(url, skyid, token,
                    reqList);
        } catch (Exception e) {
        }

        // Log.e("test",
        // "sfsDownloadImage............response.getResultCode= " +
        // response.getResultCode()
        // + ",response.isFailed() = " + response.isFailed());
        return response;
    }

    /**
     * 下载文件内容
     * 
     * @param info
     * @param url
     * @param path
     * @param md5
     * @param startPos
     * @return
     * @throws IOException
     */
    public NetSupResponse nupdate(TerminalInfo info, String url, String path, String md5,
            int startPos, int fileTotalSize) throws IOException {
        return netClient.getBiz().nupdate(info, url, path, md5, startPos, fileTotalSize);
    }

    /**
     * 检查更新 ,方法内部需要对请求内容进行编码
     * 
     * @param url
     * @param info
     * @return
     * @throws IOException
     */
    public NetSupResponse checkSupUpdate(String url, TerminalInfo info) throws IOException {
        try {
            return netClient.getBiz().checkSupupdate(url, info);
        } catch (IOException e) {
            Log.e(TAG, "检查更新时发生错误:" + e.getMessage());
            return null;
        }
    }

    /**
     * 查看可恢复联系人列表
     * 
     * @param start 起始页
     * @param pageSize 每页显示数量
     * @return
     */
    public NetRestorableContactsResponse getRestorableConacts(int start, int pageSize) {
        return netClient.getBiz().getRestorableConacts(start, pageSize);
    }

    /**
     * 批量恢复联系人
     * 
     * @param rids 联系人列表
     * @return
     */
    public NetRestoreContactsResp restoreContacts(ArrayList<Integer> rids) {
        return netClient.getBiz().restoreConacts(rids);
    }

}
