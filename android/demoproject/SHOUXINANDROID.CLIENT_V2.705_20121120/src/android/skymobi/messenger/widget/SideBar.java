
package android.skymobi.messenger.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.skymobi.messenger.R;
import android.skymobi.messenger.ui.ContactsListActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SideBar extends View {
    private char[] letters;
    private SectionIndexer sectionIndexter = null;
    private ListView list;
    private TextView dialogText;
    private int itemHeight = 0;
    private Paint paint;

    private Handler handler;

    private int firstIndex;

    public SideBar(Context context) {
        super(context);
        init();
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        letters = new char[] {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#'
        };
        firstIndex = 'A';
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xff494c50);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setListView(ListView _list) {
        list = _list;
        initIndexter();
        // sectionIndexter = (SectionIndexer) _list.getAdapter();
    }

    public void setTextView(final TextView mDialogText, final Handler handler) {
        this.dialogText = mDialogText;
        this.handler = handler;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        list.setOnScrollListener(null);
        int i = (int) event.getY();
        itemHeight = getItemHeight();
        int idx = itemHeight != 0 ? i / itemHeight : 0;
        if (idx >= letters.length) {
            idx = letters.length - 1;
        } else if (idx < 0) {
            idx = 0;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            setBackgroundResource(R.drawable.contacts_list_position_tip);
            if (sectionIndexter == null) {
                initIndexter();
            }
            int headerViewCount = list.getHeaderViewsCount();
            int position = sectionIndexter.getPositionForSection(letters[idx]);
            if (position == 0) {
                firstIndex = letters[idx];
            }
            if (position == -1) {
                return true;
            }
            dialogText.setVisibility(View.VISIBLE);
            dialogText.setText("" + letters[idx]);
            if (letters[idx] == firstIndex) {
                list.setSelection(0);
            } else {
                list.setSelection(position + headerViewCount);
            }

            handler.sendMessage(handler.obtainMessage(ContactsListActivity.TOUCH_MOVE_DOWN, ""
                    + letters[idx]));
        } else {
            setBackgroundColor(Color.TRANSPARENT);
            dialogText.setVisibility(View.INVISIBLE);
            handler.sendEmptyMessage(ContactsListActivity.TOUCH_UP);
        }
        return true;
    }

    /**
     * 
     */
    private void initIndexter() {
        ListAdapter adapter = list.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        sectionIndexter = (SectionIndexer) adapter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        itemHeight = getItemHeight();
        float widthCenter = getMeasuredWidth() / 2;
        for (int i = 0; i < letters.length; i++) {
            canvas.drawText(String.valueOf(letters[i]), widthCenter, itemHeight
                    + (i * itemHeight), paint);
        }
    }
    private int getItemHeight() {
        if (itemHeight <= 0) {
            itemHeight = getHeight() / letters.length;
            paint.setTextSize(itemHeight * 0.9f);
        }
        return itemHeight;
    }
}
