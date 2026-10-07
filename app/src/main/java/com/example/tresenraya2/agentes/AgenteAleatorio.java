package com.example.tresenraya2.agentes;



import com.example.tresenraya2.EstadoTresEnRaya;
import com.example.tresenraya2.TiposAcciones;

import java.util.ArrayList;
import java.util.Random;

/**
 * Agente de referencia que juega al azar. Sirve como rival para entrenar y
 * evaluar, y como ejemplo minimo de agente estilo GVGAI.
 */
public class AgenteAleatorio extends AbstractAgente {

    private final Random rnd;
    private Modo modo = Modo.TEST;

    public AgenteAleatorio() {
        this(System.nanoTime());
    }

    public AgenteAleatorio(long semilla) {
        this.rnd = new Random(semilla);
    }

    @Override
    public TiposAcciones act(EstadoTresEnRaya estado) {
        ArrayList<TiposAcciones> libres = estado.getDisponibles();
        return libres.get(rnd.nextInt(libres.size()));
    }

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
        return "Aleatorio";
    }
}
