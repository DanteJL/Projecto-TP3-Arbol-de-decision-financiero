package com.financiero.decisiontree.controller;

import com.financiero.decisiontree.model.ArbolDecision;
import com.financiero.decisiontree.model.AristaDecision;
import com.financiero.decisiontree.model.NodoDecision;
import com.financiero.decisiontree.model.TipoNodo;
import com.financiero.decisiontree.service.CalculoFinanciero;
import com.financiero.decisiontree.service.PersistenciaService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

/**
 * Controlador de la interfaz gráfica de usuario.
 */
public class MainController {

    // Controles FXML
    @FXML private Button btnNuevo;
    @FXML private Button btnCargar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCalcular;
    @FXML private Button btnEliminarNodo;
    @FXML private Button btnGuardarNodo;
    @FXML private Button btnEliminarArista;
    @FXML private Button btnGuardarArista;

    @FXML private TextField txtTasaDescuento;
    @FXML private TextField txtNodoId;
    @FXML private TextField txtNodoNombre;
    @FXML private TextField txtNodoFlujo;
    @FXML private TextField txtAristaEtiqueta;
    @FXML private TextField txtAristaProbabilidad;
    @FXML private TextField txtAristaFlujo;

    @FXML private ComboBox<TipoNodo> cbNodoTipo;
    @FXML private ComboBox<NodoDecision> cbAristaOrigen;
    @FXML private ComboBox<NodoDecision> cbAristaDestino;

    @FXML private Label lblSeleccionInfo;
    @FXML private Label lblEmvTotal;
    @FXML private Label lblVanTotal;
    @FXML private Label lblVanDetalle;
    @FXML private Label lblRutaOptima;
    @FXML private Label lblRutaOptimaCosto;

    @FXML private ScrollPane scrollCanvas;
    @FXML private AnchorPane canvasAnchorPane;
    @FXML private Pane canvasPane;

    // Servicios y Modelo
    private final CalculoFinanciero calculoFinanciero = new CalculoFinanciero();
    private final PersistenciaService persistenciaService = new PersistenciaService();
    private ArbolDecision arbol = new ArbolDecision();

    // Estado del Editor
    private Object elementoSeleccionado = null; // NodoDecision o AristaDecision
    private double clickX = 150.0;
    private double clickY = 150.0;

    // Constantes de diseño para las tarjetas de los nodos
    private final double CARD_WIDTH = 150.0;
    private final double CARD_HEIGHT = 100.0;

    // Delta auxiliar para arrastre de nodos
    private static class Delta {
        double x, y;
    }
    private final Delta dragDelta = new Delta();

