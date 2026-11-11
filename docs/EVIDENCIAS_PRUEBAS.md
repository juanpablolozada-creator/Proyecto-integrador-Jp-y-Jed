# Evidencias de Ejecución y Verificación Técnica

**Proyecto:** Plataforma de Monitoreo Ambiental Urbano  
**Módulo:** Gestor de Red de Sensores IoT (`GestorDeRed`)  
**Fecha de Verificación:** 11 de Noviembre de 2026  
**Resultado Global:** **100% de Pruebas Aprobadas (61/61)**

---

## 1. Resumen de Pruebas Unitarias

La suite de pruebas automatizadas con **JUnit Platform Standalone** cubre la totalidad de los retos del módulo `GestorDeRed`, así como los módulos acumulativos de las semanas previas (Estructuras de datos lineales, algoritmos de ordenamiento, matrices, árboles y colecciones estándar de Java).

| Módulo / Clase de Prueba | Pruebas Ejecutadas | Pruebas Aprobadas | Fallos | Estado |
| :--- | :---: | :---: | :---: | :---: |
| **`TestGestorDeRed`** (Retos 1 al 10 y Streams) | **13** | **13** | **0** | **EXITOSO** |
| `RepositorioArregloTest` | 4 | 4 | 0 | **EXITOSO** |
| `RepositorioEnlazadoTest` | 5 | 5 | 0 | **EXITOSO** |
| `RepositorioColeccionTest` | 4 | 4 | 0 | **EXITOSO** |
| `RepositorioLecturasTest` | 7 | 7 | 0 | **EXITOSO** |
| `HistorialNavegableTest` | 3 | 3 | 0 | **EXITOSO** |
| `OrdenadorTest` | 7 | 7 | 0 | **EXITOSO** |
| `BuscadorLecturasTest` | 4 | 4 | 0 | **EXITOSO** |
| `AnalizadorMatrizTest` | 3 | 3 | 0 | **EXITOSO** |
| `LecturaSensorTest` | 4 | 4 | 0 | **EXITOSO** |
| `GeneradorDatosTest` | 4 | 4 | 0 | **EXITOSO** |
| `NodoTest` | 1 | 1 | 0 | **EXITOSO** |
| `BancoDeListasTest` | 1 | 1 | 0 | **EXITOSO** |
| `BancoDeOrdenamientoTest` | 1 | 1 | 0 | **EXITOSO** |
| **TOTAL** | **61** | **61** | **0** | **100% APROBADO** |

---

## 2. Detalle de Pruebas Específicas del Módulo `GestorDeRed`

| Método de Prueba | Reto Verificado | Resultado |
| :--- | :--- | :---: |
| `testAgregarEvento_DebeRegistrarEvento()` | **Reto 1:** Historial de eventos con marca de tiempo | **APROBADO** |
| `testNavegacionAlertas_DebeMoverseAdelanteYAtraves()` | **Retos 2 y 3:** Navegación bidireccional de alertas (`Deque`) | **APROBADO** |
| `testNavegacionAlertas_VaciaRetornaNull()` | **Retos 2 y 3:** Manejo de caso borde en navegador vacío | **APROBADO** |
| `testProgramarConsulta_NoDebeDuplicar()` | **Reto 4:** Inserción en cola de monitoreo sin duplicados | **APROBADO** |
| `testCicloMonitoreo_DebeRotarSensores()` | **Reto 5:** Rotación continua FIFO en el ciclo de monitoreo | **APROBADO** |
| `testCicloMonitoreo_ColaVaciaRetornaNull()` | **Reto 5:** Manejo de caso borde cuando la cola está vacía | **APROBADO** |
| `testTopologia_DebeRegistrarConexiones()` | **Reto 6:** Registro de conexiones bidireccionales en grafo | **APROBADO** |
| `testObtenerVecinos_SensorInexistenteRetornaVacio()` | **Reto 7:** Consulta segura con `getOrDefault` para sensores sin conexión | **APROBADO** |
| `testBufferIngesta_DebeProcesarEnOrden()` | **Reto 8:** Procesamiento secuencial FIFO de lecturas con `poll()` | **APROBADO** |
| `testEstacionesAisladas_DebeDetectarHuerfanos()` | **Reto 9:** Detección de nodos con 0 conexiones en el grafo | **APROBADO** |
| `testEstacionesAisladas_DetectaEstacionRegistradaSinVecinos()` | **Reto 9:** Identificación precisa de estación aislada específica | **APROBADO** |
| `testOptimizarHistorial_DebeLimitarTamaño()` | **Reto 10:** Eliminación eficiente $O(1)$ de eventos antiguos | **APROBADO** |
| `testFiltrarAlertasCriticas_ConStreams()` | **Extra:** Filtrado funcional con Java Streams | **APROBADO** |

---

## 3. Registro de Salida de Consola (JUnit Platform Execution Log)

