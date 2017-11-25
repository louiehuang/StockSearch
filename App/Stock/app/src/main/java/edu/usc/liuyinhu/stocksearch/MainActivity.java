package edu.usc.liuyinhu.stocksearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.util.ArrayList;

import edu.usc.liuyinhu.interfaces.IAsyncResponse;

public class MainActivity extends AppCompatActivity implements IAsyncResponse {
//    private static String[] stocks = new String[] {"AAP", "AAPL", "AL"};
    AutoCompleteTextView ac_stock_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //test switch button
        configureSwitchButton();

        //auto complete
        ac_stock_input = findViewById(R.id.ac_stock_input);
        ac_stock_input.setThreshold(1); //will start working from first character

        ac_stock_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() >= 1) {
                    AutoCompleteTask acTask = new AutoCompleteTask();
                    acTask.delegate = MainActivity.this;
                    acTask.execute(s.toString());
                }
            }
        });
    }


    @Override
    public void processAutoCompleteFinish(ArrayList<String> output){
        //Here receive the result fired from async class of onPostExecute(result) method.
        ArrayList<String> stocksList = output;
        ArrayAdapter<String> acAdapter = new ArrayAdapter<>(
                MainActivity.this, android.R.layout.simple_dropdown_item_1line, stocksList); //simple_dropdown_item_1line, or use select_dialog_item
        ac_stock_input.setAdapter(acAdapter);
        ac_stock_input.showDropDown(); //refresh
    }


    private void configureSwitchButton() {
        Button switchBtn = findViewById(R.id.switch_btn);
        switchBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StockDetailsActivity.class));
            }
        });
    }


}
