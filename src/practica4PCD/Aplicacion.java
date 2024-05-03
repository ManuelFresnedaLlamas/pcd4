package practica4PCD;

import messagepassing.MailBox;


/**
 * Clase Aplicacion
 * 
 * Esta clase representa la aplicación que el proceso de compra del ejercicio 4.
 * 
 */
public class Aplicacion {
	

	/**
	 * Método main
	 * 
	 * Este método se encarga de ejecutar la aplicación.
	 * 
	 * @param args Argumentos de la aplicación
	 */
	public static void main(String[] args) {
		int numClientes = 30;

		MailBox envioControlador = new MailBox();
		MailBox[] recepcionControlador = new MailBox[numClientes]; 
		MailBox recepcionColaCaja = new MailBox();
		MailBox[] envioPagarCaja = new MailBox[numClientes];
		MailBox impresionTerminal = new MailBox();
		MailBox[] enviarPermisoImpresion = new MailBox[numClientes];
		MailBox liberarTerminal = new MailBox();

		for (int i = 0; i < numClientes; i++) {
			MailBox recepcionControlado = new MailBox();
			MailBox recepcionPagarCaja = new MailBox();
			MailBox enviarPermisoImpresionBucle = new MailBox();
			recepcionControlador[i] = recepcionControlado;
			envioPagarCaja[i] = recepcionPagarCaja;
			enviarPermisoImpresion[i] = enviarPermisoImpresionBucle;
			
		}
		
		Controlador controlador = new Controlador(envioControlador, recepcionControlador, recepcionColaCaja, envioPagarCaja, impresionTerminal, enviarPermisoImpresion, liberarTerminal);
		Thread hiloControlador = new Thread(controlador);
		hiloControlador.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < numClientes; i++) {
			Cliente cliente = new Cliente(i, envioControlador, recepcionControlador[i],
					recepcionColaCaja, envioPagarCaja[i], impresionTerminal, enviarPermisoImpresion[i], liberarTerminal);
			Thread hiloCliente = new Thread(cliente);
			hiloCliente.start();
		}

	}

}
