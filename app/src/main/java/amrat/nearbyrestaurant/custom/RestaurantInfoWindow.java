package amrat.nearbyrestaurant.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import amrat.nearbyrestaurant.R;
import amrat.nearbyrestaurant.api.model.RestaurantListResponse;
import amrat.nearbyrestaurant.api.model.Result;

public class RestaurantInfoWindow implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private LayoutInflater inflater;
    private RestaurantListResponse restaurantListResponse;

    public RestaurantInfoWindow(Context context, RestaurantListResponse restaurantListResponse) {
        this.context = context;
        this.restaurantListResponse = restaurantListResponse;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.restaurant_info_window, null);

        ImageView ivRestaurantIcon = (ImageView) v.findViewById(R.id.restaurant_icon);
        TextView tvRestaurantName = (TextView) v.findViewById(R.id.restaurant_name);
        TextView tvRestaurantAddress = (TextView) v.findViewById(R.id.restaurant_address);

        Result result = restaurantListResponse.getResults().get(Integer.parseInt(marker.getTitle()));

            Glide.with(context)
                    .load(result.getIcon())
                    .into(ivRestaurantIcon);

        tvRestaurantName.setText(result.getName());
        tvRestaurantAddress.setText(result.getVicinity());

        return v;
    }
}