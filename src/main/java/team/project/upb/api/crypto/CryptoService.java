package team.project.upb.api.crypto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    @Autowired
    private CryptoRepository cryptoRepository;

    public void save(Crypto crypto) {
        this.cryptoRepository.save(crypto);
    }

    public Crypto get(Long id) {
        return this.cryptoRepository.findById(id).get();
    }
}
