package practica4PCD;

import java.util.Random;

import messagepassing.MailBox;

public class Cliente implements Runnable{
	private int id;
	private int tiempoEstimado;
	private MailBox envioControlador, recepcionControlador;
	private MailBox envioColaCaja;
	private MailBox recepcionPagarCaja;
	private MailBox impresionTerminal, enviarPermisoImpresion;
	private MailBox liberarTerminal;
	
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
	
	@Override
	public void run() {
		try {
			Random rand = new Random();
            int tiempoAleatorio = rand.nextInt(1000) + 1;
                // Duerme el hilo durante el tiempo aleatorio generado
            Thread.sleep(tiempoAleatorio);				
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < 5; i++) {
			while(true) { //Repetirá 5 veces esto

			    //System.out.println("Cliente " + this.id + " envía mensaje al controlador");
				envioControlador.send(this.id);
				Object token = recepcionControlador.receive();
				String cajaAsignada = getCajaFromToken(token);
				this.tiempoEstimado = getTiempoEstimadoFromToken(token);
				envioColaCaja.send(this.id + "|" + cajaAsignada);
				//System.out.println("Cliente " + this.id + " envía mensaje de que quiere pagar en la caja " + cajaAsignada);
				Object tokenPagarONo = recepcionPagarCaja.receive();
				//System.out.println(tokenPagarONo);
				//Al recibir mensaje, significa que puedo pagar, por tanto, deberé hacer un Sleep del tiempoAsignado y enviar una notificación cuando haya pagado para liberar la caja
				try {
					Thread.sleep(this.tiempoEstimado * 100); //FIXME timer
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				impresionTerminal.send(createToken(this.id, cajaAsignada, this.tiempoEstimado));
				Object tokenPermiso = enviarPermisoImpresion.receive();
				int idPermiso = (int) (tokenPermiso);
				//System.out.println("Cliente: " + this.id + "Recibe Permiso: " + idPermiso);
				if (idPermiso == this.id) {
				    imprimirTicket(cajaAsignada);
				    liberarTerminal.send(cajaAsignada);
				}
				break;
			}
			
		}
	}
	
	public void imprimirTicket(String cajaAsignada) {
		System.out.println("--------------------------------------------------------------\n"
				+ "“Persona " + this.id + " ha usado la caja "+ cajaAsignada + "\n"
				+ " Tiempo de pago = " + this.tiempoEstimado + "\n"
				+ " Thread.sleep("+ this.tiempoEstimado +")\n"
				+ " Persona id liberando la caja " + cajaAsignada + "\n"
				+ "--------------------------------------------------------------\n");
	}

	public String createToken(int id, String caja, int tiempoEstimado) {
		return id + "|" + caja + "|" + tiempoEstimado;
	}
	
	public int getIDFromToken(Object token) {
        String[] tokenParts = token.toString().split("\\|");
        return Integer.parseInt(tokenParts[0]);
	}
	
	public String getCajaFromToken(Object token) {
		String[] tokenParts = token.toString().split("\\|");
		return tokenParts[1];
	}
	
	public int getTiempoEstimadoFromToken(Object token) {
		String[] tokenParts = token.toString().split("\\|");
		return Integer.parseInt(tokenParts[2]);
	}
	
	// Métodos get y setters
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getTiempoEstimado() {
		return tiempoEstimado;
	}
	
	public void setTiempoEstimado(int tiempoEstimado) {
		this.tiempoEstimado = tiempoEstimado;
	}
	
	public MailBox getEnvioControlador() {
		return envioControlador;
	}
	
	public void setEnvioControlador(MailBox envioControlador) {
		this.envioControlador = envioControlador;
	}
	
	public MailBox getRecepcionControlador() {
		return recepcionControlador;
	}
	
	public void setRecepcionControlador(MailBox recepcionControlador) {
		this.recepcionControlador = recepcionControlador;
	}
	
	public MailBox getEnvioColaCaja() {
		return envioColaCaja;
	}
	
	public void setEnvioColaCaja(MailBox envioColaCaja) {
		this.envioColaCaja = envioColaCaja;
	}
	
	public MailBox getRecepcionPagarCaja() {
		return recepcionPagarCaja;
	}
	
	public void setRecepcionPagarCaja(MailBox recepcionPagarCaja) {
		this.recepcionPagarCaja = recepcionPagarCaja;
	}
	
	public MailBox getImpresionTerminal() {
		return impresionTerminal;
	}
	
	public void setImpresionTerminal(MailBox impresionTerminal) {
		this.impresionTerminal = impresionTerminal;
	}


	
	
}
