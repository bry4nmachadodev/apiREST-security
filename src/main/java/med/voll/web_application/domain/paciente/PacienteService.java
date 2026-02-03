package med.voll.web_application.domain.paciente;

import jakarta.transaction.Transactional;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.usuario.Perfil;
import med.voll.web_application.domain.usuario.UsuarioService;
import med.voll.web_application.domain.usuario.email.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final UsuarioService usuarioService;
    private EmailService emailService;

    public PacienteService(PacienteRepository repository, UsuarioService usuarioService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
    }

    public Page<DadosListagemPaciente> listar(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosListagemPaciente::new);
    }

    @Transactional
    public void cadastrar(DadosCadastroPaciente dados) {
        if (repository.isJaCadastrado(dados.email(), dados.cpf(), dados.id())) {
            throw new RegraDeNegocioException("E-mail ou CPF já cadastrado para outro paciente!");
        }

        if (dados.id() == null) {
            var usuarioId = usuarioService.salvarUsuario(dados.nome(), dados.email(), Perfil.PACIENTE);
            repository.save(new Paciente(usuarioId, dados));
        } else {
            var paciente = repository.findById(dados.id()).orElseThrow();
            paciente.modificarDados(dados);
        }
    }

    //cadastrar para paciente deslogado
    @Transactional
    public void cadastrarDeslogado(DadosCadastroPacienteDeslogado dados) {
        if (repository.isJaCadastrado(dados.email(), dados.cpf(), dados.id())) {
            throw new RegraDeNegocioException("E-mail ou CPF já cadastrado para outro paciente!");
        }

        String token = UUID.randomUUID().toString();
        var usuarioId = usuarioService.salvarUsuarioDeslogado(dados.nome(), dados.email(), dados.senha(), Perfil.PACIENTE, token);
        repository.save(new Paciente(usuarioId, dados));

        //agora settar como ativo - salvou como ativo = false
        emailService.enviarEmailConfirmacao(dados.email(), token);
    }

    public DadosCadastroPaciente carregarPorId(Long id) {
        var paciente = repository.findById(id).orElseThrow();
        return new DadosCadastroPaciente(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getTelefone(), paciente.getCpf());
    }


    //apagar de USUARIOS e do BANCO DE DADOS
    @Transactional
    public void excluir(Long id) {
        repository.deleteById(id);
        usuarioService.excluir(id);
    }

}
