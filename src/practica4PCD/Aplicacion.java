package practica4PCD;

import messagepassing.MailBox;

public class Aplicacion {
	

	public static void main(String[] args) {
		int numClientes = 2;

		MailBox envioControlador = new MailBox();
		MailBox[] recepcionControlador = new MailBox[numClientes]; 
		MailBox recepcionColaCaja = new MailBox();
		MailBox[] envioPagarCaja = new MailBox[numClientes];
		MailBox impresionTerminal = new MailBox();
		MailBox[] enviarPermisoImpresion = new MailBox[numClientes];

		for (int i = 0; i < numClientes; i++) {
			MailBox recepcionControlado = new MailBox();
			MailBox envioColaCaja = new MailBox();
			MailBox recepcionPagarCaja = new MailBox();
			MailBox impresionTerminalBucle = new MailBox();
			MailBox enviarPermisoImpresionBucle = new MailBox();
			recepcionControlador[i] = recepcionControlado;
			envioPagarCaja[i] = recepcionPagarCaja;
			enviarPermisoImpresion[i] = enviarPermisoImpresionBucle;
			
		}
		
		Controlador controlador = new Controlador(envioControlador, recepcionControlador, recepcionColaCaja, envioPagarCaja, impresionTerminal, enviarPermisoImpresion);
		Thread hiloControlador = new Thread(controlador);
		hiloControlador.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < numClientes; i++) {
			Cliente cliente = new Cliente(i, envioControlador, recepcionControlador[i],
					recepcionColaCaja, envioPagarCaja[i], impresionTerminal, enviarPermisoImpresion[i]);
			Thread hiloCliente = new Thread(cliente);
			hiloCliente.start();
		}
	}

}
