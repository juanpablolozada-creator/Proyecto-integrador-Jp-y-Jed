package structures.management;

import model.LecturaSensor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos (JCF)
 * 
 * Implementación de la gestión de red de sensores ambientales utilizando
 * las colecciones estándar de Java (Java Collections Framework).
 * Diseñado considerando eficiencia algorítmica (Big-O), uso eficiente
 * de memoria y comportamiento ante concurrencia.
 */
public class GestorDeRed {

    /**
     * Historial de eventos:
     * Se utiliza LinkedList porque la plataforma elimina eventos antiguos desde el
     * inicio (índice 0) constantemente. En LinkedList, desvincular el primer nodo es O(1)
     * en tiempo constante ya que solo reasigna referencias de punteros. En ArrayList sería
     * O(n) al tener que desplazar todos los elementos restantes en memoria con System.arraycopy.
     */
    private List<String> historialEventos;

    /**
     * Navegador de alertas:
     * Se utiliza Deque (Double Ended Queue con ArrayDeque) porque permite insertar y
     * extraer elementos en ambos extremos en O(1), haciendo posible la navegación
     * circular bidireccional ('Atrás' y 'Adelante'). Se fundamenta en el concepto de
     * NodoDoble al permitir navegación hacia adelante (next) y atrás (prev) sin reiniciar.
     */
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    /**
     * Ciclo de monitoreo:
     * Se utiliza Queue (FIFO con ArrayDeque) para garantizar una planificación cíclica
     * equitativa (Round-Robin): el primer sensor en entrar es el primero en ser consultado.
     * Si se usara una Pila (Stack/LIFO), el último elemento reinsertado saldría de nuevo en la cima,
     * provocando inanición (starvation) al monopolizar un solo sensor y dejar a los demás sin evaluar.
     */
    private Queue<String> colaMonitoreo;

    /**
     * Topología de red:
     * Representa un grafo mediante Lista de Adyacencia con Map<String, List<String>>.
     * Es óptimo frente a una matriz de adyacencia porque las redes IoT son dispersas (sparse),
     * donde cada nodo solo tiene 2 o 3 vecinos directos. Consume memoria O(V + E) frente al O(V^2)
     * de una matriz, evitando desperdiciar espacio en celdas vacías y permitiendo claves
     * alfanuméricas directas ('EST-01') sin mapeos a índices numéricos.
     */
    private Map<String, List<String>> topologiaRed;

