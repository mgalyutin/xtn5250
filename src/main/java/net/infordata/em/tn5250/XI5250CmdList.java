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
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.tn5250;

import net.infordata.em.tnprot.XITelnet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 5250 Commands list (Works like a macro command).
 *
 * @author Valentino Proietti - Infordata S.p.A.
 */
public class XI5250CmdList extends XI5250Cmd {

    private static final Logger LOGGER = LoggerFactory.getLogger(XI5250CmdList.class);

    private static final Class<?> @NotNull [] cv5250CmdClasses = new Class[256];

    static {
        // READ immediate
        // This command sends back the contents of all the input fields on the display.
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_IMMEDIATE)] =
                XIReadImmediateCmd.class;
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_FIELDS)] =
                XIReadFieldsCmd.class;
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_MDT_FIELDS)] =
                XIReadMdtFieldsCmd.class;
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_SCREEN)] =
                XIReadScreenCmd.class;
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_SAVE_SCREEN)] =
                XISaveScreenCmd.class;

        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_CLEAR_FMT_TABLE)] =
                XIClearFmtTableCmd.class;
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_CLEAR_UNIT)] =
                XIClearUnitCmd.class;
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_CLEAR_UNIT_ALT)] =
                XIClearUnitAltCmd.class;
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_RESTORE_SCREEN)] =
                XIRestoreScreenCmd.class;
        // Roll
        // This command allows the lines to be rolled up or down on the display as specified by the size parameter of the command.
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_ROLL)] =
                XIRollCmd.class;
        // Write error code
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_WRITE_ERROR_CODE)] =
                XIWriteErrorCodeCmd.class;
        // Wirte to display
        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_WRITE_TO_DISPLAY)] =
                XIWriteToDisplayCmd.class;

        cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_QUERY_DEVICE)] =
                XIQueryCmd.class;
    }

    protected List<XI5250Cmd> ivCmdVect;
    // flags used across command execution
    protected boolean ivICOrderExecuted;

    protected XI5250CmdList(XI5250Emulator aEmulator) {
        init(aEmulator);
    }

    /**
     * Parses the command list from the given input stream.
     *
     * @param inStream stream to read from.
     * @throws IOException     raised if there is some problem in communication with server.
     * @throws XI5250Exception raised if command parameters are wrong.
     */
    @Override
    protected void readFrom5250Stream(@NotNull InputStream inStream)
            throws IOException, XI5250Exception {
        int bb;
        XI5250Cmd cmd;
        ivCmdVect = new ArrayList<>(100);

        LOGGER.debug("START OF COMMANDS LIST");

        // jump until ESC is found
        while ((bb = inStream.read()) != -1 && (byte) bb != XI5250Emulator.ESC) {
        }

        // start from 1 because ESC is already read
        for (int i = 1; (bb = inStream.read()) != -1; i++) {
            switch (i % 2) {
                case 0:
                    if ((byte) bb != XI5250Emulator.ESC) {
                        throw new XI5250Exception("Malformed 5250 packet", XI5250Emulator.ERR_INVALID_COMMAND);
                    }
                    break;
                case 1:
                    cmd = createCmdInstance(XITelnet.toInt((byte) bb));

                    if (cmd != null) {
                        cmd.init(ivEmulator);
                        cmd.readFrom5250Stream(inStream);

                        LOGGER.debug("{}", cmd);

                        ivCmdVect.add(cmd);
                    } else {
                        throw new XI5250Exception("Command not supported : 0x" +
                                XITelnet.toHex((byte) bb),
                                XI5250Emulator.ERR_INVALID_COMMAND);
                    }
                    break;
            }
        }
    }

    @Override
    protected void execute() {
        for (XI5250Cmd anIvCmdVect : ivCmdVect) {
            anIvCmdVect.execute();
        }
    }

    /**
     * Creates the 5250 command related to the given 5250 command id.
     *
     * @param aCmd byte identification for the command.
     * @return created command.
     */
    protected @Nullable XI5250Cmd createCmdInstance(int aCmd) {
        Class<?> cls;

        cls = cv5250CmdClasses[aCmd];
        if (cls != null) {
            try {
                return (XI5250Cmd) cls.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public @NotNull String toString() {
        return super.toString() + ivCmdVect.toString();
    }

}
