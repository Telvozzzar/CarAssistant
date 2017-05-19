package com.diegeilstegruppe.sasha;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView text = (TextView) findViewById(R.id.textView);

        Button button = (Button) findViewById(R.id.dummy_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recording) {
                    text.setText("Not recording");
                    recording = false;
                } else {
                    text.setText("Recording");
                    recording = true;
                }

            }
        });
    }


}
