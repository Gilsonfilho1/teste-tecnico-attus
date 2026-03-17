package com.gilson.br.cadastro.controller;

import com.gilson.br.cadastro.model.UserRequest;
import com.gilson.br.cadastro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserRequest> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserRequest savedUser = userService.registerUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping
    public ResponseEntity<List<UserRequest>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        userService.deleteUserByEmail(email);
        // O Status 204 (No Content) é o padrão REST absoluto para uma deleção bem-sucedida
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{email}")
    public ResponseEntity<UserRequest> updateUser(@PathVariable String email, @RequestBody UserRequest user) {
        UserRequest updatedUser = userService.updateUser(email, user);
        // Retorna Status 200 (OK) junto com o JSON do usuário com os dados novos
        return ResponseEntity.ok(updatedUser);
    }
}