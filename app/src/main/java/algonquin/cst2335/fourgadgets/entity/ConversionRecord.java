package algonquin.cst2335.fourgadgets.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Entity class for conversion record
 * @author Victor Che
 * @version 1.0
 */
@Entity
public class ConversionRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Long id;
    @ColumnInfo(name = "currencyCodeFrom")
    String currencyCodeFrom;
    @ColumnInfo(name = "currencyNameFrom")
    String currencyNameFrom;
    @ColumnInfo(name = "currencyCodeTo")
    String currencyCodeTo;
    @ColumnInfo(name = "currencyNameTo")
    String currencyNameTo;
    @ColumnInfo(name = "amountFrom")
    String amountFrom;
    @ColumnInfo(name = "amountTo")
    String amountTo;
    @ColumnInfo(name = "rate")
    String rate;
    @ColumnInfo(name = "time")
    String time;
    public ConversionRecord(String currencyCodeFrom, String currencyNameFrom, String currencyCodeTo, String currencyNameTo, String amountFrom, String amountTo, String rate, String time) {
        this.currencyCodeFrom = currencyCodeFrom;
        this.currencyNameFrom = currencyNameFrom;
        this.currencyCodeTo = currencyCodeTo;
        this.currencyNameTo = currencyNameTo;
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        this.rate = rate;
        this.time = time;
    }
    public Long getId() {
        return id;
    }
    public String getCurrencyCodeFrom() {
        return currencyCodeFrom;
    }
    public String getCurrencyNameFrom() {
        return currencyNameFrom;
    }
    public String getCurrencyCodeTo() {
        return currencyCodeTo;
    }
    public String getCurrencyNameTo() {
        return currencyNameTo;
    }
    public String getAmountFrom() {
        return amountFrom;
    }
    public String getAmountTo() {
        return amountTo;
    }
    public String getRate() {
        return rate;
    }
    public String getTime() {
        return time;
    }
}
