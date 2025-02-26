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
!!V 15/07/97 rel. 1.02c- XIDataOrd includes 0x1F char.
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 5250 Orders list.
 *
 * @author Valentino Proietti - Infordata S.p.A.
 */
public class XI5250OrdList extends XI5250Ord {

    private static final Logger LOGGER = LoggerFactory.getLogger(XI5250OrdList.class);

    private static final Class<?> @NotNull [] cv5250OrdClasses = new Class<?>[256];

    static {
        // Insert cursor order
        // IC,14,19 @ IC is the Insert Cursor order (hex 13). 14 and 19 define the row and column where the host system wants the Model 2 or Model 12 to set the cursor. This is the location of the error.
        cv5250OrdClasses[XI5250Emulator.ORD_IC] = XIICOrd.class;
        // Repeat to Address (RA) Order
        // This order displays a character in every position starting from the current display address and going to the last position specified by this order. If these two addresses match, 1 character is displayed.
        cv5250OrdClasses[XI5250Emulator.ORD_RA] = XIRAOrd.class;
        // Set Buffer Address (SBA) Order
        // Read: Used as a delimiter between fields that are sent back to the host system in response to the Read MDT command. See the index entry read MDT fields command.
        // Write: Used to set the current display address and thereby determine where the data display or field definition begins. Two bytes that follow this order tell the 5251 Models 2 or 12 this information
        cv5250OrdClasses[XI5250Emulator.ORD_SBA] = XISBAOrd.class;
        // Start of Field (SF) Order
        // This order defines input and output fields. If an input field is being defined, it also resets any pending aid byte and locks the keyboard.
        cv5250OrdClasses[XI5250Emulator.ORD_SF] = XISFOrd.class;
        // Start of Header (SOH) Order
        // This order specifies the header information that goes into the format table. See the index entry format table. It also selects the resequencing function when data is read from the display. See the index entry field control word.
        cv5250OrdClasses[XI5250Emulator.ORD_SOH] = XISOHOrd.class;
        // MC - move cursor
        cv5250OrdClasses[XI5250Emulator.ORD_MC] = XIMCOrd.class;
        // EA - Erase to address
        cv5250OrdClasses[XI5250Emulator.ORD_EA] = XIEAOrd.class;
        // Transparent data
        cv5250OrdClasses[XI5250Emulator.ORD_TD] = XITDOrd.class;
        // WEA - Write extended attribute - not supported
        cv5250OrdClasses[XI5250Emulator.ORD_WEA] = XIWEAOrd.class;
        // Write to Display Structured Field - not supported
        cv5250OrdClasses[XI5250Emulator.ORD_WDSF] = XIWdsfOrd.class;
    }

    protected List<XI5250Ord> ivOrdVect;
    protected boolean @NotNull [] ivOrdPresent = new boolean[256];

    protected XI5250OrdList(XI5250Emulator aEmulator) {
        init(aEmulator);
    }

    public boolean isOrderPresent(byte aOrder) {
        return ivOrdPresent[aOrder];
    }

    /**
     * @param inStream the stream from where to read the order list from.
     * @throws XI5250Exception raised if order parameters are wrong.
     * @throws IOException     raised when there is an input/output problem.
     */
    @Override
    protected void readFrom5250Stream(@NotNull InputStream inStream)
            throws IOException, XI5250Exception {

        int bb;
        XI5250Ord ord;
        ivOrdVect = new ArrayList<>(100);

        LOGGER.debug("START OF ORDERS LIST");

        while (true) {
            inStream.mark(1);
            if ((bb = inStream.read()) == -1)
                break;

            if ((byte) bb == XI5250Emulator.ESC) {
                inStream.reset();
                break;
            }

            if (XIDataOrd.isDataCharacter(bb)) {
                inStream.reset();          // need it (it is also the color attribute)
                if (ivEmulator.isStrPcCmdEnabled()) {
                    inStream.mark(XI5250Emulator.STRPCCMD.length);
                    byte[] lhbb = new byte[XI5250Emulator.STRPCCMD.length];
                    int sz = inStream.read(lhbb);
                    if (sz == XI5250Emulator.STRPCCMD.length) {
                        if (Arrays.equals(lhbb, XI5250Emulator.STRPCCMD)) {
                            ivEmulator.receivedStrPcCmd();
                        } else if (Arrays.equals(lhbb, XI5250Emulator.ENDSTRPCCMD)) {
                            ivEmulator.receivedEndStrPcCmd();
                        } else {
                            inStream.reset();
                        }
                    } else {
                        inStream.reset();
                    }
                }
            } else
                ivOrdPresent[bb] = true;  // remember orders present

            try {
                ord = createOrdInstance(bb);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            if (ord != null) {
                ord.init(ivEmulator);
                ord.readFrom5250Stream(inStream);

                LOGGER.debug("{}", ord);

                ivOrdVect.add(ord);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Order not supported : 0x{}", XITelnet.toHex((byte) bb));
                    for (int ii = 0; ii < ivOrdVect.size(); ii++) {
                        LOGGER.debug("Prev. order[{}]: {}", ii, ivOrdVect.get(ii));
                    }
                    byte[] buf = new byte[10];
                    int count = inStream.read(buf);
                    LOGGER.debug("Next {} bytes: {}", count, XITelnet.toHex(buf, count));
                }
                throw new XI5250Exception("Order not supported : 0x" + XITelnet.toHex((byte) bb),
                        XI5250Emulator.ERR_INVALID_COMMAND);
            }
        }
    }

    @Override
    protected void execute() {
        for (XI5250Ord anIvOrdVect : ivOrdVect) {
            anIvOrdVect.execute();
        }
    }

    /**
     * Creates the 5250 order instance related to the given 5250 order id.
     *
     * @param aOrd order id to create
     * @return created order
     * @throws IllegalAccessException .
     * @throws InstantiationException .
     */
    public @Nullable XI5250Ord createOrdInstance(int aOrd)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Class<?> cls;

        if (XIDataOrd.isDataCharacter(aOrd))
            cls = XIDataOrd.class;
        else
            cls = cv5250OrdClasses[aOrd];

        if (cls != null)
            return (XI5250Ord) cls.getDeclaredConstructor(new Class[0]).newInstance();
        else
            return null;
    }

    @Override
    public @NotNull String toString() {
        return super.toString() + ivOrdVect.toString();
    }

}
