package com.splitsnap.config;

import com.splitsnap.model.Group;
import com.splitsnap.model.GroupMember;
import com.splitsnap.model.GroupMemberId;
import com.splitsnap.model.User;
import com.splitsnap.repository.GroupMemberRepository;
import com.splitsnap.repository.GroupRepository;
import com.splitsnap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        abrirSwagger();

        if (userRepository.count() > 0) return;

        User carlos = userRepository.save(User.builder()
                .name("Carlos Huane")
                .email("carlos@splitsnap.com")
                .phone("987654321")
                .password(passwordEncoder.encode("test123"))
                .credits(BigDecimal.ZERO)
                .build());

        User ana = userRepository.save(User.builder()
                .name("Ana Torres")
                .email("ana@splitsnap.com")
                .phone("912345678")
                .password(passwordEncoder.encode("test123"))
                .credits(BigDecimal.ZERO)
                .build());

        User juan = userRepository.save(User.builder()
                .name("Juan Paredes")
                .email("juan@splitsnap.com")
                .phone("923456789")
                .password(passwordEncoder.encode("test123"))
                .credits(BigDecimal.ZERO)
                .build());

        Group cusco = groupRepository.save(Group.builder()
                .name("Viaje a Cusco")
                .emoji("🏔️")
                .createdBy(carlos)
                .build());

        Group depa = groupRepository.save(Group.builder()
                .name("Departamento")
                .emoji("🏠")
                .createdBy(ana)
                .build());

        List.of(carlos, ana, juan).forEach(u -> groupMemberRepository.save(
                GroupMember.builder()
                        .id(new GroupMemberId(cusco.getId(), u.getId()))
                        .group(cusco)
                        .user(u)
                        .build()
        ));

        List.of(ana, carlos).forEach(u -> groupMemberRepository.save(
                GroupMember.builder()
                        .id(new GroupMemberId(depa.getId(), u.getId()))
                        .group(depa)
                        .user(u)
                        .build()
        ));
    }

    private void abrirSwagger() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "http://localhost:8080/swagger-ui.html"});
            } catch (Exception ignored) {}
        }).start();
    }
}
