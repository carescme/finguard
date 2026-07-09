package com.finguard.core.repositories;

import com.finguard.core.entities.Account;
import com.finguard.core.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGuardarYBuscarCuentasPorUsuario() {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        User usuario = User.builder()
                .email("propietario@test.com")
                .password("securePass123")
                .build();
        User usuarioGuardado = userRepository.save(usuario);

        Account cuentaAhorros = new Account();
        cuentaAhorros.setName("Hucha de Ahorros");
        cuentaAhorros.setBalance(new BigDecimal("500.50"));
        cuentaAhorros.setUser(usuarioGuardado);

        accountRepository.save(cuentaAhorros);

        List<Account> cuentasDelUsuario = accountRepository.findByUserId(usuarioGuardado.getId());

        assertThat(cuentasDelUsuario).hasSize(1);
        assertThat(cuentasDelUsuario.get(0).getName()).isEqualTo("Hucha de Ahorros");
        assertThat(cuentasDelUsuario.get(0).getBalance()).isEqualByComparingTo("500.50");
        assertThat(cuentasDelUsuario.get(0).getUser().getId()).isEqualTo(usuarioGuardado.getId());
    }
}