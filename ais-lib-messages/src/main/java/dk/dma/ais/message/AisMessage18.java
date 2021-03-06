/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.message;

import dk.dma.ais.binary.BinArray;
import dk.dma.ais.binary.SixbitEncoder;
import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.sentence.Vdm;
import dk.dma.enav.model.geometry.Position;

/**
 * AIS message 18
 * 
 * CLASS B position report implemented according to ITU-R M.1371-4
 * 
 */
public class AisMessage18 extends AisMessage implements IVesselPositionMessage {

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Not used. Should be set to zero. Reserved for future use */
    private int spareAfterUserId; // 8 bits

    /**
     * Speed over ground in 1/10 knot steps (0-102.2 knots) 1023 = not available, 1022 = 102.2 knots or higher
     */
    private int sog; // 10 bits

    /**
     * AisPosition Accuracy 1 = high ( =< 10 m) 0 = low (>10 m) 0 = default The PA flag should be determined in
     * accordance with Table 47
     */
    private int posAcc; // 1 bit

    /** Store the positions just as in message 1-3 */
    private AisPosition pos; // : Lat/Long 1/10000 minute

    /**
     * Course over ground in 1/10 = (0-3599). 3600 (E10h) = not available = default; 3601-4095 should not be used
     */
    private int cog; // 12 bits

    /**
     * True heading Degrees (0-359) (511 indicates not available = default)
     */
    private int trueHeading; // 9 bits

    /**
     * Time stamp: UTC second when the report was generated by the EPFS (0-59 or 60 if time stamp is not available,
     * which should also be the default value or 61 if positioning system is in manual input mode or 62 if electronic
     * position fixing system operates in estimated (dead reckoning) mode or 63 if the positioning system is
     * inoperative) 61, 62, 63 are not used by CS AIS
     */
    private int utcSec; // 6 bits : UTC Seconds

    /**
     * Not used. Should be set to zero. Reserved for future use
     */
    private int spare; // 2 bits

    /**
     * Class B unit flag: 0 = Class B SOTDMA unit 1 = Class B CS unit
     */
    private int classBUnitFlag; // 1 bit

    /**
     * Class B display flag: 0 = No display available; not capable of displaying Message 12 and 14 1 = Equipped with
     * integrated display displaying Message 12 and 14
     */
    private int classBDisplayFlag; // 1 bit

    /**
     * Class B DSC flag: 0 = Not equipped with DSC function 1 = Equipped with DSC function (dedicated or time-shared)
     */
    private int classBDscFlag; // 1 bit

    /**
     * Class B band flag: 0 = Capable of operating over the upper 525 kHz band of the marine band 1 = Capable of
     * operating over the whole marine band (irrelevant if Class B Message 22 flag is 0)
     */
    private int classBBandFlag; // 1 bit

    /**
     * 0 = No frequency management via Message 22 , operating on AIS1, AIS2 only 1 = Frequency management via Message 22
     */
    private int classBMsg22Flag; // 1 bit

    /**
     * Mode flag: 0 = Station operating in autonomous and continuous mode = default 1 = Station operating in assigned
     * mode
     */
    private int modeFlag; // 1 bit

    /**
     * RAIM-flag: RAIM (Receiver autonomous integrity monitoring) flag of electronic position fixing device; 0 = RAIM
     * not in use = default; 1 = RAIM in use see Table 47
     */
    private int raim; // 1 bit

    /**
     * Communication state selector flag: 0 = SOTDMA communication state follows 1 = ITDMA communication state follows
     * (always 1 for Class-B CS)
     */
    private int commStateSelectorFlag; // 1 bit

    /**
     * Communication state: SOTDMA communication state (see 3.3.7.2.1, Annex 2), if communication state selector flag is
     * set to 0, or ITDMA communication state (see 3.3.7.3.2, Annex 2), if communication state selector flag is set to 1
     * Because Class B CS does not use any Communication State information, this field should be filled with the
     * following value: 1100000000000000110
     */
    private int commState; // 19 bits : SOTDMA sync state

    public AisMessage18() {
        super(18);
    }

    public AisMessage18(Vdm vdm) throws AisMessageException, SixbitException {
        super(vdm);
        this.parse();
    }

