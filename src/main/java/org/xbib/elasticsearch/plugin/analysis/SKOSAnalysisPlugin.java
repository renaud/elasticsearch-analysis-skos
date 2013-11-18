
package org.xbib.elasticsearch.plugin.analysis;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

import org.xbib.elasticsearch.index.analysis.SKOSTokenFilterFactory;

/**
 */
public class SKOSAnalysisPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "analysis-skos";
    }

    @Override
    public String description() {
        return "SKOS analysis support";
    }

    public void onModule(AnalysisModule module) {
        module.addTokenFilter("skos", SKOSTokenFilterFactory.class);
    }
}
