# Bitacora individual - Semana 07 (Final)

## 1. Datos de la actividad

- **Estudiante:** Jedreck Triana
- **Equipo:** Jedreck Triana y Juan Pablo Lozada (Proyecto-integrador-Jp-y-Jed)
- **Semana:** 07 (Cierre del Proyecto Integrador)
- **Fecha del laboratorio:** 2026-11-19
- **Fecha del taller:** 2026-11-19
- **Tema principal:** Java Collections Framework (JCF) y Módulo GestorDeRed (List, Deque, Queue, Map, Streams)
- **Pregunta de la semana:** ¿Cómo seleccionar y justificar técnicamente la estructura de datos adecuada del Java Collections Framework (JCF) para cada componente crítico de una plataforma de monitoreo IoT (historial, alertas, ciclo de consultas, topología y buffer de ingesta), garantizando eficiencia temporal, memoria y equidad?

## 2. Prediccion antes de ejecutar

Antes de abrir o ejecutar el programa, responde:

1. **Que creo que va a ocurrir?**
   Esperaba que al utilizar las interfaces y colecciones estándar de Java (`List`, `Deque`, `Queue`, `Map`) el código fuera mucho más modular y eficiente que implementando estructuras de nodos manualmente. Predije que todas las pruebas unitarias pasarían si respetábamos los contratos de rotación circular en el Deque y la unicidad en las consultas.

2. **Que parte del programa o del algoritmo puede fallar?**
   Consideré crítica la navegación circular de alertas con `Deque` en casos borde donde la cola estuviera vacía, y el formato de marca de tiempo en `agregarEvento` si el entorno local de Java tenía problemas para resolver zonas horarias con `java.util.Date`.

3. **Como comprobare mi prediccion?**
   Ejecutando la suite completa de pruebas unitarias automatizadas con JUnit Standalone Console (`TestGestorDeRed` y los tests acumulativos de las semanas previas) para verificar que el 100% de las pruebas aprueben.

## 3. Evidencia del laboratorio

### Resultado observado

Se compilaron y ejecutaron todas las pruebas unitarias del proyecto mediante el script automatizado `ejecutar_tests.ps1`.
- **Total de pruebas ejecutadas:** 61 pruebas unitarias.
- **Pruebas aprobadas:** 61 (100% de éxito, 0 fallos).
- **Pruebas específicas de GestorDeRed:** 13 pruebas aprobadas, cubriendo registro de eventos, navegación bidireccional, rotación equitativa FIFO, topología de grafos, detección de nodos huérfanos, optimización de historial y filtrado funcional con Streams.

### Diferencia entre la prediccion y el resultado

La lógica algorítmica funcionó exactamente como se predijo. Sin embargo, se identificó una diferencia de entorno: en Windows, cuando la máquina virtual tiene una instalación incompleta sin la base de datos de zonas horarias (`tzdb.dat`), invocar `Date.toString()` directamente generaba un error de inicialización de calendario. Esto nos obligó a implementar un mecanismo defensivo con respaldo a `Instant.ofEpochMilli`.

### Error o comportamiento inesperado

- **Que ocurrio?** Al invocar `new java.util.Date()` en una JVM de Windows con instalación defectuosa, se producía un `FileNotFoundException` sobre `tzdb.dat`.
- **Por que ocurrio?** El método `.toString()` de `Date` intenta resolver la zona horaria del sistema operativo mediante `ZoneInfoFile`, el cual requiere el archivo físico de zonas horarias.
- **Como lo corregimos o que falta corregir?** Envolvemos la obtención de la fecha en un bloque defensivo `try-catch` que realiza un fallback automático a `java.time.Instant.ofEpochMilli(fecha.getTime()).toString()`, asegurando que la marca de tiempo se registre correctamente en cualquier entorno.

## 4. Explicacion en lenguaje llano

Imagina que nuestra plataforma de monitoreo es como una central de autobuses urbanos muy organizada. Para la fila de espera de lecturas de sensores usamos una cola de turnos donde el primero en llegar es el primero en ser atendido (FIFO). Para revisar las alertas en pantalla usamos un visor circular que permite avanzar a la siguiente o regresar a la anterior sin trabarse. Y para saber qué estaciones de la ciudad están conectadas entre sí, cada estación tiene una pequeña libreta donde solo apunta a sus vecinos directos, en vez de tener que imprimir un plano gigante lleno de casillas vacías.

