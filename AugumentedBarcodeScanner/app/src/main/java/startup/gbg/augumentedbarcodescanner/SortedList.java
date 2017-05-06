package startup.gbg.augumentedbarcodescanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by simonarneson on 2017-05-06.
 */

public class SortedList<E> extends ArrayList<E> {
    private Comparator<E> sortingComparator;

    public SortedList(Comparator<E> sortingComparator) {
        this.sortingComparator = sortingComparator;
    }

    @Override
    public boolean add(E e) {
        if (contains(e)) {
            remove(e);
        }
        Boolean res = super.add(e);
        Collections.sort(this, sortingComparator);
        return res;
    }

}
