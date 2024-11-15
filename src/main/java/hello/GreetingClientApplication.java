package hello;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

@RestController
@SpringBootApplication
public class GreetingClientApplication {

  private static Logger log = LoggerFactory.getLogger(GreetingClientApplication.class);

  @Value("${load-balancer-host}")
  private String loadBalancerHost;

  public static void main(String[] args) {
    SpringApplication.run(GreetingClientApplication.class, args);
  }

  @GetMapping("/greeting-client")
  public String greet() {
    log.info("Access /greeting-client");

    Supplier<String> greetingSupplier = () -> RestClient.builder()
      .baseUrl("http://%s:8080".formatted(loadBalancerHost))
      .build()
      .method(HttpMethod.GET)
      .uri("http://%s:8080/hi".formatted(loadBalancerHost))
      .retrieve()
      .body(String.class);

    Retry retry = Retry.ofDefaults("greeting-service-retryer");

    return retry.executeSupplier(greetingSupplier);

  }

  @GetMapping("/")
  public String home() {
    log.info("Access /");
    return "Hi!";
  }
}