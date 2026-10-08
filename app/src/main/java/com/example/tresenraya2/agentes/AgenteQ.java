package com.example.tresenraya2.agentes;

import com.example.tresenraya2.EstadoTresEnRaya;
import com.example.tresenraya2.TiposAcciones;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

/**
 * Agente Q-learning tabular para el 3 en raya, al estilo de un controlador RL
 * de GVGAI (hereda de AbstractAgente, como AbstractPlayer).
 *
 * ----------------------------------------------------------------------
 *  PRACTICA: completa los apartados marcados con TODO.
 *  El motor (Tablero, EstadoTresEnRaya, Partida, Main) ya esta hecho.
 * ----------------------------------------------------------------------
 *
 * IDEA CLAVE (como en GVGAI): el agente NO recibe recompensas por movimiento.
 * En cada act() observa el estado y es el quien guarda su (estadoAnterior,
 * accionAnterior) para poder actualizar Q en el siguiente turno; y en result()
 * cierra la ultima transicion y decae epsilon.
 *
 * TABLA Q: es una matriz de doubles. La FILA es el estado y la COLUMNA la
 * accion (casilla 0..8):
 *
 *       Q[estado][accion]
 *
 * Tu tarea es transformar el estado en un entero (metodo clave) y usar esa
 * fila como "tu sitio" en la tabla.
 */
public class AgenteQ extends AbstractAgente {

    private static final int N_ACCIONES = 9;
    private static final int N_ESTADOS  = 19683;

    // ------------------------------------------------------------------
    // TODO 0 (DISENO): decide como transformar el estado en un entero.
    //   Preguntas guia:
    //     - Como distingues tus fichas de las del rival?
    //     - Normalizas por perspectiva (misma tabla para X y O) o no?
    //     - Como combinas las 9 casillas en un unico entero en [0, N_ESTADOS)?
    //   La tabla ya esta creada: Q[estado][accion].
    // ------------------------------------------------------------------
    private final double[][] Q = new double[N_ESTADOS][N_ACCIONES];

    private final double alpha;
    private final double gamma;
    private final double epsMin;
    private final double decaimiento;
    private final Random rnd;

    private double epsilon;
    private Modo modo = Modo.ENTRENAMIENTO;

    // Memoria de la transicion en curso (la lleva el propio agente).
    private EstadoTresEnRaya estadoAnterior;
    private TiposAcciones accionAnterior;

    public AgenteQ(double alpha, double gamma, double epsilonInicial,
                   double epsMin, double decaimiento, long semilla) {
        this.alpha       = alpha;
        this.gamma       = gamma;
        this.epsilon     = epsilonInicial;
        this.epsMin      = epsMin;
        this.decaimiento = decaimiento;
        this.rnd         = new Random(semilla);
    }

    /** TODO 0: transforma el estado en un entero en [0, N_ESTADOS). */
    private int clave(EstadoTresEnRaya estado) {
        int clave = 0;

        for (int i = 0; i < 9; i++) {
            int valorCasilla = estado.get(i);

            int valorNormalizado;

            if (valorCasilla == 0) {
                valorNormalizado = 0;
            } else if (valorCasilla == jugadorID) {
                valorNormalizado = 1;   // soy yo
            } else {
                valorNormalizado = 2;   // es el rival
            }

            clave += valorNormalizado * (int) Math.pow(3, i);
        }

        return clave;
    }

    /** Devuelve la fila Q del estado s (su "sitio" en la tabla). */
    private double[] fila(int s) {
        return Q[s];
    }

    // ------------------------------------------------------------------
    // TODO 1: init. Prepara el agente al empezar una partida.
    //   (Pista: olvida la transicion anterior.)
    // ------------------------------------------------------------------
    @Override
    public void init(EstadoTresEnRaya estado) {
        estadoAnterior = null;
        accionAnterior = null;
    }

        // TODO

    // ------------------------------------------------------------------
    // TODO 2: act. Se llama en cada turno del agente.
    //   2a) Si hay transicion pendiente, actualiza Q con:
    //         (estadoAnterior, accionAnterior, recompensa, estado, terminal)
    //       donde recompensa = estado.getPuntuacion(jugadorID)
    //       y terminal = estado.isFinal().
    //   2b) Guarda el estado actual como estadoAnterior.
    //   2c) Elige accion ε-greedy entre estado.getDisponibles().
    //   2d) Guardala como accionAnterior y devuelvela.
    // ------------------------------------------------------------------
    @Override
    public TiposAcciones act(EstadoTresEnRaya estado) {
        if (estadoAnterior != null && accionAnterior != null && modo==Modo.ENTRENAMIENTO) {
            double r=estado.getPuntuacion(this.jugadorID);
            actualizar(estadoAnterior,accionAnterior,r,estado,false);
        }


        TiposAcciones accion = elegir(estado);   // PLACEHOLDER: falta aprender
        estadoAnterior = estado.copy();
        accionAnterior = accion;
        return accion;
    }

