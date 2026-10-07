package com.example.tresenraya2;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageButton[] casillas = new ImageButton[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Asociamos las 9 casillas del XML
        casillas[0] = findViewById(R.id.casilla0);
        casillas[1] = findViewById(R.id.casilla1);
        casillas[2] = findViewById(R.id.casilla2);
        casillas[3] = findViewById(R.id.casilla3);
        casillas[4] = findViewById(R.id.casilla4);
        casillas[5] = findViewById(R.id.casilla5);
        casillas[6] = findViewById(R.id.casilla6);
        casillas[7] = findViewById(R.id.casilla7);
        casillas[8] = findViewById(R.id.casilla8);

        // Listener para todas las casillas
        for (int i = 0; i < casillas.length; i++) {

            final int posicion = i;

            casillas[i].setOnClickListener(v -> {
                pulsarCasilla(posicion);
            });
        }
    }

    private void pulsarCasilla(int posicion) {

        casillas[posicion].setImageResource(R.drawable.x);

    }
}