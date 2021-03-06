package org.molgenis.js.magma;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptRunner;
import org.springframework.stereotype.Service;

/** Runs a JavaScript using the Magma API with the given inputs and returns one output */
@Service
public class JsMagmaScriptRunner implements ScriptRunner {
  public static final String NAME = "JavaScript (Magma)";

  private final JsMagmaScriptExecutor jsScriptExecutor;

  public JsMagmaScriptRunner(JsMagmaScriptExecutor jsMagmaScriptExecutor) {
    this.jsScriptExecutor = requireNonNull(jsMagmaScriptExecutor);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean hasFileOutput(Script script) {
    return false;
  }

  @Override
  public String runScript(Script script, Map<String, Object> parameters) {
    String jsScript = script.getContent();
    Object scriptResult = jsScriptExecutor.executeScript(jsScript, parameters);
    return scriptResult != null ? scriptResult.toString() : null;
  }
}
