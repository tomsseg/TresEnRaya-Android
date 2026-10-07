package com.example.tresenraya2;

import java.util.ArrayList;

/**
 * Tablero de 3 en raya.
 *
 * Casillas: 0 = vacia, 1 = ficha del AGENTE, 2 = ficha del RIVAL.
 * El estado se codifica en base 3:  s = sum c[i] * 3^i   (0 .. 3^9-1).
 */
public class Tablero {

    public static final int VACIO  = 0;
    public static final int AGENTE = 1;
    public static final int RIVAL  = 2;

    private final int[] c = new int[9];

    /** Combinaciones ganadoras (filas, columnas y diagonales). */
    private static final int[][] LINEAS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},   // filas
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},   // columnas
        {0, 4, 8}, {2, 4, 6}               // diagonales
    };

    public Tablero() { }

    public Tablero(Tablero otro) {
        System.arraycopy(otro.c, 0, this.c, 0, 9);
    }

    public int get(int i) {
        return c[i];
    }

    public void poner(int i, int jugador) {
        c[i] = jugador;
    }

    /** Casillas libres. */
    public int[] libres() {
        ArrayList<Integer> l = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (c[i] == VACIO) l.add(i);
        }
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) r[i] = l.get(i);
        return r;
    }

    /**
     * Resultado del tablero:
     *   AGENTE o RIVAL si hay ganador, 0 si la partida continua,
     *   -1 si es empate (tablero lleno sin ganador).
     */
    public int resultado() {
        for (int[] L : LINEAS) {
            if (c[L[0]] != VACIO && c[L[0]] == c[L[1]] && c[L[1]] == c[L[2]]) {
                return c[L[0]];
            }
        }
        for (int v : c) {
            if (v == VACIO) return 0;
        }
        return -1;
    }

    // Nota: la codificacion del estado (como convertir el tablero en una clave)
    // la decide el agente. Este tablero solo expone sus casillas con get(i).

    public void mostrar() {
        char[] sim = {' ', 'X', 'O'};
        for (int f = 0; f < 3; f++) {
            System.out.println(" " + sim[c[f * 3]] + " | "
                    + sim[c[f * 3 + 1]] + " | " + sim[c[f * 3 + 2]]);
            if (f < 2) System.out.println("---+---+---");
        }
    }
}
