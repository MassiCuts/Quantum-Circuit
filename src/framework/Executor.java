package framework;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import preferences.AppPreferences;

public class Executor {

    static final String quilTemplate =
            "from pyquil.parser import parse_program\n" +
                    "from pyquil.api import QVMConnection\n" +
                    "qvm = QVMConnection()\n" +
                    "p = parse_program(\"\"\"CODE\"\"\")\n" +
                    "print(qvm.wavefunction(p).amplitudes)\n";

    static final String qasmTemplate =
            "import qiskit\n" +
                    "qp = qiskit.QuantumProgram()\n" +
                    "name = \"test\"\n" +
                    "qp.load_qasm_file(\"test.qasm\",name=name)\n" +
                    "if __name__ == \"__main__\":\n" +
                    "   ret = qp.execute([name])\n" +
                    "   print(ret.get_counts(name))";

    private static void fixFile(File src) throws IOException {
        if(!src.exists()) {
            src.createNewFile();
        } else {
            src.delete();
            src.createNewFile();
        }
    }

    public static String runQuil(String code) throws IOException {
        File src = new File("temp.py");
        fixFile(src);
        FileWriter fw = new FileWriter(src);
        fw.write(quilTemplate.replace("CODE",code));
        fw.close();
        String interpretorLocation = AppPreferences.get("Python", "Interpreter Location");
        Process p = Runtime.getRuntime().exec(interpretorLocation + " temp.py");
        BufferedReader isr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader isr1 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String res = isr.lines().reduce("",(x,y)-> x+"\n"+y);
        System.out.println(res);
        isr.close();
        return res;
    }

    public static String runQASM(String code) throws IOException {
        File qsrc = new File("test.qasm");
        fixFile(qsrc);
        File src = new File("temp.py");
        fixFile(src);
        FileWriter fw = new FileWriter(qsrc);
        fw.write(code);
        fw.close();
        fw = new FileWriter(src);
        fw.write(qasmTemplate);
        fw.close();
        Process p = Runtime.getRuntime().exec(new String[]{"python temp.py"});
        BufferedReader isr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String res = isr.lines().reduce("",(x,y)-> x+"\n"+y);
        System.out.println(res);
        isr.close();
        return res;
    }

}
