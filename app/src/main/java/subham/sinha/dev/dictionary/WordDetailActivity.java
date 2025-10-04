package subham.sinha.dev.dictionary;



import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class WordDetailActivity extends AppCompatActivity {

    private TextView textWord, textPhonetic, textSourceUrl;
    private LinearLayout layoutDefinitions;
    private ChipGroup chipGroupSynonyms, chipGroupAntonyms;
    private MaterialButton buttonPlayAudio, buttonFavorite, buttonShare;

    private MediaPlayer mediaPlayer;
    private String audioUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);

        // Bind views
        textWord = findViewById(R.id.text_word);
        textPhonetic = findViewById(R.id.text_phonetic);
        textSourceUrl = findViewById(R.id.text_source_url);
        layoutDefinitions = findViewById(R.id.layout_definitions_1);
        chipGroupSynonyms = findViewById(R.id.chip_group_synonyms_1);
        chipGroupAntonyms = findViewById(R.id.chip_group_antonyms_1);
        buttonPlayAudio = findViewById(R.id.button_play_audio);
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonShare = findViewById(R.id.button_share);
Intent i=getIntent();

      String response=i.getStringExtra("resp") ;
        parseAndPopulate(response);

        // Audio playback
        buttonPlayAudio.setOnClickListener(v -> {
            if (!audioUrl.isEmpty()) {
                playAudio(audioUrl);
            } else {
                Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show();
            }
        });

        // Favorite click
        buttonFavorite.setOnClickListener(v ->
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show());

        // Share click
        buttonShare.setOnClickListener(v -> {
            String shareText = "Check out the word: " + textWord.getText().toString() +
                    "\n" + textSourceUrl.getText().toString();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
    }

    private void parseAndPopulate(String jsonResponse) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            if (jsonArray.length() == 0) return;

            // Merge all entries for the same word
            JSONObject firstWord = jsonArray.getJSONObject(0);
            textWord.setText(firstWord.optString("word", "Not available"));

            // Merge phonetics: show first non-empty text and first non-empty audio
            String phoneticText = "Not available";
            audioUrl = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject wordObj = jsonArray.getJSONObject(i);
                JSONArray phonetics = wordObj.optJSONArray("phonetics");
                if (phonetics != null) {
                    for (int j = 0; j < phonetics.length(); j++) {
                        JSONObject phoneticObj = phonetics.getJSONObject(j);
                        if (phoneticObj.has("text") && !phoneticObj.getString("text").isEmpty() && phoneticText.equals("Not available")) {
                            phoneticText = phoneticObj.getString("text");
                        }
                        if (phoneticObj.has("audio") && !phoneticObj.getString("audio").isEmpty() && audioUrl.isEmpty()) {
                            audioUrl = phoneticObj.getString("audio");
                        }
                    }
                }
            }
            textPhonetic.setText(phoneticText);
            buttonPlayAudio.setEnabled(!audioUrl.isEmpty());

            // Clear previous dynamic views
            layoutDefinitions.removeAllViews();
            chipGroupSynonyms.removeAllViews();
            chipGroupAntonyms.removeAllViews();

            // Merge meanings & definitions
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject wordObj = jsonArray.getJSONObject(i);
                JSONArray meanings = wordObj.optJSONArray("meanings");
                if (meanings != null) {
                    for (int m = 0; m < meanings.length(); m++) {
                        JSONObject meaning = meanings.getJSONObject(m);

                        // Part of speech
                        String partOfSpeech = meaning.optString("partOfSpeech", "Not available");
                        TextView posText = new TextView(this);
                        posText.setText("Part of Speech: " + partOfSpeech);
                        posText.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
                        posText.setPadding(0,0,0,8);
                        layoutDefinitions.addView(posText);

                        // Definitions
                        JSONArray definitions = meaning.optJSONArray("definitions");
                        if (definitions != null && definitions.length() > 0) {
                            for (int d = 0; d < definitions.length(); d++) {
                                JSONObject defObj = definitions.getJSONObject(d);
                                String defTextStr = defObj.optString("definition", "Not available");
                                TextView defText = new TextView(this);
                                defText.setText((d + 1) + ". " + defTextStr);
                                defText.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                                defText.setPadding(0,0,0,16);
                                layoutDefinitions.addView(defText);
                            }
                        } else {
                            TextView defText = new TextView(this);
                            defText.setText("Not available");
                            defText.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                            layoutDefinitions.addView(defText);
                        }

                        // Synonyms
                        JSONArray syns = meaning.optJSONArray("synonyms");
                        if (syns != null && syns.length() > 0) {
                            for (int s = 0; s < syns.length(); s++) {
                                Chip chip = new Chip(this);
                                chip.setText(syns.getString(s));
                                chipGroupSynonyms.addView(chip);
                            }
                        } else {
                            Chip chip = new Chip(this);
                            chip.setText("Not available");
                            chipGroupSynonyms.addView(chip);
                        }

                        // Antonyms
                        JSONArray ants = meaning.optJSONArray("antonyms");
                        if (ants != null && ants.length() > 0) {
                            for (int a = 0; a < ants.length(); a++) {
                                Chip chip = new Chip(this);
                                chip.setText(ants.getString(a));
                                chipGroupAntonyms.addView(chip);
                            }
                        } else {
                            Chip chip = new Chip(this);
                            chip.setText("Not available");
                            chipGroupAntonyms.addView(chip);
                        }
                    }
                }
            }

            // Source URLs: merge unique URLs
            JSONArray allSources = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject wordObj = jsonArray.getJSONObject(i);
                JSONArray sources = wordObj.optJSONArray("sourceUrls");
                if (sources != null) {
                    for (int s = 0; s < sources.length(); s++) {
                        String url = sources.getString(s);
                        if (!allSources.toString().contains(url)) {
                            allSources.put(url);
                        }
                    }
                }
            }
            if (allSources.length() > 0) {
                textSourceUrl.setText(allSources.getString(0));
            } else {
                textSourceUrl.setText("Not available");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void playAudio(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot play audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}

