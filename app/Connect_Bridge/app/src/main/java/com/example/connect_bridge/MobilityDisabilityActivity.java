package com.example.connect_bridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MobilityDisabilityActivity extends AppCompatActivity {

    private EditText Mobility_Starting_Location;
    private EditText Mobility_Departure_Location;

    private String Starting_Location_Text = "";
    private String Destination_Location_Text = "";

    private Button start_navigation_button2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobilitydisability);

        Mobility_Starting_Location = findViewById(R.id.Mobility_Starting_Location);
        Mobility_Departure_Location = findViewById(R.id.Mobility_Departure_Location);
        start_navigation_button2 = findViewById(R.id.start_navigation_button2);

        Starting_Location_Text = String.valueOf(Mobility_Starting_Location.getText());
        Destination_Location_Text = String.valueOf(Mobility_Departure_Location.getText());

        start_navigation_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent next_intent = new Intent(MobilityDisabilityActivity.this, LoadingActivity.class);
                next_intent.putExtra("Mobility_Starting_Location", Starting_Location_Text);
                next_intent.putExtra("Mobility_Departure_Location", Destination_Location_Text);
                startActivity(next_intent);
                finish();
            }
        });

    }
}
