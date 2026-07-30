package africa.growtogether.platform.school.parent.scheduling.delivery;


import org.springframework.stereotype.Service;


@Service
public class ParentEngagementScheduledDeliveryService {


    private final ParentEngagementScheduledDeliveryRepository repository;


    public ParentEngagementScheduledDeliveryService(
            ParentEngagementScheduledDeliveryRepository repository
    ) {

        this.repository = repository;
    }


    public ParentEngagementScheduledDelivery create(
            ParentEngagementScheduledDelivery delivery
    ) {

        return repository.save(delivery);
    }


    public void markSent(
            ParentEngagementScheduledDelivery delivery
    ) {

        delivery.markSent();

        repository.save(delivery);
    }


    public void markFailed(
            ParentEngagementScheduledDelivery delivery
    ) {

        delivery.markFailed();

        repository.save(delivery);
    }
}
