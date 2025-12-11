package DC_square.spring.web.controller.walk;

import DC_square.spring.service.WalkService.ReverseGeocodingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReverseGeocodingController {

  private final ReverseGeocodingService reverseGeocodingService;

  public ReverseGeocodingController(ReverseGeocodingService reverseGeocodingService) {
    this.reverseGeocodingService = reverseGeocodingService;
  }

  @GetMapping("/reverse-geocode")
  public String getAddress(@RequestParam double latitude, @RequestParam double longitude) {
    return reverseGeocodingService.getAddressFromCoordinates(latitude, longitude);
  }
}
