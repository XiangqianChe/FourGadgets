package algonquin.cst2335.fourgadgets.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import algonquin.cst2335.fourgadgets.dao.ConversionRecordDAO;
import algonquin.cst2335.fourgadgets.entity.ConversionRecord;
/** Room database class for conversion record
 * @author Victor Che
 * @version 1.0
 */
@Database(entities = {ConversionRecord.class}, version = 1)
public abstract class CurrencyConversionDatabase extends RoomDatabase {
    public abstract ConversionRecordDAO crDAO();
}
