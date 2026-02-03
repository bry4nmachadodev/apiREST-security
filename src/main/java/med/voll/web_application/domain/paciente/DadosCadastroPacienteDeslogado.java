package med.voll.web_application.domain.paciente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroPacienteDeslogado(Long id,

                                             @NotBlank
                                             String nome,

                                             @NotBlank
                                             @Email
                                             String email,

                                             String senha,

                                             @NotBlank
                                             String telefone,

                                             @NotBlank
                                             @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}")
                                             String cpf) {
}