    // Formateador de moneda e internacionalización local
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "VE"));

    @FXML
    public void initialize() {
        // Inicializar ComboBoxes
        cbNodoTipo.setItems(FXCollections.observableArrayList(TipoNodo.values()));
        cbNodoTipo.setValue(TipoNodo.DECISION);

        // Configurar el Canvas Pane para poder expandirse
        canvasPane.setPrefWidth(2000);
        canvasPane.setPrefHeight(2000);

        // Deshabilitar botones de eliminación al inicio
        btnEliminarNodo.setDisable(true);
        btnEliminarArista.setDisable(true);
    }

    /**
     * Dibuja los nodos y aristas en el lienzo interactivo.
     */
    private void redibujarGrafo() {
        canvasPane.getChildren().clear();

        // 1. Dibujar Aristas primero (para que queden debajo de las tarjetas de los nodos)
        for (AristaDecision arista : arbol.getListaAristas()) {
            dibujarAristaVisual(arista);
        }

        // 2. Dibujar Nodos (tarjetas interactivas)
        for (NodoDecision nodo : arbol.getListaNodos()) {
            dibujarNodoVisual(nodo);
        }

        // Actualizar listas de selección en los comboboxes de aristas
        actualizarComboBoxesNodos();
    }

    /**
     * Dibuja una arista con su línea, punta de flecha y etiqueta con probabilidad/costo.
     */
    private void dibujarAristaVisual(AristaDecision arista) {
        NodoDecision origen = arista.getOrigen();
        NodoDecision destino = arista.getDestino();

        if (origen == null || destino == null) return;

        // Calcular puntos de conexión (por defecto de lado derecho a lado izquierdo en árbol horizontal)
        double startX = origen.getPosicionX() + CARD_WIDTH;
        double startY = origen.getPosicionY() + (CARD_HEIGHT / 2);
        double endX = destino.getPosicionX();
        double endY = destino.getPosicionY() + (CARD_HEIGHT / 2);

        // Ajuste en caso de que las posiciones estén invertidas horizontalmente
        if (destino.getPosicionX() < origen.getPosicionX()) {
            startX = origen.getPosicionX();
            endX = destino.getPosicionX() + CARD_WIDTH;
        }

        // Crear la línea
        Line linea = new Line(startX, startY, endX, endY);
        
        // Estilo de la línea
        if (arista.isEnRutaOptima()) {
            linea.getStyleClass().add("canvas-edge-optimo");
            linea.setStroke(Color.web("#FF6F00"));
            linea.setStrokeWidth(4.0);
        } else {
            linea.getStyleClass().add("canvas-edge");
            linea.setStroke(Color.web("#607D8B"));
            linea.setStrokeWidth(2.0);
        }

        // Añadir efecto de glow si es la ruta óptima
        if (arista.isEnRutaOptima()) {
            DropShadow glow = new DropShadow(8, Color.web("#FF6F00"));
            linea.setEffect(glow);
        }

        // Dibujar punta de flecha (ArrowHead)
        double angle = Math.atan2(endY - startY, endX - startX);
        double arrowLength = 12.0;
        double x1 = endX - arrowLength * Math.cos(angle - Math.PI / 8);
        double y1 = endY - arrowLength * Math.sin(angle - Math.PI / 8);
        double x2 = endX - arrowLength * Math.cos(angle + Math.PI / 8);
        double y2 = endY - arrowLength * Math.sin(angle + Math.PI / 8);

        Polygon flecha = new Polygon(endX, endY, x1, y1, x2, y2);
        if (arista.isEnRutaOptima()) {
            flecha.setFill(Color.web("#FF6F00"));
        } else {
            flecha.setFill(Color.web("#607D8B"));
        }

        // Crear etiqueta informativa para la arista
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;

        VBox boxEtiqueta = new VBox(2);
        boxEtiqueta.setAlignment(Pos.CENTER);
        boxEtiqueta.setStyle("-fx-background-color: rgba(240, 244, 248, 0.95); " +
                            "-fx-border-color: #90A4AE; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 4px; " +
                            "-fx-background-radius: 4px; " +
                            "-fx-padding: 4px;");

        Text txtLabel = new Text(arista.getEtiqueta());
        txtLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        txtLabel.setFill(Color.web("#0D2137"));
        boxEtiqueta.getChildren().add(txtLabel);

        // Añadir información numérica de la rama
        StringBuilder detalle = new StringBuilder();
        if (origen.getTipo() == TipoNodo.AZAR) {
            detalle.append(String.format("P: %.0f%%", arista.getProbabilidad() * 100));
        }
        if (arista.getFlujoCaja() != 0) {
            if (detalle.length() > 0) detalle.append(" | ");
            detalle.append("CF: ").append(formatoMoneda.format(arista.getFlujoCaja()));
        }

        if (detalle.length() > 0) {
            Text txtDetalle = new Text(detalle.toString());
            txtDetalle.setFont(Font.font("Segoe UI", 9));
            txtDetalle.setFill(Color.web("#546E7A"));
            boxEtiqueta.getChildren().add(txtDetalle);
        }

        // Posicionar etiqueta en el medio
        boxEtiqueta.setLayoutX(midX - 50);
        boxEtiqueta.setLayoutY(midY - 18);
        boxEtiqueta.setPrefWidth(100);

        // Manejar selección al hacer click en la línea o la etiqueta
        boxEtiqueta.setOnMouseClicked(e -> {
            seleccionarArista(arista);
            e.consume();
        });
        linea.setOnMouseClicked(e -> {
            seleccionarArista(arista);
            e.consume();
        });

        // Configurar cursor sobre la arista
        boxEtiqueta.setCursor(Cursor.HAND);
        linea.setCursor(Cursor.HAND);

        // Agregar al lienzo
        canvasPane.getChildren().addAll(linea, flecha, boxEtiqueta);
    }

    /**
     * Dibuja una tarjeta interactiva para el nodo.
     */
    private void dibujarNodoVisual(NodoDecision nodo) {
        // VBox contenedor de la tarjeta del nodo
        VBox card = new VBox();
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setLayoutX(nodo.getPosicionX());
        card.setLayoutY(nodo.getPosicionY());
        card.setAlignment(Pos.TOP_LEFT);

        // Fondo y bordes de la tarjeta
        String borderHex = nodo.isEnRutaOptima() ? "#FF6F00" : "#0D2137";
        double borderWidth = nodo.isEnRutaOptima() ? 3.0 : 1.5;
        
        card.setStyle(String.format(
            "-fx-background-color: #FFFFFF; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: %.1fpx; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 0 0 5px 0; " +
            "-fx-overflow: hidden;"
            , borderHex, borderWidth
        ));

        // 1. Barra de Tipo de Nodo superior (Header Strip)
        Pane headerStrip = new Pane();
        headerStrip.setPrefHeight(10.0);
        
        String colorHeader;
        switch (nodo.getTipo()) {
            case DECISION -> colorHeader = "#1976D2"; // Azul primario
            case AZAR -> colorHeader = "#90CAF9";     // Azul claro
            case TERMINAL -> colorHeader = "#0D47A1"; // Azul oscuro
            default -> colorHeader = "#90A4AE";
        }
        headerStrip.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 6px 6px 0px 0px;", colorHeader
        ));
        card.getChildren().add(headerStrip);

        // 2. Contenido de la Tarjeta
        VBox contentBox = new VBox(4);
        contentBox.setStyle("-fx-padding: 6px 8px 6px 8px;");

        // Nombre del Nodo
        Label lblNombre = new Label(nodo.getNombre());
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblNombre.setTextFill(Color.web("#0D2137"));
        lblNombre.setWrapText(true);
        lblNombre.setMaxHeight(32);

        // Datos básicos (Flujo de caja)
        Label lblInfoBasica = new Label(String.format("%s | CF: %s", 
            nodo.getTipo().toString(), 
            formatoMoneda.format(nodo.getFlujoCaja())
        ));
        lblInfoBasica.setFont(Font.font("Segoe UI", 9));
        lblInfoBasica.setTextFill(Color.web("#546E7A"));

        contentBox.getChildren().addAll(lblNombre, lblInfoBasica);

        // 3. Resultados calculados (si están disponibles)
        if (nodo.getEmv() != null) {
            Label lblEmv = new Label("EMV: " + formatoMoneda.format(nodo.getEmv()));
            lblEmv.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            lblEmv.setTextFill(Color.web("#E65100")); // Naranja oscuro para EMV
            contentBox.getChildren().add(lblEmv);
        }

        if (nodo.getVan() != null) {
            Label lblVan = new Label("VAN: " + formatoMoneda.format(nodo.getVan()));
            lblVan.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            lblVan.setTextFill(Color.web("#1565C0")); // Azul para VAN
            contentBox.getChildren().add(lblVan);
        }

        card.getChildren().add(contentBox);

        // 4. Efectos visuales de estado (Selección, Ruta Óptima)
        DropShadow ds = new DropShadow();
        if (nodo == elementoSeleccionado) {
            ds.setColor(Color.web("#1976D2"));
            ds.setRadius(12);
            ds.setSpread(0.4);
            card.setEffect(ds);
        } else if (nodo.isEnRutaOptima()) {
            ds.setColor(Color.web("#FF6F00"));
            ds.setRadius(10);
            ds.setSpread(0.2);
            card.setEffect(ds);
        }

        // 5. EVENTOS INTERACTIVOS (Click y Arrastre/Drag)
        card.setCursor(Cursor.HAND);

        card.setOnMousePressed(e -> {
            card.toFront();
            dragDelta.x = card.getLayoutX() - e.getScreenX();
            dragDelta.y = card.getLayoutY() - e.getScreenY();
            seleccionarNodo(nodo);
            e.consume();
        });

        card.setOnMouseDragged(e -> {
            double newX = e.getScreenX() + dragDelta.x;
            double newY = e.getScreenY() + dragDelta.y;

            // Mantener dentro de las dimensiones lógicas del lienzo (2000x2000)
            newX = Math.max(0, Math.min(2000 - CARD_WIDTH, newX));
            newY = Math.max(0, Math.min(2000 - CARD_HEIGHT, newY));

            card.setLayoutX(newX);
            card.setLayoutY(newY);

            // Guardar posición en el modelo
            nodo.setPosicionX(newX);
            nodo.setPosicionY(newY);

            // Redibujar el grafo dinámicamente en tiempo real (mueve las aristas)
            redibujarGrafo();
            e.consume();
        });

        // Añadir tarjeta al Pane
        canvasPane.getChildren().add(card);
    }

    /**
     * Selecciona un nodo en la UI y actualiza el formulario.
     */
    private void seleccionarNodo(NodoDecision nodo) {
        elementoSeleccionado = nodo;
        
        lblSeleccionInfo.setText(String.format("Nodo seleccionado:\n- ID: %s\n- Nombre: %s\n- Tipo: %s\n- Flujo: %s\n- Posición: (%.0f, %.0f)",
            nodo.getId(),
            nodo.getNombre(),
            nodo.getTipo(),
            formatoMoneda.format(nodo.getFlujoCaja()),
            nodo.getPosicionX(),
            nodo.getPosicionY()
        ));

        // Rellenar formulario del nodo
        txtNodoId.setText(nodo.getId());
        txtNodoId.setDisable(true); // El ID es primario, no editable directamente
        txtNodoNombre.setText(nodo.getNombre());
        cbNodoTipo.setValue(nodo.getTipo());
        txtNodoFlujo.setText(String.valueOf(nodo.getFlujoCaja()));

        btnEliminarNodo.setDisable(false);

        // Desactivar selección de arista
        btnEliminarArista.setDisable(true);

        // Resaltar visualmente redibujando
        redibujarGrafo();
    }

    /**
     * Selecciona una arista en la UI y actualiza el formulario.
     */
    private void seleccionarArista(AristaDecision arista) {
        elementoSeleccionado = arista;

        lblSeleccionInfo.setText(String.format("Arista seleccionada:\n- Origen: %s\n- Destino: %s\n- Etiqueta: %s\n- Probabilidad: %.2f\n- Flujo Transición: %s",
            arista.getOrigen().getNombre(),
            arista.getDestino().getNombre(),
            arista.getEtiqueta(),
            arista.getProbabilidad(),
            formatoMoneda.format(arista.getFlujoCaja())
        ));

        // Rellenar formulario de la arista
        cbAristaOrigen.setValue(arista.getOrigen());
        cbAristaDestino.setValue(arista.getDestino());
        txtAristaEtiqueta.setText(arista.getEtiqueta());
        txtAristaProbabilidad.setText(String.valueOf(arista.getProbabilidad()));
        txtAristaFlujo.setText(String.valueOf(arista.getFlujoCaja()));

        btnEliminarArista.setDisable(false);

        // Desactivar selección de nodo
        btnEliminarNodo.setDisable(true);
        txtNodoId.setDisable(false);
        txtNodoId.clear();
        txtNodoNombre.clear();
        txtNodoFlujo.clear();

        // Resaltar visualmente redibujando
        redibujarGrafo();
    }

    /**
     * Limpia la selección actual del editor.
     */
    private void deseleccionarTodo() {
        elementoSeleccionado = null;
        lblSeleccionInfo.setText("Ningún elemento seleccionado");
        
        txtNodoId.setDisable(false);
        txtNodoId.clear();
        txtNodoNombre.clear();
        txtNodoFlujo.clear();

        cbAristaOrigen.setValue(null);
        cbAristaDestino.setValue(null);
        txtAristaEtiqueta.clear();
        txtAristaProbabilidad.clear();
        txtAristaFlujo.clear();

        btnEliminarNodo.setDisable(true);
        btnEliminarArista.setDisable(true);

        redibujarGrafo();
    }

    /**
     * Actualiza los comboboxes de origen y destino con la lista de nodos.
     */
    private void actualizarComboBoxesNodos() {
        List<NodoDecision> nodos = arbol.getListaNodos();
        cbAristaOrigen.setItems(FXCollections.observableArrayList(nodos));
        cbAristaDestino.setItems(FXCollections.observableArrayList(nodos));
    }

    // ACCIONES DE BOTONES

    @FXML
    private void handleNuevo() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Nuevo Árbol");
        confirmacion.setHeaderText("¿Estás seguro de que quieres crear un nuevo árbol?");
        confirmacion.setContentText("Se perderán todos los cambios no guardados del diseño actual.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            arbol = new ArbolDecision();
            deseleccionarTodo();
            limpiarResultados();
            redibujarGrafo();
        }
    }

    @FXML
    private void handleCargar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Árbol de Decisión Financiero");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos JSON (*.json)", "*.json"));
        
        Stage stage = (Stage) btnCargar.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                arbol = persistenciaService.cargarArbol(file);
                deseleccionarTodo();
                limpiarResultados();
                redibujarGrafo();
                
                // Ejecutar cálculo automático al cargar
                handleCalcular();
                
                mostrarInformacion("Carga Exitosa", "El árbol se ha cargado correctamente y se han ejecutado los cálculos financieros.");
            } catch (Exception e) {
                mostrarError("Error de Carga", "No se pudo leer el archivo JSON: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGuardar() {
        if (arbol.getListaNodos().isEmpty()) {
            mostrarAdvertencia("Árbol Vacío", "No hay nodos en el árbol para guardar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Árbol de Decisión Financiero");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos JSON (*.json)", "*.json"));
        fileChooser.setInitialFileName("arbol_inversion.json");
        
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                // Sincronizar el ID de la raíz antes de guardar
                if (arbol.getRaiz() != null) {
                    arbol.setRaizId(arbol.getRaiz().getId());
                }
                persistenciaService.guardarArbol(arbol, file);
                mostrarInformacion("Guardado Exitoso", "El árbol se ha guardado correctamente.");
            } catch (IOException e) {
                mostrarError("Error al Guardar", "No se pudo escribir en el archivo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCanvasClicked(javafx.scene.input.MouseEvent event) {
        // Si hace click directamente en el lienzo de fondo con click izquierdo
        if (event.getButton() == MouseButton.PRIMARY && event.getTarget() == canvasAnchorPane) {
            deseleccionarTodo();
            // Capturar la posición del click para el nuevo nodo
            clickX = event.getX();
            clickY = event.getY();
            
            // Sugerir ID autogenerado
            txtNodoId.setText("n_" + (arbol.getListaNodos().size() + 1));
            txtNodoNombre.setText("Nuevo Nodo (" + (int)clickX + "," + (int)clickY + ")");
            txtNodoFlujo.setText("0");
            cbNodoTipo.setValue(TipoNodo.DECISION);
            
            mostrarMensajeBarra(String.format("Coordenadas del lienzo capturadas: X: %.0f, Y: %.0f. Configura el nodo en el panel lateral.", clickX, clickY));
        }
    }

    @FXML
    private void handleCalcular() {
        if (arbol.getRaiz() == null) {
            mostrarAdvertencia("Sin Raíz", "El árbol debe tener configurado un nodo raíz antes de calcular.");
            return;
        }

        // Obtener la tasa de descuento
        double tasa = 0.10;
        try {
            tasa = Double.parseDouble(txtTasaDescuento.getText().trim()) / 100.0;
            if (tasa < 0 || tasa > 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Tasa Inválida", "La tasa de descuento debe ser un porcentaje positivo (e.g. 10). Se usará 10% por defecto.");
            txtTasaDescuento.setText("10");
            tasa = 0.10;
        }

        // Validar consistencia de probabilidades en los nodos AZAR
        validarProbabilidadesNodosAzar();

        try {
            // 1. Calcular EMV (DFS)
            double emvRaiz = calculoFinanciero.calcularEMV(arbol);
            lblEmvTotal.setText(formatoMoneda.format(emvRaiz));

            // 2. Calcular VAN Esperado
            double vanRaiz = calculoFinanciero.calcularExpectedVAN(arbol, tasa);
            lblVanTotal.setText(formatoMoneda.format(vanRaiz));
            lblVanDetalle.setText(String.format("Tasa de Descuento: %.1f%%", tasa * 100.0));

            // 3. Encontrar Ruta Óptima (Dijkstra adaptado)
            List<NodoDecision> ruta = calculoFinanciero.encontrarRutaOptimaDijkstra(arbol);

            if (!ruta.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ruta.size(); i++) {
                    if (i > 0) sb.append(" → ");
                    sb.append(ruta.get(i).getNombre());
                }
                lblRutaOptima.setText(sb.toString());

                // Calcular el VAN específico de esta ruta determinista
                double vanRuta = calculoFinanciero.calcularVANRuta(ruta, arbol, tasa);
                lblRutaOptimaCosto.setText(String.format("Retorno Acumulado: %s | VAN Ruta: %s", 
                    formatoMoneda.format(ruta.get(ruta.size() - 1).getEmv()), // El emv final acumulado sin descuento
                    formatoMoneda.format(vanRuta)
                ));
            } else {
                lblRutaOptima.setText("Ruta no disponible.");
                lblRutaOptimaCosto.setText("");
            }

            // Redibujar para mostrar los valores calculados en las tarjetas
            redibujarGrafo();
            mostrarMensajeBarra("Cálculos finalizados correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error en Cálculo", "Ha ocurrido un error durante la ejecución de los algoritmos: " + e.getMessage());
        }
    }

    /**
     * Valida que las probabilidades salientes de cada nodo AZAR sumen exactamente 1.0 (margen 0.01).
     * Muestra una advertencia si hay alguna discrepancia.
     */
    private void validarProbabilidadesNodosAzar() {
        List<String> advertencias = new ArrayList<>();
        
        for (NodoDecision nodo : arbol.getListaNodos()) {
            if (nodo.getTipo() == TipoNodo.AZAR) {
                List<AristaDecision> salientes = arbol.getAristasSalientes(nodo);
                if (salientes.isEmpty()) {
                    advertencias.add(String.format("El nodo azar '%s' no tiene ramas salientes.", nodo.getNombre()));
                } else {
                    double suma = salientes.stream().mapToDouble(AristaDecision::getProbabilidad).sum();
                    if (Math.abs(suma - 1.0) > 0.001) {
                        advertencias.add(String.format("Las probabilidades del nodo azar '%s' suman %.0f%% (deben sumar 100%%).", 
                            nodo.getNombre(), suma * 100.0));
                    }
                }
            }
        }

        if (!advertencias.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia de Probabilidad");
            alert.setHeaderText("Inconsistencia en Probabilidades Detectada");
            
            StringBuilder sb = new StringBuilder("Por favor revise los siguientes nodos:\n");
            for (String adv : advertencias) {
                sb.append("- ").append(adv).append("\n");
            }
            alert.setContentText(sb.toString());
            alert.showAndWait();
        }
    }

    // FORMULARIO NODO: GUARDAR Y ELIMINAR

    @FXML
    private void handleGuardarNodo() {
        String id = txtNodoId.getText().trim();
        String nombre = txtNodoNombre.getText().trim();
        TipoNodo tipo = cbNodoTipo.getValue();
        String flujoStr = txtNodoFlujo.getText().trim();

        if (id.isEmpty() || nombre.isEmpty() || flujoStr.isEmpty()) {
            mostrarAdvertencia("Campos Vacíos", "Todos los campos del nodo son obligatorios.");
            return;
        }

        double flujo;
        try {
            flujo = Double.parseDouble(flujoStr);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Flujo Inválido", "El flujo de caja debe ser un valor numérico (e.g. -2500 o 5000.50).");
            return;
        }

        // Caso: Editando un nodo existente
        if (elementoSeleccionado instanceof NodoDecision && ((NodoDecision) elementoSeleccionado).getId().equals(id)) {
            NodoDecision nodoEditado = (NodoDecision) elementoSeleccionado;
            nodoEditado.setNombre(nombre);
            nodoEditado.setTipo(tipo);
            nodoEditado.setFlujoCaja(flujo);
            mostrarMensajeBarra("Nodo actualizado.");
        } else {
            // Caso: Crear un nodo nuevo
            // Validar que el ID no esté repetido
            boolean idExiste = arbol.getListaNodos().stream().anyMatch(n -> n.getId().equalsIgnoreCase(id));
            if (idExiste) {
                mostrarAdvertencia("ID Duplicado", "Ya existe un nodo con el ID '" + id + "'. Usa otro ID único.");
                return;
            }

            NodoDecision nuevoNodo = new NodoDecision(id, nombre, tipo, flujo, clickX, clickY);
            
            // Si es el primer nodo que se crea, se convierte automáticamente en raíz
            if (arbol.getListaNodos().isEmpty()) {
                arbol.setRaiz(nuevoNodo);
                mostrarMensajeBarra("Se creó el nodo raíz del árbol.");
            } else {
                arbol.addNodo(nuevoNodo);
                mostrarMensajeBarra("Nodo creado en las coordenadas: (" + (int)clickX + ", " + (int)clickY + ").");
            }
            
            // Colocar por defecto las siguientes coordenadas desplazadas para que no se superpongan
            clickX += 100;
            if (clickX > 1800) {
                clickX = 150;
                clickY += 120;
            }
        }

        arbol.reconstruirGrafo();
        deseleccionarTodo();
        redibujarGrafo();
    }

    @FXML
    private void handleEliminarNodo() {
        if (!(elementoSeleccionado instanceof NodoDecision)) return;
        NodoDecision nodo = (NodoDecision) elementoSeleccionado;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Nodo");
        confirm.setHeaderText("¿Seguro que deseas eliminar el nodo '" + nodo.getNombre() + "'?");
        confirm.setContentText("Esto también eliminará todas las aristas (conexiones) asociadas a él.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            arbol.removeNodo(nodo);
            deseleccionarTodo();
            limpiarResultados();
            redibujarGrafo();
            mostrarMensajeBarra("Nodo eliminado.");
        }
    }

    // FORMULARIO ARISTA: GUARDAR Y ELIMINAR

    @FXML
    private void handleGuardarArista() {
        NodoDecision origen = cbAristaOrigen.getValue();
        NodoDecision destino = cbAristaDestino.getValue();
        String etiqueta = txtAristaEtiqueta.getText().trim();
        String probStr = txtAristaProbabilidad.getText().trim();
        String flujoStr = txtAristaFlujo.getText().trim();

        if (origen == null || destino == null || etiqueta.isEmpty() || probStr.isEmpty() || flujoStr.isEmpty()) {
            mostrarAdvertencia("Campos Vacíos", "Todos los campos de la arista son obligatorios.");
            return;
        }

        if (origen.getId().equals(destino.getId())) {
            mostrarAdvertencia("Ciclo Inválido", "No se puede conectar un nodo consigo mismo.");
            return;
        }

        double probabilidad;
        try {
            probabilidad = Double.parseDouble(probStr);
            if (probabilidad < 0.0 || probabilidad > 1.0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Probabilidad Inválida", "La probabilidad debe ser un número entre 0.0 y 1.0 (e.g. 0.6 para 60%).");
            return;
        }

        double flujo;
        try {
            flujo = Double.parseDouble(flujoStr);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Flujo Inválido", "El flujo de caja de la arista debe ser numérico.");
            return;
        }

        // Validar ciclos (aunque es un árbol dirigido, garantizamos que sea un DAG simple)
        if (detectarCicloAlConectar(origen, destino)) {
            mostrarAdvertencia("Conexión Inválida", "Conectar esta arista produciría un ciclo en el grafo. Los árboles deben ser acíclicos.");
            return;
        }

        // Caso: Editando arista seleccionada
        if (elementoSeleccionado instanceof AristaDecision) {
            AristaDecision aristaEdit = (AristaDecision) elementoSeleccionado;
            aristaEdit.setOrigen(origen);
            aristaEdit.setDestino(destino);
            aristaEdit.setEtiqueta(etiqueta);
            aristaEdit.setProbabilidad(probabilidad);
            aristaEdit.setFlujoCaja(flujo);
            mostrarMensajeBarra("Conexión actualizada.");
        } else {
            // Caso: Crear nueva arista
            // Validar que no exista ya esa arista específica
            boolean existe = arbol.getListaAristas().stream().anyMatch(a -> 
                a.getOrigenId().equals(origen.getId()) && a.getDestinoId().equals(destino.getId()));
            if (existe) {
                mostrarAdvertencia("Conexión Existente", "Ya existe una conexión directa entre estos dos nodos.");
                return;
            }

            // Validar que el destino no tenga ya un padre (cada nodo en un árbol tiene a lo sumo 1 padre, excepto raíz)
            boolean tienePadre = arbol.getListaAristas().stream().anyMatch(a -> a.getDestinoId().equals(destino.getId()));
            if (tienePadre) {
                mostrarAdvertencia("Estructura de Árbol Violada", "El nodo destino '" + destino.getNombre() + "' ya tiene una rama que le llega. En un árbol, cada nodo tiene un único padre.");
                return;
            }

            String aristaId = "e_" + origen.getId() + "_" + destino.getId();
            AristaDecision nuevaArista = new AristaDecision(aristaId, origen, destino, probabilidad, flujo, etiqueta);
            arbol.addArista(nuevaArista);
            mostrarMensajeBarra("Nodos conectados con éxito.");
        }

        arbol.reconstruirGrafo();
        deseleccionarTodo();
        redibujarGrafo();
    }

    @FXML
    private void handleEliminarArista() {
        if (!(elementoSeleccionado instanceof AristaDecision)) return;
        AristaDecision arista = (AristaDecision) elementoSeleccionado;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Arista");
        confirm.setHeaderText("¿Seguro que deseas eliminar la rama seleccionada?");
        confirm.setContentText("Esto romperá la conexión entre '" + arista.getOrigen().getNombre() + "' y '" + arista.getDestino().getNombre() + "'.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            arbol.removeArista(arista);
            deseleccionarTodo();
            limpiarResultados();
            redibujarGrafo();
            mostrarMensajeBarra("Rama eliminada.");
        }
    }

    /**
     * Verifica si se crearía un ciclo al agregar una arista de origen a destino.
     */
    private boolean detectarCicloAlConectar(NodoDecision origen, NodoDecision destino) {
        // En un árbol dirigido, hay ciclo si desde 'destino' se puede llegar a 'origen'
        if (origen.getId().equals(destino.getId())) return true;
        
        // Búsqueda de camino (BFS)
        Queue<NodoDecision> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        
        queue.add(destino);
        visited.add(destino.getId());
        
        while (!queue.isEmpty()) {
            NodoDecision u = queue.poll();
            if (u.getId().equals(origen.getId())) {
                return true; // Encontramos un camino de regreso
            }
            
            for (NodoDecision v : u.getHijos()) {
                if (!visited.contains(v.getId())) {
                    visited.add(v.getId());
                    queue.add(v);
                }
            }
        }
        return false;
    }

    // AYUDAS DE INTERFAZ DE USUARIO Y LIMPIEZA

    private void limpiarResultados() {
        lblEmvTotal.setText("$ 0.00");
        lblVanTotal.setText("$ 0.00");
        lblRutaOptima.setText("No calculada.");
        lblRutaOptimaCosto.setText("");
    }

    private void mostrarMensajeBarra(String msg) {
        // Usar la etiqueta de selección temporalmente o imprimir en consola
        System.out.println("Log: " + msg);
        lblSeleccionInfo.setText(msg);
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
