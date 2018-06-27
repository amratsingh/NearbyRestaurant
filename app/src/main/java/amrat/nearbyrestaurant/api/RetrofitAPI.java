package amrat.nearbyrestaurant.api;

import amrat.nearbyrestaurant.api.model.RestaurantListResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RetrofitAPI {

    int CONNECT_TIMEOUT = 60000;
    int READ_TIMEOUT = 60000;
    int WRITE_TIMEOUT = 60000;

    String BASE_URL = "https://maps.googleapis.com/maps/api/place/";

    @GET("nearbysearch/json")
    Call<RestaurantListResponse> nearPlaceListAPI(@Query("location") String location, @Query("radius") String radius, @Query("type") String type, @Query("key") String key);
}