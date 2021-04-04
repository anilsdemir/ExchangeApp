package com.anild.exchangeapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.github.nikartm.button.FitButton;
import com.hanks.htextview.base.HTextView;
import com.onurkagan.ksnack_lib.Animations.Fade;
import com.onurkagan.ksnack_lib.MinimalKSnack.MinimalKSnack;
import com.onurkagan.ksnack_lib.MinimalKSnack.MinimalKSnackStyle;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import in.goodiebag.carouselpicker.CarouselPicker;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

public class MainActivity extends AppCompatActivity {
    private TextView convertInformation, convertText, convertToText;
    private CarouselPicker carouselPickerFrom, carouselPickerTo;
    private FitButton convertButton;
    private MinimalKSnack minimalKSnack;
    private HTextView convertedAmount;
    private ExtendedEditText extendedEditText;
    private RelativeLayout relativeLayout;
    private MaterialProgressBar materialProgressBar;
    private String API_KEY = Utils.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        carouselPickerFrom = findViewById(R.id.carouseFrom);
        carouselPickerTo = findViewById(R.id.carouseTo);
        convertInformation = findViewById(R.id.convertInformation);
        convertText = findViewById(R.id.convertText);
        convertToText = findViewById(R.id.convertToText);
        extendedEditText = findViewById(R.id.amountText);
        convertButton = findViewById(R.id.convertButton);
        minimalKSnack = new MinimalKSnack(MainActivity.this);
        convertedAmount = findViewById(R.id.convertedAmount);
        relativeLayout = findViewById(R.id.relativeLayout);
        materialProgressBar = findViewById(R.id.progress_circular);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        //List for CarouselPicker - FROM
        final List<CarouselPicker.PickerItem> carouselFromItems = new ArrayList<>();
        carouselFromItems.add(new CarouselPicker.TextItem("TRY",10)); // 5 is text size(sp)
        carouselFromItems.add(new CarouselPicker.TextItem("USD",10)); // 5 is text size(sp)
        carouselFromItems.add(new CarouselPicker.TextItem("EUR",10)); // 5 is text size(sp)
        carouselFromItems.add(new CarouselPicker.TextItem("RUB",10)); // 5 is text size(sp)
        carouselFromItems.add(new CarouselPicker.TextItem("INR",10)); // 5 is text size(sp)
        CarouselPicker.CarouselViewAdapter carouselFromViewAdapter = new CarouselPicker.CarouselViewAdapter(this, carouselFromItems,0);
        carouselPickerFrom.setAdapter(carouselFromViewAdapter);
        carouselFromViewAdapter.setTextColor(Color.WHITE);
        String.valueOf(carouselPickerFrom.getCurrentItem());

        //List for CarouselPicker - TO
        final List<CarouselPicker.PickerItem> carouselToItems = new ArrayList<>();
        carouselToItems.add(new CarouselPicker.TextItem("TRY",10)); // 5 is text size(sp)
        carouselToItems.add(new CarouselPicker.TextItem("USD",10)); // 5 is text size(sp)
        carouselToItems.add(new CarouselPicker.TextItem("EUR",10)); // 5 is text size(sp)
        carouselToItems.add(new CarouselPicker.TextItem("RUB",10)); // 5 is text size(sp)
        carouselToItems.add(new CarouselPicker.TextItem("INR",10)); // 5 is text size(sp)
        CarouselPicker.CarouselViewAdapter carouselToViewAdapter = new CarouselPicker.CarouselViewAdapter(this, carouselToItems, 0);
        carouselPickerTo.setAdapter(carouselToViewAdapter);
        carouselFromViewAdapter.setTextColor(Color.WHITE);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                convertedAmount.setVisibility(View.INVISIBLE);
                if (!extendedEditText.getText().toString().isEmpty()) {
                    String fromText = carouselFromItems.get(carouselPickerFrom.getCurrentItem()).getText();
                    String toText = carouselToItems.get(carouselPickerTo.getCurrentItem()).getText();
                    int amount = Integer.parseInt(extendedEditText.getText().toString());
                    if (fromText.equals(toText)) {
                        minimalKSnack
                                .setMessage("Please select a different currency")
                                .setStyle(MinimalKSnackStyle.STYLE_ERROR)
                                .setBackgroundColor(R.color.ksnack_error)
                                .setAnimation(Fade.In.getAnimation(), Fade.Out.getAnimation()) // show and hide animations
                                .setDuration(1500) // you can use for auto close.
                                .show();
                    }else {
                        materialProgressBar.setVisibility(View.VISIBLE);
                        getJson(fromText, toText, amount);

                    }

                } else {
                    minimalKSnack
                            .setMessage("The value you enter must be greater than 0 or should not be empty")
                            .setStyle(MinimalKSnackStyle.STYLE_ERROR)
                            .setBackgroundColor(R.color.ksnack_error)
                            .setAnimation(Fade.In.getAnimation(), Fade.Out.getAnimation()) // show and hide animations
                            .setDuration(4000) // you can use for auto close.
                            .show();
                }
            }
        });
    }

    public void getJson(final String fromText, final String toText, final int amount) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://free.currconv.com/api/v7/convert?apiKey=" + API_KEY + "&q=" + fromText + "_" + toText;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call,@NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    assert response.body() != null;
                    String myResponse = response.body().string();
                    System.out.println(myResponse);
                    if (myResponse.isEmpty()) {
                        minimalKSnack
                                .setMessage("Several errors occurred while performing the operation")
                                .setStyle(MinimalKSnackStyle.STYLE_ERROR)
                                .setBackgroundColor(R.color.ksnack_error)
                                .setAnimation(Fade.In.getAnimation(), Fade.Out.getAnimation()) // show and hide animations
                                .setDuration(4000) // you can use for auto close.
                                .show();
                    } else {
                        try {
                            JSONObject object = new JSONObject(myResponse);
                            JSONObject results = (JSONObject) object.get("results");
                            JSONObject value = (JSONObject) results.get(fromText + "_" + toText);
                            final String realValue = String.valueOf(amount * Double.parseDouble(value.get("val").toString()));
                            runOnUiThread(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    //List for Currency Symbol
                                    Map<String, String> currencies = new HashMap<String, String>(){
                                        {
                                            put("TRY", "\u20BA");
                                            put("USD","\u0024");
                                            put("EUR","\u20AC");
                                            put("RUB","\u20BD");
                                            put("INR", "\u20B9");
                                        }
                                    };
                                    if (realValue.length() > 4) {
                                        String outcome = realValue.substring(0,5);
                                        String fifthElement = String.valueOf(outcome.charAt(4));
                                        if (fifthElement.equals(".")) {
                                            String symbol = currencies.get(fromText);
                                            materialProgressBar.setVisibility(View.INVISIBLE);
                                            convertedAmount.setVisibility(View.VISIBLE);
                                            convertedAmount.animateText(amount + " " + symbol + " = " + outcome + "  " + toText);
                                        }else {
                                            String symbol = currencies.get(fromText);
                                            materialProgressBar.setVisibility(View.INVISIBLE);
                                            convertedAmount.setVisibility(View.VISIBLE);
                                            convertedAmount.animateText(amount + " " + symbol + " = " + outcome + "  " + toText);
                                        }

                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }
}




