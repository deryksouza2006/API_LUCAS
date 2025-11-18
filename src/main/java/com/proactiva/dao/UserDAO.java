package com.proactiva.dao;

import com.proactiva.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para a entidade User.
 * Implementa operações CRUD utilizando JDBC puro.
 */
@ApplicationScoped
public class UserDAO {

    @Inject
    DatabaseConnection databaseConnection;

    /**
     * Cria um novo usuário no banco de dados.
     *
     * @param user usuário a ser criado
     * @return usuário criado com ID gerado
     * @throws SQLException se houver erro na operação
     */
    public User create(User user) throws SQLException {
        // CORREÇÃO: Incluindo first_name e last_name na instrução SQL
        String sql = "INSERT INTO USERS (USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, CREATED_AT, UPDATED_AT) " +
                "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

        try (Connection conn = databaseConnection.getConnection();
             // CORREÇÃO: Removendo new String[]{"user_id"} para evitar o erro ORA-02289
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            // CORREÇÃO: Definindo os valores para first_name e last_name
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar usuário, nenhuma linha afetada.");
            }

            // CORREÇÃO: Busca o usuário recém-criado para obter o ID gerado pelo Trigger/Sequence
            Optional<User> createdUser = findByUsername(user.getUsername());
            if (createdUser.isPresent()) {
                user.setId(createdUser.get().getId());
            } else {
                throw new SQLException("Falha ao recuperar usuário recém-criado.");
            }
        }

        return user;
    }

    /**
     * Busca um usuário por ID.
     *
     * @param id ID do usuário
     * @return Optional contendo o usuário se encontrado
     * @throws SQLException se houver erro na operação
     */
    public Optional<User> findById(Long id) throws SQLException {
        // CORREÇÃO: Usando "user_id" na cláusula WHERE
        String sql = "SELECT * FROM USERS WHERE USER_ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Busca um usuário por username.
     *
     * @param username username do usuário
     * @return Optional contendo o usuário se encontrado
     * @throws SQLException se houver erro na operação
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE USERNAME = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Busca um usuário por email.
     *
     * @param email email do usuário
     * @return Optional contendo o usuário se encontrado
     * @throws SQLException se houver erro na operação
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE EMAIL = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Lista todos os usuários.
     *
     * @return lista de usuários
     * @throws SQLException se houver erro na operação
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM USERS ORDER BY CREATED_AT DESC";
        List<User> users = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    /**
     * Atualiza um usuário existente.
     *
     * @param user usuário com dados atualizados
     * @return usuário atualizado
     * @throws SQLException se houver erro na operação
     */
    public User update(User user) throws SQLException {
        // CORREÇÃO: Usando "user_id" na cláusula WHERE
        String sql = "UPDATE USERS SET USERNAME = ?, EMAIL = ?, PASSWORD = ?, UPDATED_AT = SYSTIMESTAMP " +
                "WHERE USER_ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setLong(4, user.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao atualizar usuário, nenhuma linha afetada.");
            }
        }

        return user;
    }

    /**
     * Deleta um usuário por ID.
     *
     * @param id ID do usuário a ser deletado
     * @return true se deletado com sucesso
     * @throws SQLException se houver erro na operação
     */
    public boolean delete(Long id) throws SQLException {
        // CORREÇÃO: Usando "user_id" na cláusula WHERE
        String sql = "DELETE FROM USERS WHERE USER_ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Mapeia um ResultSet para um objeto User.
     *
     * @param rs ResultSet contendo dados do usuário
     * @return objeto User
     * @throws SQLException se houver erro ao ler dados
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        // CORREÇÃO: Usando "user_id" para obter o ID
        user.setId(rs.getLong("USER_ID"));
        user.setUsername(rs.getString("USERNAME"));
        user.setEmail(rs.getString("EMAIL"));
        user.setPassword(rs.getString("PASSWORD"));

        // Mapeando first_name e last_name
        user.setFirstName(rs.getString("FIRST_NAME"));
        user.setLastName(rs.getString("LAST_NAME"));

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}