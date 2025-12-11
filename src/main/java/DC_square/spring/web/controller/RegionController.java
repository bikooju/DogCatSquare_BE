package DC_square.spring.web.controller;

import DC_square.spring.service.RegionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Region", description = "지역 관련 API")
@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

  private final RegionService regionService;

//    @Operation(summary = "지역 생성 API")
//    @PostMapping
//    public ApiResponse<Long> createRegion(@RequestBody RegionRequestDTO request) {
//        regionService.creatRegion(request);
//        Long regionId = regionService.creatRegion(request);
//        return ApiResponse.onSuccess(regionId);
//    }
}
