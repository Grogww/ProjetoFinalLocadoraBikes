package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.dto.mapper.LocacaoMapper;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Locacao;
import br.edu.ifc.bikes.exception.BicicletaIndisponivelException;
import br.edu.ifc.bikes.exception.CartaoVencidoException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import br.edu.ifc.bikes.repository.ClienteRepository;
import br.edu.ifc.bikes.repository.LocacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class LocacaoService {

    private final LocacaoRepository locacaoRepository;
    private final ClienteRepository clienteRepository;
    private final BicicletaRepository bicicletaRepository;
    private final LocacaoMapper locacaoMapper;

    //o cliente recebe a primeira bicicleta disponível, que passa para ALUGADA e não entra em outra locação
    @Transactional()
    public LocacaoResponseDTO alugar(LocacaoRequestDTO locacaoRequestDTO, Long idUsuario) {
        Cliente cliente = clienteRepository.findByUsuarioId(idUsuario).orElseThrow(
                ()->new EntityNotFoundException(String.format("Cliente do usuário id=%d não encontrado", idUsuario))
        );

        YearMonth validade = YearMonth.parse(locacaoRequestDTO.validadeCartao(), DateTimeFormatter.ofPattern("MM/yy"));
        if (validade.isBefore(YearMonth.now())){
            throw new CartaoVencidoException(String.format("O cartão com validade %s está vencido", locacaoRequestDTO.validadeCartao()));
        }

        Bicicleta bicicleta = bicicletaRepository.findFirstByStatusOrderByIdAsc(Bicicleta.Status.DISPONIVEL).orElseThrow(
                ()->new BicicletaIndisponivelException("Não há bicicleta disponível no momento")
        );
        bicicleta.setStatus(Bicicleta.Status.ALUGADA);

        Locacao locacao = new Locacao();
        locacao.setCliente(cliente);
        locacao.setBicicleta(bicicleta);
        locacao.setDataRetirada(LocalDateTime.now());
        locacao.setDataPrevistaDevolucao(locacao.getDataRetirada().plusHours(locacaoRequestDTO.horas()));
        locacao.setFinalCartao(locacaoRequestDTO.numeroCartao().substring(12));
        return locacaoMapper.toResponse(locacaoRepository.save(locacao));
    }
}
