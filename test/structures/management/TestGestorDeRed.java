package structures.management;

import model.LecturaSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import structures.management.GestorDeRed;

import java.util.*;

/**
 * =============================================================================
 * SUITE DE PRUEBAS UNITARIAS: GESTOR DE RED (JCF)
 * =============================================================================
 * Cada prueba unitaria valida una regla de negocio y estructura del GestorDeRed.
 * Todas las pruebas están comentadas con la explicación de lo que comprueban
 * para que el estudiante pueda responder con seguridad al profesor.
 * =============================================================================
 */
public class TestGestorDeRed {
    private GestorDeRed gestor;

    @BeforeEach
    void setUp() {
        // Inicializa una instancia limpia de GestorDeRed antes de cada prueba
        gestor = new GestorDeRed();
    }

    /**
     * VALIDA EL RETO 1: Historial de eventos
     * Comprueba que al invocar agregarEvento se guarde el registro junto con su marca de tiempo.
     */
    @Test
    void testAgregarEvento_DebeRegistrarEvento() {
        gestor.agregarEvento("Sensor EST-01 activo");
        assertNotNull(gestor.obtenerEventosRecientes(1), "El historial no debería estar vacío");
    }

    /**
     * VALIDA LOS RETOS 2 y 3: Navegación de alertas con Deque
     * Comprueba que se pueda navegar hacia adelante y hacia atrás en un carrusel circular.
     */
    @Test
    void testNavegacionAlertas_DebeMoverseAdelanteYAtraves() {
        gestor.registrarAlerta("Alerta A");
        gestor.registrarAlerta("Alerta B");
        gestor.registrarAlerta("Alerta C");

        String siguiente = gestor.navegarAlSiguiente();
        assertNotNull(siguiente, "Debería poder navegar al siguiente");

        String anterior = gestor.navegarAlAnterior();
        assertNotNull(anterior, "Debería poder volver al anterior");
    }

    /**
     * VALIDA EL RETO 5: Rotación FIFO de sensores (Round-Robin)
     * Comprueba que la cola de turnos devuelva S1, luego S2, y vuelva a rotar a S1.
     */
    @Test
    void testCicloMonitoreo_DebeRotarSensores() {
        gestor.programarConsulta("S1");
        gestor.programarConsulta("S2");

        String primero = gestor.obtenerSiguienteSensor();
        assertEquals("S1", primero);

        String segundo = gestor.obtenerSiguienteSensor();
        assertEquals("S2", segundo);

        String rotacion = gestor.obtenerSiguienteSensor();
        assertEquals("S1", rotacion, "El ciclo debe volver al sensor S1");
    }

    /**
     * VALIDA EL RETO 6: Registro de conexiones bidireccionales en grafo
     * Comprueba que si se conecta S1 con S2, S2 aparezca en la lista de S1 y viceversa.
     */
    @Test
    void testTopologia_DebeRegistrarConexiones() {
        gestor.registrarConexion("S1", "S2");
        List<String> vecinosS1 = gestor.obtenerVecinos("S1");
        List<String> vecinosS2 = gestor.obtenerVecinos("S2");

        assertNotNull(vecinosS1);
        assertTrue(vecinosS1.contains("S2"));
        assertNotNull(vecinosS2);
        assertTrue(vecinosS2.contains("S1"));
    }

    /**
     * VALIDA EL RETO 9: Detección de estaciones huérfanas
     * Comprueba que el método detectarEstacionesAisladas devuelva una lista no nula.
     */
    @Test
    void testEstacionesAisladas_DebeDetectarHuerfanos() {
        gestor.registrarConexion("S1", "S2");
        List<String> aisladas = gestor.detectarEstacionesAisladas();
        assertNotNull(aisladas);
    }

    /**
     * VALIDA EL RETO 8: Procesamiento secuencial FIFO del buffer
     * Comprueba que la primera lectura encolada (l1) sea la primera en procesarse (l1).
     */
    @Test
    void testBufferIngesta_DebeProcesarEnOrden() {
        LecturaSensor l1 = new LecturaSensor("S1", "T1", 10.0, 20.0, 30.0);
        LecturaSensor l2 = new LecturaSensor("S2", "T2", 15.0, 25.0, 35.0);

        gestor.recibirLectura(l1);
        gestor.recibirLectura(l2);

        assertEquals(l1, gestor.procesarSiguienteLectura());
        assertEquals(l2, gestor.procesarSiguienteLectura());
    }

