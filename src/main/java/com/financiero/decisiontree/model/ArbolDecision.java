package com.financiero.decisiontree.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa el Árbol de Decisión Financiero como un grafo dirigido.
 */
public class ArbolDecision {
    private String raizId;
    private List<NodoDecision> listaNodos = new ArrayList<>();
    private List<AristaDecision> listaAristas = new ArrayList<>();

    @JsonIgnore
    private NodoDecision raiz;

    public ArbolDecision() {
        // Constructor vacío para Jackson
    }

    public ArbolDecision(NodoDecision raiz) {
        this.raiz = raiz;
        if (raiz != null) {
            this.raizId = raiz.getId();
            this.listaNodos.add(raiz);
        }
    }

    /**
     * Agrega un nodo al grafo.
     */
    public void addNodo(NodoDecision nodo) {
        if (nodo != null && !listaNodos.contains(nodo)) {
            listaNodos.add(nodo);
        }
    }

    /**
     * Agrega una arista al grafo.
     */
    public void addArista(AristaDecision arista) {
        if (arista != null && !listaAristas.contains(arista)) {
            listaAristas.add(arista);
        }
    }

    /**
     * Elimina un nodo y todas las aristas asociadas a él.
     */
    public void removeNodo(NodoDecision nodo) {
        if (nodo == null) return;
        listaNodos.remove(nodo);
        listaAristas.removeIf(arista -> 
            arista.getOrigenId().equals(nodo.getId()) || arista.getDestinoId().equals(nodo.getId())
        );
        if (raiz == nodo) {
            raiz = null;
            raizId = null;
        }
        reconstruirGrafo();
    }

    /**
     * Elimina una arista del grafo.
     */
    public void removeArista(AristaDecision arista) {
        if (arista == null) return;
        listaAristas.remove(arista);
        reconstruirGrafo();
    }

    /**
     * Reconstruye las referencias cruzadas de memoria (padre, hijos, origen, destino)
     * a partir de los datos cargados desde el archivo JSON.
     */
    public void reconstruirGrafo() {
        // 1. Crear un mapa para búsqueda rápida de nodos por ID
        Map<String, NodoDecision> mapaNodos = new HashMap<>();
        for (NodoDecision nodo : listaNodos) {
            nodo.getHijos().clear();
            nodo.setPadre(null);
            mapaNodos.put(nodo.getId(), nodo);
        }

        // 2. Resolver la raíz del árbol
        if (raizId != null) {
            raiz = mapaNodos.get(raizId);
        } else if (!listaNodos.isEmpty()) {
            // Si no hay raizId, por defecto la raíz es el primer nodo o el que no tenga padre
            raiz = listaNodos.get(0);
            raizId = raiz.getId();
        }

        // 3. Conectar nodos y aristas
        for (AristaDecision arista : listaAristas) {
            NodoDecision origen = mapaNodos.get(arista.getOrigenId());
            NodoDecision destino = mapaNodos.get(arista.getDestinoId());

            if (origen != null && destino != null) {
                arista.setOrigen(origen);
                arista.setDestino(destino);
                
                // Agregar destino a los hijos del origen
                if (!origen.getHijos().contains(destino)) {
                    origen.getHijos().add(destino);
                }
                // Establecer el padre del destino
                destino.setPadre(origen);
            }
        }
    }

    // Getters y Setters
    public String getRaizId() {
        if (raiz != null) {
            return raiz.getId();
        }
        return raizId;
    }

    public void setRaizId(String raizId) {
        this.raizId = raizId;
    }

    public List<NodoDecision> getListaNodos() {
        return listaNodos;
    }

    public void setListaNodos(List<NodoDecision> listaNodos) {
        this.listaNodos = listaNodos;
    }

    public List<AristaDecision> getListaAristas() {
        return listaAristas;
    }

    public void setListaAristas(List<AristaDecision> listaAristas) {
        this.listaAristas = listaAristas;
    }

    public NodoDecision getRaiz() {
        if (raiz == null && raizId != null) {
            reconstruirGrafo();
        }
        return raiz;
    }

    public void setRaiz(NodoDecision raiz) {
        this.raiz = raiz;
        if (raiz != null) {
            this.raizId = raiz.getId();
            addNodo(raiz);
        } else {
            this.raizId = null;
        }
    }

    /**
     * Retorna todas las aristas salientes de un nodo dado.
     */
    public List<AristaDecision> getAristasSalientes(NodoDecision nodo) {
        List<AristaDecision> salientes = new ArrayList<>();
        if (nodo == null) return salientes;
        for (AristaDecision arista : listaAristas) {
            if (arista.getOrigenId().equals(nodo.getId())) {
                salientes.add(arista);
            }
        }
        return salientes;
    }

    /**
     * Retorna la arista que conecta a origen con destino, si existe.
     */
    public AristaDecision getAristaEntre(NodoDecision origen, NodoDecision destino) {
        if (origen == null || destino == null) return null;
        for (AristaDecision arista : listaAristas) {
            if (arista.getOrigenId().equals(origen.getId()) && arista.getDestinoId().equals(destino.getId())) {
                return arista;
            }
        }
        return null;
    }
}
