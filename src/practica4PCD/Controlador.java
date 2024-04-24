package practica4PCD;

import java.util.LinkedList;

import messagepassing.MailBox;
import messagepassing.Selector;

public class Controlador implements Runnable {
	private MailBox envioControlador;
	private MailBox[] recepcionControlador;
	private MailBox recepcionColaCaja;
	private MailBox[] envioPagarCaja;
	private MailBox impresionTerminal;
	private MailBox[] enviarPermisoImpresion;
	private Selector selector;
	private LinkedList<Integer> colaCajaA;
	private LinkedList<Integer> colaCajaB;
	private boolean cajaALibre;
	private boolean cajaBLibre;
	
	public Controlador(MailBox envioControlador, MailBox[] recepcionControlador, 
			MailBox recepcionColaCaja,
			MailBox[] envioPagarCaja, MailBox impresionTerminal,  MailBox[] enviarPermisoImpresion) {
		this.envioControlador = envioControlador;
		this.recepcionControlador = recepcionControlador;
		this.recepcionColaCaja = recepcionColaCaja;
		this.envioPagarCaja = envioPagarCaja;
		this.impresionTerminal = impresionTerminal;
		this.enviarPermisoImpresion = enviarPermisoImpresion;
		this.colaCajaA = new LinkedList<Integer>();
		this.colaCajaB = new LinkedList<Integer>();
		this.selector = new Selector();
		this.cajaALibre = true;
		this.cajaBLibre = true;
	}
	
	
	@Override
	public void run() {
		while (true) {
			
			selector.addSelectable(envioControlador, false);
			prepararSelector();
			switch (selector.selectOrBlock()) {
				case 1:
					Object token = envioControlador.receive();
					int id = getIDFromToken(token);
					System.out.println("Controlador lee mensaje de clienteID: " + id);
					int tiempoEstimado = (int) (Math.random() * 10 + 1);
					String cajaAsignada = asignarCaja(tiempoEstimado);
					String tokenToSend = createToken(id, cajaAsignada, tiempoEstimado);
					recepcionControlador[id].send(tokenToSend);
					//break;
				case 2:
					//En este caso accederemos a la recepcionColaCaja y pondremos a la persona en la cola de la caja asignada
					Object tokenColaCaja = recepcionColaCaja.receive(); //Aqui recibiremos el ID|Caja
					if (tokenColaCaja != null) {
						int idColaCaja = getIDFromToken(tokenColaCaja);
						String caja = getCajaFromToken(tokenColaCaja);
						if (!isCajaLibre(caja)) {
							System.out.println("Controlador envia a la cola de la caja " + caja + " al cliente " + idColaCaja);
                            enviarAColaCaja(idColaCaja, caja);
                        } else {
                        	cajaOcupada(caja);
                            System.out.println("Controlador envia a caja " + caja + " al cliente " + idColaCaja + " A PAGAR DIRECTAMENTE");
                            selector.addSelectable(envioPagarCaja[idColaCaja], false);
                            envioPagarCaja[idColaCaja].send(tokenColaCaja);

                        }
						
					}
				case 3:
					/*Aqui tendremos dos opciones, puede que la caja ya este libre y tengamos que responder a un cliente
					 * de que ya puede pagar,o las cajas pueden seguir estando ocupadas y deberemos analizar si se ha recibido un mensaje
					 * de un cliente que ha pagado y quiere imprimir por pantalla, a lo cual deberemos darle permiso.
					 * 
					 * Sabremos que la caja esta libre cuando recibimos el mensaje de impresionPorTerminal, sino, estara pagando
					 */
					Object tokenPermisoImpresion = impresionTerminal.receive();
					String cajaLiberada = getCajaFromToken(tokenPermisoImpresion);
					int idImpresion = getIDFromToken(tokenPermisoImpresion);
					liberarCaja(cajaLiberada); 
					/* Quedara liberada y podemos dar paso al siguiente en la cola
					 * tambien hay que dar permiso de impresion de ticket
                     */
					enviarPermisoImpresion[idImpresion].send(idImpresion);
						
					break;
					
				
			}

		}	
	}
	
	public void liberarCaja(String caja) {
		if (caja.equals("A")) {
			cajaALibre = true;
		} else {
			cajaBLibre = true;
		}
	}
	
	public void cajaOcupada(String caja) {
		if (caja.equals("A")) {
			cajaALibre = false;
		} else {
			cajaBLibre = false;
		}
	}
	
	public boolean isCajaLibre(String caja) {
		if (caja.equals("A")) {
			return cajaALibre;
		} else {
			return cajaBLibre;
		}
	}
	
	
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

	
	public String asignarCaja(int tiempoEstimado) {
		if (tiempoEstimado >= 5) {
			return "A";
		} else {
			return "B";
		}
	}
	
	
	public String getCajaFromToken(Object token) {
		String[] tokenParts = token.toString().split("\\|");
		return tokenParts[1];
	}

	public String createToken(int id, String caja, int tiempoEstimado) {
		return id + "|" + caja + "|" + tiempoEstimado;
	}
	
	public int getIDFromToken(Object token) {
        String[] tokenParts = token.toString().split("\\|");
        return Integer.parseInt(tokenParts[0]);
	}
	
	public void prepararSelector() {
		for (int i = 0; i < recepcionControlador.length; i++) {
			selector.addSelectable(recepcionControlador[i], false);
		}
		/*for (int i = 0; i < recepcionColaCaja.length; i++) {
			selector.addSelectable(recepcionColaCaja[i], false);
		}*/
		for (int i = 0; i < envioPagarCaja.length; i++) {
			selector.addSelectable(envioPagarCaja[i], false);
		}
		/*for (int i = 0; i < impresionTerminal.length; i++) {
			selector.addSelectable(impresionTerminal[i], false);
		}*/
		for (int i = 0; i < enviarPermisoImpresion.length; i++) {
            selector.addSelectable(enviarPermisoImpresion[i], false);
        }
	}
	
}