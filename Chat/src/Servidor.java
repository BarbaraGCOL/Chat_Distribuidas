import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Servidor extends Thread {

    private static Map<String, PrintStream> CLIENTES;
    private Socket conexao;
    private String nomeCliente;
    private static final List<String> CONECTADOS = new ArrayList<>();
    private JSONObject json = null;
    JSONObject mensagem = null;
    
    public Servidor(Socket socket) {
        this.conexao = socket;
    }
    
    /**
     * Faz login do cliente
     * @param newName 
     * @return true se já existe usuário logado com o respectivo nome
     */
    public boolean verificaLogin(String newName) {
        // Verifica se já existe usuário logado com esse nome
        if (CONECTADOS.stream().anyMatch((nome) -> (nome.equals(newName)))) {
            return true;
        }
        CONECTADOS.add(newName);
        return false;
    }
    
    /**
     * Faz loggof do cliente
     * @param oldName 
     */
    public void logoff(String oldName, PrintStream saida) {
        for (int i = 0; i < CONECTADOS.size(); i++) {
            if (CONECTADOS.get(i).equals(oldName))
                CONECTADOS.remove(oldName);
        }
        CLIENTES.remove(this.nomeCliente);
        System.out.println(this.nomeCliente + " saiu do bate-papo!");
        //String[] out = {" do bate-papo!"};
        mensagem = new JSONObject();
        formaJSON("MENSAGEM", "DESLOGADO");
        send(saida, mensagem.toString());
        try {
            getConexao().close();
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Lista usuários logados no sistema
     * @param saida 
     */
    public void listaUsuarios(PrintStream saida){
        	mensagem = new JSONObject();
        	formaJSON("OPERACAO", "LISTAR");
        	formaJSON("MENSAGEM", CONECTADOS.size() + " usu�rios conectados: " + CONECTADOS.toString());
            saida.println(mensagem.toString());
    }
    
    public static void rodarServer(int porta){
        CLIENTES = new HashMap<String, PrintStream>();
        try {
            ServerSocket server = new ServerSocket(porta);
            System.out.println("ServidorSocket rodando na porta " + porta);
            while (true) {
                Socket conexao = server.accept();
                Thread t = new Servidor(conexao);
                t.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }
    
    public static void main(String args[]) {
       int porta = 7000;
       rodarServer(porta);
    }
    
    @Override
    public void run() {
        try {
            BufferedReader entrada = 
                new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            PrintStream saida = new PrintStream(this.conexao.getOutputStream());
            
            String operacao = "";
          
            do{
            	
            	try {
                    json = new JSONObject(entrada.readLine());
                } catch (JSONException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            	
            	operacao = (json.get("OPERACAO")).toString();
            	
                String msg;

                switch(operacao){
                    case "CONECTAR":
                        setNomeCliente((json.get("MENSAGEM")).toString());
                        conectar(saida, getNomeCliente());
                        break;
                    case "ENVIAR":
                        msg = (json.get("MENSAGEM")).toString();
                        send(saida, msg);
                        break;    
                    case "LISTAR":
                    	listaUsuarios(saida);
                        break;
                    case "SAIR":
                        logoff(getNomeCliente(), saida);
                        break;
                    default:
                        break;
                }
            }while(!operacao.equals("DESLOGAR"));
        
        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
        } catch (JSONException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param saida
     * @param acao
     * @param msg 
     */
    public void send(PrintStream saida, String msg) {
        for (Map.Entry<String, PrintStream> cliente : CLIENTES.entrySet()) {
            
            PrintStream chat = cliente.getValue();
            
            if (chat != saida) {
            	mensagem = new JSONObject();
            	formaJSON("MENSAGEM", this.nomeCliente +" disse: "+ msg);
            	chat.println(mensagem.toString());
            }
        }
    }
    
    public boolean conectar(PrintStream saida, String nomeUsuario)
    {
        if (verificaLogin(nomeUsuario)) {
            saida.println("Este nome ja existe! Conecte novamente com outro Nome.");
            try {
                getConexao().close();
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } else {
            //mostra o nome do cliente conectado ao servidor
            System.out.println(this.nomeCliente + " : Conectado ao Servidor!");
        }
        if (nomeUsuario == null || nomeUsuario.equals("")) {
            return false;
        }
        
        CLIENTES.put(this.nomeCliente, saida);
            
        return true;
    }
    
    public void formaJSON(String chave, String valor) {
    	try
        {
            mensagem.put(chave, valor);
        }
		catch (Exception e)
		{
		}
    }
    
    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }
    
    public Socket getConexao() {
        return conexao;
    }

    public void setConexao(Socket conexao) {
        this.conexao = conexao;
    }
}