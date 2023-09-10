package reactionsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.addAll;

/**
 *
 * @author caba
 */
public class BioResolve {

    static final boolean DEBUG = true;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
    }
    // TODO: when a nil is encountered, stop the computation (as it can further react just by using Di) <--- check
    // TODO: Maybe cache results for recursive self-call
    // TODO: Add normal strategy instead of random choice?
    // TODO: test environment multiple vars with recursive calls between them;
}
