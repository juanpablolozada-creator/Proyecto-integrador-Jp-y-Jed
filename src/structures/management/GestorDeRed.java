package structures.management;

import model.LecturaSensor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 * VERSION ESTUDIANTE
 *
 * Objetivo: Implementar la gestión de la red utilizando las colecciones de Java (JCF).
 * El éxito de este módulo no es solo que el código compile, sino que elijas
 * la estructura correcta para cada problema.
 */
public class GestorDeRed {
    // 1. Historial de Eventos: Registro lineal de todas las actividades
    private List<String> historialEventos;

    // 2. Navegador de Alertas: Permite moverse adelante y atrás entre alertas
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    // 3. Ciclo de Monitoreo: Gestión de la rotación de sensores a consultar
    private Queue<String> colaMonitoreo;

    // 4. Topología de la Red: Grafo de conexiones entre estaciones (Lista de adyacencia)
    private Map<String, List<String>> topologiaRed;

    // 5. Buffer de Ingesta: Cola de espera para procesar lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        // Inicialización de colecciones usando las implementaciones más adecuadas de Java (JCF):
        // LinkedList para el historial: inserciones O(1) y eliminación eficiente de eventos antiguos O(1) desde el inicio
        this.historialEventos = new LinkedList<>();
        // ArrayDeque para navegador de alertas: acceso y manipulación en ambos extremos en O(1)
        this.navegadorAlertas = new ArrayDeque<>();
        // ArrayDeque como Queue para ciclo de monitoreo: rotación FIFO sin sobrecarga de sincronización
        this.colaMonitoreo = new ArrayDeque<>();
        // HashMap para topología de red: lista de adyacencia con búsquedas por sensor en O(1) promedio
        this.topologiaRed = new HashMap<>();
        // ArrayDeque como Queue para buffer de ingesta: procesamiento FIFO en O(1)
        this.bufferIngesta = new ArrayDeque<>();
    }

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    public void agregarEvento(String evento) {
        // TODO 1: Implementar la adición del evento al historial.
        // Debe registrarse la fecha/hora actual junto con la descripción del evento.
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

    public List<String> obtenerEventosRecientes(int limite) {
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size());
    }

    public void limpiarHistorialAntiguo(int mantener) {
        // TODO 10: Implementar la optimización del historial.
        // Eliminar los registros más antiguos para mantener solo los últimos 'mantener' eventos.
        if (mantener <= 0) {
            historialEventos.clear();
            return;
        }
        while (historialEventos.size() > mantener) {
            historialEventos.remove(0);
        }
    }

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    public String navegarAlSiguiente() {
        // TODO 2: Implementar la lógica de desplazamiento hacia adelante.
        if (navegadorAlertas.isEmpty()) {
            alertaActual = null;
            return null;
        }
        String siguiente = navegadorAlertas.pollFirst();
        navegadorAlertas.addLast(siguiente);
        alertaActual = siguiente;
        return siguiente;
    }

    public String navegarAlAnterior() {
        // TODO 3: Implementar la lógica de desplazamiento hacia atrás.
        if (navegadorAlertas.isEmpty()) {
            alertaActual = null;
            return null;
        }
        String anterior = navegadorAlertas.pollLast();
        navegadorAlertas.addFirst(anterior);
        alertaActual = anterior;
        return anterior;
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    public void programarConsulta(String idSensor) {
        // TODO 4: Implementar la programación de la consulta evitando duplicados.
        if (idSensor != null && !colaMonitoreo.contains(idSensor)) {
            colaMonitoreo.add(idSensor);
        }
    }

    public String obtenerSiguienteSensor() {
        // TODO 5: Implementar la rotación del ciclo (extraer frente y reinsertar al final).
        if (colaMonitoreo.isEmpty()) {
            return null;
        }
        String sensor = colaMonitoreo.poll();
        colaMonitoreo.add(sensor);
        return sensor;
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    public void registrarEstacion(String idSensor) {
        if (idSensor != null) {
            topologiaRed.computeIfAbsent(idSensor, k -> new ArrayList<>());
        }
    }

    public void agregarSensor(String idSensor) {
        registrarEstacion(idSensor);
    }

    public void registrarConexion(String sensorA, String sensorB) {
        // TODO 6: Implementar la conexión bidireccional entre dos sensores.
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

    public List<String> obtenerVecinos(String idSensor) {
        // TODO 7: Implementar la consulta de conexiones usando getOrDefault.
        return topologiaRed.getOrDefault(idSensor, new ArrayList<>());
    }

    public List<String> detectarEstacionesAisladas() {
        // TODO 9: Implementar la detección de nodos huérfanos.
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

    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    public LecturaSensor procesarSiguienteLectura() {
        // TODO 8: Implementar la extracción del buffer (FIFO).
        return bufferIngesta.poll();
    }

    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        // TODO EXTRA: Usar Streams para filtrar lecturas con PM2.5 superior al límite.
        return bufferIngesta.stream()
                .filter(l -> l != null && l.getPm25() > limitePm25)
                .collect(Collectors.toList());
    }
}
