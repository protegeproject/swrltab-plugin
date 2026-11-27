package org.swrltab.fixture;

import org.swrlapi.builtins.AbstractSWRLBuiltInLibrary;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgument;
import org.swrlapi.exceptions.SWRLBuiltInException;

import java.util.Collections;
import java.util.List;

/** A contributed library used by both unit and separate-bundle integration tests. */
public class ExampleLibrary extends AbstractSWRLBuiltInLibrary
{
  public int resets;

  public ExampleLibrary()
  {
    super("contributed", "urn:swrlapi:contributed#", Collections.singleton("answer"));
  }

  @Override public void reset()
  {
    resets++;
  }

  public boolean answer(List<SWRLBuiltInArgument> arguments) throws SWRLBuiltInException
  {
    checkNumberOfArgumentsEqualTo(1, arguments.size());
    if (isUnboundArgument(0, arguments))
      return processResultArgument(arguments, 0, java.math.BigInteger.valueOf(42));
    return getArgumentAsADecimal(0, arguments).compareTo(java.math.BigDecimal.valueOf(42)) == 0;
  }
}
