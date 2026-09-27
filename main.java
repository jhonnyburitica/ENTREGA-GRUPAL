package co.edu.poligran.proyectoventas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Procesa información de productos, vendedores y ventas.
 *
 * Genera un reporte de vendedores ordenado por dinero recaudado
 * y un reporte de productos ordenado por unidades vendidas.
 *
 * Permite procesar varios archivos del mismo vendedor y detecta
 * formatos incorrectos, datos duplicados y valores negativos.
 * @authores: 
 * Jhonny Alberto Buritica Echeverri.
 * Daniel Arias Diaz
 * Juan Cubillos Castillo
 * Yuri Alejandra Cruz Bonilla
 */
public class main {

    /*
     * Usa la misma codificación predeterminada que FileWriter
     * en el generador actual. Ambas clases deben ejecutarse
     * con la misma configuración de codificación.
     */
    private static final Charset CODIFICACION = Charset.defaultCharset();

    /**
     * Ejecuta el procesamiento sin solicitar datos al usuario.
     *
     * @param args argumentos no utilizados
     */
    public static void main(String[] args) {

        Map<String, String> nombresProductos = new HashMap<>();
        Map<String, BigDecimal> preciosProductos = new HashMap<>();
        Map<String, Long> cantidadesProductos = new HashMap<>();

        Map<String, String> nombresVendedores = new HashMap<>();
        Map<String, BigDecimal> recaudosVendedores = new HashMap<>();

        try {
            leerProductos(
                    nombresProductos,
                    preciosProductos,
                    cantidadesProductos
            );

            leerVendedores(
                    nombresVendedores,
                    recaudosVendedores
            );

            procesarVentas(
                    preciosProductos,
                    cantidadesProductos,
                    nombresVendedores,
                    recaudosVendedores
            );

            generarReporteVendedores(
                    nombresVendedores,
                    recaudosVendedores
            );

            generarReporteProductos(
                    nombresProductos,
                    preciosProductos,
                    cantidadesProductos
            );

            System.out.println();
            System.out.println("Procesamiento finalizado correctamente.");
            System.out.println("Archivo creado: reporte_vendedores.csv");
            System.out.println("Archivo creado: reporte_productos.csv");

        } catch (IOException e) {
            System.out.println(
                    "No se pudo completar el procesamiento de los archivos."
            );
            System.out.println("Detalle: " + e.getMessage());
            System.out.println(
                    "Los reportes existentes podrían corresponder "
                    + "a una ejecución anterior."
            );
        }
    }

    /**
     * Lee los productos y valida sus identificadores, nombres y precios.
     *
     * @param nombres nombres asociados a cada ID
     * @param precios precios asociados a cada ID
     * @param cantidades cantidades vendidas, inicialmente en cero
     * @throws IOException si el archivo no puede leerse o contiene errores
     */
    private static void leerProductos(
            Map<String, String> nombres,
            Map<String, BigDecimal> precios,
            Map<String, Long> cantidades) throws IOException {

        File archivo = new File("productos.txt");

        try (BufferedReader lector = Files.newBufferedReader(
                archivo.toPath(), CODIFICACION)) {

            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                String[] datos = separarCampos(
                        linea, 3, archivo.getName(), numeroLinea, false
                );

                String idProducto = datos[0];
                String nombreProducto = datos[1];

                if (nombres.containsKey(idProducto)) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "ID de producto repetido: " + idProducto
                    );
                }

                BigDecimal precio;

