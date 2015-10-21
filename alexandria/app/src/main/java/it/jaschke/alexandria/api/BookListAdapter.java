package it.jaschke.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.ListOfBooks;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.services.DownloadImage;

/**
 * Created by saj on 11/01/15.
 *
 * Transformed to RecyclerView.Adapter with Cursor Support by Gabriel
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private BookListAdapterOnClickHandler mClickHandler;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIvCover;
        public final TextView mTvTitle;
        public final TextView mTvSubTitle;

        public ViewHolder(View view) {
            super(view);

            mIvCover = (ImageView) view.findViewById(R.id.fullBookCover);
            mTvTitle = (TextView) view.findViewById(R.id.listBookTitle);
            mTvSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getString(ListOfBooks.COL_BOOK_ID), this);
        }
    }

    public static interface BookListAdapterOnClickHandler {
        void onClick(String ean, ViewHolder vh);
    }

    public BookListAdapter(Context context, BookListAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_list_item, viewGroup, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        mCursor.moveToPosition(position);

        // TODO: change by picasso library
        new DownloadImage(vh.mIvCover).execute(mCursor.getString(ListOfBooks.COL_BOOK_COVER));
        vh.mTvTitle.setText(mCursor.getString(ListOfBooks.COL_BOOK_TITLE));
        vh.mTvSubTitle.setText(mCursor.getString(ListOfBooks.COL_BOOK_SUBTITLE));
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
