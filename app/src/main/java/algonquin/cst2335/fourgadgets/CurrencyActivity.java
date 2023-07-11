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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.fourgadgets.dao.ConversionRecordDAO;
import algonquin.cst2335.fourgadgets.database.CurrencyConversionDatabase;
import algonquin.cst2335.fourgadgets.entity.ConversionRecord;
import algonquin.cst2335.fourgadgets.viewmodel.ConversionRecordViewModel;

/** Currency Activity to make currency conversions
 * @author Victor Che
 * @version 1.0
 */
public class CurrencyActivity extends AppCompatActivity {
    // global fields
    EditText input_money;
    RecyclerView rv_conversion;
    Button btn_clear_records;
    /** Adapter for conversion record */
    RecordAdapter adapter;
    final String[] currency_from = {null};
    final String[] currency_to = {null};
    /** List to hold conversion records */
    List<ConversionRecord> records = new ArrayList<>();
    /** View model which caches state to hold conversion records */
    ConversionRecordViewModel viewModel;
    /** Room database for conversion record */
    CurrencyConversionDatabase database;
    /** DAO to CRUD conversion record */
    ConversionRecordDAO crDAO;
    /** New thread to perform CRUD with database */
    Executor thread = Executors.newSingleThreadExecutor();
    /** Shared preferences to store data to local */
    SharedPreferences preferences;
    /** Shared preferences editor to store data to local */
    SharedPreferences.Editor editor;
    // on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);
        // toolbar setting
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // views
        input_money = findViewById(R.id.input_money);
        Spinner spinner_from = findViewById(R.id.spinner_from);
        Spinner spinner_to = findViewById(R.id.spinner_to);
        Button btn_convert = findViewById(R.id.btn_convert);
        ImageButton icon_swap = findViewById(R.id.icon_swap);
        btn_clear_records = findViewById(R.id.btn_clear_records);
        rv_conversion = findViewById(R.id.rv_conversion);
        adapter = new RecordAdapter();
        rv_conversion.setAdapter(adapter);
        rv_conversion.setLayoutManager(new LinearLayoutManager(this));
        // shared preferences
        preferences = getSharedPreferences("data_currency_conversion", Context.MODE_PRIVATE);
        String amountFrom = preferences.getString("AmountFrom", "0");
        input_money.setText(amountFrom);
        // get all records in database
        database = Room.databaseBuilder(this, CurrencyConversionDatabase.class, "database_currency_conversion").build();
        crDAO = database.crDAO();
        viewModel = new ViewModelProvider(this).get(ConversionRecordViewModel.class);
        records = viewModel.records.getValue();
        if (records == null) {
            viewModel.records.postValue((ArrayList<ConversionRecord>) (records = new ArrayList<>()));
            thread.execute(() -> {
                records.addAll(crDAO.getAll());
                if (records.size() > 0) {
                    btn_clear_records.setVisibility(View.VISIBLE);
                } else {
                    btn_clear_records.setVisibility(View.INVISIBLE);
                }
                runOnUiThread(() -> rv_conversion.setAdapter(adapter));
            });
        }
        // get currency list in json from http server
        String url_available_currencies = "https://currency-converter5.p.rapidapi.com/currency/list"
                + "?" + "rapidapi-key=******";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_available_currencies, null,
                (Response.Listener<JSONObject>) response -> {
                    try {
                        JSONObject currencies_json = response.getJSONObject("currencies");
                        // put json into string array
                        Iterator<String> keys = currencies_json.keys();
                        List<String> currencies_arr = new ArrayList<>();
                        while (keys.hasNext()) {
                            String k = keys.next();
                            String v = currencies_json.get(k).toString();
                            currencies_arr.add(k + ": " + v);
                        }
                        Collections.sort(currencies_arr);
                        // put string array into spinner
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies_arr);
                        runOnUiThread(() -> {
                            spinner_from.setAdapter(arrayAdapter);
                            spinner_to.setAdapter(arrayAdapter);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {});
        requestQueue.add(request);
        // select spinner_from to get currency_from
        spinner_from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency_from[0] = ((String) parent.getItemAtPosition(position)).substring(0,3);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        // select spinner_to to get currency_to
        spinner_to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency_to[0] = ((String) parent.getItemAtPosition(position)).substring(0,3);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        // get currency convert result in json
        btn_convert.setOnClickListener(click -> checkConversionResult());
        // swap currency_from and currency_to
        icon_swap.setOnClickListener(click -> {
            // swap values
            Toast.makeText(this, "2 Currencies are swapped", Toast.LENGTH_LONG).show();
            String temp = currency_from[0];
            currency_from[0] = currency_to[0];
            currency_to[0] = temp;
            checkConversionResult();
        });
        // click to clear input_money
        input_money.setOnClickListener(click -> input_money.setText(""));
        // click to clear all records
        btn_clear_records.setOnClickListener(click -> {
            thread.execute(() -> crDAO.clear());
            for (int i = records.size() - 1; i >= 0; i--) {
                adapter.notifyItemRemoved(i);
            }
            records.clear();
            btn_clear_records.setVisibility(View.INVISIBLE);
        });
    }
    /** Method to get conversion result from remote http server */
    private void checkConversionResult () {
        String money_from = input_money.getText().toString();
        if ("".equals(money_from)) {
            money_from = "1";
        }
        // shared preferences
        editor = preferences.edit();
        editor.putString("AmountFrom", money_from).apply();
        // get currency convert result in json from http server
        String url_currency_convertor = "https://currency-converter5.p.rapidapi.com/currency/convert"
                + "?" + "format=json"
                + "&" + "from=" + currency_from[0]
                + "&" + "to=" + currency_to[0]
                + "&" + "amount=" + money_from
                + "&" + "rapidapi-key=86411869admsh5dd5a3890b256fbp15db65jsnad2e2486246d";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_currency_convertor, null,
                (Response.Listener<JSONObject>) response -> {
                    try {
                        String currency_code_from = currency_from[0];
                        String currency_name_from = response.getString("base_currency_name");
                        String currency_code_to = currency_to[0];
                        String currency_name_to = response.getJSONObject("rates").getJSONObject(currency_to[0]).getString("currency_name");
                        String amount_from = response.getString("amount");
                        String amount_to = response.getJSONObject("rates").getJSONObject(currency_to[0]).getString("rate_for_amount");
                        String rate = response.getJSONObject("rates").getJSONObject(currency_to[0]).getString("rate");
                        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM/dd/yyyy hh:mm:ss", Locale.getDefault());
                        String time = format.format(new Date());
                        ConversionRecord insertedRecord = new ConversionRecord(currency_code_from, currency_name_from, currency_code_to, currency_name_to, amount_from, amount_to, rate, time);
                        thread.execute(() -> {
                            crDAO.insert(insertedRecord);
                            records.clear();
                            records.addAll(crDAO.getAll());
                        });
                        records.add(insertedRecord);
                        runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount() - 1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {});
        requestQueue.add(request);
        btn_clear_records.setVisibility(View.VISIBLE);
    }
    // create toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.currency_activity_actions, menu);
        return true;
    }
    // select toolbar menu item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // intent to each activity page
        Intent first = new Intent(CurrencyActivity.this, FlightActivity.class);
        Intent third = new Intent(CurrencyActivity.this, QuestionActivity.class);
        Intent fourth = new Intent(CurrencyActivity.this, BearActivity.class);
        Intent main = new Intent(CurrencyActivity.this, MainActivity.class);
        // intent actions
        if (item.getItemId() == R.id.icon_flight) {
            startActivity(first);
        } else if (item.getItemId() == R.id.icon_question) {
            startActivity(third);
        } else if (item.getItemId() == R.id.icon_bear) {
            startActivity(fourth);
        } else if (item.getItemId() == R.id.icon_back) {
            startActivity(main);
        } else if (item.getItemId() == R.id.icon_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CurrencyActivity.this);
            builder.setTitle("HELP: ")
                    .setMessage("1.Input a number\n2.Pick a currency (from)\n3.Pick a currency (to)\n" +
                            "4.Click CONVERT to make a conversion\n5.Click ARROWS between currencies to swap them")
                    .setPositiveButton("OK", (dialog, click) -> {}).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
    /** Inner class adapter for conversion records in recycler view */
    private class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecordViewHolder(getLayoutInflater().inflate(R.layout.conversion_record, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            ConversionRecord record = records.get(position);
            String record_text = record.getAmountFrom() + " " + record.getCurrencyCodeFrom()
                    + " = " + record.getAmountTo() + " " + record.getCurrencyCodeTo();
            holder.tv_record.setText(record_text);
            holder.tv_time.setText(record.getTime());
            holder.setPosition(position);
        }
        @Override
        public int getItemCount() {
            return records.size();
        }
    }
    /** Inner class view holder for views in conversion record */
    private class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tv_record;
        TextView tv_time;
        int position = -1;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_record = itemView.findViewById(R.id.tv_record);
            tv_time = itemView.findViewById(R.id.tv_time);
            // delete and undo actions
            itemView.setOnClickListener(click -> {
                int chosenPositon = getAdapterPosition();
                ConversionRecord chosenRecord = records.get(chosenPositon);
                RecordDetailFragment detailFragment = new RecordDetailFragment(chosenRecord, chosenPositon);
                getSupportFragmentManager().beginTransaction().replace(R.id.layout_currency, detailFragment).commit();
            });
        }
        public void setPosition(int position) {
            this.position = position;
        }
    }
    /** Inner class fragment for conversion record detail */
    public static class RecordDetailFragment extends Fragment {
        ConversionRecord chosenRecord;
        int chosenPosition;
        public RecordDetailFragment(ConversionRecord chosenRecord, int chosenPosition) {
            this.chosenRecord = chosenRecord;
            this.chosenPosition = chosenPosition;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View detailView = inflater.inflate(R.layout.conversion_record_detail, container, false);
            // views
            TextView tv_record = detailView.findViewById(R.id.tv_record);
            TextView tv_rate = detailView.findViewById(R.id.tv_rate);
            TextView tv_currency_from = detailView.findViewById(R.id.tv_currency_from);
            TextView tv_currency_to = detailView.findViewById(R.id.tv_currency_to);
            TextView tv_time = detailView.findViewById(R.id.tv_time);
            Button btn_delete = detailView.findViewById(R.id.btn_delete);
            Button btn_close = detailView.findViewById(R.id.btn_close);
            // strings
            String currency_code_from = chosenRecord.getCurrencyCodeFrom();
            String currency_name_from = chosenRecord.getCurrencyNameFrom();
            String currency_code_to = chosenRecord.getCurrencyCodeTo();
            String currency_name_to = chosenRecord.getCurrencyNameTo();
            String amount_from = chosenRecord.getAmountFrom();
            String amount_to = chosenRecord.getAmountTo();
            String rate = chosenRecord.getRate();
            String time = chosenRecord.getTime();
            // text setting
            tv_record.setText(String.format("%s %s = %s %s", amount_from, currency_code_from, amount_to, currency_code_to));
            tv_rate.setText(String.format("1.00 %s = %s %s", currency_code_from, rate, currency_code_to));
            tv_currency_from.setText(String.format("%s: %s", currency_code_from, currency_name_from));
            tv_currency_to.setText(String.format("%s: %s", currency_code_to, currency_name_to));
            tv_time.setText(String.format("search at %s", time));
            // delete
            btn_delete.setOnClickListener(click -> {
                CurrencyActivity currencyActivity = (CurrencyActivity) getContext();
                assert currencyActivity != null;
                currencyActivity.notifyParentItemRemoved(chosenRecord, chosenPosition);
                getParentFragmentManager().beginTransaction().remove(this).commit();
            });
            // close
            btn_close.setOnClickListener(click -> getParentFragmentManager().beginTransaction().remove(this).commit());
            return detailView;
        }
    }
    /** Method to notify activity that fragment's conversion record is removed */
    private void notifyParentItemRemoved(ConversionRecord chosenRecord, int chosenPosition) {
        String record_text = chosenRecord.getAmountFrom() + " " + chosenRecord.getCurrencyCodeFrom()
                + " = " + chosenRecord.getAmountTo() + " " + chosenRecord.getCurrencyCodeTo();
        AlertDialog.Builder builder = new AlertDialog.Builder(CurrencyActivity.this);
        builder.setTitle("ALERT: ")
                .setMessage("Delete record: " + record_text + " ?")
                .setPositiveButton("YES", (dialog, click1) -> {
                    thread.execute(() -> crDAO.delete(chosenRecord));
                    records.remove(chosenPosition);
                    adapter.notifyItemRemoved(chosenPosition);
                    if (records.size() == 0) {
                        btn_clear_records.setVisibility(View.INVISIBLE);
                    }
                    Snackbar.make(rv_conversion, "Record #" + chosenPosition + " deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", click11 -> {
                                btn_clear_records.setVisibility(View.VISIBLE);
                                thread.execute(() -> crDAO.insert(chosenRecord));
                                records.add(chosenPosition, chosenRecord);
                                adapter.notifyItemInserted(chosenPosition);

                            }).show();
                }).setNegativeButton("NO", (dialog, click2) -> {}).create().show();
    }
}
