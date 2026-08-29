package africa.growtogether.platform.ens; import static org.junit.jupiter.api.Assertions.*; import java.time.Instant; import java.util.UUID; import org.junit.jupiter.api.Test;
class NotificationRequestTest {
 @Test void whatsAppChannelParticipatesInStandardLifecycle(){
  var n=new NotificationRequest(
      UUID.randomUUID(),
      "parent_activation",
      "+256700000000",
      NotificationChannel.WHATSAPP,
      NotificationPriority.HIGH,
      "Activate your GrowTogether account",
      "Use your secure activation link.",
      "corr-whatsapp-1",
      "GT-SCHOOL",
      "ADM-001"
  );
  n.queue();
  n.processing();
  n.sent("whatsapp-provider-ref-1");
  n.delivered();
  assertEquals(NotificationChannel.WHATSAPP,n.channel());
  assertEquals(NotificationStatus.DELIVERED,n.notificationStatus());
  assertEquals("whatsapp-provider-ref-1",n.providerReference());
 }
 @Test void lifecycleAndRetryAreControlled(){var n=new NotificationRequest(UUID.randomUUID(),"welcome","a@b.com",NotificationChannel.EMAIL,NotificationPriority.NORMAL,"Hi","Body","c","TEST","1");n.queue();n.processing();n.fail("temporary",Instant.now().plusSeconds(30),3);assertEquals(NotificationStatus.RETRYING,n.notificationStatus());n.processing();n.sent("provider-1");n.delivered();assertEquals(NotificationStatus.DELIVERED,n.notificationStatus());} @Test void deadLettersAtMaximumAttempts(){var n=new NotificationRequest(UUID.randomUUID(),"x","1",NotificationChannel.SMS,NotificationPriority.HIGH,null,"Body",null,"TEST",null);n.queue();n.processing();n.fail("bad",Instant.now(),1);assertEquals(NotificationStatus.DEAD_LETTER,n.notificationStatus());}}
