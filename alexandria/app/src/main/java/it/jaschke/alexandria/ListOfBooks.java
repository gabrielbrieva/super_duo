package it.jaschke.alexandria;

import android.app.Activity;
import android.app.SearchManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    private final String SEARCH_KEY = "BOOK_SEARCH_KEY";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        mRvBooks = (RecyclerView) view.findViewById(R.id.rv_books);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRvBooks.setLayoutManager(mLayoutManager);

        mBooksAdapter = new BookListAdapter(getActivity(), new BookListAdapter.BookListAdapterOnClickHandler() {
            @Override
            public void onClick(String ean, BookListAdapter.ViewHolder vh) {
                ((Callback)getActivity()).onItemSelected(ean);
            }
        });

        mRvBooks.setAdapter(mBooksAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        // Get the SearchView and set the searchable configuration
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && searchView.getQuery().length() == 0) {
                    searchItem.collapseActionView();
                }
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        restartLoader(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        restartLoader(query);
        return true;
    }

    private void restartLoader(String query) {

        Bundle args = null;

        if (query != null && query.trim().length() > 0) {
            args = new Bundle();
            args.putString(SEARCH_KEY, query);
        }

        getLoaderManager().restartLoader(BOOKS_LOADER_ID, args, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(BOOKS_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (args != null) {

            String searchString = args.getString(SEARCH_KEY);

            if (searchString != null && searchString.trim().length() > 0) {

                searchString = "%" + searchString.trim() + "%";

                return new CursorLoader(getActivity(),
                        AlexandriaContract.BookEntry.CONTENT_URI,
                        BOOKS_COLUMNS,
                        AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ",
                        new String[]{searchString, searchString},
                        null
                );
            }
        }

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
