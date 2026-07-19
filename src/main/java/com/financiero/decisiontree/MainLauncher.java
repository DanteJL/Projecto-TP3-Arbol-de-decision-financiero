package com.financiero.decisiontree;

/**
 * Lanzador principal que no extiende de Application.
 * Evita problemas de configuración modular de JavaFX al arrancar desde Maven.
 */
public class MainLauncher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
