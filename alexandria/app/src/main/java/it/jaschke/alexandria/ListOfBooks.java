package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRvBooks;
    private BookListAdapter mBooksAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static final String[] BOOKS_COLUMNS = {
            AlexandriaContract.BookEntry._ID,
            AlexandriaContract.BookEntry.TITLE,
            AlexandriaContract.BookEntry.SUBTITLE,
            AlexandriaContract.BookEntry.IMAGE_URL
    };

    public static final int COL_BOOK_ID = 0;
    public static final int COL_BOOK_TITLE = 1;
    public static final int COL_BOOK_SUBTITLE = 2;
    public static final int COL_BOOK_COVER = 3;

    private static final int BOOKS_LOADER_ID = 10;

    private int position = ListView.INVALID_POSITION;
    //private EditText mEtSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        /*mEtSearch = (EditText) view.findViewById(R.id.searchText);
        view.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooks.this.restartLoader();
                    }
                }
        );*/

        mRvBooks = (RecyclerView) view.findViewById(R.id.rv_books);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRvBooks.setLayoutManager(mLayoutManager);

        mBooksAdapter = new BookListAdapter(getActivity());
        mRvBooks.setAdapter(mBooksAdapter);

        /*mRvBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback)getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });*/

        return view;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(BOOKS_LOADER_ID, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(BOOKS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        /*String searchString = mEtSearch.getText().toString();

        if (searchString.length() > 0) {

            searchString = "%"+searchString+"%";

            return new CursorLoader(getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    BOOKS_COLUMNS,
                    AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ",
                    new String[]{ searchString, searchString },
                    null
            );
        }*/

        return new CursorLoader(getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                BOOKS_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBooksAdapter.swapCursor(data);

        if (position != ListView.INVALID_POSITION) {
            mRvBooks.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBooksAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }
}
