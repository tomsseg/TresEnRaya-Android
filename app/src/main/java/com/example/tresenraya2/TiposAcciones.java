package com.example.tresenraya2;

/**
 * Acciones del 3 en raya, al estilo de "ontology.Types.ACTIONS" de GVGAI.
 *
 * Cada accion es una casilla, nombrada por fila-columna (0..2). El ordinal
 * coincide con la casilla 0..8 (recorrido por filas), asi que no hace falta
 * saber en que orden esta declarado el enum.
 */
public enum TiposAcciones {
    ACTION_00, ACTION_01, ACTION_02,
    ACTION_10, ACTION_11, ACTION_12,
    ACTION_20, ACTION_21, ACTION_22;

    private static final TiposAcciones[] VALORES = values();

    public int getFila() {
        return ordinal() / 3;
    }

    public int getColumna() {
        return ordinal() % 3;
    }

    /** Casilla 0..8 equivalente. */
    public int getCasilla() {
        return ordinal();
    }

    /** Equivalente a Types.ACTIONS.fromInt(int). */
    public static TiposAcciones fromInt(int i) {
        return VALORES[i];
    }

    public static TiposAcciones[] todas() {
        return VALORES.clone();
    }
}
