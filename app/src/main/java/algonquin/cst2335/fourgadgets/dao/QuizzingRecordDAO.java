package algonquin.cst2335.fourgadgets.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import algonquin.cst2335.fourgadgets.entity.QuizzingRecord;

/** DAO to CRUD quizzing record
 * @author Victor Che
 * @version 1.0
 */
@Dao
public interface QuizzingRecordDAO {
    @Insert
    void insert(QuizzingRecord quizzingRecord);

    @Query("SELECT * FROM QuizzingRecord")
    List<QuizzingRecord> getAll();
}
