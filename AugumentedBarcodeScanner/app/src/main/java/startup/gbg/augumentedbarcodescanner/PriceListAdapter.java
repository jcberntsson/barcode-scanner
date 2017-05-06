package startup.gbg.augumentedbarcodescanner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

/**
 * Created by simonarneson on 2017-05-06.
 */

class PriceListAdapter extends RecyclerView.Adapter<PriceListAdapter.ViewHolder> {
    private List<PriceData> dataset;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView storeName;
        public TextView price;

        public ViewHolder(View v) {
            super(v);
            storeName = (TextView) v.findViewById(R.id.storeName);
            price = (TextView) v.findViewById(R.id.price);

        }
    }

    public PriceListAdapter(Context context) {
        this.context = context;

        dataset = new SortedList<>(new PriceListComparator());
    }

    public void setPrices(List<PriceData> prices) {
        dataset.clear();
        for (PriceData price : prices) {
            dataset.add(price);
        }
        notifyDataSetChanged();
    }

    @Override
    public PriceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_price_list, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PriceData price = dataset.get(position);

        holder.storeName.setText(price.store.chain.name());
        holder.price.setText(price.price.amount + "");
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    private static class PriceListComparator implements Comparator<PriceData> {
        @Override
        public int compare(PriceData price, PriceData p1) {
            return Long.compare(p1.price.amount, price.price.amount);
        }
    }
}
