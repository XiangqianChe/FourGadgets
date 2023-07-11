package algonquin.cst2335.fourgadgets;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/** Main Activity to invoke other activities
 * @author Victor Che
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // toolbar setting
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // intent to each activity page
        Intent first = new Intent(MainActivity.this, FlightActivity.class);
        Intent second = new Intent(MainActivity.this, CurrencyActivity.class);
        Intent third = new Intent(MainActivity.this, QuestionActivity.class);
        Intent fourth = new Intent(MainActivity.this, BearActivity.class);
        // views
        ImageButton imgbtn_flight = findViewById(R.id.imgbtn_flight);
        ImageButton imgbtn_currency = findViewById(R.id.imgbtn_currency);
        ImageButton imgbtn_question = findViewById(R.id.imgbtn_question);
        ImageButton imgbtn_bear = findViewById(R.id.imgbtn_bear);
        // intent actions
        imgbtn_flight.setOnClickListener(click -> startActivity(first));
        imgbtn_currency.setOnClickListener(click -> startActivity(second));
        imgbtn_question.setOnClickListener(click -> startActivity(third));
        imgbtn_bear.setOnClickListener(click -> startActivity(fourth));
    }
    // create toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_actions, menu);
        return true;
    }
    // select toolbar menu item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // intent to each activity page
        Intent first = new Intent(MainActivity.this, FlightActivity.class);
        Intent second = new Intent(MainActivity.this, CurrencyActivity.class);
        Intent third = new Intent(MainActivity.this, QuestionActivity.class);
        Intent fourth = new Intent(MainActivity.this, BearActivity.class);
        // intent actions
        if (item.getItemId() == R.id.icon_flight) {
            startActivity(first);
        } else if (item.getItemId() == R.id.icon_currency) {
            startActivity(second);
        } else if (item.getItemId() == R.id.icon_question) {
            startActivity(third);
        } else if (item.getItemId() == R.id.icon_bear) {
            startActivity(fourth);
        }
        return super.onOptionsItemSelected(item);
    }
}