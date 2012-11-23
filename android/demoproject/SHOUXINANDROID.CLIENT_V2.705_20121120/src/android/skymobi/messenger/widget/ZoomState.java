
package android.skymobi.messenger.widget;

import java.util.Observable;

/**
 * @ClassName: ZoomState
 * @Description: 记录缩放状态
 * @author Lv.Lv
 * @date 2012-4-17 下午3:17:35
 */
public class ZoomState extends Observable {
    private float zoom = 1.0f;// 缩放系数，值越大图像越大
    private float panX = 0.5f;// 图片位置横坐标系数，值越小图像越靠左，0.5f时居中
    private float panY = 0.5f;// 图片位置纵坐标系数，值越小图像越靠上，0.5f时居中

    private float maxZoom = 20.0f;
    private float minZoom = 0.05f;

    public float getZoom() {
        return zoom;
    }

    public float setZoom(float zoom) {
        if (this.zoom != zoom && zoom > minZoom && zoom < maxZoom) {
            this.zoom = zoom;
            this.setChanged();
        }
        return this.zoom;
    }

    public void setZoomBound(float min, float max) {
        minZoom = min;
        maxZoom = max;
    }

    public float getPanX() {
        return panX;
    }

    public void setPanX(float panX) {
        if (this.panX != panX) {
            this.panX = panX;
            this.setChanged();
        }
    }

    public float getPanY() {
        return panY;
    }

    public void setPanY(float panY) {
        if (this.panY != panY) {
            this.panY = panY;
            this.setChanged();
        }
    }

    public float getZoomX(float aspectQuotient) {
        return Math.min(zoom, zoom * aspectQuotient);
    }

    public float getZoomY(float aspectQuotient) {
        return Math.min(zoom, zoom / aspectQuotient);
    }
}
