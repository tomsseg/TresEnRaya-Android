package com.example.tresenraya2.agentes;


import com.example.tresenraya2.EstadoTresEnRaya;
import com.example.tresenraya2.TiposAcciones;

/**
 * Interfaz de un agente, al estilo de core.player.Player de GVGAI.
 *
 * El ciclo de vida es:  init -> act (cada turno) -> result (al terminar).
 * El agente observa el estado completo en cada act y devuelve una accion.
 * No recibe recompensas por movimiento: es el propio agente quien lleva su
 * memoria (estado/accion anteriores) para poder aprender.
 */
public interface Agente {

    /** Se llama una vez al empezar la partida. */
    void init(EstadoTresEnRaya estado);

    /** Se llama en cada turno del agente: observa y devuelve una accion. */
    TiposAcciones act(EstadoTresEnRaya estado);

    /** Se llama al terminar la partida. */
    void result(EstadoTresEnRaya estado);

    int getJugadorID();

    void setJugadorID(int id);

    /** Nombre legible del agente. */
    String nombre();

    // ------------------------------------------------------------------
    // Extras de la practica (no forman parte de GVGAI).
    // ------------------------------------------------------------------

    default void setModo(Modo modo) { }

    default Modo getModo() { return Modo.TEST; }

    default int numEstados() { return 0; }

    default String info() { return ""; }

    default String volcarTabla(int maxFilas) { return ""; }

    default void guardar(String ruta) { }

    default void cargar(String ruta) { }
}
