
package android.skymobi.messenger.logreport;

import android.content.Context;
import android.util.Log;

import com.skymobi.freesky.FreeSkySdk;

/**
 * @ClassName: FreeSkyAgent
 * @Description: 冒泡客sdk接入
 * @author Michael.Pan
 * @date 2012-9-25 下午04:21:29
 */
public class FreeSkyAgent {
    public static void initAgent(Context ctx) {
        // 获得 SDK 实例
        FreeSkySdk sdk = FreeSkySdk.getInstance();

        // 激活应用必须的接口,应用一启动就需要调用此接口
        // 应用文件上传到冒泡开放平台后,需立即下载并执行
        // 通过此接口上行的数据,平台会激活此应用,然后才允许提交发布
        sdk.init(ctx);

        // 激活有效用户的接口,可以根据业务特点放在其它地方
        // 若应用选择付费推广,则此接口的调用会计算一个有效用户
        // 多次执行只计算第一次有效
        sdk.doActiveReoprt();
        Log.d("FreeSkyAgent", "initAgent");
    }

    public static String getSkySdkChannel() {
        Log.d("FreeSkyAgent", "getSkySdkChannel");
        FreeSkySdk sdk = FreeSkySdk.getInstance();
        return String.valueOf(sdk.getAppInfo().getChannelId());
    }
}
