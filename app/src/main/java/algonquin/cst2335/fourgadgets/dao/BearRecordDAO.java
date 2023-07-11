package algonquin.cst2335.fourgadgets.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import algonquin.cst2335.fourgadgets.entity.BearRecord;

/** DAO to CRUD bear record
 * @author Victor Che
 * @version 1.0
 */
@Dao
public interface BearRecordDAO {
    @Insert
    void insert(BearRecord bearRecord);

    @Query("SELECT * FROM BearRecord")
    List<BearRecord> getAll();

    @Delete
    void delete(BearRecord bearRecord);
}