    public void parse() throws AisMessageException, SixbitException {
        BinArray sixbit = vdm.getBinArray();
        if (sixbit.getLength() != 168) {
            throw new AisMessageException("Message 18 wrong length " + sixbit.getLength());
        }

        super.parse(sixbit);

        this.spareAfterUserId = (int) sixbit.getVal(8);
        this.sog = (int) sixbit.getVal(10);
        this.posAcc = (int) sixbit.getVal(1);
        // Extract position
        this.pos = new AisPosition();
        this.pos.setRawLongitude(sixbit.getVal(28));
        this.pos.setRawLatitude(sixbit.getVal(27));

        this.cog = (int) sixbit.getVal(12);
        this.trueHeading = (int) sixbit.getVal(9);
        this.utcSec = (int) sixbit.getVal(6);
        this.spare = (int) sixbit.getVal(2);

        // Extract class B flags
        this.classBUnitFlag = (int) sixbit.getVal(1);
        this.classBDisplayFlag = (int) sixbit.getVal(1);
        this.classBDscFlag = (int) sixbit.getVal(1);
        this.classBBandFlag = (int) sixbit.getVal(1);
        this.classBMsg22Flag = (int) sixbit.getVal(1);
        this.modeFlag = (int) sixbit.getVal(1);

        // Raim and communication
        this.raim = (int) sixbit.getVal(1);
        this.commStateSelectorFlag = (int) sixbit.getVal(1);
        this.commState = (int) sixbit.getVal(19);
    }

    @Override
    public SixbitEncoder getEncoded() {
        SixbitEncoder encoder = super.encode();
        encoder.addVal(spareAfterUserId, 8);
        encoder.addVal(sog, 10);
        encoder.addVal(posAcc, 1);
        encoder.addVal(pos.getRawLongitude(), 28);
        encoder.addVal(pos.getRawLatitude(), 27);
        encoder.addVal(cog, 12);
        encoder.addVal(trueHeading, 9);
        encoder.addVal(utcSec, 6);
        encoder.addVal(spare, 2);
        encoder.addVal(classBUnitFlag, 1);
        encoder.addVal(classBDisplayFlag, 1);
        encoder.addVal(classBDscFlag, 1);
        encoder.addVal(classBBandFlag, 1);
        encoder.addVal(classBMsg22Flag, 1);
        encoder.addVal(modeFlag, 1);
        encoder.addVal(raim, 1);
        encoder.addVal(commStateSelectorFlag, 1);
        encoder.addVal(commState, 19);
        return encoder;
    }

    /**
     * @return the spareAfterUserId
     */
    public int getSpareAfterUserId() {
        return spareAfterUserId;
    }

    /**
     * @param spareAfterUserId
     *            the spareAfterUserId to set
     */
    public void setSpareAfterUserId(int spareAfterUserId) {
        this.spareAfterUserId = spareAfterUserId;
    }

    /**
     * @return the sog
     */
    public int getSog() {
        return sog;
    }

    /**
     * @param sog
     *            the sog to set
     */
    public void setSog(int sog) {
        this.sog = sog;
    }

    /**
     * @return the posAcc
     */
    public int getPosAcc() {
        return posAcc;
    }

    /**
     * @param posAcc
     *            the posAcc to set
     */
    public void setPosAcc(int posAcc) {
        this.posAcc = posAcc;
    }

    /**
     * @return the pos
     */
    public AisPosition getPos() {
        return pos;
    }

    @Override
    public Position getValidPosition() {
        AisPosition pos = this.pos;
        return pos == null ? null : pos.getGeoLocation();
    }

    /**
     * @param pos
     *            the pos to set
     */
    public void setPos(AisPosition pos) {
        this.pos = pos;
    }

    /**
     * @return the cog
     */
    public int getCog() {
        return cog;
    }

    /**
     * @param cog
     *            the cog to set
     */
    public void setCog(int cog) {
        this.cog = cog;
    }

    /**
     * @return the trueHeading
     */
    public int getTrueHeading() {
        return trueHeading;
    }

    /**
     * @param trueHeading
     *            the trueHeading to set
     */
    public void setTrueHeading(int trueHeading) {
        this.trueHeading = trueHeading;
    }

    /**
     * @return the utcSec
     */
    public int getUtcSec() {
        return utcSec;
    }

    /**
     * @param utcSec
     *            the utcSec to set
     */
    public void setUtcSec(int utcSec) {
        this.utcSec = utcSec;
    }

    /**
     * @return the spare
     */
    public int getSpare() {
        return spare;
    }

    /**
     * @param spare
     *            the spare to set
     */
    public void setSpare(int spare) {
        this.spare = spare;
    }

