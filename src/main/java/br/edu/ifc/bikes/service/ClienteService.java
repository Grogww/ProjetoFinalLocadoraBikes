package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.dto.mapper.ClienteMapper;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.exception.ClienteUniqueViolationException;
import br.edu.ifc.bikes.exception.CpfUniqueViolationException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper clienteMapper;

    //o cliente é associado ao usuário autenticado, que só pode ter um cadastro de cliente
    public ClienteResponseDTO create(ClienteRequestDTO clienteRequestDTO, Long idUsuario) {
        if (clienteRepository.existsByUsuarioId(idUsuario)){
            throw new ClienteUniqueViolationException(String.format("O usuário id=%d já possui um cliente cadastrado", idUsuario));
        }
        try {
            Cliente cliente = clienteMapper.toCliente(clienteRequestDTO);
            cliente.setUsuario(usuarioRepository.getReferenceById(idUsuario));
            return clienteMapper.toResponse(clienteRepository.save(cliente));
        }catch (DataIntegrityViolationException ex){
            throw new CpfUniqueViolationException(String.format("O cpf '%s' já está cadastrado", clienteRequestDTO.cpf()));
        }
    }

    @Transactional()
    public ClienteResponseDTO getById(Long id) {
        return clienteMapper.toResponse(clienteRepository.findById(id).orElseThrow(
                ()->new EntityNotFoundException(String.format("Cliente com id=%d não encontrado", id))
        ));
    }

    @Transactional()
    public ClienteResponseDTO getByUsuarioId(Long idUsuario) {
        return clienteMapper.toResponse(clienteRepository.findByUsuarioId(idUsuario).orElseThrow(
                ()->new EntityNotFoundException(String.format("Cliente do usuário id=%d não encontrado", idUsuario))
        ));
    }

    public List<ClienteResponseDTO> getAll(){
        return clienteMapper.toResponse(clienteRepository.findAll());
    }
}
