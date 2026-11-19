package structures.management;

import model.LecturaSensor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * GESTOR DE RED - MÓDULO INTEGRADOR DE ESTRUCTURAS DE DATOS (JCF)
 * =============================================================================
 * GUÍA DE SUSTENTACIÓN RÁPIDA PARA EL ESTUDIANTE:
 * Cada sección, atributo y método contiene comentarios explicativos con la
 * respuesta exacta para leerle al profesor si pregunta sobre la decisión técnica,
 * la estructura de datos utilizada o la complejidad algorítmica (Big-O).
 * =============================================================================
 */
public class GestorDeRed {

    // -------------------------------------------------------------------------
    // ATRIBUTOS Y ESTRUCTURAS DE DATOS ELEGIDAS
    // -------------------------------------------------------------------------

    /**
     * RETO 1 y 10 / PREGUNTA 1: HISTORIAL DE EVENTOS
     * ¿Por qué List<String> con implementación LinkedList?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Se utiliza una LinkedList porque la plataforma elimina eventos antiguos
     * desde el inicio (índice 0) constantemente. En LinkedList eliminar en la
     * cabeza es O(1) tiempo constante (solo reasigna punteros del nodo). En un
     * ArrayList sería O(n) porque requiere desplazar todos los elementos en memoria."
     */
    private List<String> historialEventos;

    /**
     * RETOS 2 y 3 / PREGUNTA 2: NAVEGADOR DE ALERTAS
     * ¿Por qué Deque<String> (ArrayDeque) y su relación con NodoDoble?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Se utiliza un Deque (Double Ended Queue) porque permite insertar y extraer
     * elementos en ambos extremos en O(1). Esto hace posible crear un carrusel
     * bidireccional ('Atrás' y 'Adelante'). Se relaciona directamente con el
     * NodoDoble porque un nodo doble tiene punteros hacia adelante (next) y hacia
     * atrás (prev), permitiendo navegación en ambos sentidos sin reiniciar."
     */
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    /**
     * RETOS 4 y 5 / PREGUNTA 3: CICLO DE MONITOREO
     * ¿Por qué Queue<String> (ArrayDeque) y el concepto de equidad vs Pila (Stack)?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Se utiliza una Queue (FIFO) para asegurar un turno estricto y equitativo
     * (Round-Robin): el primer sensor en entrar es el primero en ser consultado.
     * Si usáramos una Pila (Stack/LIFO), el último sensor en reinsertarse volvería
     * a salir inmediatamente en la cima, causando inanición (starvation): un solo
     * sensor monopolizaría las consultas y los demás nunca serían evaluados."
     */
    private Queue<String> colaMonitoreo;

    /**
     * RETOS 6, 7 y 9 / PREGUNTA 4: TOPOLOGÍA DE RED (GRAFO)
     * ¿Por qué Map<String, List<String>> (Lista de adyacencia) vs Matriz?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Representa un grafo mediante Lista de Adyacencia. Es superior a una matriz
     * porque las redes IoT son dispersas (cada sensor solo tiene 2 o 3 vecinos directos).
     * La lista de adyacencia usa memoria O(V + E), mientras que una matriz gasta O(V^2),
     * desperdiciando el 99% del espacio en ceros. Además, el Map permite usar IDs
     * alfanuméricos reales ('EST-01') sin necesidad de convertirlos a índices numéricos.
     * Se relaciona con NodoGrafo donde cada nodo almacena la lista de sus nodos adyacentes."
     */
    private Map<String, List<String>> topologiaRed;

