package br.com.isaque.cursoudemy.modules.product.service;

import br.com.isaque.cursoudemy.config.exception.SuccessResponse;
import br.com.isaque.cursoudemy.config.exception.ValidationException;
import br.com.isaque.cursoudemy.modules.category.service.CategoryService;
import br.com.isaque.cursoudemy.modules.product.dto.*;
import br.com.isaque.cursoudemy.modules.product.model.Product;
import br.com.isaque.cursoudemy.modules.product.repository.ProductRepository;
import br.com.isaque.cursoudemy.modules.sales.client.SalesClient;
import br.com.isaque.cursoudemy.modules.sales.dto.SalesConfirmationDTO;
import br.com.isaque.cursoudemy.modules.sales.enums.SalesStatus;
import br.com.isaque.cursoudemy.modules.sales.rabbitmq.SalesConfirmationSender;
import br.com.isaque.cursoudemy.modules.supplier.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class ProductService {

    private static final Integer ZERO = 0;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SalesConfirmationSender salesConfirmationSender;
    @Autowired
    private SalesClient salesClient;

    public List<ProductResponse> findAll(){
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByName(String name){

        if(isEmpty(name)){
            throw new ValidationException("The product name must be informed");
        }

        return productRepository.findByNameIgnoreCaseContaining(name)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findBySupplierId(Integer supplierId){

        if(isEmpty(supplierId)){
            throw new ValidationException("The product' supplier ID must be informed");
        }

        return productRepository.findBySupplierId(supplierId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByCategoryId(Integer categoryId){

        if(isEmpty(categoryId)){
            throw new ValidationException("The product' category ID must be informed");
        }

        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }


    public ProductResponse findByIdResponse(Integer id){
        return ProductResponse.of(findById(id));
    }

    public Product findById(Integer id){
        validateInformedId(id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ValidationException("There's no product for the given ID"));
    }

    public ProductResponse save(ProductRequest request){
        validateProductInformed(request);
        validateCategoryAndSupplierIdInformed(request);

        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());

        var product = productRepository.save(Product.of(request, category, supplier));
        return ProductResponse.of(product);
    }

    public ProductResponse update(ProductRequest request, Integer id){
        validateProductInformed(request);
        validateInformedId(id);
        validateCategoryAndSupplierIdInformed(request);

        var category = categoryService.findById(request.getCategoryId());
        var supplier = supplierService.findById(request.getSupplierId());
        var product = Product.of(request, category, supplier);
        product.setId(id);
        productRepository.save(product);
        return ProductResponse.of(product);
    }

    public SuccessResponse delete(Integer id){
        validateInformedId(id);
        productRepository.deleteById(id);
        return SuccessResponse.create("The product was deleted.");
    }

    private void validateProductInformed(ProductRequest request){
        if(isEmpty(request.getName()) || isEmpty(request.getQuantityAvailable())){
            throw new ValidationException("Product name and quantity available must be informed");
        }
        if(request.getQuantityAvailable() <= ZERO){
            throw new ValidationException("The quantity should not be less or equal to zero");
        }
    }

    private void validateCategoryAndSupplierIdInformed(ProductRequest request){
        if(isEmpty(request.getCategoryId())){
            throw new ValidationException("The Category ID was no informed");
        }

        if(isEmpty(request.getSupplierId())){
            throw new ValidationException("The Supplier ID was no informed");
        }
    }

    public Boolean existsByCategoryId(Integer categoryId){
        return productRepository.existsByCategoryId(categoryId);
    }

    public Boolean existsBySupplierId(Integer supplierId){
        return productRepository.existsBySupplierId(supplierId);
    }

    private void validateInformedId(Integer id){
        if(isEmpty(id)){
            throw new ValidationException("The product id must be informed.");
        }
    }

    private void validateStockUpdateData(ProductStockDTO product){
        if(isEmpty(product)
                || isEmpty(product.getSalesId())){
            throw  new ValidationException("The product data and the sales ID must be informed.");
        }
        if(isEmpty(product.getProducts())){
            throw  new ValidationException("The sales' products must be informed");
        }

        product
                .getProducts()
                .forEach(salesProduct -> {
                    if(isEmpty(salesProduct.getQuantity()) || isEmpty(salesProduct.getProductId())){
                        throw new ValidationException("The productID and quantity must be informed.");
                    }
                });
    }

    public void updateProductStock(ProductStockDTO product){
        try{
            validateStockUpdateData(product);
            updateStock(product);
        }catch (Exception e){
            log.error("Error while trying to update stock for message with error: {}", e.getMessage(), e);
            var rejectedMessage = new SalesConfirmationDTO(product.getSalesId(), SalesStatus.REJECTED);
            salesConfirmationSender.sendSalesConfirmationMessage(rejectedMessage);
        }
    }

    @Transactional
    public void updateStock(ProductStockDTO product){

        var productsForUpdate = new ArrayList<Product>();

        product.getProducts()
                .forEach(salesProduct ->{
                    var existingProducts = findById(salesProduct.getProductId());
                    validateQuantityInStock(salesProduct, existingProducts);
                    existingProducts.updateStock(salesProduct.getQuantity());
                    productsForUpdate.add(existingProducts);
                });
        if(!isEmpty(productsForUpdate)){
            productRepository.saveAll(productsForUpdate);
            var approvedMessage = new SalesConfirmationDTO(product.getSalesId(), SalesStatus.APPROVED);
            salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
        }
    }

    private void validateQuantityInStock(ProductQuantityDTO salesProduct, Product existingProducts){
        if(salesProduct.getQuantity() > existingProducts.getQuantityAvailable()){
            throw new ValidationException(
                    String.format("The product %s is out of stock", existingProducts.getId()));
        }
    }

    public ProductSalesResponse findProductSales(Integer id){
        var product = findById(id);
        try{
            var sales = salesClient.findSalesByProductId(product.getId())
                    .orElseThrow(() -> new ValidationException("The sales was not found by this product."));
            return ProductSalesResponse.of(product, sales.getSalesIds());
        }catch (Exception e){
            throw new ValidationException("There was an error trying to get the product's sales.");
        }
    }

    public SuccessResponse checkProductsStock(ProductCheckStockRequest request){
        if(isEmpty(request) || isEmpty(request.getProducts())){
            throw new ValidationException("The request data and products must be informed.");
        }
        request
                .getProducts()
                .forEach(this::validateStock);
        return SuccessResponse.create("The stock is ok!");
    }

    private void validateStock(ProductQuantityDTO productQuantity){
        if(isEmpty(productQuantity.getProductId()) || isEmpty(productQuantity.getQuantity())){
            throw new ValidationException("Product ID and quantity must be informed.");
        }
        var product = findById(productQuantity.getProductId());
        if(productQuantity.getQuantity() > product.getQuantityAvailable()){
            throw new ValidationException(String.format("The product %s is out of stock.", product.getId()));
        }
    }

}
