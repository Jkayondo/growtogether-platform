package africa.growtogether.platform.common.events;

public interface EventPublisher {

    void publish(DomainEvent event);
}
