package barqsoft.footballscores.service;

import com.google.gson.JsonObject;

import barqsoft.footballscores.Utilies;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public class FootballAPIServiceFactory {

    public static FootballAPIService createService(final String apiKey) {
        // Using retrofit library we create a RestAdapter
        // based on FootballAPIService Interface.
        return new RestAdapter.Builder()
                .setEndpoint(Utilies.URL_END_POINT)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        // We expect a json response from API
                        request.addHeader("Accept", "application/json");
                        // we add API Key value as header parameter
                        request.addHeader("X-Auth-Token", apiKey);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL) // only by debugging purpose
                .build()
                .create(FootballAPIService.class);
    }

    public interface FootballAPIService {
        @GET("/soccerseasons/{leagueId}/fixtures")
        JsonObject fixtures(@Path("leagueId") int leagueId, @Query("timeFrame") String timeFrame);
    }

}
