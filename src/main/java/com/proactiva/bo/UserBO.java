package com.proactiva.bo;

import com.proactiva.dao.UserDAO;
import com.proactiva.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Business Object para a entidade User.
 * Contém a lógica de negócio e validações.
 */
@ApplicationScoped
public class UserBO {

    @Inject
    UserDAO userDAO;

    /**
     * Cria um novo usuário com senha criptografada.
     *
     * @param user usuário a ser criado
     * @return usuário criado
     * @throws SQLException se houver erro na operação
     * @throws IllegalArgumentException se dados forem inválidos
     */
    public User create(@Valid User user) throws SQLException {
        // Validar se username já existe
        Optional<User> existingUser = userDAO.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username já está em uso");
        }

        // Validar se email já existe
        Optional<User> existingEmail = userDAO.findByEmail(user.getEmail());
        if (existingEmail.isPresent()) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        // Criptografar senha
        String hashedPassword = hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        return userDAO.create(user);
    }

    /**
     * Busca um usuário por ID.
     *
     * @param id ID do usuário
     * @return Optional contendo o usuário se encontrado
     * @throws SQLException se houver erro na operação
     */
    public Optional<User> findById(Long id) throws SQLException {
        return userDAO.findById(id);
    }

    /**
     * Busca um usuário por username.
     *
     * @param username username do usuário
     * @return Optional contendo o usuário se encontrado
     * @throws SQLException se houver erro na operação
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }

    /**
     * Lista todos os usuários.
     *
     * @return lista de usuários
     * @throws SQLException se houver erro na operação
     */
    public List<User> findAll() throws SQLException {
        return userDAO.findAll();
    }

    /**
     * Atualiza um usuário existente.
     *
     * @param id ID do usuário
     * @param updatedUser dados atualizados
     * @return usuário atualizado
     * @throws SQLException se houver erro na operação
     * @throws IllegalArgumentException se usuário não for encontrado
     */
    public User update(Long id, @Valid User updatedUser) throws SQLException {
        Optional<User> existingUser = userDAO.findById(id);
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        User user = existingUser.get();

        // Validar se novo username já está em uso por outro usuário
        if (!user.getUsername().equals(updatedUser.getUsername())) {
            Optional<User> userWithSameUsername = userDAO.findByUsername(updatedUser.getUsername());
            if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
                throw new IllegalArgumentException("Username já está em uso");
            }
        }

        // Validar se novo email já está em uso por outro usuário
        if (!user.getEmail().equals(updatedUser.getEmail())) {
            Optional<User> userWithSameEmail = userDAO.findByEmail(updatedUser.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("Email já está em uso");
            }
        }

        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());

        // Se a senha foi alterada, criptografar
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            String hashedPassword = hashPassword(updatedUser.getPassword());
            user.setPassword(hashedPassword);
        }

        return userDAO.update(user);
    }

    /**
     * Deleta um usuário por ID.
     *
     * @param id ID do usuário
     * @return true se deletado com sucesso
     * @throws SQLException se houver erro na operação
     * @throws IllegalArgumentException se usuário não for encontrado
     */
    public boolean delete(Long id) throws SQLException {
        Optional<User> user = userDAO.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        return userDAO.delete(id);
    }

    /**
     * Autentica um usuário.
     *
     * @param username username
     * @param password senha
     * @return Optional contendo o usuário se autenticado
     * @throws SQLException se houver erro na operação
     */
    public Optional<User> authenticate(String username, String password) throws SQLException {
        Optional<User> user = userDAO.findByUsername(username);
        
        if (user.isPresent()) {
            String hashedPassword = hashPassword(password);
            if (user.get().getPassword().equals(hashedPassword)) {
                return user;
            }
        }
        
        return Optional.empty();
    }

    /**
     * Criptografa uma senha usando SHA-256.
     *
     * @param password senha em texto plano
     * @return senha criptografada em Base64
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar senha", e);
        }
    }
}
