package africa.growtogether.platform.eip;

interface ExternalProviderAdapter {

    String connectorType();

    ExternalProviderDispatchResult dispatch(
            ExternalProviderExecutionContext context,
            ExternalProviderDispatchRequest request
    );
}
