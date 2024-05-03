package practica4PCD;

import java.util.Random;
import messagepassing.MailBox;

//javadoc

/**
 * Clase Cliente
 * 
 * Esta clase representa a un cliente que va a realizar una compra.
 * 
 */
public class Cliente implements Runnable{
	private int id;
	private int tiempoEstimado;
	private MailBox envioControlador, recepcionControlador;
	private MailBox envioColaCaja;
	private MailBox recepcionPagarCaja;
	private MailBox impresionTerminal, enviarPermisoImpresion;
	private MailBox liberarTerminal;
	
	
	/**
	 * Constructor de la clase Cliente
	 * 
	 * @param id                     Identificador del cliente
	 * @param envioControlador       MailBox para enviar mensajes al controlador
	 * @param recepcionControlador   MailBox para recibir mensajes del controlador
	 * @param envioColaCaja          MailBox para enviar mensajes sobre acceso a la caja
	 * @param recepcionPagarCaja     MailBox para recibir mensajes del controlador para acceder a la caja
	 * @param impresionTerminal      MailBox para enviar mensajes al controlador pidiendo permiso de la terminal de
	 *                               impresión
	 * @param enviarPermisoImpresion MailBox para recibir mensajes de permiso de
	 *                               impresión
	 * @param liberarTerminal        MailBox para liberar la terminal
	 */
	public Cliente(int id, MailBox envioControlador, MailBox recepcionControlador, MailBox envioColaCaja, MailBox recepcionPagarCaja, MailBox impresionTerminal, MailBox enviarPermisoImpresion, MailBox liberarTerminal){
		this.id = id;
		this.tiempoEstimado = 0;
		this.envioControlador = envioControlador;
		this.recepcionControlador = recepcionControlador;
		this.envioColaCaja = envioColaCaja;
		this.recepcionPagarCaja = recepcionPagarCaja;
		this.impresionTerminal = impresionTerminal;
		this.enviarPermisoImpresion = enviarPermisoImpresion;
		this.liberarTerminal = liberarTerminal;
	}
	
