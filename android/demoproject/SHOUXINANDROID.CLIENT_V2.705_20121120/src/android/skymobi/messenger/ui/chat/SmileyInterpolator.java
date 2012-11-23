
package android.skymobi.messenger.ui.chat;

import android.view.animation.Interpolator;

/**
 * @ClassName: SmileyInterpolator
 * @Description: TODO
 * @author Sivan.LV
 * @date 2012-10-10 下午4:58:58
 */
public class SmileyInterpolator implements Interpolator {
    private final float alY = 1.3F;

    @Override
    public float getInterpolation(float input) {
        float f = input - 1.0F;
        return 1.0F + f * f * (f * (1.0F + this.alY) + this.alY);
    }

}
