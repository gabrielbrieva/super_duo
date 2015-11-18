package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.Utils;
import it.jaschke.alexandria.data.AlexandriaContract;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    private final String ITEMS = "items";
    private final String VOLUME_INFO = "volumeInfo";
    private final String TITLE = "title";
    private final String SUBTITLE = "subtitle";
    private final String AUTHORS = "authors";
    private final String DESC = "description";
    private final String CATEGORIES = "categories";
    private final String IMG_URL_PATH = "imageLinks";
    private  final String IMG_URL = "thumbnail";

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                fetchBook(ean);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if (ean != null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(final String ean) {

        if (ean.length() != 13) {
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (bookEntry.getCount() > 0) {
            // book is already on local database
            bookEntry.close();
            return;
        }

        bookEntry.close();

        if (!Utils.isNetworkConnected(this.getApplicationContext())) {
            // without internet connection
            // TODO: notify user about connection status error
            return;
        }

        // using retrofit to make http request to Google Book API

        String isbn = "isbn:" + ean;
        GoogleBookServiceFactory.createService().getBook(isbn, new Callback<JsonObject>() {
            @Override
            public void success(JsonObject bookJson, Response response) {

                if (bookJson != null || !bookJson.isJsonNull()) {
                    try {

                        JsonArray bookArray;
                        if (bookJson.has(ITEMS)) {
                            bookArray = bookJson.getAsJsonArray(ITEMS);
                        } else {
                            requestFeedback(getString(R.string.not_found));
                            return;
                        }

                        JsonObject bookInfo = ((JsonObject) bookArray.get(0)).getAsJsonObject(VOLUME_INFO);

                        String title = bookInfo.get(TITLE).getAsString();

                        String subtitle = "";
                        if(bookInfo.has(SUBTITLE)) {
                            subtitle = bookInfo.get(SUBTITLE).getAsString();
                        }

                        String desc="";
                        if(bookInfo.has(DESC)){
                            desc = bookInfo.get(DESC).getAsString();
                        }

                        String imgUrl = "";
                        if(bookInfo.has(IMG_URL_PATH) && bookInfo.getAsJsonObject(IMG_URL_PATH).has(IMG_URL)) {
                            imgUrl = bookInfo.getAsJsonObject(IMG_URL_PATH).get(IMG_URL).getAsString();
                        }

                        writeBackBook(ean, title, subtitle, desc, imgUrl);

                        if(bookInfo.has(AUTHORS)) {
                            writeBackAuthors(ean, bookInfo.getAsJsonArray(AUTHORS));
                        }
                        if(bookInfo.has(CATEGORIES)){
                            writeBackCategories(ean, bookInfo.getAsJsonArray(CATEGORIES) );
                        }

                    } catch (Exception e) {
                        requestFeedback(getString(R.string.request_error));
                        Log.e(LOG_TAG, "Error ", e);
                    }
                } else {
                    // empty result
                    requestFeedback(getString(R.string.not_found));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                requestFeedback(getString(R.string.not_found));
            }
        });
    }

    private void requestFeedback(String message) {
        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
        messageIntent.putExtra(MainActivity.MESSAGE_KEY, message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values = new ContentValues();

        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);

        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(String ean, JsonArray jsonArray) {
        ContentValues values = new ContentValues();

        for (int i = 0; i < jsonArray.size(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.get(i).getAsString());

            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    private void writeBackCategories(String ean, JsonArray jsonArray) {
        ContentValues values = new ContentValues();

        for (int i = 0; i < jsonArray.size(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.get(i).getAsString());

            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }
 }