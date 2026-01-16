package med.voll.web_application.domain.usuario;

import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.usuario.email.EmailService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder encriptador;

    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder encriptador, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.encriptador = encriptador;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));
    }

    public Long salvarUsuario(String nome, String email, Perfil perfil) {
        String primeiraSenha = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("Senha gerada: " + primeiraSenha);
        String senhaCriptografada = encriptador.encode(primeiraSenha);
        var usuario = usuarioRepository.save(new Usuario(nome, email, senhaCriptografada, perfil, false));
        return usuario.getId();
    }

    public void excluir(Long id) {
        usuarioRepository.deleteById(id);
    }

    public void alterarSenha(DadosAlteracaoSenha dados, Usuario logado){
        if(!encriptador.matches(dados.senhaAtual(), logado.getPassword())){
            throw new RegraDeNegocioException("Senha digitada não confere com senha atual!");
        }

        if(!dados.novaSenha().equals(dados.novaSenhaConfirmacao())){
            throw new RegraDeNegocioException("Senha e confirmação não conferem!");
        }

        String senhaCriptografada = encriptador.encode(dados.novaSenha());
        logado.alterarSenha(senhaCriptografada);

        logado.setSenhaAlterada(true);

        usuarioRepository.save(logado);
    }

    //busca usuário com base no email e manda junto do token
    public void enviarToken(String email){

        //busca do USUARIO com o EMAIL
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new RegraDeNegocioException("Usuário não encontrado"));

        //cria o token
        String token = UUID.randomUUID().toString();
        usuario.setToken(token);
        usuario.setExpiracaoToken(LocalDateTime.now().plusMinutes(15));

        //salvar pois muda 2 colunas no db - TOKEN and EXPIRAÇÃO
        usuarioRepository.save(usuario);
        emailService.enviarEmailSenha(usuario);
    }

}