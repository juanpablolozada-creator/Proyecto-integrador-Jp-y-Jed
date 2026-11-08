# Criterios de Decisión: Estructuras de Datos en la Red de Sensores IoT

Este documento presenta la justificación técnica, matemática y arquitectónica de las decisiones de diseño adoptadas en la implementación del módulo `GestorDeRed`, conectando los principios fundamentales de la teoría de nodos y complejidad algorítmica ($Big\text{-}O$) con el comportamiento interno del **Java Collections Framework (JCF)**.

---

### 1. El dilema del Historial: ArrayList vs. LinkedList

> **Pregunta:** Para el `historialEventos`, podrías usar un `ArrayList` o un `LinkedList`. Si la plataforma registra miles de eventos por segundo y necesitas eliminar los más antiguos frecuentemente para ahorrar memoria, ¿cuál de las dos colecciones es más eficiente y por qué?

#### Análisis Técnico:
La operación crítica que se ejecuta de forma periódica sobre el historial es `limpiarHistorialAntiguo(mantener)`, la cual consiste en remover de manera sucesiva los registros más antiguos situados en el índice 0 (cabeza de la lista).

1. **Comportamiento interno de `ArrayList`:**
   - Un `ArrayList` almacena sus elementos en un arreglo contiguo en memoria.
   - Cada vez que se invoca `remove(0)`, la JVM debe reubicar y desplazar todos los $n - 1$ elementos restantes una posición hacia la izquierda mediante `System.arraycopy`.
   - El costo temporal de cada eliminación en el índice inicial es **$O(n)$**.
   - Si se reciben miles de eventos por segundo y se eliminan $k$ registros antiguos, la complejidad acumulada escala a **$O(k \cdot n)$**, provocando una degradación severa de latencia por sobrecarga en la CPU y constantes pausas de memoria.

2. **Comportamiento interno de `LinkedList`:**
   - `LinkedList` está implementada como una lista doblemente enlazada compuesta por instancias independientes de `Node<E>` (`item`, `next`, `prev`).
   - La eliminación en la cabeza (`removeFirst()` o `remove(0)`) se realiza en **tiempo constante $O(1)$**, pues únicamente requiere reasignar la referencia del puntero inicial (`first = first.next; first.prev = null;`) y decrementar el contador de tamaño. No existe copia ni desplazamiento de memoria.
   - La inserción al final con `add()` también toma **$O(1)$** al actualizar directamente el puntero `last`.

#### Decisión Justificada:
Se elige **`LinkedList`** (o alternativamente un buffer circular como `ArrayDeque`). Para un escenario de alta tasa de ingesta y descarte frecuente de datos viejos en el frente, el costo $O(1)$ de `LinkedList` evita el estrangulamiento computacional del $O(n)$ que impone el desplazamiento de celdas en `ArrayList`.

---

### 2. Navegación Bidireccional: Deque vs. Queue y la teoría del NodoDoble

> **Pregunta:** En el `navegadorAlertas`, utilizamos un `Deque` (Double Ended Queue). ¿Qué ventaja ofrece un `Deque` sobre una `Queue` simple para implementar la funcionalidad de "Atrás" y "Adelante"? ¿Cómo se relaciona esto con el concepto de un `NodoDoble`?

#### Análisis Técnico:
1. **Diferencias entre `Queue` y `Deque`:**
   - La interfaz `Queue` modela estrictamente la semántica FIFO (o con prioridad). Solo permite insertar elementos por la cola (`offer`/`add`) y extraer o inspeccionar por la cabeza (`poll`/`peek`). No proporciona primitivas para operar en sentido inverso.
   - La interfaz `Deque` (*Double Ended Queue*) generaliza el concepto de cola permitiendo inserciones, extracciones e inspecciones simétricas en **ambos extremos** (`addFirst`, `addLast`, `pollFirst`, `pollLast`, `peekFirst`, `peekLast`) con costo temporal garantizado de **$O(1)$**.

2. **Ventaja para la navegación interactiva:**
   - Para avanzar ("Adelante"), se extrae el elemento del frente con `pollFirst()` y se reinserta al final con `addLast()`, generando una rotación continua.
   - Para retroceder ("Atrás"), se extrae el elemento del final con `pollLast()` y se reinserta al frente con `addFirst()`.
   - Con una `Queue` estándar, el desplazamiento hacia atrás es imposible sin vaciar la cola completa en una pila auxiliar en tiempo $O(n)$, lo cual destruiría la interactividad del operador en tiempo real.

