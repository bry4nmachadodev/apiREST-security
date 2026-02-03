package med.voll.web_application.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracoesSeguranca {

    @Bean
    public SecurityFilterChain filtrosSeguranca(
            HttpSecurity http,
            OncePerRequestFilter filtroAlteracaoSenha
    ) throws Exception {

        return http
                .authorizeHttpRequests(req -> req
                        // arquivos estáticos
                        .requestMatchers(
                                "/css/**", "/js/**", "/assets/**"
                        ).permitAll()

                        // páginas públicas
                        .requestMatchers(
                                "/", "/index", "/home",
                                "/login",
                                "/esqueci-minha-senha",
                                "/recuperar-conta/**"
                        ).permitAll()

                        // 🔥 REGISTRO DE PACIENTE (GET + POST)
                        .requestMatchers(HttpMethod.GET, "/pacientes/registrar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/pacientes/registrar").permitAll()

                        // confirmação por token
                        .requestMatchers("/pacientes/confirmar-email/**").permitAll()

                        // resto protegido
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        filtroAlteracaoSenha,
                        UsernamePasswordAuthenticationFilter.class
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                .rememberMe(rememberMe -> rememberMe
                        .key("lembrarDeMim")
                        .alwaysRemember(true)
                )

                // 🔥 CSRF ignorado apenas onde precisa
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/pacientes/registrar",
                                "/pacientes/confirmar-email/**"
                        )
                )

                .build();
    }

    @Bean
    public PasswordEncoder codificadorSenha(){
        return new BCryptPasswordEncoder();
    }
}
