package algonquin.cst2335.fourgadgets.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import algonquin.cst2335.fourgadgets.entity.FlightRecord;

/** DAO to CRUD flight record
 * @author Victor Che
 * @version 1.0
 */
@Dao
public interface FlightRecordDAO {
    @Insert
    void insert(FlightRecord flightRecord);

    @Query("SELECT * FROM FlightRecord")
    List<FlightRecord> getAll();

    @Delete
    void delete(FlightRecord flightRecord);
}
