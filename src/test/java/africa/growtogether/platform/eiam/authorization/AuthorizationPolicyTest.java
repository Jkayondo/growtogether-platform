package africa.growtogether.platform.eiam.authorization;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class AuthorizationPolicyTest {
 @Test void normalizesAndMatchesPolicy(){var p=new AuthorizationPolicy("school admin","School admin",null,"learner","read",PolicyEffect.ALLOW,10,"school.learners.read","school_admin",false,2,true);assertEquals("SCHOOL_ADMIN",p.getCode());assertEquals("SCHOOL_ADMIN",p.getRequiredRole());assertTrue(p.matches("LEARNER","READ"));assertEquals(2,p.getMinimumAal());}
 @Test void inactivePolicyDoesNotMatch(){var p=new AuthorizationPolicy("deny","Deny",null,"document","delete",PolicyEffect.DENY,100,null,null,false,1,false);assertFalse(p.matches("document","delete"));}
}
