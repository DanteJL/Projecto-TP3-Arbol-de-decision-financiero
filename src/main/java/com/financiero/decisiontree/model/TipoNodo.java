package com.financiero.decisiontree.model;

/**
 * Representa los tipos de nodos en el Árbol de Decisión Financiero.
 */
public enum TipoNodo {
    DECISION, // Nodo de decisión (representado usualmente por un cuadrado)
    AZAR,     // Nodo de azar/probabilidad (representado usualmente por un círculo)
    TERMINAL  // Nodo final o de resultado (representado usualmente por un triángulo)
}
