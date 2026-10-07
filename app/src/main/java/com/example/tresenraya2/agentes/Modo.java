package com.example.tresenraya2.agentes;

/**
 * Modo de funcionamiento de un agente.
 *
 *   ENTRENAMIENTO: explora (epsilon-greedy) y actualiza su conocimiento.
 *   TEST:          no aprende; elige siempre la mejor accion (greedy).
 */
public enum Modo {
    ENTRENAMIENTO,
    TEST
}