                try {
                    precio = new BigDecimal(datos[2]);

                } catch (NumberFormatException e) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "Precio inválido. Use números y punto decimal."
                    );
                }

                if (precio.signum() < 0) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "El precio no puede ser negativo."
                    );
                }

                nombres.put(idProducto, nombreProducto);
                precios.put(idProducto, precio);
                cantidades.put(idProducto, 0L);
            }
        }

        if (nombres.isEmpty()) {
            throw new IOException("productos.txt no contiene productos.");
        }
    }

    /**
     * Lee los vendedores y asigna un recaudo inicial de cero.
     *
     * @param nombres nombres completos asociados al tipo y número de documento
     * @param recaudos dinero acumulado por vendedor
     * @throws IOException si el archivo no puede leerse o contiene errores
     */
    private static void leerVendedores(
            Map<String, String> nombres,
            Map<String, BigDecimal> recaudos) throws IOException {

        File archivo = new File("vendedores.txt");

        try (BufferedReader lector = Files.newBufferedReader(
                archivo.toPath(), CODIFICACION)) {

            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                String[] datos = separarCampos(
                        linea, 4, archivo.getName(), numeroLinea, false
                );

                String documento = datos[0] + ";" + datos[1];
                String nombreCompleto = datos[2] + " " + datos[3];

                if (nombres.containsKey(documento)) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "Documento de vendedor repetido: " + documento
                    );
                }

                nombres.put(documento, nombreCompleto);
                recaudos.put(documento, BigDecimal.ZERO);
            }
        }

        if (nombres.isEmpty()) {
            throw new IOException("vendedores.txt no contiene vendedores.");
        }
    }

    /**
     * Busca y procesa todos los archivos ventas_*.txt de la carpeta del proyecto.
     *
     * @param precios precios de los productos
     * @param cantidades unidades acumuladas por producto
     * @param vendedores nombres de los vendedores registrados
     * @param recaudos dinero acumulado por vendedor
     * @throws IOException si se encuentran archivos o datos incorrectos
     */
    private static void procesarVentas(
            Map<String, BigDecimal> precios,
            Map<String, Long> cantidades,
            Map<String, String> vendedores,
            Map<String, BigDecimal> recaudos) throws IOException {

        File carpeta = new File(".");
        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            throw new IOException(
                    "No se pudo revisar la carpeta del proyecto."
            );
        }

        int archivosProcesados = 0;

        for (File archivo : archivos) {

            if (!archivo.isFile()
                    || !archivo.getName().startsWith("ventas_")
                    || !archivo.getName().endsWith(".txt")) {
                continue;
            }

            procesarArchivoVentas(
                    archivo,
                    precios,
                    cantidades,
                    vendedores,
                    recaudos
            );

            archivosProcesados++;
        }

        System.out.println(
                "Archivos de ventas procesados: " + archivosProcesados
        );

        if (archivosProcesados == 0) {
            System.out.println(
                    "No se encontraron ventas. Los recaudos serán cero."
            );
        }
    }

    /**
     * Procesa un archivo de ventas y acumula sus resultados.
     *
     * @param archivo archivo que contiene las ventas
     * @param precios precios de los productos
     * @param cantidades unidades acumuladas por producto
     * @param vendedores vendedores registrados
     * @param recaudos dinero acumulado por vendedor
     * @throws IOException si el archivo contiene información inválida
     */
    private static void procesarArchivoVentas(
            File archivo,
            Map<String, BigDecimal> precios,
            Map<String, Long> cantidades,
            Map<String, String> vendedores,
            Map<String, BigDecimal> recaudos) throws IOException {

        try (BufferedReader lector = Files.newBufferedReader(
                archivo.toPath(), CODIFICACION)) {

            String primeraLinea = lector.readLine();

            if (primeraLinea == null) {
                throw new IOException(
                        "El archivo está vacío: " + archivo.getName()
                );
            }

            String[] cabecera = separarCampos(
                    primeraLinea, 2, archivo.getName(), 1, false
            );

            String documento = cabecera[0] + ";" + cabecera[1];

            if (!vendedores.containsKey(documento)) {
                throw error(
                        archivo.getName(),
                        1,
                        "Vendedor no registrado: " + documento
                );
            }

            String linea;
            int numeroLinea = 1;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                String[] datos = separarCampos(
                        linea, 2, archivo.getName(), numeroLinea, true
                );

                String idProducto = datos[0];

                if (!precios.containsKey(idProducto)) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "Producto no registrado: " + idProducto
                    );
                }

                long cantidad;

                try {
                    cantidad = Long.parseLong(datos[1]);

                } catch (NumberFormatException e) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "La cantidad debe ser un entero dentro "
                            + "del rango permitido."
                    );
                }

                if (cantidad < 0) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "La cantidad no puede ser negativa."
                    );
                }

                long nuevaCantidad;

                try {
                    nuevaCantidad = Math.addExact(
                            cantidades.get(idProducto), cantidad
                    );

                } catch (ArithmeticException e) {
                    throw error(
                            archivo.getName(),
                            numeroLinea,
                            "La cantidad acumulada supera el rango permitido."
                    );
                }

                BigDecimal valorVenta = precios.get(idProducto)
                        .multiply(BigDecimal.valueOf(cantidad));

                cantidades.put(idProducto, nuevaCantidad);

                recaudos.put(
                        documento,
                        recaudos.get(documento).add(valorVenta)
                );
            }
        }
    }

    /**
     * Separa y valida los campos de una línea.
     *
     * @param linea texto que se va a separar
     * @param esperados número de campos requeridos
     * @param archivo nombre del archivo de origen
     * @param numeroLinea número de línea para los mensajes de error
     * @param permitirSeparadorFinal permite un punto y coma final en una venta
     * @return campos sin espacios exteriores y sin valores vacíos
     * @throws IOException si la estructura es incorrecta
     */
    private static String[] separarCampos(
            String linea,
            int esperados,
            String archivo,
            int numeroLinea,
            boolean permitirSeparadorFinal) throws IOException {

        // Admite una marca de codificación al inicio del archivo.
        if (numeroLinea == 1 && linea.startsWith("\uFEFF")) {
            linea = linea.substring(1);
        }

        linea = linea.trim();

        // La guía muestra un punto y coma opcional al final de cada venta.
        if (permitirSeparadorFinal && linea.endsWith(";")) {
            linea = linea.substring(0, linea.length() - 1);
        }

        // El -1 conserva los campos vacíos para poder detectarlos.
        String[] datos = linea.split(";", -1);

        if (datos.length != esperados) {
            throw error(
                    archivo,
                    numeroLinea,
                    "Se esperaban " + esperados
                    + " campos separados por punto y coma."
            );
        }

        for (int i = 0; i < datos.length; i++) {
            datos[i] = datos[i].trim();

            if (datos[i].isEmpty()) {
                throw error(
                        archivo,
                        numeroLinea,
                        "Hay un campo vacío."
                );
            }
        }

        return datos;
    }

    /**
     * Escribe todos los vendedores ordenados por recaudo descendente.
     *
     * @param nombres nombres completos de los vendedores
     * @param recaudos dinero recaudado por cada vendedor
     * @throws IOException si no se puede escribir el reporte
     */
    private static void generarReporteVendedores(
            Map<String, String> nombres,
            Map<String, BigDecimal> recaudos) throws IOException {

        List<String> ordenados = new ArrayList<>(nombres.keySet());

        ordenados.sort((documento1, documento2) -> {

            int comparacion = recaudos.get(documento2)
                    .compareTo(recaudos.get(documento1));

            if (comparacion != 0) {
                return comparacion;
            }

            comparacion = nombres.get(documento1)
                    .compareTo(nombres.get(documento2));

            if (comparacion != 0) {
                return comparacion;
            }

            return documento1.compareTo(documento2);
        });

        Path salida = new File("reporte_vendedores.csv").toPath();

        try (BufferedWriter escritor = Files.newBufferedWriter(
                salida, CODIFICACION)) {

            System.out.println();
            System.out.println("TOTAL RECAUDADO POR VENDEDOR");

            for (String documento : ordenados) {

                String nombre = nombres.get(documento);
                String total = recaudos.get(documento).toPlainString();

                escritor.write(nombre + ";" + total);
                escritor.newLine();

                System.out.println(nombre + " | Total: " + total);
            }
        }
    }

    /**
     * Escribe los productos vendidos ordenados por cantidad descendente.
     * Cada línea contiene nombre y precio, como indica la guía.
     *
     * @param nombres nombres de los productos
     * @param precios precios por unidad
     * @param cantidades unidades vendidas por producto
     * @throws IOException si no se puede escribir el reporte
     */
    private static void generarReporteProductos(
            Map<String, String> nombres,
            Map<String, BigDecimal> precios,
            Map<String, Long> cantidades) throws IOException {

        List<String> ordenados = new ArrayList<>(nombres.keySet());

        ordenados.sort((producto1, producto2) -> {

            int comparacion = Long.compare(
                    cantidades.get(producto2),
                    cantidades.get(producto1)
            );

            if (comparacion != 0) {
                return comparacion;
            }

            comparacion = nombres.get(producto1)
                    .compareTo(nombres.get(producto2));

            if (comparacion != 0) {
                return comparacion;
            }

            return producto1.compareTo(producto2);
        });

        Path salida = new File("reporte_productos.csv").toPath();

        try (BufferedWriter escritor = Files.newBufferedWriter(
                salida, CODIFICACION)) {

            System.out.println();
            System.out.println("UNIDADES VENDIDAS POR PRODUCTO");

            for (String idProducto : ordenados) {

                long cantidad = cantidades.get(idProducto);

                System.out.println(
                        nombres.get(idProducto)
                        + " | Unidades vendidas: " + cantidad
                );

                if (cantidad > 0) {
                    escritor.write(
                            nombres.get(idProducto)
                            + ";"
                            + precios.get(idProducto).toPlainString()
                    );

                    escritor.newLine();
                }
            }
        }
    }

    /**
     * Construye un mensaje que identifica dónde se encontró un error.
     *
     * @param archivo nombre del archivo
     * @param linea número de línea
     * @param detalle descripción del problema
     * @return excepción con información del error
     */
    private static IOException error(
            String archivo, int linea, String detalle) {

        return new IOException(
                archivo + ", línea " + linea + ": " + detalle
        );
    }
}