package com.bookhair.backend.repository;

import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.bookhair.backend.model.UserRole;
import com.bookhair.backend.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository repo;

    @Test
    void shouldFindEmployees() {
        var emp = new User();
        emp.setUserId(UUID.randomUUID().toString());
        emp.setName("Ana");
        emp.setEmail("ana@ex.com");
        emp.setPhone("96666666");
        emp.setPassword("password");
        emp.setStatusUser(StatusUser.ATIVO);
        emp.setUserRole(UserRole.FUNCIONARIO);
        repo.save(emp);

        var cli = new User();
        cli.setUserId(UUID.randomUUID().toString());
        cli.setName("João");
        cli.setEmail("joao@ex.com");
        cli.setPhone("97777777");
        cli.setPassword("password");
        cli.setStatusUser(StatusUser.ATIVO);
        cli.setUserRole(UserRole.CLIENTE);
        repo.save(cli);

        List<?> result = repo.findUsersWithPhoto(UserRole.FUNCIONARIO);
        assertThat(result).hasSize(1);
    }
}
