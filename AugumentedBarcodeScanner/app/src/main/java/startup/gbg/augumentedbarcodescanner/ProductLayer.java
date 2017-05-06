package startup.gbg.augumentedbarcodescanner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    ScoreView economicScoreView;
    TextView productTitle;

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

        healthScoreView = (ScoreView) findViewById(R.id.healthScore);
        healthScoreView.setScoreType(HEALTH);
        environmentScoreView = (ScoreView) findViewById(R.id.environmentScore);
        environmentScoreView.setScoreType(ENVIRONMENT);

        socialScoreView = (ScoreView) findViewById(R.id.socialScore);
        socialScoreView.setScoreType(SOCIAL);

        economicScoreView = (ScoreView) findViewById(R.id.economicScore);

        economicScoreView.setScoreType(ECONOMY);

        productTitle = (TextView) findViewById(R.id.productTitle);

    }

    public void setProduct(Product product) {
        healthScoreView.setScore(product.gtin.substring(12,13));
        environmentScoreView.setScore(product.gtin.substring(11,12));
        socialScoreView.setScore(product.gtin.substring(10,11));
        economicScoreView.setScore(product.gtin.substring(9,10
        ));
        productTitle.setText(product.name);
    }
}
