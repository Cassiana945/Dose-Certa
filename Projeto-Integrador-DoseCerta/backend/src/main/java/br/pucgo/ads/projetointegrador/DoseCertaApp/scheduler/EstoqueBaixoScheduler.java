package br.pucgo.ads.projetointegrador.DoseCertaApp.scheduler;

import br.pucgo.ads.projetointegrador.DoseCertaApp.model.Medicamento;
import br.pucgo.ads.projetointegrador.DoseCertaApp.repository.MedicamentoRepository;
import br.pucgo.ads.projetointegrador.DoseCertaApp.sms.SmsService;
import br.pucgo.ads.projetointegrador.plataforma.entity.User;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EstoqueBaixoScheduler {

    private final MedicamentoRepository medicamentoRepository;
    private final SmsService smsService;

    public EstoqueBaixoScheduler(
            MedicamentoRepository medicamentoRepository,
            SmsService smsService
    ) {
        this.medicamentoRepository = medicamentoRepository;
        this.smsService = smsService;
    }

    // Executa a cada 10 segundos (para testes)
    @Scheduled(fixedRate = 10000)
    public void verificarEstoqueMedicamentos() {

        System.out.println("==================================");
        System.out.println("Scheduler executando...");
        System.out.println("==================================");

        List<Medicamento> medicamentos =
                medicamentoRepository.findByEstoqueNotificadoFalse();

        System.out.println("Medicamentos encontrados: " + medicamentos.size());

        for (Medicamento medicamento : medicamentos) {

            String nomeMedicamento =
                    medicamento.getMedicamentoAnvisa().getNomeProduto();

            System.out.println("----------------------------------");
            System.out.println("Medicamento: " + nomeMedicamento);

            int diasRestantes = medicamento.calcularDias();

            System.out.println("Dias restantes: " + diasRestantes);

            // Notifica quando faltar 5 dias ou menos
            if (diasRestantes <= 5) {

                User usuario = medicamento.getUsuario();

                if (usuario == null) {

                    System.out.println("Usuário é nulo!");
                    continue;
                }

                if (usuario.getPhone() == null) {

                    System.out.println("Telefone do usuário é nulo!");
                    continue;
                }

                System.out.println("Telefone encontrado: " + usuario.getPhone());

                String mensagem = String.format(
                        "Dose Certa: O medicamento %s possui estoque para aproximadamente 5 dias. Recomendamos reabastecer.",
                        nomeMedicamento
                );

                System.out.println("Tentando enviar SMS...");

                try {

                    String sid = smsService.sendSms(
                            usuario.getPhone(),
                            mensagem
                    );

                    System.out.println("SMS enviado com sucesso!");
                    System.out.println("SID da mensagem: " + sid);

                    medicamento.setEstoqueNotificado(true);

                    medicamentoRepository.save(medicamento);

                    System.out.println("Medicamento marcado como notificado.");

                } catch (Exception e) {

                    System.out.println("Erro ao enviar SMS:");
                    e.printStackTrace();
                }

            } else {

                System.out.println(
                        "Ainda não faltam 5 dias. Dias restantes: "
                                + diasRestantes
                );
            }
        }

        System.out.println("Fim da execução do scheduler.");
    }
}