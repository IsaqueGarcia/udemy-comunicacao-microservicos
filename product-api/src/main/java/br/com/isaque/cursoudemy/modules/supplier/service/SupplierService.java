package br.com.isaque.cursoudemy.modules.supplier.service;

import br.com.isaque.cursoudemy.config.exception.SuccessResponse;
import br.com.isaque.cursoudemy.config.exception.ValidationException;
import br.com.isaque.cursoudemy.modules.product.service.ProductService;
import br.com.isaque.cursoudemy.modules.supplier.dto.SupplierRequest;
import br.com.isaque.cursoudemy.modules.supplier.dto.SupplierResponse;
import br.com.isaque.cursoudemy.modules.supplier.model.Supplier;
import br.com.isaque.cursoudemy.modules.supplier.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductService productService;

    public List<SupplierResponse> findAll(){
        return supplierRepository.findAll()
                .stream()
                .map(SupplierResponse::of)
                .collect(Collectors.toList());
    }

    public List<SupplierResponse> findByName(String name){

        if(isEmpty(name)){
            throw new ValidationException("The supplier name must be informed");
        }

        return supplierRepository.findByNameIgnoreCaseContaining(name)
                .stream()
                .map(SupplierResponse::of)
                .collect(Collectors.toList());
    }

    public SupplierResponse findByIdResponse(Integer id){
        return SupplierResponse.of(findById(id));
    }

    public Supplier findById(Integer id){

        validateInformedId(id);

        return supplierRepository.findById(id)
                .orElseThrow(() -> new ValidationException("There's no supplier for the given ID"));
    }

    public SupplierResponse save(SupplierRequest request){
        validateSupplierInformed(request);
        var supplier = supplierRepository.save(Supplier.of(request));
        return SupplierResponse.of(supplier);
    }

    public SupplierResponse update(SupplierRequest request, Integer id){
        validateSupplierInformed(request);
        validateInformedId(id);
        var supplier = Supplier.of(request);
        supplier.setId(id);
        supplierRepository.save(supplier);
        return SupplierResponse.of(supplier);
    }


    private void validateSupplierInformed(SupplierRequest request){
        if(isEmpty(request.getName())){
            throw new ValidationException("Supplier name must be informed.");
        }
    }

    public SuccessResponse delete(Integer id){
        validateInformedId(id);
        if(productService.existsBySupplierId(id)){
            throw new ValidationException("You cannot delete this supplier because it's already defined by a product.");
        }
        supplierRepository.deleteById(id);
        return SuccessResponse.create("The supplier was deleted.");
    }

    private void validateInformedId(Integer id){
        if(isEmpty(id)){
            throw new ValidationException("The supplier id must be informed.");
        }
    }
}
