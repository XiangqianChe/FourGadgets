package algonquin.cst2335.fourgadgets.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import algonquin.cst2335.fourgadgets.entity.ConversionRecord;

/** DAO to CRUD conversion record
 * @author Victor Che
 * @version 1.0
 */
@Dao
public interface ConversionRecordDAO {
    @Insert
    void insert(ConversionRecord conversionRecord);

    @Query("SELECT * FROM ConversionRecord")
    List<ConversionRecord> getAll();

    @Delete
    void delete(ConversionRecord conversionRecord);

    @Query("DELETE FROM ConversionRecord")
    void clear();
}
