package algonquin.cst2335.fourgadgets.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import algonquin.cst2335.fourgadgets.dao.BearRecordDAO;
import algonquin.cst2335.fourgadgets.entity.BearRecord;

/** Room database class for bear record
 * @author Victor Che
 * @version 1.0
 */
@Database(entities = {BearRecord.class}, version = 1)
public abstract class BearImageGeneratorDatabase extends RoomDatabase {
    public abstract BearRecordDAO brDAO();
}
