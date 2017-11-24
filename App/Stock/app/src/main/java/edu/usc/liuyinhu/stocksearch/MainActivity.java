package edu.usc.liuyinhu.stocksearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        configureSwitchButton();

    }

    private void configureSwitchButton() {
        Button switchBtn = (Button) findViewById(R.id.switch_btn);
        switchBtn.setOnClickListener(new View.OnClickListener(){
            private static final String TAG = "MainActivity";

            @Override
            public void onClick(View v) {
                {
                    Log.v(TAG, "Clicked");
                }
                startActivity(new Intent(MainActivity.this, StockDetailsActivity.class));
            }
        });
    }


}
