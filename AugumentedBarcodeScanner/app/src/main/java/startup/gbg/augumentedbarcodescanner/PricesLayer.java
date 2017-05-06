package startup.gbg.augumentedbarcodescanner;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.LinkedList;

/**
 * Created by simonarneson on 2017-05-06.
 */

public class PricesLayer extends LinearLayout {

    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView priceList;
    private PriceListAdapter adapter;

    public PricesLayer(Context context) {
        super(context);
        initViews(context);
    }

    public PricesLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public PricesLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.prices_layer, this);

        priceList = (RecyclerView) findViewById(R.id.list);

        layoutManager = new LinearLayoutManager(getContext());
        priceList.setLayoutManager(layoutManager);

        adapter = new PriceListAdapter(context);
        priceList.setAdapter(adapter);
    }

    public void setPrices(LinkedList<PriceData> prices) {
        adapter.setPrices(prices);
    }
}


