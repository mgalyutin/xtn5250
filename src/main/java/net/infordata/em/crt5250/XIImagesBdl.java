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
!!V 15/06/99 rel. 1.13 - creation.
 */
package net.infordata.em.crt5250;

import net.infordata.em.util.XIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;

/**
 * The image bundle.
 *
 * @author Valentino Proietti - Infordata S.p.A.
 */
public class XIImagesBdl extends ListResourceBundle {

  private static XIImagesBdl cvImagesBdl;

  private static final Object[][] cvContents;

  static {
    cvContents = new Object[][]{
        {"ShiftDown",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/ShiftDown.gif")},
        {"CapsLock",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/CapsLock.gif")},
        {"3dFx",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/3dFx.gif")},
        {"Copy",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/Copy.gif")},
        {"Paste",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/Paste.gif")},
        {"RefCursor",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/RefCursor.gif")},
        {"Print",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/Print.gif")},
        {"Logo",
            XIUtil.createImage(
                XIImagesBdl.class, "resources/Logo.gif")},
    };
  }

  private XIImagesBdl() {
  }

  public static @NotNull XIImagesBdl getImagesBdl() {
    if (cvImagesBdl == null) {
      cvImagesBdl = new XIImagesBdl();
    }

    return cvImagesBdl;
  }

  @Override
  public Object[][] getContents() {
    return cvContents;
  }

  public final Image getImage(@NotNull String anImageName) {
    return ((ImageIcon) getIcon(anImageName)).getImage();
  }

  private final @NotNull Map<String, Icon> ivIcons = new HashMap<>();

  public synchronized final @NotNull Icon getIcon(@NotNull String anImageName) {
    Icon icon = ivIcons.get(anImageName);
    if (icon == null) {
      icon = new ImageIcon((Image) getObject(anImageName));
      ivIcons.put(anImageName, icon);
    }
    return icon;
  }

}
