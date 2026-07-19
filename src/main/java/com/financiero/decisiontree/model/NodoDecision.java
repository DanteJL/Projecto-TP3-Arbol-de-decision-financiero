package com.financiero.decisiontree.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un vértice (nodo) en el árbol de decisión financiera.
 */
public class NodoDecision {
    private String id;
    private String nombre;
    private TipoNodo tipo;
    private double flujoCaja;
    private double posicionX;
    private double posicionY;

    // Relaciones de grafo. Se ignoran en Jackson para evitar referencias circulares
    // y se reconstruyen dinámicamente usando las aristas.
    @JsonIgnore
    private List<NodoDecision> hijos = new ArrayList<>();
    
    @JsonIgnore
    private NodoDecision padre;

    // Valores calculados (no persistidos directamente en la estructura básica o recalculados)
    private Double emv; 
    private Double van; // Valor Actual Neto esperado desde este nodo
    private boolean enRutaOptima;

    public NodoDecision() {
        // Constructor vacío requerido por Jackson
    }

    public NodoDecision(String id, String nombre, TipoNodo tipo, double flujoCaja, double posicionX, double posicionY) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.flujoCaja = flujoCaja;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoNodo getTipo() {
        return tipo;
    }

    public void setTipo(TipoNodo tipo) {
        this.tipo = tipo;
    }

    public double getFlujoCaja() {
        return flujoCaja;
    }

    public void setFlujoCaja(double flujoCaja) {
        this.flujoCaja = flujoCaja;
    }

    public double getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(double posicionX) {
        this.posicionX = posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(double posicionY) {
        this.posicionY = posicionY;
    }

    public List<NodoDecision> getHijos() {
        return hijos;
    }

    public void setHijos(List<NodoDecision> hijos) {
        this.hijos = hijos;
    }

    public NodoDecision getPadre() {
        return padre;
    }

    public void setPadre(NodoDecision padre) {
        this.padre = padre;
    }

    public Double getEmv() {
        return emv;
    }

    public void setEmv(Double emv) {
        this.emv = emv;
    }

    public Double getVan() {
        return van;
    }

    public void setVan(Double van) {
        this.van = van;
    }

    public boolean isEnRutaOptima() {
        return enRutaOptima;
    }

    public void setEnRutaOptima(boolean enRutaOptima) {
        this.enRutaOptima = enRutaOptima;
    }

    @Override
    public String toString() {
        return nombre + " (" + tipo + ")";
    }
}
