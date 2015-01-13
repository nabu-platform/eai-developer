package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.artifacts.subscription.DefinedSubscription;
import be.nabu.eai.repository.artifacts.subscription.SubscriptionConfiguration;
import be.nabu.eai.repository.managers.SubscriptionManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class SubscriptionGUIManager extends BaseJAXBGUIManager<SubscriptionConfiguration, DefinedSubscription> {

	public SubscriptionGUIManager() {
		super("Subscription", DefinedSubscription.class, new SubscriptionManager(), SubscriptionConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected DefinedSubscription newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new DefinedSubscription(entry.getId(), entry.getContainer(), entry.getRepository());
	}

}
