package capstonesu25.warehouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pusher.rest.Pusher;

@Configuration
public class PusherConfig {

  @Bean
  public Pusher pusher(@Value("${spring.pusher.app-id}") String appId,
                       @Value("${spring.pusher.key}")   String key,
                       @Value("${spring.pusher.secret}")String secret,
                       @Value("${spring.pusher.cluster}") String cluster) {
      Pusher pusher = new Pusher(appId, key, secret);       
      pusher.setCluster(cluster);
      return pusher;
  }
}