    /**
     * RETO 8 / PREGUNTA 5: BUFFER DE INGESTA
     * ¿Por qué Queue<LecturaSensor> y qué pasaría en un entorno multihilo?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Es una cola FIFO donde las lecturas de los sensores esperan ser procesadas en orden de llegada.
     * En monohilo usamos ArrayDeque por su bajo consumo y costo O(1).
     * Si tuviéramos múltiples hilos (sensores enviando datos en paralelo), ArrayDeque NO
     * es thread-safe (se corromperían sus punteros head y tail). En ese caso se DEBE cambiar
     * por ConcurrentLinkedQueue, que es thread-safe usando operaciones atómicas CAS
     * (Compare-And-Swap) sin bloqueos pesados."
     */
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        // Inicialización de colecciones según el estándar Java Collections Framework (JCF):
        this.historialEventos = new LinkedList<>();     // O(1) en inserción final y eliminación inicial
        this.navegadorAlertas = new ArrayDeque<>();     // O(1) en ambos extremos, sin sobrecarga de nodos
        this.colaMonitoreo = new ArrayDeque<>();        // FIFO puro, más rápido que LinkedList para colas
        this.topologiaRed = new HashMap<>();            // Búsqueda de nodo y acceso a vecinos en O(1) promedio
        this.bufferIngesta = new ArrayDeque<>();        // Buffer de espera FIFO en memoria contigua circular
    }

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    /**
     * RETO 1: AGREGAR EVENTO
     * 
     * ¿Qué hace este método?
     * Registra un nuevo evento al final del historial añadiendo la marca de tiempo actual.
     * 
     * RESPUESTA PARA EL PROFESOR:
     * - Cumple el requerimiento de usar new java.util.Date() para capturar fecha y hora.
     * - Complejidad temporal: O(1) constante, ya que agregar al final de una LinkedList
     *   aprovecha el puntero directo 'last'.
     * - Incluye manejo de excepciones como respaldo en caso de anomalías con tzdb.dat.
     */
    public void agregarEvento(String evento) {
        // TODO 1: Implementar la adición del evento al historial con marca de tiempo.
        java.util.Date fecha = new java.util.Date();
        String fechaTexto;
        try {
            fechaTexto = fecha.toString();
        } catch (Throwable t) {
            // Respaldo ISO robusto en caso de ausencia de zonas horarias locales
            fechaTexto = java.time.Instant.ofEpochMilli(fecha.getTime()).toString();
        }
        String marcaTiempo = "[" + fechaTexto + "] ";
        historialEventos.add(marcaTiempo + evento);
    }

    /**
     * OBTIENE LOS ÚLTIMOS 'LIMITE' EVENTOS DEL HISTORIAL
     * Si hay menos eventos que el límite, devuelve una copia de todos.
     */
    public List<String> obtenerEventosRecientes(int limite) {
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size());
    }

    /**
     * RETO 10 / PREGUNTA 1: LIMPIEZA DE HISTORIAL ANTIGUO
     * 
     * ¿Qué hace este método?
     * Reduce el tamaño del historial eliminando los registros más viejos (los del frente)
     * hasta que solo queden 'mantener' elementos.
     * 
     * RESPUESTA PARA EL PROFESOR:
     * - Si 'mantener' <= 0 se vacía la lista completa con clear() en O(1).
     * - Mientras el tamaño supere el límite, remove(0) desvincula el nodo de la cabeza.
     * - En LinkedList, remove(0) es O(1) por cada elemento eliminado (complejidad O(k) para k elementos).
     * - En ArrayList, remove(0) requeriría O(n) por cada elemento debido a System.arraycopy,
     *   resultando en un costo total de O(k * n) ineficiente.
     */
    public void limpiarHistorialAntiguo(int mantener) {
        // TODO 10: Implementar la optimización del historial eliminando registros viejos.
        if (mantener <= 0) {
            historialEventos.clear();
            return;
        }
        while (historialEventos.size() > mantener) {
            historialEventos.remove(0); // O(1) en LinkedList
        }
    }

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    /**
     * REGISTRA UNA ALERTA EN LA COLA DOBLE
     */
    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    /**
     * RETO 2 / PREGUNTA 2: NAVEGACIÓN HACIA ADELANTE
     * 
     * ¿Cómo funciona la rotación hacia adelante?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Extrae el elemento del frente con pollFirst() y lo reinserta al final con addLast().
     * Esto crea un ciclo infinito y continuo de navegación hacia adelante en O(1).
     * Si la colección está vacía, devuelve null de forma segura."
     */
    public String navegarAlSiguiente() {
        // TODO 2: Lógica de desplazamiento hacia adelante con Deque.
        if (navegadorAlertas.isEmpty()) {
            alertaActual = null;
            return null;
        }
        String siguiente = navegadorAlertas.pollFirst();
        navegadorAlertas.addLast(siguiente);
        alertaActual = siguiente;
        return siguiente;
    }

    /**
     * RETO 3 / PREGUNTA 2: NAVEGACIÓN HACIA ATRÁS
     * 
     * ¿Cómo funciona la rotación hacia atrás?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Hace la operación inversa a navegarAlSiguiente: extrae el elemento del final
     * con pollLast() y lo reinserta en el frente con addFirst() en O(1).
     * Con una Queue simple esto sería imposible porque Queue no permite extraer del final
     * ni insertar al frente."
     */
    public String navegarAlAnterior() {
        // TODO 3: Lógica de desplazamiento hacia atrás con Deque.
        if (navegadorAlertas.isEmpty()) {
            alertaActual = null;
            return null;
        }
        String anterior = navegadorAlertas.pollLast();
        navegadorAlertas.addFirst(anterior);
        alertaActual = anterior;
        return anterior;
    }

    public String navegarALAnterior() {
        return navegarAlAnterior();
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    /**
     * RETO 4: PROGRAMAR CONSULTA SIN DUPLICADOS
     * 
     * ¿Cómo se asegura que no haya duplicados?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Verificamos que el idSensor no sea null y que no exista previamente en la cola
     * usando el método colaMonitoreo.contains(idSensor). Solo si no está presente se
     * encola con add(idSensor). Esto evita que un sensor se consulte dos veces en el mismo ciclo."
     */
    public void programarConsulta(String idSensor) {
        // TODO 4: Programar consulta evitando duplicados con contains().
        if (idSensor != null && !colaMonitoreo.contains(idSensor)) {
            colaMonitoreo.add(idSensor);
        }
    }

    /**
     * RETO 5 / PREGUNTA 3: ROTACIÓN DE SENSORES (ROUND-ROBIN)
     * 
     * ¿Cómo funciona la rotación del turno de sensores?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Se extrae el sensor que está en el frente de la cola mediante poll() (que era el que
     * más tiempo llevaba esperando turno) y se reencola inmediatamente al final con add(sensor).
     * Así todos los sensores reciben exactamente un turno antes de repetir, garantizando
     * equidad estricta (fairness) y evitando inanición."
     */
    public String obtenerSiguienteSensor() {
        // TODO 5: Rotación del ciclo de monitoreo (sacar del frente y reinsertar al final).
        if (colaMonitoreo.isEmpty()) {
            return null;
        }
        String sensor = colaMonitoreo.poll();
        colaMonitoreo.add(sensor);
        return sensor;
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED (GRAFO CON LISTA DE ADYACENCIA)
    // =========================================================================

    /**
     * REGISTRA UNA ESTACIÓN INDIVIDUAL EN LA TOPOLOGÍA
     * Utiliza computeIfAbsent para crear la lista de adyacencia vacía si no existe.
     */
    public void registrarEstacion(String idSensor) {
        if (idSensor != null) {
            topologiaRed.computeIfAbsent(idSensor, k -> new ArrayList<>());
        }
    }

    public void agregarSensor(String idSensor) {
        registrarEstacion(idSensor);
    }

    /**
     * RETO 6 / PREGUNTA 4: REGISTRO DE CONEXIONES BIDIRECCIONALES
     * 
     * ¿Cómo se crea una conexión entre sensorA y sensorB?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Al ser un grafo no dirigido (bidireccional), una conexión entre A y B significa que:
     *  1) B es vecino de A (se agrega a la lista de adyacencia de A).
     *  2) A es vecino de B (se agrega a la lista de adyacencia de B).
     * Se usa computeIfAbsent para inicializar la lista de cada sensor si aún no existía en el mapa,
     * y se verifica con contains() para evitar duplicar la misma arista."
     */
    public void registrarConexion(String sensorA, String sensorB) {
        // TODO 6: Registrar conexión bidireccional en el grafo.
        if (sensorA == null || sensorB == null) return;
        topologiaRed.computeIfAbsent(sensorA, k -> new ArrayList<>());
        topologiaRed.computeIfAbsent(sensorB, k -> new ArrayList<>());

        if (!topologiaRed.get(sensorA).contains(sensorB)) {
            topologiaRed.get(sensorA).add(sensorB);
        }
        if (!topologiaRed.get(sensorB).contains(sensorA)) {
            topologiaRed.get(sensorB).add(sensorA);
        }
    }

    /**
     * RETO 7: CONSULTA DE VECINOS
     * 
     * ¿Cómo se consultan los vecinos de forma segura?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Se utiliza el método topologiaRed.getOrDefault(idSensor, new ArrayList<>()).
     * Si el sensor está en la red, devuelve su lista de vecinos en O(1) tiempo promedio.
     * Si el sensor no existe o no ha sido registrado, devuelve una lista vacía en lugar
     * de null, evitando excepciones de tipo NullPointerException."
     */
    public List<String> obtenerVecinos(String idSensor) {
        // TODO 7: Consulta de vecinos usando getOrDefault para evitar null.
        return topologiaRed.getOrDefault(idSensor, new ArrayList<>());
    }

    /**
     * RETO 9: DETECCIÓN DE ESTACIONES AISLADAS (HUÉRFANAS)
     * 
     * ¿Cómo se identifican estaciones sin conexión?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Se recorre el conjunto de entradas del mapa (entrySet). Cada entrada tiene como clave
     * el identificador del sensor y como valor su lista de vecinos. Si la lista es null o
     * está vacía (isEmpty()), significa que la estación no tiene enlaces a otros sensores
     * (grado 0), por lo que se añade a la lista de estaciones aisladas."
     */
    public List<String> detectarEstacionesAisladas() {
        // TODO 9: Detectar nodos huérfanos con lista de vecinos vacía.
        List<String> aisladas = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : topologiaRed.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                aisladas.add(entry.getKey());
            }
        }
        return aisladas;
    }

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    /**
     * RECIBE UNA LECTURA Y LA ENCOLA EN EL BUFFER
     */
    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    /**
     * RETO 8 / PREGUNTA 5: PROCESAMIENTO SECUENCIAL FIFO
     * 
     * ¿Cómo se procesa la siguiente lectura del buffer?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Se extrae mediante bufferIngesta.poll().
     * La disciplina FIFO (First-In, First-Out) garantiza que las lecturas se procesen en el
     * orden exacto en que llegaron. Si el buffer está vacío, poll() devuelve null sin lanzar
     * ninguna excepción, a diferencia de remove() que lanzaría NoSuchElementException."
     */
    public LecturaSensor procesarSiguienteLectura() {
        // TODO 8: Extracción FIFO del buffer usando poll().
        return bufferIngesta.poll();
    }

    /**
     * EXTRA / FILTRADO CON JAVA STREAMS
     * 
     * ¿Cómo funciona el filtro con Streams?
     * 
     * RESPUESTA PARA EL PROFESOR:
     * "Aplica programación funcional mediante la API Stream de Java.
     * Con bufferIngesta.stream().filter(...) evalúa cada lectura, descarta nulos y selecciona
     * aquellas cuyo nivel de material particulado PM2.5 sea superior al límite umbral.
     * Finalmente collect(Collectors.toList()) empaqueta los resultados en una nueva lista."
     */
    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        return bufferIngesta.stream()
                .filter(l -> l != null && l.getPm25() > limitePm25)
                .collect(Collectors.toList());
    }
}
