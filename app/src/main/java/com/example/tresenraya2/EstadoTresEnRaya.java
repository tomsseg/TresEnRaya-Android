package com.example.tresenraya2;

import java.util.ArrayList;


public class EstadoTresEnRaya {

    private final Tablero tablero;
    private int turno;                 // Tablero.AGENTE (1) o Tablero.RIVAL (2)

    public EstadoTresEnRaya() {
        this.tablero = new Tablero();
        this.turno = Tablero.AGENTE;
    }

    private EstadoTresEnRaya(Tablero tablero, int turno) {
        this.tablero = tablero;
        this.turno = turno;
    }

    public int getTurno() {
        return turno;
    }

    public int get(int casilla) {
        return tablero.get(casilla);
    }

    public Tablero getTablero() {
        return tablero;
    }

    /** Acciones legales disponibles */
    public ArrayList<TiposAcciones> getDisponibles() {
        ArrayList<TiposAcciones> libres = new ArrayList<>();
        if (isFinal()) return libres;
        for (int i : tablero.libres()) {
            libres.add(TiposAcciones.fromInt(i));
        }
        return libres;
    }

    /** Copia profunda */
    public EstadoTresEnRaya copy() {
        return new EstadoTresEnRaya(new Tablero(tablero), turno);
    }

    /** Aplica una accion y pasa el turno, como advance(accion) de GVGAI. */
    public void advance(TiposAcciones accion) {
        if (isFinal()) return;
        int casilla = accion.getCasilla();
        if (tablero.get(casilla) != Tablero.VACIO) return;   // accion ilegal
        tablero.poner(casilla, turno);
        turno = (turno == Tablero.AGENTE) ? Tablero.RIVAL : Tablero.AGENTE;
    }

    /** isGameOver(): true si hay ganador o empate. */
    public boolean isFinal() {
        return tablero.resultado() != 0;
    }

    /** getWinner(): 1 o 2 si gana alguien, -1 empate, 0 si sigue la partida. */
    public int getGanador() {
        return tablero.resultado();
    }

    /**
     * Puntuacion desde la perspectiva de un jugador:
     *   +1 si gano, -1 si perdio, 0 si empato o la partida sigue.
     * Es la recompensa natural para Q-learning.
     */
    public double getPuntuacion(int jugador) {
        int res = tablero.resultado();
        if (res == 0 || res == -1) return 0.0;
        return (res == jugador) ? 1.0 : -1.0;
    }

    public void mostrar() {
        tablero.mostrar();
    }
}