3. **Relación con el concepto de `NodoDoble`:**
   - A nivel teórico, una lista doblemente enlazada se compone de `NodoDoble`, donde cada nodo contiene dos enlaces de navegación: un puntero hacia adelante (`siguiente` o `next`) y un puntero hacia atrás (`anterior` o `prev`).
   - `Deque` es la abstracción del JCF que materializa esta capacidad bidireccional: su diseño permite navegar y modificar la secuencia en ambas direcciones sin necesidad de reiniciar el recorrido desde el origen.

---

### 3. El Ciclo de Monitoreo: Colas y Equidad del Sistema

> **Pregunta:** Para la `colaMonitoreo`, implementaste una rotación (sacar del frente y meter al final). ¿Por qué una `Queue` es la estructura natural para este problema? ¿Qué efecto tendría utilizar una `Stack` (Pila) sobre la equidad del procesamiento?

#### Análisis Técnico:
1. **Idoneidad de la estructura `Queue` (FIFO):**
   - El monitoreo de estaciones ambientales requiere un esquema de planificación cíclica (*Round-Robin*), donde cada sensor debe ser consultado de forma periódica, predecible y uniforme.
   - Una `Queue` opera bajo la disciplina **FIFO** (*First-In, First-Out*). La rutina de rotación:
     ```java
     String sensor = colaMonitoreo.poll(); // Extrae el más antiguo en espera
     colaMonitoreo.add(sensor);           // Lo ubica al final de la fila
     return sensor;
     ```
     asegura matemáticamente que todos los $n$ sensores registrados sean consultados exactamente una vez antes de que el primer sensor vuelva a ser evaluado. Esto garantiza **equidad temporal estricta** (*fairness*).

2. **Efecto destructivo de utilizar una `Stack` (LIFO):**
   - Una pila opera bajo la disciplina **LIFO** (*Last-In, First-Out*). Las inserciones y extracciones ocurren sobre el mismo extremo (*tope*).
   - Si se extrae un sensor del tope (`pop()`) y se vuelve a insertar en la pila (`push(sensor)`), dicho sensor quedará nuevamente en la cima. En la siguiente consulta, el sistema volverá a extraer exactamente el mismo sensor.
   - **Consecuencia en el sistema:** Se produce **inanición** (*starvation*) severa. Un único sensor monopoliza el 100% de los ciclos de procesamiento, mientras que el resto de los sensores en el fondo de la pila jamás son consultados. En una red IoT urbana, esto implicaría que zonas completas de la ciudad queden a ciegas ante emergencias de contaminación.

---

### 4. Topología de Red: Lista de Adyacencia vs. Matriz de Adyacencia

> **Pregunta:** Representaste la red de sensores como un `Map<String, List<String>>`. ¿Por qué usamos un `Map` y no un arreglo bidimensional (matriz de adyacencia) considerando que la mayoría de los sensores solo están conectados a 2 o 3 vecinos? ¿Cómo se traduce esta estructura de `Map` a la idea de un `NodoGrafo` con una lista de adyacencias?

#### Análisis Técnico y Comparativa:

| Criterio | Matriz de Adyacencia (`boolean[][]`) | Lista de Adyacencia (`Map<String, List<String>>`) |
| :--- | :--- | :--- |
| **Consumo Espacial** | **$O(V^2)$** (cuadrático respecto a sensores) | **$O(V + E)$** (lineal respecto a conexiones) |
| **Redes Dispersas (*Sparse*)** | Desperdicio masivo de memoria en celdas vacías (`false` / `0`) | Uso óptimo: solo almacena las aristas reales existentes |
| **Identificadores** | Requiere índices enteros continuos $0 \dots V-1$ | Admite identificadores alfanuméricos reales (`"EST-01"`, `"S-NORTE"`) |
| **Inserción de nuevo sensor** | Muy costoso: requiere redimensionar toda la matriz a $(V+1)^2$ en $O(V^2)$ | Inmediato: `map.put(sensor, new ArrayList<>())` en tiempo promedio **$O(1)$** |
| **Consulta de Vecinos** | **$O(V)$**: debe escanear la fila completa de $V$ columnas | **$O(1)$** para obtener la lista + **$O(\text{grado})$** para recorrer 2 o 3 vecinos |
| **Verificar conexión directa ($A \leftrightarrow B$)** | **$O(1)$** directo (`matriz[a][b]`) | **$O(\text{grado})$** buscando en la lista de adyacencia de $A$ |

