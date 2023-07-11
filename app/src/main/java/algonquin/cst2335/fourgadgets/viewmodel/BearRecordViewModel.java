package algonquin.cst2335.fourgadgets.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import algonquin.cst2335.fourgadgets.entity.BearRecord;

/** View model which caches state to hold bear records
 * @author Victor Che
 * @version 1.0
 */
public class BearRecordViewModel extends ViewModel {
    public MutableLiveData<ArrayList<BearRecord>> records = new MutableLiveData<>();
}
