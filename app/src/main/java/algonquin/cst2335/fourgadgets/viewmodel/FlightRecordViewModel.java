package algonquin.cst2335.fourgadgets.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import algonquin.cst2335.fourgadgets.entity.FlightRecord;

/** View model which caches state to hold flight records
 * @author Victor Che
 * @version 1.0
 */
public class FlightRecordViewModel extends ViewModel {
    public MutableLiveData<ArrayList<FlightRecord>> records = new MutableLiveData<>();
    public MutableLiveData<ArrayList<FlightRecord>> searchingResults = new MutableLiveData<>();
}