    /** TODO 2: regla Q-learning. */
    private void actualizar(EstadoTresEnRaya s, TiposAcciones a, double r,
                            EstadoTresEnRaya s2, boolean terminal) {
            int estado = clave(s);
            int accion=a.getCasilla();

            double qActual= Q[estado][accion];

        double maxQSiguiente = 0.0;

        if(!terminal){
            int estadoSiguiente=clave(s2);

            maxQSiguiente = Double.NEGATIVE_INFINITY;

            for (TiposAcciones accionDisponible : s2.getDisponibles()) {
                int a2 = accionDisponible.getCasilla();
                maxQSiguiente = Math.max(maxQSiguiente, Q[estadoSiguiente][a2]);
            }
        }

        Q[estado][accion] = qActual + alpha * (r + gamma * maxQSiguiente - qActual);
        // TODO
    }

    /** TODO 2: politica ε-greedy sobre las acciones disponibles. */
    private TiposAcciones elegir(EstadoTresEnRaya estado) {
        ArrayList<TiposAcciones> libres = estado.getDisponibles();
        // PLACEHOLDER: ahora juega al azar. Sustituir por explorar/explotar.
        //explorar
        if(rnd.nextDouble()<epsilon && modo==Modo.ENTRENAMIENTO){
            return libres.get(rnd.nextInt(libres.size()));
        }

        //explotar
        int claveEstado = clave(estado);

        TiposAcciones mejorAccion = libres.get(0);

        for (TiposAcciones accion : libres) {
            if (Q[claveEstado][accion.getCasilla()] > Q[claveEstado][mejorAccion.getCasilla()]) {

                mejorAccion = accion;
            }
        }

        return mejorAccion;
    }

    // ------------------------------------------------------------------
    // TODO 3: result. Se llama al terminar la partida.
    //   - Cierra la ultima transicion con la recompensa final.
    //   - Decae epsilon: epsilon = max(epsMin, epsilon * decaimiento)
    // ------------------------------------------------------------------
    @Override
    public void result(EstadoTresEnRaya estado) {
        // TODO
       if(modo==Modo.ENTRENAMIENTO) {
           double r = estado.getPuntuacion(this.jugadorID);
           actualizar(estadoAnterior, accionAnterior, r, estado, true);

           epsilon *= decaimiento;
           if (epsilon < epsMin) epsilon = epsMin;
       }
    }

    // ------------------------------------------------------------------
    // TODO 4 (opcional): persistencia en texto plano.
    // TODO 5 (extra opcional): volcarTabla para el menu.
    // ------------------------------------------------------------------
    @Override
    public void guardar(String ruta) {
        // TODO (opcional)
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {

            for (int estado = 0; estado < N_ESTADOS; estado++) {
                for (int accion = 0; accion < N_ACCIONES; accion++) {

                    pw.print(Q[estado][accion]);

                    if (accion < N_ACCIONES - 1) {
                        pw.print(" ");
                    }
                }

                pw.println();
            }

        } catch (IOException e) {
            System.out.println("Error al guardar la tabla Q: " + e.getMessage());
        }

    }

    @Override
    public void cargar(String ruta) {
        try (Scanner sc = new Scanner(new File(ruta))) {

            sc.useLocale(Locale.US);

            for (int estado = 0; estado < N_ESTADOS; estado++) {
                for (int accion = 0; accion < N_ACCIONES; accion++) {
                    Q[estado][accion] = sc.nextDouble();
                }
            }

        } catch (IOException e) {
            System.out.println("Error al cargar la tabla Q: " + e.getMessage());
        }
    }

    @Override
    public String volcarTabla(int maxFilas) {
        return "";
    }


    public void cargar(InputStream entrada) throws IOException {
        try (Scanner sc = new Scanner(entrada)) {
            sc.useLocale(Locale.US);

            double[][] tablaTemporal =
                    new double[N_ESTADOS][N_ACCIONES];

            for (int estado = 0; estado < N_ESTADOS; estado++) {
                for (int accion = 0; accion < N_ACCIONES; accion++) {
                    if (!sc.hasNextDouble()) {
                        throw new IOException(
                                "Tabla Q incompleta o incorrecta en estado "
                                        + estado + ", accion " + accion
                        );
                    }

                    tablaTemporal[estado][accion] = sc.nextDouble();
                }
            }

            for (int estado = 0; estado < N_ESTADOS; estado++) {
                System.arraycopy(
                        tablaTemporal[estado], 0,
                        Q[estado], 0, N_ACCIONES
                );
            }
        }
    }


    // ----------------------------------------------------------------
    // A partir de aqui ya esta implementado: no es necesario tocarlo.
    // ----------------------------------------------------------------

    @Override
    public void setModo(Modo modo) {
        this.modo = modo;
    }

    @Override
    public Modo getModo() {
        return modo;
    }

    @Override
    public String nombre() {
        return "Q-learning";
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double e) {
        this.epsilon = e;
    }

    @Override
    public int numEstados() {
        int n = 0;
        for (double[] fila : Q) {
            for (double v : fila) {
                if (v != 0.0) { n++; break; }
            }
        }
        return n;
    }

    @Override
    public String info() {
        return String.format("estados=%d, epsilon=%.4f", numEstados(), epsilon);
    }
}
