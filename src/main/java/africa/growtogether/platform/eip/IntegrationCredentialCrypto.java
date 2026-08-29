package africa.growtogether.platform.eip;
import java.nio.charset.StandardCharsets; import java.security.SecureRandom; import java.util.*; import javax.crypto.*; import javax.crypto.spec.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
@Service public class IntegrationCredentialCrypto {private final SecretKey key; private final SecureRandom random=new SecureRandom(); private final String keyId;
 public IntegrationCredentialCrypto(
     @Value("${gt.eip.encryption-key:}") String encoded,
     @Value("${gt.eip.encryption-key-id:}") String keyId
 ){
  this.keyId =
      keyId == null || keyId.isBlank()
          ? null
          : keyId.trim();

  /*
   * Ordinary GT operation may start without external-provider
   * credentials. However, EIP must never fall back to a built-in
   * encryption secret for real connector credentials.
   */
  if(encoded == null || encoded.isBlank()){
   key = null;
   return;
  }

  byte[] raw;

  try{
   raw = Base64.getDecoder().decode(encoded.trim());
  }catch(IllegalArgumentException e){
   throw new IllegalStateException(
       "GT EIP encryption key must be valid Base64",
       e
   );
  }

  if(raw.length != 32){
   throw new IllegalStateException(
       "GT EIP encryption key must be 32 bytes"
   );
  }

  if(this.keyId == null){
   throw new IllegalStateException(
       "GT EIP encryption key id is required when encryption is configured"
   );
  }

  key = new SecretKeySpec(raw,"AES");
 }
 public String encrypt(String plaintext){if(plaintext==null||plaintext.isBlank())return null;requireConfigured();try{byte[] iv=new byte[12];random.nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,iv));byte[] out=c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));byte[] all=new byte[iv.length+out.length];System.arraycopy(iv,0,all,0,iv.length);System.arraycopy(out,0,all,iv.length,out.length);return Base64.getUrlEncoder().withoutPadding().encodeToString(all);}catch(Exception e){throw new IllegalStateException("Unable to encrypt integration credential",e);}}
 String decrypt(String ciphertext,String storedKeyId){
  if(ciphertext==null||ciphertext.isBlank())return null;

  requireConfigured();

  if(storedKeyId==null||storedKeyId.isBlank()){
   throw new IllegalStateException(
    "Integration credential key id is missing"
   );
  }

  if(!keyId.equals(storedKeyId)){
   throw new IllegalStateException(
    "Integration credential key id does not match active key"
   );
  }

  try{
   byte[] all=Base64.getUrlDecoder().decode(ciphertext);

   if(all.length<=12){
    throw new IllegalStateException(
     "Integration credential ciphertext is invalid"
    );
   }

   byte[] iv=Arrays.copyOfRange(all,0,12);
   byte[] encrypted=Arrays.copyOfRange(all,12,all.length);

   Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
   c.init(
    Cipher.DECRYPT_MODE,
    key,
    new GCMParameterSpec(128,iv)
   );

   return new String(
    c.doFinal(encrypted),
    StandardCharsets.UTF_8
   );

  }catch(IllegalStateException e){
   throw e;
  }catch(Exception e){
   throw new IllegalStateException(
    "Unable to decrypt integration credential",
    e
   );
  }
 }

 private void requireConfigured(){
  if(key == null || keyId == null || keyId.isBlank()){
   throw new IllegalStateException(
       "GT EIP integration credential encryption is not configured"
   );
  }
 }

 public String keyId(){return keyId;}}
