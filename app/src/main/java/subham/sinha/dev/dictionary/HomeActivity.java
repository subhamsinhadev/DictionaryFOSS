package subham.sinha.dev.dictionary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends Activity {
    TextInputEditText search_edit_text;
    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        search_edit_text=findViewById(R.id.search_edit_text);
        search_edit_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                if(action== EditorInfo.IME_ACTION_SEARCH){

                    String word_to_search=search_edit_text.getText().toString().trim();
                    String resp= subWord(word_to_search);

                    Intent i=new Intent(HomeActivity.this, WordDetailActivity.class);
                    i.putExtra("resp",resp);
                    startActivity(i);


                }
                return false;
            }
        });
    }

//    private String subWord(String word) {
//        String data;
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder().url(
//                "https://api.dictionaryapi.dev/api/v2/entries/en/" + word
//        ).build();
//
////        search.setEnabled(false); // Disable button during request
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                runOnUiThread(() -> {
//
////                    result.setText(e.getMessage().toString());
//                   Toast.makeText(HomeActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    data= response.body().string();
//                    try {
//
//// Synonyms
//
//
//                        runOnUiThread(() -> {
//                            Toast.makeText(HomeActivity.this,data.toString(),Toast.LENGTH_SHORT).show();
//
//
//                        });
//
//                    } catch (Exception e) {
//                        runOnUiThread(() -> {
//
//                        });
//                    }
//                } else {
//                    runOnUiThread(() -> {
//
//                    });
//                }
//            }
//        });
//        return data;
//    }



private String subWord(String word) {
    AtomicReference<String> dataRef = new AtomicReference<>("");
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url("https://api.dictionaryapi.dev/api/v2/entries/en/" + word)
            .build();

    Thread thread = new Thread(() -> {
        try {
            Response response = client.newCall(request).execute(); // synchronous
            if (response.isSuccessful() && response.body() != null) {
                String responseData = response.body().string();
                dataRef.set(responseData); // store in AtomicReference

                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, dataRef.get(), Toast.LENGTH_SHORT).show();
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Response not successful", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            runOnUiThread(() -> {
                Toast.makeText(HomeActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    });

    thread.start();

    try {
        thread.join(); // Wait for the network thread to finish
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    return dataRef.get(); // return the response
}


}