#### Justificación en Sensores IoT:
En una topología urbana real, los sensores solo se enlazan por proximidad física (radiofrecuencia, LoRaWAN o Zigbee) con 2 o 3 vecinos directos ($E \approx 2V$ o $3V$). 
- Una matriz de 10.000 sensores exigiría $100.000.000$ de celdas en memoria, de las cuales el $99.97\%$ estarían vacías.
- En contraste, `Map<String, List<String>>` almacena únicamente las conexiones existentes, utilizando una fracción insignificante de memoria y permitiendo consultar los vecinos mediante `topologiaRed.getOrDefault(idSensor, Collections.emptyList())` en tiempo constante $O(1)$.

#### Traducción al concepto de `NodoGrafo`:
En la teoría formal de grafos, un `NodoGrafo` contiene su valor/etiqueta y un puntero a una lista enlazada de vértices adyacentes. El `Map<String, List<String>>` es la implementación idiomática moderna en Java de este concepto: la **clave** del mapa representa el vértice (nodo origen) y el **valor** (`List<String>`) corresponde a la lista de adyacencia que almacena todos los vértices vecinos accesibles a través de sus aristas bidireccionales.

---

### 5. Buffer de Ingesta y Rendimiento: ArrayDeque vs. ConcurrentLinkedQueue

> **Pregunta:** El `bufferIngesta` procesa datos en orden FIFO. Si el volumen de datos aumenta drásticamente y necesitas procesar las lecturas en paralelo usando múltiples hilos, ¿seguiría siendo adecuado un `ArrayDeque` o deberías cambiar a una `ConcurrentLinkedQueue`? Justifica tu respuesta basándote en la seguridad de hilos (*Thread-safety*).

#### Análisis Técnico:
1. **Concepto de Seguridad para Concurrencia (*Thread-safety*):**
   Una estructura de datos es *thread-safe* cuando garantiza la preservación de su estado interno consistente, la visibilidad de cambios en memoria entre diferentes hilos y la ausencia de condiciones de carrera (*race conditions*), aun cuando múltiples hilos ejecuten operaciones de lectura y escritura concurrentes sobre ella sin mecanismos de sincronización externos.

2. **Limitaciones críticas de `ArrayDeque` en escenarios multihilo:**
   - `ArrayDeque` **no es thread-safe** por diseño.
   - Mantiene variables de control de estado mutables no sincronizadas ni volátiles (`elements[]`, `head`, `tail`).
   - Si múltiples hilos productores (sensores transmitiendo) invocan `add()` concurrentemente mientras hilos consumidores (procesadores analíticos) invocan `poll()`, las modificaciones concurrentes corrompen los índices de cabeza y cola, sobreescriben lecturas legítimas o pueden dejar el arreglo en estados ilegales, arrojando excepciones inesperadas (`IndexOutOfBoundsException`, lecturas nulas erróneas o pérdida silenciosa de datos críticos).

3. **Arquitectura y ventajas de `ConcurrentLinkedQueue`:**
   - `ConcurrentLinkedQueue` es una implementación thread-safe de la interfaz `Queue` diseñada específicamente para altos volúmenes de concurrencia y patrones Productor-Consumidor (MPSC / MPMC).
   - Utiliza una cola enlazada basada en el algoritmo no bloqueante de **Michael & Scott**.
   - No utiliza bloqueos pesados a nivel de método (`synchronized` ni cerrojos `ReentrantLock` que congelan los hilos); en su lugar, implementa operaciones atómicas a nivel de hardware mediante **CAS** (*Compare-And-Swap*) sobre referencias volátiles.
   - Esto permite que cientos de hilos encolen lecturas (`offer`) y procesen datos (`poll`) simultáneamente con una latencia mínima y sin riesgo de inanición ni corrupción de datos.

#### Decisión Justificada:
Bajo una arquitectura distribuida o concurrente, el cambio hacia **`ConcurrentLinkedQueue`** (o `ArrayBlockingQueue` si se requiere control de contrapresión / *backpressure*) es **absolutamente indispensable**. `ArrayDeque` está reservado exclusivamente para ejecución monohilo.

---

*Documento completado conforme a los requerimientos de entrega y sustentación técnica del Proyecto Integrador.*
