package org.delcom.app.modules.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {
    private final ItemRepository productRepository;

    public ItemService(ItemRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Item> getAllProducts(UUID userId) {
        return productRepository.findByUserId(userId);
    }

    public Item getProductById(UUID id, UUID userId) {
        Item product = productRepository.findById(id).orElse(null);
        if (product != null && product.getUserId().equals(userId)) {
            return product;
        }
        return null;
    }

    @Transactional
    public Item saveProduct(Item product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }
    
    public List<Object[]> getChartData(UUID userId) {
        return productRepository.countStockByCategory(userId);
    }
}