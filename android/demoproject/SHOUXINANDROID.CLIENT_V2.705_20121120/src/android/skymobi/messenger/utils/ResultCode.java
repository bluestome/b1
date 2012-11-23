package android.skymobi.messenger.utils;

/**
 * 返回码临时保存对象
 * @ClassName: ResultCodeBlock
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-8-17 上午11:17:00
 */
public class ResultCode {

    private static int code = 0;
    
    private static byte[] lock = new byte[0];

    /**
     * @return the code
     */
    public static int getCode() {
        synchronized(lock){
            return code;
        }
    }

    /**
     * @param code the code to set
     */
    public static void setCode(int code) {
        synchronized(lock){
            ResultCode.code = code;
        }
    }
    
    
}
