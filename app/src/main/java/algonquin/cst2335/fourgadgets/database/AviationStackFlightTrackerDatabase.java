package algonquin.cst2335.fourgadgets.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import algonquin.cst2335.fourgadgets.dao.FlightRecordDAO;
import algonquin.cst2335.fourgadgets.entity.FlightRecord;

/** Room database class for flight record
 * @author Victor Che
 * @version 1.0
 */
@Database(entities = {FlightRecord.class}, version = 1)
public abstract class AviationStackFlightTrackerDatabase extends RoomDatabase {
    public abstract FlightRecordDAO frDAO();
}