    /**
     * Buffer de ingesta:
     * Cola FIFO para procesar lecturas entrantes en orden estricto de llegada.
     * Para ejecución monohilo se usa ArrayDeque por su bajo consumo y costo O(1).
     * En un escenario multihilo con múltiples sensores productores y consumidores, ArrayDeque
     * no es thread-safe (se corromperían head y tail); en ese caso debe utilizarse
     * ConcurrentLinkedQueue, que ofrece seguridad para concurrencia mediante el algoritmo
     * no bloqueante de Michael & Scott con operaciones atómicas CAS (Compare-And-Swap).
     */
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        this.historialEventos = new LinkedList<>();
        this.navegadorAlertas = new ArrayDeque<>();
        this.colaMonitoreo = new ArrayDeque<>();
        this.topologiaRed = new HashMap<>();
        this.bufferIngesta = new ArrayDeque<>();
    }

    // -------------------------------------------------------------------------
    // HISTORIAL Y EVENTOS
    // -------------------------------------------------------------------------

    /**
     * Registra un nuevo evento al final del historial añadiendo la marca de tiempo
     * actual mediante java.util.Date.
     * Inserción en tiempo constante O(1) aprovechando el puntero directo al final de LinkedList.
     */
    public void agregarEvento(String evento) {
        java.util.Date fecha = new java.util.Date();
        String fechaTexto;
        try {
            fechaTexto = fecha.toString();
        } catch (Throwable t) {
            fechaTexto = java.time.Instant.ofEpochMilli(fecha.getTime()).toString();
        }
        String marcaTiempo = "[" + fechaTexto + "] ";
        historialEventos.add(marcaTiempo + evento);
    }

    /**
     * Obtiene los últimos 'limite' eventos registrados en el historial.
     * Si la lista contiene menos eventos que el límite, devuelve una copia íntegra.
     */
    public List<String> obtenerEventosRecientes(int limite) {
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size());
    }

    /**
     * Conserva únicamente los últimos 'mantener' eventos, descartando los más antiguos.
     * Se aprovecha que en LinkedList la operación remove(0) toma O(1) tiempo constante
     * por elemento al solo actualizar la referencia de la cabeza, a diferencia de ArrayList
     * donde cada remove(0) tomaría O(n) por desplazamiento en memoria.
     */
    public void limpiarHistorialAntiguo(int mantener) {
        if (mantener <= 0) {
            historialEventos.clear();
            return;
        }
        while (historialEventos.size() > mantener) {
            historialEventos.remove(0);
        }
    }

    // -------------------------------------------------------------------------
    // NAVEGACIÓN DE ALERTAS
    // -------------------------------------------------------------------------

    /**
     * Añade una nueva alerta al final del navegador y la establece como alerta activa.
     */
    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    /**
     * Navega a la siguiente alerta extrayendo el elemento del frente con pollFirst()
     * y reinsertándolo al final con addLast() en O(1), creando un ciclo continuo.
     * Retorna null si la colección está vacía.
     */
    public String navegarAlSiguiente() {
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
     * Navega a la alerta previa extrayendo el elemento del final con pollLast()
     * y reinsertándolo al inicio con addFirst() en O(1).
     * Operación exclusiva de interfaces Deque que una Queue simple no permite.
     */
    public String navegarAlAnterior() {
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

    // -------------------------------------------------------------------------
    // CICLO DE MONITOREO
    // -------------------------------------------------------------------------

    /**
     * Programa la consulta de un sensor garantizando la ausencia de duplicados.
     * Se valida no nulidad y pertenencia mediante contains() antes de encolar.
     */
    public void programarConsulta(String idSensor) {
        if (idSensor != null && !colaMonitoreo.contains(idSensor)) {
            colaMonitoreo.add(idSensor);
        }
    }

    /**
     * Gestiona la rotación continua de turnos (Round-Robin): extrae el sensor al frente
     * con poll() y lo reencola al final con add() en O(1).
     * Asegura equidad temporal estricta para que todos los sensores sean evaluados.
     * Retorna null de forma segura si la cola está vacía.
     */
    public String obtenerSiguienteSensor() {
        if (colaMonitoreo.isEmpty()) {
            return null;
        }
        String sensor = colaMonitoreo.poll();
        colaMonitoreo.add(sensor);
        return sensor;
    }

    // -------------------------------------------------------------------------
    // TOPOLOGÍA DE RED
    // -------------------------------------------------------------------------

    /**
     * Registra un sensor en la topología inicializando su lista de adyacencia
     * si aún no existe en el mapa.
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
     * Registra una conexión bidireccional (arista no dirigida) entre sensorA y sensorB.
     * Agrega B a la lista de adyacencia de A, y A a la lista de B, evitando aristas duplicadas.
     */
    public void registrarConexion(String sensorA, String sensorB) {
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
     * Obtiene los vecinos de un sensor en O(1) tiempo promedio.
     * Emplea getOrDefault para retornar una lista vacía en lugar de null si el sensor no existe,
     * previniendo NullPointerException.
     */
    public List<String> obtenerVecinos(String idSensor) {
        return topologiaRed.getOrDefault(idSensor, new ArrayList<>());
    }

    /**
     * Detecta estaciones aisladas (nodos con grado 0).
     * Recorre las entradas del mapa y selecciona aquellas cuya lista de vecinos sea nula o vacía.
     */
    public List<String> detectarEstacionesAisladas() {
        List<String> aisladas = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : topologiaRed.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                aisladas.add(entry.getKey());
            }
        }
        return aisladas;
    }

    // -------------------------------------------------------------------------
    // BUFFER DE INGESTA
    // -------------------------------------------------------------------------

    /**
     * Encola una lectura en el buffer de espera para su posterior procesamiento.
     */
    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    /**
     * Extrae y procesa la siguiente lectura en estricto orden FIFO mediante poll() en O(1).
     * Si el buffer se encuentra vacío, retorna null de manera segura sin lanzar excepciones.
     */
    public LecturaSensor procesarSiguienteLectura() {
        return bufferIngesta.poll();
    }

    /**
     * Filtra lecturas críticas utilizando la API Stream de Java.
     * Evalúa las lecturas presentes en el buffer cuyo valor PM2.5 supere el límite establecido.
     */
    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        return bufferIngesta.stream()
                .filter(l -> l != null && l.getPm25() > limitePm25)
                .collect(Collectors.toList());
    }
}
