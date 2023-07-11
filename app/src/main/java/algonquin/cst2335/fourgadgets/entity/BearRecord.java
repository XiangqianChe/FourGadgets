package algonquin.cst2335.fourgadgets.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Entity class for bear record
 * @author Victor Che
 * @version 1.0
 */
@Entity
public class BearRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Long id;
    @ColumnInfo(name = "bearImgData")
    byte[] bearImgData;
    @ColumnInfo(name = "imgName")
    String imgName;
    @ColumnInfo(name = "imgWidth")
    int imgWidth;
    @ColumnInfo(name = "imgHeight")
    int imgHeight;
    @ColumnInfo(name = "time")
    String time;
    public BearRecord(byte[] bearImgData, String imgName, int imgWidth, int imgHeight, String time) {
        this.bearImgData = bearImgData;
        this.imgName = imgName;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.time = time;
    }
    public Long getId() {
        return id;
    }
    public byte[] getBearImgData() {
        return bearImgData;
    }
    public String getImgName() {
        return imgName;
    }
    public int getImgWidth() {
        return imgWidth;
    }
    public int getImgHeight() {
        return imgHeight;
    }
    public String getTime() {
        return time;
    }
}
