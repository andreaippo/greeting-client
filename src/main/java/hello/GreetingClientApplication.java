package hello;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@RestController
@SpringBootApplication
public class GreetingClientApplication {

  private static Logger log = LoggerFactory.getLogger(GreetingClientApplication.class);

  @Value("${load-balancer-host}")
  private String loadBalancerHost;

  // Open circuit when reaching 20% of failure rate - try to half-open after 1' (default)
  private final CircuitBreaker circuitBreaker = CircuitBreaker.of("default",
    CircuitBreakerConfig.custom().failureRateThreshold(20f).build());

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

    log.info("circuit breaker is {}", circuitBreaker.getState());

    return circuitBreaker.executeSupplier(greetingSupplier);

    //    Retry retry = Retry.ofDefaults("greeting-service-retryer");
    //
    //    return retry.executeSupplier(greetingSupplier);

  }

  @GetMapping("/")
  public String home() {
    log.info("Access /");
    return "Hi!";
  }
}