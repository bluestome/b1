
package android.skymobi.messenger.logreport;

import android.content.Context;

import com.mobclick.android.MobclickAgent;


public class SkymobiclickAgent extends BaseAgent{
	

	
	/**
	 * 在Debug 模式下，会在logcat中打印debug信息
	 * */
	public static void setDebugMode(boolean isDebug){
		MobclickAgent.setDebugMode(isDebug);
	}
	

    /**
     * 设置Session启动
     * 自定义Session重启间隔，注意参数是以毫秒为单位的。例如当应用切入后台，如果您认为应用在60秒之内返回应用可视为同一次启动
     * ，超过60秒返回当前应用可视为一次新的启动，那么在程序入口调用 setSessionContinueMillis(60000)。
     */
    public static void setSessionContinueMillis(long paramLong) {
        MobclickAgent.setSessionContinueMillis(paramLong);
    }

    /**
     * context 当前Activity的引用,这里请不要将全局的application context传入。 确保在activity中都调用
     * MobclickAgent.onResume() 和MobclickAgent.onPause()方法
     * 基本统计实现本页面跳转，机型，分辨率，地理位置 …的统计
     */
    public static void onPause(Context ctx) {
        MobclickAgent.onPause(ctx);
    }

    /**
	 * 
	 * */
    public static void onResume(Context ctx) {
        MobclickAgent.onResume(ctx);
    }

    /**
     * 自动捕获异常退出（FC） 在AndroidManifest.xml里面添加权限android.permission.READ_LOGS
     * 在程序的Main Activity(应用程序入口)的onCreate方法里调用
     */
    public static void onError(Context ctx) {
        MobclickAgent.onError(ctx);
    }

    /**
     * 手动发送错误报告 如果您自己捕获了程序中的异常，但是依然希望，将这次异常信息发送到服务器，您可以调用下面的函数。
     */
    public static void reportError(Context ctx, String error) {
        MobclickAgent.reportError(ctx, error);
    }
    
    /**
     * 事件数量统计
     * event_id 为当前统计的事件ID
     * "net_change"网络切换事件
     * */
   // public static void onEvent(Context ctx, String event_id){
   // 	MobclickAgent.onEvent(ctx, event_id);
   // }
    
    public static void onEvent(Context ctx, String event_id, String label){
    	MobclickAgent.onEvent(ctx, event_id, label);
    }
}
