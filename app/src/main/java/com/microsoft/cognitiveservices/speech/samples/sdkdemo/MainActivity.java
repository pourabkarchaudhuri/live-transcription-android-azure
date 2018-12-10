//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
package com.microsoft.cognitiveservices.speech.samples.sdkdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.intent.LanguageUnderstandingModel;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.intent.IntentRecognitionResult;
import com.microsoft.cognitiveservices.speech.intent.IntentRecognizer;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.samples.sdkdemo.MicrophoneStream;
import com.microsoft.cognitiveservices.speech.CancellationDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity {

    //
    // Configuration for speech recognition
    //

    // Replace below with your own subscription key
    private static final String SpeechSubscriptionKey = "5491fb6eb85f4ec5ba82f5e22755f47b";
    // Replace below with your own service region (e.g., "westus").
    private static final String SpeechRegion = "westus";

    //
    // Configuration for intent recognition
    //

    // Replace below with your own Language Understanding subscription key
    // The intent recognition service calls the required key 'endpoint key'.
    private static final String LanguageUnderstandingSubscriptionKey = "YourLanguageUnderstandingSubscriptionKey";
    // Replace below with the deployment region of your Language Understanding application
    private static final String LanguageUnderstandingServiceRegion = "YourLanguageUnderstandingServiceRegion";
    // Replace below with the application ID of your Language Understanding application
    private static final String LanguageUnderstandingAppId = "YourLanguageUnderstandingAppId";

    private TextView recognizedTextView;

    private Button recognizeButton;
    private Button recognizeIntermediateButton;
    private Button recognizeContinuousButton;
    private Button recognizeIntentButton;
  //  LinearLayout mainLayout;

    private MicrophoneStream microphoneStream;
    private MicrophoneStream createMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }

    private static final String logTag = "reco 3";
    private boolean continuousListeningStarted = false;
    private SpeechRecognizer reco = null;
    private AudioConfig audioInput = null;
    private String buttonText = "";
    private ArrayList<String> content = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        recognizedTextView = findViewById(R.id.recognizedText);
    //    mainLayout=(LinearLayout) findViewById(R.id.main_layout) ;

        ScrollView scrollC=(ScrollView)findViewById(R.id.scrolllayout) ;
        final SpeechConfig speechConfig;
        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            displayException(ex);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }

      //  ImageView img=(ImageView)findViewById(R.id.imageView2);

        //img.setVisibility(View.INVISIBLE);

        scrollC.setOnTouchListener(new SwipeDetector(this)
                                      {


                                          @Override
                                          public void onSwipeRight() {

                                              startService(new Intent(MainActivity.this, FloatingViewService.class));
                                              System.out.print("UP");
                                              Toast.makeText(getApplicationContext(),"Starting Service",Toast.LENGTH_SHORT).show();
                                              clearTextBox();
                                              //img.setVisibility(View.VISIBLE);
                                            //  Glide.with(getApplicationContext()).load(R.mipmap.animatedvoice).into(img);

                                              try {
                                                  content.clear();

                                                  // audioInput = AudioConfig.fromDefaultMicrophoneInput();
                                                  audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
                                                  reco = new SpeechRecognizer(speechConfig, audioInput);

                                                  reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                                                      scrollC.fullScroll(ScrollView.FOCUS_DOWN);
                                                      final String s = speechRecognitionResultEventArgs.getResult().getText();
                                                      Log.i(logTag, "Intermediate result received: " + s);
                                                      content.add(s);

                                                      setRecognizedText(TextUtils.join(" ", content));
                                                      content.remove(content.size() - 1);
                                                  });

                                                  reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                                                      scrollC.fullScroll(ScrollView.FOCUS_DOWN);

                                                      final String s = speechRecognitionResultEventArgs.getResult().getText();
                                                      Log.i(logTag, "Final result received: " + s);
                                                      content.add(s);
                                                      setRecognizedText(TextUtils.join(" ", content));
                                                  });

                                                  final Future<Void> task = reco.startContinuousRecognitionAsync();
                                                  setOnTaskCompletedListener(task, result -> {
                                                      continuousListeningStarted = true;

                                                  });


                                                  scrollC.fullScroll(ScrollView.FOCUS_DOWN);
                                              } catch (Exception ex) {
                                                  System.out.println(ex.getMessage());
                                                  displayException(ex);
                                              }

                                          }

                                          @Override
                                          public void onSwipeLeft() {

                                              Toast.makeText(getApplicationContext(),"Stopping Service",Toast.LENGTH_SHORT).show();
                                                clearTextBox();
                                           //   img.setVisibility(View.INVISIBLE);
                                              if (continuousListeningStarted) {
                                                  if (reco != null) {
                                                      final Future<Void> task = reco.stopContinuousRecognitionAsync();
                                                      setOnTaskCompletedListener(task, result -> {

                                                          continuousListeningStarted = false;
                                                      });
                                                  } else {
                                                      continuousListeningStarted = false;
                                                  }

                                                  return;
                                              }

                                              clearTextBox();

                                          }


                                      }


        );






        try {
            // a unique number within the application to allow
            // correlating permission request responses with the request.
            int permissionRequestId = 5;

            // Request permissions needed for speech recognition
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET}, permissionRequestId);
        }
        catch(Exception ex) {
            Log.e("SpeechSDK", "could not init sdk, " + ex.toString());
            recognizedTextView.setText("Could not initialize: " + ex.toString());
        }



        final HashMap<String, String> intentIdMap = new HashMap<>();
        intentIdMap.put("1", "play music");
        intentIdMap.put("2", "stop");


    }

    private void displayException(Exception ex) {
        recognizedTextView.setText(ex.getMessage() + System.lineSeparator() + TextUtils.join(System.lineSeparator(), ex.getStackTrace()));
    }

    private void clearTextBox() {
        AppendTextLine("", true);
    }

    private void setRecognizedText(final String s) {



        AppendTextLine(s, true);
    }

    private void AppendTextLine(final String s, final Boolean erase) {
        MainActivity.this.runOnUiThread(() -> {
            if (erase) {
                recognizedTextView.setText(s);
            } else {
                String txt = recognizedTextView.getText().toString();
                recognizedTextView.setText(txt + System.lineSeparator() + s);
            }
        });
    }



    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 2084);
    }
}
