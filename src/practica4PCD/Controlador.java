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
	private MailBox liberarTerminal;
	private Selector selector;
	private LinkedList<Integer> colaCajaA;
	private LinkedList<Integer> colaCajaB;
	private boolean cajaALibre;
	private boolean cajaBLibre;
	
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
		this.colaCajaA = new LinkedList<Integer>();
		this.colaCajaB = new LinkedList<Integer>();
		this.selector = new Selector();
		this.cajaALibre = true;
		this.cajaBLibre = true;
	}
	
	
	@Override
	public void run() {
		while (true) {
			//System.out.println("Controlador esperando mensaje de cliente");
			selector.addSelectable(envioControlador, false);
			prepararSelector();
			switch (selector.selectOrBlock()) {
				case 1:
					Object token = envioControlador.receive();
					int id = getIDFromToken(token);
					//System.out.println("Controlador lee mensaje de clienteID: " + id);
					int tiempoEstimado = (int) (Math.random() * 10 + 1);
					String cajaAsignada = asignarCaja(tiempoEstimado);
					String tokenToSend = createToken(id, cajaAsignada, tiempoEstimado);
					recepcionControlador[id].send(tokenToSend);
				case 2:
					Object tokenColaCaja = recepcionColaCaja.receive();
					if (tokenColaCaja != null) {
						int idColaCaja = getIDFromToken(tokenColaCaja);
						String caja = getCajaFromToken(tokenColaCaja);
						//System.out.print(this.cajaALibre);
						//System.out.println(this.cajaBLibre);
						if (!isCajaLibre(caja)) {
							//System.out.println("Controlador envia a la cola de la caja " + caja + " al cliente " + idColaCaja);
							//System.out.print(this.cajaALibre);
							//System.out.println(this.cajaBLibre);
							//System.out.println("DEJARA PROCESO BLOQUEADO ESPERANDO RESPUESTA, POR TANTO LO TIRAMOS DE NUEVO");
							recepcionColaCaja.send(tokenColaCaja);
                           // enviarAColaCaja(idColaCaja, caja);
                        } else {
                        	cajaOcupada(caja);
                            //System.out.println("Controlador envia a caja " + caja + " al cliente " + idColaCaja + " A PAGAR DIRECTAMENTE");
                            //selector.addSelectable(envioPagarCaja[idColaCaja], false);
                            envioPagarCaja[idColaCaja].send(tokenColaCaja);
                        }
						
					}
				case 3:
					Object tokenPermisoImpresion = impresionTerminal.receive();
					//System.out.println("Controlador recibe mensaje de impresion " + tokenPermisoImpresion);
					String cajaLiberada = getCajaFromToken(tokenPermisoImpresion);
					int idImpresion = getIDFromToken(tokenPermisoImpresion);
					//System.out.println(cajaLiberada + " " + idImpresion);
					if (!isCajaLibre(cajaLiberada)) {
						//System.out.println("Controlador envia permiso de impresion al cliente " + idImpresion);
						enviarPermisoImpresion[idImpresion].send(idImpresion);
					} else {
						//System.out.println("Controlador envia a la cola de la caja " + cajaLiberada + " al cliente " + idImpresion);
						impresionTerminal.send(tokenPermisoImpresion);
					}
				case 4:
					// En este caso, recibiremos un mensaje de liberarTerminal, por tanto, deberemos
					// liberar la caja
					Object cajaLiberadaPorCliente =  liberarTerminal.receive();
					//System.out.println("Controlador recibe mensaje de liberar caja " + cajaLiberadaPorCliente);
					liberarCaja((String)cajaLiberadaPorCliente);
			}
		}	
	}
	
	public void liberarCaja(String caja) {
		if (caja.equals("A")) {
			//System.out.println("Controlador libera caja A");
			cajaALibre = true;
		} else {
			//System.out.println("Controlador libera caja B");
			cajaBLibre = true;
		}
	}
	
	public void cajaOcupada(String caja) {
		if (caja.equals("A")) {
			cajaALibre = false;
		} else if (caja.equals("B")) {
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