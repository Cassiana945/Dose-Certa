package br.pucgo.ads.projetointegrador.dosecerta.repository;

import br.pucgo.ads.projetointegrador.dosecerta.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndMedicamentoAnvisaIdAndContatarEmergenciaFalse(
            Long usuarioId, Long medicamentoAnvisaId
    );

    List<Medicamento> findByUsuarioIdAndMedicamentoAnvisa_NomeProdutoContainingIgnoreCase(
            Long usuarioId, String nomeProduto
    );

}
