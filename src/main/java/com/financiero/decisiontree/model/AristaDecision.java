package com.financiero.decisiontree.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa una transición (arista) en el árbol de decisión financiera.
 */
public class AristaDecision {
    private String id;
    private double probabilidad; // Entre 0.0 y 1.0. Para nodos DECISION puede ser 1.0 (o no relevante)
    private double flujoCaja;    // Flujo de caja o costo asociado a tomar esta rama
    private String etiqueta;     // Descripción de la opción o suceso

    @JsonIgnore
    private NodoDecision origen;
    
    @JsonIgnore
    private NodoDecision destino;

    // Campos auxiliares para la persistencia JSON
    private String origenId;
    private String destinoId;

    // Campo auxiliar para marcar si pertenece a la ruta óptima
    private boolean enRutaOptima;

    public AristaDecision() {
        // Constructor vacío para Jackson
    }

    public AristaDecision(String id, NodoDecision origen, NodoDecision destino, double probabilidad, double flujoCaja, String etiqueta) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.probabilidad = probabilidad;
        this.flujoCaja = flujoCaja;
        this.etiqueta = etiqueta;
        if (origen != null) this.origenId = origen.getId();
        if (destino != null) this.destinoId = destino.getId();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public double getFlujoCaja() {
        return flujoCaja;
    }

    public void setFlujoCaja(double flujoCaja) {
        this.flujoCaja = flujoCaja;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public NodoDecision getOrigen() {
        return origen;
    }

    public void setOrigen(NodoDecision origen) {
        this.origen = origen;
        if (origen != null) {
            this.origenId = origen.getId();
        }
    }

    public NodoDecision getDestino() {
        return destino;
    }

    public void setDestino(NodoDecision destino) {
        this.destino = destino;
        if (destino != null) {
            this.destinoId = destino.getId();
        }
    }

    public String getOrigenId() {
        if (origen != null) {
            return origen.getId();
        }
        return origenId;
    }

    public void setOrigenId(String origenId) {
        this.origenId = origenId;
    }

    public String getDestinoId() {
        if (destino != null) {
            return destino.getId();
        }
        return destinoId;
    }

    public void setDestinoId(String destinoId) {
        this.destinoId = destinoId;
    }

    public boolean isEnRutaOptima() {
        return enRutaOptima;
    }

    public void setEnRutaOptima(boolean enRutaOptima) {
        this.enRutaOptima = enRutaOptima;
    }

    @Override
    public String toString() {
        return etiqueta + " (" + (probabilidad * 100) + "%, CF: " + flujoCaja + ")";
    }
}
