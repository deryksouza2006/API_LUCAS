package com.proactiva.dao;

import com.proactiva.model.Task;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para a entidade Task.
 * Implementa operações CRUD utilizando JDBC puro.
 */
@ApplicationScoped
public class TaskDAO {

    @Inject
    DatabaseConnection databaseConnection;

    /**
     * Cria uma nova tarefa no banco de dados.
     *
     * @param task tarefa a ser criada
     * @return tarefa criada com ID gerado
     * @throws SQLException se houver erro na operação
     */
    public Task create(Task task) throws SQLException {
        // 1. Obter o próximo ID da sequence
        String seqSql = "SELECT TASKS_SEQ.NEXTVAL FROM DUAL";
        Long nextId = null;
        try (Connection conn = databaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(seqSql)){
            if (rs.next()) {
                nextId = rs.getLong(1);
                task.setId(nextId);
            } else {
                throw new SQLException("Falha ao obter o próximo valor da sequence TASKS_SEQ.");
            }
        }

        // 2. Inserir a tarefa com o ID obtido
        String insertSql = "INSERT INTO TASKS (TASK_ID, USER_ID, TITLE, DESCRIPTION, CATEGORY, PRIORITY, STATUS, DUE_DATE, CREATED_AT, UPDATED_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setLong(1, nextId); // TASK_ID
            stmt.setLong(2, task.getUserId()); // USER_ID
            stmt.setString(3, task.getTitle()); // TITLE
            stmt.setString(4, task.getDescription()); // DESCRIPTION
            stmt.setString(5, task.getCategory()); // CATEGORY
            stmt.setString(6, task.getPriority()); // PRIORITY
            stmt.setString(7, task.getStatus()); // STATUS

            if (task.getDueDate() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(task.getDueDate())); // DUE_DATE
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar tarefa, nenhuma linha afetada.");
            }

            // O ID já foi definido no início do método.
            // Não é necessário obter o ID gerado, pois ele foi inserido manualmente.
        }

        return task;
    }

    /**
     * Busca uma tarefa por ID.
     *
     * @param id ID da tarefa
     * @return Optional contendo a tarefa se encontrada
     * @throws SQLException se houver erro na operação
     */
    public Optional<Task> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM TASKS WHERE TASK_ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Lista todas as tarefas de um usuário.
     *
     * @param userId ID do usuário
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM TASKS WHERE USER_ID = ? ORDER BY CREATED_AT DESC";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }

        return tasks;
    }

    /**
     * Lista tarefas de um usuário filtradas por status.
     *
     * @param userId ID do usuário
     * @param status status da tarefa
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findByUserIdAndStatus(Long userId, String status) throws SQLException {
        String sql = "SELECT * FROM TASKS WHERE USER_ID = ? AND STATUS = ? ORDER BY CREATED_AT DESC";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }

        return tasks;
    }

    /**
     * Lista todas as tarefas.
     *
     * @return lista de tarefas
     * @throws SQLException se houver erro na operação
     */
    public List<Task> findAll() throws SQLException {
        String sql = "SELECT * FROM TASKS ORDER BY CREATED_AT DESC";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }

        return tasks;
    }

    /**
     * Atualiza uma tarefa existente.
     *
     * @param task tarefa com dados atualizados
     * @return tarefa atualizada
     * @throws SQLException se houver erro na operação
     */
    public Task update(Task task) throws SQLException {
        String sql = "UPDATE TASKS SET TITLE = ?, DESCRIPTION = ?, CATEGORY = ?, PRIORITY = ?, " +
                "STATUS = ?, DUE_DATE = ?, COMPLETED_AT = ?, UPDATED_AT = SYSTIMESTAMP " +
                "WHERE TASK_ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getCategory());
            stmt.setString(4, task.getPriority());
            stmt.setString(5, task.getStatus());

            if (task.getDueDate() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(task.getDueDate()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            if (task.getCompletedAt() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(task.getCompletedAt()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setLong(8, task.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao atualizar tarefa, nenhuma linha afetada.");
            }
        }

        return task;
    }

    /**
     * Deleta uma tarefa por ID.
     *
     * @param id ID da tarefa a ser deletada
     * @return true se deletada com sucesso
     * @throws SQLException se houver erro na operação
     */
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM TASKS WHERE TASK_ID = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Mapeia um ResultSet para um objeto Task.
     *
     * @param rs ResultSet contendo dados da tarefa
     * @return objeto Task
     * @throws SQLException se houver erro ao ler dados
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("TASK_ID"));
        task.setUserId(rs.getLong("USER_ID"));
        task.setTitle(rs.getString("TITLE"));
        task.setDescription(rs.getString("DESCRIPTION"));
        task.setCategory(rs.getString("CATEGORY"));
        task.setPriority(rs.getString("PRIORITY"));
        task.setStatus(rs.getString("STATUS"));

        Timestamp dueDate = rs.getTimestamp("DUE_DATE");
        if (dueDate != null) {
            task.setDueDate(dueDate.toLocalDateTime());
        }

        Timestamp completedAt = rs.getTimestamp("COMPLETED_AT");
        if (completedAt != null) {
            task.setCompletedAt(completedAt.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            task.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");
        if (updatedAt != null) {
            task.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return task;
    }
}