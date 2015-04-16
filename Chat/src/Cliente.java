import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class Cliente extends Thread {
    
    private final Socket conexao;
    
    public Cliente(Socket socket) {
        this.conexao = socket;
    }
    public static void main(String args[])
    {
        try {
            // conecta a IP do Servidor, Porta
            Socket socket = new Socket("127.0.0.1", 7000);
            
            PrintStream saida = new PrintStream(socket.getOutputStream());
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
           
            System.out.println("==============================================================================");
            System.out.println("========================== CHAT MULTIUSUÁRIOS TCP ============================");
            System.out.println("DIGITE ~LISTAR para visualizar os usuários logados, ou ~SAIR para fazer LOGOFF");
            System.out.println("==============================================================================");
            
            System.out.print("Digite seu nome: ");
            String meuNome = teclado.readLine();
            
            conectar(saida, meuNome.toUpperCase());
            
            Thread thread = new Cliente(socket);
            thread.start();
            
            String texto;
            
            while (true)
            {
                try
                {	
                	texto = teclado.readLine();
                    JSONObject mensagem = new JSONObject();
                    
                    if(texto.equals("~LISTAR")) {
                    	mensagem.put("OPERACAO", "LISTAR");
                    }
                    else
                    	if(texto.equals("~DESLOGAR")) {
                    		mensagem.put("OPERACAO", "SAIR");
                    	}
                    	else {
                    		mensagem.put("OPERACAO", "ENVIAR");
                    		mensagem.put("MENSAGEM", texto);
                    		System.out.println("VOCÊ disse: " + texto);
                    	}
                    
                    saida.println(mensagem.toString());
                }
                catch (Exception e)
                {
                }
            }
        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
        }
    }
    
    // execução da thread
    @Override
    public void run()
    {
        try {
            //recebe mensagens de outro cliente através do servidor
            BufferedReader entrada = 
                new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            
            String msg = "";
            String in;
            while (true)
            {
            	in = entrada.readLine();
                if (in == null) {
                    System.exit(0);
                }
                else {
                	// pega o que o servidor enviou
                	try {
    					JSONObject json = new JSONObject(in);
    					msg = json.get("MENSAGEM").toString();
    				} catch (JSONException e) {
    					e.printStackTrace();
    				}
                	if(msg == "DESLOGADO") {
                		System.exit(0);
                	}
                	else {
//                		System.out.println();
                        System.out.println(msg);
//                        System.out.print(" > ");
                	}
                }
            }
        } catch (IOException e) {
            System.out.println("Ocorreu uma Falha... .. ." + 
                " IOException: " + e);
        }
    }
    
    public static boolean conectar(PrintStream saida, String nomeUsuario)
    {
        try
        {
            JSONObject mensagem = new JSONObject();
            mensagem.put("OPERACAO", "CONECTAR");
            mensagem.put("MENSAGEM", nomeUsuario);
            saida.println(mensagem.toString());
            return true;
		}
		catch (Exception e)
		{
			return false;
		}
    }
}