    /**
     * @return the classBUnitFlag
     */
    public int getClassBUnitFlag() {
        return classBUnitFlag;
    }

    /**
     * @param classBUnitFlag
     *            the classBUnitFlag to set
     */
    public void setClassBUnitFlag(int classBUnitFlag) {
        this.classBUnitFlag = classBUnitFlag;
    }

    /**
     * @return the classBDisplayFlag
     */
    public int getClassBDisplayFlag() {
        return classBDisplayFlag;
    }

    /**
     * @param classBDisplayFlag
     *            the classBDisplayFlag to set
     */
    public void setClassBDisplayFlag(int classBDisplayFlag) {
        this.classBDisplayFlag = classBDisplayFlag;
    }

    /**
     * @return the classBDscFlag
     */
    public int getClassBDscFlag() {
        return classBDscFlag;
    }

    /**
     * @param classBDscFlag
     *            the classBDscFlag to set
     */
    public void setClassBDscFlag(int classBDscFlag) {
        this.classBDscFlag = classBDscFlag;
    }

    /**
     * @return the classBBandFlag
     */
    public int getClassBBandFlag() {
        return classBBandFlag;
    }

    /**
     * @param classBBandFlag
     *            the classBBandFlag to set
     */
    public void setClassBBandFlag(int classBBandFlag) {
        this.classBBandFlag = classBBandFlag;
    }

    /**
     * @return the classBMsg22Flag
     */
    public int getClassBMsg22Flag() {
        return classBMsg22Flag;
    }

    /**
     * @param classBMsg22Flag
     *            the classBMsg22Flag to set
     */
    public void setClassBMsg22Flag(int classBMsg22Flag) {
        this.classBMsg22Flag = classBMsg22Flag;
    }

    /**
     * @return the modeFlag
     */
    public int getModeFlag() {
        return modeFlag;
    }

    /**
     * @param modeFlag
     *            the modeFlag to set
     */
    public void setModeFlag(int modeFlag) {
        this.modeFlag = modeFlag;
    }

    /**
     * @return the raimFlag
     */
    public int getRaim() {
        return raim;
    }

    /**
     * @param raimFlag
     *            the raimFlag to set
     */
    public void setRaim(int raim) {
        this.raim = raim;
    }

    /**
     * @return the commStateSelectorFlag
     */
    public int getCommStateSelectorFlag() {
        return commStateSelectorFlag;
    }

    /**
     * @param commStateSelectorFlag
     *            the commStateSelectorFlag to set
     */
    public void setCommStateSelectorFlag(int commStateSelectorFlag) {
        this.commStateSelectorFlag = commStateSelectorFlag;
    }

    /**
     * @return the commState
     */
    public int getCommState() {
        return commState;
    }

    /**
     * @param commState
     *            the commState to set
     */
    public void setCommState(int commState) {
        this.commState = commState;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(", classBBandFlag=");
        builder.append(classBBandFlag);
        builder.append(", classBDisplayFlag=");
        builder.append(classBDisplayFlag);
        builder.append(", classBDscFlag=");
        builder.append(classBDscFlag);
        builder.append(", classBMsg22Flag=");
        builder.append(classBMsg22Flag);
        builder.append(", classBUnitFlag=");
        builder.append(classBUnitFlag);
        builder.append(", cog=");
        builder.append(cog);
        builder.append(", commState=");
        builder.append(commState);
        builder.append(", commStateSelectorFlag=");
        builder.append(commStateSelectorFlag);
        builder.append(", modeFlag=");
        builder.append(modeFlag);
        builder.append(", pos=");
        builder.append(pos);
        builder.append(", posAcc=");
        builder.append(posAcc);
        builder.append(", raim=");
        builder.append(raim);
        builder.append(", sog=");
        builder.append(sog);
        builder.append(", spare=");
        builder.append(spare);
        builder.append(", spareAfterUserId=");
        builder.append(spareAfterUserId);
        builder.append(", trueHeading=");
        builder.append(trueHeading);
        builder.append(", utcSec=");
        builder.append(utcSec);
        builder.append("]");
        return builder.toString();
    }

    public boolean isPositionValid() {
        return pos.getGeoLocation() != null;
    }

    public boolean isCogValid() {
        return cog < 3600;
    }

    public boolean isSogValid() {
        return sog < 1023;
    }

    public boolean isHeadingValid() {
        return trueHeading < 360;
    }

}
