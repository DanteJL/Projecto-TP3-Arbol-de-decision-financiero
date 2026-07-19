package com.financiero.decisiontree.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.financiero.decisiontree.model.ArbolDecision;

import java.io.File;
import java.io.IOException;

/**
 * Servicio para persistir y cargar los árboles de decisión en formato JSON.
 */
public class PersistenciaService {
    private final ObjectMapper mapper;

    public PersistenciaService() {
        this.mapper = new ObjectMapper();
        // Habilitar escritura bonita con sangrado
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Guarda el árbol de decisión en un archivo JSON.
     *
     * @param arbol   El árbol a guardar.
     * @param archivo El archivo de destino.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public void guardarArbol(ArbolDecision arbol, File archivo) throws IOException {
        if (arbol == null) {
            throw new IllegalArgumentException("El árbol no puede ser nulo.");
        }
        mapper.writeValue(archivo, arbol);
    }

    /**
     * Carga un árbol de decisión desde un archivo JSON y reconstruye su estructura de grafos.
     *
     * @param archivo El archivo JSON a leer.
     * @return El árbol de decisión reconstruido.
     * @throws IOException Si ocurre un error de lectura o deserialización.
     */
    public ArbolDecision cargarArbol(File archivo) throws IOException {
        if (archivo == null || !archivo.exists()) {
            throw new IOException("El archivo no existe.");
        }
        
        ArbolDecision arbol = mapper.readValue(archivo, ArbolDecision.class);
        
        // Es indispensable reconstruir los punteros de memoria
        if (arbol != null) {
            arbol.reconstruirGrafo();
        }
        
        return arbol;
    }
}
