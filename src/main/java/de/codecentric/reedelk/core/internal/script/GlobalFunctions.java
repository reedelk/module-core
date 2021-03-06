package de.codecentric.reedelk.core.internal.script;

import de.codecentric.reedelk.runtime.api.configuration.ConfigurationService;
import de.codecentric.reedelk.runtime.api.script.ScriptGlobalFunctions;

import java.util.HashMap;
import java.util.Map;

public class GlobalFunctions implements ScriptGlobalFunctions {

    private final long moduleId;

    private final Log log;
    private final Util util;
    private final Config config;
    private final BCryptUtil bCryptUtil;

    public GlobalFunctions(long moduleId, ConfigurationService configurationService) {
        this.moduleId = moduleId;
        this.log = new Log();
        this.util = new Util();
        this.config = new Config(configurationService);
        this.bCryptUtil = new BCryptUtil();
    }

    @Override
    public long moduleId() {
        return moduleId;
    }

    @Override
    public Map<String, Object> bindings() {
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("Log", log);       // binding key are names of the global variables.
        bindings.put("Util", util);     // binding key are names of the global variables.
        bindings.put("Config", config); // binding key are names of the global variables.
        bindings.put("BCryptUtil", bCryptUtil); // binding key are names of the global variables.
        return bindings;
    }
}
