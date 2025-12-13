package jp.developer.bbee.pcassem.presentation.data;

import java.util.Locale;

public class ReviewItem {
    public String itemName;
    public int itemPrice;

    @Override
    public String toString() {
        return String.format(Locale.JAPAN, "%s(¥%,d)", itemName, itemPrice);
    }
}
