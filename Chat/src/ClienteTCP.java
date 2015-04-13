import java.net.Socket;
import java.util.Scanner;
import java.io.*;

public class ClienteTCP
{

	//MÉTODO PRINCIPAL DA CLASSE
	public static void main (String args[])
	{
		
		Scanner ler = new Scanner(System.in);
		String s;
		
		try
		{
			//ENDEREÇO DO SERVIDOR
			String IPServidor = "10.14.7.27";
			int PortaServidor = 7000;
			
			//ESTABELECE CONEXÃO COM SERVIDOR
			System.out.println(" -C- Conectando ao servidor ->" + IPServidor + ":" +PortaServidor);
			Socket socktCli = new Socket (IPServidor,PortaServidor);
			System.out.println(" -C- Detalhes conexao :" + socktCli.toString()); //DETALHAMENTO (EXTRA)
			
			//CRIA UM PACOTE DE SAÍDA PARA ENVIAR MENSAGENS, ASSOCIANDO-O À CONEXÃO (c)
			ObjectOutputStream sCliOut = new ObjectOutputStream(socktCli.getOutputStream());
			
			for(int i = 0; i<3; i++){
				s = ler.nextLine();
				sCliOut.writeObject(s);//ESCREVE NO PACOTE
				System.out.println(" -C- Enviando mensagem...");
				sCliOut.flush(); //ENVIA O PACOTE
			}
			
			//CRIA UM PACOTE DE ENTRADA PARA RECEBER MENSAGENS, ASSOCIADO À CONEXÃO (c)
			ObjectInputStream sCliIn = new ObjectInputStream (socktCli.getInputStream());
			System.out.println(" -C- Recebendo mensagem...");
			String strMsg = sCliIn.readObject().toString(); //ESPERA (BLOQUEADO) POR UM PACOTE
			ler.close();
			//PROCESSA O PACOTE RECEBIDO
			System.out.println(" -C- Mensagem recebida: " + strMsg);

			//FINALIZA A CONEXÃO
			socktCli.close();
			System.out.println(" -C- Conexao finalizada...");
		}
		catch(Exception e) //SE OCORRER ALGUMA EXCESSÃO, ENTÃO DEVE SER TRATADA (AMIGAVELMENTE)
		{
			System.out.println(" -C- O seguinte problema ocorreu : \n" + e.toString());
		}
	}
}		