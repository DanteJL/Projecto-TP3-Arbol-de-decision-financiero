package com.financiero.decisiontree.service;

import com.financiero.decisiontree.model.ArbolDecision;
import com.financiero.decisiontree.model.AristaDecision;
import com.financiero.decisiontree.model.NodoDecision;
import com.financiero.decisiontree.model.TipoNodo;

import java.util.*;

/**
 * Servicio encargado de realizar los cálculos de teoría de grafos y financieros:
 * - EMV (Valor Monetario Esperado) mediante DFS recursivo (post-orden)
 * - VAN (Valor Actual Neto) con tasa de descuento configurable
 * - Ruta óptima de inversión usando un algoritmo de Dijkstra modificado (maximización)
 */
public class CalculoFinanciero {

    /**
     * Calcula el Valor Monetario Esperado (EMV) para todos los nodos del árbol.
     * Utiliza Búsqueda en Profundidad (DFS) para evaluar desde las hojas hacia la raíz.
     *
     * @param arbol El árbol de decisión.
     * @return El EMV de la raíz del árbol.
     */
    public double calcularEMV(ArbolDecision arbol) {
        NodoDecision raiz = arbol.getRaiz();
        if (raiz == null) return 0.0;
        
        // Limpiar cálculos previos
        for (NodoDecision nodo : arbol.getListaNodos()) {
            nodo.setEmv(null);
            nodo.setEnRutaOptima(false);
        }
        for (AristaDecision arista : arbol.getListaAristas()) {
            arista.setEnRutaOptima(false);
        }

        return calcularEMVRec(raiz, arbol);
    }

    private double calcularEMVRec(NodoDecision nodo, ArbolDecision arbol) {
        if (nodo == null) return 0.0;

        // Caso base: Nodo terminal o sin hijos se evalúa por su propio flujo de caja
        if (nodo.getTipo() == TipoNodo.TERMINAL || nodo.getHijos().isEmpty()) {
            nodo.setEmv(nodo.getFlujoCaja());
            return nodo.getFlujoCaja();
        }

        // DFS: Evaluar recursivamente todos los hijos primero
        for (NodoDecision hijo : nodo.getHijos()) {
            calcularEMVRec(hijo, arbol);
        }

        List<AristaDecision> aristasSalientes = arbol.getAristasSalientes(nodo);
        double valorRamas = 0.0;

        if (nodo.getTipo() == TipoNodo.DECISION) {
            // Un nodo DECISIÓN elige la rama que maximiza el EMV esperado
            double maxValor = -Double.MAX_VALUE;
            for (AristaDecision arista : aristasSalientes) {
                NodoDecision hijo = arista.getDestino();
                if (hijo != null) {
                    // Valor acumulado de esta opción = flujo inmediato de la arista + EMV del hijo
                    double valorOpcion = arista.getFlujoCaja() + hijo.getEmv();
                    if (valorOpcion > maxValor) {
                        maxValor = valorOpcion;
                    }
                }
            }
            valorRamas = (maxValor == -Double.MAX_VALUE) ? 0.0 : maxValor;

        } else if (nodo.getTipo() == TipoNodo.AZAR) {
            // Un nodo AZAR calcula el promedio ponderado por probabilidades: Σ(probabilidad * valor)
            double sum = 0.0;
            double sumaProbabilidades = 0.0;
            for (AristaDecision arista : aristasSalientes) {
                NodoDecision hijo = arista.getDestino();
                if (hijo != null) {
                    sum += arista.getProbabilidad() * (arista.getFlujoCaja() + hijo.getEmv());
                    sumaProbabilidades += arista.getProbabilidad();
                }
            }
            // Si la suma de probabilidades no es exactamente 1.0, se puede alertar en la UI,
            // pero para el cálculo asumimos la ponderación directa.
            valorRamas = sum;
        }

        // El EMV del nodo es su flujo directo más el valor esperado de sus ramas
        double emvNodo = nodo.getFlujoCaja() + valorRamas;
        nodo.setEmv(emvNodo);
        return emvNodo;
    }

