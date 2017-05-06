package startup.gbg.augumentedbarcodescanner;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static startup.gbg.augumentedbarcodescanner.ScoreType.ECONOMY;
import static startup.gbg.augumentedbarcodescanner.ScoreType.ENVIRONMENT;
import static startup.gbg.augumentedbarcodescanner.ScoreType.HEALTH;
import static startup.gbg.augumentedbarcodescanner.ScoreType.SOCIAL;

/**
 * Created by simonarneson on 2017-05-06.
 */

public class ProductLayer extends RelativeLayout {
    ScoreView healthScoreView;
    ScoreView environmentScoreView;
    ScoreView socialScoreView;
    ImageView economicScoreView;
    TextView productTitle;
    PricesLayer pricesLayer;
    private API backendService;
    private Product product;


    public ProductLayer(Context context) {
        super(context);
        initViews(context);
    }

    public ProductLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public ProductLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.product_layer, this);

        pricesLayer =(PricesLayer) findViewById(R.id.pricesLayer);

        healthScoreView = (ScoreView) findViewById(R.id.healthScore);
        healthScoreView.setScoreType(HEALTH);
        environmentScoreView = (ScoreView) findViewById(R.id.environmentScore);
        environmentScoreView.setScoreType(ENVIRONMENT);

        socialScoreView = (ScoreView) findViewById(R.id.socialScore);
        socialScoreView.setScoreType(SOCIAL);

        economicScoreView = (ImageView) findViewById(R.id.economicScore);


        productTitle = (TextView) findViewById(R.id.productTitle);

    }

    public void setProduct(Product product) {
        this.product = product;
        healthScoreView.setScore(product.gtin.substring(12,13));
        environmentScoreView.setScore(product.gtin.substring(11,12));
        socialScoreView.setScore(product.gtin.substring(10,11));
        productTitle.setText(product.name);
    }

    public void setBackendService(API service) {
        backendService = service;
    }

    private void getPricesForProduct(final Product p, final Callback<LinkedList<PriceData>> callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<LinkedList<PriceData>> priceCall =  backendService.getPrices(p.id);
                try {
                    Response<LinkedList<PriceData>> res = priceCall.execute();
                    callback.onResponse(priceCall,res);
                    Log.d("PRODUCT_LAYER", "Hittade : "+res.body().size() + " produkter yooo!");
                }catch (IOException e){
                    Log.d("PRODUCT_LAYER", "N[got gick fel yoo: "+e.getMessage());
                }
            }
        }).start();
    }

    public void hidePricesLayer() {
        pricesLayer.setVisibility(GONE);
    }

    public void togglePrices() {
        if(pricesLayer.getVisibility() == VISIBLE) {
            pricesLayer.setVisibility(GONE);
            return;
        }

        if(product == null || backendService == null) {
            return;
        }
        getPricesForProduct(product, new Callback<LinkedList<PriceData>>() {
            @Override
            public void onResponse(Call<LinkedList<PriceData>> call, final Response<LinkedList<PriceData>> response) {
                pricesLayer.post(new Runnable() {
                    @Override
                    public void run() {
                        LinkedList<PriceData> prices = response.body();
                        pricesLayer.setVisibility(VISIBLE);
                        pricesLayer.setPrices(prices);
                    }
                });
            }

            @Override
            public void onFailure(Call<LinkedList<PriceData>> call, Throwable t) {

            }
        });
    }
}
