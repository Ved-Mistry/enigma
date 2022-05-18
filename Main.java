package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Ved Mistry
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine P = readConfig();
        if (!_input.hasNext("\\*")) {
            throw new EnigmaException("Settings not starting with *");
        }

        while (_input.hasNextLine() && _input.hasNext()) {
            String input = _input.nextLine();
            if (input.contains("*")) {
                setUp(P, input);
            } else {
                printMessageLine(P.convert(input));
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            _numRotors = _config.nextInt();
            _numPawls = _config.nextInt();
            ArrayList<Rotor> rotors = new ArrayList<>();
            while (_config.hasNextLine() && _config.hasNext()) {
                rotors.add(readRotor());
            }
            return new Machine(_alphabet, _numRotors, _numPawls, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String description = _config.next();
            String rotation = description.substring(0, 1);
            String notches = description.substring(1);
            String cycles = "";

            while (_config.hasNext(".*[\\\\(|\\\\)]+.*")) {
                cycles = cycles + _config.next() + " ";
            }

            if (cycles.length() > 0
                    && cycles.charAt((cycles.length() - 2)) != ')') {
                throw new EnigmaException(
                        "Permutation not closed with parenthesis"
                );
            }

            Permutation permutation = new Permutation(cycles, _alphabet);
            if (rotation.charAt(0) == 'M') {
                return new MovingRotor(name, permutation, notches);
            }
            if (rotation.charAt(0) == 'R') {
                return new Reflector(name, permutation);
            } else {
                return new FixedRotor(name, permutation);
            }
        } catch (NoSuchElementException excp) {

            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] chosenRotors = new String[M.numRotors()];
        Scanner settingsScanned = new Scanner(settings);
        settingsScanned.next();
        for (int i = 0; i < chosenRotors.length; i++) {
            chosenRotors[i] = settingsScanned.next();
        }
        M.insertRotors(chosenRotors);
        String toSet = "";
        if (settingsScanned.hasNext()) {
            toSet = settingsScanned.next();
        }
        M.setRotors(toSet);
        String plugboardSettings = "";
        while (settingsScanned.hasNext()) {
            plugboardSettings += settingsScanned.next();
        }
        Permutation plugboard = new Permutation(plugboardSettings, _alphabet);
        M.setPlugboard(plugboard);
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 1; i <= Math.floorDiv(msg.length(), 5); i++) {
            _output.print(msg.substring(5 * i - 5, 5 * i) + " ");
        }
        _output.print(msg.substring(msg.length() - (msg.length() % 5)));
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** Number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _numPawls;
}
