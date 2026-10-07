package com.example.tresenraya2.agentes;


import com.example.tresenraya2.EstadoTresEnRaya;

/**
 * Clase base para agentes, al estilo de core.player.AbstractPlayer de GVGAI.
 *
 * Guarda el identificador de jugador y deja init/result vacios. Las clases
 * hijas solo tienen que implementar act().
 */
public abstract class AbstractAgente implements Agente {

    protected int jugadorID = -1;

    @Override
    public void init(EstadoTresEnRaya estado) { }

    @Override
    public void result(EstadoTresEnRaya estado) { }

    @Override
    public int getJugadorID() {
        return jugadorID;
    }

    @Override
    public void setJugadorID(int id) {
        this.jugadorID = id;
    }
}
