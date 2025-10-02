package br.com.ecocoleta.client;

import br.com.ecocoleta.model.PontoDeColeta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class EcoColetaClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor EcoColeta!");

            while (true) {
                exibirMenu();
                System.out.print("Escolha uma opção: ");
                int escolha = scanner.nextInt();
                scanner.nextLine();

                switch (escolha) {
                    case 1: listarTodos(out, in); break;
                    case 2: buscarPorTipo(out, in, scanner); break;
                    case 3: cadastrarNovoPonto(out, in, scanner); break;
                    case 4: editarPonto(out, in, scanner); break;
                    case 5: excluirPonto(out, in, scanner); break;
                    case 0: out.writeObject("SAIR"); System.out.println("Desconectando..."); return;
                    default: System.out.println("Opção inválida. Tente novamente.");
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Servidor não encontrado no endereço: " + SERVER_ADDRESS);
        } catch (IOException e) {
            System.err.println("Não foi possível conectar ao servidor. Ele está rodando? Erro: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao receber dados do servidor: " + e.getMessage());
        }
    }

    private static void exibirMenu() {
        System.out.println("\n--- MENU ECOCOLETA ---");
        System.out.println("1. Listar todos os pontos de coleta");
        System.out.println("2. Buscar ponto por tipo de resíduo");
        System.out.println("3. Cadastrar novo ponto de coleta");
        System.out.println("4. Editar um ponto de coleta");
        System.out.println("5. Excluir um ponto de coleta");
        System.out.println("0. Sair");
        System.out.println("----------------------");
    }

    private static void listarTodos(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        System.out.println("\nBuscando todos os pontos...");
        out.writeObject("LISTAR_TODOS");
        List<PontoDeColeta> pontos = (List<PontoDeColeta>) in.readObject();
        if (pontos.isEmpty()) {
            System.out.println("Nenhum ponto de coleta cadastrado.");
        } else {
            System.out.println("--- PONTOS DE COLETA ENCONTRADOS ---");
            pontos.forEach(System.out::println);
        }
    }

    private static void buscarPorTipo(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.print("Digite o tipo de resíduo a ser buscado (ex: Vidro, Pilhas): ");
        String tipo = scanner.nextLine();
        out.writeObject("BUSCAR_POR_TIPO");
        out.writeObject(tipo);
        List<PontoDeColeta> pontos = (List<PontoDeColeta>) in.readObject();
        if (pontos.isEmpty()) {
            System.out.println("Nenhum ponto de coleta encontrado para o tipo '" + tipo + "'.");
        } else {
            System.out.println("--- PONTOS ENCONTRADOS QUE ACEITAM '" + tipo + "' ---");
            pontos.forEach(System.out::println);
        }
    }

    private static void cadastrarNovoPonto(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("\n--- Cadastro de Novo Ponto ---");
        System.out.print("Nome do ponto: ");
        String nome = scanner.nextLine();
        System.out.print("Endereço: ");
        String endereco = scanner.nextLine();
        System.out.print("Tipos de resíduos aceitos (separados por vírgula): ");
        String tiposInput = scanner.nextLine();
        List<String> tipos = Arrays.asList(tiposInput.split(",\\s*"));
        PontoDeColeta novoPonto = new PontoDeColeta(nome, endereco, tipos);
        out.writeObject("CADASTRAR");
        out.writeObject(novoPonto);
        String resposta = (String) in.readObject();
        System.out.println("Servidor: " + resposta);
    }


    private static void editarPonto(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("\n--- Edição de Ponto de Coleta ---");
        System.out.print("Digite o ID do ponto que deseja editar: ");
        int idParaEditar = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Agora, insira os NOVOS dados para este ponto:");
        System.out.print("Novo nome: ");
        String novoNome = scanner.nextLine();
        System.out.print("Novo endereço: ");
        String novoEndereco = scanner.nextLine();
        System.out.print("Novos tipos de resíduos (separados por vírgula): ");
        String novosTiposInput = scanner.nextLine();
        List<String> novosTipos = Arrays.asList(novosTiposInput.split(",\\s*"));

        PontoDeColeta pontoAtualizado = new PontoDeColeta(novoNome, novoEndereco, novosTipos);

        out.writeObject("EDITAR");
        out.writeObject(idParaEditar);
        out.writeObject(pontoAtualizado);

        String resposta = (String) in.readObject();
        System.out.println("Servidor: " + resposta);
    }

    private static void excluirPonto(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("\n--- Exclusão de Ponto de Coleta ---");
        System.out.print("Digite o ID do ponto que deseja excluir: ");
        int idParaExcluir = scanner.nextInt();
        scanner.nextLine();

        out.writeObject("EXCLUIR");
        out.writeObject(idParaExcluir);

        String resposta = (String) in.readObject();
        System.out.println("Servidor: " + resposta);
    }
}