package africa.growtogether.platform.eiam.recovery;
public class InvalidRecoveryTokenException extends RuntimeException { public InvalidRecoveryTokenException(){super("Recovery token is invalid, expired, or already used.");} }