    /**
     * Calcula el Valor Actual Neto (VAN) esperado recursivamente para todos los nodos del árbol.
     *
     * @param arbol El árbol de decisión.
     * @param tasaDescuento Tasa de descuento anual (por ejemplo, 0.10 para el 10%).
     * @return El VAN de la raíz.
     */
    public double calcularExpectedVAN(ArbolDecision arbol, double tasaDescuento) {
        NodoDecision raiz = arbol.getRaiz();
        if (raiz == null) return 0.0;

        for (NodoDecision nodo : arbol.getListaNodos()) {
            nodo.setVan(null);
        }

        return calcularExpectedVANRec(raiz, arbol, tasaDescuento);
    }

    private double calcularExpectedVANRec(NodoDecision nodo, ArbolDecision arbol, double tasaDescuento) {
        if (nodo == null) return 0.0;

        // Caso base
        if (nodo.getTipo() == TipoNodo.TERMINAL || nodo.getHijos().isEmpty()) {
            nodo.setVan(nodo.getFlujoCaja());
            return nodo.getFlujoCaja();
        }

        // Evaluar hijos
        for (NodoDecision hijo : nodo.getHijos()) {
            calcularExpectedVANRec(hijo, arbol, tasaDescuento);
        }

        List<AristaDecision> aristasSalientes = arbol.getAristasSalientes(nodo);
        double valorRamasDescontado = 0.0;
        double factorDescuento = 1.0 / (1.0 + tasaDescuento);

        if (nodo.getTipo() == TipoNodo.DECISION) {
            double maxValor = -Double.MAX_VALUE;
            for (AristaDecision arista : aristasSalientes) {
                NodoDecision hijo = arista.getDestino();
                if (hijo != null) {
                    // Descontar la transición y el valor futuro un período
                    double valorOpcion = factorDescuento * (arista.getFlujoCaja() + hijo.getVan());
                    if (valorOpcion > maxValor) {
                        maxValor = valorOpcion;
                    }
                }
            }
            valorRamasDescontado = (maxValor == -Double.MAX_VALUE) ? 0.0 : maxValor;

        } else if (nodo.getTipo() == TipoNodo.AZAR) {
            double sum = 0.0;
            for (AristaDecision arista : aristasSalientes) {
                NodoDecision hijo = arista.getDestino();
                if (hijo != null) {
                    sum += arista.getProbabilidad() * factorDescuento * (arista.getFlujoCaja() + hijo.getVan());
                }
            }
            valorRamasDescontado = sum;
        }

        // El VAN del nodo actual es su propio flujo 
        // más el valor de ramas descontado.
        double vanNodo = nodo.getFlujoCaja() + valorRamasDescontado;
        nodo.setVan(vanNodo);
        return vanNodo;
    }

    /**
     * Calcula el VAN determinista de una ruta específica de inversión.
     *
     * @param rutaNodos Lista secuencial de nodos que forman la ruta.
     * @param arbol El árbol de decisión.
     * @param tasaDescuento Tasa de descuento anual.
     * @return El VAN acumulado de la ruta.
     */
    public double calcularVANRuta(List<NodoDecision> rutaNodos, ArbolDecision arbol, double tasaDescuento) {
        if (rutaNodos == null || rutaNodos.isEmpty()) return 0.0;

        double vanTotal = 0.0;
        for (int t = 0; t < rutaNodos.size(); t++) {
            NodoDecision nodo = rutaNodos.get(t);
            double flujoPeriodo = nodo.getFlujoCaja();

            // Si no es el nodo inicial, le sumamos el flujo de la arista de transición que lleva a él
            if (t > 0) {
                NodoDecision anterior = rutaNodos.get(t - 1);
                AristaDecision arista = arbol.getAristaEntre(anterior, nodo);
                if (arista != null) {
                    flujoPeriodo += arista.getFlujoCaja();
                }
            }

            // Descontar a tasa (1 + r)^t
            vanTotal += flujoPeriodo / Math.pow(1.0 + tasaDescuento, t);
        }
        return vanTotal;
    }

