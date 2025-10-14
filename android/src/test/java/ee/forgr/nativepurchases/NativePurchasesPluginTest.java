package ee.forgr.nativepurchases;

import static org.junit.Assert.assertEquals;

import com.getcapacitor.JSObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Test;

public class NativePurchasesPluginTest {

  private JSObject invokeParse(String period)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    NativePurchasesPlugin plugin = new NativePurchasesPlugin();
    Method m = NativePurchasesPlugin.class.getDeclaredMethod("parseSubscriptionPeriod", String.class);
    m.setAccessible(true);
    return (JSObject) m.invoke(plugin, period);
  }

  @Test
  public void testParseISO8601Duration_monthly() throws Exception {
    JSObject result = invokeParse("P1M");
    assertEquals(1, result.getInteger("numberOfUnits").intValue());
    assertEquals(2, result.getInteger("unit").intValue()); // MONTH
  }

  @Test
  public void testParseISO8601Duration_quarterly() throws Exception {
    JSObject result = invokeParse("P3M");
    assertEquals(3, result.getInteger("numberOfUnits").intValue());
    assertEquals(2, result.getInteger("unit").intValue()); // MONTH
  }

  @Test
  public void testParseISO8601Duration_yearly() throws Exception {
    JSObject result = invokeParse("P1Y");
    assertEquals(1, result.getInteger("numberOfUnits").intValue());
    assertEquals(3, result.getInteger("unit").intValue()); // YEAR
  }

  @Test
  public void testParseISO8601Duration_invalid() throws Exception {
    JSObject result = invokeParse("INVALID");
    assertEquals(1, result.getInteger("numberOfUnits").intValue());
    assertEquals(2, result.getInteger("unit").intValue()); // Default MONTH
  }
}


