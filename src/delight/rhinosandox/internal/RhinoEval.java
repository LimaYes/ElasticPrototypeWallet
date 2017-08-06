package delight.rhinosandox.internal;

import java.lang.reflect.Member;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("all")
public class RhinoEval extends FunctionObject {
  public RhinoEval(final String name, final Member methodOrConstructor, final Scriptable scope) {
    super(name, methodOrConstructor, scope);
  }
  
  @Override
  public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
    Object _get = args[0];
    final String script = _get.toString();
    final String toFind = "//# sourceURL=";
    final int idx = script.lastIndexOf(toFind);
    String scriptUrl = null;
    if ((idx != (-1))) {
      int _length = toFind.length();
      int _plus = (idx + _length);
      String _substring = script.substring(_plus);
      scriptUrl = _substring;
      String _replace = scriptUrl.replace("\n", "");
      String _replace_1 = _replace.replace(" ", "");
      scriptUrl = _replace_1;
    }
    return cx.evaluateString(scope, script, scriptUrl, 1, null);
  }
}
