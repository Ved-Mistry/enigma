package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Ved Mistry
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _name = name;
        _perm = perm;
        _notches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        return notches().contains("" + alphabet().toChar(setting()));
    }

    @Override
    String notches() {
        return _notches;
    }

    /** Name of rotor. */
    private String _name;

    /** Permutation of rotor. */
    private Permutation _perm;

    /** Notches of rotor. */
    private String _notches;

}
