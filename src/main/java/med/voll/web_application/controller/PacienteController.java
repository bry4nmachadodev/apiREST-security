package med.voll.web_application.controller;

import jakarta.validation.Valid;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.paciente.DadosCadastroPaciente;
import med.voll.web_application.domain.paciente.DadosCadastroPacienteDeslogado;
import med.voll.web_application.domain.paciente.PacienteService;
import med.voll.web_application.domain.usuario.Perfil;
import med.voll.web_application.domain.usuario.Usuario;
import med.voll.web_application.domain.usuario.UsuarioService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("pacientes")
public class PacienteController {

    private static final String PAGINA_LISTAGEM = "paciente/listagem-pacientes";

    //erro -> 500 (erro interntet)
    private static final String PAGINA_ERRO = "erro/500";

    //erro -> 403 (não autorizado)
    private static final String PAGINA_ERRO_403 = "erro/403";

    private static final String PAGINA_CADASTRO = "paciente/formulario-paciente";
    private static final String REDIRECT_LISTAGEM = "redirect:/pacientes?sucesso";
    private static final String PAGINA_CADASTRO_DESLOGADO = "paciente/registrar-paciente";

    private final PacienteService pacienteService;
    private final UsuarioService usuarioService;

    public PacienteController(PacienteService service, UsuarioService usuarioService) {
        this.pacienteService = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ATENDENTE')")
    public String carregarPaginaListagem(@PageableDefault Pageable paginacao, Model model) {
        var pacientesCadastrados = pacienteService.listar(paginacao);
        model.addAttribute("pacientes", pacientesCadastrados);
        return PAGINA_LISTAGEM;
    }

    @GetMapping("formulario")
    @PreAuthorize("hasRole('ATENDENTE')")
    public String carregarPaginaCadastro(Long id, Model model) {
        if (id != null) {
            model.addAttribute("dados", pacienteService.carregarPorId(id));
        } else {
            model.addAttribute("dados", new DadosCadastroPaciente(null, "", "", "", ""));
        }

        return PAGINA_CADASTRO;
    }

    @PostMapping
    @PreAuthorize("hasRole('ATENDENTE')")
    public String cadastrar(@Valid @ModelAttribute("dados") DadosCadastroPaciente dados, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO;
        }

        try {
            pacienteService.cadastrar(dados);
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO;
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ATENDENTE')")
    public String excluir(Long id, @AuthenticationPrincipal Usuario logado) {
        if (logado.getPerfil() == Perfil.PACIENTE &&
                !logado.getId().equals(id)) {
            throw new RegraDeNegocioException("Você não pode excluir outro paciente");
        }
        pacienteService.excluir(id);
        return REDIRECT_LISTAGEM;
    }


    //pontos responsáveis pelo registro (sem login)
    @GetMapping("/registrar")
    public String registrar(Model model) {
        model.addAttribute("dados", new DadosCadastroPacienteDeslogado(null, "", "", "", "",  ""));
        return PAGINA_CADASTRO_DESLOGADO;
    }

    @PostMapping("/registrar")
    public String cadastrar(@Valid @ModelAttribute("dados") DadosCadastroPacienteDeslogado dados, BindingResult result , Model model){
        if (result.hasErrors()) {
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO_DESLOGADO;
        }

        try {
            pacienteService.cadastrarDeslogado(dados);
            return "redirect:/login?registrado=true";

        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO_DESLOGADO;
        }
    }


    //responsáveis pela ativação da conta
    @GetMapping("/confirmar-email")
    public String confirmarEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.confirmarConta(token);
            redirectAttributes.addFlashAttribute("mensagem", "Conta ativada com sucesso!");
            return "redirect:/login?confirmado=true";
        } catch (RegraDeNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/login?erro=true";
        }
    }

}