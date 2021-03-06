package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
class NegotiatorConfigFactory
    extends AbstractSystemEntityFactory<NegotiatorConfig, NegotiatorConfigMetadata, String> {
  NegotiatorConfigFactory(NegotiatorConfigMetadata myEntityMeta, EntityPopulator entityPopulator) {
    super(NegotiatorConfig.class, myEntityMeta, entityPopulator);
  }
}
