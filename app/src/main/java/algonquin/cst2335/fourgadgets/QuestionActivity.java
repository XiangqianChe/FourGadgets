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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import algonquin.cst2335.fourgadgets.dao.QuizzingRecordDAO;
import algonquin.cst2335.fourgadgets.database.QuizzingDatabase;
import algonquin.cst2335.fourgadgets.entity.QuizzingRecord;
import algonquin.cst2335.fourgadgets.viewmodel.QuizzingRecordViewModel;

/** Question Activity to do quizzes from trivia question database
 * @author Victor Che
 * @version 1.0
 */
public class QuestionActivity extends AppCompatActivity {

    // global fields
    RecyclerView rv_question;
    /** Adapter for quiz */
    QuizAdapter adapter;
    /** List to hold quizzes */
    List<Quiz> quizzes = new ArrayList<>();
    /** List to hold top 10 quizzing records */
    List<QuizzingRecord> top10;
    /** View model which caches state to hold quizzing records */
    QuizzingRecordViewModel viewModel;
    /** Room database for quizzing record */
    QuizzingDatabase database;
    /** DAO to CRUD quizzing record */
    static QuizzingRecordDAO qrDAO;
    /** New thread to perform CRUD with database */
    static Executor thread = Executors.newSingleThreadExecutor();
    /** Shared preferences to store data to local */
    static SharedPreferences preferences;
    /** Shared preferences editor to store data to local */
    static SharedPreferences.Editor editor;
    /** Strings in preferences */
    static String quizzer;
    // on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        // toolbar setting
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // views
        Spinner spinner_question_categories = findViewById(R.id.spinner_question_categories);
        Button btn_quiz = findViewById(R.id.btn_quiz);
        Button btn_top10 = findViewById(R.id.btn_top10);
        rv_question = findViewById(R.id.rv_question);
        adapter = new QuizAdapter();
        rv_question.setAdapter(adapter);
        rv_question.setLayoutManager(new LinearLayoutManager(this));
        // shared preferences
        preferences = getSharedPreferences("data_quizzing", Context.MODE_PRIVATE);
        quizzer = preferences.getString("Quizzer", "Victor Che");
        // initialize database
        database = Room.databaseBuilder(this, QuizzingDatabase.class, "database_quizzing").build();
        qrDAO = database.qrDAO();
        viewModel = new ViewModelProvider(this).get(QuizzingRecordViewModel.class);
        // get all categories for spinner
        String[] question_categories = getResources().getStringArray(R.array.qustion_categories);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, question_categories);
        spinner_question_categories.setAdapter(arrayAdapter);
        // select spinner_question_categories to get question_category
        final String[] question_category = {null};
        spinner_question_categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                question_category[0] = ((String) parent.getItemAtPosition(position));
                question_category[0] = question_category[0].substring(0, question_category[0].indexOf('.'));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        // get trivia question database result in json from http server
        btn_quiz.setOnClickListener(click -> {
            String url_trivia_question_database = "https://opentdb.com/api.php?amount=10&type=multiple&category=" +  question_category[0];
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_trivia_question_database, null,
                    response -> {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            List<QA> QAs = new ArrayList<>();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                String category = result.getString("category");
                                String question = result.getString("question");
                                String correct_answer = result.getString("correct_answer");
                                JSONArray incorrect_answers = result.getJSONArray("incorrect_answers");
                                List<String> answers = new ArrayList<>();
                                for (int j = 0; j < incorrect_answers.length(); j++) {
                                    answers.add(incorrect_answers.getString(j));
                                }
                                answers.add(correct_answer);
                                Collections.sort(answers);
                                QAs.add(new QA(category, question, correct_answer, answers));
                            }
                            String quizName = "Quiz_" + "_" + UUID.randomUUID().toString().substring(0, 8);
                            SimpleDateFormat format = new SimpleDateFormat("EEE, MMM/dd/yyyy hh:mm:ss", Locale.getDefault());
                            String quizTime = format.format(new Date());
                            Quiz quiz = new Quiz(quizName, QAs, quizTime);
                            quizzes.add(quiz);
                            runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount() - 1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {});
            requestQueue.add(request);
        });
        // get top 10 records in database
        btn_top10.setOnClickListener(click -> thread.execute(() -> {
            List<QuizzingRecord> all = qrDAO.getAll();
            top10 = all.stream().sorted((r1, r2) -> r2.getScore() - r1.getScore()).limit(10).collect(Collectors.toList());
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(QuestionActivity.this);
                builder.setTitle("TOP 10 RECORDS: ");
                if (top10 != null) {
                    StringBuilder message = new StringBuilder();
                    for (int i = 0; i < Math.min(top10.size(), 10); i++) {
                        message.append(i + 1).append(". ").append(top10.get(i).getQuizzer()).append(": ").append(top10.get(i).getScore()).append("\n");
                    }
                    builder.setMessage(message.toString());
                } else {
                    builder.setMessage("No records now. Create yours.");
                }
                builder.setPositiveButton("OK", (dialog, click1) -> {}).create().show();
            });
        }));
    }
    // create toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.question_activity_actions, menu);
        return true;
    }
    // select toolbar menu item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // intent to each activity page
        Intent first = new Intent(QuestionActivity.this, FlightActivity.class);
        Intent second = new Intent(QuestionActivity.this, CurrencyActivity.class);
        Intent fourth = new Intent(QuestionActivity.this, BearActivity.class);
        Intent main = new Intent(QuestionActivity.this, MainActivity.class);
        // intent actions
        if (item.getItemId() == R.id.icon_flight) {
            startActivity(first);
        } else if (item.getItemId() == R.id.icon_currency) {
            startActivity(second);
        } else if (item.getItemId() == R.id.icon_bear) {
            startActivity(fourth);
        } else if (item.getItemId() == R.id.icon_back) {
            startActivity(main);
        } else if (item.getItemId() == R.id.icon_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(QuestionActivity.this);
            builder.setTitle("HELP: ")
                    .setMessage("1.Choose a category and generate a new quiz, r pick a quiz in list\n2.Click to open the quiz\n3.Answer 10 questions\n" +
                            "4.Click SUBMIT to get your score\n5.Click Top 10 Records to see if you are in")
                    .setPositiveButton("OK", (dialog, click) -> {}).create().show();
        }
        return super.onOptionsItemSelected(item);
    }
    /** Inner class adapter for quizzes in recycler view */
    private class QuizAdapter extends RecyclerView.Adapter<QuizViewHolder> {
        @NonNull
        @Override
        public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new QuizViewHolder(getLayoutInflater().inflate(R.layout.quiz, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
            Quiz quiz = quizzes.get(position);
            holder.tv_quiz_name.setText(quiz.getQuizName());
            holder.tv_quiz_time.setText(quiz.getQuizTime());
            holder.setPosition(position);
        }
        @Override
        public int getItemCount() {
            return quizzes.size();
        }
    }
    /** Inner class view holder for views in quiz */
    private class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tv_quiz_name;
        TextView tv_quiz_time;
        int position = -1;
        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_quiz_name = itemView.findViewById(R.id.tv_quiz_name);
            tv_quiz_time = itemView.findViewById(R.id.tv_quiz_time);
            // delete and submit actions
            itemView.setOnClickListener(click -> {
                int chosenPosition = getAdapterPosition();
                Quiz chosenQuiz = quizzes.get(chosenPosition);
                QuizDetailFragment detailFragment = new QuizDetailFragment(chosenQuiz, chosenPosition);
                getSupportFragmentManager().beginTransaction().replace(R.id.layout_question, detailFragment).commit();
            });
        }
        public void setPosition(int position) {
            this.position = position;
        }
    }
    /** Inner class fragment for quiz detail */
    public static class QuizDetailFragment extends Fragment {
        Quiz chosenQuiz;
        int chosenPosition;
        public QuizDetailFragment(Quiz chosenQuiz, int chosenPosition) {
            this.chosenQuiz = chosenQuiz;
            this.chosenPosition = chosenPosition;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View detailView = inflater.inflate(R.layout.quiz_detail, container, false);
            // views
            TextView tv_quiz_name = detailView.findViewById(R.id.tv_quiz_name);
            EditText et_quizzer = detailView.findViewById(R.id.et_quizzer);
            TextView tv_quiz_time = detailView.findViewById(R.id.tv_quiz_time);
            TextView tv_question01 = detailView.findViewById(R.id.tv_question01);
            RadioGroup rg_choices01 = detailView.findViewById(R.id.rg_choices01);
            RadioButton rb_choice01_A = detailView.findViewById(R.id.rb_choice01_A);
            RadioButton rb_choice01_B = detailView.findViewById(R.id.rb_choice01_B);
            RadioButton rb_choice01_C = detailView.findViewById(R.id.rb_choice01_C);
            RadioButton rb_choice01_D = detailView.findViewById(R.id.rb_choice01_D);
            TextView tv_question02 = detailView.findViewById(R.id.tv_question02);
            RadioGroup rg_choices02 = detailView.findViewById(R.id.rg_choices02);
            RadioButton rb_choice02_A = detailView.findViewById(R.id.rb_choice02_A);
            RadioButton rb_choice02_B = detailView.findViewById(R.id.rb_choice02_B);
            RadioButton rb_choice02_C = detailView.findViewById(R.id.rb_choice02_C);
            RadioButton rb_choice02_D = detailView.findViewById(R.id.rb_choice02_D);
            TextView tv_question03 = detailView.findViewById(R.id.tv_question03);
            RadioGroup rg_choices03 = detailView.findViewById(R.id.rg_choices03);
            RadioButton rb_choice03_A = detailView.findViewById(R.id.rb_choice03_A);
            RadioButton rb_choice03_B = detailView.findViewById(R.id.rb_choice03_B);
            RadioButton rb_choice03_C = detailView.findViewById(R.id.rb_choice03_C);
            RadioButton rb_choice03_D = detailView.findViewById(R.id.rb_choice03_D);
            TextView tv_question04 = detailView.findViewById(R.id.tv_question04);
            RadioGroup rg_choices04 = detailView.findViewById(R.id.rg_choices04);
            RadioButton rb_choice04_A = detailView.findViewById(R.id.rb_choice04_A);
            RadioButton rb_choice04_B = detailView.findViewById(R.id.rb_choice04_B);
            RadioButton rb_choice04_C = detailView.findViewById(R.id.rb_choice04_C);
            RadioButton rb_choice04_D = detailView.findViewById(R.id.rb_choice04_D);
            TextView tv_question05 = detailView.findViewById(R.id.tv_question05);
            RadioGroup rg_choices05 = detailView.findViewById(R.id.rg_choices05);
            RadioButton rb_choice05_A = detailView.findViewById(R.id.rb_choice05_A);
            RadioButton rb_choice05_B = detailView.findViewById(R.id.rb_choice05_B);
            RadioButton rb_choice05_C = detailView.findViewById(R.id.rb_choice05_C);
            RadioButton rb_choice05_D = detailView.findViewById(R.id.rb_choice05_D);
            TextView tv_question06 = detailView.findViewById(R.id.tv_question06);
            RadioGroup rg_choices06 = detailView.findViewById(R.id.rg_choices06);
            RadioButton rb_choice06_A = detailView.findViewById(R.id.rb_choice06_A);
            RadioButton rb_choice06_B = detailView.findViewById(R.id.rb_choice06_B);
            RadioButton rb_choice06_C = detailView.findViewById(R.id.rb_choice06_C);
            RadioButton rb_choice06_D = detailView.findViewById(R.id.rb_choice06_D);
            TextView tv_question07 = detailView.findViewById(R.id.tv_question07);
            RadioGroup rg_choices07 = detailView.findViewById(R.id.rg_choices07);
            RadioButton rb_choice07_A = detailView.findViewById(R.id.rb_choice07_A);
            RadioButton rb_choice07_B = detailView.findViewById(R.id.rb_choice07_B);
            RadioButton rb_choice07_C = detailView.findViewById(R.id.rb_choice07_C);
            RadioButton rb_choice07_D = detailView.findViewById(R.id.rb_choice07_D);
            TextView tv_question08 = detailView.findViewById(R.id.tv_question08);
            RadioGroup rg_choices08 = detailView.findViewById(R.id.rg_choices08);
            RadioButton rb_choice08_A = detailView.findViewById(R.id.rb_choice08_A);
            RadioButton rb_choice08_B = detailView.findViewById(R.id.rb_choice08_B);
            RadioButton rb_choice08_C = detailView.findViewById(R.id.rb_choice08_C);
            RadioButton rb_choice08_D = detailView.findViewById(R.id.rb_choice08_D);
            TextView tv_question09 = detailView.findViewById(R.id.tv_question09);
            RadioGroup rg_choices09 = detailView.findViewById(R.id.rg_choices09);
            RadioButton rb_choice09_A = detailView.findViewById(R.id.rb_choice09_A);
            RadioButton rb_choice09_B = detailView.findViewById(R.id.rb_choice09_B);
            RadioButton rb_choice09_C = detailView.findViewById(R.id.rb_choice09_C);
            RadioButton rb_choice09_D = detailView.findViewById(R.id.rb_choice09_D);
            TextView tv_question10 = detailView.findViewById(R.id.tv_question10);
            RadioGroup rg_choices10 = detailView.findViewById(R.id.rg_choices10);
            RadioButton rb_choice10_A = detailView.findViewById(R.id.rb_choice10_A);
            RadioButton rb_choice10_B = detailView.findViewById(R.id.rb_choice10_B);
            RadioButton rb_choice10_C = detailView.findViewById(R.id.rb_choice10_C);
            RadioButton rb_choice10_D = detailView.findViewById(R.id.rb_choice10_D);
            Button btn_delete = detailView.findViewById(R.id.btn_delete);
            Button btn_submit = detailView.findViewById(R.id.btn_submit);
            // text setting
            tv_quiz_name.setText(chosenQuiz.getQuizName());
            et_quizzer.setText(quizzer);
            tv_quiz_time.setText(chosenQuiz.getQuizTime());
            List<QA> QAs = chosenQuiz.getQAs();
            tv_question01.setText(StringEscapeUtils.unescapeHtml4(QAs.get(0).getQuestion()));
            rb_choice01_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(0).getAnswers().get(0)));
            rb_choice01_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(0).getAnswers().get(1)));
            rb_choice01_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(0).getAnswers().get(2)));
            rb_choice01_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(0).getAnswers().get(3)));
            tv_question02.setText(StringEscapeUtils.unescapeHtml4(QAs.get(1).getQuestion()));
            rb_choice02_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(1).getAnswers().get(0)));
            rb_choice02_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(1).getAnswers().get(1)));
            rb_choice02_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(1).getAnswers().get(2)));
            rb_choice02_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(1).getAnswers().get(3)));
            tv_question03.setText(StringEscapeUtils.unescapeHtml4(QAs.get(2).getQuestion()));
            rb_choice03_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(2).getAnswers().get(0)));
            rb_choice03_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(2).getAnswers().get(1)));
            rb_choice03_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(2).getAnswers().get(2)));
            rb_choice03_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(2).getAnswers().get(3)));
            tv_question04.setText(StringEscapeUtils.unescapeHtml4(QAs.get(3).getQuestion()));
            rb_choice04_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(3).getAnswers().get(0)));
            rb_choice04_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(3).getAnswers().get(1)));
            rb_choice04_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(3).getAnswers().get(2)));
            rb_choice04_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(3).getAnswers().get(3)));
            tv_question05.setText(StringEscapeUtils.unescapeHtml4(QAs.get(4).getQuestion()));
            rb_choice05_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(4).getAnswers().get(0)));
            rb_choice05_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(4).getAnswers().get(1)));
            rb_choice05_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(4).getAnswers().get(2)));
            rb_choice05_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(4).getAnswers().get(3)));
            tv_question06.setText(StringEscapeUtils.unescapeHtml4(QAs.get(5).getQuestion()));
            rb_choice06_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(5).getAnswers().get(0)));
            rb_choice06_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(5).getAnswers().get(1)));
            rb_choice06_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(5).getAnswers().get(2)));
            rb_choice06_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(5).getAnswers().get(3)));
            tv_question07.setText(StringEscapeUtils.unescapeHtml4(QAs.get(6).getQuestion()));
            rb_choice07_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(6).getAnswers().get(0)));
            rb_choice07_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(6).getAnswers().get(1)));
            rb_choice07_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(6).getAnswers().get(2)));
            rb_choice07_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(6).getAnswers().get(3)));
            tv_question08.setText(StringEscapeUtils.unescapeHtml4(QAs.get(7).getQuestion()));
            rb_choice08_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(7).getAnswers().get(0)));
            rb_choice08_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(7).getAnswers().get(1)));
            rb_choice08_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(7).getAnswers().get(2)));
            rb_choice08_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(7).getAnswers().get(3)));
            tv_question09.setText(StringEscapeUtils.unescapeHtml4(QAs.get(8).getQuestion()));
            rb_choice09_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(8).getAnswers().get(0)));
            rb_choice09_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(8).getAnswers().get(1)));
            rb_choice09_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(8).getAnswers().get(2)));
            rb_choice09_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(8).getAnswers().get(3)));
            tv_question10.setText(StringEscapeUtils.unescapeHtml4(QAs.get(9).getQuestion()));
            rb_choice10_A.setText(StringEscapeUtils.unescapeHtml4(QAs.get(9).getAnswers().get(0)));
            rb_choice10_B.setText(StringEscapeUtils.unescapeHtml4(QAs.get(9).getAnswers().get(1)));
            rb_choice10_C.setText(StringEscapeUtils.unescapeHtml4(QAs.get(9).getAnswers().get(2)));
            rb_choice10_D.setText(StringEscapeUtils.unescapeHtml4(QAs.get(9).getAnswers().get(3)));
            // clear quizzer
            et_quizzer.setOnClickListener(click -> et_quizzer.setText(""));
            // delete
            btn_delete.setOnClickListener(click -> {
                QuestionActivity questionActivity = (QuestionActivity) getContext();
                assert questionActivity != null;
                questionActivity.notifyParentItemRemoved(chosenQuiz, chosenPosition);
                getParentFragmentManager().beginTransaction().remove(this).commit();
            });
            // submit
            btn_submit.setOnClickListener(click -> {
                // calculate score
                int score = 0;
                for (int i = 0; i < rg_choices01.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices01.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(0).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices02.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices02.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(1).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices03.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices03.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(2).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices04.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices04.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(3).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices05.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices05.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(4).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices06.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices06.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(5).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices07.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices07.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(6).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices08.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices08.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(7).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices09.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices09.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(8).getCorrect_answer())) {
                        score++;
                    }
                }
                for (int i = 0; i < rg_choices10.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rg_choices10.getChildAt(i);
                    if (radioButton.isChecked() && radioButton.getText().equals(chosenQuiz.getQAs().get(9).getCorrect_answer())) {
                        score++;
                    }
                }
                String quizzer = et_quizzer.getText().toString();
                // shared preferences
                editor = preferences.edit();
                editor.putString("Quizzer", quizzer).apply();
                SimpleDateFormat format = new SimpleDateFormat("EEE, MMM/dd/yyyy hh:mm:ss", Locale.getDefault());
                String submitTime = format.format(new Date());
                QuizzingRecord quizzingRecord = new QuizzingRecord(quizzer, score, submitTime);
                thread.execute(() -> qrDAO.insert(quizzingRecord));
                getParentFragmentManager().beginTransaction().remove(this).commit();
                Toast.makeText(getContext(), "You got " + score + " out of 10!", Toast.LENGTH_LONG).show();
            });
            return detailView;
        }
    }
    /** Method to notify activity that fragment's quiz is removed */
    private void notifyParentItemRemoved(Quiz chosenQuiz, int chosenPosition) {
        String quizName = chosenQuiz.getQuizName();
        AlertDialog.Builder builder = new AlertDialog.Builder(QuestionActivity.this);
        builder.setTitle("ALERT: ")
                .setMessage("Delete " + quizName + " ?")
                .setPositiveButton("YES", (dialog, click1) -> {
                    quizzes.remove(chosenPosition);
                    adapter.notifyItemRemoved(chosenPosition);
                    Snackbar.make(rv_question, quizName + " deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", click11 -> {
                                quizzes.add(chosenPosition, chosenQuiz);
                                adapter.notifyItemInserted(chosenPosition);
                            }).show();
                }).setNegativeButton("NO", (dialog, click2) -> {}).create().show();
    }
    // inner class for quiz
    private static class Quiz {
        String quizName;
        List<QA> QAs;
        String quizTime;
        public Quiz(String quizName, List<QA> QAs, String quizTime) {
            this.quizName = quizName;
            this.QAs = QAs;
            this.quizTime = quizTime;
        }
        public String getQuizName() {
            return quizName;
        }
        public List<QA> getQAs() {
            return QAs;
        }
        public String getQuizTime() {
            return quizTime;
        }
    }
    // inner class for question and answers
    private static class QA {
        String category;
        String question;
        String correct_answer;
        List<String> answers;
        public QA(String category, String question, String correct_answer, List<String> answers) {
            this.category = category;
            this.question = question;
            this.correct_answer = correct_answer;
            this.answers = answers;
        }
        public String getQuestion() {
            return question;
        }
        public List<String> getAnswers() {
            return answers;
        }
        public String getCorrect_answer() {
            return correct_answer;
        }
    }
}