### Ejemplo o analogia

Es como la ventanilla de un banco con fichas numeradas: si las personas son atendidas en orden de llegada (cola FIFO), el servicio es justo. Si en lugar de una cola usáramos una pila de papeles donde siempre atienden al último que puso su papel encima (pila LIFO), la persona que llegó primero se quedaría esperando para siempre en el fondo sin ser atendida nunca (inanición).

## 5. El vacio que encontre

Al intentar explicar el tema, identifica el punto que aun no comprendes bien.

- **Mi duda concreta es:** ¿Por qué `ArrayDeque` funciona tan bien en un solo hilo pero es peligroso en entornos con múltiples hilos, y cómo lo soluciona `ConcurrentLinkedQueue`?
- **Lo que ya puedo explicar es:** Que `ArrayDeque` no está sincronizado y si dos hilos intentan modificar los punteros `head` o `tail` al mismo tiempo, los datos se sobreescriben o se corrompen.
- **Para resolver la duda consulte:** La documentación del Java Collections Framework y el análisis del algoritmo no bloqueante de Michael & Scott.
- **Ahora lo entiendo asi:** `ConcurrentLinkedQueue` no usa bloqueos pesados (`synchronized`) que congelen los hilos; en su lugar, utiliza instrucciones a nivel de hardware llamadas CAS (*Compare-And-Swap*) que actualizan los punteros de manera atómica, permitiendo que muchos sensores envíen datos concurrentemente sin corromper la cola.

## 6. Trazado de la solucion

Trazado del ciclo de monitoreo continuo mediante rotación Round-Robin en `obtenerSiguienteSensor()`:

| Paso | Estado de la cola de monitoreo | Decisión o resultado |
|---|---|---|
| 1 | `colaMonitoreo = ["S1", "S2", "S3"]` | Se invoca `obtenerSiguienteSensor()` |
| 2 | Extracción del frente: `sensor = poll()` | Extrae `"S1"`. La cola queda con `["S2", "S3"]` |
| 3 | Reingreso al final: `add("S1")` | Se ubica `"S1"` al final. La cola queda `["S2", "S3", "S1"]`. Retorna `"S1"`. |
| 4 | Siguiente llamada a `obtenerSiguienteSensor()` | Extrae `"S2"`, reencola al final, cola queda `["S3", "S1", "S2"]`. Retorna `"S2"`. |
| 5 | Tercera llamada | Extrae `"S3"`, cola queda `["S1", "S2", "S3"]`. Retorna `"S3"`. Turno rotado al 100% con equidad. |

## 7. Decision de diseño

Relaciona lo aprendido con la Plataforma de Monitoreo Ambiental Urbano.

- **Problema que debiamos resolver:** Eliminar frecuentemente los eventos más antiguos del historial para ahorrar memoria en un flujo constante de miles de registros.
- **Estructura elegida:** `LinkedList<String>` para el atributo `historialEventos`.
- **Alternativa descartada:** `ArrayList<String>`.
- **Por que elegimos la primera:** En `LinkedList`, eliminar el primer elemento (`remove(0)`) toma tiempo constante **$O(1)$** porque únicamente se reasigna la referencia del puntero de la cabeza (`first = first.next`). En `ArrayList`, eliminar en la posición 0 cuesta **$O(n)$** porque la JVM debe copiar y mover en memoria todos los elementos restantes una casilla a la izquierda (`System.arraycopy`), degradando severamente el rendimiento del sistema.
- **Que evidencia respalda la decision:** Las pruebas en el banco de colecciones y la prueba unitaria `testOptimizarHistorial_DebeLimitarTamaño`, donde se descartan 90 eventos de 100 de manera instantánea.

## 8. Aporte al proyecto

- **Archivo(s) o modulo(s) trabajado(s):** 
  - `src/structures/management/GestorDeRed.java`
  - `test/structures/management/TestGestorDeRed.java`
  - `docs/Preguntas_Criterio.md`
  - `docs/EVIDENCIAS_PRUEBAS.md`
  - `ejecutar_tests.bat` y `ejecutar_tests.ps1`
