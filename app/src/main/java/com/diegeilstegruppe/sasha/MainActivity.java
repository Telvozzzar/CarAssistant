package com.diegeilstegruppe.sasha;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.diegeilstegruppe.sasha.network.Communicator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Communicator communicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        communicator = new Communicator();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "click");

                        communicator.send("Hi");
                    }
                }
        );
    }
}
