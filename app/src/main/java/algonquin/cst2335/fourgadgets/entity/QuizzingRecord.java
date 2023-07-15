package algonquin.cst2335.fourgadgets.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Entity class for quizzing record
 * @author Victor Che
 * @version 1.0
 */
@Entity
public class QuizzingRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Long id;
    @ColumnInfo(name = "quizzer")
    String quizzer;
    @ColumnInfo(name = "score")
    int score;
    @ColumnInfo(name = "submitTime")
    String submitTime;
    public QuizzingRecord(String quizzer, int score, String submitTime) {
        this.quizzer = quizzer;
        this.score = score;
        this.submitTime = submitTime;
    }
    public Long getId() {
        return id;
    }
    public String getQuizzer() {
        return quizzer;
    }
    public int getScore() {
        return score;
    }
    public String getSubmitTime() {
        return submitTime;
    }
}
