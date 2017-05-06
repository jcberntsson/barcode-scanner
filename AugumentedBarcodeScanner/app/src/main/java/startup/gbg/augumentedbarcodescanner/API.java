package startup.gbg.augumentedbarcodescanner;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Bohn on 2017-05-06.
 */

public interface API {

    @GET("product/GTIN")
    Call<LinkedList<Product>> listByGTIN(@Query("q") String GTIN);
}