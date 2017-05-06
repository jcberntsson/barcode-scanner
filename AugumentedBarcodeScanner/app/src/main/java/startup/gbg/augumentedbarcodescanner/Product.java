package startup.gbg.augumentedbarcodescanner;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;

/**
 * Created by Bohn on 2017-05-06.
 */



public class Product {

    public String id;

    public String gtin;

    public String name ;

    public String name2 ;

    public String manufacturer;

    public UnitType unit;

    public double numberOfItems ;

    public int amountOfUnits;

    public double incrementValue ;

    public String dataText;

    public LinkedList<String> categories;
}
