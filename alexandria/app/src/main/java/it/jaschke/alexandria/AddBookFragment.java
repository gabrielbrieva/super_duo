package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.picasso.PicassoBigCache;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.ui.FragmentBase;
import it.jaschke.alexandria.ui.FragmentKeys;
import it.jaschke.alexandria.ui.FragmentOrchestrator;


public class AddBookFragment extends FragmentBase implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    public static final String EAN_KEY = "EAN_KEY";
    private EditText mEtEan;
    private TextInputLayout mTilEan;
    private String mEanValue = "";
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";

    private Picasso mPicasso;

    public AddBookFragment() {
        super();
        setArguments(new Bundle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EAN_CONTENT, mEanValue);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getArguments().putString(AddBookFragment.EAN_KEY, mEtEan.getText().toString());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initToolbar(R.string.title_scan, true);

        mPicasso = PicassoBigCache.INSTANCE.getPicassoBigCache(getActivity());

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        mTilEan = (TextInputLayout) rootView.findViewById(R.id.eantextinputlayou);
        mTilEan.setErrorEnabled(true);

        mEtEan = (EditText) rootView.findViewById(R.id.ean);

        if (savedInstanceState != null && savedInstanceState.getString(EAN_CONTENT) != null) {
            mEanValue = toIsbn13(savedInstanceState.getString(EAN_CONTENT));
        } else {
            Bundle arguments = getArguments();
            if (arguments != null && arguments.getString(AddBookFragment.EAN_KEY) != null) {
                mEanValue = toIsbn13(arguments.getString(AddBookFragment.EAN_KEY));
            }
        }

        mEtEan.addTextChangedListener(this);

        mEtEan.setText(mEanValue);

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentOrchestrator) getActivity()).loadFragment(FragmentKeys.BARCODE_SCANNER, null, FragmentKeys.ADD);
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEtEan.setText("");
                restartLoader();
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mEtEan.getText() != null && mEtEan.getText().length() > 0) {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, mEtEan.getText().toString());
                    bookIntent.setAction(BookService.DELETE_BOOK);
                    getActivity().startService(bookIntent);
                }

                mEtEan.setText("");
                restartLoader();
            }
        });

        return rootView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("ADD_BOOK", "ISBN Text Editor: " + s.toString());

        mEanValue = toIsbn13(s.toString());

        if (s.toString().length() == 13) {
            startBookSearch(mEanValue);
        }
    }

    private String toIsbn13(String barcode) {

        mTilEan.setErrorEnabled(false);
        mTilEan.setError(null);

        if (barcode == null)
            barcode = "";

        if (barcode.length() > 0) {

            String b = barcode.toString();

            //catch isbn10 numbers

            if (b.length() == 10 && !b.startsWith("978")) {
                b = "978" + b;
            }

            if (b.length() == 13 && isNumeric(b)) {
                return b;
            }

            mTilEan.setErrorEnabled(true);
            mTilEan.setError("Invalid Format: Must ISBN-13 Format");

        }

        clearFields();

        return barcode;
    }

    private void startBookSearch(String ean) {
        //Once we have an ISBN, start a book intent
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);

        // TODO: is required ?? cursor must notified ¬¬
        AddBookFragment.this.restartLoader();
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mEtEan.getText().length()==0){
            return null;
        }
        String eanStr= mEtEan.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader != null && data != null && !data.moveToFirst()) {

            // TODO show empty result

            clearFields();
        } else {

            // TODO show book detail and validate null values before to use...

            String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
            ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

            String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
            ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

            String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
            String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
            if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                mPicasso.load(imgUrl).into((ImageView) rootView.findViewById(R.id.bookCover));
                rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
            }

            String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
            ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

            rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void clearFields() {
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }

        return true;
    }
}
