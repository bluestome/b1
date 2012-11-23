package android.skymobi.messenger.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class MoveBar extends RelativeLayout {
	
	Scroller mScroller=null;
	public MoveBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context);
	}
	
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub

		if(mScroller!=null)
		{
			if(mScroller.computeScrollOffset())
			{
	            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
	            postInvalidate();  // So we draw again
			}
		}
		
	}

	public void startScroll(int startX, int startY, int dx, int dy, int duration)
	{
		mScroller.startScroll(startX, startY, dx, dy, duration);
	}
}
