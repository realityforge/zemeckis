package zemeckis;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class VirtualProcessorUnitsHolderTest
  extends AbstractTest
{
  @Test
  public void basicOperation()
  {
    assertFalse( VirtualProcessorUnitsHolder.isVpuActivated() );
    assertNull( VirtualProcessorUnitsHolder.currentVpu() );

    final VirtualProcessorUnit vpu = VirtualProcessorUnitsHolder.macroTaskVpu();
    VirtualProcessorUnitsHolder.activate( vpu, () -> {
      assertTrue( VirtualProcessorUnitsHolder.isVpuActivated() );
      assertEquals( VirtualProcessorUnitsHolder.currentVpu(), vpu );
      assertInvariantFailure( () -> VirtualProcessorUnitsHolder.CurrentVPU.activate( VirtualProcessorUnitsHolder.microTaskVpu() ),
                              "Zemeckis-0004: Attempting to activate VirtualProcessorUnit named 'Micro' but an existing VirtualProcessorUnit named 'Macro' is activated" );
    } );

    assertFalse( VirtualProcessorUnitsHolder.isVpuActivated() );
    assertNull( VirtualProcessorUnitsHolder.currentVpu() );

    assertInvariantFailure( () -> VirtualProcessorUnitsHolder.CurrentVPU.deactivate( vpu ),
                            "Zemeckis-0005: Attempting to deactivate VirtualProcessorUnit named 'Macro' but no VirtualProcessorUnit is activated" );
  }
}
