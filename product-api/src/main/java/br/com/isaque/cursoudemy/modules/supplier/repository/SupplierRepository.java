package br.com.isaque.cursoudemy.modules.supplier.repository;

import br.com.isaque.cursoudemy.modules.supplier.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    List<Supplier> findByNameIgnoreCaseContaining(String name);
}
