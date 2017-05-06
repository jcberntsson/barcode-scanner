package startup.gbg.augumentedbarcodescanner;

import java.util.LinkedList;

/**
 * Created by Bohn on 2017-05-06.
 */



public class Product {

    private String Id;

    private String GTIN;

    private String Name ;

    private String Name2 ;

    private String Manufacturer;

    private UnitType Unit;

    private double NumberOfItems ;

    private int AmountOfUnits;

    private double IncrementValue ;

    private String DataText;

    private LinkedList<String> Categories;

    public String getId() {
        return Id;
    }

    public String getGTIN() {
        return GTIN;
    }

    public String getName() {
        return Name;
    }

    public String getName2() {
        return Name2;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public UnitType getUnit() {
        return Unit;
    }

    public double getNumberOfItems() {
        return NumberOfItems;
    }

    public int getAmountOfUnits() {
        return AmountOfUnits;
    }

    public double getIncrementValue() {
        return IncrementValue;
    }

    public String getDataText() {
        return DataText;
    }

    public LinkedList<String> getCategories() {
        return Categories;
    }
}
