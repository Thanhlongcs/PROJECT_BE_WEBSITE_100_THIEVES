package soixam.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soixam.dto.reponse.ResponseMessage;
import soixam.model.Category;
import soixam.model.Product;
import soixam.model.User;
import soixam.repository.ICategoryRepository;
import soixam.security.userpincal.UserDetailService;
import soixam.service.category.ICategoryService;
import soixam.service.product.IProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {
    @Autowired
    private IProductService productService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private UserDetailService userDetailService;
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product){
        User currentUser = userDetailService.getCurrentUser();
        if (productService.existsByName(product.getName())){
            return new ResponseEntity<>(new ResponseMessage("product_invalid"),HttpStatus.OK);
        }

        Long idCate = product.getCategory().getId();
        Optional<Category> categoryOptional = categoryService.findById(idCate);
        if (!categoryOptional.isPresent()) {
            return new ResponseEntity<>(new ResponseMessage("cate_not"), HttpStatus.NOT_FOUND);
        }
        product.setUser(currentUser);
        productService.save(product);
        return new ResponseEntity<>(new ResponseMessage("create"),HttpStatus.OK);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> editProduct(@PathVariable Long id, @RequestBody Product product){
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()){
            return new ResponseEntity<>(new ResponseMessage("product_not"),HttpStatus.NOT_FOUND);
        }else {
            productOptional.get().setName(product.getName());
            productOptional.get().setPrice(product.getPrice());
            productOptional.get().setAvatar(product.getAvatar());
            productOptional.get().setQuantity(product.getQuantity());

            Long idCate = product.getCategory().getId();
            Optional<Category> categoryOptional = categoryService.findById(idCate);
            if (!categoryOptional.isPresent()){
                return new ResponseEntity<>(new ResponseMessage("cate_not"),HttpStatus.NOT_FOUND);
            }else {
                productOptional.get().setCategory(categoryOptional.get());
            }
            productService.save(productOptional.get());
            return new ResponseEntity<>(new ResponseMessage("edit"),HttpStatus.OK);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> detailProduct(@PathVariable Long id){
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()){
            return new ResponseEntity<>(new ResponseMessage("product_not"),HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<>(productOptional.get(),HttpStatus.OK);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable("id") Optional<Product> product){
        if (!product.isPresent()){
            return new ResponseEntity<>(new ResponseMessage("product_not"),HttpStatus.NOT_FOUND);
        }
        productService.deleteById(product.get().getId());
        return new ResponseEntity<>(new ResponseMessage("delete"),HttpStatus.OK);
    }
    @GetMapping
    public ResponseEntity<?>showList(){
        List<Product>productList = productService.findAll();
        return new ResponseEntity<>(productList,HttpStatus.OK);
    }
    @GetMapping("search/{name}")
    public ResponseEntity<?>searchNameProduct(@PathVariable String name){
        if (name.trim().equals("")){
            return new ResponseEntity<>(new ResponseMessage("not_found"),HttpStatus.OK);
        }
        return new ResponseEntity<>(productService.findAllByNameContaining(name),HttpStatus.OK);
    }
}
