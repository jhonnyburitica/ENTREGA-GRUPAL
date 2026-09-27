package co.edu.poligran.proyectoventas;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Clase encargada de generar archivos planos con información
 * pseudoaleatoria de productos, vendedores y ventas.
 *
 * Estos archivos serán utilizados posteriormente como entrada
 * para el programa principal del proyecto.
 *
 * @authores: 
 * Jhonny Alberto Buritica Echeverri.
 * Daniel Arias Diaz
 * Juan Cubillos Castillo
 * Yuri Alejandra Cruz Bonilla
 */
public class GenerateInfoFiles {

	// Cantidad de productos que se generarán.
	private static final int PRODUCTS_COUNT = 10;

	// Cantidad de vendedores que se generarán.
	private static final int SALESMAN_COUNT = 5;

	// Cantidad de ventas que tendrá cada vendedor.
	private static final int SALES_PER_SALESMAN = 5;

	// Primer identificador utilizado para los productos.
	private static final int FIRST_PRODUCT_ID = 1001;

	// Objeto utilizado para generar valores pseudoaleatorios.
	private static final Random RANDOM = new Random();

	/**
	 * Método principal del programa.
	 *
	 * Genera el archivo de productos, el archivo de vendedores
	 * y los respectivos archivos de ventas.
	 *
	 * @param args argumentos de ejecución del programa.
	 */
	public static void main(String[] args) {

		try {

			createProductsFile(PRODUCTS_COUNT);
			createSalesManInfoFile(SALESMAN_COUNT);

			System.out.println("------------------------------------");
			System.out.println("Archivos generados correctamente.");
			System.out.println("------------------------------------");

		} catch (IOException e) {

			System.out.println("Ocurrió un error durante la generación de archivos.");
			System.out.println("Detalle: " + e.getMessage());
		}
	}

	/**
	 * Crea un archivo de texto con información pseudoaleatoria
	 * de productos.
	 *
	 * Cada línea contiene:
	 * IDProducto;NombreProducto;PrecioPorUnidad
	 *
	 * @param productsCount cantidad de productos a generar.
	 * @throws IOException si ocurre un error al escribir el archivo.
	 */
	public static void createProductsFile(int productsCount)
			throws IOException {

		String[] nombresProductos = {
				"Laptop",
				"Mouse",
				"Teclado",
				"Monitor",
				"Audifonos",
				"Webcam",
				"Impresora",
				"MemoriaUSB",
				"DiscoSSD",
				"Parlante"
		};

		BufferedWriter bufferedWriter =
				new BufferedWriter(new FileWriter("productos.txt"));

		for (int i = 1; i <= productsCount; i++) {

			int idProducto = 1000 + i;

			String nombreProducto;

			if (i <= nombresProductos.length) {

				nombreProducto = nombresProductos[i - 1];

			} else {

				nombreProducto = "Producto" + i;
			}

			int precio = RANDOM.nextInt(90001) + 10000;

			bufferedWriter.write(
					idProducto + ";" +
					nombreProducto + ";" +
					precio
			);

			bufferedWriter.newLine();
		}

		bufferedWriter.close();

		System.out.println("Archivo productos.txt creado.");
	}

	/**
	 * Crea un archivo de texto con la información
	 * pseudoaleatoria de los vendedores.
	 *
	 * Cada línea contiene:
	 * TipoDocumento;NumeroDocumento;Nombre;Apellido
	 *
	 * Además, genera un archivo de ventas para cada vendedor.
	 *
	 * @param salesmanCount cantidad de vendedores a generar.
	 * @throws IOException si ocurre un error al escribir los archivos.
	 */
	public static void createSalesManInfoFile(int salesmanCount)
			throws IOException {

		String[] nombres = {
				"Carlos",
				"Laura",
				"Andres",
				"Sofia",
				"Mateo",
				"Valentina",
				"Daniel",
				"Camila"
		};

		String[] apellidos = {
				"Gomez",
				"Rodriguez",
				"Martinez",
				"Lopez",
				"Garcia",
				"Ramirez",
				"Torres",
				"Restrepo"
		};

		BufferedWriter bufferedWriter =
				new BufferedWriter(new FileWriter("vendedores.txt"));

		for (int i = 1; i <= salesmanCount; i++) {

			String nombre =
					nombres[RANDOM.nextInt(nombres.length)];

			String apellido =
					apellidos[RANDOM.nextInt(apellidos.length)];

			long numeroDocumento =
					1000000000L + i;

			bufferedWriter.write(
					"CC" + ";" +
					numeroDocumento + ";" +
					nombre + ";" +
					apellido
			);

			bufferedWriter.newLine();

			createSalesMenFile(
					SALES_PER_SALESMAN,
					nombre,
					numeroDocumento
			);
		}

		bufferedWriter.close();

		System.out.println("Archivo vendedores.txt creado.");
	}

	/**
	 * Crea un archivo de ventas para un vendedor específico.
	 *
	 * La primera línea contiene el tipo y número de documento.
	 * Las líneas siguientes contienen:
	 * IDProducto;CantidadVendida
	 *
	 * @param randomSalesCount cantidad de registros de ventas.
	 * @param name nombre del vendedor.
	 * @param id número de documento del vendedor.
	 * @throws IOException si ocurre un error al escribir el archivo.
	 */
	public static void createSalesMenFile(
			int randomSalesCount,
			String name,
			long id) throws IOException {

		String nombreArchivo =
				"ventas_" + name + "_" + id + ".txt";

		BufferedWriter bufferedWriter =
				new BufferedWriter(
						new FileWriter(nombreArchivo)
				);

		// Primera línea: información del vendedor.
		bufferedWriter.write("CC;" + id);
		bufferedWriter.newLine();

		for (int i = 1; i <= randomSalesCount; i++) {

			/*
			 * Genera solamente identificadores de productos
			 * existentes en productos.txt.
			 */
			int idProducto =
					FIRST_PRODUCT_ID +
					RANDOM.nextInt(PRODUCTS_COUNT);

			// Cantidad vendida entre 1 y 10.
			int cantidadVendida =
					RANDOM.nextInt(10) + 1;

			bufferedWriter.write(
					idProducto + ";" +
					cantidadVendida
			);

			bufferedWriter.newLine();
		}

		bufferedWriter.close();

		System.out.println(
				"Archivo de ventas creado para: " +
				name + " - " + id
		);
	}
}
