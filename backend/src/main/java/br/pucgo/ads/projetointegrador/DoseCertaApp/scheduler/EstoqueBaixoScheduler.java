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

    // Executa todos os dias às 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void verificarEstoqueMedicamentos() {

        List<Medicamento> medicamentos =
                medicamentoRepository.findByEstoqueNotificadoFalse();

        for (Medicamento medicamento : medicamentos) {

            int diasRestantes = medicamento.calcularDias();

            // Notifica quando faltar exatamente 5 dias
            if (diasRestantes == 5) {

                User usuario = medicamento.getUsuario();

                if (usuario == null || usuario.getPhone() == null) {
                    continue;
                }

                String nomeMedicamento =
                        medicamento.getMedicamentoAnvisa().getNomeProduto();

                String mensagem = String.format(
                        "Dose Certa: O medicamento %s possui estoque para aproximadamente 5 dias. Recomendamos reabastecer.",
                        nomeMedicamento
                );

                try {

                    smsService.sendSms(
                            usuario.getPhone(),
                            mensagem
                    );

                    medicamento.setEstoqueNotificado(true);

                    medicamentoRepository.save(medicamento);

                    System.out.println(
                            "SMS enviado para: " + usuario.getPhone()
                    );

                } catch (Exception e) {

                    System.out.println(
                            "Erro ao enviar SMS: " + e.getMessage()
                    );
                }
            }
        }
    }
}