	/**
	 * Método run
	 * 
	 * Este método se encarga de ejecutar las acciones que realizará el cliente.
	 * 
	 */
	@Override
	public void run() {
		try {
			Random rand = new Random();
            int tiempoAleatorio = rand.nextInt(1000) + 1;
            Thread.sleep(tiempoAleatorio);				
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 5; i++) {
			while(true) { //Repetirá 5 veces esto
				envioControlador.send(this.id);
				Object token = recepcionControlador.receive();
				String cajaAsignada = getCajaFromToken(token);
				this.tiempoEstimado = getTiempoEstimadoFromToken(token);
				envioColaCaja.send(this.id + "|" + cajaAsignada);
				Object tokenPagarONo = recepcionPagarCaja.receive();
				//Al recibir mensaje, significa que puedo pagar, por tanto, deberé hacer un Sleep del tiempoAsignado y enviar una notificación cuando haya pagado para liberar la caja
				try {
					Thread.sleep(this.tiempoEstimado * 1000); 
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				impresionTerminal.send(createToken(this.id, cajaAsignada, this.tiempoEstimado));
				Object tokenPermiso = enviarPermisoImpresion.receive();
				int idPermiso = (int) (tokenPermiso);
				if (idPermiso == this.id) {
				    imprimirTicket(cajaAsignada);
				    liberarTerminal.send(cajaAsignada);
				}
				break;
			}
			
		}
	}
	
	/**
	 * Método imprimirTicket
	 * 
	 * Este método se encarga de imprimir el ticket de compra.
	 * 
	 * @param cajaAsignada Caja asignada al cliente
	 */
	public void imprimirTicket(String cajaAsignada) {
		System.out.println("--------------------------------------------------------------\n"
				+ "“Persona " + this.id + " ha usado la caja "+ cajaAsignada + "\n"
				+ " Tiempo de pago = " + this.tiempoEstimado + "\n"
				+ " Thread.sleep("+ this.tiempoEstimado +")\n"
				+ " Persona " + this.id + " liberando la caja " + cajaAsignada + "\n"
				+ "--------------------------------------------------------------\n");
	}

	/**
	 * Método createToken
	 * 
	 * Este método se encarga de crear un token con la información del cliente.
	 * 
	 * @param id             Identificador del cliente
	 * @param caja           Caja asignada al cliente
	 * @param tiempoEstimado Tiempo estimado de pago
	 * @return Token con la información del cliente
	 */
	
	public String createToken(int id, String caja, int tiempoEstimado) {
		return id + "|" + caja + "|" + tiempoEstimado;
	}
	
	
	/**
	 * Método getIDFromToken
	 * 
	 * Este método se encarga de obtener el identificador del cliente de un token.
	 * 
	 * @param token Token con la información del cliente
	 * @return Identificador del cliente
	 */
	public int getIDFromToken(Object token) {
        String[] tokenParts = token.toString().split("\\|");
        return Integer.parseInt(tokenParts[0]);
	}
	
	
	/**
	 * Método getCajaFromToken
	 * 
	 * Este método se encarga de obtener la caja asignada al cliente de un token.
	 * 
	 * @param token Token con la información del cliente
	 * @return Caja asignada al cliente
	 */
	public String getCajaFromToken(Object token) {
		String[] tokenParts = token.toString().split("\\|");
		return tokenParts[1];
	}
	
	
	/**
	 * Método getTiempoEstimadoFromToken
	 * 
	 * Este método se encarga de obtener el tiempo estimado de pago de un token.
	 * 
	 * @param token Token con la información del cliente
	 * @return Tiempo estimado de pago
	 */
	public int getTiempoEstimadoFromToken(Object token) {
		String[] tokenParts = token.toString().split("\\|");
		return Integer.parseInt(tokenParts[2]);
	}
	
	/**
	 * Método getId
	 * 
	 * Este método se encarga de obtener el identificador del cliente.
	 * 
	 * @return Identificador del cliente
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Método setId
	 * 
	 * Este método se encarga de establecer el identificador del cliente.
	 * 
	 * @param id Identificador del cliente
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	
	/**
	 * Método getTiempoEstimado
	 * 
	 * Este método se encarga de obtener el tiempo estimado de pago.
	 * 
	 * @return Tiempo estimado de pago
	 */
	public int getTiempoEstimado() {
		return tiempoEstimado;
	}
	
	/**
	 * Método setTiempoEstimado
	 * 
	 * Este método se encarga de establecer el tiempo estimado de pago.
	 * 
	 * @param tiempoEstimado Tiempo estimado de pago
	 */
	public void setTiempoEstimado(int tiempoEstimado) {
		this.tiempoEstimado = tiempoEstimado;
	}
	
	/**
	 * Método getEnvioControlador
	 * 
	 * Este método se encarga de obtener el MailBox para enviar mensajes al
	 * controlador.
	 * 
	 * @return MailBox para enviar mensajes al controlador
	 */
	public MailBox getEnvioControlador() {
		return envioControlador;
	}
	
	/**
	 * Método setEnvioControlador
	 * 
	 * Este método se encarga de establecer el MailBox para enviar mensajes al
	 * controlador.
	 * 
	 * @param envioControlador MailBox para enviar mensajes al controlador
	 */
	public void setEnvioControlador(MailBox envioControlador) {
		this.envioControlador = envioControlador;
	}
	
	/**
	 * Método getRecepcionControlador
	 * 
	 * Este método se encarga de obtener el MailBox para recibir mensajes del
	 * controlador.
	 * 
	 * @return MailBox para recibir mensajes del controlador
	 */
	public MailBox getRecepcionControlador() {
		return recepcionControlador;
	}
	
	/**
	 * Método setRecepcionControlador
	 * 
	 * Este método se encarga de establecer el MailBox para recibir mensajes del
	 * controlador.
	 * 
	 * @param recepcionControlador MailBox para recibir mensajes del controlador
	 */
	public void setRecepcionControlador(MailBox recepcionControlador) {
		this.recepcionControlador = recepcionControlador;
	}
	
	/**
	 * Método getEnvioColaCaja
	 * 
	 * Este método se encarga de obtener el MailBox para enviar mensajes a la cola
	 * de caja.
	 * 
	 * @return MailBox para enviar mensajes a la cola de caja
	 */
	public MailBox getEnvioColaCaja() {
		return envioColaCaja;
	}
	
	
	/**
	 * Método setEnvioColaCaja
	 * 
	 * Este método se encarga de establecer el MailBox para enviar mensajes a la
	 * cola de caja.
	 * 
	 * @param envioColaCaja MailBox para enviar mensajes a la cola de caja
	 */
	public void setEnvioColaCaja(MailBox envioColaCaja) {
		this.envioColaCaja = envioColaCaja;
	}
	
	/**
	 * Método getRecepcionPagarCaja
	 * 
	 * Este método se encarga de obtener el MailBox para recibir mensajes de la caja
	 * para pagar.
	 * 
	 * @return MailBox para recibir mensajes de la caja para pagar
	 */
	
	public MailBox getRecepcionPagarCaja() {
		return recepcionPagarCaja;
	}
	
	
	/**
	 * Método setRecepcionPagarCaja
	 * 
	 * Este método se encarga de establecer el MailBox para recibir mensajes de la
	 * caja para pagar.
	 * 
	 * @param recepcionPagarCaja MailBox para recibir mensajes de la caja para pagar
	 */
	public void setRecepcionPagarCaja(MailBox recepcionPagarCaja) {
		this.recepcionPagarCaja = recepcionPagarCaja;
	}
	
	
	/**
	 * Método getImpresionTerminal
	 * 
	 * Este método se encarga de obtener el MailBox para enviar mensajes a la
	 * terminal de impresión.
	 * 
	 * @return MailBox para enviar mensajes a la terminal de impresión
	 */
	public MailBox getImpresionTerminal() {
		return impresionTerminal;
	}
	
	
	/**
	 * Método setImpresionTerminal
	 * 
	 * Este método se encarga de establecer el MailBox para enviar mensajes a la
	 * terminal de impresión.
	 * 
	 * @param impresionTerminal MailBox para enviar mensajes a la terminal de
	 *                          impresión
	 */
	public void setImpresionTerminal(MailBox impresionTerminal) {
		this.impresionTerminal = impresionTerminal;
	}


	
	
}
