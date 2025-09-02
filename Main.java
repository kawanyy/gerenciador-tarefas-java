import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/gerenciador_tarefas";
        String user = "root"; // seu usuário MySQL
        String password = "pequenaflor1"; // sua senha MySQL

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexao realizada com sucesso!");

            Statement stmt = conn.createStatement();

            // Criar tabela Usuarios se não existir
            String createUsuarios = "CREATE TABLE IF NOT EXISTS Usuarios (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "nome VARCHAR(50) NOT NULL," +
                    "email VARCHAR(50) NOT NULL UNIQUE," +
                    "senha VARCHAR(255) NOT NULL)";
            stmt.execute(createUsuarios);

            // Criar tabela Tarefas se não existir
            String createTarefas = "CREATE TABLE IF NOT EXISTS Tarefas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "titulo VARCHAR(255) NOT NULL," +
                    "concluida BOOLEAN DEFAULT FALSE," +
                    "usuario_id INT NOT NULL," +
                    "FOREIGN KEY (usuario_id) REFERENCES Usuarios(id))";
            stmt.execute(createTarefas);

            Scanner scanner = new Scanner(System.in);
            int usuarioId = 0;
            boolean logado = false;

            // Menu inicial: Login / Cadastro / Sair
            while (!logado) {
                System.out.println("\n=== Bem-vindo ao Gerenciador de Tarefas ===");
                System.out.println("\nEscolha uma opcao:");
                System.out.println("1 - Login");
                System.out.println("2 - Cadastro");
                System.out.println("3 - Sair");
                System.out.print("> ");
                int escolha = scanner.nextInt();
                scanner.nextLine(); // limpar buffer

                if (escolha == 1) { // Login
                    System.out.print("Digite seu email: ");
                    String email = scanner.nextLine();

                    PreparedStatement checkUser = conn.prepareStatement(
                            "SELECT id, senha, nome FROM Usuarios WHERE email = ?");
                    checkUser.setString(1, email);
                    ResultSet rsUser = checkUser.executeQuery();

                    if (rsUser.next()) {
                        usuarioId = rsUser.getInt("id");
                        String senhaCadastrada = rsUser.getString("senha");
                        String nome = rsUser.getString("nome");

                        boolean senhaCorreta = false;
                        while (!senhaCorreta) {
                            System.out.print("Digite sua senha: ");
                            String senha = scanner.nextLine();
                            if (senha.equals(senhaCadastrada)) {
                                System.out.println("Bem-vindo de volta, " + nome + "!");
                                senhaCorreta = true;
                                logado = true;
                            } else {
                                System.out.println("Senha incorreta. Tente novamente.");
                            }
                        }
                    } else {
                        System.out.println("Email não cadastrado. Escolha a opção 2 para criar usuário.");
                    }

                } else if (escolha == 2) { // Cadastro
                    System.out.print("Digite seu nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("Digite seu email: ");
                    String email = scanner.nextLine();
                    System.out.print("Digite sua senha: ");
                    String senha = scanner.nextLine();

                    PreparedStatement insertUser = conn.prepareStatement(
                            "INSERT INTO Usuarios (nome, email, senha) VALUES (?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    insertUser.setString(1, nome);
                    insertUser.setString(2, email);
                    insertUser.setString(3, senha);
                    insertUser.executeUpdate();
                    ResultSet generatedKeys = insertUser.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        usuarioId = generatedKeys.getInt(1);
                    }
                    System.out.println("Usuário cadastrado com sucesso!");
                    logado = true;

                } else if (escolha == 3) { // Sair
                    System.out.println("Saindo do programa...");
                    conn.close();
                    scanner.close();
                    return; // encerra o programa

                } else {
                    System.out.println("Opção invalida!");
                }
            }

            // Menu principal de tarefas
            boolean sair = false;
            while (!sair) {
                System.out.println("\nEscolha uma opcao:");
                System.out.println("1 - Adicionar tarefa");
                System.out.println("2 - Listar tarefas");
                System.out.println("3 - Marcar tarefa como concluida");
                System.out.println("4 - Excluir tarefa");
                System.out.println("5 - Sair");

                int opcao = scanner.nextInt();
                scanner.nextLine(); // limpar buffer

                switch (opcao) {
                    case 1:
                        System.out.print("Digite o titulo da tarefa: ");
                        String titulo = scanner.nextLine();
                        PreparedStatement pstmt = conn.prepareStatement(
                                "INSERT INTO Tarefas (titulo, usuario_id) VALUES (?, ?)");
                        pstmt.setString(1, titulo);
                        pstmt.setInt(2, usuarioId);
                        pstmt.executeUpdate();
                        System.out.println("Tarefa adicionada!");
                        break;

                    case 2:
                        PreparedStatement listStmt = conn.prepareStatement(
                                "SELECT * FROM Tarefas WHERE usuario_id = ?");
                        listStmt.setInt(1, usuarioId);
                        ResultSet rs = listStmt.executeQuery();
                        System.out.println("\n--- Lista de Tarefas ---");
                        while (rs.next()) {
                            System.out.println("ID: " + rs.getInt("id") +
                                    ", Titulo: " + rs.getString("titulo") +
                                    ", Concluida: " + rs.getBoolean("concluida"));
                        }
                        break;

                    case 3:
                        System.out.print("Digite o ID da tarefa para marcar como concluida: ");
                        int idConcluida = scanner.nextInt();
                        PreparedStatement markStmt = conn.prepareStatement(
                                "UPDATE Tarefas SET concluida = TRUE WHERE id = ? AND usuario_id = ?");
                        markStmt.setInt(1, idConcluida);
                        markStmt.setInt(2, usuarioId);
                        int linhasAtualizadas = markStmt.executeUpdate();
                        if (linhasAtualizadas > 0) {
                            System.out.println("Tarefa marcada como concluida!");
                        } else {
                            System.out.println("ID inválido ou tarefa não pertence a você.");
                        }
                        break;

                    case 4:
                        System.out.print("Digite o ID da tarefa para excluir: ");
                        int idExcluir = scanner.nextInt();
                        PreparedStatement deleteStmt = conn.prepareStatement(
                                "DELETE FROM Tarefas WHERE id = ? AND usuario_id = ?");
                        deleteStmt.setInt(1, idExcluir);
                        deleteStmt.setInt(2, usuarioId);
                        int linhasDeletadas = deleteStmt.executeUpdate();
                        if (linhasDeletadas > 0) {
                            System.out.println("Tarefa excluida!");
                        } else {
                            System.out.println("ID inválido ou tarefa não pertence a você.");
                        }
                        break;

                    case 5:
                        sair = true;
                        break;

                    default:
                        System.out.println("Opcao invalida!");
                }
            }

            conn.close();
            scanner.close();
            System.out.println("Programa finalizado.");

        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL não encontrado. Verifique o .jar.");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