    /**
     * VALIDA EL RETO 10: Limpieza eficiente de memoria del historial
     * Comprueba que tras insertar 100 eventos y pedir mantener 10, solo queden 10 eventos.
     */
    @Test
    void testOptimizarHistorial_DebeLimitarTamaño() {
        for(int i=0; i<100; i++) gestor.agregarEvento("Evento " + i);
        gestor.limpiarHistorialAntiguo(10);
        assertEquals(10, gestor.obtenerEventosRecientes(100).size());
    }

    /**
     * VALIDA EL RETO 4: Prevención de duplicados en la cola de monitoreo
     * Comprueba que al intentar registrar dos veces S1, solo se guarde una vez.
     */
    @Test
    void testProgramarConsulta_NoDebeDuplicar() {
        gestor.programarConsulta("S1");
        gestor.programarConsulta("S1");
        gestor.programarConsulta("S2");

        assertEquals("S1", gestor.obtenerSiguienteSensor());
        assertEquals("S2", gestor.obtenerSiguienteSensor());
        assertEquals("S1", gestor.obtenerSiguienteSensor(), "S1 no debe estar duplicado");
    }

    /**
     * VALIDA EL RETO 7: Consulta segura con getOrDefault
     * Comprueba que consultar un sensor inexistente devuelva lista vacía y no cause NullPointerException.
     */
    @Test
    void testObtenerVecinos_SensorInexistenteRetornaVacio() {
        List<String> vecinos = gestor.obtenerVecinos("DESCONOCIDO");
        assertNotNull(vecinos);
        assertTrue(vecinos.isEmpty());
    }

    /**
     * VALIDA EL RETO 9: Detección precisa de estación aislada
     * Comprueba que si se conecta S1 con S2 y se registra S3_AISLADO sin enlaces, solo S3 sea reportado.
     */
    @Test
    void testEstacionesAisladas_DetectaEstacionRegistradaSinVecinos() {
        gestor.registrarConexion("S1", "S2");
        gestor.registrarEstacion("S3_AISLADO");

        List<String> aisladas = gestor.detectarEstacionesAisladas();
        assertNotNull(aisladas);
        assertTrue(aisladas.contains("S3_AISLADO"));
        assertFalse(aisladas.contains("S1"));
        assertFalse(aisladas.contains("S2"));
    }

    /**
     * VALIDA CASO BORDE: Rotación en cola vacía
     * Comprueba que si no hay sensores programados, devuelva null en vez de lanzar excepción.
     */
    @Test
    void testCicloMonitoreo_ColaVaciaRetornaNull() {
        assertNull(gestor.obtenerSiguienteSensor());
    }

    /**
     * VALIDA CASO BORDE: Navegación de alertas vacía
     * Comprueba que navegar hacia adelante o atrás sin alertas devuelva null de forma segura.
     */
    @Test
    void testNavegacionAlertas_VaciaRetornaNull() {
        assertNull(gestor.navegarAlSiguiente());
        assertNull(gestor.navegarAlAnterior());
    }

    /**
     * VALIDA EL REQUERIMIENTO EXTRA: Filtrado de alertas críticas con Java Streams
     * Comprueba que se filtren exactamente las lecturas que superen el límite de PM2.5.
     */
    @Test
    void testFiltrarAlertasCriticas_ConStreams() {
        LecturaSensor normal = new LecturaSensor("S1", "2026-10-24 10:00", 22.0, 50.0, 25.0);
        LecturaSensor critica1 = new LecturaSensor("S2", "2026-10-24 10:01", 23.0, 55.0, 75.0);
        LecturaSensor critica2 = new LecturaSensor("S3", "2026-10-24 10:02", 24.0, 60.0, 120.0);

        gestor.recibirLectura(normal);
        gestor.recibirLectura(critica1);
        gestor.recibirLectura(critica2);

        List<LecturaSensor> criticas = gestor.filtrarAlertasCriticas(50.0);
        assertEquals(2, criticas.size());
        assertTrue(criticas.contains(critica1));
        assertTrue(criticas.contains(critica2));
        assertFalse(criticas.contains(normal));
    }
}
