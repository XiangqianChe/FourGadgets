package algonquin.cst2335.fourgadgets.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import algonquin.cst2335.fourgadgets.entity.QuizzingRecord;

/** View model which caches state to hold quizzing records
 * @author Victor Che
 * @version 1.0
 */
public class QuizzingRecordViewModel extends ViewModel {
    public MutableLiveData<ArrayList<QuizzingRecord>> records = new MutableLiveData<>();
}
