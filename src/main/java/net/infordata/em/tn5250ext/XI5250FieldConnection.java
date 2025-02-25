/*
Copyright 2007 Infordata S.p.A.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.tn5250ext;

import net.infordata.em.crt5250.XI5250Field;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Connect an AWT Component to a XI5250Field
 * moving and resizing the Component as the field grows or shrinks.
 *
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250FieldConnection {
  private final Component ivComponent;

  private final int ivColsDelta;
  private final int ivRowsDelta;
  private final int ivNCols;
  private final int ivNRows;

  public XI5250FieldConnection(@NotNull XI5250PanelHandler aPanelHndl, XI5250Field aField,
                               Component aComponent,
                               int aColsDelta, int aRowsDelta,
                               int aNCols, int aNRows) {
    ivColsDelta = aColsDelta;
    ivRowsDelta = aRowsDelta;
    ivNCols = aNCols;
    ivNRows = aNRows;
    ivComponent = aComponent;
    aPanelHndl.connect(aField, this);
  }

  public XI5250FieldConnection(@NotNull XI5250PanelHandler aPanelHndl,
                               XI5250Field aField,
                               Component aComponent) {
    this(aPanelHndl, aField, aComponent, 0, 0, 1, 1);
  }

  protected void recalcBounds(@NotNull XI5250EmulatorExt aEm, @NotNull XI5250Field aField) {
    Rectangle[] rcts = aField.getRows();
    // use the last row
    Rectangle   rct  = rcts[rcts.length - 1];

    int bufPos = aEm.toLinearPos(rct.x + rct.width, rct.y);
    bufPos += aEm.toLinearPos(ivColsDelta, ivRowsDelta);

    Point     pt  = aEm.toPoints(aEm.toColPos(bufPos), aEm.toRowPos(bufPos));

    ivComponent.setBounds(pt.x, pt.y,
                          ivNCols * aEm.getCharSize().width,
                          ivNRows * aEm.getCharSize().height);
  }

  public final Component getComponent() {
    return ivComponent;
  }

}
