package subham.sinha.dev.dictionary;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    MaterialButton search;
    AppCompatTextView result;
    TextInputEditText input;
    LottieAnimationView searchAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        DynamicColors.applyToActivityIfAvailable(MainActivity.this);
        setContentView(R.layout.activity_main);
        Intent i=new Intent(MainActivity.this, WordDetailActivity.class);
       startActivity(i);
        search = findViewById(R.id.search);
        result = findViewById(R.id.result);
        input = findViewById(R.id.input);
        searchAnim=findViewById(R.id.searchAnim);
        GradientDrawable corner=new GradientDrawable();
        corner.setCornerRadius(50);
        corner.setStroke(5, Color.WHITE);
        result.setBackgroundDrawable(corner);

        searchAnim.setVisibility(View.GONE);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = input.getText().toString();
                if (!input.getText().toString().isEmpty()) {
                    search.setEnabled(false);
                    searchAnim.setVisibility(View.VISIBLE);

                    subWord(word);

                } else {
                    input.setError("Enter Something Before Searching");

                }

            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    private  void searchWord(String word){

        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(
                "https://api.dictionaryapi.dev/api/v2/entries/en/"+word
        ).build();
        Call call =client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       search.setEnabled(true);
                       searchAnim.setVisibility(View.GONE);

                       Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                   }
               });

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){

                    String data=response.body().string();
                    String definition = parseDefinition(data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,definition,Toast.LENGTH_SHORT).show();
                            result.setText(definition);
                            search.setEnabled(true);
                            searchAnim.setVisibility(View.GONE);

                        }
                    });
                }


            }
        });
    }
    private String parseDefinition(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            JSONObject firstEntry = jsonArray.getJSONObject(0);
            JSONArray meanings = firstEntry.getJSONArray("meanings");
            JSONObject firstMeaning = meanings.getJSONObject(0);
            JSONArray definitions = firstMeaning.getJSONArray("definitions");
            JSONObject definition = definitions.getJSONObject(0);
            return definition.getString("definition");
        } catch (JSONException e) {
            return "Could not parse definition.";
        }
    }
    private void subWord(String word) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(
                "https://api.dictionaryapi.dev/api/v2/entries/en/" + word
        ).build();

        search.setEnabled(false); // Disable button during request

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    search.setEnabled(true); // Re-enable button
                    searchAnim.setVisibility(View.GONE);

                    result.setText(e.getMessage().toString());
                    Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        JSONObject firstEntry = jsonArray.getJSONObject(0);
                        JSONArray meanings = firstEntry.getJSONArray("meanings");
                        JSONObject firstMeaning = meanings.getJSONObject(0);
                        JSONArray definitions = firstMeaning.getJSONArray("definitions");
                        JSONObject definitionObj = definitions.getJSONObject(0);

                        String definition = definitionObj.getString("definition");

                        JSONArray synonymsArray = firstMeaning.optJSONArray("synonyms");
                        JSONArray antonymsArray = firstMeaning.optJSONArray("antonyms");

                        StringBuilder synonyms = new StringBuilder();
                        StringBuilder antonyms = new StringBuilder();

                        if (synonymsArray != null && synonymsArray.length() > 0) {
                            for (int i = 0; i < synonymsArray.length(); i++) {
                                synonyms.append(synonymsArray.getString(i)).append(", ");
                            }
                        } else {
                            synonyms.append("None");
                        }

                        if (antonymsArray != null && antonymsArray.length() > 0) {
                            for (int i = 0; i < antonymsArray.length(); i++) {
                                antonyms.append(antonymsArray.getString(i)).append(", ");
                            }
                        } else {
                            antonyms.append("None");
                        }



                        SpannableStringBuilder finalResult = new SpannableStringBuilder();

// Definition
                        finalResult.append("Definition: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        finalResult.append(definition + "\n\n");

// Synonyms
                        finalResult.append("Synonyms: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        finalResult.append(synonyms.toString().replaceAll(", $", "") + "\n\n");

// Antonyms
                        finalResult.append("Antonyms: ", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        finalResult.append(antonyms.toString().replaceAll(", $", ""));



                        runOnUiThread(() -> {
                            result.setText(finalResult);
                            search.setEnabled(true); // Re-enable after result
                            searchAnim.setVisibility(View.GONE);

                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            search.setEnabled(true);
                            searchAnim.setVisibility(View.GONE);

                            result.setText("Parsing error");
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        search.setEnabled(true);
                        searchAnim.setVisibility(View.GONE);

                        result.setText("Word not found.");
                    });
                }
            }
        });
    }


}