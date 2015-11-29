package it.jaschke.alexandria.services;

import com.google.gson.JsonObject;

import it.jaschke.alexandria.Utils;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Factory to create an instance of GoogleBookService
 */
public class GoogleBookServiceFactory {

    /**
     * Creator of GoogleBookService instance.
     *
     * @return instance of GoogleBookService
     */
    public static GoogleBookService createService()
    {
        // Using retrofit library we create a RestAdapter
        // based on GoogleBookService Interface.
        return new RestAdapter.Builder()
                .setEndpoint(Utils.GOOGLEBOOK_ENDPOINT)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        // We expect a json response from Google Book API
                        request.addHeader("Accept", "application/json");
                    }
                })
                //.setLogLevel(RestAdapter.LogLevel.FULL) // only by debugging purpose
                .build()
                .create(GoogleBookService.class);
    }

    /**
     * Interface to wrap the google book services we will use in the application
     */
    public interface GoogleBookService {
        @GET("/volumes") // get significant result
        void getBook(@Query("q") String query, Callback<JsonObject> callback);
    }

}
