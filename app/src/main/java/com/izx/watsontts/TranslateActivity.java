package com.izx.watsontts;

import android.app.DialogFragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.IdentifiableLanguage;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationModel;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.exception.UnauthorizedException;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;

public class TranslateActivity extends AppCompatActivity {

    private TextToSpeech speechService;
    private LanguageTranslation translationService;
    private MediaPlayer mediaPlayer;

    private Spinner entryLanguageSpinner;
    private Spinner resultLanguageSpinner;
    private TextView entryEditText;
    private TextView resultEditText;

    // Data structures used to hold LanguageTranslation Data and Relationships.
    private ArrayList<ArrayList<String>> supportedTranslationModels;
    private Map<String, String> shortToLongLanguageNameMap;
    private Map<String, String> longToShortLanguageNameMap;

    String judul, ing, how, image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mediaPlayer = new MediaPlayer();

        // Spinners that will hold our possible input/output languages.
        entryLanguageSpinner = (Spinner)findViewById(R.id.entryLanguageSpinner);
        resultLanguageSpinner = (Spinner)findViewById(R.id.resultLanguageSpinner);

        // Textboxes that will hold our input and result text.
        entryEditText = (TextView)findViewById(R.id.translationEntry);
        resultEditText = (TextView)findViewById(R.id.translationResult);