- **Cambio realizado:** Implementación funcional de los 10 retos planteados en la hoja de ruta, resolución detallada de las 5 preguntas de criterio de decisión, adición de scripts de ejecución autónoma y documentación técnica exhaustiva para sustentación.
- **Como se conecta con la capa anterior:** Integra las estructuras lineales de las semanas anteriores (listas simples, listas dobles, pilas y colas circulares) bajo las abstracciones estandarizadas del Java Collections Framework (JCF), conectándolas con el grafo de topología de red.
- **Que queda pendiente para la siguiente semana:** Ninguna tarea pendiente; se alcanzó el 100% de completitud para la entrega y sustentación final del proyecto integrador.

## 9. Commits realizados

| Commit | Mensaje | Que demuestra |
|---|---|---|
| `e8bd9b6` | `feat(gestor): implementar retos 1 al 10 y filtro de alertas criticas con JCF` | Implementación de todos los retos y reglas de negocio del gestor |
| `d0aa82f` | `test(gestor): ampliar suite de pruebas unitarias para 100% de aprobacion` | Validación de todas las funcionalidades y casos de borde |
| `caf4eb6` | `docs(criterio): responder y justificar las 5 preguntas de criterio de decision tecnica` | Argumentación teórica, matemática y arquitectónica de las estructuras |
| `f2e4ea5` | `chore(tests): anadir evidencias de 100% de aprobacion, scripts de ejecucion y robustez en marca de tiempo` | Generación de evidencias y automatización de pruebas |
| `ecdd6d9` | `docs(code): limpiar encabezados y formatear comentarios tecnicos de manera profesional` | Documentación técnica limpia y clara dentro del código fuente |

## 10. Reexplicacion final

La selección de colecciones en Java no debe basarse en la costumbre sino en el análisis del costo computacional ($Big\text{-}O$) de las operaciones críticas: utilizamos `LinkedList` para el historial por su eliminación frontal en $O(1)$; `ArrayDeque` para navegación de alertas y buffer de ingesta por su manipulación en ambos extremos en $O(1)$; una `Queue` para garantizar equidad temporal estricta (Round-Robin) evitando la inanición que provocaría una pila; y un `Map` (lista de adyacencia) para modelar la topología de la red con un consumo espacial óptimo de $O(V + E)$ adaptado a redes IoT dispersas.

## 11. Reflexion individual

1. **Lo que ahora puedo hacer y antes no podia:**
   Puedo elegir y justificar con argumentos matemáticos de complejidad ($Big\text{-}O$) la colección adecuada del Java Collections Framework para cualquier problema de ingeniería de software, en lugar de usar `ArrayList` para todo por defecto.
2. **El error o supuesto que mas me enseno:**
   Suponer que las operaciones sobre arreglos siempre son rápidas; comprender el impacto oculto del desplazamiento de elementos en memoria (`System.arraycopy`) al borrar en la cabeza de un `ArrayList` me demostró la importancia de las estructuras enlazadas.
3. **La pregunta que llevaria a la proxima clase:**
   ¿Cómo cambia la arquitectura de almacenamiento cuando el volumen de lecturas de sensores satura la memoria RAM y se requiere persistencia distribuida o streaming reactivo?
4. **Que parte del trabajo fue realmente mia:**
   La implementación y verificación de los métodos del `GestorDeRed`, la depuración de los casos borde en las pruebas unitarias y la redacción del análisis técnico para la sustentación de las decisiones de diseño.

## Lista de verificacion antes de entregar

- [x] Escribi la prediccion antes de consultar el resultado.
- [x] Inclui evidencia concreta del laboratorio.
- [x] Explique un concepto sin depender de jerga.
- [x] Registre un vacio, una duda o un error real.
- [x] Trace al menos un caso paso a paso.
- [x] Justifique una decision del proyecto y una alternativa descartada.
- [x] Registre mis commits y mi aporte individual.
- [x] Deje claro que queda pendiente.
- [x] Renombre el archivo con el formato `sXX-nombre.md`.
