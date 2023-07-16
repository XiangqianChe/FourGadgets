package algonquin.cst2335.fourgadgets;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.fourgadgets.dao.FlightRecordDAO;
import algonquin.cst2335.fourgadgets.database.AviationStackFlightTrackerDatabase;
import algonquin.cst2335.fourgadgets.entity.FlightRecord;
import algonquin.cst2335.fourgadgets.viewmodel.FlightRecordViewModel;

/** Flight Activity to track aviation stack flights
 * @author Victor Che
 * @version 1.0
 */
public class FlightActivity extends AppCompatActivity {
    // global fields
    EditText et_airportcode;
    RecyclerView rv_searchedflights;
    RecyclerView rv_savedflights;
    /** Adapter for flight record */
    RecordAdapter adapter;
    SearchingResultAdapter adapterSearching;
    /** List to hold flight records */
    List<FlightRecord> records = new ArrayList<>();
    List<FlightRecord> searchingResults = new ArrayList<>();
    /** View model which caches state to hold flight records */
    FlightRecordViewModel viewModel;
    /** Room database for flight records */
    AviationStackFlightTrackerDatabase database;
    /** DAO to CRUD flight record */
    FlightRecordDAO frDAO;
    /** New thread to perform CRUD with database */
    static Executor thread = Executors.newSingleThreadExecutor();
    /** Shared preferences to store data to local */
    SharedPreferences preferences;
    /** Shared preferences editor to store data to local */
    SharedPreferences.Editor editor;
    // on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);
        // toolbar setting
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // views
        et_airportcode = findViewById(R.id.et_airportcode);
        Button btn_searchflights = findViewById(R.id.btn_searchflights);
        Button btn_savedflights = findViewById(R.id.btn_savedflights);
        adapter = new RecordAdapter();
        rv_savedflights = findViewById(R.id.rv_savedflights);
        rv_savedflights.setAdapter(adapter);
        rv_savedflights.setLayoutManager(new LinearLayoutManager(this));
        adapterSearching = new SearchingResultAdapter();
        rv_searchedflights = findViewById(R.id.rv_searchedflights);
        rv_searchedflights.setAdapter(adapterSearching);
        rv_searchedflights.setLayoutManager(new LinearLayoutManager(this));
        // shared preferences
        preferences = getSharedPreferences("data_airport_stack_flight_tracker", Context.MODE_PRIVATE);
        String airportCode = preferences.getString("AirportCode", "YOW");
        et_airportcode.setText(airportCode);
        // get all records in database
        database = Room.databaseBuilder(this, AviationStackFlightTrackerDatabase.class, "database_airport_stack_flight_tracker").build();
        frDAO = database.frDAO();
        btn_savedflights.setOnClickListener(click -> {
            viewModel = new ViewModelProvider(this).get(FlightRecordViewModel.class);
            records = viewModel.records.getValue();
            if (records == null) {
                viewModel.records.postValue((ArrayList<FlightRecord>) (records = new ArrayList<>()));
            }
            thread.execute(() -> {
                records.addAll(frDAO.getAll());
                runOnUiThread(() -> rv_savedflights.setAdapter(adapter));
            });
        });
        // get flight search result in json
        btn_searchflights.setOnClickListener(click -> {
            // shared preferences
            String airportCode1 = et_airportcode.getText().toString();
            editor = preferences.edit();
            editor.putString("AirportCode", airportCode1).apply();
            // get flight search result in json from http server
            String url_airport_stack_flight_tracker = "http://api.aviationstack.com/v1/flights?access_key=144b72dbdc1e8f20f8346ea2f5f6fa0c&limit=10&dep_iata=" + airportCode1;
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_airport_stack_flight_tracker, null,
                    response -> {
                        try {
                            viewModel = new ViewModelProvider(this).get(FlightRecordViewModel.class);
                            searchingResults = viewModel.searchingResults.getValue();
                            if (searchingResults == null) {
                                viewModel.searchingResults.postValue((ArrayList<FlightRecord>) (searchingResults = new ArrayList<>()));
                            }
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject flight = (JSONObject)(data.get(i));
                                String destination = flight.getJSONObject("arrival").getString("iata");
                                String flightDate = flight.getString("flight_date");
                                String flightNumber = flight.getJSONObject("flight").getString("number");
                                String terminal = flight.getJSONObject("departure").getString("terminal");
                                String gate = flight.getJSONObject("departure").getString("gate");
                                String delay = flight.getJSONObject("departure").getString("delay");
                                FlightRecord insertedSearchingResult = new FlightRecord(airportCode1, destination, flightDate, flightNumber, terminal, gate, delay);
                                searchingResults.add(insertedSearchingResult);
                                runOnUiThread(() -> adapterSearching.notifyItemInserted(adapterSearching.getItemCount() - 1));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {});
            requestQueue.add(request);
        });
        // click to clear et_airportcode
        et_airportcode.setOnClickListener(click -> et_airportcode.setText(""));
    }
    // create toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.flight_activity_actions, menu);
        return true;
    }
    // select toolbar menu item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // intent to each activity page
        Intent second = new Intent(this, CurrencyActivity.class);
        Intent third = new Intent(this, QuestionActivity.class);
        Intent fourth = new Intent(this, BearActivity.class);
        Intent main = new Intent(this, MainActivity.class);
        // intent actions
        if (item.getItemId() == R.id.icon_currency) {
            startActivity(second);
        } else if (item.getItemId() == R.id.icon_question) {
            startActivity(third);
        } else if (item.getItemId() == R.id.icon_bear) {
            startActivity(fourth);
        } else if (item.getItemId() == R.id.icon_back) {
            startActivity(main);
        } else if (item.getItemId() == R.id.icon_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("HELP: ")
                    .setMessage("1.Input an airport code like YOW\n2.Click Search FLIGHTS to get available flights in a list\n3.Click each flight to check detail and save any\n" +
                            "4.Click Saved Flights to get saved flights in a list\n5.Click each flight to check detail and delete any")
                    .setPositiveButton("OK", (dialog, click) -> {}).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
    /** Inner class adapter for flight records in recycler view */
    private class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecordViewHolder(getLayoutInflater().inflate(R.layout.flight_record, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            FlightRecord record = records.get(position);
            String record_text = record.getAirportCode() + " - " + record.getDestination()
                    + " (" + record.getFlightNumber() + ") on " + record.getFlightDate();
            holder.tv_record.setText(record_text);
            holder.setPosition(position);
        }
        @Override
        public int getItemCount() {
            return records.size();
        }
    }
    /** Inner class adapter for flight searching results in recycler view */
    private class SearchingResultAdapter extends RecyclerView.Adapter<SearchingResultViewHolder> {
        @NonNull
        @Override
        public SearchingResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SearchingResultViewHolder(getLayoutInflater().inflate(R.layout.flight_record, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull SearchingResultViewHolder holder, int position) {
            FlightRecord searchingResult = searchingResults.get(position);
            String record_text = searchingResult.getAirportCode() + " - " + searchingResult.getDestination()
                    + " (" + searchingResult.getFlightNumber() + ") on " + searchingResult.getFlightDate();
            holder.tv_record.setText(record_text);
            holder.setPosition(position);
        }
        @Override
        public int getItemCount() {
            return searchingResults.size();
        }
    }
    /** Inner class view holder for views in flight record */
    private class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tv_record;
        int position = -1;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_record = itemView.findViewById(R.id.tv_record);
            // delete and undo actions
            itemView.setOnClickListener(click -> {
                int chosenPosition = getAdapterPosition();
                FlightRecord chosenRecord = records.get(chosenPosition);
                RecordDetailFragment detailFragment = new RecordDetailFragment(chosenRecord, chosenPosition);
                getSupportFragmentManager().beginTransaction().replace(R.id.layout_flight, detailFragment).commit();
            });
        }
        public void setPosition(int position) {
            this.position = position;
        }
    }
    /** Inner class view holder for views in flight searching result */
    private class SearchingResultViewHolder extends RecyclerView.ViewHolder {
        TextView tv_record;
        int position = -1;
        public SearchingResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_record = itemView.findViewById(R.id.tv_record);
            // save actions
            itemView.setOnClickListener(click -> {
                int chosenPosition = getAdapterPosition();
                FlightRecord chosenSearchingResult = searchingResults.get(chosenPosition);
                SearchingResultDetailFragment detailFragment = new SearchingResultDetailFragment(chosenSearchingResult, chosenPosition);
                getSupportFragmentManager().beginTransaction().replace(R.id.layout_flight, detailFragment).commit();
            });
        }
        public void setPosition(int position) {
            this.position = position;
        }
    }
    /** Inner class fragment for flight record detail */
    public static class RecordDetailFragment extends Fragment {
        FlightRecord chosenRecord;
        int chosenPosition;
        public RecordDetailFragment(FlightRecord chosenRecord, int chosenPosition) {
            this.chosenRecord = chosenRecord;
            this.chosenPosition = chosenPosition;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View detailView = inflater.inflate(R.layout.saved_flight_record_detail, container, false);
            // views
            TextView tv_airportcode = detailView.findViewById(R.id.tv_airportcode);
            TextView tv_destination = detailView.findViewById(R.id.tv_destination);
            TextView tv_flightdate = detailView.findViewById(R.id.tv_flightdate);
            TextView tv_flightnumber = detailView.findViewById(R.id.tv_flightnumber);
            TextView tv_terminal = detailView.findViewById(R.id.tv_terminal);
            TextView tv_gate = detailView.findViewById(R.id.tv_gate);
            TextView tv_delay = detailView.findViewById(R.id.tv_delay);
            Button btn_delete = detailView.findViewById(R.id.btn_delete);
            Button btn_close = detailView.findViewById(R.id.btn_close);
            // text setting
            tv_airportcode.setText(String.format("Airport Code: %s", chosenRecord.getAirportCode()));
            tv_destination.setText(String.format("Destination: %s", chosenRecord.getDestination()));
            tv_flightdate.setText(String.format("Flight Date: %s", chosenRecord.getFlightDate()));
            tv_flightnumber.setText(String.format("Flight Number: %s", chosenRecord.getFlightNumber()));
            tv_terminal.setText(String.format("Terminal: %s", chosenRecord.getTerminal()));
            tv_gate.setText(String.format("Gate: %s", chosenRecord.getGate()));
            tv_delay.setText(String.format("Delay: %s", chosenRecord.getDelay()));
            // delete
            btn_delete.setOnClickListener(click -> {
                FlightActivity flightActivity = (FlightActivity) getContext();
                assert flightActivity != null;
                flightActivity.notifyParentItemRemoved(chosenRecord, chosenPosition);
                getParentFragmentManager().beginTransaction().remove(this).commit();
            });
            // close
            btn_close.setOnClickListener(click -> getParentFragmentManager().beginTransaction().remove(this).commit());
            return detailView;
        }
    }
    /** Method to notify activity that fragment's flight record is removed */
    private void notifyParentItemRemoved(FlightRecord chosenRecord, int chosenPosition) {
        String record_text = chosenRecord.getAirportCode() + " - " + chosenRecord.getDestination()
                + " (" + chosenRecord.getFlightNumber() + ") on " + chosenRecord.getFlightDate();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ALERT: ")
                .setMessage("Delete record: " + record_text + " ?")
                .setPositiveButton("YES", (dialog, click1) -> {
                    thread.execute(() -> frDAO.delete(chosenRecord));
                    records.remove(chosenPosition);
                    adapter.notifyItemRemoved(chosenPosition);
                    Snackbar.make(rv_savedflights, "Record #" + chosenPosition + " deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", click11 -> {
                                thread.execute(() -> frDAO.insert(chosenRecord));
                                records.add(chosenPosition, chosenRecord);
                                adapter.notifyItemInserted(chosenPosition);
                            }).show();
                }).setNegativeButton("NO", (dialog, click2) -> {}).create().show();
    }
    /** Inner class fragment for flight searching result detail */
    public static class SearchingResultDetailFragment extends Fragment {
        FlightRecord chosenSearchingResult;
        int chosenPosition;
        public SearchingResultDetailFragment(FlightRecord chosenSearchingResult, int chosenPosition) {
            this.chosenSearchingResult = chosenSearchingResult;
            this.chosenPosition = chosenPosition;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View detailView = inflater.inflate(R.layout.searched_flight_record_detail, container, false);
            // views
            TextView tv_airportcode = detailView.findViewById(R.id.tv_airportcode);
            TextView tv_destination = detailView.findViewById(R.id.tv_destination);
            TextView tv_flightdate = detailView.findViewById(R.id.tv_flightdate);
            TextView tv_flightnumber = detailView.findViewById(R.id.tv_flightnumber);
            TextView tv_terminal = detailView.findViewById(R.id.tv_terminal);
            TextView tv_gate = detailView.findViewById(R.id.tv_gate);
            TextView tv_delay = detailView.findViewById(R.id.tv_delay);
            Button btn_save = detailView.findViewById(R.id.btn_save);
            Button btn_close = detailView.findViewById(R.id.btn_close);
            // text setting
            tv_airportcode.setText(String.format("Airport Code: %s", chosenSearchingResult.getAirportCode()));
            tv_destination.setText(String.format("Destination: %s", chosenSearchingResult.getDestination()));
            tv_flightdate.setText(String.format("Flight Date: %s", chosenSearchingResult.getFlightDate()));
            tv_flightnumber.setText(String.format("Flight Number: %s", chosenSearchingResult.getFlightNumber()));
            tv_terminal.setText(String.format("Terminal: %s", chosenSearchingResult.getTerminal()));
            tv_gate.setText(String.format("Gate: %s", chosenSearchingResult.getGate()));
            tv_delay.setText(String.format("Delay: %s", chosenSearchingResult.getDelay()));
            // save
            btn_save.setOnClickListener(click -> {
                FlightActivity flightActivity = (FlightActivity) getContext();
                assert flightActivity != null;
                thread.execute(() -> flightActivity.frDAO.insert(chosenSearchingResult));
                getParentFragmentManager().beginTransaction().remove(this).commit();
                Toast.makeText(getContext(), "You saved a searching result into database. Click to check it.", Toast.LENGTH_LONG).show();
            });
            // close
            btn_close.setOnClickListener(click -> getParentFragmentManager().beginTransaction().remove(this).commit());
            return detailView;
        }
    }
}