package com.gilson.br.cadastro.service;

import com.gilson.br.cadastro.exception.EmailJaCadastradoException;
import com.gilson.br.cadastro.exception.UsuarioNaoEncontradoException;
import com.gilson.br.cadastro.model.UserRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final List<UserRequest> usersDatabase = new ArrayList<>();

    public UserRequest registerUser(UserRequest user) {
        // Nova Regra de Negócio: Bloqueio de e-mail duplicado
        boolean emailExiste = usersDatabase.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));

        if (emailExiste) {
            throw new EmailJaCadastradoException("O e-mail " + user.getEmail() + " já está em uso.");
        }

        // "Regra" de formatação de telefone (O nosso bug do cenário 3 continua aqui!)
        String telefoneFormatado = user.getTelefone().replace("-", "").replace(" ", "");
        user.setTelefone(telefoneFormatado);

        // Guarda na nossa lista em memória
        usersDatabase.add(user);

        return user;
    }

    public void deleteUserByEmail(String email) {
        // Tenta remover o usuário da lista baseando-se no e-mail
        boolean removido = usersDatabase.removeIf(u -> u.getEmail().equalsIgnoreCase(email));

        // Se não removeu ninguém (porque não achou), avisa que não existe
        if (!removido) {
            throw new UsuarioNaoEncontradoException("Usuário com e-mail " + email + " não foi encontrado para deleção.");
        }
    }

    public UserRequest updateUser(String email, UserRequest updatedData) {
        // 1. Busca o usuário na lista
        UserRequest existingUser = usersDatabase.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário com e-mail " + email + " não encontrado para atualização."));

        // 2. Atualiza as informações permitidas
        existingUser.setNome(updatedData.getNome());
        existingUser.setEndereco(updatedData.getEndereco());

        // Atualiza o telefone com a formatação (desta vez, protegendo contra nulos para mostrar maturidade)
        if (updatedData.getTelefone() != null) {
            existingUser.setTelefone(updatedData.getTelefone().replace("-", "").replace(" ", ""));
        } else {
            existingUser.setTelefone(null);
        }

        return existingUser;
    }

    public List<UserRequest> getAllUsers() {
        return usersDatabase;
    }
}

