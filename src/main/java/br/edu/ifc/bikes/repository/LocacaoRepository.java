package br.edu.ifc.bikes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifc.bikes.entity.Locacao;

import java.util.Optional;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {
    //só encontra a locação se ela for do cliente do usuário informado
    Optional<Locacao> findByIdAndClienteUsuarioId(Long id, Long idUsuario);
}
