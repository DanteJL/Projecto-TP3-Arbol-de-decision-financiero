# Árbol de Decisión Financiero para Inversiones

<div align="center">

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-FF0000?style=flat&logo=javafx&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=flat&logo=apache-maven&logoColor=white)


**Aplicación de escritorio para construir, analizar y optimizar árboles de decisión financiera con algoritmos de grafos.**

</div>

---

---

## Descripción

Una aplicación de escritorio desarrollada en **JavaFX** que permite modelar inversiones como grafos dirigidos acíclicos (DAG). Cada nodo representa una situación financiera (decisión, azar o resultado terminal) y cada arista representa una transición con probabilidad y flujo de caja asociado.

La aplicación realiza automáticamente:

| Métrica | Descripción | Algoritmo |
|---------|-------------|-----------|
| **EMV** | Valor Monetario Esperado | DFS recursivo |
| **VAN** | Valor Actual Neto con descuento | DFS + Factor de descuento |
| **Ruta Óptima** | Camino de mayor retorno | Dijkstra modificado |

---

## Características

- **Editor Visual Interactivo** — Arrastrar y soltar nodos en un lienzo de 2000x2000
- **Tres Tipos de Nodos** — Decisión, Azar y Terminal con colores diferenciados
- **Gestión Completa** — Crear, editar y eliminar nodos y aristas
- **Validación Automática** — Previene ciclos y verifica probabilidades
- **Persistencia JSON** — Guardar y cargar árboles de decisión
- **Cálculos en Tiempo Real** — EMV, VAN y ruta óptima instantáneos
- **Ruta Óptima Visual** — Resaltado naranja de la mejor inversión
- **Moneda Local** — Formato VES (Bolívares Venezolanos)
- **Interfaz Profesional** — Tema corporativo azul oscuro

---

## Requisitos Previos

| Componente | Versión Mínima | Descarga |
|------------|----------------|----------|
| **JDK** | 25+ | [Adoptium](https://adoptium.net/) / [Oracle](https://www.oracle.com/java/) |
| **Maven** | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |

### Dependencias del Sistema (Linux)

```bash
sudo apt install openjfx libopenjfx-java libgtk-3-0 libwebkit2gtk-4.0-37
```

---

## Instalación

### Opción 1: Ejecutar con Maven (Recomendado)

```bash
# 1. Clonar el repositorio
git clone https://github.com/DanteJL/Projecto-TP3-Arbol-de-decision-financiero.git

# 2. Compilar
mvn clean compile

# 3. Ejecutar
mvn javafx:run
```

### Opción 2: Generar JAR Ejecutable

```bash
# 1. Empaquetar
mvn clean package


### Opción 3: Maven Wrapper

```bash
# Linux / macOS
./mvnw javafx:run

# Windows
mvnw.cmd javafx:run
```

---

## Uso

### 1. Crear un Nodo

```
┌─────────────────────────────────────┐
│ 1. Clic en el lienzo               │
│ 2. Completar: ID, Nombre, Tipo     │
│ 3. Ingresar Flujo de Caja          │
│ 4. Clic en "Guardar/Crear"         │
└─────────────────────────────────────┘
```

### 2. Conectar Nodos (Arista)

```
┌─────────────────────────────────────┐
│ 1. Seleccionar Origen y Destino    │
│ 2. Ingresar: Etiqueta, Prob. (0-1) │
│ 3. Ingresar Flujo de Transición    │
│ 4. Clic en "Conectar"              │
└─────────────────────────────────────┘
```

### 3. Calcular Análisis Financiero

```
┌─────────────────────────────────────┐
│ 1. Ingresar Tasa de Descuento (%)  │
│ 2. Clic en "Calcular Financiero"   │
│ 3. Ver resultados en panel inferior│
└─────────────────────────────────────┘
```

### 4. Guardar / Cargar Proyecto

- **Guardar JSON** → Exporta el árbol actual
- **Cargar JSON** → Importa un árbol previamente guardado

---

## Estructura del Proyecto

```
Proyecto_Luis/
├── pom.xml                                    # Configuración Maven
├── src/main/
│   ├── java/com/financiero/decisiontree/
│   │   ├── Main.java                          # Entry point JavaFX
│   │   ├── MainLauncher.java                  # Lanzador modular
│   │   ├── controller/
│   │   │   └── MainController.java            # Controlador UI (922 líneas)
│   │   ├── model/
│   │   │   ├── ArbolDecision.java             # Modelo grafo completo
│   │   │   ├── NodoDecision.java              # Nodo con propiedades financieras
│   │   │   ├── AristaDecision.java            # Transición con probabilidad
│   │   │   └── TipoNodo.java                  # Enum: DECISION | AZAR | TERMINAL
│   │   └── service/
│   │       ├── CalculoFinanciero.java         # DFS + Dijkstra (311 líneas)
│   │       └── PersistenciaService.java       # Serialización JSON
│   └── resources/com/financiero/decisiontree/
│       ├── main.fxml                          # Layout de la interfaz
│       └── styles.css                         # Tema corporativo azul
└── target/                                    # Build output
```

---

## Algoritmos

### EMV — Valor Monetario Esperado

```java
// Evaluación recursiva de hojas a raíz (DFS post-orden)
EMV(nodo) = nodo.flujoCaja + Σ(probabilidad_rama × EMV(hijo_rama))
```

- **Nodo DECISION**: Selecciona la rama con mayor EMV
- **Nodo AZAR**: Calcula promedio ponderado por probabilidades

### VAN — Valor Actual Neto

```java
// VAN descontado con tasa configurable
VAN = Σ (flujo_t / (1 + tasa)^t)
```

### Ruta Óptima — Dijkstra Modificado

```
┌────────┐      ┌────────┐      ┌────────┐
│ RAÍZ   │─────▶│ DECISIÓN│─────▶│TERMINAL│
│ (CF:0) │      │ (CF:-5k)│      │(CF:20k)│
└────────┘      └────────┘      └────────┘
                        │
                        ▼
                ┌────────┐
                │ AZAR   │─────▶ Terminal 2
                │(CF:-3k)│       (CF:15k)
                └────────┘

Resultado: Ruta óptima = Raíz → Decisión → Terminal 1
           Retorno acumulado = $15,000
```

---

## Tecnologías

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 25 | Lenguaje principal |
| **JavaFX** | 21 | Interfaz gráfica de escritorio |
| **Jackson** | 2.17.2 | Serialización/deserialización JSON |
| **Maven** | 3.8+ | Sistema de construcción |

---


## Colores de la Interfaz

| Elemento | Color | Significado |
|----------|-------|-------------|
| 🔵 Azul oscuro | `#0D2137` | Barra principal |
| 🔵 Azul primario | `#1976D2` | Nodo de Decisión |
| 🔵 Azul claro | `#90CAF9` | Nodo de Azar |
| 🔵 Azul intenso | `#0D47A1` | Nodo Terminal |
| 🟠 Naranja | `#FF6F00` | Ruta Óptima |
| ⬜ Blanco | `#FFFFFF` | Tarjetas de nodos |

---

<div align="center">

### Hecho con ❤️ en Java

**Proyecto_Luis** © 2025

</div>
