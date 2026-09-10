package africa.growtogether.platform.school.results.parent;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;


@RestController
@RequestMapping("/api/parent/results")
public class ParentResultController {


    private final ParentResultQueryService service;


    public ParentResultController(
            ParentResultQueryService service
    ){

        this.service = service;

    }



    @GetMapping("/{learnerId}")
    public ResponseEntity<ParentResultView> getResults(

            @PathVariable UUID learnerId,

            @RequestParam UUID tenantId,

            @RequestParam UUID academicYearId,

            @RequestParam UUID academicTermId,

            @RequestParam UUID classGradeId

    ){


        ParentResultView result =
                service.getResults(

                        tenantId,

                        learnerId,

                        academicYearId,

                        academicTermId,

                        classGradeId

                );


        return ResponseEntity.ok(result);

    }


}
