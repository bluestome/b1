package android.skymobi.messenger.service.exception;

/**
 * @ClassName: ServiceIsNullException
 * @Description: TODO
 * @author Wing.Hu
 * @date 2012-10-15 下午01:40:36
 */
public class ServiceIsNullException  extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public ServiceIsNullException() {
        super();
    }


    public ServiceIsNullException(String msg) {
        super(msg);
    }


    public ServiceIsNullException(String msg, Throwable throwable) {
        super(msg, throwable);
    }


    public ServiceIsNullException(Throwable throwable) {
        super(throwable);
    }

}
