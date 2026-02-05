package uk.ac.comm2020;

import com.sun.net.httpserver.HttpServer;
import uk.ac.comm2020.api.PassportsHandler;
import uk.ac.comm2020.api.ProductsHandler;
import uk.ac.comm2020.api.StaticFileHandler;
import uk.ac.comm2020.api.TemplateByIdHandler;
import uk.ac.comm2020.api.TemplatesHandler;
import uk.ac.comm2020.config.EnvConfig;
import uk.ac.comm2020.dao.PassportDao;
import uk.ac.comm2020.dao.ProductDao;
import uk.ac.comm2020.dao.TemplateDao;
import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.service.PassportService;
import uk.ac.comm2020.service.ProductService;
import uk.ac.comm2020.service.TemplateService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

// Build services, set routes, then start server.
public class App {

    public static void main(String[] args) throws IOException {
        // load env config first
        EnvConfig config = EnvConfig.load();
        int port = config.getInt("APP_PORT", 8080);

        // make db + services
        Database database = new Database(config);
        TemplateService templateService = new TemplateService(new TemplateDao(database));
        ProductService productService = new ProductService(new ProductDao(database));

        // passport need more dao, so build it here
        PassportService passportService = new PassportService(
                new PassportDao(database),
                new TemplateDao(database),
                new ProductDao(database)
        );

        // make http server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // api routes
        server.createContext("/api/templates", new TemplatesHandler(templateService));
        server.createContext("/api/templates/", new TemplateByIdHandler(templateService));
        server.createContext("/api/products", new ProductsHandler(productService));
        server.createContext("/api/passports", new PassportsHandler(passportService));

        // static files (web page)
        server.createContext("/", new StaticFileHandler("static"));

        // simple thread pool for requests
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        // log for user
        System.out.println("API server running on port " + port);
    }
}
