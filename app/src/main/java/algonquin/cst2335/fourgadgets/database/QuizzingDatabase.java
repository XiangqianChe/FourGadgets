package algonquin.cst2335.fourgadgets.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import algonquin.cst2335.fourgadgets.dao.QuizzingRecordDAO;
import algonquin.cst2335.fourgadgets.entity.QuizzingRecord;

/** Room database class for quizzing record
 * @author Victor Che
 * @version 1.0
 */
@Database(entities = {QuizzingRecord.class}, version = 1)
public abstract class QuizzingDatabase extends RoomDatabase {
    public abstract QuizzingRecordDAO qrDAO();
}
