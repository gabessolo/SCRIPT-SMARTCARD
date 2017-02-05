package main;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;

public class PIN extends Applet {
    /* Constantes */
    public static final byte CLA_MONAPPLET = (byte) 0xB0;

    public static final byte INS_VERIF_PIN = 0x00;
    public static final byte INS_PIN_REMAINING_TRIES = 0x01;
    public static final byte INS_PUK_REMAINING_TRIES = 0x02;
    public static final byte INS_UNLOCK_WITH_PUK = 0x03;
    private static final byte INS_GET_PIN = 0x04;
    private static final byte INS_GET_PUK = 0x05;

    private static OwnerPIN pin; 
    private static OwnerPIN puk;

    private boolean pin_retrieved;
    private boolean puk_retrieved;

    public static boolean test_PIN(byte[] PIN, short offset, short length)
    {
        return pin.check(PIN, offset, (byte) length);			
    }


    /* Constructeur */
    private PIN() {
        pin = new OwnerPIN((byte) 3, (byte) 2 );
        pin.update(new byte[]{15,12}, (short) 0, (byte) 2);
        puk = new OwnerPIN((byte) 3, (byte) 4);
        puk.update(new byte[]{15,12,45,124}, (short) 0, (byte) 4);
        pin_retrieved = false;
        puk_retrieved = false;
    }

    public static void install(byte bArray[], short bOffset, byte bLength) throws ISOException {
        new PIN().register();
    }


    public static short getState(){
        //récupération de l'état de la carte
        //si la carte est bloquée à cause de trop de codes PIN faux
        if(pin.getTriesRemaining() == 0)
        {
            return (short) 0x1235;
        }
        //si la carte est bloquée par le code PIN
        else if (!pin.isValidated())
        {
            return (short) 0x1234;
        }
        return (short) 0x9000;

    }


    public void process(APDU apdu) throws ISOException {
        byte[] buffer = apdu.getBuffer();

        if (this.selectingApplet()) return;

        if (buffer[ISO7816.OFFSET_CLA] != CLA_MONAPPLET) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_PIN_REMAINING_TRIES:
                //renvoie le nombres d'essais de code PIN restant
                buffer[0] = pin.getTriesRemaining();
                apdu.setOutgoingAndSend((short) 0, (short) 1);
                break;
                
            case INS_PUK_REMAINING_TRIES:
                //renvoie le nombres d'essais de code PIN restant
                buffer[0] = puk.getTriesRemaining();
                apdu.setOutgoingAndSend((short) 0, (short) 1);
                break;
                
            case INS_VERIF_PIN:
                //vérifie le PIN envoyé par le client
                //en cas d'erreur, le nombre d'essais restants est décrémenté automatiquement
                if(pin.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) buffer[ISO7816.OFFSET_P1]))
                {
                    buffer[0] = 1;
                }
                else
                {
                    buffer[0] = 0;
                }
                apdu.setOutgoingAndSend((short) 0, (short) 1);
                break;

            case INS_UNLOCK_WITH_PUK:
                //débloque le PUK 
                if(puk.check(buffer, (short) ISO7816.OFFSET_CDATA, (byte) buffer[ISO7816.OFFSET_P1]))
                {
                    pin.resetAndUnblock();
                    gen_random.genRandom(buffer, (short) 2);
                    pin.update(buffer, (short) 0, (byte) 2);
                    apdu.setOutgoingAndSend((short) 0, (short) 2);	
                }
                else
                {
                    gen_random.genRandom(buffer, (short) 2);
                    ISOException.throwIt((short) 0x0001);
                }

                break;

            case INS_GET_PIN:
                if (!pin_retrieved) {
                    try {
                        pin.resetAndUnblock();
                        gen_random.genRandom(buffer, (short) 2);
                        pin.update(buffer, (short) 0, (byte) 2);
                        apdu.setOutgoingAndSend((short) 0, (short) 2);
                        pin_retrieved = true;
                    } catch(Exception e) {
                        ISOException.throwIt((short) 0x01);
                    }
                }
                else {
                    gen_random.genRandom(buffer, (short) 2);
                    ISOException.throwIt((short) 0x02);
                }
                break;

            case INS_GET_PUK:
                if (!puk_retrieved) {
                    try {
                        puk.resetAndUnblock();
                        gen_random.genRandom(buffer, (short) 2);
                        puk.update(buffer, (short) 0, (byte) 2);
                        apdu.setOutgoingAndSend((short) 0, (short) 2);
                        puk_retrieved = true;
                    } catch(Exception e) {
                        ISOException.throwIt((short) 0x01);
                    }
                }
                else {
                    gen_random.genRandom(buffer, (short) 2);
                    ISOException.throwIt((short) 0x02);
                }
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
