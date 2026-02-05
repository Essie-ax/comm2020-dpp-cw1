package uk.ac.comm2020.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.ac.comm2020.model.Product;
import uk.ac.comm2020.service.ProductService;
import uk.ac.comm2020.service.ServiceException;
import uk.ac.comm2020.util.RequestUtil;
import uk.ac.comm2020.util.ResponseUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Handler for products api.
// Only do simple check here, then call service.
public class ProductsHandler implements HttpHandler {
    private final ProductService productService;

    // take service from outside, so code easy to use and test
    public ProductsHandler(ProductService productService) {
        this.productService = productService;
    }

    // Main entry. Only support GET.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // only GET is ok here
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                ResponseUtil.sendJson(exchange, 405,
                        ResponseUtil.error("METHOD_NOT_ALLOWED", "Only GET is allowed", null));
                return;
            }

            Map<String, String> query = RequestUtil.parseQuery(exchange.getRequestURI());

            // category is must, without it we cannot find data
            String category = query.get("category");
            if (category == null || category.isBlank()) {
                ResponseUtil.sendJson(exchange, 400,
                        ResponseUtil.error("BAD_REQUEST", "Query param 'category' is required", null));
                return;
            }

            // get products list by category
            List<Product> products = productService.getProductsByCategory(category);

            // wrap data then send back
            Map<String, Object> data = new HashMap<>();
            data.put("products", products);
            ResponseUtil.sendJson(exchange, 200, ResponseUtil.success(data));
        } catch (ServiceException e) {
            // service error, keep status and message
            ResponseUtil.sendJson(exchange, e.getStatus(),
                    ResponseUtil.error(e.getCode(), e.getMessage(), e.getDetails()));
        } catch (Exception e) {
            // other error, just 500
            ResponseUtil.sendJson(exchange, 500,
                    ResponseUtil.error("INTERNAL_ERROR", "Unexpected server error", null));
        }
    }
}
