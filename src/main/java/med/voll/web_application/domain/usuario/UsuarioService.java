package med.voll.web_application.domain.usuario;

import med.voll.web_application.domain.RegraDeNegocioException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder encriptador;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder encriptador) {
        this.usuarioRepository = usuarioRepository;
        this.encriptador = encriptador;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));
    }

    public Long salvarUsuario(String nome, String email, String senha, Perfil perfil){
        String senhaCriptografada = encriptador.encode(senha);
        Usuario usuario = usuarioRepository.save(new Usuario(nome, email, senhaCriptografada, perfil));
        return usuario.getId();
    }

    public void excluir(Long id) {
        usuarioRepository.deleteById(id);
    }

    public void alterarSenha(DadosAltercaoSenha dadosAltercaoSenha, Usuario logado){
        if(!encriptador.matches(dadosAltercaoSenha.senhaAtual(), logado.getPassword())){
            throw new RegraDeNegocioException("Senha digitada não confere com a senha atual!");
        }

        if (!dadosAltercaoSenha.novaSenha().equals(dadosAltercaoSenha.novaSenhaConfirmacao())){
            throw new RegraDeNegocioException("Senha digitada não confere com a senha da confirmação!");
        }

        String senhaCriptografada = encriptador.encode(dadosAltercaoSenha.novaSenha());
        logado.alterarSenha(senhaCriptografada);

        usuarioRepository.save(logado);
    }
}