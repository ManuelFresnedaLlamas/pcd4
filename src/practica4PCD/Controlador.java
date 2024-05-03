package practica4PCD;

import java.util.LinkedList;

import messagepassing.MailBox;
import messagepassing.Selector;


/**
 * Esta clase representa el controlador del sistema.
 */
public class Controlador implements Runnable {
	private MailBox envioControlador;
	private MailBox[] recepcionControlador;
	private MailBox recepcionColaCaja;
	private MailBox[] envioPagarCaja;
	private MailBox impresionTerminal;
	private MailBox[] enviarPermisoImpresion;
	private MailBox liberarTerminal;
	private Selector selector;
	private LinkedList<Object> colaCajaA;
	private LinkedList<Object> colaCajaB;
	private boolean cajaALibre;
	private boolean cajaBLibre;
	
	
	/**
     * Constructor de la clase Controlador.
     * 
     * @param envioControlador         MailBox donde el controlador recibirá los diferentes mensajes.
     * @param recepcionControlador     Array de MailBox para enviar mensajes al Cliente inequivocamente.
     * @param recepcionColaCaja        MailBox para recibir mensajes de los clientes para ponerse en cola de caja.
     * @param envioPagarCaja           Array de MailBox para enviar mensajes a los clientes sobre poder pagar en caja.
     * @param impresionTerminal        MailBox para recibir mensajes de impresión.
     * @param enviarPermisoImpresion   Array de MailBox para enviar permisos de impresión.
     * @param liberarTerminal          MailBox para liberar terminales.
     */
	public Controlador(MailBox envioControlador, MailBox[] recepcionControlador, 
			MailBox recepcionColaCaja,
			MailBox[] envioPagarCaja, MailBox impresionTerminal,  MailBox[] enviarPermisoImpresion, MailBox liberarTerminal) {
		this.envioControlador = envioControlador;
		this.recepcionControlador = recepcionControlador;
		this.recepcionColaCaja = recepcionColaCaja;
		this.envioPagarCaja = envioPagarCaja;
		this.impresionTerminal = impresionTerminal;
		this.enviarPermisoImpresion = enviarPermisoImpresion;
		this.liberarTerminal = liberarTerminal;
		this.colaCajaA = new LinkedList<Object>();
		this.colaCajaB = new LinkedList<Object>();
		this.selector = new Selector();
		this.cajaALibre = true;
		this.cajaBLibre = true;
	}

	/**
     * Método que se ejecuta cuando se inicia la ejecución del hilo.
     */
	@Override
	public void run() {
		while (true) {
			selector.addSelectable(envioControlador, false);
			prepararSelector();
			switch (selector.selectOrBlock()) {
				case 1:
					Object token = envioControlador.receive();
					int id = getIDFromToken(token);
					int tiempoEstimado = (int) (Math.random() * 10 + 1);
					String cajaAsignada = asignarCaja(tiempoEstimado);
					String tokenToSend = createToken(id, cajaAsignada, tiempoEstimado);
					recepcionControlador[id].send(tokenToSend);
				case 2:
					Object tokenColaCaja = recepcionColaCaja.receive();
					if (tokenColaCaja != null) {
						int idColaCaja = getIDFromToken(tokenColaCaja);
						String caja = getCajaFromToken(tokenColaCaja);
						
						if (!isCajaLibre(caja)) {
							//recepcionColaCaja.send(tokenColaCaja);
							if (caja.equals("A")) {
								colaCajaA.add(tokenColaCaja);
							} else {
								colaCajaB.add(tokenColaCaja);
							}
                        } else {
                        	Object tokenToSend1 = tokenColaCaja;
							if (caja.equals("A") && colaCajaA.size() > 0) {
								tokenToSend1 = colaCajaA.poll();
							} else if (caja.equals("B") && colaCajaB.size() > 0) {
								tokenToSend1 = colaCajaB.poll();

                        	}
                        	cajaOcupada(caja);
                            envioPagarCaja[idColaCaja].send(tokenToSend1);
                        }
						
					}
				case 3:
					Object tokenPermisoImpresion = impresionTerminal.receive();
					String cajaLiberada = getCajaFromToken(tokenPermisoImpresion);
					int idImpresion = getIDFromToken(tokenPermisoImpresion);
					if (!isCajaLibre(cajaLiberada)) {
						enviarPermisoImpresion[idImpresion].send(idImpresion);
					} else {
						impresionTerminal.send(tokenPermisoImpresion);
					}
				case 4:
					// En este caso, recibiremos un mensaje de liberarTerminal, por tanto, deberemos
					// liberar la caja
					Object cajaLiberadaPorCliente =  liberarTerminal.receive();
					liberarCaja((String)cajaLiberadaPorCliente);
			}
		}	
	}
	/**
	 * Método que libera una caja.
	 * 
	 * @param caja Caja a liberar.
	 */
	public void liberarCaja(String caja) {
		if (caja.equals("A")) {
			cajaALibre = true;
		} else {
			cajaBLibre = true;
		}
	}
	
