package algonquin.cst2335.fourgadgets;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.fourgadgets.dao.BearRecordDAO;
import algonquin.cst2335.fourgadgets.database.BearImageGeneratorDatabase;
import algonquin.cst2335.fourgadgets.entity.BearRecord;
import algonquin.cst2335.fourgadgets.viewmodel.BearRecordViewModel;

/** Bear Activity to generate bear images
 * @author Victor Che
 * @version 1.0
 */
public class BearActivity extends AppCompatActivity {
    // global fields
    EditText et_bear_width;
    EditText et_bear_height;
    RecyclerView rv_bear;
    /** Adapter for bear record */
    RecordAdapter adapter;
    /** List to hold bear records */
    List<BearRecord> records = new ArrayList<>();
    /** View model which caches state to hold bear records */
    BearRecordViewModel viewModel;
    /** Room database for bear record */
    BearImageGeneratorDatabase database;
    /** DAO to CRUD bear record */
    BearRecordDAO brDAO;
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
        setContentView(R.layout.activity_bear);
        // toolbar setting
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // views
        et_bear_width = findViewById(R.id.et_bear_width);
        et_bear_height = findViewById(R.id.et_bear_height);
        Button btn_bear = findViewById(R.id.btn_bear);
        rv_bear = findViewById(R.id.rv_bear);
        adapter = new RecordAdapter();
        rv_bear.setAdapter(adapter);
        rv_bear.setLayoutManager(new GridLayoutManager(this, 5));
        // shared preferences
        preferences = getSharedPreferences("data_bear_image_generator", Context.MODE_PRIVATE);
        String stored_width = preferences.getString("Width", "100");
        String stored_height = preferences.getString("Height", "100");
        et_bear_width.setText(stored_width);
        et_bear_height.setText(stored_height);
        // get all records in database
        database = Room.databaseBuilder(this, BearImageGeneratorDatabase.class, "database_bear_image_generator").build();
        brDAO = database.brDAO();
        viewModel = new ViewModelProvider(this).get(BearRecordViewModel.class);
        records = viewModel.records.getValue();
        if (records == null) {
            viewModel.records.postValue((ArrayList<BearRecord>) (records = new ArrayList<>()));
            thread.execute(() -> {
                records.addAll(brDAO.getAll());
                runOnUiThread(() -> rv_bear.setAdapter(adapter));
            });
        }
        // get bear image generation result
        btn_bear.setOnClickListener(click -> {
            String input_width = "".equals(et_bear_width.getText().toString()) ? "100" : et_bear_width.getText().toString();
            String input_height = "".equals(et_bear_height.getText().toString()) ? "100" : et_bear_height.getText().toString();
            // shared preferences
            editor = preferences.edit();
            editor.putString("Width", input_width).putString("Height", input_height).apply();
            // get bear image generation result from http server
            String url_bear_image_generator = "https://placebear.com/" + input_width +"/" + input_height;
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            ImageRequest request = new ImageRequest(url_bear_image_generator,
                    response -> {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(response.getByteCount());
                        response.copyPixelsToBuffer(byteBuffer);
                        byte[] bearImgData = byteBuffer.array();
                        String imgName = "BEAR_IMG_" + input_width + "x" + input_height;
                        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM/dd/yyyy hh:mm:ss", Locale.getDefault());
                        String time = format.format(new Date());
                        BearRecord insertedRecord = new BearRecord(bearImgData, imgName, Integer.parseInt(input_width), Integer.parseInt(input_height), time);
                        thread.execute(() -> {
                            brDAO.insert(insertedRecord);
                            records.clear();
                            records.addAll(brDAO.getAll());
                        });
                        runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount() - 1));
                    }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565,
                    error -> {}
            );
            requestQueue.add(request);
            Toast.makeText(this, "You just generated a new BEAR image.\nTry modify Width and Height for more!", Toast.LENGTH_LONG).show();
        });
        // click to clear fields
        et_bear_width.setOnClickListener(click -> et_bear_width.setText(""));
        et_bear_height.setOnClickListener(click -> et_bear_height.setText(""));
    }

    // create toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.bear_activity_actions, menu);
        return true;
    }
    // select toolbar menu item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // intent to each activity page
        Intent first = new Intent(BearActivity.this, FlightActivity.class);
        Intent second = new Intent(BearActivity.this, CurrencyActivity.class);
        Intent third = new Intent(BearActivity.this, QuestionActivity.class);
        Intent main = new Intent(BearActivity.this, MainActivity.class);
        // intent actions
        if (item.getItemId() == R.id.icon_flight) {
            startActivity(first);
        } else if (item.getItemId() == R.id.icon_currency) {
            startActivity(second);
        } else if (item.getItemId() == R.id.icon_question) {
            startActivity(third);
        } else if (item.getItemId() == R.id.icon_back) {
            startActivity(main);
        } else if (item.getItemId() == R.id.icon_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BearActivity.this);
            builder.setTitle("HELP: ")
                    .setMessage("1.Input a width\n2.Input a height\n3.Click BEAR! to generate a bear image")
                    .setPositiveButton("OK", (dialog, click) -> {}).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
    /** Inner class adapter for bear records in recycler view */
    private class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecordViewHolder(getLayoutInflater().inflate(R.layout.bear_record, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            BearRecord record = records.get(position);
            Bitmap bearBitmap = Bitmap.createBitmap(record.getImgWidth(), record.getImgHeight(), Bitmap.Config.RGB_565);
            bearBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(record.getBearImgData()));
            holder.imgview_record.setImageBitmap(bearBitmap);
            holder.tv_time.setText(record.getTime().substring(4, 16));
            holder.setPosition(position);
        }
        @Override
        public int getItemCount() {
            return records.size();
        }
    }
    /** Inner class view holder for views in bear record */
    private class RecordViewHolder extends RecyclerView.ViewHolder {
        ImageView imgview_record;
        TextView tv_time;
        int position = -1;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            imgview_record = itemView.findViewById(R.id.imgview_record);
            tv_time = itemView.findViewById(R.id.tv_time);
            // delete and undo actions
            itemView.setOnClickListener(click -> {
                int chosenPosition = getAdapterPosition();
                BearRecord chosenRecord = records.get(chosenPosition);
                RecordDetailFragment detailFragment = new RecordDetailFragment(chosenRecord, chosenPosition);
                getSupportFragmentManager().beginTransaction().replace(R.id.layout_bear, detailFragment).commit();
            });
        }
        public void setPosition(int position) {
            this.position = position;
        }
    }
    /** Inner class fragment for bear record detail */
    public static class RecordDetailFragment extends Fragment {
        BearRecord chosenRecord;
        int chosenPosition;
        public RecordDetailFragment(BearRecord chosenRecord, int chosenPosition) {
            this.chosenRecord = chosenRecord;
            this.chosenPosition = chosenPosition;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View detailView = inflater.inflate(R.layout.bear_record_detail, container, false);
            // views
            ImageView imgview_bear = detailView.findViewById(R.id.imgview_bear);
            TextView tv_imgname = detailView.findViewById(R.id.tv_imgname);
            TextView tv_imgwidth = detailView.findViewById(R.id.tv_imgwidth);
            TextView tv_imgheight = detailView.findViewById(R.id.tv_imgheight);
            TextView tv_time = detailView.findViewById(R.id.tv_time);
            Button btn_delete = detailView.findViewById(R.id.btn_delete);
            Button btn_close = detailView.findViewById(R.id.btn_close);
            // fields
            String imgName = chosenRecord.getImgName();
            int imgWidth = chosenRecord.getImgWidth();
            int imgHeight = chosenRecord.getImgHeight();
            String time = chosenRecord.getTime();
            // text setting
            Bitmap bearBitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.RGB_565);
            bearBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(chosenRecord.getBearImgData()));
            imgview_bear.setImageBitmap(bearBitmap);
            tv_imgname.setText(imgName);
            tv_imgwidth.setText(String.format(Locale.getDefault(), "WIDTH: %s", imgWidth));
            tv_imgheight.setText(String.format(Locale.getDefault(),"HEIGHT: %d", imgHeight));
            tv_time.setText(String.format("search at %s", time));
            // delete
            btn_delete.setOnClickListener(click -> {
                BearActivity bearActivity = (BearActivity) getContext();
                assert bearActivity != null;
                bearActivity.notifyParentItemRemoved(chosenRecord, chosenPosition);
                getParentFragmentManager().beginTransaction().remove(this).commit();
            });
            // close
            btn_close.setOnClickListener(click -> getParentFragmentManager().beginTransaction().remove(this).commit());
            return detailView;
        }
    }
    /** Method to notify activity that fragment's bear record is removed */
    private void notifyParentItemRemoved(BearRecord chosenRecord, int chosenPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BearActivity.this);
        builder.setTitle("ALERT: ")
                .setMessage("Delete record: " + chosenRecord.getImgName() + " ?")
                .setPositiveButton("YES", (dialog, click1) -> {
                    thread.execute(() -> brDAO.delete(chosenRecord));
                    records.remove(chosenPosition);
                    adapter.notifyItemRemoved(chosenPosition);
                    Snackbar.make(rv_bear, "Record #" + chosenPosition + " deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", click11 -> {
                                thread.execute(() -> brDAO.insert(chosenRecord));
                                records.add(chosenPosition, chosenRecord);
                                adapter.notifyItemInserted(chosenPosition);
                            }).show();
                }).setNegativeButton("NO", (dialog, click2) -> {}).create().show();
    }
}