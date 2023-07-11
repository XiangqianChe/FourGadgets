package algonquin.cst2335.fourgadgets.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import algonquin.cst2335.fourgadgets.entity.ConversionRecord;

/** View model which caches state to hold conversion records
 * @author Victor Che
 * @version 1.0
 */
public class ConversionRecordViewModel extends ViewModel {
    public MutableLiveData<ArrayList<ConversionRecord>> records = new MutableLiveData<>();
}
