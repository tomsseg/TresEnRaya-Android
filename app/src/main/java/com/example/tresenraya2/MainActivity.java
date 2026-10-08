package com.example.tresenraya2;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tresenraya2.agentes.AgenteQ;
import com.example.tresenraya2.agentes.Modo;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TRES_EN_RAYA";
    private static final String PREF_NIVELES = "maxDesbloqueado";

    private final ImageButton[] casillas = new ImageButton[9];
    private final AgenteQ[] agentes = new AgenteQ[4];
    private final String[] nombresDificultad = {"Fácil", "Normal", "Difícil", "Imposible"};
    private final String[] archivosAgentes = {
            "agente_facil.txt", "agente_normal.txt",
            "agente_dificil.txt", "agente_imposible.txt"
    };

    // Un solo hilo lee las tablas Q. Nunca se modifica un agente desde ese hilo
    // después de publicarlo en el hilo principal.
    private final ExecutorService cargador = Executors.newSingleThreadExecutor();

    private Button botonNuevaPartida;
    private TextView textoResultado;
    private ColorStateList colorOriginalNuevaPartida;
    private ImageButton botonDificultad;
    private SharedPreferences preferencias;
    private EstadoTresEnRaya estado;
    private AgenteQ agente;

    private int dificultadActual = 0;
    private int dificultadPendiente = -1;
    private boolean partidaTerminada = false;
    private boolean cargaFinalizada = false;
    private boolean[] fallosCarga = new boolean[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        preferencias = getSharedPreferences("TresEnRaya", MODE_PRIVATE);
        botonNuevaPartida = findViewById(R.id.btnNuevaPartida);
        textoResultado = findViewById(R.id.textoResultado);
        colorOriginalNuevaPartida = botonNuevaPartida.getBackgroundTintList();
        botonDificultad = findViewById(R.id.btnDificultad);

        int[] ids = {
                R.id.casilla0, R.id.casilla1, R.id.casilla2,
                R.id.casilla3, R.id.casilla4, R.id.casilla5,
                R.id.casilla6, R.id.casilla7, R.id.casilla8
        };
        for (int i = 0; i < casillas.length; i++) {
            casillas[i] = findViewById(ids[i]);
            final int posicion = i;
            casillas[i].setOnClickListener(v -> pulsarCasilla(posicion));
        }

        botonNuevaPartida.setOnClickListener(v -> nuevaPartida());
        botonDificultad.setOnClickListener(v -> mostrarDificultades());

        estado = new EstadoTresEnRaya();
        int inicial = maxDesbloqueado();
        dificultadActual = inicial;
        dificultadPendiente = inicial;
        actualizarControles();
        cargarTodosLosAgentes(inicial);
    }

    private int maxDesbloqueado() {
        return Math.max(0, Math.min(3, preferencias.getInt(PREF_NIVELES, 0)));
    }

    private void cargarTodosLosAgentes(int prioridad) {
        cargador.execute(() -> {
            // Primero el nivel que debe estar disponible al abrir la aplicación.
            int[] orden = new int[4];
            orden[0] = prioridad;
            int siguiente = 1;
            for (int i = 0; i < 4; i++) {
                if (i != prioridad) orden[siguiente++] = i;
            }

            for (int nivel : orden) {
                if (Thread.currentThread().isInterrupted()) break;
                try {
                    AgenteQ cargado = new AgenteQ(0.30, 0.95, 1.0, 0.05, 0.9997, 42);
                    cargado.setJugadorID(Tablero.RIVAL);
                    cargado.setModo(Modo.TEST);
                    try (InputStream entrada = getAssets().open(archivosAgentes[nivel])) {
                        cargado.cargar(entrada);
                    }
                    final int n = nivel;
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        agentes[n] = cargado;
                        Log.d(TAG, "Cargado " + nombresDificultad[n]);
                        if (dificultadPendiente == n) activarAgente(n);
                    });
                } catch (IOException | RuntimeException e) {
                    Log.e(TAG, "Error al cargar " + archivosAgentes[nivel], e);
                    final int n = nivel;
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        fallosCarga[n] = true;
                        if (dificultadPendiente == n) {
                            dificultadPendiente = -1;
                            Toast.makeText(this, "No se pudo cargar " + nombresDificultad[n],
                                    Toast.LENGTH_LONG).show();
                            // Si el nivel inicial falla, usar otro ya cargado, si existe.
                            if (agente == null) {
                                for (int i = 0; i <= maxDesbloqueado(); i++) {
                                    if (agentes[i] != null) {
                                        activarAgente(i);
                                        break;
                                    }
                                }
                            }
                            actualizarControles();
                        }
                    });
                }
            }
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                cargaFinalizada = true;
                // Recuperación si el nivel prioritario falló y otro se cargó después.
                if (agente == null && dificultadPendiente == -1) {
                    for (int i = maxDesbloqueado(); i >= 0; i--) {
                        if (agentes[i] != null) {
                            activarAgente(i);
                            break;
                        }
                    }
                }
                actualizarControles();
            });
        });
    }

    private void actualizarControles() {
        boolean listo = agente != null && dificultadPendiente == -1;
        botonNuevaPartida.setEnabled(listo);
        botonDificultad.setEnabled(dificultadPendiente == -1);
        for (int i = 0; i < casillas.length; i++) {
            casillas[i].setEnabled(listo && !partidaTerminada
                    && estado.get(i) == Tablero.VACIO);
        }
    }

    private void activarAgente(int nivel) {
        if (agentes[nivel] == null) return;
        agente = agentes[nivel];
        dificultadActual = nivel;
        dificultadPendiente = -1;
        nuevaPartida();
        Log.d(TAG, "Dificultad activa: " + nombresDificultad[nivel]);
    }

    private void cambiarDificultad(int nivel) {
        if (nivel < 0 || nivel > maxDesbloqueado() || dificultadPendiente != -1) return;
        if (nivel == dificultadActual && agente != null) return;
        if (fallosCarga[nivel]) {
            Toast.makeText(this, "No se pudo cargar este nivel", Toast.LENGTH_SHORT).show();
            return;
        }
        if (agentes[nivel] != null) {
            activarAgente(nivel);
        } else {
            dificultadPendiente = nivel;
            actualizarControles();
            Toast.makeText(this, "Preparando " + nombresDificultad[nivel] + "...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void pulsarCasilla(int posicion) {
        if (agente == null || dificultadPendiente != -1 || partidaTerminada
                || estado.getTurno() != Tablero.AGENTE
                || estado.get(posicion) != Tablero.VACIO) return;

        estado.advance(TiposAcciones.fromInt(posicion));
        casillas[posicion].setImageResource(R.drawable.x);
        if (!comprobarFinPartida()) jugarAgente();
        actualizarControles();
    }

    private void jugarAgente() {
        if (agente == null || partidaTerminada || estado.isFinal()) return;
        TiposAcciones accion = agente.act(estado);
        if (accion == null || accion.getCasilla() < 0 || accion.getCasilla() >= 9
                || estado.get(accion.getCasilla()) != Tablero.VACIO) {
            Log.e(TAG, "El agente devolvió una acción inválida");
            Toast.makeText(this, "Error en el movimiento de la IA", Toast.LENGTH_LONG).show();
            partidaTerminada = true;
            textoResultado.setText("Error en el movimiento de la IA");
            actualizarControles();
            return;
        }
        int posicion = accion.getCasilla();
        estado.advance(accion);
        casillas[posicion].setImageResource(R.drawable.o);
        comprobarFinPartida();
    }

    private boolean comprobarFinPartida() {
        if (!estado.isFinal()) return false;
        if (partidaTerminada) return true;

        partidaTerminada = true;
        int ganador = estado.getGanador();
        if (ganador == Tablero.AGENTE) {
            textoResultado.setText("¡Has ganado!");
        } else if (ganador == Tablero.RIVAL) {
            textoResultado.setText("¡Ha ganado la IA!");
        } else {
            textoResultado.setText("¡Empate!");
        }

        // Guardar el desbloqueo sin abrir ventanas ni mostrar mensajes.
        if (ganador == Tablero.AGENTE) {
            int siguiente = dificultadActual + 1;
            if (siguiente <= 3 && siguiente > maxDesbloqueado()) {
                preferencias.edit().putInt(PREF_NIVELES, siguiente).apply();
                Log.d(TAG, "Desbloqueado: " + nombresDificultad[siguiente]);
            }
        }

        // El cambio de color indica que la partida ha terminado.
        botonNuevaPartida.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        );
        actualizarControles();
        return true;
    }

    private void nuevaPartida() {
        if (agente == null || dificultadPendiente != -1) return;
        estado = new EstadoTresEnRaya();
        agente.init(estado);
        partidaTerminada = false;
        textoResultado.setText("");
        botonNuevaPartida.setBackgroundTintList(colorOriginalNuevaPartida);
        for (ImageButton casilla : casillas) casilla.setImageDrawable(null);
        actualizarControles();
    }

    private void mostrarDificultades() {
        if (dificultadPendiente != -1) return;
        int maximo = maxDesbloqueado();
        String[] opciones = new String[4];
        for (int i = 0; i < 4; i++) {
            opciones[i] = i > maximo ? "🔒 " + nombresDificultad[i]
                    : nombresDificultad[i] + (i == dificultadActual ? " ✓" : "");
        }
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar dificultad")
                .setItems(opciones, (dialog, seleccion) -> {
                    if (seleccion > maximo) {
                        Toast.makeText(this, "Primero supera el nivel anterior",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        cambiarDificultad(seleccion);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        cargador.shutdownNow();
        super.onDestroy();
    }
}
