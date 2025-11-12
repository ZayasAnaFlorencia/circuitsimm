# circuitsimm (Spring Boot backend)
Proyecto backend en Java (Spring Boot) que replica la lógica del script Python intentomil.py.
- Paquete base: com.zayas.circuit
- Nombre visible: circuitsimm

## Ejecutar local
Requisitos: JDK 17+, Maven 3.8+

mvn clean package
mvn spring-boot:run

Endpoints principales (POST/GET):
- POST /api/circuit/example            -> crear circuito ejemplo
- POST /api/component                  -> agregar componente (JSON {"type","name","value"})
- POST /api/solver?method=cramer       -> cambiar solver (cramer|gauss-jordan|manual)
- POST /api/solve                      -> resolver circuito
- GET  /api/circuit/state              -> estado actual (componentes y soluciones)
- POST /api/schedule?scheduler=fcfs    -> ejecutar prueba de planificación (fcfs|round_robin|sjf)
- POST /api/report/save?name=...       -> guardar reporte (txt + csv) en carpeta reports/
- GET  /api/report/download?filename=  -> descargar reporte guardado
- POST /api/lang?lang=es               -> setear idioma (guardado mínimo en estado)

## Deploy en Render
1. Crear repo en GitHub con este proyecto.
2. En Render, crear nuevo Web Service desde repositorio, build command: `mvn package`, start command: `java -jar target/circuitsimm-0.0.1-SNAPSHOT.jar`
3. Configurar variables de entorno en Render:
   - `app.allowed.origin` : dominio permitido para CORS (ej: https://tusitio.netlify.app). Si se omite, permite '*'.

   - `app.reports.dir` : carpeta para guardar reportes (por defecto 'reports').

4. Desplegar y tomar la URL pública para configurar el frontend en Netlify.

## Inicializar repo local y subir a GitHub
- Editar `init_repo.sh` y reemplazar la variable REMOTE por tu repo en GitHub.
- Ejecutar: `./init_repo.sh` y luego `git push -u origin main`.
