# Decisiones de diseño — Semana 3

## 1. Punto de entrada

El proyecto mantiene un único punto de entrada:

```text
IngestaSensores.main()
```

No se crean aplicaciones independientes por semana.

`BancoDePruebas` es una clase auxiliar y no contiene `main`.

## 2. Búsqueda por timestamp

Se utilizan dos estrategias:

- Búsqueda lineal: no requiere ordenamiento.
- Búsqueda binaria: requiere que el arreglo esté ordenado por timestamp.

Los datos sintéticos de `GeneradorDatos` se generan en orden cronológico, por lo que la búsqueda binaria por timestamp cumple su precondición.

## 3. Búsqueda por PM2.5

No se asume que los datos estén ordenados por PM2.5.

Por tanto, la búsqueda binaria por PM2.5 se conserva como experimento para demostrar el efecto de una precondición incumplida.

## 4. Comparación de String

Los identificadores de estación se comparan mediante:

```java
equals()
```

y no mediante:

```java
==
```

porque se necesita comparar contenido.

## 5. Medición

La comparación principal entre algoritmos utiliza el número de comparaciones.

El tiempo en milisegundos se conserva como evidencia experimental, pero no es la única medida utilizada.

## 6. Evolución del proyecto

La Semana 3 agrega una nueva capacidad a la misma plataforma:

```text
Sensores
   ↓
Ingesta
   ↓
Repositorio
   ↓
Búsqueda
   ↓
Medición de eficiencia
```

La Semana 4 podrá extender esta misma arquitectura para estudiar ordenamiento.


## 7. Módulo Integrador: Gestor de Red y Java Collections Framework (JCF)

Para la integración final de la red de monitoreo IoT mediante colecciones estándar de Java, se adoptaron las siguientes decisiones de ingeniería:

### 7.1 Programación contra interfaces
Se sigue la Regla de Oro del JCF: los campos se tipifican con las interfaces (`List`, `Queue`, `Deque`, `Map`) y no contra las clases concretas (`ArrayList`, `ArrayDeque`, `HashMap`). Esto desacopla el contrato del sistema de la implementación subyacente.

### 7.2 Historial de Eventos (`historialEventos`)
- **Estructura elegida:** `LinkedList<String>` (implementando `List<String>`).
- **Justificación:** La optimización periódica (`limpiarHistorialAntiguo`) remueve recurrentemente los registros más antiguos situados en el índice 0. En una lista enlazada esta operación toma tiempo $O(1)$, evitando el desplazamiento en bloque de celdas en memoria de costo $O(n)$ propio de un `ArrayList`.

### 7.3 Navegación de Alertas (`navegadorAlertas`)
- **Estructura elegida:** `ArrayDeque<String>` (implementando `Deque<String>`).
- **Justificación:** Provee operaciones en ambos extremos (`pollFirst`, `addLast`, `pollLast`, `addFirst`) con costo garantizado $O(1)$ sin sobrecarga de sincronización, permitiendo una navegación bidireccional continua ("Adelante" y "Atrás") análoga al comportamiento de una lista doblemente enlazada (`NodoDoble`).

### 7.4 Ciclo de Monitoreo (`colaMonitoreo`)
- **Estructura elegida:** `ArrayDeque<String>` (implementando `Queue<String>`).
- **Justificación:** El esquema Round-Robin para la rotación equitativa de sensores requiere disciplina FIFO. La extracción del frente con `poll()` y la reinserción inmediata al final con `add()` garantiza que todos los sensores sean consultados de forma cíclica y equitativa, eliminando el riesgo de inanición (*starvation*) que provocaría una estructura LIFO (`Stack`). La programación evita duplicados mediante validación con `contains()`.

### 7.5 Topología de Red (`topologiaRed`)
- **Estructura elegida:** `HashMap<String, List<String>>` (implementando `Map<String, List<String>>`).
- **Justificación:** En redes de sensores IoT, el grafo es disperso ($E \approx 2V$ a $3V$). La lista de adyacencia sobre un mapa consume $O(V + E)$ en comparación con el desperdicio $O(V^2)$ de una matriz de adyacencia. Permite identificadores alfanuméricos directos sin tablas de traducción, registro dinámico con `computeIfAbsent` y consultas inmediatas de vecinos mediante `getOrDefault` en tiempo $O(1)$ promedio. La detección de estaciones aisladas se resuelve inspeccionando los nodos cuya lista de vecinos esté vacía.

### 7.6 Buffer de Ingesta (`bufferIngesta`)
- **Estructura elegida:** `ArrayDeque<LecturaSensor>` (implementando `Queue<LecturaSensor>`).
- **Justificación:** Procesa el flujo continuo de lecturas entrantes en estricto orden de llegada (FIFO) mediante `poll()` en $O(1)$ sin excepciones cuando el buffer está vacío. Para filtros avanzados se aprovecha la API de `Streams` para seleccionar lecturas críticas según su nivel de PM2.5.