    /**
     * Algoritmo de Dijkstra adaptado para encontrar la ruta de mayor retorno (máximo flujo de caja acumulado)
     * desde la raíz del árbol hasta los nodos terminales (hojas).
     * Utiliza una PriorityQueue configurada para maximizar la distancia acumulada.
     *
     * @param arbol El árbol de decisión.
     * @return Lista de nodos en la ruta óptima desde la raíz hasta la hoja con mayor retorno.
     */
    public List<NodoDecision> encontrarRutaOptimaDijkstra(ArbolDecision arbol) {
        NodoDecision raiz = arbol.getRaiz();
        if (raiz == null) return new ArrayList<>();

        // Mapa de retorno máximo acumulado para cada nodo
        Map<String, Double> dist = new HashMap<>();
        // Mapa de predecesores para reconstruir el camino
        Map<String, NodoDecision> pred = new HashMap<>();
        // Nodos ya expandidos para evitar re-procesamientos
        Set<String> visitados = new HashSet<>();

        // Inicializar todas las distancias a -infinito
        for (NodoDecision nodo : arbol.getListaNodos()) {
            dist.put(nodo.getId(), -Double.MAX_VALUE);
        }

        // El retorno inicial en la raíz es su propio flujo de caja
        dist.put(raiz.getId(), raiz.getFlujoCaja());

        // Cola de prioridad que saca primero el nodo con mayor retorno acumulado (Max-Heap)
        PriorityQueue<NodoDecision> pq = new PriorityQueue<>((a, b) -> 
            Double.compare(dist.get(b.getId()), dist.get(a.getId()))
        );

        pq.add(raiz);

        while (!pq.isEmpty()) {
            NodoDecision u = pq.poll();
            
            if (visitados.contains(u.getId())) continue;
            visitados.add(u.getId());

            double distU = dist.get(u.getId());

            // Procesar vecinos (hijos a través de aristas salientes)
            for (AristaDecision arista : arbol.getAristasSalientes(u)) {
                NodoDecision v = arista.getDestino();
                if (v == null) continue;

                // El costo de transición es el flujo de la arista + el flujo del nodo destino
                double costeTransicion = arista.getFlujoCaja() + v.getFlujoCaja();
                double nuevoRetorno = distU + costeTransicion;

                // Si encontramos un camino que produce mayor retorno, actualizamos
                if (nuevoRetorno > dist.get(v.getId())) {
                    dist.put(v.getId(), nuevoRetorno);
                    pred.put(v.getId(), u);
                    
                    // Volver a encolar para que se actualice su prioridad
                    pq.add(v);
                }
            }
        }

        // Buscar el nodo terminal con el máximo retorno acumulado
        NodoDecision terminalOptimo = null;
        double maxRetorno = -Double.MAX_VALUE;

        for (NodoDecision nodo : arbol.getListaNodos()) {
            if (nodo.getTipo() == TipoNodo.TERMINAL) {
                double d = dist.get(nodo.getId());
                if (d > maxRetorno) {
                    maxRetorno = d;
                    terminalOptimo = nodo;
                }
            }
        }

        // Fallback: Si no hay nodos marcados como TERMINAL, buscamos entre las hojas físicas
        if (terminalOptimo == null) {
            for (NodoDecision nodo : arbol.getListaNodos()) {
                if (nodo.getHijos().isEmpty()) {
                    double d = dist.get(nodo.getId());
                    if (d > maxRetorno) {
                        maxRetorno = d;
                        terminalOptimo = nodo;
                    }
                }
            }
        }

        // Reconstruir la ruta (desde el nodo terminal óptimo de regreso hasta la raíz)
        List<NodoDecision> ruta = new ArrayList<>();
        if (terminalOptimo != null) {
            NodoDecision actual = terminalOptimo;
            while (actual != null) {
                ruta.add(0, actual); // Insertar al principio para obtener orden Raíz -> Terminal
                actual = pred.get(actual.getId());
            }
        }

        // Marcar la ruta óptima en el modelo para facilitar la visualización en la UI
        if (!ruta.isEmpty()) {
            for (int i = 0; i < ruta.size(); i++) {
                NodoDecision nodo = ruta.get(i);
                nodo.setEnRutaOptima(true);
                
                if (i > 0) {
                    NodoDecision anterior = ruta.get(i - 1);
                    AristaDecision arista = arbol.getAristaEntre(anterior, nodo);
                    if (arista != null) {
                        arista.setEnRutaOptima(true);
                    }
                }
            }
        }

        return ruta;
    }
}
