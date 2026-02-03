package med.voll.web_application.domain.usuario.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.usuario.Usuario;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    //instâncias
    private final JavaMailSender enviadorEmail;
    private static final String EMAIL_ORIGEM = "vollmed@email.com";
    private static final String NOME_ENVIADOR = "Clínica Voll Med";
    public static final String URL_SITE = "http://localhost:8080"; //"voll.med.com.br"
    public static final String URL_SITE_LOGIN = "http://localhost:8080/login";

    public EmailService(JavaMailSender enviadorEmail) {
        this.enviadorEmail = enviadorEmail;
    }

    private void enviarEmail(String emailUsuario, String assunto, String conteudo) {
        MimeMessage message = enviadorEmail.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(EMAIL_ORIGEM, NOME_ENVIADOR);
            helper.setTo(emailUsuario);
            helper.setSubject(assunto);
            helper.setText(conteudo, true);
        } catch(MessagingException | UnsupportedEncodingException e){
            throw new RegraDeNegocioException("Erro ao enviar email");
        }

        enviadorEmail.send(message);
    }

    private String gerarConteudoEmail(String template, String nome, String url) {
        return template.replace("[[name]]", nome).replace("[[URL]]", url);
    }

    public void enviarEmailSenha(Usuario usuario) {
        String assunto = "Aqui está seu link para alterar a senha";
        String conteudo = gerarConteudoEmail("Olá [[name]],<br>"
                + "Por favor clique no link abaixo para alterar a senha:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">ALTERAR</a></h3>"
                + "Obrigado,<br>"
                + "Clínica Voll Med.", usuario.getNome(), URL_SITE + "/recuperar-conta?codigo=" + usuario.getToken());

        enviarEmail(usuario.getUsername(), assunto, conteudo);
    }

    public void enviarCredenciaisEmail(String senha, Usuario usuario) {
        String assunto = "Aqui estão suas credenciais!";
        String conteudo = gerarConteudoEmail(

                //apresentação
                "Olá [[name]],<br>"
                + "Aqui estão suas informações de login<br>"
                + "Email: " + usuario.getUsername() + "<br>"
                + "Senha: " + senha + "<br>"

                //url
                + "Por favor clique no link abaixo para alterar a senha:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">ACESSAR SUA CONTA</a></h3>"

                //despedir
                + "Conte com a nossa equipe para o que precisar!<br>"
                + "Obrigado,<br>"
                + "Clínica Voll Med.", usuario.getNome(), URL_SITE_LOGIN);

        enviarEmail(usuario.getUsername(), assunto, conteudo);
    }

    public void enviarEmailConfirmacao(@NotBlank @Email String email, String token) {
        String assunto = "Confirme sua conta - Voll Med";

        String urlConfirmacao = URL_SITE + "/confirmar-email?token=" + token;

        String conteudo =
                "Olá,<br><br>"
                        + "Obrigado por se cadastrar na Clínica Voll Med!<br><br>"
                        + "Para ativar sua conta, clique no link abaixo:<br>"
                        + "<h3><a href=\"" + urlConfirmacao + "\" target=\"_self\">CONFIRMAR MINHA CONTA</a></h3>"
                        + "<br>"
                        + "Se você não se cadastrou, ignore este email.<br><br>"
                        + "Obrigado,<br>"
                        + "Equipe Voll Med.";

        enviarEmail(email, assunto, conteudo);
    }
}
