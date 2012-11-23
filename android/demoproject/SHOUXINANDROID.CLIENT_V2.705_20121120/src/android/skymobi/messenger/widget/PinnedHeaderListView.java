/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.skymobi.messenger.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.skymobi.messenger.R;
import android.skymobi.messenger.adapter.ContactsBaseAdapter.ContactsListItem;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A ListView that maintains a header pinned at the top of the list. The pinned
 * header can be pushed up and dissolved as needed.
 */
public class PinnedHeaderListView extends ListView {

    /**
     * Adapter interface. The list adapter must implement this interface.
     */
    public interface PinnedHeaderAdapter {

        /**
         * Pinned header state: don't show the header.
         */
        public static final int PINNED_HEADER_GONE = 0;

        /**
         * Pinned header state: show the header at the top of the list.
         */
        public static final int PINNED_HEADER_VISIBLE = 1;

        /**
         * Pinned header state: show the header. If the header extends beyond
         * the bottom of the first shown element, push it up and clip.
         */
        public static final int PINNED_HEADER_PUSHED_UP = 2;

        /**
         * Configures the pinned header view to match the first visible list
         * item.
         * 
         * @param header pinned header view.
         * @param position position of the first visible list item.
         * @param alpha fading of the header view, between 0 and 255.
         */
        void configurePinnedHeader(View header, int position);

    }

    private PinnedHeaderAdapter mAdapter;
    private View mHeaderView;
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;
    private int mHeaderViewHeight;

    public PinnedHeaderListView(Context context) {
        super(context);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPinnedHeaderView(View headerView) {
        mHeaderView = headerView;

        // Disable vertical fading when the pinned header is present
        // change ListView to allow separate measures for top and bottom
        // fading edge;
        // in this particular case we would like to disable the top, but not the
        // bottom edge.
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }

        requestLayout();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (PinnedHeaderAdapter) adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisiblePosition());
        }
    }

    public void configureHeaderView(int position) {
        if (mHeaderView == null) {
            return;
        }

        if (position == 0) {
            mHeaderViewVisible = false;
            return;
        }
        int state = getScollState(position);

        switch (state) {
            case PinnedHeaderAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }

            case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE: {
                mAdapter.configurePinnedHeader(mHeaderView, position);
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            }

            case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP: {
                View firstView = getChildAt(0);
                if (firstView == null) {
                    return;
                }
                int bottom = firstView.getBottom();
                int headerHeight = mHeaderView.getHeight();
                int y;
                if (bottom < headerHeight) {
                    y = (bottom - headerHeight);
                } else {
                    y = 0;
                }
                int headViewCount = getHeaderViewsCount();
                mAdapter.configurePinnedHeader(mHeaderView, position - headViewCount);
                if (mHeaderView.getTop() != y) {
                    mHeaderView.layout(0, y, mHeaderViewWidth,
                            mHeaderViewHeight + y);
                }
                mHeaderViewVisible = true;
                break;
            }
        }
    }

    public int getScollState(int firstVisibleItem) {
        View firstView = getChildAt(0);
        int headViewCount = getHeaderViewsCount();
        if (firstView != null && firstVisibleItem > (headViewCount - 1)) {
            ContactsListItem item = null;
            if (getItemAtPosition(firstVisibleItem) instanceof ContactsListItem) {
                item = (ContactsListItem) getItemAtPosition(firstVisibleItem);
            }
            if (null != item && item.isGroup()) {
                return PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;
            } else {
                ContactsListItem nextItem = null;
                if (getItemAtPosition(firstVisibleItem + 1) instanceof ContactsListItem) {
                    nextItem = (ContactsListItem) getItemAtPosition(firstVisibleItem + 1);
                }
                if (nextItem != null && nextItem.isGroup()) {
                    return PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP;
                } else {
                    return PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;
                }
            }
        } else {
            return PinnedHeaderAdapter.PINNED_HEADER_GONE;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = this.findViewById(R.id.contacts_list_item_search);
        if (null != view) {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.dispatchTouchEvent(ev);
    }

}
