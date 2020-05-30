package com.openclassrooms.go4lunch.utils;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Class used to manage the recyclerView clicks
 */
public class ItemClickSupport {
    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;
    private int mItemID;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
        }
    };

    private ItemClickSupport(RecyclerView recyclerView, int itemID) {
        mRecyclerView = recyclerView;
        mItemID = itemID;
        mRecyclerView.setTag(itemID, this);
        RecyclerView.OnChildAttachStateChangeListener attachListener = new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if (mOnItemClickListener != null) {
                    view.setOnClickListener(mOnClickListener);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
            }
        };
        mRecyclerView.addOnChildAttachStateChangeListener(attachListener);
    }

    public static ItemClickSupport addTo(RecyclerView view, int itemID) {
        ItemClickSupport support = (ItemClickSupport) view.getTag(itemID);
        if (support == null) {
            support = new ItemClickSupport(view, itemID);
        }
        return support;
    }

    public ItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }

}