package africa.growtogether.platform.ewe;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class WorkflowDefinitionTest {@Test void normalizesAndActivates(){WorkflowDefinition d=new WorkflowDefinition("school admission","Admission","Academic",null);assertEquals("SCHOOL_ADMISSION",d.getCode());d.activate(1);assertEquals(WorkflowDefinitionStatus.ACTIVE,d.getDefinitionStatus());assertEquals(1,d.getActiveVersion());}}
