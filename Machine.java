package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Ved Mistry
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        clearRotors();
        int movingCounter = 0;
        for (String j : rotors) {
            int rotorCounter = _rotors.size();
            for (Rotor i : _allRotors) {
                if (i.name().equals(j)) {
                    if (i.rotates()) {
                        movingCounter += 1;
                    }
                    _rotors.add(i);
                }
            }
            if (_rotors.size() == rotorCounter) {
                throw new EnigmaException("No rotor added");
            }
        }
        if (movingCounter > _pawls) {
            throw new EnigmaException("Too many M-Rotors");
        }
        if (!_rotors.get(0).reflecting()) {
            throw new EnigmaException("First rotor not reflector");
        }
        for (int i = 0; i < _rotors.size(); i++) {
            if (_rotors.get(i).reflecting() && i != 0) {
                throw new EnigmaException("Reflector wrong spot");
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 0; i < setting.length(); i++) {
            _rotors.get(i + 1).set(setting.charAt(i));
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        Boolean[] advanced = new Boolean[_rotors.size()];
        for (int j = 0; j < advanced.length; j++) {
            advanced[j] = false;
        }
        for (int i = 0; i < _rotors.size(); i++) {
            if ((i == _rotors.size() - 1)
                    || (_rotors.get(i + 1).atNotch()
                            && _rotors.get(i).rotates())) {
                advanced[i] = true;
                _rotors.get(i).advance();
            } else if (_rotors.get(i).atNotch() && advanced[i - 1]) {
                _rotors.get(i).advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _rotors.size() - 1; i >= 0; i--) {
            c = _rotors.get(i).convertForward(c);
        }
        for (int i = 1; i < _rotors.size(); i++) {
            c = _rotors.get(i).convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replaceAll("\\s", "");
        String word = "";
        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == ' ') {
                word = word + ' ';
            } else {
                String letter = "" + _alphabet.toChar(convert(
                        _alphabet.toInt(msg.charAt(i))));
                word = word + letter;
            }
        }
        return word;
    }

    /** Remove all rotors from machine. */
    void clearRotors() {
        _rotors = new ArrayList<>();
    }

    /** Number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** All available rotors. */
    private Collection<Rotor> _allRotors;

    /** Chosen rotors. */
    private ArrayList<Rotor> _rotors = new ArrayList<>();

    /** Plugboard permutation. */
    private Permutation _plugboard;

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

}