        // Core SDK must be initialized to interact with Bluemix Mobile services.
        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_UK);

        // Insert your credentials first in Constant.java
        // Instantiate Watson Services and Grab User Credentials.
        speechService = new TextToSpeech();
        speechService.setUsernameAndPassword(Constant.USERNAME_TTS, Constant.PASSWORD_TTS);

        translationService = new LanguageTranslation();
        translationService.setEndPoint("https://gateway.watsonplatform.net/language-translator/api");
        translationService.setUsernameAndPassword(Constant.USERNAME_TRANSLATOR, Constant.PASSWORD_TRANSLATOR);

        // Validate User Credentials.
        ValidateCredentialsTask vct = new ValidateCredentialsTask();
        vct.execute();

        // Buttons to be given onClick Listeners.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        ImageButton entrySpeechButton = (ImageButton)findViewById(R.id.entrySpeechButton);
        ImageButton resultSpeechButton = (ImageButton)findViewById(R.id.resultSpeechButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUIDataToTranslationTask();
            }
        });

        entrySpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = entryEditText.getText().toString();

                if (inputText.length() > 0) {
                    String inputLanguage = (String)entryLanguageSpinner.getSelectedItem();

                    String[] inputs = {inputText, inputLanguage};

                    SpeechTask speaker = new SpeechTask();
                    speaker.execute(inputs);
                }
            }
        });

        resultSpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = resultEditText.getText().toString();

                if (inputText.length() > 0) {
                    String outputLanguage = (String)resultLanguageSpinner.getSelectedItem();

                    String[] inputs = {inputText, outputLanguage};

                    SpeechTask speaker = new SpeechTask();
                    speaker.execute(inputs);
                }
            }
        });

        getdata();
    }

    private void getdata() {
        try {
            Intent i = getIntent();
            judul = i.getStringExtra(Constant.JUDUL);
            ing = i.getStringExtra(Constant.INGREDIENTS);
            how = i.getStringExtra(Constant.HOW);
            image = i.getStringExtra(Constant.IMAGE);
            getSupportActionBar().setTitle(judul+" Translate");
            if (ing == null) {
                entryEditText.setText("");
                entryEditText.setText(how);
            } else {
                entryEditText.setText("");
                entryEditText.setText(ing);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // We stop media player onStop() so recreate if we're resuming from a stop.
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();

        mediaPlayer.release();
        mediaPlayer = null;
    }

    /**
     * Displays an AlertDialogFragment with the given parameters.
     * @param errorTitle Error Title from values/strings.xml.
     * @param errorMessage Error Message either from values/strings.xml or response from server.
     * @param canContinue Whether the application can continue without needing to be rebuilt.
     */
    private void showDialog(int errorTitle, String errorMessage, boolean canContinue) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(errorTitle, errorMessage, canContinue);
        newFragment.show(getFragmentManager(), "dialog");
    }

    /**
     * Helper function that pulls all user input and sends it to the TranslationTask.
     */
    private void sendUIDataToTranslationTask() {

        String inputText = entryEditText.getText().toString();
        if (inputText.length() > 0) {
            String inputLanguage = (String)entryLanguageSpinner.getSelectedItem();
            String outputLanguage = (String)resultLanguageSpinner.getSelectedItem();

            String[] inputValues = {inputText, inputLanguage, outputLanguage};

            TranslationTask translator = new TranslationTask();
            translator.execute(inputValues);
        }
    }

    /**
     * Asynchronous Task, called in onCreate, used to validate Watson credentials and give pertinent
     * information if credentials are invalid or if the application cannot connect to Bluemix.
     */
    private class ValidateCredentialsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            // First testing if we can reach Bluemix and Watson Translator.
            try {
                translationService.getToken().execute();
            } catch (Exception ex) {
                if (ex.getClass().equals(UnauthorizedException.class) ||
                        ex.getClass().equals(IllegalArgumentException.class)) {
                    showDialog(R.string.error_title_invalid_credentials_translator,
                            getString(R.string.error_message_invalid_credentials_translator), false);
                    return false;
                } else if (ex.getCause() != null &&
                        ex.getCause().getClass().equals(UnknownHostException.class)) {
                    showDialog(R.string.error_title_bluemix_connection,
                            getString(R.string.error_message_bluemix_connection), false);
                    return false;
                } else {
                    showDialog(R.string.error_title_default, ex.getMessage(), true);
                }
            }

            // Now Text to Speech, we have already checked the connection to Bluemix so only checking Credentials.
            try {
                speechService.getToken().execute();
            } catch (Exception ex) {
                if (ex.getClass().equals(UnauthorizedException.class) ||
                        ex.getClass().equals(IllegalArgumentException.class)) {
                    showDialog(R.string.error_title_invalid_credentials_speech,
                            getString(R.string.error_message_invalid_credentials_speech), true);

                    // Disable the Text to Speech buttons.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageButton entrySpeechButton = (ImageButton)findViewById(R.id.entrySpeechButton);
                            ImageButton resultSpeechButton = (ImageButton)findViewById(R.id.resultSpeechButton);

                            entrySpeechButton.setVisibility(View.GONE);
                            resultSpeechButton.setVisibility(View.GONE);
                        }
                    });
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // If the credentials are valid enough to continue then build the UI using the FTD Task.
                FetchTranslationDataTask ftdt = new FetchTranslationDataTask();
                ftdt.execute();
            }
        }
    }

    /**
     * Asynchronous Task, called in onCreate, used to populate UI Spinners dynamically from Watson
     * LanguageTranslation and create mappings for Short to Long Language names and vice-versa.
     */
    private class FetchTranslationDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            supportedTranslationModels = new ArrayList<>();
            shortToLongLanguageNameMap = new HashMap<>();
            longToShortLanguageNameMap = new HashMap<>();

            List<TranslationModel> allLanguages = translationService.getModels().execute();

            List<IdentifiableLanguage> allIdLanguages = translationService.getIdentifiableLanguages().execute();
            for (int i = 0; i < allIdLanguages.size(); i++) {
                shortToLongLanguageNameMap.put(allIdLanguages.get(i).getLanguage(), allIdLanguages.get(i).getName());
                longToShortLanguageNameMap.put(allIdLanguages.get(i).getName(), allIdLanguages.get(i).getLanguage());
            }

            // Watson Translation Models want "arz" whereas Identifiable Languages returns "az".
            shortToLongLanguageNameMap.put("arz", "Azerbaijan");
            longToShortLanguageNameMap.put("Azerbaijan", "arz");

            // Create the source-to-target language relations by iterating through the returned structure from Translator.
            boolean alreadyContainsFlag;
            int entryLanguagePosition;

            for (int j = 0; j < allLanguages.size(); j++) {
                if(allLanguages.get(j).isDefaultModel()) {

                    entryLanguagePosition = -1;
                    alreadyContainsFlag = false;
                    String sourceLanguage = shortToLongLanguageNameMap.get(allLanguages.get(j).getSource());

                    for (int k = 0; k < supportedTranslationModels.size(); k++) {
                        if (supportedTranslationModels.get(k).get(0).equals(sourceLanguage)){
                            alreadyContainsFlag = true;
                            entryLanguagePosition = k;
                        }
                    }
                    if (!alreadyContainsFlag) {
                        ArrayList<String> tempList = new ArrayList<>();
                        tempList.add(sourceLanguage);
                        supportedTranslationModels.add(tempList);
                        entryLanguagePosition = supportedTranslationModels.size() - 1;
                    }

                    String targetLanguage = shortToLongLanguageNameMap.get(allLanguages.get(j).getTarget());
                    supportedTranslationModels.get(entryLanguagePosition).add(targetLanguage);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            ArrayList<String> sourceLanguages = new ArrayList<>();

            int englishLocation = 0; //I want to default to English input language.

            for (int i = 0; i < supportedTranslationModels.size(); i++) {
                String supportedModelSource = supportedTranslationModels.get(i).get(0);

                if (supportedModelSource.equals("English"))
                    englishLocation = i;

                sourceLanguages.add(supportedModelSource);
            }

            // Populate the Input Spinner with Source Languages that have Output Languages.
            ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(getApplicationContext(),
                    R.layout.spinner_item, sourceLanguages);

            inputAdapter.setDropDownViewResource(R.layout.dropdown_item);
            entryLanguageSpinner.setAdapter(inputAdapter);
            entryLanguageSpinner.setSelection(englishLocation);

            // For each Source Language add its respective Output Languages to the Output Spinner.
            entryLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    resultLanguageSpinner.setEnabled(true);

                    String entryLanguage = (String)parent.getItemAtPosition(position);
                    ArrayList<String> supportOutput = new ArrayList<>();

                    for (int i = 0; i < supportedTranslationModels.size(); i++) {
                        if (supportedTranslationModels.get(i).get(0).equals(entryLanguage)){
                            supportOutput.addAll(supportedTranslationModels.get(i));
                            supportOutput.remove(0); // 0th entry is the source language itself, remove it.
                        }
                    }

                    ArrayAdapter<String> outputAdapter = new ArrayAdapter<>(getApplicationContext(),
                            R.layout.spinner_item, supportOutput);
                    outputAdapter.setDropDownViewResource(R.layout.dropdown_item);
                    resultLanguageSpinner.setAdapter(outputAdapter);
                    resultLanguageSpinner.setSelection(0);

                    sendUIDataToTranslationTask();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    resultLanguageSpinner.setEnabled(false);
                }
            });

            // When a new Output Language is selected translate the text in the input to the new selection.
            resultLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sendUIDataToTranslationTask();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    /**
     * Asynchronous Task used to consume Watson LanguageTranslation Service given an input
     * language, output language, and input text. Shows result in the resultEditText.
     */
    private class TranslationTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String inputText = params[0];
            String inputLanguage = params[1];
            String outputLanguage = params[2];

            // Translation Model Names are of the format shortName-shortName (so "en-ar").
            String tlModelName = longToShortLanguageNameMap.get(inputLanguage) + "-"
                    + longToShortLanguageNameMap.get(outputLanguage);

            TranslationResult result = translationService.translate(inputText, tlModelName).execute();

            return result.getFirstTranslation();
        }

        @Override
        protected void onPostExecute(String result) {
            resultEditText.setText(result);
        }
    }

    /**
     * Asynchronous Task used to consume Watson Text to Speech Service. Takes an input string and
     * either creates and plays an audio transcript of the string or displays a Toast for unsupported
     * languages. Unsupported languages are dynamically received from Watson Text to Speech.
     */
    private class SpeechTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String inputText = params[0];
            String inputLanguage = longToShortLanguageNameMap.get(params[1]); // Grab internal short-name.

            List<Voice> allVoices = speechService.getVoices().execute();

            boolean voiceAvailable = false;
            Voice matchingVoice = new Voice();

            // Iterate through all available voices, if a voice in the correct language is found use it.
            for (int i = 0; i < allVoices.size(); i++) {
                // Voice.language is of the form "en-English". We just want the short-name.
                String languageShortName = allVoices.get(i).getLanguage().substring(0, 2);

                if (languageShortName.equals(inputLanguage)) {
                    voiceAvailable = true;
                    matchingVoice = allVoices.get(i);
                    break;
                }
            }

            // If we can't find a voice we'll inform the user.
            if (!voiceAvailable) {
                return false;
            }

            // Synthesize returns an inputStream, we have to write to a file before playback can occur.
            InputStream in = speechService.synthesize(inputText, matchingVoice,
                    AudioFormat.OGG_VORBIS).execute();

            try {
                File tempTranslation = File.createTempFile("translation", ".ogg", getCacheDir());
                FileOutputStream out = new FileOutputStream(tempTranslation);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                out.close();
                in.close();

                FileInputStream fis = new FileInputStream(tempTranslation);

                mediaPlayer.reset();
                mediaPlayer.setDataSource(fis.getFD());

                fis.close();

                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                String unspeakableLanguage = resultLanguageSpinner.getSelectedItem().toString();

                Toast.makeText(getApplicationContext(), "Speech-to-Text does not currently support "
                        + unspeakableLanguage + " playback.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