	/**
	 * Método que ocupa una caja.
	 * 
	 * @param caja Caja a ocupar.
	 */
	public void cajaOcupada(String caja) {
		if (caja.equals("A")) {
			cajaALibre = false;
		} else if (caja.equals("B")) {
			cajaBLibre = false;
		}
	}
	
	/**
	 * Método que comprueba si una caja está libre.
	 * 
	 * @param caja Caja a comprobar.
	 * @return true si la caja está libre, false en caso contrario.
	 */
	public boolean isCajaLibre(String caja) {
		if (caja.equals("A")) {
			return cajaALibre;
		} else {
			return cajaBLibre;
		}
	}
	
	
/*	
	public void enviarAColaCaja(int id, String caja) {
		if (caja.equals("A")) {
			colaCajaA.add(id);
		} else {
			colaCajaB.add(id);
		}
	}
	
	public boolean colaCajaVacia(String caja) {
		if (caja.equals("A")) {
			return colaCajaA.isEmpty();
		} else {
			return colaCajaB.isEmpty();
		}
	}

	*/
	
	
/**
 * Método que asigna una caja a un cliente en función del tiempo estimado.
 * 
 * @param tiempoEstimado Tiempo estimado de la compra del cliente.
 * @return Caja asignada al cliente.
 */
	public String asignarCaja(int tiempoEstimado) {
		if (tiempoEstimado >= 5) {
			return "A";
		} else {
			return "B";
		}
	}
	
	/**
	 * Método que obtiene la caja de un token.
	 * 
	 * @param token Token a analizar.
	 * @return Caja del token.
	 */
	public String getCajaFromToken(Object token) {
		String[] tokenParts = token.toString().split("\\|");
		return tokenParts[1];
	}

	/**
	 * Método que crea un token.
	 * 
	 * @param id             ID del cliente.
	 * @param caja           Caja asignada al cliente.
	 * @param tiempoEstimado Tiempo estimado de la compra del cliente.
	 * @return Token creado.
	 */
	public String createToken(int id, String caja, int tiempoEstimado) {
		return id + "|" + caja + "|" + tiempoEstimado;
	}
	
	/**
	 * Método que obtiene el ID de un token.
	 * 
	 * @param token Token a analizar.
	 * @return ID del token.
	 */
	public int getIDFromToken(Object token) {
        String[] tokenParts = token.toString().split("\\|");
        return Integer.parseInt(tokenParts[0]);
	}
	
	
	/**
	 * Método que prepara el selector.
	 */
	public void prepararSelector() {
		for (int i = 0; i < recepcionControlador.length; i++) {
			selector.addSelectable(recepcionControlador[i], false);
		}

		for (int i = 0; i < envioPagarCaja.length; i++) {
			selector.addSelectable(envioPagarCaja[i], false);
		}

		for (int i = 0; i < enviarPermisoImpresion.length; i++) {
            selector.addSelectable(enviarPermisoImpresion[i], false);
        }
	}
	
}