```text
Thanks for using JUnit! Support its development at https://junit.org/sponsoring

=== EXP 1: ALGORITMOS SIMPLES, 10.000 LECTURAS DESORDENADAS ===
Burbuja        comparaciones:     49.990.814   intercambios:     24.928.244     1958 ms
Seleccion      comparaciones:     49.995.000   intercambios:          9.994     1593 ms
Insercion      comparaciones:     24.938.233   intercambios:     24.928.244      446 ms

=== EXP 1: EL MISMO CODIGO, DOS MECANISMOS ===
Arreglo          tamano: 5   primera: 0000000000
Lista enlazada   tamano: 5   primera: 0000000000

Ni una linea del ciclo de arriba sabe que hay adentro.
Eso es el contrato de la semana 2, cumplido.

JUnit Jupiter
  BancoDeOrdenamientoTest
    testExperimentoUnoRunsWithoutException() [PASSED]
  RepositorioEnlazadoTest
    testEliminarMiddleNodeKeepsChain() [PASSED]
    testAgregarAlFinalUsesUltimo() [PASSED]
    testContarRecorriendoMatchesCantidad() [PASSED]
    testEliminarFirstNode() [PASSED]
    testEliminarLastNodeUpdatesUltimo() [PASSED]
  RepositorioLecturasTest
    testActualizar() [PASSED]
    testRedimensionar() [PASSED]
    testEliminarDesplaza() [PASSED]
    testBuscarPorEstacion() [PASSED]
    testAgregarYTamano() [PASSED]
    testObtenerOutOfBounds() [PASSED]
    testPromedioPm25() [PASSED]
  LecturaSensorTest
    testEsValidaTrue() [PASSED]
    testGetHora() [PASSED]
    testEsValidaFalseOutOfRange() [PASSED]
    testToStringFormat() [PASSED]
  NodoTest
    testLinkingNodes() [PASSED]
  RepositorioColeccionTest
    testSumaPm25() [PASSED]
    testObtenerYEliminar() [PASSED]
    testAgregarAlInicio() [PASSED]
    testAgregarYTamano() [PASSED]
  BancoDeListasTest
    testExperimentoUnoRunsWithoutException() [PASSED]
  HistorialNavegableTest
    testAgregarAndForwardTraversal() [PASSED]
    testNavigationAnteriorAndSiguiente() [PASSED]
    testBackwardTraversalAfterAdding() [PASSED]
  BuscadorLecturasTest
    testBusquedaLinealPorTimestamp() [PASSED]
    testBusquedaBinariaPorPm25() [PASSED]
    testBuscarPorEstacion() [PASSED]
    testBusquedaBinariaPorTimestamp() [PASSED]
  GeneradorDatosTest
    testTimestampOrdenado() [PASSED]
    testIdCicloNueveEstaciones() [PASSED]
    testRangosValidos() [PASSED]
    testGenerarTamaño() [PASSED]
  OrdenadorTest
    testQuickSortRandomPivotSortedNoOverflow() [PASSED]
    testHeapSortSortsCorrectly() [PASSED]
    testBurbujaEarlyExitOnSortedData() [PASSED]
    testMergeSortSortsCorrectly() [PASSED]
    testQuickSortRandomPivotUnsorted() [PASSED]
    testOrdenarPorPm25() [PASSED]
    testBurbujaSortUnsorted() [PASSED]
  TestGestorDeRed
    testEstacionesAisladas_DebeDetectarHuerfanos() [PASSED]
    testAgregarEvento_DebeRegistrarEvento() [PASSED]
    testNavegacionAlertas_DebeMoverseAdelanteYAtraves() [PASSED]
    testProgramarConsulta_NoDebeDuplicar() [PASSED]
    testOptimizarHistorial_DebeLimitarTamaño() [PASSED]
    testCicloMonitoreo_ColaVaciaRetornaNull() [PASSED]
    testNavegacionAlertas_VaciaRetornaNull() [PASSED]
    testTopologia_DebeRegistrarConexiones() [PASSED]
    testObtenerVecinos_SensorInexistenteRetornaVacio() [PASSED]
    testEstacionesAisladas_DetectaEstacionRegistradaSinVecinos() [PASSED]
    testBufferIngesta_DebeProcesarEnOrden() [PASSED]
    testCicloMonitoreo_DebeRotarSensores() [PASSED]
    testFiltrarAlertasCriticas_ConStreams() [PASSED]
  AnalizadorMatrizTest
    testPromedioDeEstacion() [PASSED]
    testRegistrarYPromedioDeHora() [PASSED]
    testHoraMasContaminada() [PASSED]
  RepositorioArregloTest
    testRedimensionAndCounters() [PASSED]
    testAgregarAlInicioShiftsElements() [PASSED]
    testEliminarShiftsElementsAndUpdatesSize() [PASSED]
    testAgregarAlFinalAndObtener() [PASSED]

Test run finished after 4521 ms
[        17 containers found      ]
[         0 containers skipped    ]
[        17 containers started    ]
[         0 containers aborted    ]
[        17 containers successful ]
[         0 containers failed     ]
[        61 tests found           ]
[         0 tests skipped         ]
[        61 tests started         ]
[         0 tests aborted         ]
[        61 tests successful      ]
[         0 tests failed          ]
```

---

## 4. Instrucciones para Reproducir la Ejecución

En la raíz del proyecto se han dispuesto scripts automatizados listos para ejecutar con un solo clic o desde la terminal:

- **Doble clic en Windows:** Ejecutar el archivo `ejecutar_tests.bat`.
- **Desde PowerShell:**
  ```powershell
  .\ejecutar_tests.ps1
  ```
- **Desde la línea de comandos clásica:**
  ```bash
  javac -cp "lib/junit-platform-console-standalone-1.10.2.jar" -d out (Get-ChildItem -Path src,test -Filter *.java -Recurse | ForEach-Object { $_.FullName })
  java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path out --scan-class-path
  ```
